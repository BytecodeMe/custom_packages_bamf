package com.bamf.settings.preferences;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.bamf.settings.R;
import com.bamf.settings.activities.SettingsActivity;
import com.bamf.settings.widgets.BAMFPreference;
import com.bamf.settings.widgets.BAMFSwitchPreference;
import com.bamf.settings.widgets.ColorPickerDialog;
import com.bamf.settings.widgets.IconPreference;

public class VisualNavbarFragment extends PreferenceFragment implements OnPreferenceChangeListener,OnPreferenceClickListener{
	
	private static final String PREF_NAVBAR_REFLECT = "pref_visual_navbar_reflect";
	private static final String PREF_NAVBAR_SEARCH = "pref_visual_navbar_search";
	private static final String PREF_SEARCH_LONG_PRESS_CHECK = "pref_long_press_def";
	private static final String PREF_SEARCH_LONG_PRESS_ACTIVITY = "pref_custom_search";
	private static final String PREF_COLOR_PICKER = "pref_color_picker";
	private static final String PREF_GLOW_PICKER = "pref_glow_picker";
	
	private static final int REQUEST_PICK_SHORTCUT = 1;
    private static final int REQUEST_PICK_APPLICATION = 2;
    private static final int REQUEST_CREATE_SHORTCUT = 3;
    
    private static final int GLOW_COLOR_DEFAULT = 0x7d00c3ff;
	
	private SettingsActivity mSettings;
	private ContentResolver mResolver;
	
