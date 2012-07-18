package com.bamf.settings.activities;

import com.bamf.settings.R;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class IconPreferenceActivity extends PreferenceActivity {

	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
