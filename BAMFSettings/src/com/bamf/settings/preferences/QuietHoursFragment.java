/*
* Copyright (C) 2012 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.bamf.settings.preferences;

import com.bamf.settings.R;
import com.bamf.settings.activities.NotificationManagerActivity;
import com.bamf.settings.widgets.TimeRangePreference;

import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

public class QuietHoursFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "QuietHours";
    private static final String KEY_QUIET_HOURS_MUTE = "quiet_hours_mute";
    private static final String KEY_QUIET_HOURS_STILL = "quiet_hours_still";
    private static final String KEY_QUIET_HOURS_DIM = "quiet_hours_dim";
    private static final String KEY_QUIET_HOURS_HAPTIC = "quiet_hours_haptic";
    private static final String KEY_QUIET_HOURS_NOTE = "quiet_hours_note";
    private static final String KEY_QUIET_HOURS_TIMERANGE = "quiet_hours_timerange";

    private Preference mQuietHoursNote;
    private CheckBoxPreference mQuietHoursMute;
    private CheckBoxPreference mQuietHoursStill;
    private CheckBoxPreference mQuietHoursDim;
    private CheckBoxPreference mQuietHoursHaptic;
    private TimeRangePreference mQuietHoursTimeRange;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        if (getPreferenceManager() != null) {
            addPreferencesFromResource(R.xml.quiet_hours_settings);

            ContentResolver resolver = getActivity().getApplicationContext().getContentResolver();

            PreferenceScreen prefSet = getPreferenceScreen();

            // Load the preferences
            mQuietHoursNote = prefSet.findPreference(KEY_QUIET_HOURS_NOTE);
            
            mQuietHoursTimeRange = (TimeRangePreference) prefSet.findPreference(KEY_QUIET_HOURS_TIMERANGE);
            mQuietHoursMute = (CheckBoxPreference) prefSet.findPreference(KEY_QUIET_HOURS_MUTE);
            mQuietHoursStill = (CheckBoxPreference) prefSet.findPreference(KEY_QUIET_HOURS_STILL);
            mQuietHoursHaptic = (CheckBoxPreference) prefSet.findPreference(KEY_QUIET_HOURS_HAPTIC);
            mQuietHoursDim = (CheckBoxPreference) findPreference(KEY_QUIET_HOURS_DIM);

            // Remove the "Incoming calls behaviour" note if the device does not support phone calls
            if (mQuietHoursNote != null && getResources().getBoolean(com.android.internal.R.bool.config_voice_capable) == false) {
                getPreferenceScreen().removePreference(mQuietHoursNote);
            }

            // Set the preference state and listeners where applicable
            mQuietHoursTimeRange.setTimeRange(Settings.System.getIntForUser(resolver, Settings.System.QUIET_HOURS_START, 0, UserHandle.USER_CURRENT),
                    Settings.System.getIntForUser(resolver, Settings.System.QUIET_HOURS_END, 0, UserHandle.USER_CURRENT));
            mQuietHoursTimeRange.setOnPreferenceChangeListener(this);
            mQuietHoursMute.setChecked(Settings.System.getIntForUser(resolver, Settings.System.QUIET_HOURS_MUTE, 0, UserHandle.USER_CURRENT) == 1);
            mQuietHoursStill.setChecked(Settings.System.getIntForUser(resolver, Settings.System.QUIET_HOURS_STILL, 0, UserHandle.USER_CURRENT) == 1);
            mQuietHoursHaptic.setChecked(Settings.System.getIntForUser(resolver, Settings.System.QUIET_HOURS_HAPTIC, 0, UserHandle.USER_CURRENT) == 1);

            // Remove the notification light setting if the device does not support it
            if (mQuietHoursDim != null && getResources().getBoolean(com.android.internal.R.bool.config_intrusiveNotificationLed) == false) {
                getPreferenceScreen().removePreference(mQuietHoursDim);
            } else {
                mQuietHoursDim.setChecked(Settings.System.getInt(resolver, Settings.System.QUIET_HOURS_DIM, 0) == 1);
            }
        }
    }
    
    @Override 
	public void onStart(){
		super.onStart();
		setupActionBar();
		
		// TODO: setup preferences here
	}
    
    @Override
	public void onResume(){
		super.onResume();
		boolean enabled = ((NotificationManagerActivity) getActivity()).getSwitchChecked();
		getPreferenceScreen().setEnabled(enabled);

	}
	
	private void setupActionBar(){
		((NotificationManagerActivity) getActivity()).setupFragmentActionBar(NotificationManagerActivity.FRAGMENT_PREFERENCE, "Quiet Hours");
		
		boolean enabled = Settings.System.getIntForUser(getActivity().getContentResolver(), 
				Settings.System.QUIET_HOURS_ENABLED,0, UserHandle.USER_CURRENT)==1;
		((NotificationManagerActivity) getActivity()).setSwitchChecked(enabled);
	}

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver resolver = getActivity().getApplicationContext().getContentResolver();

        if (preference == mQuietHoursMute) {
            Settings.System.putIntForUser(resolver, Settings.System.QUIET_HOURS_MUTE,
                    mQuietHoursMute.isChecked() ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQuietHoursStill) {
            Settings.System.putIntForUser(resolver, Settings.System.QUIET_HOURS_STILL,
                    mQuietHoursStill.isChecked() ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQuietHoursDim) {
            Settings.System.putIntForUser(resolver, Settings.System.QUIET_HOURS_DIM,
                    mQuietHoursDim.isChecked() ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQuietHoursHaptic) {
            Settings.System.putIntForUser(resolver, Settings.System.QUIET_HOURS_HAPTIC,
                    mQuietHoursHaptic.isChecked() ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getApplicationContext().getContentResolver();
        if (preference == mQuietHoursTimeRange) {
            Settings.System.putIntForUser(resolver, Settings.System.QUIET_HOURS_START,
                    mQuietHoursTimeRange.getStartTime(), UserHandle.USER_CURRENT);
            Settings.System.putIntForUser(resolver, Settings.System.QUIET_HOURS_END,
                    mQuietHoursTimeRange.getEndTime(), UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }
}
