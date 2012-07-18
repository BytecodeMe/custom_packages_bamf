package com.bamf.ics.ltewidget.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.internal.telephony.Phone;

public class Receiver extends BroadcastReceiver
{
	private RainRequest mNetworkHandler;
	private final static String TAG = "SkyLteReceiver";

	public void onReceive(Context paramContext, Intent paramIntent)
	{
		Log.d(TAG, "I have the request");
		
		{
			if (paramIntent.getAction().equals("com.bamf.settings.request.LTE_NETWORK_CHANGE")){
				if (paramIntent.getExtras() != null){
	
					int i = paramIntent.getExtras().getInt("networkType");
					Log.d(TAG, "networkType = "+i);
					if(i != 0 && (i == Phone.NT_MODE_CDMA || i == Phone.NT_MODE_GLOBAL )){
    					if(mNetworkHandler == null){
    			    		mNetworkHandler = new RainRequest(paramContext);
    			    	}
    					this.mNetworkHandler.setNetworkType(i);
					}else{
					    Log.w(TAG, "Invalid or no network type specified");
					}
				}else{
					Log.w(TAG, "No extras found");
				}
			}else{
				Log.w(TAG, "Action did not match: "+paramIntent.getAction());
			}
		}
	}
}