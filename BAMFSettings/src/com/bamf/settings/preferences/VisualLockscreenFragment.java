package com.bamf.settings.preferences;

import com.bamf.settings.R;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.provider.Settings;

public class VisualLockscreenFragment extends PreferenceFragment implements OnPreferenceChangeListener,OnPreferenceClickListener {
		
	private static final String ALL_WIDGETS_PREF = "lockscreen_all_widgets";
	private static final String SHOW_UNLOCK_FIRST = "lockscreen_show_unlock";
	private static final String ENABLE_QUICK_PREF = "pref_visual_basic_enable_quicklaunch";
	private static final String MANAGE_QUICK_PREF = "pref_visual_basic_manage_quicklaunch";
	private static final String ALWAYS_QUICK_PREF = "pref_visual_basic_always_quicklaunch";
	
 /** If there is no setting in the provider, use this. */    
	
	private Activity mSettings;	
    private ContentResolver mResolver;    
    private CheckBoxPreference mAllWidgets;  
    private CheckBoxPreference mEnableQuick;
    private CheckBoxPreference mAlwaysQuick;
    private CheckBoxPreference mShowUnlock;
    private Preference mManageQuick;
	 
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
    	
    	mAllWidgets = (CheckBoxPreference) findPreference(ALL_WIDGETS_PREF);
    	mAllWidgets.setOnPreferenceChangeListener(this);    	
    	mAllWidgets.setChecked(Settings.System.getInt(mResolver, Settings.System.ALLOW_ALL_WIDGETS, 0) == 1);  
    	
    	mEnableQuick = (CheckBoxPreference) findPreference(ENABLE_QUICK_PREF);
    	mEnableQuick.setOnPreferenceChangeListener(this);    	
    	mEnableQuick.setChecked(Settings.System.getInt(mResolver, Settings.System.ENABLE_QUICK_LAUNCH, 0) == 1);  
    	
    	mAlwaysQuick = (CheckBoxPreference) findPreference(ALWAYS_QUICK_PREF);
    	mAlwaysQuick.setOnPreferenceChangeListener(this);    	
    	mAlwaysQuick.setChecked(Settings.System.getInt(mResolver, Settings.System.ALWAYS_QUICK_LAUNCH, 1) == 1); 
    	
    	mShowUnlock = (CheckBoxPreference) findPreference(SHOW_UNLOCK_FIRST);
    	mShowUnlock.setOnPreferenceChangeListener(this);    	
    	mShowUnlock.setChecked(Settings.System.getInt(mResolver, Settings.System.SHOW_LOCK_BEFORE_UNLOCK, 0) == 1); 
    	
    	mManageQuick = (Preference) findPreference(MANAGE_QUICK_PREF);
    	mManageQuick.setOnPreferenceClickListener(this);
    	
    }		

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {		

		if(pref == mAllWidgets){
			Settings.System.putInt(mResolver,Settings.System.ALLOW_ALL_WIDGETS,
					(Boolean) newValue? 1 : 0);			
			return true;
		}else if(pref == mEnableQuick){
			Settings.System.putInt(mResolver,Settings.System.ENABLE_QUICK_LAUNCH,
					(Boolean) newValue? 1 : 0);	
			Settings.System.putInt(mResolver,Settings.System.ALWAYS_QUICK_LAUNCH,
					(Boolean) newValue? 1 : 0);	
			mAlwaysQuick.setChecked((Boolean) newValue);
			return true;
		}else if(pref == mAlwaysQuick){				
			Settings.System.putInt(mResolver,Settings.System.ALWAYS_QUICK_LAUNCH,
					(Boolean) newValue? 1 : 0);	
			return true;
		}else if(pref == mShowUnlock){				
			Settings.System.putInt(mResolver,Settings.System.SHOW_LOCK_BEFORE_UNLOCK,
					(Boolean) newValue? 1 : 0);	
			return true;
		}
		return false;
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {
		
		if(pref == mManageQuick){			
			startActivity(new Intent("com.bamf.settings.quicklaunch"));
			return true;
		}
		return false;
	}	
}
