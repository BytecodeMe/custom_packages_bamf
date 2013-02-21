package com.bamf.settings.preferences;

import java.io.File;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.bamf.settings.R;
import com.bamf.settings.activities.BaseSettingsActivity;
import com.bamf.settings.preferences.performance.PerformanceVoltageFragment;

public class SettingsListFragment extends PreferenceFragment implements
		OnPreferenceClickListener {

	private static final String PREF_KERNEL = "pref_main_kernel";
	private static final String PREF_VOLTAGE = "pref_main_voltage";
	private static final String PREF_SYSTEM_BASIC = "pref_main_system_basic";
	private static final String PREF_SYSTEM_NOTIF = "pref_main_system_notif";
	private static final String PREF_VISUAL_BASIC = "pref_main_visual_basic";
	private static final String PREF_VISUAL_STATUS = "pref_main_visual_status_bar";
	private static final String PREF_VISUAL_NAVBAR = "pref_main_visual_navbar";

	private Preference mKernel;
	private Preference mVoltage;
	private Preference mSystemBasic;
	private Preference mSystemNotif;
	private Preference mVisualBasic;
	private Preference mVisualStatus;
	private Preference mVisualNav;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);			
		
		Context newContext = new ContextThemeWrapper(getActivity(),R.style.ThemeLight);		
		addPreferencesFromResource(newContext,R.xml.main_prefs);		

	}		

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		PreferenceScreen prefSet = getPreferenceScreen();

		mKernel = (Preference) findPreference(PREF_KERNEL);
		mKernel.setOnPreferenceClickListener(this);

		mVoltage = (Preference) findPreference(PREF_VOLTAGE);
		mVoltage.setOnPreferenceClickListener(this);

		mSystemBasic = (Preference) findPreference(PREF_SYSTEM_BASIC);
		mSystemBasic.setOnPreferenceClickListener(this);

		mSystemNotif = (Preference) findPreference(PREF_SYSTEM_NOTIF);
		mSystemNotif.setOnPreferenceClickListener(this);

		mVisualBasic = (Preference) findPreference(PREF_VISUAL_BASIC);
		mVisualBasic.setOnPreferenceClickListener(this);

		mVisualStatus = (Preference) findPreference(PREF_VISUAL_STATUS);
		mVisualStatus.setOnPreferenceClickListener(this);

		mVisualNav = (Preference) findPreference(PREF_VISUAL_NAVBAR);
		mVisualNav.setOnPreferenceClickListener(this);

		if (!hasVoltageOptions()) {
			prefSet.removePreference(mVoltage);
		}
		
		final ListView list = getListView();
		list.setBackground(getResources().getDrawable(R.drawable.background_holo_light));
		list.setDivider(getResources().getDrawable(R.drawable.list_divider_holo_light));
	}	

	public static boolean hasVoltageOptions() {

		File f = new File(PerformanceVoltageFragment.VOLTAGE_TABLE);
		return f.exists();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {

		int position = 0;
		final boolean volts = hasVoltageOptions();

		if (preference == mKernel) {
			position = 0;
		} else if (preference == mVoltage) {
			position = 1;
		} else if (preference == mSystemBasic) {
			position = (volts ? 2 : 1);
		} else if (preference == mSystemNotif) {
			position = (volts ? 3 : 2);
		} else if (preference == mVisualBasic) {
			position = (volts ? 4 : 3);
		} else if (preference == mVisualStatus) {
			position = (volts ? 5 : 4);
		} else if (preference == mVisualNav) {
			position = (volts ? 6 : 5);
		}

		switchFragment(position);
		return false;
	}

	// the meat of switching the above fragment
	private void switchFragment(int position) {
		if (getActivity() == null)
			return;

		if (getActivity() instanceof BaseSettingsActivity) {
			BaseSettingsActivity fca = (BaseSettingsActivity) getActivity();
			fca.switchContent(position);
		}
	}
}