	private BAMFSwitchPreference mNavbarReflect;
	private BAMFSwitchPreference mNavbarSearch;
	private CheckBoxPreference mUseSearchDefault;
	private IconPreference mSearchActivity;
	private BAMFPreference mColorPickerPref;
	private Preference mGlowPickerPref;
	
	
	private PackageManager pm;
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.visual_navbar);
        
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	mSettings = (SettingsActivity) getActivity();
    	mResolver = mSettings.getContentResolver(); 
    	pm = mSettings.getPackageManager();
    	
    	mNavbarReflect = (BAMFSwitchPreference) findPreference(PREF_NAVBAR_REFLECT);
        mNavbarReflect.setChecked(Settings.System.getInt(mResolver,
        		Settings.System.SHOW_NAVBAR_REFLECTION, 0) != 0);
        mNavbarReflect.setOnPreferenceChangeListener(this);
        mNavbarReflect.setOnPreferenceClickListener(this);
        
    	mNavbarSearch = (BAMFSwitchPreference) findPreference(PREF_NAVBAR_SEARCH);
        mNavbarSearch.setChecked(Settings.System.getInt(mResolver,
        		Settings.System.SHOW_NAVBAR_SEARCH, 0) != 0);
        mNavbarSearch.setOnPreferenceChangeListener(this);
        mNavbarSearch.setOnPreferenceClickListener(this);
        
        mColorPickerPref = (BAMFPreference) findPreference(PREF_COLOR_PICKER);
        mGlowPickerPref = (Preference) findPreference(PREF_GLOW_PICKER);
        
        
        mUseSearchDefault = (CheckBoxPreference) findPreference(PREF_SEARCH_LONG_PRESS_CHECK);
        mUseSearchDefault.setChecked(Settings.System.getInt(mSettings.getContentResolver(),
                     Settings.System.USE_CUSTOM_LONG_SEARCH_APP_TOGGLE, 0)==1);
        if(mUseSearchDefault.isChecked()){
            mUseSearchDefault.setSummary(R.string.pref_lng_press_default_summary_on);
        }else{
            mUseSearchDefault.setSummary(R.string.pref_lng_press_default_summary);
        }
        mSearchActivity = (IconPreference) findPreference(PREF_SEARCH_LONG_PRESS_ACTIVITY);
        
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;
				ListAdapter listAdapter = listView.getAdapter();
				Object obj = listAdapter.getItem(position);
				if (obj != null && obj instanceof View.OnLongClickListener) {
					View.OnLongClickListener longListener = (View.OnLongClickListener) obj;
					return longListener.onLongClick(view);
				}
				return false;
			}
		});
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if(preference == mUseSearchDefault){
            Settings.System.putInt(mSettings.getContentResolver(),
                     Settings.System.USE_CUSTOM_LONG_SEARCH_APP_TOGGLE, mUseSearchDefault.isChecked()?1:0);
            if(mUseSearchDefault.isChecked()){
                mUseSearchDefault.setSummary(R.string.pref_lng_press_default_summary_on);
            }else{
                mUseSearchDefault.setSummary(R.string.pref_lng_press_default_summary);
            }
            return true;
        }else if(preference == mSearchActivity){
            pickShortcut();
            return true;
        }else if(preference == mColorPickerPref){
            showColorPicker(Settings.System.getInt(mResolver, Settings.System.NAVBAR_BUTTON_COLOR, Color.WHITE),
            		R.string.title_color_picker_dialog);
            return true;
        }else if(preference == mGlowPickerPref){
        	// don't default this and the buttons to the same color
        	showColorPicker(Settings.System.getInt(mResolver, Settings.System.NAVBAR_GLOW_COLOR, GLOW_COLOR_DEFAULT),
        			R.string.title_glow_picker_dialog);
            return true;
        }
        
        return false;
    }
    
    @Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
    	
    	if (pref == mNavbarSearch) {    		
            Settings.System.putInt(mResolver, Settings.System.SHOW_NAVBAR_SEARCH,
                    (Boolean) newValue ? 1 : 0);
            return true;
    	}else if(pref == mNavbarReflect) {
    		Settings.System.putInt(mResolver, Settings.System.SHOW_NAVBAR_REFLECTION,
                    (Boolean) newValue ? 1 : 0);
            return true;
    	}else 
    		return false;    	
    	
    }

	@Override
	public boolean onPreferenceClick(Preference preference) {
	    if(preference instanceof TwoStatePreference){
	        ((TwoStatePreference) preference).setChecked(!((TwoStatePreference) preference).isChecked());
	    }
		return false;
	}
	
	@Override
    public void onResume(){
        super.onResume();
        refreshCustomToggle();
    }
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == android.app.Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_APPLICATION:
                    completeSetCustomApp(data);
                    break;
                case REQUEST_CREATE_SHORTCUT:
                    completeSetCustomShortcut(data);
                    break;
                case REQUEST_PICK_SHORTCUT:
                    processShortcut(data, REQUEST_PICK_APPLICATION, REQUEST_CREATE_SHORTCUT);
                    break;
            }
        }
    }
	
	private void showColorPicker(int color, final int title){
        final ColorPickerDialog d = new ColorPickerDialog(mSettings, color);
        d.setAlphaSliderVisible(true);
        d.setTitle(title);
        d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: save new color setting
            	if(title==R.string.title_color_picker_dialog){
            		Settings.System.putInt(mResolver, Settings.System.NAVBAR_BUTTON_COLOR, d.getColor());
            	}else{
            		Settings.System.putInt(mResolver, Settings.System.NAVBAR_GLOW_COLOR, d.getColor());
            	}
            }
        });
        
        d.setButton(DialogInterface.BUTTON_NEUTRAL, "Default", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            	if(title==R.string.title_color_picker_dialog){
            		Settings.System.putInt(mResolver, Settings.System.NAVBAR_BUTTON_COLOR, Color.WHITE);
            	}else{
            		Settings.System.putInt(mResolver, Settings.System.NAVBAR_GLOW_COLOR, GLOW_COLOR_DEFAULT);
            	}
            }
        });

        d.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                d.dismiss();
            }
        });

        d.show();

    }
    
    private void refreshCustomToggle(){
        String mCustomURI = Settings.System.getString(mSettings.getContentResolver(), 
                Settings.System.USE_CUSTOM_LONG_SEARCH_APP_ACTIVITY);
        if(mCustomURI != null){
            try {
                setupCustomToggle(Intent.parseUri(mCustomURI, 0));
            } catch (Throwable t) {}
        }
    }
    
    private void pickShortcut() {
        Bundle bundle = new Bundle();

        ArrayList<String> shortcutNames = new ArrayList<String>();
        shortcutNames.add(getString(R.string.group_applications));
        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

        ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
        shortcutIcons.add(ShortcutIconResource.fromContext(mSettings, R.drawable.ic_lockscreen_apps));
        bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
        pickIntent.putExtra(Intent.EXTRA_TITLE, getText(R.string.use_custom_title));
        pickIntent.putExtras(bundle);

        startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
    }
    
    void processShortcut(Intent intent, int requestCodeApplication, int requestCodeShortcut) {
        // Handle case where user selected "Applications"
        String applicationName = getResources().getString(R.string.group_applications);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        if (applicationName != null && applicationName.equals(shortcutName)) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
            startActivityForResult(pickIntent, requestCodeApplication);
        } else {
            startActivityForResult(intent, requestCodeShortcut);
        }
    }
    
    void completeSetCustomShortcut(Intent data) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        
        if (Settings.System.putString(mSettings.getContentResolver(), 
                Settings.System.USE_CUSTOM_LONG_SEARCH_APP_ACTIVITY, intent.toUri(0))) {
            setupCustomToggle(intent);
        }
    }
    
    void completeSetCustomApp(Intent data) {
        if (Settings.System.putString(mSettings.getContentResolver(), 
                Settings.System.USE_CUSTOM_LONG_SEARCH_APP_ACTIVITY, data.toUri(0))) {
            setupCustomToggle(data);
        }        
    }
    
    private void setupCustomToggle(Intent data){
        try {
            mSearchActivity.setSummary(pm.resolveActivity(data,0).activityInfo.loadLabel(pm));
            mSearchActivity.setIcon(pm.getActivityIcon(data));
        } catch (Throwable t) {}
    }

}
