package com.bamf.settings.preferences;

import com.bamf.settings.R;
import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.provider.Settings;

public class VisualLockscreenFragment extends PreferenceFragment implements OnPreferenceChangeListener {
		
	private static final String LEFT_TARGET_PREF = "lockscreen_left_target_mode";
	private static final String LOCK_UNLOCK_PREF = "pref_visual_basic_lock_unlock";
 /** If there is no setting in the provider, use this. */    
	
	private Activity mSettings;	
    private ContentResolver mResolver;    
    private ListPreference mTargetMode;
    private CheckBoxPreference mLockUnlock;
	 
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
    	int def = Settings.System.getInt(mResolver, Settings.System.SHOW_CAMERA_LOCKSCREEN, 1);
    	mTargetMode.setSummary(mSettings.getResources().getStringArray(R.array.lockscreen_left_target_entries)[def]);
    	mTargetMode.setValueIndex(def);
    	
    	mLockUnlock = (CheckBoxPreference) findPreference(LOCK_UNLOCK_PREF);
    	mLockUnlock.setChecked(Settings.System.getInt(mResolver, Settings.System.SHOW_LOCK_BEFORE_UNLOCK,0)==1);
    	mLockUnlock.setOnPreferenceChangeListener(this);
    }		

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		
		if(pref == mTargetMode){
			Settings.System.putInt(mResolver,Settings.System.SHOW_CAMERA_LOCKSCREEN,
					Integer.parseInt((String) newValue));
			mTargetMode.setSummary(mSettings.getResources().getStringArray(
					R.array.lockscreen_left_target_entries)[Integer.parseInt((String) newValue)]);
			return true;
		}else if(pref == mLockUnlock){
			int value = (Boolean) newValue? 1 : 0;
			Settings.System.putInt(mResolver,Settings.System.SHOW_LOCK_BEFORE_UNLOCK, value);
			return true;
		}
		return false;
	}	
}
