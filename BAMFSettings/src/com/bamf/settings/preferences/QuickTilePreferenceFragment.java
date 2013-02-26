package com.bamf.settings.preferences;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.bamf.settings.R;
import com.bamf.settings.utils.CustomIconUtil;
import com.bamf.settings.utils.QuickTileHelper;
import com.bamf.settings.widgets.IconPreference;

public class QuickTilePreferenceFragment extends PreferenceFragment 
	implements OnPreferenceChangeListener, DialogInterface.OnCancelListener {
	
	private static final String TAG = QuickTilePreferenceFragment.class.getSimpleName();
	
	private static final String CUSTOM_PRESS = "pref_custom_toggle";
	private static final String CUSTOM_PRESS_ICON = "pref_custom_icon";
    private static final String QUICKSETTING_VISIBILITY = "pref_notification_mode";
    private static final String QUICKSETTING_ANIMATIONS = "pref_animations";
    private static final String QUICKSETTING_BEHAVIOR = "pref_behavior";
    private static final String QUICKSETTING_COLUMNS_PORT = "pref_columns_port";
    private static final String QUICKSETTING_COLUMNS_LAND = "pref_columns_land";
    
    private static final String CATEGORY_OPTIONS = "category_options";
    
    private static final int REQUEST_PICK_SHORTCUT = 1;
    private static final int REQUEST_PICK_APPLICATION = 2;
    private static final int REQUEST_CREATE_SHORTCUT = 3;
    
    private IconPreference mCustomToggle;
    private IconPreference mCustomToggleIcon;
    private ListPreference mQuickVisibility;
    private ListPreference mQuickAnimations;
    private ListPreference mQuickBehavior;
    private ListPreference mQuickColumnPort;
    private ListPreference mQuickColumnLand;
    
    private QuickTileHelper mQuickTileHelper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.quick_settings);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(mQuickTileHelper == null){
			mQuickTileHelper = new QuickTileHelper(getActivity());
		}
		
		PreferenceScreen prefSet = getPreferenceScreen();

        mCustomToggle = (IconPreference)prefSet.findPreference(CUSTOM_PRESS);
        mCustomToggleIcon = (IconPreference)prefSet.findPreference(CUSTOM_PRESS_ICON);
        
        mQuickVisibility = (ListPreference) prefSet.findPreference(QUICKSETTING_VISIBILITY);
        mQuickVisibility.setOnPreferenceChangeListener(this);
        
        mQuickAnimations = (ListPreference) prefSet.findPreference(QUICKSETTING_ANIMATIONS);
        mQuickAnimations.setOnPreferenceChangeListener(this);
        
        mQuickBehavior = (ListPreference) prefSet.findPreference(QUICKSETTING_BEHAVIOR);
        mQuickBehavior.setOnPreferenceChangeListener(this);
        
        mQuickColumnPort = (ListPreference) prefSet.findPreference(QUICKSETTING_COLUMNS_PORT);
        mQuickColumnPort.setOnPreferenceChangeListener(this);
        
        mQuickColumnLand = (ListPreference) prefSet.findPreference(QUICKSETTING_COLUMNS_LAND);
        mQuickColumnLand.setOnPreferenceChangeListener(this);
        
        PreferenceCategory options = (PreferenceCategory)prefSet.findPreference(CATEGORY_OPTIONS);
        options.removePreference(mQuickVisibility);
        options.removePreference(mQuickAnimations);
        options.removePreference(mQuickBehavior);
	}
	
	@Override
    public void onResume(){
    	super.onResume();
    	refreshCustomToggle();
    	
    	int notifcations = Settings.System.getInt(getActivity().getContentResolver(), 
    	        Settings.System.SHOW_NOTIFICATIONS_DEFAULT, 0);
        updateSummaryText(mQuickVisibility, R.array.notification_mode_entries, notifcations);
        
        int animations = Settings.System.getInt(getActivity().getContentResolver(), 
    	        Settings.System.QUICK_SETTINGS_ANIMATION, 1);
        updateSummaryText(mQuickAnimations, R.array.animation_entries, animations);
        
        int behavior = Settings.System.getInt(getActivity().getContentResolver(), 
    	        Settings.System.QUICK_SETTINGS_BEHAVIOR, 0);
        updateSummaryText(mQuickBehavior, R.array.behavior_entries, behavior);
        
        int columns_port = Settings.System.getIntForUser(getActivity().getContentResolver(), 
    	        Settings.System.QUICK_SETTINGS_NUM_COLUMNS_PORT, 
    	        mQuickTileHelper.getMaxColumns(Configuration.ORIENTATION_PORTRAIT), UserHandle.USER_CURRENT);
        updateSummaryText(mQuickColumnPort, R.array.columns_port_entries, columns_port - 1);
        
        int columns_land = Settings.System.getIntForUser(getActivity().getContentResolver(), 
    	        Settings.System.QUICK_SETTINGS_NUM_COLUMNS_LAND, 
    	        mQuickTileHelper.getMaxColumns(Configuration.ORIENTATION_LANDSCAPE), UserHandle.USER_CURRENT);
        updateSummaryText(mQuickColumnLand, R.array.columns_land_entries, columns_land - 1);
    }
	
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		if (preference == mCustomToggle) {
            pickShortcut();
        }else if (preference == mCustomToggleIcon){
        	CustomIconUtil.getInstance(getActivity()).showContextMenu(this);
        }
        return false;
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.valueOf((String)newValue);
		if(preference == mQuickVisibility) {
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SHOW_NOTIFICATIONS_DEFAULT, value);
            updateSummaryText(mQuickVisibility, R.array.notification_mode_entries, value);
        }else if(preference == mQuickAnimations){
        	Settings.System.putInt(getActivity().getContentResolver(), Settings.System.QUICK_SETTINGS_ANIMATION, value);
            updateSummaryText(mQuickAnimations, R.array.animation_entries, value);
        }else if(preference == mQuickBehavior){
        	Settings.System.putInt(getActivity().getContentResolver(), Settings.System.QUICK_SETTINGS_BEHAVIOR, value);
            updateSummaryText(mQuickBehavior, R.array.behavior_entries, value);
        }else if(preference == mQuickColumnPort){
        	Settings.System.putInt(getActivity().getContentResolver(), Settings.System.QUICK_SETTINGS_NUM_COLUMNS_PORT, value);
            updateSummaryText(mQuickColumnPort, R.array.columns_port_entries, (value - 1));
        }else if(preference == mQuickColumnLand){
        	Settings.System.putInt(getActivity().getContentResolver(), Settings.System.QUICK_SETTINGS_NUM_COLUMNS_LAND, value);
            updateSummaryText(mQuickColumnLand, R.array.columns_land_entries, (value - 1));
        }
        return true;
    }
	
	private void updateSummaryText(ListPreference pref, int array, int value){
        try{
        	pref.setSummary(this.getResources().getStringArray(array)[value]);
        	pref.setValueIndex(value);
        }catch(Exception e){}
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
    
    /**
     * this handles the cancel for the custom icon context menu
     * we need to refresh in case the user reverts to the default
     * icon
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        refreshCustomToggle();
    }
    
    private void pickShortcut() {
        Bundle bundle = new Bundle();

        ArrayList<String> shortcutNames = new ArrayList<String>();
        shortcutNames.add(getString(R.string.group_applications));
        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

        ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
        shortcutIcons.add(ShortcutIconResource.fromContext(getActivity(), R.drawable.ic_lockscreen_apps));
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
        
        if (Settings.System.putString(getActivity().getContentResolver(), 
        		Settings.System.QUICK_SETTINGS_CUSTOM, intent.toUri(0))) {
        	setupCustomToggle(intent);
        }
    }
    
    void completeSetCustomApp(Intent data) {
        if (Settings.System.putString(getActivity().getContentResolver(), 
        		Settings.System.QUICK_SETTINGS_CUSTOM, data.toUri(0))) {
        	setupCustomToggle(data);
        }        
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
    
    private void refreshCustomToggle(){
        String mCustomURI = Settings.System.getString(getActivity().getContentResolver(), 
        		Settings.System.QUICK_SETTINGS_CUSTOM);
        if(mCustomURI != null){
            try {
                setupCustomToggle(Intent.parseUri(mCustomURI, 0));
            } catch (Throwable t) {}
        }
        
        // reload the main fragment
        getFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, new QuickTileOrderFragment())
		.commit();
    }
    
    private void setupCustomToggle(Intent data){
    	final PackageManager pm = getActivity().getPackageManager();
    	try {
        	mCustomToggle.setSummary(pm.resolveActivity(data,0).activityInfo.loadLabel(pm));
        	mCustomToggle.setIcon(pm.getActivityIcon(data));
        	
        	mCustomToggleIcon.setIcon(resizeIcon(mCustomToggle.getIcon(), 96, 96));
    		
        	Drawable custom = CustomIconUtil.getInstance(getActivity()).loadFromFile();
    		if(custom!=null){
    			mCustomToggleIcon.setIcon(custom);
    		}
		} catch (Exception e) {}
    }


}
