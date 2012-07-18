package com.bamf.settings.preferences;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.TwoStatePreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.widget.Toast;

import com.bamf.settings.R;
import com.bamf.settings.activities.SettingsActivity;
import com.bamf.settings.activities.SettingsActivity.OnServiceBindedListener;
import com.bamf.settings.widgets.BAMFSwitchPreference;

public class SystemMaintenanceFragment extends PreferenceFragment 
    implements OnPreferenceClickListener {	
    
	/**
	 * We aren't using this fragment right now, because most of the funcionality is duplicated elsewhere or useless to begin with.
	 */
	
    public static final int MSG_DALVIK = 103;   
    public static final int MSG_CACHE = 104;
    public static final int MSG_FIX_PERMS = 105;    
    
    private static final String PREF_DALVIK = "pref_system_basic_dalvik";
    private static final String PREF_CACHE = "pref_system_basic_cache";
    private static final String PREF_FIX_PERMS = "pref_system_basic_fix";
    
	private SettingsActivity mSettings;
	private Preference mDalvik;
	private Preference mCache;
	private Preference mFixPerms;
	
	private Handler mHandler = new RootHandler();
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.system_maintenance);
        
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	mSettings = (SettingsActivity) getActivity();    	
    	mDalvik = findPreference(PREF_DALVIK);
    	mCache = findPreference(PREF_CACHE);
    	mFixPerms = findPreference(PREF_FIX_PERMS);
    	 	
    	mDalvik.setOnPreferenceClickListener(this);
    	mCache.setOnPreferenceClickListener(this);
    	mFixPerms.setOnPreferenceClickListener(this);    	
    	
    }      

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference == mDalvik){
			if(SettingsActivity.isRootServiceBound(mSettings)){
                mHandler.sendEmptyMessage(MSG_DALVIK);
            }
		}else if(preference == mCache){
			if(SettingsActivity.isRootServiceBound(mSettings)){
                mHandler.sendEmptyMessage(MSG_CACHE);
            }		
		}else if(preference == mFixPerms){
            if(SettingsActivity.isRootServiceBound(mSettings)){
                mHandler.sendEmptyMessage(MSG_FIX_PERMS);
            }
        }
		
		return false;
	}	

	public class RootHandler extends Handler {
	    
	    @Override
	    public void handleMessage(Message msg){
            boolean result;
            switch(msg.what){                
                case MSG_DALVIK:
                	new AlertDialog.Builder(mSettings)
        			.setTitle(getString(R.string.reboot_warning_title))
        			.setMessage(getString(R.string.reboot_warning_desc))
        			.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int whichButton) {
        					standDialog(false,"Clearing Dalvik. Please Wait...");
        					try {
                                SettingsActivity.getRootService().clearDalvik();                        
                            } catch (RemoteException e) {
                                e.printStackTrace();                       
                            }
        				}
        			})
        			.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int whichButton) {
        					dialog.dismiss();
        				}
        			})
        			.show();                	
                    break;
                case MSG_CACHE:
                	new AlertDialog.Builder(mSettings)
        			.setTitle("Clear Cache")
        			.setMessage("Are you sure you want to clear the cache?")
        			.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int whichButton) {
        					try {
                                SettingsActivity.getRootService().clearCache();      
                                Toast.makeText(mSettings, "Cache Cleared.", Toast.LENGTH_SHORT).show();
                            } catch (RemoteException e) {
                                e.printStackTrace();                       
                            }
        				}
        			})
        			.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int whichButton) {
        					dialog.dismiss();
        				}
        			})
        			.show();                   	
                    break;                	
                case MSG_FIX_PERMS:
                	new AlertDialog.Builder(mSettings)
        			.setTitle(getString(R.string.reboot_warning_title))
        			.setMessage(getString(R.string.reboot_warning_desc))
        			.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int whichButton) {
        					standDialog(false,"Fixing Permissions. Please Wait...");
        					try {
                                SettingsActivity.getRootService().fixPerms();                        
                            } catch (RemoteException e) {
                                e.printStackTrace();                       
                            }
        				}
        			})
        			.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int whichButton) {
        					dialog.dismiss();
        				}
        			})
        			.show();     
                    break;                	
            }
	    }
	}
	
	protected void standDialog(boolean cancel, String msg) {
		
		ProgressDialog dialog = new ProgressDialog(mSettings);
		dialog.setCancelable(cancel);
		dialog.setMessage(msg);
		dialog.show();	
		
	}

}
