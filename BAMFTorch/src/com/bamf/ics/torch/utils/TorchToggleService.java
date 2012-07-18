package com.bamf.ics.torch.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.widget.Toast;

import com.android.internal.widget.Flashlight;

import java.lang.ref.WeakReference;

public class TorchToggleService extends Service {
    
    private Context mContext;
    
    public TorchToggleService() {    
    }
    
    public void ToggleTorch(int mode){
        
            try{
                Flashlight.instance(mContext).setFlashMode(mode);
            }catch(RuntimeException e){
                Toast.makeText(mContext, "Unable to open flash device", Toast.LENGTH_SHORT).show();
            }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        // the intent will be null is the system restarts the service when returning START_STICKY
        if(intent != null){
            int mode = intent.getIntExtra("mode", Flashlight.STATE_TOGGLE);
            ToggleTorch(mode);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return mBinder;
    }
    
    static class ServiceStub extends ITorchToggleService.Stub {
        WeakReference<TorchToggleService> mService;
        
        ServiceStub(TorchToggleService service) {
            mService = new WeakReference<TorchToggleService>(service);
        }
        
        public void ToggleTorch(int mode){
            mService.get().ToggleTorch(mode);
        }
    }
    
    private final IBinder mBinder = new ServiceStub(this);

}
