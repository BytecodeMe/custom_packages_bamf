package com.bamf.settings.activities;

import com.android.internal.view.RotationPolicy;
import com.bamf.settings.R;

import android.app.ActionBar;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.view.MenuItem;

public class IconPreferenceActivity extends PreferenceActivity {

	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Resources res = getResources();
        boolean enableScreenRotation =
                SystemProperties.getBoolean("lockscreen.rot_override",false)
                || ((res.getBoolean(com.android.internal.R.bool.config_enableLockScreenRotation) && 
                		Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION,0) == 1));
        
        if (enableScreenRotation) {            
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else {            
            if(res.getBoolean(com.android.internal.R.bool.config_enableLockScreenRotation) && RotationPolicy.isRotationLocked(this)){
            	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            }else{
            	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            }
        }
        
        setContentView(R.layout.visual_settings_icon_preference);
        setupActionBar();
    	
    }
    
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setTitle(R.string.icon_settings);             
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
