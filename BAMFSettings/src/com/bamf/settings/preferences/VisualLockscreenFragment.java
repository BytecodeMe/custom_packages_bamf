package com.bamf.settings.preferences;

import com.bamf.settings.R;
import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.provider.Settings;

public class VisualLockscreenFragment extends PreferenceFragment implements OnPreferenceChangeListener {
		
	private static final String LEFT_TARGET_PREF = "lockscreen_left_target_mode";
 /** If there is no setting in the provider, use this. */    
	
	private Activity mSettings;	
    private ContentResolver mResolver;    
    private ListPreference mTargetMode;
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.visual_lockscreen);
       
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	mSettings = getActivity();    	
    	mResolver = mSettings.getContentResolver();
    	
    	mTargetMode = (ListPreference) findPreference(LEFT_TARGET_PREF);
    	mTargetMode.setOnPreferenceChangeListener(this); 
    	int def = Settings.System.getInt(mResolver, "show_camera_lockscreen", 0);
    	mTargetMode.setSummary(mSettings.getResources().getStringArray(R.array.lockscreen_left_target_entries)[def]);
    	mTargetMode.setValueIndex(def);
    }		

	@Override
	public boolean onPreferenceChange(Preference pref, Object value) {
		
		if(pref == mTargetMode){
			Settings.System.putInt(mResolver,"show_camera_lockscreen",Integer.parseInt((String) value));
			mTargetMode.setSummary(mSettings.getResources().getStringArray(R.array.lockscreen_left_target_entries)[Integer.parseInt((String) value)]);
		}
		return true;
	}	
}
