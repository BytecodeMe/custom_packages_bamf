package com.bamf.bamfutils.services;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import com.bamf.bamfutils.tools.Mount;
import com.bamf.bamfutils.tools.RootTools;

public class BAMFRootService extends Service {
	
	private static final String TAG = "BAMFUtils";	
	private static Mount SYS_MOUNT;
	private static String[] SYS_MOUNT_PROPS;
	private static int MOUNT_LOCATION;
	private static boolean isMounted = false;
	private boolean onBoot = false;
	
	public static final String VOLTAGE_TABLE = "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
	public static final String VOLTAGE_TABLE1 = "/sys/devices/system/cpu/cpu1/cpufreq/UV_mV_table";	
	public static final String CURRENT_GOVERNOR = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
	public static final String MAX_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static final String MIN_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    public static final String AVAILABLE_SCHEDULER = "/sys/block/mmcblk0/queue/scheduler";
    public static final String AVAILABLE_SCHEDULERALT1 = "/sys/block/mmcblk0boot0/queue/scheduler";
    public static final String AVAILABLE_SCHEDULERALT2 = "/sys/block/mmcblk0boot1/queue/scheduler";
    public static final String AVAILABLE_SCHEDULERALT3 = "/sys/block/mtdblock0/queue/scheduler";
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand( intent, flags, startId );
		
		//Pulling data from the Boot Receiver for settings to restore CPU Settings.
		Bundle b = intent.getBundleExtra("bundle");
		if(b != null){
			onBoot = b.getBoolean("onboot");
			if(b.containsKey("gov")){
				setKernelValue(CURRENT_GOVERNOR,b.getString("gov"));
			}
			if(b.containsKey("min")){				
				setKernelValue(MIN_FREQ,b.getString("min"));
			}
			if(b.containsKey("max")){				
				setKernelValue(MAX_FREQ,b.getString("max"));
			}
			if(b.containsKey("scheduler")){				
				setScheduler(b.getString("scheduler"));
			}
			if(b.containsKey("voltage")){				
				setVoltage(b.getString("voltage"));	
			}
		}
		
