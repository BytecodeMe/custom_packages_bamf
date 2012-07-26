package com.bamf.settings.preferences;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.Notification;
import android.app.Notification.Notifications;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bamf.settings.R;
import com.bamf.settings.activities.NotificationManagerActivity;
import com.bamf.settings.widgets.BamfLedPreference;
import com.bamf.settings.widgets.ChipsFiltersTextView;
import com.bamf.settings.widgets.ColorPickerDialog;
import com.bamf.settings.widgets.IconPreference;
import com.bamf.settings.widgets.BamfVibratePreference;

public class NotificationItemFragment extends PreferenceFragment 
	implements OnPreferenceChangeListener, OnPreferenceClickListener {
	
	private static final int MENU_SAVE = 0x0;
	private static final int MENU_DELETE = 0x1;
	private static final int MENU_BACK = 0x2;
	private static final int MENU_TEST = 0x3;
	private static final int MENU_LOCK = 0x4;
	
	private static final int DEFAULT_BACKGROUND = 0xFF111111;
	private static final String WAKELOCK_SUMMARY_PREFIX = "Current setting: ";
	private static final String WAKELOCK_SUMMARY_DISABLED = "Current setting: disabled";
	
	private NotificationManagerActivity mActivity;
	private OnBackStackChangedListener mBackStackListener;
	
	private String mTitle;
	private ApplicationInfo mAppInfo;
	private Bundle mArgs;
	
	private int mLedColor;
	private int mLedOffMS;
	private int mLedOnMS;
	private int mWakeLockMS;
	private String mFilters;
	
	private boolean mDirty = false;
	
	private boolean mEnabled;
	private CheckBoxPreference mHideCheckBox;
	private NotificationSoundPreference mSoundPref; // bring up ringtone chooser
	private Preference mFilterPref; // bring up a dialog box for entering filters
	private BamfVibratePreference mVibratePref; // bring up a dialog for this as well
	private BamfLedPreference mLedColorPref;
	private IconPreference mBackgroundPref;
	private Preference mWakeLockPref;
	
    private static final int MSG_UPDATE_NOTIFICATION_SUMMARY = 1;
	private static final boolean DEBUG = false;
	    
    private Runnable mRingtoneLookupRunnable;
	
	private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_NOTIFICATION_SUMMARY:
            	mSoundPref.setSummary((CharSequence) msg.obj);
                break;
            }
        }
    };
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mActivity = (NotificationManagerActivity) getActivity();
        mArgs = getArguments();
        
        addPreferencesFromResource(R.xml.notification_item);

        PreferenceScreen prefSet = getPreferenceScreen();
        
        if(DEBUG)Toast.makeText(mActivity, "onCreate", Toast.LENGTH_SHORT).show();
        
        mHideCheckBox = new CheckBoxPreference(mActivity);
        mHideCheckBox.setTitle("Hide Notifications");
        mHideCheckBox.setSummary("Do not show notifications from this application");
        mHideCheckBox.setOnPreferenceChangeListener(this);
        
        mSoundPref = new NotificationSoundPreference(mActivity, null);
        mSoundPref.setTitle("Sound");
        mSoundPref.setPersistent(false);
        mSoundPref.setRingtoneType(RingtoneManager.TYPE_NOTIFICATION);
        mSoundPref.setOnPreferenceChangeListener(this);
        
        mFilterPref = new Preference(mActivity);
        mFilterPref.setTitle("Filters");
        mFilterPref.setSummary("Hide notifications containing certain words or phrases");
        
        mVibratePref = new BamfVibratePreference(mActivity);
        mVibratePref.setTitle("Vibrate pattern");
        mVibratePref.setSummary("Custom haptic feedback");
        mVibratePref.setDialogTitle("Vibrate pattern");
        mVibratePref.setDialogMessage("Configure a custom haptic feedback pattern");
        mVibratePref.setPersistent(true);
        mVibratePref.setOnPreferenceChangeListener(this);
        
        mWakeLockPref = new Preference(mActivity);
        mWakeLockPref.setTitle("Turn on screen for x seconds");
        
        mLedColorPref = new BamfLedPreference(mActivity, null);
        mLedColorPref.setTitle("LED color");
        mLedColorPref.setSummary("Set a custom LED color");
        mLedColorPref.setDialogTitle("Setup LED");
        mLedColorPref.setDialogMessage("Configure custom LED settings");
        mLedColorPref.setPersistent(true);
        mLedColorPref.setOnPreferenceChangeListener(this);
        
        mBackgroundPref = new IconPreference(mActivity, null);
        mBackgroundPref.setTitle("Background color");
        mBackgroundPref.setSummary("Set a custom background color");

        prefSet.addPreference(mHideCheckBox);
        prefSet.addPreference(mFilterPref);
        prefSet.addPreference(mSoundPref);
        prefSet.addPreference(mVibratePref);
        prefSet.addPreference(mLedColorPref);
        prefSet.addPreference(mBackgroundPref);
        prefSet.addPreference(mWakeLockPref);
        
        mBackStackListener = new FragmentManager.OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() {
				Toast.makeText(mActivity, "Dirty: "+mDirty, Toast.LENGTH_SHORT).show();
				if(mDirty){
					showSaveDialog();
				}
			}
		};
        
        mRingtoneLookupRunnable = new Runnable() {
            public void run() {
                if (mSoundPref != null) {
                    updateSoundName(RingtoneManager.TYPE_NOTIFICATION, mSoundPref,
                    		MSG_UPDATE_NOTIFICATION_SUMMARY);
                }
            }
        };
        
        this.setHasOptionsMenu(true);
    }
	
	private void updateSoundName(int type, Preference preference, int msg) {
        if (preference == null) return;
        Context context = mActivity;
        if (context == null) return;
        Uri ringtoneUri = ((NotificationSoundPreference)preference).getNotification();
        CharSequence summary = "Use default";
        // Is it a null?
        if (ringtoneUri == null) {
            summary = "Use default";
        } else {
            // Fetch the ringtone title from the media provider
            try {
                Cursor cursor = context.getContentResolver().query(ringtoneUri,
                        new String[] { MediaStore.Audio.Media.TITLE }, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        summary = cursor.getString(0);
                    }
                    cursor.close();
                }
            } catch (SQLiteException sqle) {
                // Unknown title for the ringtone
            	mSoundPref.setNotification(null);
            }
        }
        mHandler.sendMessage(mHandler.obtainMessage(msg, summary));
    }

    private void lookupRingtoneNames() {
        new Thread(mRingtoneLookupRunnable).start();
    }
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
       
        
	}
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem mi;
        mi = menu.add(Menu.NONE, MENU_SAVE, Menu.NONE, "Save");
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        mi = menu.add(Menu.NONE, MENU_TEST, Menu.NONE, "Test");
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        mi = menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, "Delete");
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        mi = menu.add(Menu.NONE, MENU_LOCK, Menu.NONE, "Lock");
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        mi = menu.add(Menu.NONE, MENU_BACK, Menu.NONE, "Cancel");
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case MENU_SAVE:
            	saveChanges();
            	getFragmentManager().popBackStack();
                return true;
            case MENU_DELETE:
            	showDeleteDialog();
            	return true;
            case MENU_BACK:
            	getFragmentManager().popBackStack();
            	return true;
            case MENU_TEST:
            	createTestNotification();
            	return true;
            default:
        }
        
        return false;
    }
	
	private void createTestNotification() {
		
		final Notification test = new Notification.Builder(mActivity)
			.setLights(mLedColor, mLedOnMS, mLedOffMS)
			.setContentTitle("Notification Manager")
			.setContentText("This is a test for "+mTitle)
			.setTicker("This is a test for "+mTitle)
			.setSmallIcon(R.drawable.ic_notification_open)
			.setLargeIcon(Bitmap.createBitmap(
					((BitmapDrawable)mAppInfo
							.loadIcon(mActivity.getPackageManager()))
							.getBitmap()))
			.setSound(mSoundPref.getNotification())
			.setVibrate(mVibratePref.getVibratePattern())
			.setAutoCancel(true)
			.setWhen(System.currentTimeMillis())
			.getNotification();

	    test.wakeLockMS = mWakeLockMS;
	    
	    long[] vib = mVibratePref.getVibratePattern();
	    if(vib!=null && DEBUG){
		    StringBuilder sb = new StringBuilder();
		    for(int i=0;i<vib.length;i++){
		    	sb.append(vib[i]);
		    	if(i!=(vib.length-1))sb.append(",");
		    }
		    
		    Toast.makeText(mActivity, "vibrate: "+sb.toString(), Toast.LENGTH_SHORT).show();
	    }
		
		AlertDialog d = new AlertDialog.Builder(mActivity)
			.setTitle("Test notification")
			.setMessage("Turn off your screen now to test the led color "
					+"and/or screen wake settings.\n\n"
					+"The notification will repeat every 15 seconds while "
					+"this dialog remains open.")
			.setCancelable(false)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(mTest!=null){
						mTest.Terminate();
						mTest=null;
					}
				}
			}).create();
			
		d.show();
		
		if(mTest!=null){
			mTest.Terminate();
			mTest=null;
		}
		
		mTest = new TestNotification(test);
		mTest.start();
		
	}
	
	private TestNotification mTest;
	
	private class TestNotification extends Thread {
		final static int NOTIFICATION_ID = 373737;
		private Notification notification;
		private boolean done;
		
		final NotificationManager notificationManager =
	            (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
		
		public TestNotification(Notification n){
			notification = n;
		}
		
		public void Terminate(){
			synchronized(this){
				done = true;
			}
		}
		
		@Override
		public void run(){
			while(!done){
				notificationManager.notify(NOTIFICATION_ID,notification);
				try{
					sleep(15000);
				}catch(InterruptedException e){}
			}
			notificationManager.cancel(NOTIFICATION_ID );
		}
		
		
	}

	@Override
	public void onStart(){
		super.onStart();
		// grab the values from the bundle and setup the fragment
		if(DEBUG)Toast.makeText(mActivity, "onStart", Toast.LENGTH_SHORT).show();
		mDirty = false;
		
		// setup the action bar
		mTitle = mArgs.getString("name");
        mAppInfo = (ApplicationInfo)mArgs.getParcelable("info");
        if(mTitle!=null && mAppInfo!=null){
        	setupActionBar(mTitle,mAppInfo.loadIcon(mActivity.getPackageManager()));
        }
		
		// enabled setting that controls the switch and whether
		// preferences are enabled
		mEnabled = mArgs.getBoolean("enabled");
		mActivity.setSwitchChecked(mEnabled);
		getPreferenceScreen().setEnabled(mEnabled);

		// setup preferences
        mHideCheckBox.setChecked(mArgs.getBoolean("hide"));
        mBackgroundPref.setupPanel(mArgs.getInt("background"), false); 
        mFilters = mArgs.getString("filters");
        mVibratePref.setFlags(mArgs.getInt("vibrate", 0));
        
        // wake lock setting
        mWakeLockMS = mArgs.getInt("wakelock");
        String summary = (mWakeLockMS>0)?mWakeLockMS+" milliseconds":"disabled";
		mWakeLockPref.setSummary(WAKELOCK_SUMMARY_PREFIX+summary);
        
		// Sound setting
        mSoundPref.setNotification((Uri)mArgs.getParcelable("sound"));
		lookupRingtoneNames();
        
		// LED settings
        mLedColor = mArgs.getInt("ledcolor");
        mLedOnMS = mArgs.getInt("ledonms");
        mLedOffMS = mArgs.getInt("ledoffms");
        
        int[] rate = new int[]{mLedOffMS, mLedOnMS};
        
        if(mLedColor<0 && (mLedOffMS!=1 && mLedOffMS != 2)){
        	mLedColorPref.setLED(0,mArgs.getInt("ledcolor"),rate);
        }else if(mLedOffMS==1 || mLedOffMS==2){
        	mLedColorPref.setLED(mLedOffMS,0,rate);
        }
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(mEnabled != mActivity.getSwitchChecked()){
			mEnabled = mActivity.getSwitchChecked();
			getPreferenceScreen().setEnabled(mEnabled);
			mDirty = true;
		}
	}
	
	private String getPackageName(){
		String pkg = mAppInfo.packageName;
		// notifications from Talk come from this package
		// so we need to be cognizant of this
		if(pkg.equals("com.google.android.talk")){
			pkg = "com.google.android.gsf";
		}
		
		return pkg;
	}
	
	private void saveChanges(){
		// save the settings to the database
		ContentValues values = new ContentValues();
		
        values.put(Notifications.NOTIFICATION_ENABLED, mEnabled);
        values.put(Notifications.NOTIFICATION_HIDE, mHideCheckBox.isChecked());
        if(mFilters==null){
        	values.putNull(Notifications.FILTERS);
        }else{
        	values.put(Notifications.FILTERS, mFilters);
        }
        values.put(Notifications.LED_COLOR, mLedColor);
        values.put(Notifications.LED_OFF_MS, mLedOffMS);
        values.put(Notifications.LED_ON_MS, mLedOnMS);
        values.put(Notifications.BACKGROUND_COLOR, mBackgroundPref.getColor());
        values.put(Notifications.WAKE_LOCK_TIME, mWakeLockMS);
        if(mSoundPref.getNotification()!=null){
        	values.put(Notifications.NOTIFICATION_SOUND, mSoundPref.getNotification().toSafeString());
		}else{
			values.putNull(Notifications.NOTIFICATION_SOUND);
		}
        values.put(Notifications.VIBRATE_PATTERN, mVibratePref.getFlags());
        
        int result = mActivity.getContentResolver().update(
        		Notifications.CONTENT_URI, 
        		values,
        		Notifications.PACKAGE_NAME + "=?",
        		new String[]{this.getPackageName()});
        if(DEBUG)Toast.makeText(mActivity, "update result:"+result, Toast.LENGTH_SHORT).show();
	}
	
	private void setupActionBar(String title, Drawable icon) {
		mActivity.setupFragmentActionBar(NotificationManagerActivity.FRAGMENT_ITEM);
		mActivity.setTitleText(title);
        mActivity.getActionBar().setIcon(icon);
    }
	
	private void showDeleteDialog() {
		
		final Dialog d = new AlertDialog.Builder(mActivity)
			.setCancelable(false)
			.setIcon(mAppInfo.loadIcon(mActivity.getPackageManager()))
			.setTitle("Remove "+mTitle+"?")
			.setMessage("Press Ok to delete the settings.")
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {	
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					mActivity.getContentResolver().delete(
							Notifications.CONTENT_URI, 
							Notifications.PACKAGE_NAME + "=?",
							new String[]{getPackageName()});
					getFragmentManager().popBackStack();
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			}).create();			
				
		d.show();
	}
	
	public boolean showSaveDialog() {
		if(!mDirty)return true;
		
		final Dialog d = new AlertDialog.Builder(mActivity)
			.setCancelable(false)
			.setIcon(mAppInfo.loadIcon(mActivity.getPackageManager()))
			.setTitle("Changes made to "+mTitle)
			.setMessage("Do you want to save your changes?")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {	
				@Override
				public void onClick(DialogInterface dialog, int which) {
					saveChanges();
					getFragmentManager().popBackStack();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					getFragmentManager().popBackStack();
				}
			}).create();			
				
		d.show();
		
		return false;
	}
	
	private void showColorPicker(int color){
        final ColorPickerDialog d = new ColorPickerDialog(mActivity, color);
        d.setAlphaSliderVisible(false);
        d.setTitle("Choose background color");
        d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mBackgroundPref.setupPanel(forceAlpha(d.getColor()), false);
                mDirty = true;
            }
        });
        
        d.setButton(DialogInterface.BUTTON_NEUTRAL, "Default", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	mBackgroundPref.setupPanel(0, false);
            	mDirty = true;
            }
        });

        d.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                d.dismiss();
            }
        });

        d.show();
    }
	
	private int forceAlpha(int color){
		// if the setting is currently disabled, display the default color
		if(color==0)return DEFAULT_BACKGROUND;
		
		// prevent transparent or partially transparent colors
		int alpha = 255;
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		return Color.HSVToColor(alpha, hsv);
	}
	
	private void showFilterPicker(){
		final ChipsFiltersTextView input = new ChipsFiltersTextView(mActivity, null);
		input.setHint("add filters");
		
		if(mFilters!=null && !TextUtils.isEmpty(mFilters)){
			// convert the saved words into chips
			for(String word: mFilters.split("\\|")){
				input.append(new Rfc822Token(null, word, null).toString());
			}
		}
		
		final AlertDialog d = new AlertDialog.Builder(mActivity)
			.setTitle("Configure filters")
			.setMessage("Add text phrases that will be used to filter notifications")
			.setView(input, 20, 20, 20, 20)
			.create();
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
        		// save all of the words delimited by pipes
            	StringBuilder sb = new StringBuilder();
            	for(Rfc822Token token: Rfc822Tokenizer.tokenize(input.getText().toString())){
            		sb.append(token.getAddress());
            		sb.append("|");
            	}
            	mFilters = sb.toString();
            	mDirty = true;
            }
        });
		d.setButton(DialogInterface.BUTTON_NEUTRAL, "Disable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	mFilters = null;
            	mDirty = true;
            }
        });
		d.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });

        d.show();
	}
		
	private void showWakeLockPicker(){
		final EditText input = new EditText(mActivity);
		input.setHint("milliseconds");
		input.setImeActionLabel("Ok", EditorInfo.IME_ACTION_DONE);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		
		if(mWakeLockMS>0){
			input.setText(String.valueOf(mWakeLockMS));
		}
		
		final AlertDialog d = new AlertDialog.Builder(mActivity)
			.setTitle("Configure screen wake time")
			.setMessage("Enter the time in milliseconds for waking the screen")
			.setView(input, 20, 20, 20, 20)
			.create();
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	try{
            		final int user = Integer.parseInt(input.getText().toString());
            		mWakeLockMS = user;
                    String summary = (mWakeLockMS>0)?mWakeLockMS+" milliseconds":"disabled";
            		mWakeLockPref.setSummary(WAKELOCK_SUMMARY_PREFIX+summary);
            	}catch(NumberFormatException e){
            		mWakeLockMS = 0;
            		mWakeLockPref.setSummary(WAKELOCK_SUMMARY_DISABLED);
            	}
            	mDirty = true;
            }
        });
		d.setButton(DialogInterface.BUTTON_NEUTRAL, "Disable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	mWakeLockMS = 0;
            	mWakeLockPref.setSummary(WAKELOCK_SUMMARY_DISABLED);
            	mDirty = true;
            }
        });
		d.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
		
		input.setOnEditorActionListener(
	        new EditText.OnEditorActionListener() {
	            @Override
	            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	            	if (actionId == EditorInfo.IME_ACTION_DONE ||
	                        event.getAction() == KeyEvent.ACTION_DOWN &&
	                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
	            		d.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
	                    return true;
	                }
	                return false;
	            }
	        });

        d.show();
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference){
		if(preference==mBackgroundPref){
			showColorPicker(forceAlpha(mBackgroundPref.getColor()));
			return true;
		}else if(preference==mWakeLockPref){
			showWakeLockPicker();
			return true;
		}else if(preference==mFilterPref){
			showFilterPicker();
			return true;
		}
		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference instanceof NotificationSoundPreference){
			if(DEBUG)Toast.makeText(mActivity, "firing onChange", Toast.LENGTH_SHORT).show();
			lookupRingtoneNames();
			mDirty = true;
			return true;
		}else if(preference == mHideCheckBox){
			mDirty = true;
			return true;
		}else if(preference == mVibratePref){
			mDirty = true;
			return true;
		}else if(preference == mLedColorPref){
			mDirty = true;
			mLedColor = mLedColorPref.getColor();
			// if this is not a special led, there must be a minimum of 100 here
			mLedOffMS = ((mLedColorPref.getRate()[0]>=100 || 
					mLedColorPref.getRate()[0]==0)?mLedColorPref.getRate()[0]:100);
			mLedOnMS = mLedColorPref.getRate()[1];
			if(mLedColorPref.getType()>0){
				mLedColor = Color.WHITE;
				mLedOffMS = mLedColorPref.getType();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference instanceof TwoStatePreference){
            ((TwoStatePreference) preference).setChecked(!((TwoStatePreference) preference).isChecked());
        }
		return false;
	}
}
