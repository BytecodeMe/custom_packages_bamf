package com.bamf.ics.ltewidget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.bamf.ics.ltewidget.utils.ImageAnimation;
import com.bamf.ics.ltewidget.utils.ImageAnimation.AnimationPlay;
import com.bamf.ics.ltewidget.R;

public class LTEToggleWidget extends AppWidgetProvider {
	
	private final static String TAG = LTEToggleWidget.class.getSimpleName();
	private final static boolean DEBUG = false;
	private int[] mAppWidgetIds;
	
	private final static int MSG_START_ANIMATION = 0x101;
	private final static int MSG_UPDATE_ANIMATION = 0x103;
	private final static int MSG_STOPANDRESET_ANIMATION = 0x105;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
		int[] appWidgetIds) {
		
		Intent intent = new Intent(context, NetworkObserverService.class);
		context.startService(intent);
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds){
		AppWidgetManager mManager = AppWidgetManager.getInstance(context);
		mAppWidgetIds = mManager.getAppWidgetIds(new ComponentName(context, LTEToggleWidget.class));
		if(mAppWidgetIds.length == 0){
			if(DEBUG) Toast.makeText(context, "No more widgets, stopping service", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(context, NetworkObserverService.class);
			context.stopService(intent);
		}
	}
	
	public static class NetworkObserverService extends Service {
		private Context mContext;
		private SettingsObserver mSettingsObserver;
		private Handler mHandler;
		private int mNetworkState;
		
		@Override
		public int onStartCommand(Intent intent, int flags, int startId){
			mContext = this;
			createObserver();
			createWifiReceiver();
			if(DEBUG) Toast.makeText(this, "started service", Toast.LENGTH_SHORT).show();
			return START_STICKY;
		}
		
		private void createWifiReceiver() {
           IntentFilter filter = new IntentFilter();
           filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
           filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
           mContext.registerReceiver(mCallWifiReciever, filter);
        }

        @Override
		public void onDestroy(){
			if(mSettingsObserver != null){
				mSettingsObserver.stop();
				mSettingsObserver = null;
			}
			mContext.unregisterReceiver(mCallWifiReciever);
		}

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}
		
		private BroadcastReceiver mCallWifiReciever = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())){
                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1);
                    Intent newIntent;
                    switch(state){
                        case WifiManager.WIFI_STATE_DISABLED:
                            if(DEBUG){Log.d(TAG,"wifi disabled; re-enabling widget");}
                            mSettingsObserver.update(500);
                            break;
                        case WifiManager.WIFI_STATE_ENABLED:
                            if(DEBUG){Log.d(TAG,"wifi enabled; disabling widget");}
                            newIntent = new Intent(mContext, UpdateService.class);
                            newIntent.putExtra("networkType", Phone.NT_MODE_LTE_ONLY);
                            mContext.startService(newIntent);
                            break;
                        case WifiManager.WIFI_STATE_DISABLING:
                        case WifiManager.WIFI_STATE_ENABLING:
                        default:
                            break; 
                    }
                }else if(TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())){
                    String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                    Intent newIntent;
                    if(state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                        if(DEBUG){Log.d(TAG,"phone state idle; re-enabling widget");}
                        mSettingsObserver.update();
                    }else if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) ||
                            state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                        if(DEBUG){Log.d(TAG,"phone is ringing or off the hook; disabling widget");}
                        newIntent = new Intent(mContext, UpdateService.class);
                        newIntent.putExtra("networkType", Phone.NT_MODE_LTE_ONLY);
                        mContext.startService(newIntent);
                    }
                }
            }
		    
		};
		
		private void createObserver(){
			if(mHandler == null){
				mHandler = new Handler();
			}
			if (mSettingsObserver == null) {
	            mSettingsObserver = new SettingsObserver(mHandler);
	            mSettingsObserver.observe();
	        }else{
	        	mSettingsObserver.update();
	        }
		}
		
		private class SettingsObserver extends ContentObserver {

			public SettingsObserver(Handler handler) {
				super(handler);
			}
			
			void observe() {
	            ContentResolver resolver = mContext.getContentResolver();
	            resolver.registerContentObserver(Settings.Secure.getUriFor(
	                    "preferred_network_mode"), false, this);
	            update();
	        }
			
			public void stop() {
	            ContentResolver resolver = mContext.getContentResolver();
	            resolver.unregisterContentObserver(this);
	        }

	        @Override
	        public void onChange(boolean selfChange) {
	            update();
	        }
	        
	        public void update(int delay){
	            try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
	            update();
	        }

	        public void update() {
	            ContentResolver resolver = mContext.getContentResolver();
	            mNetworkState = Settings.Secure.getInt(resolver,
	                    "preferred_network_mode", Phone.NT_MODE_GLOBAL);
	            Intent intent = new Intent(mContext, UpdateService.class);
	            intent.putExtra("networkType", mNetworkState);
	            mContext.startService(intent);
	        }
			
		}
	}
	
	public static class UpdateService extends Service {
		private final static String TAG = UpdateService.class.getSimpleName();
				
		private ImageAnimation mAnimator;
		private AnimHandler mAnimHandler;
		private AppWidgetManager mManager;
		private TelephonyManager mTelephonyManager;
		
		private int mState;
		private int[] mAnimIds;
		private boolean mInAnimation;
		private int[] mAppWidgetIds;
		private Context mContext;
		private Intent mIntent;
		
		@Override
		public void onCreate(){
			super.onCreate();
			mAnimHandler = new AnimHandler();
			mAnimIds = new int[]{R.drawable.icon_lte_01, R.drawable.icon_lte_02, R.drawable.icon_lte_03};
			mContext = this;
			
			mTelephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		}
		
		private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
	        @Override
	        public void onDataConnectionStateChanged(int state, int networkType) {
	            Log.i(TAG, "[onDataConnect] state:" + state + " networkType:" + networkType);
	            updateDataState();
	        }
	    };
	    
	    private final void updateDataState() {
	        mState = mTelephonyManager.getDataState();
	        Intent intent;
	        
	        switch (mState) {
	            case TelephonyManager.DATA_CONNECTED:
	                int mNetworkState = Settings.Secure.getInt(mContext.getContentResolver(),
	                        "preferred_network_mode", Phone.NT_MODE_GLOBAL);
	                intent = new Intent("dummy");
	                intent.putExtra("networkType", mNetworkState);
	                onStart(intent, 0);
	                break;
	            case TelephonyManager.DATA_CONNECTING:
	                break;
	            case TelephonyManager.DATA_DISCONNECTED:
	                break;
	            case TelephonyManager.DATA_SUSPENDED:
	                break;
	        }
	    }
		
		@Override
		public void onStart(Intent intent, int startId) {
			Log.i(TAG, "Called");
			
			if(intent==null)return;
			
			int networkType = intent.getExtras().getInt("networkType");
			mManager = AppWidgetManager.getInstance(this
					.getApplicationContext());
			
			mIntent = intent;
			mAppWidgetIds = mManager.getAppWidgetIds(new ComponentName(mContext, LTEToggleWidget.class));
			
			try{
				if(networkType == Phone.NT_MODE_GLOBAL && mState != TelephonyManager.DATA_CONNECTED /*&& !mInAnimation*/){
					startAnimation();
					startListening();
				}else{
				    clearAnimation();
				    stopListening();
					//mState = TelephonyManager.DATA_UNKNOWN;
					if (mAppWidgetIds.length > 0) {
						for (int widgetId : mAppWidgetIds) {
							RemoteViews updateViews = buildUpdate(this, intent);
							//updateViews = buildUpdate(this, intent);
							
					        // Push update for this widget to the home screen	        
							mManager.updateAppWidget(widgetId, updateViews);
						}
						stopSelf();
					}
				}
			}catch(NullPointerException e){}
		}
		
		private void startListening() {
		    mTelephonyManager.listen(mPhoneStateListener,
                    PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                  | PhoneStateListener.LISTEN_DATA_ACTIVITY
                  | PhoneStateListener.LISTEN_SERVICE_STATE);            
        }
		
		private void stopListening(){
		    mTelephonyManager.listen(mPhoneStateListener,
                    PhoneStateListener.LISTEN_NONE);
		}

        public void createAnimation(long duration, boolean bReverse){
			if(mAnimator != null){
				mAnimator.clearAnimation();
			}
			
			mAnimator = new ImageAnimation();
			mAnimator.setFrameCount(mAnimIds.length);
			mAnimator.setReverse(bReverse);
			mAnimator.setAnimationPlayListener(mAnimationListener);
			mAnimator.startAnimation(duration, true);
		}
		
		public void clear(){
			mAnimHandler = null;
			clearAnimation();
		}
		
		private void startAnimation(){
			if(mAnimHandler != null){
				mAnimHandler.sendEmptyMessage(MSG_START_ANIMATION);
			}
		}
		
		private void clearAnimation(){
			if(mAnimator != null){
				mAnimator.clearAnimation();
			}
		}
		
		private RemoteViews buildUpdate(Context context, Intent intent){
			
			// Build an update that holds the updated widget contents
		    RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_ics);
			
			int networkType = intent.getExtras().getInt("networkType");
			String text = null;
			int setting = 0;
			int back = 0;
			boolean enabled = true;
			
			switch(networkType){
				case Phone.NT_MODE_CDMA:
					text = "3G";
					setting = R.drawable.icon_lte_off;
					back = R.drawable.appwidget_settings_ind_off_holo;
					enabled = true;
					break;
				case Phone.NT_MODE_GLOBAL:
					text = "LTE/3G";
					setting = R.drawable.icon_lte_on;
					back = R.drawable.appwidget_settings_ind_on_holo;
					enabled = true;
					break;
				case Phone.NT_MODE_LTE_ONLY:
					text = "LTE";
					setting = R.drawable.icon_lte_off;
					back = R.drawable.appwidget_settings_ind_off_holo;
					enabled = false;
					break;
				default:
					text = "UNK";
					setting = R.drawable.icon_lte_off;
					back = R.drawable.appwidget_settings_ind_off_holo;
					enabled = false;
					Log.d(TAG, "UNK networkType="+networkType);
			}
			//updateViews.setTextViewText(R.id.toggle_label, text);
			updateViews.setImageViewResource(R.id.setting_item, setting);	
			updateViews.setInt(R.id.setting_trigger, "setBackgroundResource", back);
			updateViews.setBoolean(R.id.setting_press, "setEnabled", enabled);
			
			if(enabled){
				Intent defineIntent = null;
	
				defineIntent = new Intent("com.bamf.settings.request.LTE_NETWORK_CHANGE");
				defineIntent.putExtra("networkType", (networkType==Phone.NT_MODE_CDMA)?Phone.NT_MODE_GLOBAL:Phone.NT_MODE_CDMA);
	            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
	                    0 /* no requestCode */, defineIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	            updateViews.setOnClickPendingIntent(R.id.setting_press, pendingIntent);
	            if(DEBUG){Log.d(TAG,"I set the pending intent");}
			}
			
			return updateViews;
		}

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}
		
		private AnimationPlay mAnimationListener = new AnimationPlay(){

			@Override
			public void onAnimationStart() {
			}

			@Override
			public void onAnimationStop() {
				if(mAnimHandler != null){
					mAnimHandler.sendEmptyMessage(MSG_STOPANDRESET_ANIMATION);
				}
			}

			@Override
			public void onAnimationUpdate(int nFrameId) {
				if(mInAnimation){
					if(mAnimHandler != null && nFrameId >= 0 && nFrameId < mAnimIds.length){
						mAnimHandler.sendMessage(mAnimHandler.obtainMessage(
								MSG_UPDATE_ANIMATION, mAnimIds[nFrameId], 0));
					}
				}else{
					if(mAnimator != null){
						mAnimator.clearAnimation();
					}
				}
			}
			
		};
		
		private class AnimHandler extends Handler{
			private final static int duration = 800;
			@Override
			public void handleMessage(Message msg){
				RemoteViews updateViews = null;
				switch(msg.what){
					case MSG_START_ANIMATION:
						mInAnimation = true;
						createAnimation(duration, false);
						break;
					case MSG_UPDATE_ANIMATION:
						updateViews = buildUpdate(mContext, mIntent);
						updateViews.setImageViewResource(R.id.setting_item, msg.arg1);
						updateViews.setInt(R.id.setting_trigger, "setBackgroundResource", 
						        R.drawable.appwidget_settings_ind_mid_holo);
						for (int widgetId : mAppWidgetIds) {
							mManager.updateAppWidget(widgetId, updateViews);
						}
						break;
					case MSG_STOPANDRESET_ANIMATION:
					    mInAnimation = false;
						updateViews = buildUpdate(mContext, mIntent);
						for (int widgetId : mAppWidgetIds) {
							mManager.updateAppWidget(widgetId, updateViews);
						}				
						break;
					default:
				}
			}
		}
	}
}