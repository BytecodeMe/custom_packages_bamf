package com.bamf.settings.preferences;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bamf.settings.R;
import com.bamf.settings.activities.SettingsActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PerformanceKernelFragment extends PreferenceFragment implements OnPreferenceClickListener,OnPreferenceChangeListener {
	
	/**
	 * Used to adjust basic kernel settings like Frequency and scheduler.
	 * Based on code from CyanogenMod, because there's no need to reinvent the wheel.
	 */
	
	public static final String PREF_CPU_MIN = "pref_kernel_CPU_min";
	public static final String PREF_CPU_MAX = "pref_kernel_CPU_max";
	public static final String PREF_GOVERNOR = "pref_kernel_governor";
	public static final String PREF_SCHEDULER = "pref_kernel_scheduler";
	public static final String PREF_APPLY_ON_BOOT = "pref_kernel_AOB";
	
	private static final int MSG_SCHEDULER = 100;	
	
	private static final String TAG = "KernelSettings";
	
	//Default locations of files we'll be working with.
	public static final String CURRENT_GOVERNOR = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
	public static final String AVAILABLE_GOVERNORS = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
	public static final String AVAILABLE_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String MAX_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static final String MIN_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    public static final String AVAILABLE_SCHEDULER = "/sys/block/mmcblk0/queue/scheduler";
	
    private Preference mPrefCPUMin;
	private Preference mPrefCPUMax;
	private Preference mPrefGovernor;
	private Preference mPrefScheduler;
	private CheckBoxPreference mPrefAOB;
	
	private String[] mDisplayGovernors;
	private String[] mDisplayFrequencies;
	private String[] mDisplaySchedulers;
	
	private int mCurrentSchedulerIndex;
	private String[] mValueFrequencies;
	
	private SettingsActivity mSettings;
	private LayoutInflater mLayoutInflater;
	private NumberPicker mNumberPicker;
	private LinearLayout mDialogLayout;
	private TextView mCurrentClock;
	
	private int mOldValue = 0;
	
	private String mPrefToSet;
	private SharedPreferences mPrefs;
	private Editor mEdit;	
	
	private Handler mHandler = new RootHandler();
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG,"Kernel onCreate()");
        setRetainInstance(true);
        
        addPreferencesFromResource(R.xml.performance_kernel);
        
        mSettings = (SettingsActivity) getActivity();    	
    	mPrefs = PreferenceManager.getDefaultSharedPreferences(mSettings);
    	
    	this.setHasOptionsMenu(true);
    	
    	setupArrays();
    	setupPreferences();
    }
    
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.add(0, 0, 0, "Voltages");
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case 0:
            	//Make sure we can even set the voltages before we try to display the options.
            	File f = new File(PerformanceVoltageFragment.VOLTAGE_TABLE);
                if(f.exists())
                	showVoltages();
                else{
                    Toast.makeText(mSettings, "Voltage Settings aren't supported by your kernel.", Toast.LENGTH_LONG).show();   
                }
                
                return true;
            default:
        }
        
        return false;
    }
    
    private void showVoltages(){
    	
    	//Switches to voltage View when actionbar link is clicked.
        final FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast, 
                R.anim.fade_in_fast, R.anim.fade_out_fast);
        Fragment fragment = Fragment.instantiate(getActivity(), PerformanceVoltageFragment.class.getName());
        trans.addToBackStack("reorder")
            .replace(R.id.performance_container, fragment, "voltage_frag")
            .setBreadCrumbShortTitle(R.string.voltage_settings).commit();
    }

	@Override
	public boolean onPreferenceClick(Preference pref) {
		
		mPrefToSet = pref.getKey();
		if(pref == mPrefCPUMin){
			showDialog("Minimum CPU Speed",mDisplayFrequencies,getIndexForValue(mValueFrequencies,readOneLine(MIN_FREQ)));
		}else if(pref == mPrefCPUMax){
			showDialog("Maximum CPU Speed",mDisplayFrequencies,getIndexForValue(mValueFrequencies,readOneLine(MAX_FREQ)));
		}else if(pref == mPrefGovernor){
			showDialog("Kernel Governor",mDisplayGovernors,getIndexForValue(mDisplayGovernors,readOneLine(CURRENT_GOVERNOR)));		
		}else if(pref == mPrefScheduler){
			showDialog("I/O Scheduler",mDisplaySchedulers,mCurrentSchedulerIndex);		
		}
		
		return true;
	}	
	
	private void setupArrays(){
	    mDisplayGovernors = readOneLine(AVAILABLE_GOVERNORS).split(" ");
        
        String[] tempFrequencies = checkBadFreq(readOneLine(AVAILABLE_FREQ)).split(" ");
        mValueFrequencies = new String[tempFrequencies.length];
        int j = tempFrequencies.length - 1;
        for(int i = 0;i < tempFrequencies.length;i++){
            mValueFrequencies[j] = tempFrequencies[i]; 
            j--;
        }        
        mDisplayFrequencies = new String[mValueFrequencies.length];
        for(int i = 0;i < mValueFrequencies.length;i++){
            mDisplayFrequencies[i] = toMHz(mValueFrequencies[i]);
        }
        String[] tempScheduler = readOneLine(AVAILABLE_SCHEDULER).split(" ");
        mDisplaySchedulers = new String[tempScheduler.length];
        for(int i = 0;i < tempScheduler.length;i++){
            if(tempScheduler[i].contains("["))
                mCurrentSchedulerIndex = i;
            mDisplaySchedulers[i] = tempScheduler[i].replace("[", "").replace("]","");
        }
	}
	
	private String checkBadFreq(String input) {
		
		String[] temp = input.split(" ");
		StringBuilder sb = new StringBuilder();
		for(int i =0;i < temp.length;i++){
			String val = temp[i];
			if(Integer.parseInt(val) > 100000){
				sb.append(val);
				if(i < temp.length - 1){
					sb.append(" ");
				}
			}
		}
		Log.d("KERNEL",sb.toString());
		return sb.toString();
	}


	private void setupPreferences(){
	    mEdit = mPrefs.edit();         
                
        mLayoutInflater = mSettings.getLayoutInflater();
        mDialogLayout = (LinearLayout) mLayoutInflater.inflate(R.layout.custom_picker,null);
        mNumberPicker = (NumberPicker)mDialogLayout.findViewById(R.id.value);
        mCurrentClock = (TextView)mDialogLayout.findViewById(R.id.currentSpeed);        
        
        mPrefCPUMin = findPreference(PREF_CPU_MIN);
        mPrefCPUMin.setOnPreferenceClickListener(this);
        mPrefCPUMin.setSummary(mDisplayFrequencies[getIndexForValue(mValueFrequencies,readOneLine(MIN_FREQ))]);
                
        mPrefCPUMax = findPreference(PREF_CPU_MAX);
        mPrefCPUMax.setOnPreferenceClickListener(this);
        mPrefCPUMax.setSummary(mDisplayFrequencies[getIndexForValue(mValueFrequencies,readOneLine(MAX_FREQ))]);
        
        mPrefGovernor = findPreference(PREF_GOVERNOR);
        mPrefGovernor.setOnPreferenceClickListener(this);
        mPrefGovernor.setSummary(mDisplayGovernors[getIndexForValue(mDisplayGovernors,readOneLine(CURRENT_GOVERNOR))]);
        
        mPrefScheduler = findPreference(PREF_SCHEDULER);
        mPrefScheduler.setOnPreferenceClickListener(this);
        mPrefScheduler.setSummary(mDisplaySchedulers[mCurrentSchedulerIndex]);       
        
        mPrefAOB = (CheckBoxPreference) findPreference(PREF_APPLY_ON_BOOT);          
        
	}
	
	public void showDialog(String title, final String[] values, int index){				
				
		//Displays the numberPicker for setting values
		mNumberPicker.setMinValue(0);
		mNumberPicker.setValue(index);
		if(values.length > mOldValue && mOldValue > 0){
			mNumberPicker.setDisplayedValues(values);
			mNumberPicker.setMaxValue(values.length - 1);	
		}else{
			mNumberPicker.setMaxValue(values.length - 1);	
			mNumberPicker.setDisplayedValues(values);
		}	
		mNumberPicker.setValue(index);
		mNumberPicker.setWrapSelectorWheel(false);
		mCurrentClock.setText(values[index]);
		
		new AlertDialog.Builder(getActivity())
		.setTitle(title)		
		.setView(mDialogLayout)
		.setPositiveButton(getString(com.android.internal.R.string.date_time_set), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {				
				applySetting(mNumberPicker.getValue());
				dialog.cancel();
			}
		})
		.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {		
				dialog.cancel();	
			}
		})
		.setOnCancelListener(new OnCancelListener(){

			@Override
			public void onCancel(DialogInterface dialog) {
				((ViewGroup) mDialogLayout.getParent()).removeView(mDialogLayout);			
				mOldValue = values.length;
			}
			
		})
		.show();                	
	}
	
	@Override
	public void onPause(){
		
		((ViewGroup) mDialogLayout.getParent()).removeView(mDialogLayout);		
		super.onPause();
	}
	
	/**
	 * Writes the new values to the corresponding files.  If it fails for some reason with the fileWriter, try it again with root permissions.
	 * Root is not the preferred method here, but if RAMDisk permissions aren't set correctly, we have no choice.
	 * @param value
	 */

	protected void applySetting(int value) {		
		
		if(mPrefToSet.equals(PREF_CPU_MIN)){
			if(writeOneLine(MIN_FREQ, mValueFrequencies[value]) || tryAsRoot(MIN_FREQ, mValueFrequencies[value])){
				mPrefCPUMin.setSummary(mDisplayFrequencies[getIndexForValue(mValueFrequencies,readOneLine(MIN_FREQ))]);
				mEdit.putString(PREF_CPU_MIN, mValueFrequencies[value]);
			}
		}else if(mPrefToSet.equals(PREF_CPU_MAX)){			
			if(writeOneLine(MAX_FREQ, mValueFrequencies[value]) || tryAsRoot(MAX_FREQ, mValueFrequencies[value])){
				mPrefCPUMax.setSummary(mDisplayFrequencies[getIndexForValue(mValueFrequencies,readOneLine(MAX_FREQ))]);
				mEdit.putString(PREF_CPU_MAX, mValueFrequencies[value]);
			}
		}else if(mPrefToSet.equals(PREF_GOVERNOR)){			
			if(writeOneLine(CURRENT_GOVERNOR, mDisplayGovernors[value]) || tryAsRoot(CURRENT_GOVERNOR, mDisplayGovernors[value])){
				mPrefGovernor.setSummary(mDisplayGovernors[getIndexForValue(mDisplayGovernors,readOneLine(CURRENT_GOVERNOR))]);
				mEdit.putString(PREF_GOVERNOR, mDisplayGovernors[value]);
			}
		}else if(mPrefToSet.equals(PREF_SCHEDULER)){
			if(value != mCurrentSchedulerIndex){
				mCurrentSchedulerIndex = value;
				mHandler.sendEmptyMessage(MSG_SCHEDULER);
				mPrefScheduler.setSummary(mDisplaySchedulers[mCurrentSchedulerIndex]);
				mEdit.putString(PREF_SCHEDULER, mDisplaySchedulers[value]);
			}
		}
		mEdit.commit();
		
	}

	public static String readOneLine(String fname) {
        BufferedReader br;
        String line = null;

        try {
            br = new BufferedReader(new FileReader(fname), 512);
            try {
                line = br.readLine();
            } finally {
                br.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "IO Exception when reading /sys/ file", e);
        }
        return line;
    }
	
	public static boolean writeOneLine(String fname, String value) {
        try {
            FileWriter fw = new FileWriter(fname);
            try {
                fw.write(value);
            } finally {
                fw.close();
            }
        } catch (IOException e) {
            String Error = "Error writing to " + fname + ". Retrying as root.";            
            Log.e(TAG, Error, e);            
            return false;
        }
        return true;
    }
	
	public static boolean tryAsRoot(String fname,String value){
		try {
            //need to check the result in case it was denied root access
            SettingsActivity.getRootService().setKernelValue(fname,value);                        
        } catch (RemoteException e1) {
            e1.printStackTrace(); 
            return false;
        }
		return true;
	}
	
	private int getIndexForValue(String[] array, String value) {		
		
		int retval = 0;
		for(int i = 0;i < array.length;i++){
			if(array[i].equalsIgnoreCase(value))
				retval = i;
		}
		return retval;
	}
	
	private String toMHz(String mhzString) {
        return new StringBuilder().append(Integer.valueOf(mhzString) / 1000).append(" MHz").toString();
    }
	
	public class RootHandler extends Handler {
	    
	    @Override
	    public void handleMessage(Message msg){            
            switch(msg.what){
                case MSG_SCHEDULER:
                    try {
                        //need to check the result in case it was denied root access
                        SettingsActivity.getRootService().setScheduler(mDisplaySchedulers[mCurrentSchedulerIndex]);                        
                    } catch (RemoteException e) {
                        e.printStackTrace();                        
                    }
                    break;                
            }
	    }
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference == mPrefAOB){
			mEdit.putBoolean(PREF_APPLY_ON_BOOT, (Boolean) newValue);
			mEdit.commit();
		}
		return true;
	}	
}
