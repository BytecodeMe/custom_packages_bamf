package com.bamf.settings.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.provider.Settings;
import android.text.util.QuickTileToken;
import android.text.util.QuickTileTokenizer;
import android.util.ConfigHashMap;
import android.util.Log;

import com.bamf.settings.R;

public class QuickTileHelper {
    
    private static final char SETTING_DELIMITER = '|';

	private static final boolean DEBUG = false;
    
    private Context mContext;
    private HashMap<String, QuickSettingInfo> mSettings;
    private ConfigHashMap<String, Boolean> mConfigs;
    
    public QuickTileHelper (Context context){
    	mContext = context;
    	mSettings = new HashMap<String, QuickSettingInfo>();
    	mConfigs = new ConfigHashMap<String, Boolean>();
    	setup();
    }
    
    /**
     * This will populate {@code mSettings} with available settings
     */
    private void setup() {
    	mConfigs.clear();
    	
    	mConfigs.put(Settings.System.QUICK_TORCH, mContext.getResources()
        		.getBoolean(com.android.internal.R.bool.config_allowQuickSettingTorch));
        mConfigs.put(Settings.System.QUICK_LTE, mContext.getResources()
        		.getBoolean(com.android.internal.R.bool.config_allowQuickSettingLTE));
        mConfigs.put(Settings.System.QUICK_MOBILE_DATA, mContext.getResources()
        		.getBoolean(com.android.internal.R.bool.config_allowQuickSettingMobileData));
        mConfigs.put(Settings.System.QUICK_HOTSPOT, mContext.getResources()
        		.getBoolean(com.android.internal.R.bool.config_allowQuickSettingMobileData));
        mConfigs.put(Settings.System.QUICK_TETHER, mContext.getResources()
        		.getBoolean(com.android.internal.R.bool.config_allowQuickSettingMobileData));
        
    	mSettings.clear();
    	
    	mSettings.put(Settings.System.QUICK_USER, new QuickTileHelper.QuickSettingInfo(
        		Settings.System.QUICK_USER, R.string.title_toggle_user, "com.android.systemui:drawable/ic_qs_default_user_light_grey"));
    	mSettings.put(Settings.System.QUICK_ALARM, new QuickTileHelper.QuickSettingInfo(
        		Settings.System.QUICK_ALARM, R.string.title_toggle_alarm, "com.android.systemui:drawable/ic_qs_alarm_off"));
        mSettings.put(Settings.System.QUICK_AIRPLANE, new QuickTileHelper.QuickSettingInfo(
        		Settings.System.QUICK_AIRPLANE, R.string.title_toggle_airplane, "com.android.systemui:drawable/ic_qs_airplane_off"));
        mSettings.put(Settings.System.QUICK_ROTATE, new QuickTileHelper.QuickSettingInfo(
        		Settings.System.QUICK_ROTATE, R.string.title_toggle_autorotate, "com.android.systemui:drawable/ic_notify_rotation_on_normal"));
        mSettings.put(Settings.System.QUICK_BATTERY, new QuickTileHelper.QuickSettingInfo(
        		Settings.System.QUICK_BATTERY, R.string.title_toggle_battery, "com.android.systemui:drawable/ic_qs_battery_bolt_light_grey"));
        mSettings.put(Settings.System.QUICK_BRIGHTNESS, new QuickTileHelper.QuickSettingInfo(
        		Settings.System.QUICK_BRIGHTNESS, R.string.title_toggle_brightness, "com.android.systemui:drawable/ic_qs_brightness_auto_on"));
        mSettings.put(Settings.System.QUICK_NODISTURB, new QuickTileHelper.QuickSettingInfo(
        		Settings.System.QUICK_NODISTURB, R.string.title_toggle_donotdisturb, "com.android.systemui:drawable/ic_notification_open"));
        mSettings.put(Settings.System.QUICK_SETTING, new QuickTileHelper.QuickSettingInfo(
        		Settings.System.QUICK_SETTING, R.string.title_toggle_settings, "com.android.systemui:drawable/ic_notify_settings_normal"));
        mSettings.put(Settings.System.QUICK_WIFI, new QuickTileHelper.QuickSettingInfo(
        		Settings.System.QUICK_WIFI, R.string.title_toggle_wifi, "com.android.systemui:drawable/ic_qs_wifi_4"));
        //mSettings.put(Settings.System.QUICK_VOLUME, new QuickTileHelper.QuickSettingInfo(
        //		Settings.System.QUICK_VOLUME, R.string.title_toggle_volume, "com.android.systemui:drawable/ic_lock_silent_mode_off"));
        mSettings.put(Settings.System.QUICK_CUSTOM, new QuickTileHelper.QuickSettingInfo(
        		Settings.System.QUICK_CUSTOM, R.string.title_toggle_custom, "com.android.systemui:drawable/ic_sysbar_custom"));
        mSettings.put(Settings.System.QUICK_ADB, new QuickTileHelper.QuickSettingInfo(
        		Settings.System.QUICK_ADB, R.string.title_toggle_adb, "com.android.systemui:drawable/ic_sysbar_adb_off"));
        mSettings.put(Settings.System.QUICK_BLUETOOTH, new QuickTileHelper.QuickSettingInfo(
        		Settings.System.QUICK_BLUETOOTH, R.string.title_toggle_bluetooth, "com.android.systemui:drawable/ic_qs_bluetooth_off"));  
        mSettings.put(Settings.System.QUICK_GPS, new QuickTileHelper.QuickSettingInfo(
        		Settings.System.QUICK_GPS, R.string.title_toggle_gps, "com.android.systemui:drawable/ic_qs_location_off"));
        mSettings.put(Settings.System.QUICK_SYNC, new QuickTileHelper.QuickSettingInfo(
        		Settings.System.QUICK_SYNC, R.string.title_toggle_sync, "com.android.systemui:drawable/ic_qs_sync_off"));
        mSettings.put(Settings.System.QUICK_MEDIA, new QuickTileHelper.QuickSettingInfo(
        		Settings.System.QUICK_MEDIA, R.string.title_toggle_media, "com.android.systemui:drawable/ic_sysbar_musicplayer_lightgrey"));
        
        // these settings are visible based on the device config
        if(mConfigs.get(Settings.System.QUICK_MOBILE_DATA)){
        	mSettings.put(Settings.System.QUICK_MOBILE_DATA, new QuickTileHelper.QuickSettingInfo(
        			Settings.System.QUICK_MOBILE_DATA, R.string.title_toggle_mobiledata, "com.android.systemui:drawable/ic_qs_mobile_data_off"));
        	mSettings.put(Settings.System.QUICK_SIGNAL, new QuickTileHelper.QuickSettingInfo(
        			Settings.System.QUICK_SIGNAL, R.string.title_toggle_signal, "com.android.systemui:drawable/ic_qs_signal_4"));
	        mSettings.put(Settings.System.QUICK_HOTSPOT, new QuickTileHelper.QuickSettingInfo(
	        		Settings.System.QUICK_HOTSPOT, R.string.title_toggle_hotspot, "com.android.systemui:drawable/ic_qs_hotspot_off"));
	        mSettings.put(Settings.System.QUICK_TETHER, new QuickTileHelper.QuickSettingInfo(
	        		Settings.System.QUICK_TETHER, R.string.title_toggle_tether, "com.android.systemui:drawable/ic_qs_usb_device_off"));
        }
        if(mConfigs.get(Settings.System.QUICK_LTE)){
        	mSettings.put(Settings.System.QUICK_LTE, new QuickTileHelper.QuickSettingInfo(
        			Settings.System.QUICK_LTE, R.string.title_toggle_lte, "com.android.systemui:drawable/ic_sysbar_lte_off"));
        }
        if(mConfigs.get(Settings.System.QUICK_TORCH)){
	        mSettings.put(Settings.System.QUICK_TORCH, new QuickTileHelper.QuickSettingInfo(
	        		Settings.System.QUICK_TORCH, R.string.title_toggle_flashlight, "com.android.systemui:drawable/ic_sysbar_torch_off"));
        }
    }
    /**
     * Get the available settings for the current device
     * @return
     * 		a {@code HashMap} containing all of the settings
     */
    public HashMap<String, QuickSettingInfo> getAvailableSettings(){
    	return mSettings;
    }

