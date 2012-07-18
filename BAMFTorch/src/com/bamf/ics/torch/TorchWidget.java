package com.bamf.ics.torch;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.internal.widget.Flashlight;
import com.bamf.ics.torch.R;
import com.bamf.ics.torch.utils.TorchToggleService;

public class TorchWidget extends AppWidgetProvider {
	
	private final static String TAG = TorchWidget.class.getSimpleName();
	private final static boolean DEBUG = false;
	private int[] mAppWidgetIds;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
		int[] appWidgetIds) {
		
		Intent intent = new Intent(context, TorchService.class);
		context.startService(intent);
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds){
		AppWidgetManager mManager = AppWidgetManager.getInstance(context);
		mAppWidgetIds = mManager.getAppWidgetIds(new ComponentName(context, TorchWidget.class));
		if(mAppWidgetIds.length == 0){
			if(DEBUG) Toast.makeText(context, "No more widgets, stopping service", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(context, TorchService.class);
			context.stopService(intent);
		}
	}
	
	public static class TorchService extends Service {
		private Context mContext;
		
		@Override
		public void onStart(Intent intent, int startId){
			mContext = this;
			createTorchReceiver();
			doUpdate();
			if(DEBUG) Toast.makeText(this, "started service", Toast.LENGTH_SHORT).show();
		}
		
		private void doUpdate() {
		    Intent newIntent = new Intent(mContext, UpdateService.class);
            mContext.startService(newIntent);            
        }

        private void createTorchReceiver() {
           IntentFilter filter = new IntentFilter();
           filter.addAction(Flashlight.FLASHLIGHT_STATE_CHANGED_ACTION);
           mContext.registerReceiver(mFlashlightReceiver, filter);
        }

        @Override
		public void onDestroy(){
			mContext.unregisterReceiver(mFlashlightReceiver);
		}

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}
		
		private BroadcastReceiver mFlashlightReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                int state = 0;
                if(Flashlight.FLASHLIGHT_STATE_CHANGED_ACTION.equals(intent.getAction())){
                    state = intent.getIntExtra(Flashlight.EXTRA_FLASH_STATE, 0);
                    if(DEBUG)Toast.makeText(mContext, "got broadcast from Flashlight:"+state, Toast.LENGTH_SHORT).show();
                }
                
                Intent newIntent;
                switch(state){
                    case Flashlight.STATE_OFF:
                        if(DEBUG){Log.d(TAG,"torch turned off");}
                        newIntent = new Intent(mContext, UpdateService.class);
                        newIntent.putExtra("mode", state);
                        mContext.startService(newIntent);
                        break;
                    case Flashlight.STATE_DEATH_RAY:
                    case Flashlight.STATE_HIGH:
                    case Flashlight.STATE_STROBE:
                    case Flashlight.STATE_ON:
                        if(DEBUG){Log.d(TAG,"torch turned on");}
                        newIntent = new Intent(mContext, UpdateService.class);
                        newIntent.putExtra("mode", state);
                        mContext.startService(newIntent);
                        break;
                    default:
                        break; 
                }
            }
		    
		};
	}
	
	public static class UpdateService extends Service {
		private final static String TAG = UpdateService.class.getSimpleName();
				
		private AppWidgetManager mManager;
		private int[] mAppWidgetIds;
		private Context mContext;
		
		@Override
		public void onCreate(){
			super.onCreate();
			mContext = this;
		}
		
		@Override
		public void onStart(Intent intent, int startId) {
			Log.i(TAG, "Called");
			
			mManager = AppWidgetManager.getInstance(this
					.getApplicationContext());
			
			mAppWidgetIds = mManager.getAppWidgetIds(new ComponentName(mContext, TorchWidget.class));
			
			try{

				if (mAppWidgetIds.length > 0) {
					for (int widgetId : mAppWidgetIds) {
						RemoteViews updateViews = buildUpdate(this, intent);
						
				        // Push update for this widget to the home screen	        
						mManager.updateAppWidget(widgetId, updateViews);
					}
					stopSelf();
				}
			}catch(NullPointerException e){}
		}
		
		private RemoteViews buildUpdate(Context context, Intent intent){
			
			// Build an update that holds the updated widget contents
		    RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_ics);
			
			//int mode = Flashlight.instance(context).getFlashMode();
		    int mode = intent.getIntExtra("mode", 0);
			int setting = 0;
			int back = 0;
			boolean enabled = true;
			
			
			switch(mode){
				case Flashlight.STATE_OFF:
					setting = R.drawable.icon_torch_off;
					back = R.drawable.appwidget_settings_ind_off_holo;
					enabled = true;
					break;
				case Flashlight.STATE_HIGH:
				case Flashlight.STATE_DEATH_RAY:
				case Flashlight.STATE_STROBE:
				case Flashlight.STATE_ON:
					setting = R.drawable.icon_torch_on;
					back = R.drawable.appwidget_settings_ind_on_holo;
					enabled = true;
					break;
				default:
					setting = R.drawable.icon_torch_off;
					back = R.drawable.appwidget_settings_ind_off_holo;
					enabled = false;
					Log.d(TAG, "UNK flashlight mode="+mode);
			}

			updateViews.setImageViewResource(R.id.setting_item, setting);	
			updateViews.setInt(R.id.setting_trigger, "setBackgroundResource", back);
			updateViews.setBoolean(R.id.setting_press, "setEnabled", enabled);
			
			if(enabled){
				Intent defineIntent = null;
	
				defineIntent = new Intent(mContext, TorchToggleService.class);
				defineIntent.putExtra("mode", (mode==Flashlight.STATE_OFF)?Flashlight.STATE_ON:Flashlight.STATE_OFF);
	            PendingIntent pendingIntent = PendingIntent.getService(context,
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
	}
}