		//Stop the service if it was called by the boot receiver as there's no need for it to be running all the time.
		if(onBoot){
			onBoot = false;
			stopSelf();
		}
		Log.d( TAG, "onStart" );
		return START_STICKY;
	}
	
	private boolean toggleAppState(boolean state,String pName){
		
		//Setting the app enabled state this way actually seems to hold through a clean flash.
        PackageManager pm = getPackageManager();
        if(state){  
            pm.setApplicationEnabledSetting(pName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
        }else{              
            pm.setApplicationEnabledSetting(pName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, 0);
        }         
        return true;          
    }
	
	private boolean isMounted(){
        
        boolean retval = false;         
        ArrayList<Mount> mounts;
        try {
            mounts = RootTools.getMounts();
            for(int i = 0;i < mounts.size();i++){
                if(mounts.get(i).getMountPoint().getPath().contains("/system"))
                    SYS_MOUNT = mounts.get(i);
            }
        } catch (Exception e) {
            Log.e("BASICRECEIVER", "getMountValues()");
            e.printStackTrace();
        }
        
        Set<String> temp = SYS_MOUNT.getFlags();
        SYS_MOUNT_PROPS = temp.toArray(new String[temp.size()]);
        for(int i =0;i < SYS_MOUNT_PROPS.length;i++){
            if(SYS_MOUNT_PROPS[i].equalsIgnoreCase("ro") || SYS_MOUNT_PROPS[i].equalsIgnoreCase("rw")){
                MOUNT_LOCATION = i;
                if(SYS_MOUNT_PROPS[i].equalsIgnoreCase("rw")){
                    retval = true;                      
                }
            }   
        }       
        isMounted = retval;
        return retval;
    }
	
	private String getLastBackupDate(String dir){
    	File backupLocation = new File(dir, "databases");
    	if (backupLocation.exists()) {
    	    File[] child = backupLocation.listFiles();
    	    for (int i = 0; i < child.length;) {
    	    	Date lastModDate = new Date(child[i].lastModified());
    	    	return lastModDate.toString();
    	    }
    	}
    	return null;
    }
	
	private boolean copyFile(String from, String to, boolean isRestore){
		try{
	    	RootTools.sendShell("busybox cp "+from+" "+to, -1);
	    	if(isRestore){
	    		RootTools.sendShell("chown 1000.1000 "+to, -1);
	    		RootTools.sendShell("chmod 0660 "+to, -1);
	    	}
		}catch(Exception e){
			return false;
		}
		
		return true;
	}
	
	/**
	 * This is where we are writing Kernel control values if we can't manage it without root.  
	 * If everyone just set the correct perms, it wouldn't matter. :) 
	 */
	
	public void setScheduler(String scheduler){		
		try {
			RootTools.sendShell("echo " + scheduler +" > "+AVAILABLE_SCHEDULER,-1);
			if(new File(AVAILABLE_SCHEDULERALT1).exists())
				RootTools.sendShell("echo " + scheduler +" > "+AVAILABLE_SCHEDULERALT1,-1);
			if(new File(AVAILABLE_SCHEDULERALT2).exists())
				RootTools.sendShell("echo " + scheduler +" > "+AVAILABLE_SCHEDULERALT2,-1);
			if(new File(AVAILABLE_SCHEDULERALT3).exists())
				RootTools.sendShell("echo " + scheduler +" > "+AVAILABLE_SCHEDULERALT3,-1);
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	public void setVoltage(String file){
		
		try {			
			RootTools.sendShell("echo " + file +" > "+VOLTAGE_TABLE,-1);
			if(new File(VOLTAGE_TABLE1).exists())
				RootTools.sendShell("echo " + file +" > "+VOLTAGE_TABLE1,-1);
		}catch(Exception e){
			Log.w(TAG, "Voltage failed");
			e.printStackTrace();
		}		
	}
	
	public void setKernelValue(String file,String value){
		try {
			RootTools.sendShell("echo " + value +" > "+ file,-1);
		} catch (Exception e) {
			Log.e(TAG, "setNewValue()");			
            e.printStackTrace();
		} 
	}
	
	public String getFilesPath(){
	    try {
            return createPackageContext("com.bamf.settings", 0)
                    .getDir("files", Context.MODE_WORLD_READABLE).getAbsolutePath();
        } catch (NameNotFoundException e) {
            // can't happen
            return "";
        }
	}
	 
	@Override
	public IBinder onBind(Intent intent) {
	    // needed to add this so it was not null the first time it is used;
	    // we should not expect the calling code to do this
	    isMounted = isMounted();
		return mBinder;
	}
	
	private final IBinder mBinder = new ServiceStub(this);
	
	/**
     * The IAdderService is defined through IDL
     */
	static class ServiceStub extends IRootService.Stub {
	    WeakReference<BAMFRootService> mService;
        
        ServiceStub(BAMFRootService service) {
            mService = new WeakReference<BAMFRootService>(service);
        }        
      
    	@Override
    	public boolean toggleAppState(boolean state,String pName){
    		return mService.get().toggleAppState(state, pName);    	  
    	}
    	
    	@Override
    	public boolean removeApp(String dLocation,String location){
    		
    		//Deletes the apk from /system/app before running the uninstall.
    		RootTools.remount(SYS_MOUNT.getMountPoint().toString(), "RW");			
    		try {
				RootTools.sendShell("rm " + location, -1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
    		
			if(!isMounted)
				RootTools.remount(SYS_MOUNT.getMountPoint().toString(), "RO");
			return true;    		
    	}
    	
    	@Override
        public boolean isMounted() throws RemoteException {
            return mService.get().isMounted();
        }    	
    	
    	@Override
    	public boolean remount(boolean readWrite){ 
    		
			if(readWrite){
				if(RootTools.remount(SYS_MOUNT.getMountPoint().toString(), "RO")){
				    isMounted = false;
				}
			}else{
				if(RootTools.remount(SYS_MOUNT.getMountPoint().toString(), "RW")){
				    isMounted = true;
				}
			}

			return isMounted;
    	}
    	
    	@Override
    	public boolean blockAds(boolean blocked){
    	    
    	    boolean retval = false;
    	    
    	    //RootTools will not remount if already rw
    		RootTools.remount(SYS_MOUNT.getMountPoint().toString(), "RW");	
    		int result;
			try {				
				if(!blocked){
					RootTools.sendShell("busybox cp "+mService.get().getFilesPath()+"/hostsblocked /system/etc/hosts",-1);
					result = 1;
				}else{
					RootTools.sendShell("busybox cp "+mService.get().getFilesPath()+"/hostsopen /system/etc/hosts",-1);
					result = 0;
				}
				RootTools.sendShell("chmod 644 /system/etc/hosts",-1);
				Settings.System.putInt(mService.get().getContentResolver(), Settings.System.BLOCK_ADS, result);				
				retval = true;				
			} catch (Exception e) {
				Log.e(TAG, "setAds()");			
	            e.printStackTrace();
			}
			//only set to ro if it was prior to this method executing
			if(!isMounted)
				RootTools.remount(SYS_MOUNT.getMountPoint().toString(), "RO");
			return retval;
    	}
    	
    	/** These aren't currently being used, because they don't work properly 
    	 * and are really just copies of what you can do in  Recovery anyway. 
		**/
    	
    	@Override
    	public void clearDalvik(){
    		
    		try {
    			RootTools.sendShell(new String[]{"rm -r /data/dalvik-cache","reboot now"}, 2000,-1);
    		} catch (Exception e) {
    			Log.e(TAG, "clearDalvik()");			
    			e.printStackTrace();
    		}
    	}
    	
    	@Override
    	public void clearCache(){
    		
    		try {
    			RootTools.sendShell("rm -R /cache",-1);
    		}catch (Exception e){
    			Log.e(TAG, "clearCache()");			
	            e.printStackTrace();
    		}
    	}
    	
    	@Override
    	public void fixPerms(){
    		
    		try {
    			RootTools.sendShell(new String[]{"chmod 777 /data/data/com.bamf.settings/files/fix_permissions",
    					"./data/data/com.bamf.settings/files/fix_permissions"},500,-1);
    		} catch (Exception e) {
    			Log.e(TAG, "fixPerms()");			
    			e.printStackTrace();
    		}
    	}
    	
    	@Override
    	public void setScheduler(String scheduler){
    		mService.get().setScheduler(scheduler);
    	}
    	
    	@Override
    	public void setVoltage(String file){
    		mService.get().setVoltage(file);
    	}
    	
    	@Override
    	public void setKernelValue(String file,String value){
    		mService.get().setKernelValue(file,value);
    	}
    	
    	@Override
    	public String getLastBackupDate(String dir){
    		return mService.get().getLastBackupDate(dir);
    	}
    	
    	@Override 
    	public boolean copyFile(String from, String to, boolean isRestore){
    		return mService.get().copyFile(from, to, isRestore);
    	}
    };
}
