package com.bamf.settings.activities;

import java.util.ArrayList;
import java.util.Arrays;
import com.android.internal.view.RotationPolicy;
import com.bamf.settings.R;
import com.bamf.settings.widgets.NavbarDragView;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class VisualNavbarActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "NavBarSettings";	
    
    private static final int REQUEST_PICK_SHORTCUT = 1;
    private static final int REQUEST_PICK_APPLICATION = 2;
    private static final int REQUEST_CREATE_SHORTCUT = 3;   
    
    private static final String KEY_BACK = "back";
    private static final String KEY_MENU = "menu_large";
    private static final String KEY_HOME = "home";
    private static final String KEY_RECENT = "recent_apps";
    private static final String KEY_SEARCH = "search";
    
    final static String ACTION_NONE = "None";
    public final static String ACTION_DEFAULT = "Default"; 
    public final static String ACTION_DEFAULT_NONE = "Default(none)";
    final static String ACTION_MENU = "Menu";
    final static String ACTION_RECENT = "Recent Apps";
    final static String ACTION_KILL = "Kill Current App";
    final static String ACTION_SCREEN_OFF = "Turn Off Screen";
    final static String ACTION_CUSTOM = "Custom";
    
    private NavbarDragView mAvailContainer;
	private NavbarDragView mCurrentContainer; 
	private PackageManager mPm;
	private String mSelectedKey;
	CharSequence[] items = {};
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Resources res = getResources();
        boolean isPhablet = res.getBoolean(R.bool.config_isPhablet);
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
        
        setContentView(R.layout.visual_settings_navbar_setup);
        mAvailContainer = (NavbarDragView) findViewById(R.id.avail_container);
		mCurrentContainer = (NavbarDragView) findViewById(R.id.current_container);
		
		String[] keys = Settings.System.getString(getContentResolver(),
				Settings.System.NAVBAR_KEY_ORDER,KEY_BACK+" "+KEY_HOME+" "+KEY_RECENT).split(" ");			
		mCurrentContainer.setupViews(keys,false,this);
		
		String[] avail = mCurrentContainer.getAvailKeys(Arrays.asList(keys));
		mAvailContainer.setupViews(avail,false,this);
        
		findViewById(R.id.navbar_button_save).setOnClickListener(this);
		mPm = getPackageManager();
    	setupActionBar();
    	
    }    

	private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setTitle(R.string.navbar_settings);             
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

	@Override
	public void onClick(View v) {
		
		
		if(v.getTag() == null && v.getId() == R.id.navbar_button_save){
			Settings.System.putInt(getContentResolver(), Settings.System.NAVBAR_ORDER_CHANGED, 1);
			Settings.System.putString(getContentResolver(),Settings.System.NAVBAR_KEY_ORDER,mCurrentContainer.getKeyTags());
			v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			return;
		}else if(v.getTag() == null) return;		
		
		v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		selectAction((String) v.getTag());
		
	}

	private void selectAction(String keyName) {	 		
		
		int index = -1;
		String action = null;
		
		if(keyName.equals(KEY_BACK)){
			action = Settings.System.getString(
					getContentResolver(), Settings.System.LONG_ACTION_BACK,ACTION_DEFAULT_NONE);
			items = new CharSequence[]{ACTION_DEFAULT_NONE,ACTION_SCREEN_OFF,ACTION_MENU,ACTION_RECENT,ACTION_KILL,resolveAction(action) +"..."};
			mSelectedKey = Settings.System.LONG_ACTION_BACK;
		}else if(keyName.equals(KEY_HOME)){
			action = Settings.System.getString(
					getContentResolver(), Settings.System.LONG_ACTION_HOME,ACTION_DEFAULT);
			items = new CharSequence[]{ACTION_DEFAULT,ACTION_NONE,ACTION_SCREEN_OFF,ACTION_MENU,ACTION_RECENT,ACTION_KILL,resolveAction(action)+"..."};
			mSelectedKey = Settings.System.LONG_ACTION_HOME;
		}else if(keyName.equals(KEY_MENU)){
			action = Settings.System.getString(
					getContentResolver(), Settings.System.LONG_ACTION_MENU,ACTION_DEFAULT_NONE);
			items = new CharSequence[]{ACTION_DEFAULT_NONE,ACTION_SCREEN_OFF,ACTION_RECENT,ACTION_KILL,resolveAction(action)+"..."};
			mSelectedKey = Settings.System.LONG_ACTION_MENU;
		}else if(keyName.equals(KEY_RECENT)){
			action = Settings.System.getString(
					getContentResolver(), Settings.System.LONG_ACTION_RECENT,ACTION_DEFAULT_NONE);
			items = new CharSequence[]{ACTION_DEFAULT_NONE,ACTION_SCREEN_OFF,ACTION_MENU,ACTION_KILL,resolveAction(action)+"..."};
			mSelectedKey = Settings.System.LONG_ACTION_RECENT;
		}else if(keyName.equals(KEY_SEARCH)){
			action = Settings.System.getString(
					getContentResolver(), Settings.System.LONG_ACTION_SEARCH,ACTION_DEFAULT);
			items = new CharSequence[]{ACTION_DEFAULT,ACTION_NONE,ACTION_SCREEN_OFF,ACTION_MENU,ACTION_RECENT,ACTION_KILL,resolveAction(action)+"..."};
			mSelectedKey = Settings.System.LONG_ACTION_SEARCH;
		}		
		
		index = getIndexForAction(action,items);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose an Action");
		builder.setSingleChoiceItems(items, index, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	if(items[item].equals(ACTION_DEFAULT) || items[item].equals(ACTION_DEFAULT_NONE) ||
						items[item].equals(ACTION_NONE) || items[item].equals(ACTION_MENU) || items[item].equals(ACTION_RECENT) ||
						items[item].equals(ACTION_KILL) || items[item].equals(ACTION_SCREEN_OFF)){
		    		
		    		Settings.System.putString(getContentResolver(), mSelectedKey, (String) items[item]);		    		
		    	}else{
		    		pickShortcut();
		    	}
		        dialog.dismiss();
		    }
		}).show();		
		
	}

	private int getIndexForAction(String action, CharSequence[] items) {
		
		for(int i =0;i<items.length;i++){
			if(items[i].equals(action))
				return i;
		}
		return items.length-1;
	}

	private String resolveAction(String action) {
		
		String newAction = ACTION_CUSTOM;
		if(action.equals(ACTION_DEFAULT) || action.equals(ACTION_DEFAULT_NONE) ||
				action.equals(ACTION_NONE) || action.equals(ACTION_MENU) || action.equals(ACTION_RECENT) ||
				action.equals(ACTION_KILL) || action.equals(ACTION_SCREEN_OFF)) return newAction;
		try{
			newAction = mPm.resolveActivity(Intent.parseUri(action, 0), 0).activityInfo.loadLabel(mPm).toString();
		}catch (Exception e){		
		}
		return newAction;
	}
	
	private void pickShortcut() {
    	
        Bundle bundle = new Bundle();

        ArrayList<String> shortcutNames = new ArrayList<String>();
        
        shortcutNames.add(getString(R.string.group_applications));
        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

        ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();        
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
    	
		Intent intent = (Intent) data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);        
        Settings.System.putString(getContentResolver(), mSelectedKey, intent.toUri(0));           
        
    }
	
	void completeSetCustomApp(Intent data) {
		
        Settings.System.putString(getContentResolver(),mSelectedKey, data.toUri(0));        
    }
}
