package com.bamf.settings.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.Notification.Notifications;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

public class SettingsProvider extends ContentProvider {
	
	private static final String TAG = SettingsProvider.class.getSimpleName();

	public static final String AUTHORITY = "com.bamf.settings.SettingsContent";
	
	private static final String DB_PATH = "/data/data/com.bamf.settings/databases";
    private static final String DATABASE_NAME = "BAMFSettings.db";
    
    // Any changes to the database format *must* include update-in-place code.
    // Original version: 1
    public static final int DATABASE_VERSION = 8;
    
    private static final String BACKUP_TABLE_NAME = Notifications.TABLE_NAME + "_backup";
    
    // Any changes to the database format *must* include update-in-place code.
    // Original version: 1
    private static final int NOTIFICATION_BASE = 0;
    private static final int NOTIFICATION = NOTIFICATION_BASE;
    private static final int NOTIFICATION_ID = NOTIFICATION_BASE + 1;
    private static final int PACKAGE_NAME = NOTIFICATION_BASE + 2;
    
    private static final int BASE_SHIFT = 2;  // 1 bit to the base type: 0, 0x1000, 0x2000, etc.
    
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	private static final boolean DEBUG = false;
	
	private static final String[] TABLE_NAMES = {
        Notifications.TABLE_NAME
    };
    
    static {
        // Settings URI matching table
        UriMatcher matcher = sURIMatcher;

        // All packages for notification wake
        matcher.addURI(AUTHORITY, "notifications", NOTIFICATION);
        // A single package using the table id
        matcher.addURI(AUTHORITY, "notifications/#", NOTIFICATION_ID);
        // A single package using the package name
        // TODO: Not Yet Implemented
        matcher.addURI(AUTHORITY, "notifications/package/#", PACKAGE_NAME);

    }
    
    /**
     * Wrap the UriMatcher call so we can throw a runtime exception if an unknown Uri is passed in
     * @param uri the Uri to match
     * @return the match value
     */
    private static int findMatch(Uri uri, String methodName) {
        int match = sURIMatcher.match(uri);
        if (match < 0) {
            throw new IllegalArgumentException("Unknown uri: " + uri);
        } else if (DEBUG) {
            Log.v(TAG, methodName + ": uri=" + uri + ", match is " + match);
        }
        return match;
    }
    
    /*
     * Internal helper method for index creation.
     * Example:
     * "create index message_" + MyColumns.FLAG_READ
     * + " on " + MyTable.TABLE_NAME + " (" + MyColumns.FLAG_READ + ");"
     */
    /* package */
    static String createIndex(String tableName, String columnName) {
        return "create index " + tableName.toLowerCase() + '_' + columnName
            + " on " + tableName + " (" + columnName + ");";
    }
    
    static void createNotificationTable(SQLiteDatabase db){
    	createNotificationTable(db, Notifications.TABLE_NAME);
    }
    
    static void createNotificationTable(SQLiteDatabase db, String name) {
        String notificationColumns = Notifications.PACKAGE_NAME + " text unique, "
        		+ Notifications.NOTIFICATION_ENABLED + " boolean default true, "
        		+ Notifications.NOTIFICATION_HIDE + " boolean default false, "
        		+ Notifications.BACKGROUND_COLOR + " integer default 0, "
        		+ Notifications.LED_COLOR + " integer default 0, "
        		+ Notifications.LED_OFF_MS + " integer default 100, "
        		+ Notifications.LED_ON_MS + " integer default 200, "
        		+ Notifications.WAKE_LOCK_TIME + " long default 0, "
        		+ Notifications.NOTIFICATION_SOUND + " text, "
        		+ Notifications.VIBRATE_PATTERN + " integer default 0, "
        		+ Notifications.FILTERS + " text, "
        		+ Notifications.NOTIFICATION_NOTIFY_ONCE + " boolean default false"
        		+ ");";
        		      		
		// This String and the following String MUST have the same columns, except for the type
        // of those columns!
        String createString = " (" + Notifications._ID + " integer primary key autoincrement, "
            + notificationColumns;
        
        // create the table
        db.execSQL("create table " + name + createString);
        
        String indexColumns[] = {
                Notifications.PACKAGE_NAME
        };

        for (String columnName : indexColumns) {
            db.execSQL(createIndex(name, columnName));
        }
    }
    
