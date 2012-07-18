/*
 * Copyright (C) 2011 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bamf.settings.preferences;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.bamf.settings.R;
import android.app.Notification.Notifications;

import com.bamf.settings.activities.QuickSettingsActivity;
import com.bamf.settings.utils.QuickSettingsUtil;
import com.bamf.settings.widgets.BAMFCheckBox;
import com.bamf.settings.widgets.BAMFCheckBox.OnPrefCreatedListener;
import com.bamf.settings.widgets.IconPreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuickSettingsFragment extends PreferenceFragment 
    implements OnPreferenceChangeListener, OnPrefCreatedListener, View.OnClickListener,
    DialogInterface.OnCancelListener {
	
	private static final String TAG = QuickSettingsFragment.class.getSimpleName();

    private static final String QUICKSETTINGS_CATEGORY = "pref_quick_settings";
    private static final String SELECT_SETTING_KEY_PREFIX = "pref_button_";
    private static final String CUSTOM_PRESS = "pref_custom_toggle";
    private static final String QUICKSETTING_VISIBILITY = "pref_notification_mode";
    private static final String QUICKSETTING_ANIMATIONS = "pref_animations";
    private static final String QUICKSETTING_BEHAVIOR = "pref_behavior";
    
    private static final String QUICK_CUSTOM = "QuickCustom";
    
    private static final int REQUEST_PICK_SHORTCUT = 1;
    private static final int REQUEST_PICK_APPLICATION = 2;
    private static final int REQUEST_CREATE_SHORTCUT = 3;
    
    private static final int MENU_CHANGEORDER = 1;
/*
    private static final String EXP_FLASH_MODE = "pref_flash_mode";
*/
    private HashMap<BAMFCheckBox, String> mCheckBoxPrefs = new HashMap<BAMFCheckBox, String>();
    private QuickSettingsActivity mActivity;
    private PackageManager pm;
    
    private IconPreference mCustomToggle;
    private BAMFCheckBox mCustomCheckBox;
    private ListPreference mQuickVisibility;
    private ListPreference mQuickAnimations;
    private ListPreference mQuickBehavior;
    private Resources mSystemUIResources;
    /*
    ListPreference mFlashMode;
	*/
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.quick_settings);
        this.setRetainInstance(true);
        this.setHasOptionsMenu(true);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        mActivity = (QuickSettingsActivity) getActivity();
        pm = mActivity.getPackageManager();

        if(pm != null) {
            try {
                mSystemUIResources = pm.getResourcesForApplication("com.android.systemui");
            } catch(Exception e) {
                mSystemUIResources = null;
                Log.e(TAG, "Could not load SystemUI resources", e);
            }
        }

        PreferenceScreen prefSet = getPreferenceScreen();

        mCustomToggle = (IconPreference)prefSet.findPreference(CUSTOM_PRESS);
        
        mQuickVisibility = (ListPreference) prefSet.findPreference(QUICKSETTING_VISIBILITY);
        mQuickVisibility.setOnPreferenceChangeListener(this);
        
        mQuickAnimations = (ListPreference) prefSet.findPreference(QUICKSETTING_ANIMATIONS);
        mQuickAnimations.setOnPreferenceChangeListener(this);
        
        mQuickBehavior = (ListPreference) prefSet.findPreference(QUICKSETTING_BEHAVIOR);
        mQuickBehavior.setOnPreferenceChangeListener(this);
        
        PreferenceCategory prefQuickSettings = (PreferenceCategory) prefSet.findPreference(QUICKSETTINGS_CATEGORY);

        // empty our preference category and set it to order as added
        prefQuickSettings.removeAll();
        prefQuickSettings.setOrderingAsAdded(false);

        // empty our checkbox map
        mCheckBoxPrefs.clear();

        // get our list of settings
        ArrayList<String> settingList = QuickSettingsUtil.getQuickSettingListFromString
                (QuickSettingsUtil.getCurrentQuickSettings(mActivity));

        // fill that checkbox map!
        for(QuickSettingsUtil.QuickSettingInfo qSetting : QuickSettingsUtil.SETTINGS.values()) {
            // create a checkbox
            BAMFCheckBox cb = new BAMFCheckBox(mActivity);
            if(qSetting.getId().equals(QUICK_CUSTOM)){
            	mCustomCheckBox = cb;
            }

            // set a dynamic key based on button id
            cb.setKey(SELECT_SETTING_KEY_PREFIX + qSetting.getId());

            // set vanity info
            cb.setTitle(qSetting.getTitleResId());
            
            // set icon
            cb.setIcon(getIconDrawable(qSetting.getIcon()));            
            
            // set our checked state
            if(settingList.contains(qSetting.getId())) {
                cb.setChecked(true);
            } else {
                cb.setChecked(false);
            }

            // add to our prefs set
            mCheckBoxPrefs.put(cb, qSetting.getId());

            // add to the category
            prefQuickSettings.addPreference(cb);
        }
        
        //PreferenceCategory prefOptions = (PreferenceCategory) prefSet.findPreference("category_options");
        //prefOptions.removeAll();
        //prefSet.removePreference(prefOptions);
        
        this.getListView().setPadding(30, 0, 60, 0);
        this.getListView().setFastScrollAlwaysVisible(true);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem mi;
        mi = menu.add(Menu.NONE, MENU_CHANGEORDER, Menu.NONE, "Change Order");
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case MENU_CHANGEORDER:
            	final FragmentTransaction trans = mActivity.getFragmentManager().beginTransaction();
                trans.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, 
                        R.anim.slide_in_left, R.anim.slide_out_right);
                if(mActivity.getFragmentManager().findFragmentByTag("basic").isVisible()){   
                    trans.addToBackStack("reorder");
                    trans.replace(R.id.container, Fragment.instantiate(mActivity, 
                    		QuickSettingsOrderFragment.class.getName(),
                            new Bundle()),"reorder").commit();
                }
                return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
    }
    
    private void updateSummaryText(ListPreference pref, int array, int value){
        try{
        	pref.setSummary(this.getResources().getStringArray(array)[value]);
        	pref.setValueIndex(value);
        }catch(Exception e){}
    }
    
    private Drawable resizeIcon(Drawable icon, float newWidth, float newHeight){
        BitmapDrawable bmp_icon = (BitmapDrawable)icon;
       Bitmap bmp = bmp_icon.getBitmap();
       
       float scaleWidth = ((float)newWidth) / bmp.getWidth();
       float scaleHeight = ((float)newHeight) / bmp.getHeight();
       
       Matrix matrix = new Matrix();
       matrix.postScale(scaleWidth, scaleHeight);
       Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
       return new BitmapDrawable(getResources(), resizedBitmap);
   }

    private Drawable getIconDrawable(String id) {
    	Drawable icon = null;
    	try {
		    if(mSystemUIResources.getIdentifier(id, null, null) > 0){
		    		icon = mSystemUIResources.getDrawable(mSystemUIResources.getIdentifier(id, null, null));
		    }
		    
		} catch (Throwable t) {	
				Log.e(TAG, "Could not load icon for ["+id+"]", t);
		}
    	
    	if(icon == null){
            icon = getResources().getDrawable(android.R.drawable.sym_def_app_icon);
        }
		return icon;
	}

	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // we only modify the settings list if it was one of our checks that was clicked
        boolean settingWasModified = false;
        ArrayList<String> settingList = new ArrayList<String>();
        for(Map.Entry<BAMFCheckBox, String> entry : mCheckBoxPrefs.entrySet()) {
            if(entry.getKey().isChecked()) {
                settingList.add(entry.getValue());
            }

            if(preference == entry.getKey()) {
                settingWasModified = true;
            }
            
            if(entry.getValue().equals(QUICK_CUSTOM) && entry.getKey().isChecked()){
            	if(Settings.System.getString(mActivity.getContentResolver(), 
            			Settings.System.QUICK_SETTINGS_CUSTOM) == null){
            		((BAMFCheckBox)preference).setChecked(false);
            		settingWasModified = false;
            		
            		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
            			.setTitle("Custom Setting")
            			.setMessage("You must first configure the setting before it can be used.")
            			.setNegativeButton(android.R.string.cancel, new OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();								
							}
            				
            			})
            			.setPositiveButton("Configure", new OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								pickShortcut();								
							}
            				
            			})
            			.setCancelable(true);
            		
            		AlertDialog dialog = builder.create();
            		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, 
            				WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            		dialog.show();            		
            	}
            }
        }

        if(settingWasModified) {
            //now we do some wizardry and reset the button list
            QuickSettingsUtil.saveCurrentQuickSettingsTemp(QuickSettingsUtil.mergeInNewSettingsString(
            		QuickSettingsUtil.getCurrentQuickSettings(mActivity), QuickSettingsUtil.getQuickSettingsFromList(settingList)));
        	QuickSettingsUtil.saveCurrentQuickSettings(mActivity);
            return true;
        }
        
        if (preference == mCustomToggle) {
            pickShortcut();
        }

        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.valueOf((String)newValue);
		if(preference == mQuickVisibility) {
            Settings.System.putInt(mActivity.getContentResolver(), Settings.System.SHOW_NOTIFICATIONS_DEFAULT, value);
            updateSummaryText(mQuickVisibility, R.array.notification_mode_entries, value);
        }else if(preference == mQuickAnimations){
        	Settings.System.putInt(mActivity.getContentResolver(), Settings.System.QUICK_SETTINGS_ANIMATION, value);
            updateSummaryText(mQuickAnimations, R.array.animation_entries, value);
        }else if(preference == mQuickBehavior){
        	Settings.System.putInt(mActivity.getContentResolver(), Settings.System.QUICK_SETTINGS_BEHAVIOR, value);
            updateSummaryText(mQuickBehavior, R.array.behavior_entries, value);
        }
        return true;
    }
    
    @Override
    public void onPrefCreated(String key) {
        //set the context button visible and allow clicks
        if(mCustomCheckBox.getKey().equals(key)){
            if(mCustomCheckBox.getContextButton()!=null){
                mCustomCheckBox.getContextButton().setVisibility(View.VISIBLE);
                mCustomCheckBox.getContextButton().setOnClickListener(this);
            }
        }
    }

    /**
     * this handles the click for the context menu
     * button that shows on the custom setting
     */
    @Override
    public void onClick(View v) {
        //show the user our context menu
        QuickSettingsUtil.CustomIconUtil.showContextMenu(this);
    }
    
    /**
     * this handles the cancel for the custom icon context menu
     * we need to refresh in case the user reverts to the default
     * icon
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        refreshCustomToggle();        
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	refreshCustomToggle();
    	
    	int notifcations = Settings.System.getInt(mActivity.getContentResolver(), 
    	        Settings.System.SHOW_NOTIFICATIONS_DEFAULT, 0);
        updateSummaryText(mQuickVisibility, R.array.notification_mode_entries, notifcations);
        
        int animations = Settings.System.getInt(mActivity.getContentResolver(), 
    	        Settings.System.QUICK_SETTINGS_ANIMATION, 1);
        updateSummaryText(mQuickAnimations, R.array.animation_entries, animations);
        
        int behavior = Settings.System.getInt(mActivity.getContentResolver(), 
    	        Settings.System.QUICK_SETTINGS_BEHAVIOR, 0);
        updateSummaryText(mQuickBehavior, R.array.behavior_entries, behavior);
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
    
    private void refreshCustomToggle(){
        String mCustomURI = Settings.System.getString(mActivity.getContentResolver(), 
        		Settings.System.QUICK_SETTINGS_CUSTOM);
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
        shortcutIcons.add(ShortcutIconResource.fromContext(mActivity, R.drawable.ic_lockscreen_apps));
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
        
        if (Settings.System.putString(mActivity.getContentResolver(), 
        		Settings.System.QUICK_SETTINGS_CUSTOM, intent.toUri(0))) {
        	setupCustomToggle(intent);
        }
    }
    
    void completeSetCustomApp(Intent data) {
        if (Settings.System.putString(mActivity.getContentResolver(), 
        		Settings.System.QUICK_SETTINGS_CUSTOM, data.toUri(0))) {
        	setupCustomToggle(data);
        }        
    }
    
    private void setupCustomToggle(Intent data){
    	try {
        	mCustomToggle.setSummary(pm.resolveActivity(data,0).activityInfo.loadLabel(pm));
        	mCustomToggle.setIcon(pm.getActivityIcon(data));
        	if(mCustomCheckBox != null){
        		mCustomCheckBox.setTitle("Custom");
        		mCustomCheckBox.setSummary(mCustomToggle.getSummary());
        		mCustomCheckBox.setIcon(resizeIcon(mCustomToggle.getIcon(), 96, 96));
        		
        		Drawable custom = QuickSettingsUtil.CustomIconUtil.loadFromFile(mActivity);
        		if(custom!=null){
        		    mCustomCheckBox.setIcon(custom);
        		}
        		mCustomCheckBox.setOnPrefCreatedListener(this);
        	}
		} catch (Throwable t) {}
    }
}
