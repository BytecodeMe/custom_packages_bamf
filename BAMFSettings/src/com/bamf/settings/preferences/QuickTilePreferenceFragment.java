package com.bamf.settings.preferences;

import com.bamf.settings.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class QuickTilePreferenceFragment extends PreferenceFragment {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.quick_settings);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

}
