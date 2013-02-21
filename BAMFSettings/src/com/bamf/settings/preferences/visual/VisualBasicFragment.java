package com.bamf.settings.preferences.visual;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.bamf.settings.R;
import com.bamf.settings.widgets.BAMFSwitchPreference;

public class VisualBasicFragment extends PreferenceFragment implements OnPreferenceChangeListener,OnPreferenceClickListener {
	
	private static final String PREF_DOCK_DIVIDER = "pref_visual_basic_show_dock_divider";
	private static final String PREF_LAUNCHER_SCREENS = "pref_visual_basic_launcher_screens";
	private static final String PREF_SEARCH_BAR = "pref_visual_basic_search_bar";
	private static final String PREF_BOOT_OPTIONS = "pref_visual_basic_boot_options";   
	private static final String SKIN_TEST = "pref_skin_test";
	
	private static final String BOOT_ANIMATION_PROP = "persist.sys.boot_enabled";
    private static final String BOOT_SOUND_PROP = "persist.sys.boot_sound";
    private static final String BOOT_VOLUME_PROP = "persist.sys.boot_volume";
	
	private static final String TAG = VisualBasicFragment.class.getSimpleName();
	
	private static final boolean DEBUG = false;
	
	private Activity mSettings;
	private ContentResolver mResolver;
	private BAMFSwitchPreference mDockDivider;
	private BAMFSwitchPreference mSearchBar;
	private ListPreference mLauncherScreens;
	private Preference mLockscreen;
	private Preference mBootOptions;	
	private Preference mSkinTest;
	