    static void resetNotificationTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("drop table " + Notifications.TABLE_NAME);
        } catch (SQLException e) {
        }
        createNotificationTable(db);
    }
    
    /**
     * Just delete the whole database when the user requests a reset
     * @hide
     */
    public static boolean resetTables(){
    	boolean result = false;
    	try{
	    	// Local database
	    	File from = new File(DB_PATH, DATABASE_NAME);
	    	if(from.canRead() && from.exists()){
	    		result = from.delete();
	    	}
    	}catch(Exception e){}
    	return result;
    }
    
    private SQLiteDatabase mDatabase;
    
    //@VisibleForTesting
    synchronized SQLiteDatabase getDatabase(Context context) {
        // Always return the cached database, if we've got one
        if (mDatabase != null) {
        	try{
        		mDatabase.getVersion();
        		deleteUninstalledPackages(context, mDatabase, Notifications.TABLE_NAME);
                return mDatabase;
        	}catch(Exception e){
        		mDatabase = null;
        	}
        }

        DatabaseHelper helper = new DatabaseHelper(context, DATABASE_NAME);
        mDatabase = helper.getWritableDatabase();

        if (DEBUG) {
            Log.d(TAG, "Deleting uninstalled packages...");
        }
        // Check for any uninstalled packages in the notifications table
        // this may be used as a feature instead of a requirement at a later date
        deleteUninstalledPackages(context, mDatabase, Notifications.TABLE_NAME);

        if (DEBUG) {
            Log.d(TAG, "SettingsProvider ready.");
        }
        return mDatabase;
    }
    
    /*package*/ static void deleteUninstalledPackages(Context context, SQLiteDatabase database, String tableName) {
        if (database != null) {
        	//look through the table and remove entries for uninstalled packages
        	final PackageManager pm = context.getPackageManager();
        	final List<PackageInfo> list = pm.getInstalledPackages(0);
        	final ArrayList<String> packageNames = new ArrayList<String>();
        	
        	for(int x=0;x<list.size();x++){
        		packageNames.add(list.get(x).packageName);
        	}
        	
        	final String[] columns = new String[]{Notifications.PACKAGE_NAME};
        	final ArrayList<String> uninstalled = new ArrayList<String>();
        	final Cursor c = database.query(tableName, columns, null, null, null, null, null);
        	
    		while(c.moveToNext()){
    			String value = c.getString(c.getColumnIndexOrThrow(Notifications.PACKAGE_NAME));
    			if(!packageNames.contains(value)){
    				uninstalled.add(value);
    			}
    		}
        	c.close();
        	
        	if(uninstalled.size()>0){
        		database.beginTransaction();
        		final Iterator<String> it = uninstalled.iterator();
        		while(it.hasNext()){
        			database.delete(tableName, Notifications.PACKAGE_NAME + "=?", new String[]{it.next()});
        		}
        		database.setTransactionSuccessful();
        		database.endTransaction();
        	}
        }
    }
    
    @Override
    public Bundle call(String tableName, String request, Bundle args){
    	Bundle value = null;
    	
    	if(Notifications.TABLE_NAME.equals(tableName)){
    		Context context = getContext();
    		Cursor c = null;
    		
    		try{
	    		SQLiteDatabase db = getDatabase(context);
	    		c = db.query(tableName, null, Notifications.PACKAGE_NAME+"=?", 
	    				new String[]{request}, null, null, null);
	    		
	    		if(!c.moveToFirst())return value;
	    		
	    		value = new Bundle();
	    		value.putString(Notifications.PACKAGE_NAME, request);
	        	value.putBoolean(Notifications.NOTIFICATION_ENABLED, c.getInt(c.getColumnIndex(Notifications.NOTIFICATION_ENABLED))==1);
	        	value.putBoolean(Notifications.NOTIFICATION_HIDE, c.getInt(c.getColumnIndex(Notifications.NOTIFICATION_HIDE))==1);
	        	value.putString(Notifications.NOTIFICATION_SOUND, c.getString(c.getColumnIndex(Notifications.NOTIFICATION_SOUND)));
	        	value.putInt(Notifications.VIBRATE_PATTERN, c.getInt(c.getColumnIndex(Notifications.VIBRATE_PATTERN)));
	        	value.putString(Notifications.FILTERS, c.getString(c.getColumnIndex(Notifications.FILTERS)));
	        	value.putInt(Notifications.LED_COLOR, c.getInt(c.getColumnIndex(Notifications.LED_COLOR)));
	        	value.putInt(Notifications.LED_ON_MS, c.getInt(c.getColumnIndex(Notifications.LED_ON_MS)));
	        	value.putInt(Notifications.LED_OFF_MS, c.getInt(c.getColumnIndex(Notifications.LED_OFF_MS)));
	        	value.putInt(Notifications.BACKGROUND_COLOR, c.getInt(c.getColumnIndex(Notifications.BACKGROUND_COLOR)));
	        	value.putInt(Notifications.WAKE_LOCK_TIME, c.getInt(c.getColumnIndex(Notifications.WAKE_LOCK_TIME)));
	        	value.putBoolean(Notifications.NOTIFICATION_NOTIFY_ONCE, c.getInt(c.getColumnIndex(Notifications.NOTIFICATION_NOTIFY_ONCE))==1);

    		}catch(Exception e){
    			e.printStackTrace();
    		}
    		finally{
    			if(c!=null)c.close();
    		}
    	}else if(tableName.equals("clearCache")){
    		mDatabase = null;
    	}
    	
    	return value;
    }
    
    private String whereWithId(String id, String selection) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("_id=");
        sb.append(id);
        if (selection != null) {
            sb.append(" AND (");
            sb.append(selection);
            sb.append(')');
        }
        return sb.toString();
    }

    /**
     * Combine a locally-generated selection with a user-provided selection
     *
     * This introduces risk that the local selection might insert incorrect chars
     * into the SQL, so use caution.
     *
     * @param where locally-generated selection, must not be null
     * @param selection user-provided selection, may be null
     * @return a single selection string
     */
    private String whereWith(String where, String selection) {
        if (selection == null) {
            return where;
        }
        StringBuilder sb = new StringBuilder(where);
        sb.append(" AND (");
        sb.append(selection);
        sb.append(')');

        return sb.toString();
    }
    
    /*package*/ static SQLiteDatabase getReadableDatabase(Context context) {
        DatabaseHelper helper = new DatabaseHelper(context, DATABASE_NAME);
        return helper.getReadableDatabase();
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {
    	Context mContext;
    	
        DatabaseHelper(Context context, String name) {
            super(context, name, null, DATABASE_VERSION);
            // may need this at a later point
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create all tables here; each class has its own method
            Log.d(TAG, "Creating Notification database");
            createNotificationTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	if (oldVersion == 4) {
                upgradeFromVersion4ToVersion5(db);
                oldVersion = 5;
            }
        	if (oldVersion == 5) {
                upgradeFromVersion5ToVersion6(db);
                oldVersion = 6;
            }
        	if (oldVersion == 6) {
        		resetNotificationTable(db, oldVersion, newVersion);
                oldVersion = 7;
        	}
        	if (oldVersion == 7) {
        		upgradeFromVersion7ToVersion8(db);
                oldVersion = 8;
        	}else
            	resetNotificationTable(db, oldVersion, newVersion);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
        }
    }

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor c = null;
        int match;
        try {
            match = findMatch(uri, "query");
        } catch (IllegalArgumentException e) {
        	throw e;
        }
        
        Context context = getContext();
        
        if (context.checkCallingOrSelfPermission("com.bamf.ics.permission.READ_SETTINGS")
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG,"Permission Denial: can't query SettingsProvider from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
            return null;
        }

        SQLiteDatabase db = getDatabase(context);
        int table = match >> BASE_SHIFT;
        
        String tableName = TABLE_NAMES[table];
        
        try {
            switch (match) {
            	case NOTIFICATION:
            		c = db.query(tableName, projection,
                            selection, selectionArgs, null, null, sortOrder);
            		break;
            	case NOTIFICATION_ID:
            		List<String> pathSegments = uri.getPathSegments();
                    String id = pathSegments.get(1);
                    c = db.query(tableName, projection,
                    		whereWithId(id, selection), 
                    		selectionArgs, null, null, sortOrder);
                    break;
            	default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } catch (SQLiteException e) {
            throw e;
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (c == null) {
                // This should never happen, but let's be sure to log it...
                Log.e(TAG, "Query returning null for uri: " + uri + ", selection: " + selection);
            }
        }
        
        if ((c != null) && !isTemporary()) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		int match = findMatch(uri, "getType");
        switch (match) {
	        case NOTIFICATION:
	        	return "vnd.android.cursor.dir/bamf-notification";
	        case NOTIFICATION_ID:
	        	return "vnd.android.cursor.item/bamf-notification";
        	default:
        		throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int match = findMatch(uri, "insert");
        Context context = getContext();
        
        if (context.checkCallingOrSelfPermission("com.bamf.ics.permission.READ_SETTINGS")
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG,"Permission Denial: can't insert into SettingsProvider from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
            return null;
        }
        
        ContentResolver resolver = context.getContentResolver();

        SQLiteDatabase db = getDatabase(context);
        int table = match >> BASE_SHIFT;

        long longId;
        
		longId = db.insert(TABLE_NAMES[table], "foo", values);
		Uri resultUri = ContentUris.withAppendedId(uri, longId);
		
		// Notify all existing cursors.
        resolver.notifyChange(Notifications.CONTENT_URI, null);
        return resultUri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final int match = findMatch(uri, "delete");
        Context context = getContext();
        
        if (context.checkCallingOrSelfPermission("com.bamf.ics.permission.READ_SETTINGS")
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG,"Permission Denial: can't delete data in SettingsProvider from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
            return 0;
        }
        
        SQLiteDatabase db = getDatabase(context);
        int table = match >> BASE_SHIFT;
        
        boolean rowDeletion = false;
        String id = "0";
        ContentResolver resolver = context.getContentResolver();
        String tableName = TABLE_NAMES[table];
        int result = -1;
        
        try {
            switch (match) {
            	case NOTIFICATION:
            		rowDeletion = true;
                    db.beginTransaction();
                    
                    result = db.delete(tableName, selection, selectionArgs);
                    break;
            	case NOTIFICATION_ID:
            		rowDeletion = true;
                    db.beginTransaction();
                    
                    id = uri.getPathSegments().get(1);
                    result = db.delete(tableName, whereWithId(id, selection), selectionArgs);
                    break;
            	default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
            
            if (rowDeletion) {
                db.setTransactionSuccessful();
            }
        } catch (SQLiteException e) {
            throw e;
        } finally {
            if (rowDeletion) {
            	Settings.System.putInt(resolver, Settings.System.NOTIFICATIONS_DIRTY, 1);
                db.endTransaction();
            }
        }
        
        // Notify all content cursors
		resolver.notifyChange(Notifications.CONTENT_URI, null);
        return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		final int match = findMatch(uri, "update");
        Context context = getContext();
        
        if (context.checkCallingOrSelfPermission("com.bamf.ics.permission.READ_SETTINGS")
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG,"Permission Denial: can't update data in SettingsProvider from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
            return 0;
        }
        
        SQLiteDatabase db = getDatabase(context);
        int table = match >> BASE_SHIFT;
        
        boolean rowUpdate = false;
        String id = "0";
        ContentResolver resolver = context.getContentResolver();
        String tableName = TABLE_NAMES[table];
        int result = -1;
        
        try {
            switch (match) {
            	case NOTIFICATION:
            		rowUpdate = true;
                    db.beginTransaction();
                    
                    result = db.update(tableName, values, selection, selectionArgs);
                    break;
            	case NOTIFICATION_ID:
            		rowUpdate = true;
                    db.beginTransaction();
                    
                    id = uri.getPathSegments().get(1);
                    result = db.update(tableName, values, whereWithId(id, selection), selectionArgs);
                    break;
            	default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
            
            if (rowUpdate) {
                db.setTransactionSuccessful();
            }
        } catch (SQLiteException e) {
            throw e;
        } finally {
            if (rowUpdate) {
            	Settings.System.putInt(resolver, Settings.System.NOTIFICATIONS_DIRTY, 1);
                db.endTransaction();
            }
        }
        
        // Notify all content cursors
		resolver.notifyChange(Notifications.CONTENT_URI, null);
        return result;
	}
	
	/** Upgrades the database from v4 to v5 */
    private static void upgradeFromVersion4ToVersion5(SQLiteDatabase db) {
        try {
            db.execSQL("alter table " + Notifications.TABLE_NAME
                    + " add column " + Notifications.FILTERS + " text;");
        } catch (SQLException e) {
            // Shouldn't be needed unless we're debugging and interrupt the process
            Log.w(TAG, "Exception upgrading BAMFSettings.db from 4 to 5 " + e);
        }
    }
    
    /** Upgrades the database from v5 to v6 */
    private static void upgradeFromVersion5ToVersion6(SQLiteDatabase db) {
        try {
            db.execSQL("alter table " + Notifications.TABLE_NAME
                    + " add column " + Notifications.LED_OFF_MS + " integer default 100;");
            db.execSQL("alter table " + Notifications.TABLE_NAME
                    + " add column " + Notifications.LED_ON_MS + " integer default 200;");
        } catch (SQLException e) {
            // Shouldn't be needed unless we're debugging and interrupt the process
            Log.w(TAG, "Exception upgrading BAMFSettings.db from 5 to 6 " + e);
        }
    }
    
    /** Upgrades the database from v7 to v8 */
    private static void upgradeFromVersion7ToVersion8(SQLiteDatabase db) {
    	try{
    		db.execSQL("alter table " + Notifications.TABLE_NAME
    				+ " add column " + Notifications.NOTIFICATION_NOTIFY_ONCE + " boolean default false;");
    	}catch(Exception e){
    		Log.w(TAG, "Exception upgrading BAMFSettings.db from 7 to 8 " + e);
    	}
    	
    }

}
