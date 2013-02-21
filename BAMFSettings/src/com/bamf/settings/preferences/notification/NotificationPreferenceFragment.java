package com.bamf.settings.preferences.notification;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification.Notifications;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.bamf.bamfutils.services.BAMFRootService;
import com.bamf.bamfutils.services.IRootService;
import com.bamf.settings.R;
import com.bamf.settings.activities.BaseSettingsActivity;
import com.bamf.settings.activities.NotificationManagerActivity;
import com.bamf.settings.provider.SettingsProvider;
import com.bamf.settings.widgets.BAMFSwitchPreference;

public class NotificationPreferenceFragment extends PreferenceFragment
		implements OnPreferenceClickListener, OnPreferenceChangeListener {

	private static final String TAG = NotificationPreferenceFragment.class
			.getSimpleName();
	private static final String DATABASE_NAME = "BAMFSettings.db";

	private ServiceConnection mServiceConnection;
	private static IRootService mRootService;
	private static boolean mBound = false;

	private BaseSettingsActivity mActivity;
	private Preference mGeneral;
	private Preference mApplications;
	private Preference mContacts;
	private Preference mBattery;
	private BAMFSwitchPreference mNotif;

	private PreferenceCategory mMaintenanceCat;
	private Preference mBackupDB;
	private Preference mRestoreDB;
	private Preference mResetDB;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.notification_settings);
		this.setRetainInstance(true);

		mActivity = (BaseSettingsActivity) getActivity();

		PreferenceScreen prefSet = getPreferenceScreen();

		boolean enabled = Settings.System.getInt(
				mActivity.getContentResolver(),
				Settings.System.NOTIFICATION_MANAGER, 0) == 1;

		mNotif = new BAMFSwitchPreference(mActivity);
		mNotif.setTitle("Notifcation Manager");
		mNotif.setChecked(enabled);
		mNotif.setOnPreferenceClickListener(this);
		mNotif.setOnPreferenceChangeListener(this);
		prefSet.addPreference(mNotif);

		mGeneral = new Preference(mActivity);
		mGeneral.setTitle("General");
		mGeneral.setSummary("Global settings and tweaks");
		mGeneral.setFragment(NotificationGeneralFragment.class.getName());
		mGeneral.setOnPreferenceClickListener(this);
		mGeneral.setEnabled(false);
		// prefSet.addPreference(mGeneral);

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
		// prefSet.addPreference(mContacts);

		mBattery = new Preference(mActivity);
		mBattery.setTitle("Battery");
		mBattery.setSummary("Manage custom notification settings");
		mBattery.setEnabled(false);
		// prefSet.addPreference(mBattery);

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

	}

	private void refreshLastBackup() {
		// String lastBackup =
		// SettingsProvider.getLastBackupDate(mActivity.getExternalFilesDir("databases"));
		String lastBackup = null;
		try {
			lastBackup = mRootService.getLastBackupDate(Environment
					.getExternalStorageAppFilesDirectory(
							mActivity.getPackageName()).getAbsolutePath());
		} catch (RemoteException e) {
			Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}

		String prefix = "Last backup: ";
		mRestoreDB.setSummary(prefix
				+ ((lastBackup == null) ? "NONE" : lastBackup));
	}

	private void userResetDB() {
		AlertDialog d = new AlertDialog.Builder(mActivity)
				.setTitle("Reset all settings?")
				.setMessage(
						"WARNING: All settings will be lost!! This action cannot be undone!!")
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								SettingsProvider.resetTables();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).create();
		d.show();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setupServices();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		releaseService();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference == mApplications || preference == mGeneral) {
			final FragmentTransaction trans = getFragmentManager()
					.beginTransaction();
			trans.setCustomAnimations(R.anim.slide_in_right,
					R.anim.slide_out_left, R.anim.slide_in_left,
					R.anim.slide_out_right);
			trans.addToBackStack("custom");
			trans.replace(
					R.id.container,
					Fragment.instantiate(mActivity, preference.getFragment(),
							new Bundle()), "custom").commit();

			return true;
		} else if (preference == mResetDB) {
			userResetDB();
			return true;
		} else if (preference == mBackupDB) {
			if (mBound) {
				try {
					boolean result = mRootService.copyFile(
							mActivity.getDatabasePath(DATABASE_NAME)
									.getAbsolutePath(),
							Environment.getExternalStorageAppFilesDirectory(
									mActivity.getPackageName())
									.getAbsolutePath()
									+ "/databases/" + DATABASE_NAME, false);
					Toast.makeText(mActivity,
							result ? "Backup successful" : "Backup failed!",
							Toast.LENGTH_SHORT).show();
				} catch (RemoteException e) {
				}
				refreshLastBackup();
			}
			return true;
		} else if (preference == mRestoreDB) {
			if (mBound) {
				try {
					boolean result = mRootService.copyFile(
							Environment.getExternalStorageAppFilesDirectory(
									mActivity.getPackageName())
									.getAbsolutePath()
									+ "/databases/" + DATABASE_NAME, mActivity
									.getDatabasePath(DATABASE_NAME)
									.getAbsolutePath(), true);
					// force the provider to read from the disk now
					final IContentProvider cp = mActivity.getContentResolver()
							.acquireProvider(Notifications.AUTHORITY);
					if (cp != null) {
						cp.call("clearCache", null, null);
					}
					Toast.makeText(mActivity,
							result ? "Restore successful" : "Restore failed!",
							Toast.LENGTH_SHORT).show();
				} catch (RemoteException e) {
					Toast.makeText(mActivity, "Restore failed!",
							Toast.LENGTH_SHORT).show();
				}
			}
			return true;
		} else if (preference instanceof TwoStatePreference) {
			((TwoStatePreference) preference)
					.setChecked(!((TwoStatePreference) preference).isChecked());
		}
		return false;
	}

	private void setupServices() {

		if (mServiceConnection == null) {
			mServiceConnection = new ServiceConnection() {

				@Override
				public void onServiceConnected(ComponentName name,
						IBinder service) {
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
			try {
				if (mActivity.bindService(
						new Intent(
								new Intent(mActivity.createPackageContext(
										"com.bamf.bamfutils", 0),
										BAMFRootService.class)),
						mServiceConnection, Context.BIND_AUTO_CREATE)) {
					Log.d(TAG, "service started and the bind was successful");
				}
			} catch (Exception e) {
				Log.e(TAG, "service did not start", e);
			}
		}
	}

	public void releaseService() {
		try {
			// this is an asynchronous call so we should not set the connection
			// variable to null here
			mActivity.unbindService(mServiceConnection);
			Log.d(TAG, "service was stopped and unbound");
		} catch (Exception e) {
		}
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		if (pref == mNotif) {
			Settings.System.putInt(mActivity.getContentResolver(),
					Settings.System.NOTIFICATION_MANAGER,
					(Boolean) newValue ? 1 : 0);
		}
		return false;
	}

}
