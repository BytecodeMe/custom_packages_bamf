package com.bamf.settings.preferences;

import android.app.Activity;
import android.app.Notification.Notifications;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.widget.Toast;

import com.bamf.settings.R;

public class VisualSystemUIFragment extends PreferenceFragment implements OnPreferenceChangeListener,OnPreferenceClickListener {
	
	private static final String PREF_USE_CUSTOM_CARRIER = "pref_use_custom_carrier_label";
	private static final String PREF_CUSTOM_CARRIER_LABEL = "pref_custom_carrier_label";	
	private static final String PREF_SCREENSHOT_ACTION = "pref_screenshot";
		
	private static final String TAG = VisualSystemUIFragment.class.getSimpleName();
	private static final boolean DEBUG = false;
	
	private Activity mSettings;
	private ContentResolver mResolver;		
	private CheckBoxPreference mUseCustomCarrier;
	private EditTextPreference mCarrierText;
	private ListPreference mScreenshotAction;
	
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.visual_sys_ui);
        
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	mSettings = getActivity();
    	mResolver = mSettings.getContentResolver();     	
    	
    	mUseCustomCarrier = (CheckBoxPreference) findPreference(PREF_USE_CUSTOM_CARRIER);    	
    	mUseCustomCarrier.setChecked(Settings.System.getInt(mResolver,Settings.System.USE_CUSTOM_CARRIER_LABEL,0) == 1);
    	mUseCustomCarrier.setOnPreferenceChangeListener(this);
    	
    	mCarrierText = (EditTextPreference) findPreference(PREF_CUSTOM_CARRIER_LABEL);
    	if(Settings.System.getString(mResolver,Settings.System.CUSTOM_CARRIER_LABEL)!=null){
    		mCarrierText.setSummary(Settings.System.getString(mResolver,Settings.System.CUSTOM_CARRIER_LABEL));
    	}
    	mCarrierText.setOnPreferenceChangeListener(this);
    	
    	mScreenshotAction = (ListPreference) findPreference(PREF_SCREENSHOT_ACTION);
    	mScreenshotAction.setOnPreferenceChangeListener(this);
    }

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		
		if(pref == mUseCustomCarrier){
			int value = (Boolean) newValue? 1 : 0;
			Settings.System.putInt(mResolver,Settings.System.USE_CUSTOM_CARRIER_LABEL,value);
			return true;
		}else if(pref == mCarrierText){
			Settings.System.putString(mResolver,Settings.System.CUSTOM_CARRIER_LABEL,newValue.toString());
			mCarrierText.setSummary(newValue.toString());
			return true;
		}else if(pref == mScreenshotAction){
			int value = Integer.valueOf(newValue.toString());
			Settings.System.putInt(mResolver,Settings.System.POST_SCREENSHOT_ACTION,value);
			updateSummaryText(mScreenshotAction, R.array.screenshot_entries, value);
			return true;
		}else
			return false;
	}	
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference instanceof TwoStatePreference)
			((TwoStatePreference) preference).setChecked(!((TwoStatePreference) preference).isChecked());

		return false;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		int action = Settings.System.getInt(getActivity().getContentResolver(), 
    	        Settings.System.POST_SCREENSHOT_ACTION, 1);
        updateSummaryText(mScreenshotAction, R.array.screenshot_entries, action);
	}
	
	private void updateSummaryText(ListPreference pref, int array, int value){
        try{
        	pref.setSummary(this.getResources().getStringArray(array)[value]);
        	pref.setValueIndex(value);
        }catch(Exception e){}
    }
	
}
