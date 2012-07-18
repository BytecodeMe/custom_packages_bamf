/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bamf.ics.ltewidget.utils;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyProperties;

import android.util.Log;

public class RainRequest {
    
    // variables from smali
    private static final String DBG_TAG = "SkyNetworkTypeRequest";
    
    protected Context mContext;
    private Phone mPhone = null;
    private TelephonyManager mTelephonyManager;
    private MyHandler mHandler;
    private int mState;
    private int mNetworkType;
    public int networkType;
    protected Integer request = 0;
    private boolean mSwitching = false;
    
    static final int preferredNetworkMode = Phone.PREFERRED_NT_MODE;
    static final boolean DBG = true;

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
        	Log.i(DBG_TAG, "[onDataConnect] state:" + state + " networkType:" + networkType);
        	updateDataState();
        	updateNetworkType();
        }
    };
    
    private class MyHandler extends Handler {

        private static final int MESSAGE_GET_PREFERRED_NETWORK_TYPE = 0;
        private static final int MESSAGE_SET_PREFERRED_NETWORK_TYPE = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_GET_PREFERRED_NETWORK_TYPE:
                    handleGetPreferredNetworkTypeResponse(msg);
                    break;

                case MESSAGE_SET_PREFERRED_NETWORK_TYPE:
                    handleSetPreferredNetworkTypeResponse(msg);
                    break;
            }
        }

        private void handleGetPreferredNetworkTypeResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;

            if (ar.exception == null) {
                int modemNetworkMode = ((int[])ar.result)[0];

                if (DBG) {
                    log ("handleGetPreferredNetworkTypeResponse: modemNetworkMode = " +
                            modemNetworkMode);
                }

                int settingsNetworkMode = android.provider.Settings.Secure.getInt(
                        mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                        preferredNetworkMode);

                if (DBG) {
                    log("handleGetPreferredNetworkTypeReponse: settingsNetworkMode = " +
                            settingsNetworkMode);
                }

                //check that modemNetworkMode is from an accepted value
                if (modemNetworkMode == Phone.NT_MODE_WCDMA_PREF ||
                        modemNetworkMode == Phone.NT_MODE_GSM_ONLY ||
                        modemNetworkMode == Phone.NT_MODE_WCDMA_ONLY ||
                        modemNetworkMode == Phone.NT_MODE_GSM_UMTS ||
                        modemNetworkMode == Phone.NT_MODE_CDMA ||
                        modemNetworkMode == Phone.NT_MODE_CDMA_NO_EVDO ||
                        modemNetworkMode == Phone.NT_MODE_EVDO_NO_CDMA ||
                        modemNetworkMode == Phone.NT_MODE_GLOBAL ) {
                    if (DBG) {
                        log("handleGetPreferredNetworkTypeResponse: if 1: modemNetworkMode = " +
                                modemNetworkMode);
                    }

                    //check changes in modemNetworkMode and updates settingsNetworkMode
                    if (modemNetworkMode != settingsNetworkMode) {
                        if (DBG) {
                            log("handleGetPreferredNetworkTypeResponse: if 2: " +
                                    "modemNetworkMode != settingsNetworkMode");
                        }

                        settingsNetworkMode = modemNetworkMode;

                        if (DBG) { log("handleGetPreferredNetworkTypeResponse: if 2: " +
                                "settingsNetworkMode = " + settingsNetworkMode);
                        }

                        //changes the Settings.System accordingly to modemNetworkMode
                        android.provider.Settings.Secure.putInt(
                                mPhone.getContext().getContentResolver(),
                                android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                                settingsNetworkMode );
                    }

                } else if (modemNetworkMode == Phone.NT_MODE_LTE_ONLY) {
                    // LTE Only mode not yet supported on UI, but could be used for testing
                    if (DBG) log("handleGetPreferredNetworkTypeResponse: lte only: no action");
                } else {
                    if (DBG) log("handleGetPreferredNetworkTypeResponse: else: reset to default");
                    resetNetworkModeToDefault();
                }
            }
        }

        private void handleSetPreferredNetworkTypeResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;

            if (ar.exception == null) {
                int networkMode = mNetworkType;
                //android.provider.Settings.Secure.putInt(mPhone.getContext().getContentResolver(),
                //        android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                //        networkMode );
                log("set networkType success: " + mNetworkType);
                mSwitching = false;int t = Phone.NT_MODE_EVDO_NO_CDMA;
                mPhone.getPreferredNetworkType(obtainMessage(MESSAGE_GET_PREFERRED_NETWORK_TYPE));
            } else {
                mPhone.getPreferredNetworkType(obtainMessage(MESSAGE_GET_PREFERRED_NETWORK_TYPE));
            }
        }

        private void resetNetworkModeToDefault() {
            //set the Settings.System
            android.provider.Settings.Secure.putInt(mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                        preferredNetworkMode );
            //Set the Modem
            mPhone.setPreferredNetworkType(preferredNetworkMode,
                    this.obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));
        }
    }

    public RainRequest(Context context) {

    	mContext = context;
        mTelephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mPhone = PhoneFactory.getDefaultPhone();

        mHandler = new MyHandler();
        
        //networkType = Settings.Secure.getInt(mContext.getContentResolver(),
        //		"preferred_network_mode", SELECT_VALUE_NONE);
        
        
        String unk = "unknown";
    	String current = SystemProperties.get(TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE,
                unk);
    	
        mNetworkType = mTelephonyManager.getNetworkType();
        log( "Current network: mNetworkType=" + mNetworkType + ", "+current);
        

        //phone.getPreferredNetworkType(
        //        mHandler.obtainMessage(MESSAGE_GET_PREFERRED_NETWORKTYPE, request));
        
        log("I'm created!");

    }
    
    private final void
    updateDataState() {
    	mState = mTelephonyManager.getDataState();
    	/**
        switch (mState) {
            case TelephonyManager.DATA_CONNECTED:
                break;
            case TelephonyManager.DATA_CONNECTING:
                break;
            case TelephonyManager.DATA_DISCONNECTED:
                break;
            case TelephonyManager.DATA_SUSPENDED:
                break;
        }
    	 **/
    }

    private final void updateNetworkType() {
    	
    	String unk = "unknown";
    	String current = SystemProperties.get(TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE,
                unk);
    	
    	mNetworkType = mTelephonyManager.getNetworkType();
    	log("updateNetworkType: mNetworkType=" + mNetworkType + ", "+current);
    	
    	if(mNetworkType == TelephonyManager.NETWORK_TYPE_1xRTT || mNetworkType == TelephonyManager.NETWORK_TYPE_LTE){
        	log("LTE changing to 3G Auto");
        	mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        	setNetworkType(Phone.NT_MODE_GLOBAL);
        }
    }

    public void setNetworkType(int requestedType){
    	//networkType = type;
    	//mNetworkType = type;
        
        int mCallState = mTelephonyManager.getCallState();
    	
        if(mCallState == TelephonyManager.CALL_STATE_OFFHOOK ||
                mCallState == TelephonyManager.CALL_STATE_RINGING ){
            log("Phone is off the hook or ringing, exiting");
            return;
        }
        
    	log("setNetworkType: mNetworkType=" + mNetworkType + " requestedType:" + requestedType);
    	
    	int settingsNetworkMode = android.provider.Settings.Secure.getInt(
                mPhone.getContext().getContentResolver(),
                android.provider.Settings.Secure.PREFERRED_NETWORK_MODE, preferredNetworkMode);
        if (requestedType != settingsNetworkMode) {

            // If button has no valid selection && setting is LTE ONLY
            // mode, let the setting stay in LTE ONLY mode. UI is not
            // supported but LTE ONLY mode could be used in testing.
            //if ((modemNetworkMode == Phone.PREFERRED_NT_MODE) &&
            //    (settingsNetworkMode == Phone.NT_MODE_LTE_ONLY)) {
            //    return;
            //}

            if (requestedType == Phone.NT_MODE_LTE_ONLY){
                mTelephonyManager.listen(mPhoneStateListener,
                        PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                      | PhoneStateListener.LISTEN_DATA_ACTIVITY
                      | PhoneStateListener.LISTEN_SERVICE_STATE);
            }
            
            if(!mHandler.hasMessages(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE) && !mSwitching){
                Message msg = mHandler.obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE, request);

                mPhone.setPreferredNetworkType(requestedType, msg);
                    
                Settings.Secure.putInt(mContext.getContentResolver(),"preferred_network_mode",requestedType);
                mSwitching = true;
            }else{
                log("setNetworkType: found existing messages so decided not to send another");
            }
        }
    		
    }
    
    private static void log(String msg) {
        Log.d(DBG_TAG, msg);
    }
    
}
