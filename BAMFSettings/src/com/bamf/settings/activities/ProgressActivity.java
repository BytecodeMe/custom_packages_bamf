package com.bamf.settings.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class ProgressActivity extends Activity {

	private ProgressDialog mProgress;
		
	private static final String TAG = ProgressActivity.class.getSimpleName();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                    
        
        mProgress = new ProgressDialog(this);
		mProgress.setIndeterminate(true);
		mProgress.setCancelable(false);
		mProgress.setMessage("Updating Skin...");
		mProgress.show();
//		IntentFilter filter = new IntentFilter();        
//        filter.addAction(Intent.ACTION_SKIN_CHANGE_COMPLETE);
//        registerReceiver(mBroadcastReceiver, filter);          
    	
    }	
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//            if (Intent.ACTION_SKIN_CHANGE_COMPLETE.equals(action)) {
//                // Normally it will restart on its own, but sometimes it doesn't.  Other times it's slow. 
//                // This will help it restart reliably and faster.            	
//                if(mProgress != null && mProgress.isShowing()){
//                	mProgress.cancel();
//                }
//                finish();
//            }
        }
    };
    
    
}