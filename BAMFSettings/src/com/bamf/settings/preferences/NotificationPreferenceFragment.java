package com.bamf.settings.preferences;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.Toast;

import com.bamf.settings.R;
import com.bamf.settings.activities.NotificationManagerActivity;
import com.bamf.settings.provider.SettingsProvider;

public class NotificationPreferenceFragment extends PreferenceFragment 
	implements OnPreferenceClickListener {

	private NotificationManagerActivity mActivity;
	private Preference mGeneral;
	private Preference mApplications;
	private Preference mContacts;
	private Preference mBattery;
	
	private PreferenceCategory mMaintenanceCat;
	private Preference mBackupDB;
	private Preference mRestoreDB;
	private Preference mResetDB;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.notification_settings);
        this.setRetainInstance(true);
        
        mActivity = (NotificationManagerActivity) getActivity();

        PreferenceScreen prefSet = getPreferenceScreen();
        
        mGeneral = new Preference(mActivity);
        mGeneral.setTitle("General");
        mGeneral.setSummary("Global settings and tweaks");
        mGeneral.setFragment(NotificationGeneralFragment.class.getName());
        mGeneral.setOnPreferenceClickListener(this);
        mGeneral.setEnabled(false);
        prefSet.addPreference(mGeneral);
        
        mApplications = new Preference(mActivity);
        mApplications.setTitle("Applications");
        mApplications.setSummary("Manage custom notification settings");
        mApplications.setFragment(NotificationListFragment.class.getName());
        mApplications.setOnPreferenceClickListener(this);
        prefSet.addPreference(mApplications);
        
        mContacts = new Preference(mActivity);
        mContacts.setTitle("Contacts");
        mContacts.setSummary("Manage custom notification settings");
        mContacts.setEnabled(false);
        prefSet.addPreference(mContacts);
        
        mBattery = new Preference(mActivity);
        mBattery.setTitle("Battery");
        mBattery.setSummary("Manage custom notification settings");
        mBattery.setEnabled(false);
        prefSet.addPreference(mBattery);
        
        mMaintenanceCat = new PreferenceCategory(mActivity);
        mMaintenanceCat.setTitle("Maintenance");
        prefSet.addPreference(mMaintenanceCat);
        
        mBackupDB = new Preference(mActivity);
        mBackupDB.setTitle("Backup settings");
        mBackupDB.setOnPreferenceClickListener(this);
        mMaintenanceCat.addPreference(mBackupDB);
        
        mRestoreDB = new Preference(mActivity);
        mRestoreDB.setTitle("Restore settings from backup");
        mRestoreDB.setOnPreferenceClickListener(this);
        mMaintenanceCat.addPreference(mRestoreDB);
        
        mResetDB = new Preference(mActivity);
        mResetDB.setTitle("Reset to defaults");
        mResetDB.setOnPreferenceClickListener(this);
        mMaintenanceCat.addPreference(mResetDB);
        
        refreshLastBackup();
        this.setHasOptionsMenu(false);
    }
	
	private void setupActionBar(){
		mActivity.setupFragmentActionBar(NotificationManagerActivity.FRAGMENT_PREFERENCE);	
		
		boolean enabled = Settings.System.getInt(mActivity.getContentResolver(), 
				Settings.System.NOTIFICATION_MANAGER,0)==1;
		mActivity.setSwitchChecked(enabled);
	}
	
	private void refreshLastBackup(){
		String lastBackup = SettingsProvider.getLastBackupDate(mActivity.getExternalFilesDir("databases"));
		String prefix = "Last backup: ";
		
		mRestoreDB.setSummary(prefix + ((lastBackup==null)?"NONE":lastBackup));
	}
	
	private void userResetDB(){
		AlertDialog d = new AlertDialog.Builder(mActivity)
			.setTitle("Reset all settings?")
			.setMessage("WARNING: All settings will be lost!! This action cannot be undone!!")
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SettingsProvider.resetTables();
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			})
			.create();
		d.show();
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

	}
	
	@Override 
	public void onStart(){
		super.onStart();
		setupActionBar();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference==mApplications || preference==mGeneral){
			final FragmentTransaction trans = getFragmentManager().beginTransaction();
	        trans.setCustomAnimations(R.anim.slide_in_right, 
	        		R.anim.slide_out_left, 
	        		R.anim.slide_in_left, 
	        		R.anim.slide_out_right);
	        trans.addToBackStack("custom");
	        trans.replace(R.id.container, Fragment.instantiate(mActivity, preference.getFragment(),
	                new Bundle()),"custom").commit();
	    
	        return true;
		}else if(preference==mResetDB){
			userResetDB();
			return true;
		}else if(preference==mBackupDB){
			boolean result = SettingsProvider.backupDatabase(mActivity.getExternalFilesDir("databases"));
			Toast.makeText(mActivity, result?"Backup successful":"Backup failed!", Toast.LENGTH_SHORT).show();
			refreshLastBackup();
			return true;
		}else if(preference==mRestoreDB){
			boolean result = SettingsProvider.restoreDatabase(mActivity.getExternalFilesDir("databases"));
			Toast.makeText(mActivity, result?"Restore successful":"Restore failed!", Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}

}
