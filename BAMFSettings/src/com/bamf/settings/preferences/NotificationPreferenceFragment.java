package com.bamf.settings.preferences;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification.Notifications;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.bamf.bamfutils.services.BAMFRootService;
import com.bamf.bamfutils.services.IRootService;
import com.bamf.settings.R;
import com.bamf.settings.activities.NotificationManagerActivity;
import com.bamf.settings.provider.SettingsProvider;

public class NotificationPreferenceFragment extends PreferenceFragment 
implements OnPreferenceClickListener {

	private static final String TAG = NotificationPreferenceFragment.class.getSimpleName();
	private static final String DATABASE_NAME = "BAMFSettings.db";

	private ServiceConnection mServiceConnection; 
	private static IRootService mRootService;
	private static boolean mBound = false;

	private NotificationManagerActivity mActivity;
	private Preference mGeneral;
	private Preference mApplications;
	private Preference mContacts;
	private Preference mBattery;
	private Preference mQuietHours;

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

		mQuietHours = new Preference(mActivity);
		mQuietHours.setTitle("Quiet Hours");
		mQuietHours.setFragment(QuietHoursFragment.class.getName());
		mQuietHours.setOnPreferenceClickListener(this);
		mQuietHours.setEnabled(true);
		updateQuietHoursSummary();
		
		prefSet.addPreference(mQuietHours);

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
		//prefSet.addPreference(mContacts);

		mBattery = new Preference(mActivity);
		mBattery.setTitle("Battery");
		mBattery.setSummary("Manage custom notification settings");
		mBattery.setEnabled(false);
		//prefSet.addPreference(mBattery);

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

		this.setHasOptionsMenu(false);
	}

	private void updateQuietHoursSummary() {
		
		final ContentResolver resolver = getActivity().getContentResolver();
		if (Settings.System.getIntForUser(resolver, Settings.System.QUIET_HOURS_ENABLED, 0, UserHandle.USER_CURRENT) == 1) {
			mQuietHours.setSummary(getString(R.string.quiet_hours_active_from) + " " +
					returnTime(String.valueOf(Settings.System.getIntForUser(resolver, Settings.System.QUIET_HOURS_START, 0, UserHandle.USER_CURRENT)))
					+ " " + getString(R.string.quiet_hours_active_to) + " " +
					returnTime(String.valueOf(Settings.System.getIntForUser(resolver, Settings.System.QUIET_HOURS_END, 0, UserHandle.USER_CURRENT))));
		} else {
			mQuietHours.setSummary(getString(R.string.quiet_hours_summary));
		}
	}

	private String returnTime(String t) {
		if (t == null || t.equals("")) {
			return "";
		}
		int hr = Integer.parseInt(t.trim());
		int mn = hr;

		hr = hr / 60;
		mn = mn % 60;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hr);
		cal.set(Calendar.MINUTE, mn);
		Date date = cal.getTime();
		return DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(date);
	}

	private void setupActionBar(){
		mActivity.setupFragmentActionBar(NotificationManagerActivity.FRAGMENT_PREFERENCE);	

		boolean enabled = Settings.System.getIntForUser(mActivity.getContentResolver(), 
				Settings.System.NOTIFICATION_MANAGER,0, UserHandle.USER_CURRENT)==1;
		mActivity.setSwitchChecked(enabled);
	}

	private void refreshLastBackup(){
		//String lastBackup = SettingsProvider.getLastBackupDate(mActivity.getExternalFilesDir("databases"));
		String lastBackup = null;
		try{
			lastBackup = mRootService.getLastBackupDate(
					Environment.getExternalStorageAppFilesDirectory(mActivity.getPackageName(),true)
					.getAbsolutePath());
		}catch(RemoteException e){
			Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
		}

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
		setupServices();
	}

	@Override 
	public void onStart(){
		super.onStart();
		setupActionBar();
		updateQuietHoursSummary();
	}

	@Override
	public void onDetach(){
		super.onDetach();
		releaseService();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference==mApplications || preference==mGeneral || preference==mQuietHours){
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
			File f = null;
			try{
				f = new File(Environment.getExternalStorageAppFilesDirectory(mActivity.getPackageName(),true)
					.getAbsolutePath()+"/databases/");
				if(!f.exists())
					f.mkdirs();				
			}catch(Exception e){}
			if(mBound){
				try{
					boolean result = mRootService.copyFile(
						mActivity.getDatabasePath(DATABASE_NAME).getAbsolutePath(), 
						Environment.getExternalStorageAppFilesDirectory(mActivity.getPackageName(),true)
							.getAbsolutePath()+"/databases/"+DATABASE_NAME, false);
					Toast.makeText(mActivity, result?"Backup successful":"Backup failed!", Toast.LENGTH_SHORT).show();
				}catch(RemoteException e){}
				refreshLastBackup();
			}
			return true;
		}else if(preference==mRestoreDB){
			if(mBound){
				try{
					boolean result = mRootService.copyFile( 
						Environment.getExternalStorageAppFilesDirectory(mActivity.getPackageName(),true)
							.getAbsolutePath()+"/databases/"+DATABASE_NAME,
							mActivity.getDatabasePath(DATABASE_NAME).getAbsolutePath(), true);
					// force the provider to read from the disk now
					final IContentProvider cp = mActivity.getContentResolver().acquireProvider(Notifications.AUTHORITY);
					if(cp!=null){
						cp.call("clearCache", null, null);
					}
					Toast.makeText(mActivity, result?"Restore successful":"Restore failed!", Toast.LENGTH_SHORT).show();
				}catch(RemoteException e){
					Toast.makeText(mActivity, "Restore failed!", Toast.LENGTH_SHORT).show();
				}
			}	
			return true;
		}
		return false;
	}

	private void setupServices() {

		if(mServiceConnection == null){
			mServiceConnection = new ServiceConnection(){

				@Override
				public void onServiceConnected(ComponentName name, IBinder service) {
					mRootService = IRootService.Stub.asInterface(service);
					mBound = true;
					refreshLastBackup();
				}
				@Override
				public void onServiceDisconnected(ComponentName name) {
					mServiceConnection = null;
					mRootService = null;
					mBound = false;
				}               
			};
			try{
				if(mActivity.bindService(new Intent(
						new Intent(mActivity.createPackageContext("com.bamf.bamfutils", 0)
								, BAMFRootService.class)), mServiceConnection, 
								Context.BIND_AUTO_CREATE)){
					Log.d(TAG, "service started and the bind was successful");
				}
			}catch(Exception e){
				Log.e(TAG, "service did not start", e);
			}
		}
	}

	public void releaseService() {
		try{
			// this is an asynchronous call so we should not set the connection variable to null here
			mActivity.unbindService(mServiceConnection);
			Log.d(TAG, "service was stopped and unbound");
		}catch(Exception e){}      
	}

}
