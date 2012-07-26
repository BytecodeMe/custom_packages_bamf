package com.bamf.settings.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.bamf.settings.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class QuickSettingsUtil {
    private static final String QUICK_AIRPLANE = "QuickAirplane";
    private static final String QUICK_ROTATE = "QuickRotate";
    private static final String QUICK_BRIGHTNESS = "QuickBrightness";
    private static final String QUICK_BLUETOOTH = "QuickBluetooth";
    private static final String QUICK_NODISTURB = "QuickNoDisturb";
    private static final String QUICK_TORCH = "QuickTorch";
    private static final String QUICK_SETTING = "QuickSetting";
    private static final String QUICK_WIFI = "QuickWifi";
    private static final String QUICK_VOLUME = "QuickVolume";
    private static final String QUICK_LTE = "QuickLTE";
    private static final String QUICK_CUSTOM = "QuickCustom";
    private static final String QUICK_ADB = "QuickAdb";
    private static final String QUICK_GPS = "QuickGPS";
    private static final String QUICK_MOBILE_DATA = "QuickMobileData";
    private static final String QUICK_SYNC = "QuickSync";
    private static final String QUICK_MEDIA = "QuickMedia";
    private static final String QUICK_HOTSPOT = "QuickHotspot";
    private static final String QUICK_TETHER = "QuickTether";
    
    
    public static final HashMap<String, QuickSettingInfo> SETTINGS = new HashMap<String, QuickSettingInfo>();
    static {
        SETTINGS.put(QUICK_AIRPLANE, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_AIRPLANE, R.string.title_toggle_airplane, "com.android.systemui:drawable/ic_sysbar_airplane_on"));
        SETTINGS.put(QUICK_ROTATE, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_ROTATE, R.string.title_toggle_autorotate, "com.android.systemui:drawable/ic_sysbar_rotate_on"));
        SETTINGS.put(QUICK_BRIGHTNESS, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_BRIGHTNESS, R.string.title_toggle_brightness, "com.android.systemui:drawable/ic_sysbar_brightness"));
        SETTINGS.put(QUICK_NODISTURB, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_NODISTURB, R.string.title_toggle_donotdisturb, "com.android.systemui:drawable/ic_notification_open"));
        SETTINGS.put(QUICK_TORCH, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_TORCH, R.string.title_toggle_flashlight, "com.android.systemui:drawable/ic_sysbar_torch_on"));
        SETTINGS.put(QUICK_SETTING, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_SETTING, R.string.title_toggle_settings, "com.android.systemui:drawable/ic_sysbar_quicksettings"));
        SETTINGS.put(QUICK_WIFI, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_WIFI, R.string.title_toggle_wifi, "com.android.systemui:drawable/ic_sysbar_wifi_on"));
        SETTINGS.put(QUICK_VOLUME, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_VOLUME, R.string.title_toggle_volume, "com.android.systemui:drawable/ic_lock_silent_mode_off"));
        if(Build.DEVICE.equalsIgnoreCase("toro")){
        	SETTINGS.put(QUICK_LTE, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_LTE, R.string.title_toggle_lte, "com.android.systemui:drawable/ic_sysbar_lte_on"));
        }
        SETTINGS.put(QUICK_CUSTOM, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_CUSTOM, R.string.title_toggle_custom, "com.android.systemui:drawable/ic_sysbar_custom"));
        SETTINGS.put(QUICK_ADB, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_ADB, R.string.title_toggle_adb, "com.android.systemui:drawable/ic_sysbar_adb_on"));
        SETTINGS.put(QUICK_BLUETOOTH, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_BLUETOOTH, R.string.title_toggle_bluetooth, "com.android.systemui:drawable/ic_sysbar_bluetooth"));  
        SETTINGS.put(QUICK_GPS, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_GPS, R.string.title_toggle_gps, "com.android.systemui:drawable/ic_sysbar_gps"));
        SETTINGS.put(QUICK_MOBILE_DATA, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_MOBILE_DATA, R.string.title_toggle_mobiledata, "com.android.systemui:drawable/ic_sysbar_data"));
        SETTINGS.put(QUICK_SYNC, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_SYNC, R.string.title_toggle_sync, "com.android.systemui:drawable/ic_sysbar_sync"));
        SETTINGS.put(QUICK_MEDIA, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_MEDIA, R.string.title_toggle_media, "com.android.systemui:drawable/ic_sysbar_musicplayer"));
        SETTINGS.put(QUICK_HOTSPOT, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_HOTSPOT, R.string.title_toggle_hotspot, "com.android.systemui:drawable/ic_sysbar_hotspot_on"));
        SETTINGS.put(QUICK_TETHER, new QuickSettingsUtil.QuickSettingInfo(
                QUICK_TETHER, R.string.title_toggle_tether, "com.android.systemui:drawable/ic_sysbar_tether"));
    }

    private static String TEMP = null;
    private static final String SETTING_DELIMITER = "|";
    private static final String SETTINGS_DEFAULT = QUICK_AIRPLANE
            + SETTING_DELIMITER + QUICK_TORCH
            + SETTING_DELIMITER + QUICK_VOLUME
            + SETTING_DELIMITER + QUICK_ROTATE
            + SETTING_DELIMITER + QUICK_BRIGHTNESS
            + SETTING_DELIMITER + QUICK_SETTING;

    public static String getCurrentQuickSettings(Context context) {   		
        String quick_settings = Settings.System.getString(context.getContentResolver(), 
                Settings.System.QUICK_SETTINGS);
        if(quick_settings == null && TEMP == null){ 
        	quick_settings = SETTINGS_DEFAULT;
        }        	
        else if(TEMP != null){
        	quick_settings = TEMP;
        }
        else{
        	TEMP = quick_settings;
        }
        
        return quick_settings;
    }
    
    public static void saveCurrentQuickSettingsTemp(String settings) {
        TEMP = settings;
    }
    
    public static void saveCurrentQuickSettings(Context context) {
    	Settings.System.putString(context.getContentResolver(),
                Settings.System.QUICK_SETTINGS, TEMP);
    	clear();
    }
    
    public static void clear(){
    	TEMP = null;
    }

    public static String mergeInNewSettingsString(String oldString, String newString) {
        ArrayList<String> oldList = getQuickSettingListFromString(oldString);
        ArrayList<String> newList = getQuickSettingListFromString(newString);
        ArrayList<String> mergedList = new ArrayList<String>();

        // add any items from oldlist that are in new list
        for(String quick_setting : oldList) {
            if(newList.contains(quick_setting)) {
                mergedList.add(quick_setting);
            }
        }

        // append anything in newlist that isn't already in the merged list to the end of the list
        for(String quick_setting : newList) {
            if(!mergedList.contains(quick_setting)) {
                mergedList.add(quick_setting);
            }
        }

        // return merged list
        return getQuickSettingsFromList(mergedList);
    }

    public static ArrayList<String> getQuickSettingListFromString(String buttons) {
        return new ArrayList<String>(Arrays.asList(buttons.split("\\|")));
    }

    public static String getQuickSettingsFromList(ArrayList<String> quick_settings) {
        if(quick_settings == null || quick_settings.size() <= 0) {
            return "";
        } else {
            String s = quick_settings.get(0);
            for(int i = 1; i < quick_settings.size(); i++) {
                s += SETTING_DELIMITER + quick_settings.get(i);
            }
            return s;
        }
    }

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
    
    public static class Item{
        public final String text;
        public final int icon;
        public Item(String text, Integer icon) {
            this.text = text;
            this.icon = icon;
        }
        @Override
        public String toString() {
            return text;
        }
    }
    
    public static class CustomIconUtil {
        public static final String SETTING_ICON = "custom_setting.png";
        private static final String SYSUI = "com.android.systemui";
        
        private static Activity mContext;
        private static Context mSysUIContext = null;
        
        public static void setContext(Activity context){
            mContext = context;
        }
        
        private static Context getSysUIContext(){
            try {
                if(mSysUIContext == null){
                    mSysUIContext = mContext.createPackageContext(SYSUI, Context.CONTEXT_IGNORE_SECURITY);
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            
            return mSysUIContext;
        }
        
        public static void showContextMenu(Fragment cancelListener){
            final int GALLERY = 0;
            final int GALLERY_CROP = 1;
            final int ICON_PACK = 2;
            final int REMOVE = 3;          
            
            //items in the context menu list
            final Item[] items = {
                new Item("Gallery", com.android.internal.R.drawable.ic_menu_gallery),
                new Item("Gallery Cropped", com.android.internal.R.drawable.ic_menu_crop),
                new Item("ADW Icon Pack", com.android.internal.R.drawable.ic_menu_archive),
                new Item("Default Icon", com.android.internal.R.drawable.ic_menu_revert),
            };
            
            final ListAdapter adapter = new ArrayAdapter<Item>(
                mContext,
                R.layout.select_icon_source_item,
                R.id.select_photo_which,
                items){
                    public View getView(int position, View convertView, ViewGroup parent) {
                        //User super class to create the View
                        View v = super.getView(position, convertView, parent);
                        TextView tv = (TextView)v.findViewById(R.id.select_photo_which);
                        ImageView iv = (ImageView)v.findViewById(R.id.select_photo_icon);
                        tv.setText(items[position].text);
                        iv.setBackgroundResource(items[position].icon);
                        
                        return v;
                    }
                };
                    
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setAdapter(adapter, new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    switch(which){
                        case GALLERY:
                            fromGallery(false);
                            break;
                        case GALLERY_CROP:
                            fromGallery(true);
                            break;
                        case ICON_PACK:
                            fromIconPack();
                            break;
                        case REMOVE:
                            dialog.cancel();
                            removeCustomIcon();
                    }
                }
                
            })
            .setOnCancelListener((OnCancelListener) cancelListener)
            .setTitle("Select custom icon");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        
        private static void fromGallery(boolean crop){
            
            Intent intent = new Intent();
            intent.setType("image/*");
            
            if(crop){
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputX", 55);
                intent.putExtra("outputY", 55);
                intent.putExtra("scale", false);
            }
            
            intent.putExtra("return-data", true);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        
            intent.setAction(Intent.ACTION_GET_CONTENT);
            mContext.startActivityForResult(Intent.createChooser(intent, "Select Icon"),1);
        }
        
        private static void fromIconPack(){
            Intent intent = new Intent();
            
            intent.setAction("org.adw.launcher.icons.ACTION_PICK_ICON");
            mContext.startActivityForResult(Intent.createChooser(intent, "Select Icon Pack"),1);
        }
        
        private static void removeCustomIcon(){
            File f = new File(getSysUIContext().getDir("bamf", Context.MODE_WORLD_WRITEABLE),SETTING_ICON);
            if(f.exists()){
                f.delete();
            }
        }
        
        public static synchronized File getTempFile() {
            //File f = new File(getFilesDir(),RIGHT_BUTTON_ICON);
            File f = null;
            try{
                f = new File(getSysUIContext().getDir("bamf", Context.MODE_WORLD_WRITEABLE),SETTING_ICON);
                f.setReadable(true, false);
                
            }catch(Exception e){
                e.printStackTrace();
            }
            return f;
        }
        
        public static Drawable loadFromFile(Context context){
            File f = getTempFile();
            Drawable temp = null;
            if(f.exists()){
                Bitmap b = readBitmap(context, Uri.fromFile(f));
                if(b != null){
                    try{
                        temp = new BitmapDrawable(getSysUIContext().getResources(), b);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            
            return temp;
        }
        
        public static Bitmap readBitmap(Context context, Uri selectedImage) {
            Bitmap bm = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inSampleSize = 2; //reduce quality 
            AssetFileDescriptor fileDescriptor =null;
            try {
                fileDescriptor = context.getContentResolver().openAssetFileDescriptor(selectedImage,"r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            finally{
                try {
                    bm = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
                    fileDescriptor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bm;
        }
        
    }
}
