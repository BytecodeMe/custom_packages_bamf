package com.bamf.settings.preferences;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.View.OnClickListener;

import com.bamf.settings.R;
import com.bamf.settings.activities.NotificationManagerActivity;
import com.bamf.settings.widgets.BamfLedPreference;
import com.bamf.settings.widgets.BamfVibratePreference;

public class NotificationGeneralFragment extends PreferenceFragment implements
		OnClickListener, OnPreferenceChangeListener {
	
	private NotificationManagerActivity mActivity;
	
	private BamfLedPreference mDefaultLED;
	private BamfVibratePreference mDefaultVibrate;
	private CheckBoxPreference mProximity;
	private CheckBoxPreference mLEDScreenMode;
	private CheckBoxPreference mVibrateScreenMode;
	private CheckBoxPreference mClearLED;
	private CheckBoxPreference mShowAllLED;
	private CheckBoxPreference mSoundScreenMode;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.notification_settings);
        mActivity = (NotificationManagerActivity) getActivity();

        PreferenceScreen prefSet = getPreferenceScreen();
        
        mDefaultLED = new BamfLedPreference(mActivity, null);
        mDefaultLED.setTitle("Default LED");
        mDefaultLED.setSummary("Change the default system settings");
        mDefaultLED.setDialogTitle("Setup LED");
        mDefaultLED.setDialogMessage("Configure default LED settings");
        mDefaultLED.setPersistent(true);
        mDefaultLED.setOnPreferenceChangeListener(this);
        prefSet.addPreference(mDefaultLED);
        
        mLEDScreenMode = new CheckBoxPreference(mActivity);
        mLEDScreenMode.setTitle("LED Screen Mode");
        mLEDScreenMode.setSummary("Show LED notifications when the screen is on");
        prefSet.addPreference(mLEDScreenMode);
        
        mShowAllLED = new CheckBoxPreference(mActivity);
        mShowAllLED.setTitle("LED History");
        mShowAllLED.setSummary("Cycle through all current LED notifications");
        prefSet.addPreference(mShowAllLED);
        
        mClearLED = new CheckBoxPreference(mActivity);
        mClearLED.setTitle("Clear LED Behavior");
        mClearLED.setSummary("Clear all current LEDs when the notification area is visible");
        prefSet.addPreference(mClearLED);
        
        mDefaultVibrate = new BamfVibratePreference(mActivity);
        mDefaultVibrate.setTitle("Default Vibrate Pattern");
        mDefaultVibrate.setSummary("Change the default system settings");
        mDefaultVibrate.setDialogTitle("Default vibrate pattern");
        mDefaultVibrate.setDialogMessage("Configure a custom haptic feedback pattern");
        mDefaultVibrate.setPersistent(true);
        mDefaultVibrate.setOnPreferenceChangeListener(this);
        prefSet.addPreference(mDefaultVibrate);
        
        mVibrateScreenMode = new CheckBoxPreference(mActivity);
        mVibrateScreenMode.setTitle("Vibrate Screen Mode");
        mVibrateScreenMode.setSummary("Vibrate when the screen is on");
        prefSet.addPreference(mVibrateScreenMode);
        
        mSoundScreenMode = new CheckBoxPreference(mActivity);
        mSoundScreenMode.setTitle("Sound Screen Mode");
        mSoundScreenMode.setSummary("Mute sounds when the screen is on");
        prefSet.addPreference(mSoundScreenMode);
        
        mProximity = new CheckBoxPreference(mActivity);
        mProximity.setTitle("Pocket Mode");
        mProximity.setSummary("Do not wake the screen while the proximity sensor is covered");
        prefSet.addPreference(mProximity);
        
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
	}
	
	@Override 
	public void onStart(){
		super.onStart();
		setupActionBar();
		
		// TODO: setup preferences here
	}
	
	private void setupActionBar(){
		mActivity.setupFragmentActionBar(NotificationManagerActivity.FRAGMENT_GENERAL);	
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		return false;
	}

}
