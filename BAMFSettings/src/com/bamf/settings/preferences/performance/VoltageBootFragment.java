package com.bamf.settings.preferences.performance;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import com.bamf.settings.R;

public class VoltageBootFragment extends PreferenceFragment implements OnPreferenceChangeListener {
	
	public static final String PREF_APPLY_ON_BOOT = "pref_voltage_AOB";
	
	private CheckBoxPreference mPrefAOB;
	
	private Activity mSettings;
	private SharedPreferences mPrefs;
	private Editor mEdit;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.voltage_on_boot);       
       
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        mSettings = getActivity();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mSettings);
    	mEdit = mPrefs.edit();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		
		mEdit.putBoolean(PREF_APPLY_ON_BOOT, (Boolean) newValue);
		mEdit.commit();
		return false;
	}   
    
    
}
