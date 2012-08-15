package com.bamf.settings.activities;

import java.util.ArrayList;

import com.android.internal.view.RotationPolicy;
import com.bamf.settings.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class VisualLockscreenActivity extends PreferenceActivity implements OnClickListener {
	
	private static final String TAG = "LockScreenSettings";
	private static final String BAD = "Bad URI";
	private static final String EMPTY = "";	
 /** If there is no setting in the provider, use this. */
    
    private static final int REQUEST_PICK_SHORTCUT = 1;
    private static final int REQUEST_PICK_APPLICATION = 2;
    private static final int REQUEST_CREATE_SHORTCUT = 3;
    private static final int TOTAL_TARGETS = 3;
	
	private View mLockscreenView;
	private Button mAddButton,mClearButton;
	private ImageView mShortcutLeft,mShortcutCenter,mShortcutRight;
	private Drawable mShortcut1Icon,mShortcut2Icon,mShortcut3Icon;
	
	private String mShortcut1,mShortcut2,mShortcut3;
	private String mShortcutToSet = "0";
	private int mTotalSet = 0;
    private PackageManager mPackageManager;
    private ContentResolver mResolver;
    private boolean hasBadTarget = false;   
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Resources res = getResources();
        boolean isPhablet = res.getBoolean(com.android.internal.R.bool.config_enableLockScreenRotation);
        boolean enableScreenRotation =
                SystemProperties.getBoolean("lockscreen.rot_override",false)
                || (isPhablet && 
                		Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION,0) == 1);
        
        if (enableScreenRotation) {            
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else {            
            if(isPhablet && RotationPolicy.isRotationLocked(this)){
            	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            }else{
            	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            }
        }
        
        setContentView(R.layout.visual_settings_lockscreen);
    	mPackageManager = getPackageManager();
    	mResolver = getContentResolver();
    	
    	if(!isPhablet){
    		mLockscreenView = findViewById(R.id.lockscreen_settings);
        	findViewById(R.id.lockscreen_backer).setBackground(getWallpaper());
        	setupViews();
        	getLoadedApps();
        	setViewStates();
    	}   	
    	
    	setupActionBar();
    	
    }
    
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setTitle(R.string.lockscreen_settings);             
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked;
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	private void setupViews() {
		
		//View v = mLockscreenView.findViewById(R.id.button_container);
		mAddButton = (Button) findViewById(R.id.lockscreen_button_add);
		mClearButton = (Button) findViewById(R.id.lockscreen_button_clear);
		
		mShortcutLeft = (ImageView) mLockscreenView.findViewById(R.id.left_app_position);
		mShortcutCenter = (ImageView) mLockscreenView.findViewById(R.id.center_app_position);
		mShortcutRight = (ImageView) mLockscreenView.findViewById(R.id.right_app_position);
		
		mAddButton.setOnClickListener(this);
		mClearButton.setOnClickListener(this);
		mShortcutLeft.setOnClickListener(this);
		mShortcutCenter.setOnClickListener(this);
		mShortcutRight.setOnClickListener(this);
	}

	/**
	 * Populate the apps that are currently assigned to the 3 lockscreen shortcuts.
	 * If there is an error (app was removed or something) make sure to handle it and let the system know there is a bad target assigned.
	 */
	
	private void getLoadedApps() {
		
		hasBadTarget = false;
		mTotalSet = 0;		
		
		try{     
        	mShortcut1 = Settings.System.getString(mResolver, "lock_shortcut_1");
        	if(mShortcut1 == null || mShortcut1.isEmpty()){
        		mShortcut1 = EMPTY;
        		Settings.System.putString(mResolver, "lock_shortcut_1", mShortcut1);        		
        	}else{        		
        		mShortcut1Icon = mPackageManager.getActivityIcon(Intent.parseUri(mShortcut1,0));        		
        	}
        	
        } catch (Exception e) {
        	mShortcut1 = EMPTY;
        	hasBadTarget = true;
    		mShortcut1 = BAD;
    		mShortcut1Icon = null;    		
			Log.e(TAG, "Bad URI, removing existing data.");
			e.printStackTrace();
		}
		
		if(!mShortcut1.isEmpty())
			mTotalSet++;
		
		try{     
        	mShortcut2 = Settings.System.getString(mResolver, "lock_shortcut_2");
        	if(mShortcut2 == null || mShortcut2.isEmpty()){
        		mShortcut2 = EMPTY;
        		Settings.System.putString(mResolver, "lock_shortcut_2", mShortcut2);        		
        	}else{        		
        		mShortcut2Icon = mPackageManager.getActivityIcon(Intent.parseUri(mShortcut2,0));        		
        	}
        	
        } catch (Exception e) {
        	mShortcut2 = EMPTY;
        	hasBadTarget = true;
    		mShortcut2 = BAD;
    		mShortcut2Icon = null;    		
			Log.e(TAG, "Bad URI, removing existing data.");
			e.printStackTrace();
		}
		
		if(!mShortcut2.isEmpty())
			mTotalSet++;
		
		try{     
        	mShortcut3 = Settings.System.getString(mResolver, "lock_shortcut_3");
        	if(mShortcut3 == null || mShortcut3.isEmpty()){
        		mShortcut3 = EMPTY;
        		Settings.System.putString(mResolver, "lock_shortcut_3", mShortcut3);        		
        	}else{        		
        		mShortcut3Icon = mPackageManager.getActivityIcon(Intent.parseUri(mShortcut3,0));        		
        	}
        	
        } catch (Exception e) {
        	mShortcut3 = EMPTY;
        	hasBadTarget = true;
    		mShortcut3 = BAD;
    		mShortcut3Icon = null;    		
			Log.e(TAG, "Bad URI, removing existing data.");
			e.printStackTrace();
		}
		
		if(!mShortcut3.isEmpty())
			mTotalSet++;
	}
	
	/**
	 * Assign the drawables for the shortcuts and viewStates for the number of shortcuts we have.
	 * If one is "BAD" assign it the default shortcut to let the user know there is a problem.
	 * 
	 */
	
	private void setViewStates() {
		
		switch(mTotalSet){
			case 1:
				if(!mShortcut1.isEmpty() && !mShortcut1.equals(BAD)){
					mShortcutCenter.setImageDrawable(mShortcut1Icon);					
				}else if(mShortcut1.equals(BAD)){
					mShortcutCenter.setImageResource(R.drawable.highlight_ring_error);					
				}
				mShortcutCenter.setVisibility(View.VISIBLE);
				mShortcutLeft.setVisibility(View.GONE);
				mShortcutRight.setVisibility(View.GONE);
				mAddButton.setEnabled(true);
				mClearButton.setEnabled(true);
				break;
			case 2:
				if(!mShortcut1.isEmpty() && !mShortcut1.equals(BAD)){
					mShortcutLeft.setImageDrawable(mShortcut1Icon);					
				}else if(mShortcut1.equals(BAD)){
					mShortcutLeft.setImageResource(R.drawable.highlight_ring_error);					
				}
				if(!mShortcut2.isEmpty() && !mShortcut2.equals(BAD)){
					mShortcutRight.setImageDrawable(mShortcut2Icon);					
				}else if(mShortcut2.equals(BAD)){
					mShortcutRight.setImageResource(R.drawable.highlight_ring_error);					
				}
				mShortcutLeft.setVisibility(View.VISIBLE);
				mShortcutRight.setVisibility(View.VISIBLE);
				mShortcutCenter.setVisibility(View.GONE);
				mAddButton.setEnabled(true);
				mClearButton.setEnabled(true);
				break;
			case 3:
				if(!mShortcut1.isEmpty() && !mShortcut1.equals(BAD)){
					mShortcutLeft.setImageDrawable(mShortcut1Icon);					
				}else if(mShortcut1.equals(BAD)){
					mShortcutLeft.setImageResource(R.drawable.highlight_ring_error);					
				}
				if(!mShortcut2.isEmpty() && !mShortcut2.equals(BAD)){
					mShortcutCenter.setImageDrawable(mShortcut2Icon);					
				}else if(mShortcut2.equals(BAD)){
					mShortcutCenter.setImageResource(R.drawable.highlight_ring_error);					
				}
				if(!mShortcut3.isEmpty() && !mShortcut3.equals(BAD)){
					mShortcutRight.setImageDrawable(mShortcut3Icon);					
				}else if(mShortcut3.equals(BAD)){
					mShortcutRight.setImageResource(R.drawable.highlight_ring_error);					
				}
				mShortcutLeft.setVisibility(View.VISIBLE);
				mShortcutCenter.setVisibility(View.VISIBLE);
				mShortcutRight.setVisibility(View.VISIBLE);
				mClearButton.setEnabled(true);
				mAddButton.setEnabled(false);
				break;
			default:
				mShortcutLeft.setVisibility(View.INVISIBLE);
				mShortcutCenter.setVisibility(View.INVISIBLE);
				mShortcutRight.setVisibility(View.INVISIBLE);
				mAddButton.setEnabled(true);
				mClearButton.setEnabled(false);
		}		
	}

	@Override
	public void onClick(View v) {
		
		if(!(v instanceof Button))
			v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		switch(v.getId()){
			//Add Button
			case R.id.lockscreen_button_add:
				addNewTarget(hasBadTarget);
				mAddButton.setEnabled(false);
				break;
			//Clear button
			case R.id.lockscreen_button_clear:
				clearAllTargets();
				break;			
			default:
				setAppTarget(v.getId());
				break;
		}		
		
	}	

	private void addNewTarget(boolean lockout) {
		
		//If there is a bad target, don't allow another target to be added until it is fixed.
		if(lockout){			
			new AlertDialog.Builder(this)
			.setTitle(getString(R.string.lockscreen_bad_target_title))
			.setMessage(getString(R.string.lockscreen_bad_target_description))
			.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				
				}
			})
			.show();
		}else{
			switch(mTotalSet){
				case 1:
					mShortcutLeft.setImageDrawable(mShortcut1Icon);
					mShortcutLeft.setVisibility(View.VISIBLE);
					mShortcutCenter.setImageResource(R.drawable.highlight_ring);
					mShortcutCenter.setVisibility(View.INVISIBLE);
					mShortcutRight.setImageResource(R.drawable.highlight_ring);
					mShortcutRight.setVisibility(View.VISIBLE);
					break;
				case 2:
					mShortcutLeft.setImageDrawable(mShortcut1Icon);
					mShortcutCenter.setImageDrawable(mShortcut2Icon);
					mShortcutCenter.setVisibility(View.VISIBLE);
					mShortcutRight.setImageResource(R.drawable.highlight_ring);
					break;
				default:
					mShortcutCenter.setImageResource(R.drawable.highlight_ring);
					mShortcutCenter.setVisibility(View.VISIBLE);
					break;
			}			
		}
	}
	
	private void clearAllTargets() {
		
		new AlertDialog.Builder(this)
		.setTitle(getString(R.string.lockscreen_clear_shortcut))
		.setMessage(getString(R.string.lockscreen_clear_description))
		.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				for(int i = 0;i < TOTAL_TARGETS;i++){
					Settings.System.putString(mResolver, "lock_shortcut_" + String.valueOf(i+1), EMPTY);
				}
				getLoadedApps();
				setViewStates();
			}
		})
		.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
			}
		})
		.show();	
		
	}
	
	private void setAppTarget(int id) {
		
		boolean showClear = true;
		
		switch(id){
			case R.id.left_app_position:				
				if(mTotalSet > 1)
					showClear = false;
				mShortcutToSet = "1";
				pickShortcut(showClear);
				break;
			case R.id.center_app_position:
				switch(mTotalSet){
					case 2:
					case 3:
						mShortcutToSet = "2";
						break;
					default:
						mShortcutToSet = "1";
						break;
				}
				if(mTotalSet > 2)
					showClear = false;
				pickShortcut(showClear);
				break;
			case R.id.right_app_position:
				switch(mTotalSet){
					default:
						mShortcutToSet = "2";
						break;	
					case 2:
					case 3:
						if(mAddButton.isEnabled())
							mShortcutToSet = "2";
						else 
							mShortcutToSet = "3";
						break;
				}
				pickShortcut(showClear);
				break;
		}
		
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
        	shortcutIcons.add(ShortcutIconResource.fromContext(this, android.R.drawable.ic_delete));
        shortcutIcons.add(ShortcutIconResource.fromContext(this, R.drawable.ic_lockscreen_apps));
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
