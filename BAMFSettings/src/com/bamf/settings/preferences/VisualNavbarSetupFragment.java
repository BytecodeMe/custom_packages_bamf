package com.bamf.settings.preferences;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.util.Log;

import com.bamf.settings.R;
import com.bamf.settings.widgets.BAMFSwitchPreference;

public class VisualNavbarSetupFragment extends PreferenceFragment implements OnPreferenceChangeListener,OnPreferenceClickListener {
	
	private static final String PREF_CURSOR = "pref_visual_navbar_cursor";
	private static final String PREF_NAVBAR_MENU = "pref_visual_navbar_menu";
	
	final static int MENU_DEFAULT = 0;
    final static int MENU_DEFAULT_HIDDEN = 1;
    final static int MENU_LEFT = 2;
    final static int MENU_LEFT_HIDDEN = 3;
    final static int MENU_BOTH = 4;
    final static int MENU_BOTH_HIDDEN = 5;
    final static int MENU_DISABLED = 6;
		
	private static final String TAG = VisualNavbarSetupFragment.class.getSimpleName();
	
	private static final boolean DEBUG = false;
	
	private Context mContext;
	private ContentResolver mResolver;
	private BAMFSwitchPreference mCursor;	
	private ListPreference mMenu;
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.visual_navbar_setup);
        
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	mContext = getActivity();
    	mResolver = mContext.getContentResolver();   	
    	
    	mCursor = (BAMFSwitchPreference) findPreference(PREF_CURSOR);
    	mCursor.setChecked(Settings.System.getInt(mResolver,Settings.System.SHOW_KEYBOARD_CURSOR,1) == 1);   
    	mCursor.setOnPreferenceChangeListener(this);
    	mCursor.setOnPreferenceClickListener(this);
    	
    	mMenu = (ListPreference) findPreference(PREF_NAVBAR_MENU);
    	int menuMode = Settings.System.getInt(mResolver,Settings.System.NAVBAR_MENU_MODE,MENU_DEFAULT);
    	mMenu.setValue(String.valueOf(menuMode));
    	mMenu.setOnPreferenceChangeListener(this);
    	mMenu.setSummary(mMenu.getEntry());   	
    	
    }    

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		
		if(pref == mCursor){
			int val = (Boolean) newValue? 1 : 0;
			Settings.System.putInt(mResolver,Settings.System.SHOW_KEYBOARD_CURSOR,val);
			return true;
		}else if(pref == mMenu){			
			int mode = Integer.parseInt((String) newValue);				
			Settings.System.putInt(mResolver,Settings.System.NAVBAR_MENU_MODE,mode);	
			mMenu.setSummary(mMenu.getEntries()[mode]);   			
			return true;
		}
		
		return false;
	}	
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		
		((TwoStatePreference) preference).setChecked(!((TwoStatePreference) preference).isChecked());
		
		return false;
	}	
}