    /**
     * This will retrieve the current settings
     * @return 
     * 		A {@code List<QuickTileToken>} of what is currently saved in settings
     */
    public List<QuickTileToken> getCurrentQuickSettings() {   		
        List<QuickTileToken> quick_settings = new ArrayList<QuickTileToken>();
        QuickTileTokenizer.tokenize(Settings.System.getString(mContext.getContentResolver(), 
                Settings.System.QUICK_SETTINGS_TILES), quick_settings);
        
        if(quick_settings.size()==0){ 
        	QuickTileTokenizer.tokenize(Settings.System.QUICK_TILES_DEFAULT, quick_settings);
        }
        else{
        	// just in case one sneaks in, get rid of it
        	for(Object token: quick_settings.toArray()){
        		if(!mConfigs.getNonNull(((QuickTileToken)token).getName(), true)){
        			quick_settings.remove(token);
        		}
        	}
        }
        
        return quick_settings;
    }
    /**
     * Saves the settings to the database
     * @param settings 
     * 		a {@code List<}{@link QuickTileToken}{@code >} containing the new settings
     */
    public void saveQuickSettings(List<QuickTileToken> settings) {
    	
    	StringBuilder sb = new StringBuilder();
    	if(settings!=null){
	    	for(QuickTileToken token: settings){
	    		sb.append(token.toString());
	    		sb.append(SETTING_DELIMITER);
	    	}
	    	if(sb.length()>0){
	    		sb.deleteCharAt(sb.length()-1);
	    	}
    	}
    	if(DEBUG)Log.d("QuickTiles", "Saving this:"+sb.toString());
    	Settings.System.putString(mContext.getContentResolver(),
                Settings.System.QUICK_SETTINGS_TILES, sb.toString());
    }

