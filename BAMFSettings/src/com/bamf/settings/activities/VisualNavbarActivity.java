package com.bamf.settings.activities;

import java.util.List;

import com.android.internal.view.RotationPolicy;
import com.bamf.settings.R;

import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class VisualNavbarActivity extends Activity {
	
	private static final String TAG = "NavBarSettings";	
    
    private static final int REQUEST_PICK_SHORTCUT = 1;
    private static final int REQUEST_PICK_APPLICATION = 2;
    private static final int REQUEST_CREATE_SHORTCUT = 3;   
    
    private static final String KEY_BACK = "back";
    private static final String KEY_MENU = "menu_large";
    private static final String KEY_HOME = "home";
    private static final String KEY_RECENT = "recent_apps";
    private static final String KEY_SEARCH = "search";
    
    
    
    private List<String> mUsedKeys;
    private List<String> mAvailableKeys;
    
    private int mKeyWidth;
    
	 
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
        
        mKeyWidth = getResources().getDimensionPixelSize(R.dimen.navigation_key_width);
        
        setContentView(R.layout.visual_settings_navbar_setup);
    	setupSpacing();
        
    	setupActionBar();
    	
    }
    
    private void setupSpacing() {
    	LinearLayout aC = (LinearLayout)findViewById(R.id.available_container);
    	int width = (mKeyWidth*3)/5;
    	for(int i = 0;i<aC.getChildCount();i++){
    		View v = aC.getChildAt(i);
    		LayoutParams p = (LayoutParams) v.getLayoutParams();
    		p.width = width;
    		v.setLayoutParams(p);
    	}
		
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
}