	private ProgressDialog mProgress;
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.visual_basic);
        
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	mSettings = getActivity();
    	mResolver = mSettings.getContentResolver();  
    	PreferenceScreen prefSet = getPreferenceScreen();
    	
    	mDockDivider = (BAMFSwitchPreference) findPreference(PREF_DOCK_DIVIDER);
    	mDockDivider.setChecked(Settings.System.getInt(mResolver,Settings.System.SHOW_LAUNCHER_DIVIDER,1) == 1);
    	
    	mSearchBar = (BAMFSwitchPreference) findPreference(PREF_SEARCH_BAR);
    	mSearchBar.setChecked(Settings.System.getInt(mResolver,"show_search_bar",0) == 1);
    	
    	mLauncherScreens = (ListPreference) findPreference(PREF_LAUNCHER_SCREENS);
    	int currentScreens = Settings.System.getInt(mResolver,"max_launcher_screens",7);
    	mLauncherScreens.setValue(String.valueOf(currentScreens));
    	mLauncherScreens.setSummary(String.format(mSettings.getString(R.string.launcher_screen_summary), currentScreens));
    	
    	mBootOptions = findPreference(PREF_BOOT_OPTIONS);    	
    	mBootOptions.setOnPreferenceClickListener(this);
    	
    	mLockscreen = findPreference("pref_visual_basic_lockscreen");
    	mLockscreen.setOnPreferenceClickListener(this);
    	//mLockscreen.setEnabled(false);
    	mDockDivider.setOnPreferenceChangeListener(this);
    	mDockDivider.setOnPreferenceClickListener(this);  
    	
    	mSearchBar.setOnPreferenceChangeListener(this);
    	mSearchBar.setOnPreferenceClickListener(this);   
    	mLauncherScreens.setOnPreferenceChangeListener(this);
    	
    	IntentFilter filter = new IntentFilter();
        filter.addAction("com.bamf.settings.LAUNCHER_CHANGE_COMPLETE");
        
        mSettings.registerReceiver(mLauncherCompleteReceiver, filter);
    	
        mProgress = new ProgressDialog(mSettings);
        mProgress.setTitle("Applying Change");
        mProgress.setCancelable(false);
        mProgress.setMessage("Adjusting Launcher. Please wait...");
    	
    } 
    
    @Override
    public void onPause(){  
    	
    	if(mSettings != null && mLauncherCompleteReceiver != null)
    		mSettings.unregisterReceiver(mLauncherCompleteReceiver);
    	super.onPause();
    }
    
    @Override
    public void onResume(){
    	if(mSettings != null){
    		IntentFilter filter = new IntentFilter();
    		filter.addAction("com.bamf.settings.LAUNCHER_CHANGE_COMPLETE");
    		mSettings.registerReceiver(mLauncherCompleteReceiver, filter);
    	}
    	super.onResume();
    }
    

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		
		if(pref == mDockDivider){
			int val = (Boolean) newValue? 1 : 0;
			Settings.System.putInt(mResolver,Settings.System.SHOW_LAUNCHER_DIVIDER,val);
			return true;
		}else if(pref == mSearchBar){
			int val = (Boolean) newValue? 1 : 0;
			Settings.System.putInt(mResolver,"show_search_bar",val);
			mProgress.show();
			return true;
		}else if(pref == mLauncherScreens){			
			int screens = Integer.parseInt((String) newValue);
			int sdefault = 3;
			Settings.System.putInt(mResolver,"max_launcher_screens",screens);			
			switch(screens){
				case 3:
					sdefault = 1;
					break;
				case 5:
					sdefault = 2;
					break;
				case 7:
					sdefault = 3;
					break;					
			}
			Settings.System.putInt(mResolver,"default_launcher_screen",sdefault);
			mLauncherScreens.setSummary(String.format(mSettings.getString(R.string.launcher_screen_summary), screens));
			mProgress.show();
			return true;
		}else
			return false;
	}
	

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference == mLockscreen){
			Intent i = new Intent("com.bamf.settings.visuallockscreen");
			startActivity(i);
		}else if(preference == mSkinTest){
			Intent i = new Intent("com.bamf.settings.visualskinchooser");
			startActivity(i);			
		}else if(preference == mBootOptions){
			showBootDialog();			
		}else
		((TwoStatePreference) preference).setChecked(!((TwoStatePreference) preference).isChecked());
		
		return false;
	}	
	
	private void showBootDialog() {
		
		boolean soundEnabled = SystemProperties.get(BOOT_SOUND_PROP, "1").equals("1");
		boolean aniEnabled = SystemProperties.get(BOOT_ANIMATION_PROP, "1").equals("1");
		float curVolume = Float.parseFloat(SystemProperties.get(BOOT_VOLUME_PROP, "0.2"));
		
		View v = getActivity().getLayoutInflater().inflate(R.layout.boot_settings_dialog, null);
		
		final Switch aniSwitch = (Switch)v.findViewById(R.id.ani_switch);
		final TextView aniSummary = (TextView)v.findViewById(R.id.ani_summary);
		aniSwitch.setChecked(aniEnabled);
		aniSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {		
				aniSummary.setText(isChecked ? getActivity().getString(R.string.boot_ani_on):getActivity().getString(R.string.boot_ani_off));
			}			
		});
		aniSummary.setText(aniEnabled ? getActivity().getString(R.string.boot_ani_on):getActivity().getString(R.string.boot_ani_off));
		
		final Switch soundSwitch = (Switch)v.findViewById(R.id.sound_switch);
		final TextView soundSummary = (TextView)v.findViewById(R.id.sound_summary);
		soundSwitch.setChecked(soundEnabled);
		soundSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {		
				soundSummary.setText(isChecked ? getActivity().getString(R.string.boot_sound_on):getActivity().getString(R.string.boot_sound_off));
			}			
		});
		soundSummary.setText(soundEnabled ? getActivity().getString(R.string.boot_sound_on):getActivity().getString(R.string.boot_sound_off));
		
		final SeekBar volSeek = (SeekBar)v.findViewById(R.id.vol_seek);
		final TextView volProgress = (TextView)v.findViewById(R.id.vol_progress);
		volSeek.setMax(100);
		volSeek.setProgress((int) (curVolume*100));
		volProgress.setText(String.valueOf((int)(curVolume*100))+"%");
		volSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {					
				volProgress.setText(String.valueOf(progress)+"%");
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {				
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}			
		});		
		
		new AlertDialog.Builder(mSettings)
		.setTitle("Boot Animation Settings")
		.setView(v, 15,10,15,5)
		.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				SystemProperties.set(BOOT_ANIMATION_PROP, aniSwitch.isChecked() ? "1":"0");
				SystemProperties.set(BOOT_SOUND_PROP, soundSwitch.isChecked() ? "1":"0");
				SystemProperties.set(BOOT_VOLUME_PROP, String.valueOf((float)((float)volSeek.getProgress()/100)));
			}
		})	
		.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
			}
		})	
		.show();     
	}	
	
	private BroadcastReceiver mLauncherCompleteReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			killLauncher();
		}
	};
	/**
	 * We need to give the launcher some time to rearrange things before killing it
	 * 
	 */
	private void killLauncher() {
		
		final Handler handler = new Handler();
		final Runnable finished = new Runnable() {
		    public void run() {		    	
		    	final ActivityManager am = (ActivityManager)mSettings.getSystemService(Context.ACTIVITY_SERVICE);
		    	am.forceStopPackage("com.android.launcher");
		    	mProgress.cancel();	    	
		    }
		};
		
		new Thread() {		    
			@Override public void run() {					
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
				handler.post(finished);					
			}
		}.start();        
		
	}
}
