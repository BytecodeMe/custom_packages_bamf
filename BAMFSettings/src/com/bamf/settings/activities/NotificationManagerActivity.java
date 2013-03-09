package com.bamf.settings.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.android.internal.view.RotationPolicy;
import com.bamf.settings.R;
import com.bamf.settings.preferences.NotificationItemFragment;
import com.bamf.settings.preferences.NotificationPreferenceFragment;

public class NotificationManagerActivity extends Activity {

	public final FragmentManager mManager = getFragmentManager();
	private MenuItem mMenuButton;
	
	private Switch mSwitch;
	private TextView mTitle;
	private TextView mLabelCount;
	
	public final static int FRAGMENT_PREFERENCE = 0;
	public final static int FRAGMENT_LIST = 1;
	public final static int FRAGMENT_ITEM = 2;
	public final static int FRAGMENT_GENERAL = 3;
	
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
        
        setContentView(R.layout.quick_settings_container);

        if (savedInstanceState == null) {
            
            mManager.beginTransaction()
                    .add(R.id.container, Fragment.instantiate(this, NotificationPreferenceFragment.class.getName(),
                            getIntent().getExtras()), "main")
                    .commit();
        }
        
        setupActionBar();
        
        mSwitch.setChecked(true);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//TODO: switch off all custom notifications
				if(mManager.findFragmentByTag("item")!=null){
					if(mManager.findFragmentByTag("item").isVisible()){
						mManager.findFragmentByTag("item").onResume();
					}	
				}else if(mManager.findFragmentByTag("custom")!=null){
					if(mManager.findFragmentByTag("custom").isVisible()){
						Settings.System.putIntForUser(getContentResolver(), 
								Settings.System.QUIET_HOURS_ENABLED, 
								isChecked ? 1:0, UserHandle.USER_CURRENT);
						mManager.findFragmentByTag("custom").onResume();
					}
				}else{
					Settings.System.putIntForUser(getContentResolver(), 
							Settings.System.NOTIFICATION_MANAGER, 
							isChecked ? 1:0, UserHandle.USER_CURRENT);
				}
			}
		});
        
    }
        
    private void setupActionBar() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
        		|ActionBar.DISPLAY_HOME_AS_UP,
        		 ActionBar.DISPLAY_SHOW_CUSTOM
        		|ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setCustomView(R.layout.preference_header_switch_item);
        mTitle = (TextView)actionBar.getCustomView().findViewById(android.R.id.title);
        mSwitch = (Switch)getActionBar().getCustomView().findViewById(R.id.switchWidget);
        mLabelCount = (TextView)getActionBar().getCustomView().findViewById(R.id.number_count);
    }
    
    public boolean getSwitchChecked(){
    	return mSwitch.isChecked();
    }
    
    public void setSwitchChecked(boolean isChecked){
    	mSwitch.setChecked(isChecked);
    }
    
    public void setTitleText(CharSequence text){
    	mTitle.setText(text);
    }
    
    public void setLabelCount(CharSequence text){
    	mLabelCount.setText(text);
    }
    
    public void setupFragmentActionBar(int fragment){
    	setupFragmentActionBar(fragment, null);
    }
    
    public void setupFragmentActionBar(int fragment, CharSequence title){
    	getActionBar().setIcon(R.drawable.ic_launcher);
    	
    	switch(fragment){
    		case FRAGMENT_GENERAL:
    			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    			mTitle.setText((title==null)?"General":title);
    			showSwitchTitle(true);
    			mSwitch.setVisibility(View.GONE);
    			break;
	    	case FRAGMENT_PREFERENCE:
	    		mTitle.setText((title==null)?this.getText(R.string.notifications_main_title):title);
	    	case FRAGMENT_ITEM:
	    		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	    		showSwitchTitle(true);
	    		break;
	    	case FRAGMENT_LIST:
	    		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	    		getActionBar().setDisplayShowTitleEnabled(false);
	    		showSwitchTitle(false);
	    		setLabelCount("");
	    		break;
    		default:
    	}
    }
    
    private void showSwitchTitle(boolean show){
    	mTitle.setVisibility(show ? View.VISIBLE:View.GONE);
    	mSwitch.setVisibility(show ? View.VISIBLE:View.GONE);
    	mLabelCount.setVisibility(show ? View.GONE:View.VISIBLE);
    }
    
    @Override
    public void onBackPressed(){
    	if(mManager.findFragmentByTag("item")!=null){
			if(mManager.findFragmentByTag("item").isVisible()){
				if(((NotificationItemFragment)mManager.findFragmentByTag("item"))
					.showSaveDialog()){
					super.onBackPressed();
				}
			}	
		}else{
			super.onBackPressed();
		}
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked;
                if(mManager.getBackStackEntryCount() > 0){
                    mManager.popBackStack();
                }else{
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
 }
