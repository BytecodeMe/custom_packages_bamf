package com.bamf.settings.preferences;

import java.util.ArrayList;

import com.bamf.settings.R;
import com.bamf.settings.widgets.IconPreference;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

public class VisualLockscreenPhabletFragment extends PreferenceFragment implements OnPreferenceClickListener {
	
	private static final String BAD = "Bad URI";
	private static final String EMPTY = "";
	private static final int REQUEST_PICK_SHORTCUT = 1;
    private static final int REQUEST_PICK_APPLICATION = 2;
    private static final int REQUEST_CREATE_SHORTCUT = 3;
    private static final int TOTAL_TARGETS = 3;
    
	private static final String TARGET_1_PREF = "lockscreen_target_one";
	private static final String TARGET_2_PREF = "lockscreen_target_two";
	private static final String TARGET_3_PREF = "lockscreen_target_three";
 /** If there is no setting in the provider, use this. */    
	
	private Activity mSettings;	
    private ContentResolver mResolver;    
    private IconPreference mTarget1;
    private IconPreference mTarget2;
    private IconPreference mTarget3;
    private Drawable mShortcut1Icon,mShortcut2Icon,mShortcut3Icon;
	
	private String mShortcut1,mShortcut2,mShortcut3;
	private String mShortcut1Desc,mShortcut2Desc,mShortcut3Desc;
	private String mShortcutToSet = "0";
	private int mTotalSet = 0;
    private PackageManager mPackageManager;    
    private boolean hasBadTarget = false; 
	
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.visual_lockscreen_phablet);
       
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	mSettings = getActivity();    	
    	mResolver = mSettings.getContentResolver();
    	
    	mTarget1 = (IconPreference) findPreference(TARGET_1_PREF);
    	mTarget2 = (IconPreference) findPreference(TARGET_2_PREF);
    	mTarget3 = (IconPreference) findPreference(TARGET_3_PREF);
    	
    	mPackageManager = mSettings.getPackageManager();
    	mResolver = mSettings.getContentResolver();
    	
    	getLoadedApps();
    	setViewStates();
    	mTarget1.setOnPreferenceClickListener(this);
    	mTarget2.setOnPreferenceClickListener(this);
    	mTarget3.setOnPreferenceClickListener(this);
    }	
    
    private void getLoadedApps() {
		
		hasBadTarget = false;
		mTotalSet = 0;		
		
		try{     
        	mShortcut3 = Settings.System.getString(mResolver, "lock_shortcut_3");
        	if(mShortcut3 == null || mShortcut3.isEmpty()){
        		mShortcut3 = EMPTY;
        		Settings.System.putString(mResolver, "lock_shortcut_3", mShortcut3);        		
        	}else{ 
        		Intent data = Intent.parseUri(mShortcut3,0);
        		mShortcut3Icon = mPackageManager.getActivityIcon(data); 
        		mShortcut3Desc = (String) mPackageManager.resolveActivity(data,0).activityInfo.loadLabel(mPackageManager);
        	}
        	
        } catch (Exception e) {
        	mShortcut3 = EMPTY;
        	hasBadTarget = true;
    		mShortcut3 = BAD;
    		mShortcut3Icon = null;    		
			//Log.e(TAG, "Bad URI, removing existing data.");
			e.printStackTrace();
		}
		
		if(!mShortcut3.isEmpty())
			mTotalSet++;
		
		try{     
        	mShortcut2 = Settings.System.getString(mResolver, "lock_shortcut_2");
        	if(mShortcut2 == null || mShortcut2.isEmpty()){
        		mShortcut2 = EMPTY;
        		Settings.System.putString(mResolver, "lock_shortcut_2", mShortcut2);        		
        	}else{        		
        		Intent data = Intent.parseUri(mShortcut2,0);
        		mShortcut2Icon = mPackageManager.getActivityIcon(data); 
        		mShortcut2Desc = (String) mPackageManager.resolveActivity(data,0).activityInfo.loadLabel(mPackageManager);       		
        	}
        	
        } catch (Exception e) {
        	mShortcut2 = EMPTY;
        	hasBadTarget = true;
    		mShortcut2 = BAD;
    		mShortcut2Icon = null;    		
			//Log.e(TAG, "Bad URI, removing existing data.");
			e.printStackTrace();
		}
		
		if(!mShortcut2.isEmpty())
			mTotalSet++;
		
		try{     
        	mShortcut1 = Settings.System.getString(mResolver, "lock_shortcut_1");
        	if(mShortcut1 == null || mShortcut1.isEmpty()){
        		mShortcut1 = EMPTY;
        		Settings.System.putString(mResolver, "lock_shortcut_1", mShortcut1);        		
        	}else{        		
        		Intent data = Intent.parseUri(mShortcut1,0);
        		mShortcut1Icon = mPackageManager.getActivityIcon(data); 
        		mShortcut1Desc = (String) mPackageManager.resolveActivity(data,0).activityInfo.loadLabel(mPackageManager);       		
        	}
        	
        } catch (Exception e) {
        	mShortcut1 = EMPTY;
        	hasBadTarget = true;
    		mShortcut1 = BAD;
    		mShortcut1Icon = null;    		
			//Log.e(TAG, "Bad URI, removing existing data.");
			e.printStackTrace();
		}
		
		if(!mShortcut1.isEmpty())
			mTotalSet++;
	}
    
	private void setViewStates() {
		
		switch(mTotalSet){
			case 1:
				if(!mShortcut1.isEmpty() && !mShortcut1.equals(BAD)){
					mTarget1.setIcon(mShortcut1Icon);
					mTarget1.setSummary(mShortcut1Desc);
				}else if(mShortcut1.equals(BAD)){
					mTarget1.setIcon(R.drawable.highlight_ring_error);	
					mTarget1.setSummary("Bad Target");
				}
				mTarget1.setEnabled(true);
				mTarget2.setEnabled(true);
				mTarget3.setEnabled(false);				
				mTarget2.setIcon(null);
				mTarget3.setIcon(null);				
				mTarget2.setSummary("");
				mTarget3.setSummary("");
				break;
			case 2:
				if(!mShortcut1.isEmpty() && !mShortcut1.equals(BAD)){
					mTarget1.setIcon(mShortcut1Icon);
					mTarget1.setSummary(mShortcut1Desc);
				}else if(mShortcut1.equals(BAD)){
					mTarget1.setIcon(R.drawable.highlight_ring_error);	
					mTarget1.setSummary("Bad Target");
				}
				if(!mShortcut2.isEmpty() && !mShortcut2.equals(BAD)){
					mTarget2.setIcon(mShortcut2Icon);
					mTarget2.setSummary(mShortcut2Desc);
				}else if(mShortcut2.equals(BAD)){
					mTarget2.setIcon(R.drawable.highlight_ring_error);	
					mTarget2.setSummary("Bad Target");
				}
				mTarget1.setEnabled(true);
				mTarget2.setEnabled(true);
				mTarget3.setEnabled(true);				
				mTarget3.setIcon(null);				
				mTarget3.setSummary("");
				break;
			case 3:
				if(!mShortcut1.isEmpty() && !mShortcut1.equals(BAD)){
					mTarget1.setIcon(mShortcut1Icon);
					mTarget1.setSummary(mShortcut1Desc);
				}else if(mShortcut1.equals(BAD)){
					mTarget1.setIcon(R.drawable.highlight_ring_error);	
					mTarget1.setSummary("Bad Target");
				}
				if(!mShortcut2.isEmpty() && !mShortcut2.equals(BAD)){
					mTarget2.setIcon(mShortcut2Icon);
					mTarget2.setSummary(mShortcut2Desc);
				}else if(mShortcut2.equals(BAD)){
					mTarget2.setIcon(R.drawable.highlight_ring_error);	
					mTarget2.setSummary("Bad Target");
				}
				if(!mShortcut3.isEmpty() && !mShortcut3.equals(BAD)){
					mTarget3.setIcon(mShortcut3Icon);
					mTarget3.setSummary(mShortcut3Desc);
				}else if(mShortcut3.equals(BAD)){
					mTarget3.setIcon(R.drawable.highlight_ring_error);	
					mTarget3.setSummary("Bad Target");
				}
				mTarget1.setEnabled(true);
				mTarget2.setEnabled(true);
				mTarget3.setEnabled(true);
				break;
			default:
				mTarget1.setIcon(null);
				mTarget2.setIcon(null);
				mTarget3.setIcon(null);
				mTarget1.setSummary("");
				mTarget2.setSummary("");
				mTarget3.setSummary("");
				mTarget1.setEnabled(true);
				mTarget2.setEnabled(false);
				mTarget3.setEnabled(false);
		}		
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {
		
		boolean showClear = true;
		
		if(pref == mTarget1){
			if(mTotalSet > 1)
				showClear = false;
			mShortcutToSet = "1";
			pickShortcut(showClear);
		}else if(pref == mTarget2){
			if(mTotalSet > 2)
				showClear = false;
			mShortcutToSet = "2";
			pickShortcut(showClear);
		}else if(pref == mTarget3){
			mShortcutToSet = "3";
			pickShortcut(showClear);
		}
		return false;
	}	
	
	private void pickShortcut(boolean showClear) {
    	
        Bundle bundle = new Bundle();

        ArrayList<String> shortcutNames = new ArrayList<String>();
        if(showClear)
        	shortcutNames.add(getString(R.string.clear_shortcut));
        shortcutNames.add(getString(R.string.group_applications));
        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

        ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
        if(showClear)
        	shortcutIcons.add(ShortcutIconResource.fromContext(mSettings, android.R.drawable.ic_delete));
        shortcutIcons.add(ShortcutIconResource.fromContext(mSettings, R.drawable.ic_lockscreen_apps));
        bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtras(bundle);
        pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
        pickIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.use_custom_title));        

        startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
    }
	
	@Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
		if (resultCode == Activity.RESULT_OK) {
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
	
	void processShortcut(Intent intent, int requestCodeApplication, int requestCodeShortcut) {
    	
        // Handle case where user selected "Applications"
        String applicationName = getString(R.string.group_applications);
        String clearName = getString(R.string.clear_shortcut);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        if (applicationName != null && applicationName.equals(shortcutName)) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
            startActivityForResult(pickIntent, requestCodeApplication);
        } else if(clearName != null && clearName.equals(shortcutName)){
        	if (Settings.System.putString(mResolver, "lock_shortcut_" + mShortcutToSet, EMPTY)){
        		getLoadedApps();
				setViewStates();
        	}        
    	} else {
            startActivityForResult(intent, requestCodeShortcut);
        }
    }
    
    void completeSetCustomShortcut(Intent data) {
    	
		Intent intent = (Intent) data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        
        if (Settings.System.putString(mResolver, "lock_shortcut_" + mShortcutToSet, intent.toUri(0))) {            
        	getLoadedApps();
			setViewStates();
        }
    }
	
	void completeSetCustomApp(Intent data) {
		
        if (Settings.System.putString(mResolver, "lock_shortcut_" + mShortcutToSet, data.toUri(0))) {
        	getLoadedApps();
			setViewStates();
        }
    }
}
