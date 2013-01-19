package com.bamf.settings.preferences;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.bamf.settings.R;
import com.bamf.settings.activities.NotificationManagerActivity;
import com.bamf.settings.activities.SettingsActivity;
import com.bamf.settings.activities.SettingsActivity.OnServiceBindedListener;
import com.bamf.settings.widgets.BAMFSwitchPreference;

public class SystemBasicFragment extends PreferenceFragment 
    implements OnPreferenceChangeListener,OnPreferenceClickListener,
        OnServiceBindedListener {
	
	private static final boolean DEBUG = true;
	
    public static final int MSG_REMOUNT = 101;
    public static final int MSG_BLOCK_ADS = 102;    
    
    private static final String PREF_BLOCK = "pref_system_basic_block";
    private static final String PREF_MOUNT = "pref_system_basic_mount";
    
    private static final String PREF_MANAGE = "pref_system_basic_manage";
    private static final String PREF_BRIGHT = "pref_system_basic_bright"; 
    private static final String PREF_END_CALL = "pref_system_basic_end_call"; 
    private static final String PREF_VOL_SKIP = "pref_system_basic_vol_skip"; 
    private static final String PREF_VOL_CONTROL = "pref_system_basic_vol_control"; 
    private static final String PREF_HINT = "pref_bright_hint";  
    
	private SettingsActivity mSettings;
	private BAMFSwitchPreference mBlockAds;
	private BAMFSwitchPreference mMountSystem;
	
	private BAMFSwitchPreference mEndCall;	
	private BAMFSwitchPreference mVolSkip;
	private CheckBoxPreference mVolControl;
	private Preference mAppsPreference;
	private Preference mBrightPreference;	
	
	private Preference mNotificationManager;
	
	private SharedPreferences mPrefs;
	private Editor mEdit;
	
	private Handler mHandler = new RootHandler();
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.system_basic);
        
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	mSettings = (SettingsActivity) getActivity();
    	mBlockAds = (BAMFSwitchPreference)findPreference(PREF_BLOCK);
    	mMountSystem = (BAMFSwitchPreference)findPreference(PREF_MOUNT);
    	
    	mEndCall = (BAMFSwitchPreference)findPreference(PREF_END_CALL);
    	mVolSkip = (BAMFSwitchPreference)findPreference(PREF_VOL_SKIP);
    	mVolControl = (CheckBoxPreference)findPreference(PREF_VOL_CONTROL);
    	mAppsPreference = findPreference(PREF_MANAGE);
    	mBrightPreference = findPreference(PREF_BRIGHT);
    	 	
    	mAppsPreference.setOnPreferenceClickListener(this);
    	mBrightPreference.setOnPreferenceClickListener(this);
    	
    	/**
    	 * the only way to check this on create is to have a system setting
    	 * that is set by the root service
    	 */
    	mBlockAds.setOnPreferenceChangeListener(this);
    	mBlockAds.setOnPreferenceClickListener(this);
    	mBlockAds.setChecked(checkAdsBlocked());
    	
    	mEndCall.setOnPreferenceChangeListener(this);
    	mEndCall.setOnPreferenceClickListener(this);
    	mEndCall.setChecked(Settings.System.getInt(mSettings.getContentResolver(), Settings.System.END_BUTTON_NOTIFICATION, 1)==1);
    	getPreferenceScreen().removePreference(mEndCall);
    	
    	mVolSkip.setOnPreferenceChangeListener(this);
    	mVolSkip.setOnPreferenceClickListener(this);
    	mVolSkip.setChecked(Settings.System.getInt(mSettings.getContentResolver(), Settings.System.VOL_KEYS_SKIP, 0)==1);
    	
    	mVolControl.setOnPreferenceChangeListener(this);
    	mVolControl.setChecked(Settings.System.getInt(mSettings.getContentResolver(), Settings.System.CONTROL_RINGER_NOTIF_VOLUME, 0)==1);
    	getPreferenceScreen().removePreference(mVolControl);
    	
    	mMountSystem.setOnPreferenceChangeListener(this);
    	mMountSystem.setOnPreferenceClickListener(this);
    	
    	if(DEBUG){
	    	mNotificationManager = new Preference(getActivity());
	    	mNotificationManager.setTitle("Manage Notifications");
	    	mNotificationManager.setSummary("Customize and manage notifications for the system and applications");
			((PreferenceScreen)findPreference("MAIN")).addPreference(mNotificationManager);
			mNotificationManager.setIntent(
					new Intent(getActivity(),
					NotificationManagerActivity.class));
    	}
    	
    	/**
    	 * This will only benefit us if this is the only fragment that uses root functions
    	 * out of all of the fragments under SettingsActivity
    	 * If this turns out not to be the case, we need a different plan
    	 * Could possibly use the TabHost to send a message to the fragments
    	 * Other activities should use SettingsActivity.isRootServiceBound(context)
    	 */
    	mSettings.setOnServiceBindedListener(this);
    	
    	mPrefs = PreferenceManager.getDefaultSharedPreferences(mSettings);
    	mEdit = mPrefs.edit();
    }
    
    private boolean checkAdsBlocked() {
		
    	File f = new File("/system/etc","hosts");    	
		return (f.exists() && f.length() > 30);
	}

	@Override
    public void onServiceBinded(boolean isBound) {
        if(isBound){
            try {
                mMountSystem.setChecked(SettingsActivity.getRootService().isMounted());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
	public boolean onPreferenceChange(Preference preference, Object value) {
        if(preference == mBlockAds){
            if(SettingsActivity.isRootServiceBound(mSettings)){
                mHandler.sendEmptyMessage(MSG_BLOCK_ADS);
            }else{
                mBlockAds.setChecked(!mBlockAds.isChecked());
            }
        }else if(preference == mMountSystem){
            if(SettingsActivity.isRootServiceBound(mSettings)){
                mHandler.sendEmptyMessage(MSG_REMOUNT);
            }else{
                mMountSystem.setChecked(!mMountSystem.isChecked());
            }
        }else if(preference == mEndCall){
        	Settings.System.putInt(mSettings.getContentResolver(), Settings.System.END_BUTTON_NOTIFICATION, (Boolean) value ? 1:0);
        }else if(preference == mVolSkip){
        	Settings.System.putInt(mSettings.getContentResolver(), Settings.System.VOL_KEYS_SKIP, (Boolean) value ? 1:0);
        }else if(preference == mVolControl){
        	Settings.System.putInt(mSettings.getContentResolver(), Settings.System.CONTROL_RINGER_NOTIF_VOLUME, (Boolean) value ? 1:0);
        }
		return true; 	
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference == mAppsPreference){
			Intent intent = new Intent("com.bamf.settings.systemapps");
			startActivity(intent);
		}else if(preference == mBrightPreference){
			if(mPrefs.getBoolean(PREF_HINT, true)){				
				mEdit.putBoolean(PREF_HINT, false);
				mEdit.commit();
				showHintDialog();
			}else
				showBrightnessDialog();
			//If it's a toggle switch, we want to trigger the toggle instead of it just highlighting and looking silly.
		}else if(preference instanceof TwoStatePreference){
            ((TwoStatePreference) preference).setChecked(!((TwoStatePreference) preference).isChecked());
        }
		
		return false;
	}		

	private void showHintDialog() {
		
		//Let the user know how the autobrightness settings work, so we don't answer that question 300 times.
		
		View v = getActivity().getLayoutInflater().inflate(R.layout.hint_dialog,null);
		CheckBox check = (CheckBox)v.findViewById(R.id.hint_check);
		check.setChecked(mPrefs.getBoolean(PREF_HINT, false));
		check.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {					
				mEdit.putBoolean(PREF_HINT, isChecked);
				mEdit.commit();
			}
			
		});
		
		new AlertDialog.Builder(mSettings)
		.setTitle("AutoBrightness Tip")
		.setView(v, 30, 10, 30, 5)		
		.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				showBrightnessDialog();
			}
		})		
		.show();     
		
	}

	void showBrightnessDialog() {	    

	    // DialogFragment.show() will take care of adding the fragment
	    // in a transaction.  We also want to remove any currently showing
	    // dialog, so make our own transaction and take care of that here.
	    FragmentTransaction ft = getFragmentManager().beginTransaction();
	    Fragment prev = getFragmentManager().findFragmentByTag("dialog");
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.addToBackStack(null);

	    // Create and show the dialog.
	    DialogFragment newFragment = BrightnessDialog.newInstance(mSettings);
	    newFragment.show(ft, "dialog");
	}

	public class RootHandler extends Handler {
	    
	    @Override
	    public void handleMessage(Message msg){
            boolean result;
            switch(msg.what){
                case MSG_REMOUNT:
                    try {
                        //need to check the result in case it was denied root access
                        result = SettingsActivity.getRootService().remount(!mMountSystem.isChecked());
                        mMountSystem.setChecked(result);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        mMountSystem.setChecked(!mMountSystem.isChecked());
                    }
                    break;
                case MSG_BLOCK_ADS:
                    try {
                        result = SettingsActivity.getRootService().blockAds(!mBlockAds.isChecked());
                        
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        mBlockAds.setChecked(!mBlockAds.isChecked());
                    }
                    break;                     	
            }
	    }
	}	
	
	protected void standDialog(boolean cancel, String msg) {
		
		ProgressDialog dialog = new ProgressDialog(mSettings);
		dialog.setCancelable(cancel);
		dialog.setMessage(msg);
		dialog.show();	
		
	}
	
	public static class BrightnessDialog extends DialogFragment implements OnSeekBarChangeListener {
		
		//Using some code from the boys at CyanogenMod here, since they got it right. :)
		
		private static LayoutInflater mLayoutInflater;
		private static ContentResolver mResolver;
		
		private static final int MINIMUM_BACKLIGHT = android.os.PowerManager.BRIGHTNESS_DIM -1;
	    private static final int MAXIMUM_BACKLIGHT = android.os.PowerManager.BRIGHTNESS_ON;
	    private int BLOCK_SIZE;
	    
	    private IPowerManager mPower;
		
		private int[] mLevels;
		private int[] mLevelsDefault;
		private View mDimLayout;
		private View mLowLayout;
		private View mMedLayout;
		private View mHighLayout;
		private CheckBox mCheck;
		
		private int mOldBrightness;
		
		private int mDimOld;
		private int mLowOld;
		private int mMedOld;
		private int mHighOld;
		
		private int mDeltaDim = 0;
		private int mDeltaLow = 0;
		private int mDeltaMed = 0;
		private int mDeltaHigh = 0;
		
		private boolean automatic;
		
	    static BrightnessDialog newInstance(Context context) {
	    	
	    	mLayoutInflater = LayoutInflater.from(context);
	    	mResolver = context.getContentResolver();		    	
	    	
	        return new BrightnessDialog();
	    }
	    
	    private void getLevels() {
	    	
	    	//Grab the current backlight values from settings.  If they don't exist, just use the defaults from framework.
	    	try{
	    		mLevelsDefault = getActivity().getResources().getIntArray(
	                    com.android.internal.R.array.config_autoBrightnessLcdBacklightValues);
	    		mLevels = parseIntArray(Settings.System.getString(mResolver,
	    				Settings.System.LIGHT_SENSOR_LCD_VALUES));
	    		if(mLevels.length < mLevelsDefault.length)
	    			mLevels = mLevelsDefault;
	    	}catch (Exception e){	    		
	    		mLevels = mLevelsDefault;
	    	}
	    	
	    	BLOCK_SIZE = mLevels.length/4;
			
		}	    
	    
	    private int[] parseIntArray(String intArray) {
	        int[] result;
	        if (intArray == null || intArray.length() == 0) {
	            result = new int[0];
	        } else {
	            String[] split = intArray.split(",");
	            result = new int[split.length];
	            for (int i = 0; i < split.length; i++) {
	                result[i] = Integer.parseInt(split[i]);
	            }
	        }
	        return result;
	    }

		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        
			
			setup();
			getLevels();
	    	View v = mLayoutInflater.inflate(R.layout.brightness_dialog,null);
	    	
	        mDimLayout = v.findViewById(R.id.bright_seeker_dim);	        
	        mLowLayout = v.findViewById(R.id.bright_seeker_low);	        
	        mMedLayout = v.findViewById(R.id.bright_seeker_med);	        
	        mHighLayout = v.findViewById(R.id.bright_seeker_high);
	        
	        mCheck = (CheckBox)v.findViewById(R.id.enable_auto);
	        mCheck.setChecked(automatic);
	        
	        setupView(mDimLayout);
	        setupView(mLowLayout);
	        setupView(mMedLayout);
	        setupView(mHighLayout);
	        
	        return new AlertDialog.Builder(getActivity())	        
	        .setView(v, 30, 10, 30, 5)
            .setTitle("AutoBrightness Settings")
            .setPositiveButton("Set",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	setNewLevels();   
                    	//Set it to autobrightness mode if the user so desires, otherwise, set it back to what it was before.
                    	if(mCheck.isChecked()){				
            				setMode(Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            			}else{
            				setBrightness(mOldBrightness);
            			}
                    }					
                }
            )
            .setNeutralButton("Default",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	defaultDialog();                    	
                    }
                }
            )
            .setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	//Set it to autobrightness mode if it was before, otherwise, set it back to what it was.
                    	if(automatic){				
            				setMode(Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            			}else{
            				setBrightness(mOldBrightness);
            			}
                    }
                }
            )
            .create();	        
	    }

		private void setup() {
			mPower = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
			automatic = ((Settings.System.getInt(mResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL))
                    	== Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);	
			
			if(!automatic){
				mOldBrightness = Integer.parseInt(Settings.System.getString(mResolver, Settings.System.SCREEN_BRIGHTNESS));
			}
			
		}

		protected void defaultDialog() {
			
			new AlertDialog.Builder(getActivity())
			.setTitle("Reset Defaults")
			.setMessage("Are you sure you wish to reset the Backlight Values to stock?")
			.setPositiveButton("Yes",  
					new DialogInterface.OnClickListener() {
                    	public void onClick(DialogInterface dialog, int whichButton) {
                    		//Write the stock values back to the settings database and toggle brightness modes to reset the system.
                    		mLevels = mLevelsDefault;
                    		Settings.System.putString(mResolver,
                                    Settings.System.LIGHT_SENSOR_LCD_VALUES, intArrayToString(mLevels));
                    		if(automatic){
                    			setMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    			setMode(Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                    		}
                    		//Set it to autobrightness mode if the user so desires, otherwise, set it back to what it was before.
                    		if(mCheck.isChecked()){				
                				setMode(Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                			}else{
                				setBrightness(mOldBrightness);
                			}
                    	}
                    }
			)
			.setNegativeButton("Cancel",
	                new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    	
	                    }
	                }
			)
	        .show();
            
			
		}

		private void setupView(View v) {			
			
			/*
			 * Proabably not the cleanest way to do this, as there is a lot of repeat code, but it works well.
			 * Here we set all the text fields and progress levels.
			 */
			int percent = 0;
			int percentDef = 0;
			
			TextView level = (TextView)v.findViewById(R.id.bright_level);
			TextView def = (TextView)v.findViewById(R.id.bright_default_text);
			TextView current = (TextView)v.findViewById(R.id.bright_current_text);
			SeekBar seek = (SeekBar)v.findViewById(R.id.seekBar);
			seek.setMax(intToPercent(MAXIMUM_BACKLIGHT));			
			
			if(v == mDimLayout){
				mDimOld = mLevels[0];
				percentDef = intToPercent(mLevelsDefault[0]);
				percent = intToPercent(mDimOld);
				level.setText("Dim");
				def.setText(String.valueOf(percentDef)+ "%");
				current.setText(String.valueOf(percent)+ "%");
				seek.setProgress(percent);
			}else if(v == mLowLayout){
				mLowOld = mLevels[BLOCK_SIZE];
				percentDef = intToPercent(mLevelsDefault[BLOCK_SIZE]);
				percent = intToPercent(mLowOld);
				level.setText("Low");
				def.setText(String.valueOf(percentDef)+ "%");
				current.setText(String.valueOf(percent)+ "%");
				seek.setProgress(percent);
			}else if(v == mMedLayout){
				mMedOld = mLevels[BLOCK_SIZE*2];
				percentDef = intToPercent(mLevelsDefault[BLOCK_SIZE*2]);
				percent = intToPercent(mMedOld);
				level.setText("Medium");
				def.setText(String.valueOf(percentDef)+ "%");
				current.setText(String.valueOf(percent)+ "%");
				seek.setProgress(percent);
			}else if(v == mHighLayout){
				mHighOld = mLevels[BLOCK_SIZE*3];
				percentDef = intToPercent(mLevelsDefault[BLOCK_SIZE*3]);
				percent = intToPercent(mHighOld);
				level.setText("High");
				def.setText(String.valueOf(percentDef)+ "%");
				current.setText(String.valueOf(percent)+ "%");
				seek.setProgress(percent);
			}
			seek.setOnSeekBarChangeListener(this);
		}

		private int intToPercent(int i) {	
			
			int value = Math.round((((float)i)/MAXIMUM_BACKLIGHT)*100);		
			return value;
		}
		
		private int percentToInt(int progress) {
			
			double value = ((double)progress/100);			
			return (int) (value*MAXIMUM_BACKLIGHT);
		}
		
		private void setNewLevels() {
			
			/*
			 * Set new levels, checking along the way that we aren't going over the maximum or below minimum safe levels.
			 */
			
			int minLevel = intToPercent(MINIMUM_BACKLIGHT);
			int maxLevel = MAXIMUM_BACKLIGHT;
			
			int[] tempLevels = new int[mLevels.length];
			for(int i = 0;i<tempLevels.length;i++){
				if(i < BLOCK_SIZE){
					if((mLevels[i] + mDeltaDim) < minLevel)
						tempLevels[i] = minLevel;						
					else if((mLevels[i] + mDeltaDim)>maxLevel)
						tempLevels[i] = maxLevel;
					else tempLevels[i] = mLevels[i] + mDeltaDim;
				}else if(i >= BLOCK_SIZE && i < BLOCK_SIZE*2){
					if(tempLevels[i-1] > mLevels[i]){
						minLevel = tempLevels[i-1];
					}
					if((mLevels[i] + mDeltaLow) < minLevel)
						tempLevels[i] = minLevel;						
					else if((mLevels[i] + mDeltaLow)>maxLevel)
						tempLevels[i] = maxLevel;
					else tempLevels[i] = mLevels[i] + mDeltaLow;
				}else if(i >= BLOCK_SIZE*2 && i < BLOCK_SIZE*3){
					if(tempLevels[i-1] > mLevels[i]){
						minLevel = tempLevels[i-1];
					}
					if((mLevels[i] + mDeltaMed) < minLevel)
						tempLevels[i] = minLevel;						
					else if((mLevels[i] + mDeltaMed)>maxLevel)
						tempLevels[i] = maxLevel;
					else tempLevels[i] = mLevels[i] + mDeltaMed;
				}else if(i >= BLOCK_SIZE*3){
					if(tempLevels[i-1] > mLevels[i]){
						minLevel = tempLevels[i-1];
					}
					if((mLevels[i] + mDeltaHigh) < minLevel)
						tempLevels[i] = minLevel;						
					else if((mLevels[i] + mDeltaHigh)>maxLevel)
						tempLevels[i] = maxLevel;
					else tempLevels[i] = mLevels[i] + mDeltaHigh;
				}
			}
			mLevels = tempLevels;
			Settings.System.putString(mResolver,
                    Settings.System.LIGHT_SENSOR_LCD_VALUES, intArrayToString(mLevels));
			
		}
		
		private String intArrayToString(int[] array) {
	        StringBuilder sb = new StringBuilder();
	        for (int i = 0; i < array.length - 1; i++) {
	            sb.append(array[i]);
	            sb.append(",");
	        }
	        sb.append(array[array.length - 1]);	        
	        return sb.toString();
	    }

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {			
			
			if(progress < intToPercent(MINIMUM_BACKLIGHT)){
				progress = intToPercent(MINIMUM_BACKLIGHT);
				seekBar.setProgress(progress);
			}			
			View v = (View) seekBar.getParent();
			View root = (View) v.getParent();
			
			TextView tv = (TextView) v.findViewById(R.id.bright_current_text);
			tv.setText(String.valueOf(progress)+"%");
			if(automatic){				
				setMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			}
			setBrightness(percentToInt(progress));
			
			//Grab the change in level based on which view we're adjusting.
			if(root == mDimLayout){				
				SeekBar seek = (SeekBar)mLowLayout.findViewById(R.id.seekBar);
				if(progress > seek.getProgress()){
					seek.setProgress(progress);
				}
				mDeltaDim = percentToInt(progress - intToPercent(mDimOld));				
			}else if(root == mLowLayout){				
				SeekBar seek = (SeekBar)mMedLayout.findViewById(R.id.seekBar);
				if(progress > seek.getProgress()){
					seek.setProgress(progress);					
				}
				seek = (SeekBar)mDimLayout.findViewById(R.id.seekBar);
				if(progress < seek.getProgress()){
					seek.setProgress(progress);					
				}
				mDeltaLow = percentToInt(progress - intToPercent(mLowOld));				
			}else if(root == mMedLayout){				
				SeekBar seek = (SeekBar)mHighLayout.findViewById(R.id.seekBar);
				if(progress > seek.getProgress()){
					seek.setProgress(progress);					
				}
				seek = (SeekBar)mLowLayout.findViewById(R.id.seekBar);
				if(progress < seek.getProgress()){
					seek.setProgress(progress);					
				}
				mDeltaMed = percentToInt(progress - intToPercent(mMedOld));				
			}else if(root == mHighLayout){				
				SeekBar seek = (SeekBar)mMedLayout.findViewById(R.id.seekBar);
				if(progress < seek.getProgress()){
					seek.setProgress(progress);					
				}
				mDeltaHigh = percentToInt(progress - intToPercent(mHighOld));				
			}
			
		}		

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
		private void setMode(int mode) {
	        Settings.System.putInt(mResolver,
	                Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
	    }
	    
	    private void setBrightness(int brightness) {
	        try {
	            mPower.setBacklightBrightness(brightness);
	        } catch (RemoteException ex) {
	        }        
	    }
		
	}

}
