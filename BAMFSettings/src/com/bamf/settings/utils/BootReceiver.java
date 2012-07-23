package com.bamf.settings.utils;

import java.util.Arrays;
import java.util.List;

import com.bamf.bamfutils.services.BAMFRootService;
import com.bamf.settings.preferences.PerformanceKernelFragment;
import com.bamf.settings.preferences.PerformanceVoltageFragment;
import com.bamf.settings.preferences.VoltageBootFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = "KernelSettings";
	
	private static final String CPU_SETTINGS_PROP = "sys.cpufreq.restored";
	
	private Context mContext;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.w(TAG, "Recieved Boot Completed");
		mContext = context;
		if(SystemProperties.getBoolean(CPU_SETTINGS_PROP, false) == false
                && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SystemProperties.set(CPU_SETTINGS_PROP, "true");
			configureCPU(context);			
		}
	}

	private void configureCPU(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String voltages = null;		
		
		Bundle b = new Bundle();
		
		if (prefs.getBoolean(PerformanceKernelFragment.PREF_APPLY_ON_BOOT, false) == false) {
            Log.i(TAG, "Restore disabled by user preference.");
            return;
        }
		
		if (prefs.getBoolean(VoltageBootFragment.PREF_APPLY_ON_BOOT, false) == false) {
            Log.i(TAG, "Voltage restore disabled by user preference.");
		}else{
			voltages = prefs.getString(PerformanceVoltageFragment.PREF_VOLTAGE_TABLE_KEY, null);
		}
		
		String governor = prefs.getString(PerformanceKernelFragment.PREF_GOVERNOR, null);
		String minFrequency = prefs.getString(PerformanceKernelFragment.PREF_CPU_MIN, null);
        String maxFrequency = prefs.getString(PerformanceKernelFragment.PREF_CPU_MAX, null);
        String scheduler = prefs.getString(PerformanceKernelFragment.PREF_SCHEDULER, null);
        String availableFrequenciesLine = PerformanceKernelFragment.readOneLine(PerformanceKernelFragment.AVAILABLE_FREQ);
        String availableGovernorsLine = PerformanceKernelFragment.readOneLine(PerformanceKernelFragment.AVAILABLE_GOVERNORS); 
        String availableSchedulersLine = PerformanceKernelFragment.readOneLine(PerformanceKernelFragment.AVAILABLE_SCHEDULER);
        boolean noSettings = ((availableGovernorsLine == null) || (governor == null)) &&
                             ((availableFrequenciesLine == null) || ((minFrequency == null) && (maxFrequency == null)) &&
                            		(availableSchedulersLine == null) || (scheduler == null));
        List<String> frequencies = null;
        List<String> governors = null;
        
        if (noSettings) {
            Log.d(TAG, "No settings saved. Nothing to restore.");
        } else {
            if (availableGovernorsLine != null){
                governors = Arrays.asList(availableGovernorsLine.split(" "));
            }
            if (availableFrequenciesLine != null){
                frequencies = Arrays.asList(availableFrequenciesLine.split(" "));
            }
            if (governor != null && governors != null && governors.contains(governor)) {
            	if(!(PerformanceKernelFragment.writeOneLine(PerformanceKernelFragment.CURRENT_GOVERNOR, governor))){   
            		Log.w(TAG, "Setting governor in bundle");
            		b.putString("gov", governor);
            	}
            }
            if (maxFrequency != null && frequencies != null && frequencies.contains(maxFrequency)) {
            	if(!(PerformanceKernelFragment.writeOneLine(PerformanceKernelFragment.MAX_FREQ, maxFrequency))){    
            		Log.w(TAG, "Setting max in bundle");
            		b.putString("max", maxFrequency);
            	}
            }
            if (minFrequency != null && frequencies != null && frequencies.contains(minFrequency)) {
            	if(!(PerformanceKernelFragment.writeOneLine(PerformanceKernelFragment.MIN_FREQ, minFrequency))){ 
            		Log.w(TAG, "Setting min in bundle");
            		b.putString("min", minFrequency);
            	}
            }             
            if (scheduler != null){
            	b.putString("scheduler", scheduler);
            }
            if (voltages != null){
            	b.putString("voltage", voltages);
            }  
            if(!b.isEmpty()){
            	b.putBoolean("onboot",true);
            	startService(b);
            }
            Log.d(TAG, "CPU settings restored.");
        }
	}

	private void startService(Bundle bundle) {
		
		Intent serviceIntent = null;
		try {
			serviceIntent = new Intent(mContext.createPackageContext("com.bamf.BAMFUtils", 0)
			        , BAMFRootService.class);	
			
			serviceIntent.putExtra("bundle", bundle);
			mContext.startService(serviceIntent);			
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
