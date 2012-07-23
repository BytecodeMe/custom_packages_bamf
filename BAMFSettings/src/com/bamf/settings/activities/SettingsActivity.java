package com.bamf.settings.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.bamf.bamfutils.services.IRootService;
import com.bamf.bamfutils.services.BAMFRootService;
import com.bamf.settings.R;
import com.bamf.settings.preferences.PerformanceKernelFragment;
import com.bamf.settings.preferences.VoltageBootFragment;
import com.bamf.settings.widgets.SettingsPagedView;
import com.bamf.settings.widgets.SettingsTabHost;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SettingsActivity extends Activity {
    
    private static final String TAG = SettingsActivity.class.getSimpleName();
	
	private SettingsTabHost mSettingsTabHost;
	private SettingsPagedView mSettingsContent;
	private static Bundle mState;
    private ServiceConnection mServiceConnection;
    
    private static IRootService mRootService;
    private static boolean mBound = false;
    
    private ActionBar mActionBar;
    private Menu mMenu;    
    
    private OnServiceBindedListener onServiceBindedListener = null;
    
    // Define our custom Listener interface
    public interface OnServiceBindedListener {
        public abstract void onServiceBinded(boolean isBound);
    }
    
    public void setOnServiceBindedListener(OnServiceBindedListener listener){
        onServiceBindedListener = listener;
    }
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        
        setContentView(R.layout.settings_pane);
        mState = savedInstanceState;
        
        setupServices(); 
        setupKernelViews(savedInstanceState);
        setupActionBar();
        setupViews();
        copyAssets();   
//        IntentFilter filter = new IntentFilter();        
//        filter.addAction(Intent.ACTION_SKIN_CHANGED);
//        registerReceiver(mBroadcastReceiver, filter); 
    }
    
//    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (Intent.ACTION_SKIN_CHANGED.equals(action)) {
//                // Normally it will restart on its own, but sometimes it doesn't.  Other times it's slow. 
//                // This will help it restart reliably and faster.            	
//            	
//                finish();
//            }
//        }
//    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        mMenu = menu;
        return true;
    }

    public void onTabChanged(String tabId) {
        if(mMenu != null){
            if(mMenu.size() > 0){
                mMenu.getItem(0).setVisible
                    (tabId.equals(SettingsTabHost.PERFORMANCE_TAB_TAG));
            }
        }
    }

	private void setupKernelViews(Bundle savedInstanceState) {
		
		//Setting up the Kernel/Voltage toggle in the action bar
		if(savedInstanceState==null){  
	        Fragment fragment = Fragment.instantiate(this, PerformanceKernelFragment.class.getName());	        
    		getFragmentManager().beginTransaction()
                .add(R.id.performance_container, fragment, "kernel_frag")
                .setBreadCrumbShortTitle(R.string.kernel_settings)                
                .commit();    		
	    }
		getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
        
			Fragment bootFragment = Fragment.instantiate(getBaseContext(), VoltageBootFragment.class.getName());
			
			@Override
			public void onBackStackChanged() {
			    
				TextView tv = (TextView)findViewById(R.id.kernel_title);				
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				if(getFragmentManager().findFragmentByTag("kernel_frag").isVisible()){   
					//Hides the onBoot checkbox for the voltage fragment when it isn't shown
					ft.remove(bootFragment);
					tv.setText(getString(R.string.kernel_settings));
				}else{
					ft.add(R.id.onboot_container,bootFragment,"boot");
					tv.setText(getString(R.string.voltage_settings));
				}
				ft.commit();
			}
		});
		
	}
	
	@Override
	public void onBackPressed(){
	    // if the first tab is not selected, just finish
	    if(mSettingsTabHost.getCurrentTabTag() != SettingsTabHost.PERFORMANCE_TAB_TAG){
	        this.finish();
	    }else{
	        super.onBackPressed();
	    }
	}

	private void setupActionBar() {
        mActionBar = getActionBar();
    }

    private void setupViews() {
		
		// Setup Settings ViewSpace
        mSettingsTabHost = (SettingsTabHost)
                findViewById(R.id.settings_pane);
        mSettingsContent = (SettingsPagedView)
                mSettingsTabHost.findViewById(R.id.settings_pane_content);
        mSettingsContent.setup(this);
		
	}
    
    private void copyAssets(){
    	
    	new Thread() {		    
			@Override public void run() {	
				 
				try {
				    File f = new File(getDir("files", Context.MODE_WORLD_READABLE), "hostsopen");
			        if(!f.exists()){
			        	AssetManager assetManager = getAssets();
			    	    String[] files = null;
			    	    try {
			    	        files = assetManager.list("");
			    	    } catch (Exception e) {			    	    	
			            	e.printStackTrace();
			    	    }
			    	    for(String filename : files) {
			    	        InputStream in = null;
			    	        OutputStream out = null;
			    	        try {
			    	          in = assetManager.open(filename);
			    	          out = new FileOutputStream(new File(getDir("files", 
			    	                  Context.MODE_WORLD_READABLE), filename));
			    	          copyFile(in, out);
			    	          in.close();
			    	          in = null;
			    	          out.flush();
			    	          out.close();
			    	          out = null;
			    	        } catch(Exception e) {			    	        	
			    	        	e.printStackTrace();
			    	        }       
			    	    }
			        }
			    }catch(Exception e){			        	
			       	e.printStackTrace();
			    }			
			}
		}.start();        
    }
    
    private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
    
    private void setupServices() {
        
        if(mServiceConnection == null){
            mServiceConnection = new ServiceConnection(){

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mRootService = IRootService.Stub.asInterface(service);
                    mBound = true;
                    onServiceBindedListener.onServiceBinded(mBound);
                    Log.d(TAG, "root service is bound");
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mServiceConnection = null;
                    mRootService = null;
                    mBound = false;
                    onServiceBindedListener.onServiceBinded(mBound);
                    Log.d(TAG, "root service is unbound");
                }               
            };
            try{
                if(bindService(new Intent(new Intent(createPackageContext("com.bamf.BAMFUtils", 0)
                        , BAMFRootService.class)), mServiceConnection, 
                        Context.BIND_AUTO_CREATE)){
                    Log.d(TAG, "service started and the bind was successful");
                }
            }catch(Exception e){
                Log.e(TAG, "service did not start", e);
            }
        }
    }
	
    public static IRootService getRootService(){
        return mRootService;
    }
    
    public static boolean isRootServiceBound(Context context){
        if(!mBound){
            Toast.makeText(context, "Root service was not found", Toast.LENGTH_SHORT).show();
        }
        return mBound;
    }
    
    public static Bundle getState(){
        return mState;
    }
    
    @Override
    public void finish(){
        super.finish();
        releaseService();
    }

    private void releaseService() {
        if(mBound && mServiceConnection != null) {
            // this is an asynchronous call so we should not set the connection variable to null here
            unbindService(mServiceConnection);
            Log.d(TAG, "service was stopped and unbound");
        }       
    } 
	
}
