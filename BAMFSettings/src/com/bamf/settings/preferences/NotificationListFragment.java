package com.bamf.settings.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.Notification.Notifications;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bamf.settings.R;
import com.bamf.settings.activities.NotificationManagerActivity;
import com.bamf.settings.adapters.AppAdapter;
import com.bamf.settings.adapters.NotificationDescription;
import com.bamf.settings.adapters.PackageDescription;

public class NotificationListFragment extends ListFragment implements
		OnItemLongClickListener {
	
	private final static String TAG = NotificationListFragment.class.getSimpleName();
	
	private static final int REQUEST_PICK_APPLICATION = 1;
	private static final int MENU_ADDNEW = 0;
	private static final int MENU_BACK = 1;
	
	private static final int TYPE_ALL = 0;
	private static final int TYPE_APPLICATION = 1;
	private static final int TYPE_SYSTEM = 2;

	protected static final boolean DEBUG = false;
	
	private ArrayList<NotificationDescription> mAppResult;
	private AppAdapter mAdapterApps;
	private Handler mHandler = new Handler();
	private ProgressDialog mWaitDialog;
	
	private int[] mCounts;

	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWaitDialog = new ProgressDialog(getActivity());
        setUserVisibleHint(false);
    	setHasOptionsMenu(true);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem mi;
        mi = menu.add(Menu.NONE, MENU_ADDNEW, Menu.NONE, "Add New");
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        mi = menu.add(Menu.NONE, MENU_BACK, Menu.NONE, "Back");
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case MENU_ADDNEW:
            	mWaitDialog.setMessage("Please wait ...");
            	mWaitDialog.setTitle("Getting Applications");
            	mWaitDialog.setCancelable(false);
            	mWaitDialog.show();
            	pickApplication();
                return true;
            case MENU_BACK:
            	getFragmentManager().popBackStack();
            	return true;            	
            default:
        }
        
        return false;
    }    

    @Override
    public void onListItemClick(ListView parent, View v, int position, long id) {
    	NotificationDescription nd = mAppResult.get(position);
    	
    	Bundle item = new Bundle();
    	item.putString("name", nd.getLabel());
    	item.putParcelable("info", nd.getInfo());
    	item.putBoolean("enabled", nd.getEnabled());
    	item.putBoolean("hide", nd.hide);
    	item.putParcelable("sound", nd.sound);
    	item.putInt("vibrate", nd.vibrateFlags);
    	item.putString("filters", nd.filters);
    	item.putInt("ledcolor", nd.led.color);
    	item.putInt("ledonms", nd.led.onms);
    	item.putInt("ledoffms", nd.led.offms);
    	item.putInt("background", nd.background);
    	item.putInt("wakelock", nd.wakeLockMS);
    	
    	final FragmentTransaction trans = getFragmentManager().beginTransaction();
    	trans.setCustomAnimations(R.anim.slide_in_right, 
        		R.anim.slide_out_left, 
        		R.anim.slide_in_left, 
        		R.anim.slide_out_right);
        trans.addToBackStack("item");
        trans.replace(R.id.container, 
        		Fragment.instantiate(getActivity(),
        		NotificationItemFragment.class.getName(),
                item),"item").commit();
    
    }
	
	@Override
	public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
		String pkg = mAppResult.get(pos).getPackageName();
		if(!pkg.equals("android") && !pkg.equals("com.android.systemui")){
			showDeleteDialog(pos);
			return true;
		}
		return false;
	}
	
	@Override 
	public void onStart(){
		super.onStart();
		
		((NotificationManagerActivity)getActivity())
			.setupFragmentActionBar(NotificationManagerActivity.FRAGMENT_LIST);
		
		try{
			// dont show until the list is completely setup
			getListView().setAlpha(0);
			synchronized(this){
	        	loadApps(false,0,getActivity().getActionBar().getSelectedNavigationIndex());
	        }
		}catch(Exception e){
			// this will throw an exception when the user hits the back key too fast
		}
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		//final SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(
		//		activity, R.array.action_list, android.R.layout.simple_spinner_dropdown_item);	
		setupNavigation();
	}
	
	private void setupNavigation(){
		String[] actionList = getActivity().getResources().getStringArray(R.array.action_list);
		final SpinnerAdapter mSpinnerAdapter = new TypeListAdapter(
				getActivity(), R.layout.list_item_row, R.id.empty_label_name, actionList);		
		getActivity().getActionBar().setListNavigationCallbacks(mSpinnerAdapter, mNagivationListener);
	}
	
	private ActionBar.OnNavigationListener mNagivationListener = new ActionBar.OnNavigationListener() {
		
		@Override
		public boolean onNavigationItemSelected(int itemPosition, long itemId) {
			if(isVisible() && getListAdapter()!=null){
				loadApps(true,0, itemPosition);
				return true;
			}else
				return false;
		}
	};
	
	@Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
		mWaitDialog.dismiss();
		
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
	        	case REQUEST_PICK_APPLICATION: 
	        		String pkg = getActivity()
	        			.getPackageManager()
	        			.resolveActivity(data, 0)
	        			.activityInfo
	        			.packageName;
	        		PackageInfo pi = null;
	        		
	        		try{
	        			pi = getActivity()
	        				.getPackageManager()
	        				.getPackageInfo(pkg, 0);
	        		}catch(Exception e){}
	        		
	        		boolean systemApp = (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
	        		
	        		// notifications from Talk come from this package
					// so we need to be cognizant of this
					if(pkg.equals("com.google.android.talk")){
						pkg = "com.google.android.gsf";
					}
	        		
					try{
		        		if(createNewNotification(pkg)){
		        			//loadApps(true,0, systemApp?TYPE_SYSTEM:TYPE_APPLICATION);
		        			getActivity().getActionBar().setSelectedNavigationItem(systemApp?TYPE_SYSTEM:TYPE_APPLICATION);
		        		}
					}catch(Exception e){
						// user pressing buttons
					}
	                break;
			}
		}
	}
	
	private boolean createNewNotification(String pkg){
		ContentValues values = new ContentValues();
        
        values.put(Notifications.NOTIFICATION_ENABLED, false);
        values.put(Notifications.NOTIFICATION_HIDE, false);
        values.put(Notifications.PACKAGE_NAME, pkg);
        
        Uri uri = getActivity().getContentResolver().insert(Notifications.CONTENT_URI, values);
        
        return Integer.parseInt(uri.getLastPathSegment()) != -1;
	}
	
	private void pickApplication(){
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
        startActivityForResult(pickIntent, REQUEST_PICK_APPLICATION);
	}
	
	private void showDeleteDialog(int pos) {
		final NotificationDescription nd = mAppResult.get(pos);
		
		final Dialog d = new AlertDialog.Builder(getActivity())
			.setCancelable(false)
			.setIcon(nd.getIcon())
			.setTitle("Remove "+nd.getLabel()+"?")
			.setMessage("Press Ok to delete the setting.")
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {	
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String pkg = nd.getPackageName();
					// notifications from Talk come from this package
					// so we need to be cognizant of this
					if(pkg.equals("com.google.android.talk")){
						pkg = "com.google.android.gsf";
					}
					
					getActivity().getContentResolver().delete(
							Notifications.CONTENT_URI, 
							Notifications.PACKAGE_NAME + "=?",
							new String[]{pkg});
					loadApps(true, 0, getActivity().getActionBar().getSelectedNavigationIndex());
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			}).create();			
				
		d.show();
	}
	
	private void loadApps(final boolean refresh,final int selection, final int type) {
		
        mAppResult = new ArrayList<NotificationDescription>();    
		
		final Runnable finished = new Runnable() {
		    public void run() {	
		    	try{
			    	showApps(refresh,selection);
			    	// ok, showtime
			    	getListView().setAlpha(1);
			    	if(DEBUG)Toast.makeText(getActivity(), "loaded", Toast.LENGTH_SHORT).show();
			    }catch(Exception e){
			    	// stop doing that
		    	}
		    }
		};
		
		new Thread() {		    
			@Override public void run() {
				
				mCounts = new int[]{0,0,0};
				NotificationDescription details = null;
				
				Cursor c = getActivity().getContentResolver().query(Notifications.CONTENT_URI, 
						null, null, null, null);
		        
				PackageManager pm = getActivity().getPackageManager();
				
				if(c.moveToFirst())mCounts[0] = c.getCount();
				
				Log.d(TAG, "Records returned="+c.getCount());
				for(int i = 0;i < c.getCount();i++){
					try{
						String packageName = c.getString(c.getColumnIndexOrThrow(Notifications.PACKAGE_NAME));
						// notifications from Talk come from this package
						// so we need to be cognizant of this
						if(packageName.equals("com.google.android.gsf")){
							packageName = "com.google.android.talk";
						}
						
			        	PackageInfo pi = pm.getPackageInfo(packageName, 0);
			        	boolean systemApp = (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
			        	boolean add = false;
			        	
			        	mCounts[systemApp ? 2:1]++;
			        	
			        	switch(type){
			        	case TYPE_SYSTEM:
			        		if(systemApp)add = true;
			        		break;
			        	case TYPE_APPLICATION:
			        		if(!systemApp)add = true;
			        		break;
			        	case TYPE_ALL:
			        		add = true;
			        	}
			        	
			        	if(add){
				        	Log.d(TAG, "Processing record for: "+packageName);
				        	details = new NotificationDescription(pi.applicationInfo, pi.packageName, 
		                            pi.applicationInfo.loadLabel(pm).toString());
				        	Log.d(TAG,"Getting App List");
				        	
				        	details.setIcon(pi.applicationInfo.loadIcon(pm));
				        	details.setEnabled(c.getInt(c.getColumnIndexOrThrow(Notifications.NOTIFICATION_ENABLED))==1);
				        	details.background = c.getInt(c.getColumnIndexOrThrow(Notifications.BACKGROUND_COLOR));
				        	details.filters = c.getString(c.getColumnIndexOrThrow(Notifications.FILTERS));
				        	details.hide = c.getInt(c.getColumnIndexOrThrow(Notifications.NOTIFICATION_HIDE))==1;
				        	details.led = new NotificationDescription.Led(
				        			c.getInt(c.getColumnIndexOrThrow(Notifications.LED_COLOR)),
				        			c.getInt(c.getColumnIndexOrThrow(Notifications.LED_ON_MS)),
				        			c.getInt(c.getColumnIndexOrThrow(Notifications.LED_OFF_MS)));
				        	try{
				        		details.sound = Uri.parse(c.getString(c.getColumnIndexOrThrow(
				        				Notifications.NOTIFICATION_SOUND)));
				        	}catch(NullPointerException e){}
				        	
				        	details.vibrateFlags = c.getInt(c.getColumnIndexOrThrow(Notifications.VIBRATE_PATTERN));
				        	details.wakeLockMS = c.getInt(c.getColumnIndexOrThrow(Notifications.WAKE_LOCK_TIME));
				        	
				        	mAppResult.add(details);
			        	}
					}catch(NameNotFoundException e){
						e.printStackTrace();
					}
					c.moveToNext();
				}
				
				c.close();
				mHandler.post(finished);
				
			}
		}.start();        
		
	}
    
    protected void showApps(boolean refresh,int selection) {
		
		Comparator<PackageDescription> comparator = new Comparator<PackageDescription>() {    
            @Override
            public int compare(PackageDescription lhs, PackageDescription rhs) {
                return lhs.getLabel().compareToIgnoreCase(rhs.getLabel());
            }
		};      
		Collections.sort(mAppResult, comparator);
	
		mAdapterApps = new AppAdapter(getActivity(), mAppResult, R.layout.app_layout);
		
		((NotificationManagerActivity)getActivity()).setLabelCount(String.valueOf(mAdapterApps.getCount()));
		
		try{
			if(mAdapterApps.getCount()>10){
				getListView().setPadding(30, 5, 60, 0);
				getListView().setFastScrollAlwaysVisible(true);
			}else{
				getListView().setPadding(30, 5, 30, 0);
			}
			
			setListAdapter(mAdapterApps);
			getListView().setOnItemLongClickListener(this);
			
			if(refresh){
				setSelection(selection);
			}
		}catch(IllegalStateException e){
			e.printStackTrace();
		}
		
		setUserVisibleHint(true);
		
	}
    
    private class TypeListAdapter extends ArrayAdapter<String> {
    	private Context mContext;
    	private int mLayout;
    	private String[] mActionList;
		
		public TypeListAdapter(Context context, int layout,
				int textViewResourceId, String[] types) {
			super(context, layout, textViewResourceId, types);
			
			mContext = context;
			mLayout = layout;
			mActionList = types;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			
			View v = View.inflate(mContext, mLayout, null);
			v.setPadding(12, 0, 12, 0);
			TextView tvLabel = (TextView)v.findViewById(R.id.empty_label_name);
			TextView activeLabel = (TextView)v.findViewById(R.id.active_label_name);
			activeLabel.setVisibility(View.GONE);
			
			tvLabel.setText(mActionList[position]);
			
			return v;
		}
		
        @Override
		public View getDropDownView(int position, View convertView, ViewGroup parent){
			View v = View.inflate(mContext, mLayout, null);
			
			TextView activeLabel = (TextView)v.findViewById(R.id.active_label_name);
			TextView emptyLabel = (TextView)v.findViewById(R.id.empty_label_name);
			TextView labelCount = (TextView)v.findViewById(R.id.label_count);
			
			if(mCounts[position] > 0){
				activeLabel.setText(mActionList[position]);
				emptyLabel.setVisibility(View.GONE);
				labelCount.setVisibility(View.VISIBLE);
				labelCount.setText(String.valueOf(mCounts[position]));
			}else{
				activeLabel.setVisibility(View.GONE);
				emptyLabel.setVisibility(View.VISIBLE);
				emptyLabel.setText(mActionList[position]);
			}
			
			return v;
		}
    	
    }

}
