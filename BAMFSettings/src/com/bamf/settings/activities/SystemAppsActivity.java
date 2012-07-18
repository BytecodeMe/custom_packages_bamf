package com.bamf.settings.activities;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bamf.settings.R;
import com.bamf.settings.adapters.AppAdapter;
import com.bamf.settings.adapters.PackageDescription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SystemAppsActivity extends ListActivity {
    
    private static final String TAG = SystemAppsActivity.class.getSimpleName();
	
	ProgressDialog mProgress;
	Handler handler = new Handler();
	List<PackageDescription> appResult;	
	AppAdapter mAdapterApps;
	int mLastPosition = 0;
	boolean mEnableState = true;
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // use a custom layout in case we want to change it later
        setContentView(R.layout.app_listview);
        setupActionBar();
        
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);
        
        mProgress.setMessage("Please wait...");
        mProgress.show();
    	loadApps(false,0);
    }
    
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setTitle(R.string.system_app_settings);             
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked;
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadApps(final boolean refresh,final int selection) {
		
        appResult = new ArrayList<PackageDescription>();    
		
		final Runnable finished = new Runnable() {
		    public void run() {		    	
		    	showApps(refresh,selection);
		    	mProgress.cancel();
		    }
		};
		
		new Thread() {		    
			@Override public void run() {					
				
			    PackageDescription details = null;
								
				PackageManager pm = getPackageManager();
				PackageInfo sys = null;
				try {
					sys = pm.getPackageInfo("android",
					        PackageManager.GET_SIGNATURES);
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				List<PackageInfo> packs = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
				for(int i = 0;i < packs.size();i++){
		        	PackageInfo pi = packs.get(i);
		        	
		        	details = new PackageDescription(pi.applicationInfo, pi.packageName, 
                            pi.applicationInfo.loadLabel(pm).toString());
		        	Log.d(TAG,"Getting App List");
		        	
		        	if((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 ){
		        		if(pi != null && pi.signatures != null && !(sys.signatures[0].equals(pi.signatures[0]))){
		        		    details.setIcon(pi.applicationInfo.loadIcon(pm));
		        		    details.setEnabled(pi.applicationInfo.enabled);
		        			appResult.add(details);
		        		}
		        	}
				
				}
				handler.post(finished);
				
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
		Collections.sort(appResult, comparator);
	
		mAdapterApps = new AppAdapter(this, appResult, R.layout.app_layout);
		
		getListView().setPadding(30, 5, 60, 0);
		getListView().setFastScrollAlwaysVisible(true);
		setListAdapter(mAdapterApps);
		if(refresh)
			setSelection(selection);
		
	}
    
    @Override
    public void onListItemClick(ListView parent, View v, int position, long id) {
        
    	mLastPosition = position;
		TextView mTitle,mDetails;
		ImageView mIcon;		
		
		View item = LayoutInflater.from(this).inflate(R.layout.app_dialog_layout, null);
		mTitle = (TextView) item.findViewById(R.id.dialog_title);
		mDetails = (TextView) item.findViewById(R.id.dialog_detail);
		mIcon = (ImageView) item.findViewById(R.id.icon_view);
		mTitle.setText(mAdapterApps.getItem(position).getLabel());
        mDetails.setText(mAdapterApps.getItem(position).getPackageName());
        mIcon.setImageDrawable(mAdapterApps.getItem(position).getIcon());
        
		new AlertDialog.Builder(this)
		.setView(item)
		.setTitle(getString(R.string.manage_applications))
		.setPositiveButton(getString(mAdapterApps.getItem(position).getEnabled() ? R.string.disable : R.string.enable), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {			
				dialog.dismiss();				
				if(!SettingsActivity.isRootServiceBound(SystemAppsActivity.this))return;
	        	boolean state = mAdapterApps.getItem(mLastPosition).getEnabled();

				try {
				    SettingsActivity.getRootService().toggleAppState(
				            state, mAdapterApps.getItem(mLastPosition).getPackageName());
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
				forceWait(getListView().getFirstVisiblePosition(),2000);
			}
		})
		.setNeutralButton(getString(R.string.remove), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {		
				dialog.dismiss();	        
				if(!SettingsActivity.isRootServiceBound(SystemAppsActivity.this))return;
				try {
				    SettingsActivity.getRootService().removeApp(
				            mAdapterApps.getItem(mLastPosition).getInfo().dataDir,
				            mAdapterApps.getItem(mLastPosition).getInfo().sourceDir);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
				forceWait(getListView().getFirstVisiblePosition(),2000);	
				Uri packageURI = Uri.parse("package:"+mAdapterApps.getItem(mLastPosition).getPackageName());
	    		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
	    		startActivity(uninstallIntent);
			}
		})
		.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {		
				dialog.cancel();	
			}
		})
		.show();		
				
		mDetails.setSelected(true);		
		
    }

	/**
	 * Processing the Root commands can take a little time, so we force the UI thread to wait a set amount of time before refreshing the view.
	 * This ensures the new status is reflected in the listView.
	 * 
	 * @param position
	 * @param sleepTime
	 */
	private void forceWait(final int position,final int sleepTime) {
		
		mProgress.setMessage("Please wait...");
		mProgress.show();
		final Runnable finished = new Runnable() {
		    public void run() {			    	
		    	loadApps(true,position);	
		    	mProgress.dismiss();
		    }
		};
		
		new Thread() {		    
			@Override public void run() {					
				try {
					sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
				handler.post(finished);					
			}
		}.start();        
		
	}
}