    /**
     * Add a quick tile setting to the end
     * @param quickTileToken
     * 		a {@link QuickTileToken} to add
     */
	public void addSetting(QuickTileToken quickTileToken) {
		List<QuickTileToken> settings = getCurrentQuickSettings();
		settings.add(quickTileToken);
		saveQuickSettings(settings);
	}
	
	/**
	 * Remove a setting based on the name
	 * @param setting
	 * 		the name of the setting
	 * @return
	 * 		the position of where it was removed from or -1 if it was not found
	 */
	public int removeSetting(String setting){
		List<QuickTileToken> settings = getCurrentQuickSettings();
		int index = -1;
		for(Object token: settings.toArray()){
			if(((QuickTileToken)token).getName().equals(setting)){
				index = settings.indexOf(token);
				settings.remove(token);
			}
		}
		saveQuickSettings(settings);
		return index;
	}
	
	/**
	 * Will change the row and column span of an existing setting
	 * @param position
	 * 		the position of the setting to change
	 * @param rows
	 * 		the new row span
	 * @param columns
	 * 		the new column span
	 */
	public void changeSize(int position, int rows, int columns) {
		List<QuickTileToken> settings = getCurrentQuickSettings();
		QuickTileToken token = settings.get(position);
		token.setRows(rows);
		token.setColumns(columns);
		saveQuickSettings(settings);
	}

	/**
	 * Class to hold title and icon information for each setting
	 * @author ihtfp69
	 */
    public static class QuickSettingInfo {
        private String mId;
        private int mTitleResId;
        private String mIcon;

        public QuickSettingInfo(String id, int titleResId, String icon) {
            mId = id;
            mTitleResId = titleResId;
            mIcon = icon;
        }

        public String getId() { return mId; }
        public int getTitleResId() { return mTitleResId; }
        public String getIcon() { return mIcon; }
    }
}
