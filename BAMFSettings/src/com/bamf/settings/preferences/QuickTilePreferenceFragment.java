package com.bamf.settings.preferences;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.bamf.settings.R;
import com.bamf.settings.utils.CustomIconUtil;
import com.bamf.settings.widgets.IconPreference;

public class QuickTilePreferenceFragment extends PreferenceFragment 
	implements OnPreferenceChangeListener, DialogInterface.OnCancelListener {
	
	private static final String TAG = QuickTilePreferenceFragment.class.getSimpleName();
	
	private static final String CUSTOM_PRESS = "pref_custom_toggle";
	private static final String CUSTOM_PRESS_ICON = "pref_custom_icon";
    private static final String QUICKSETTING_VISIBILITY = "pref_notification_mode";
    private static final String QUICKSETTING_ANIMATIONS = "pref_animations";
    private static final String QUICKSETTING_BEHAVIOR = "pref_behavior";
    
    private static final int REQUEST_PICK_SHORTCUT = 1;
    private static final int REQUEST_PICK_APPLICATION = 2;
    private static final int REQUEST_CREATE_SHORTCUT = 3;
    
    private IconPreference mCustomToggle;
    private IconPreference mCustomToggleIcon;
    private ListPreference mQuickVisibility;
    private ListPreference mQuickAnimations;
    private ListPreference mQuickBehavior;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.quick_settings);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		PreferenceScreen prefSet = getPreferenceScreen();

        mCustomToggle = (IconPreference)prefSet.findPreference(CUSTOM_PRESS);
        mCustomToggleIcon = (IconPreference)prefSet.findPreference(CUSTOM_PRESS_ICON);
        
        mQuickVisibility = (ListPreference) prefSet.findPreference(QUICKSETTING_VISIBILITY);
        mQuickVisibility.setOnPreferenceChangeListener(this);
        
        mQuickAnimations = (ListPreference) prefSet.findPreference(QUICKSETTING_ANIMATIONS);
        mQuickAnimations.setOnPreferenceChangeListener(this);
        
        mQuickBehavior = (ListPreference) prefSet.findPreference(QUICKSETTING_BEHAVIOR);
        mQuickBehavior.setOnPreferenceChangeListener(this);
        

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
    }
	
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		if (preference == mCustomToggle) {
            pickShortcut();
        }else if (preference == mCustomToggleIcon){
        	CustomIconUtil.getInstance(getActivity()).setFragment(this);
        	CustomIconUtil.getInstance(getActivity()).showContextMenu();
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
    		
        	CustomIconUtil.getInstance(getActivity()).setFragment(this);
    		Drawable custom = CustomIconUtil.getInstance(getActivity()).loadFromFile();
    		if(custom!=null){
    			mCustomToggleIcon.setIcon(custom);
    		}
		} catch (Exception e) {}
    }


}
