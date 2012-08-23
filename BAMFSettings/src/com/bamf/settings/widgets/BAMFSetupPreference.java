package com.bamf.settings.widgets;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.Preference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Toast;

public class BAMFSetupPreference extends Preference implements OnLongClickListener {
	
	private Context mContext;	
	
	public BAMFSetupPreference(Context context, AttributeSet attrib) {
		super(context, attrib);
		// TODO Auto-generated constructor stub
		mContext = context;
		
	}
	
    private void eggEnable(boolean enable){
    	Settings.System.putInt(mContext.getContentResolver(), Settings.System.NAVBAR_FLIP_OVER, enable?1:0);
    	Settings.System.putInt(mContext.getContentResolver(), Settings.System.NAVBAR_ORDER_CHANGED, 1);
	}
    
    private boolean eggEnabled(){
        return Settings.System.getInt(mContext.getContentResolver(), Settings.System.NAVBAR_FLIP_OVER, 0)==1;
    }

	@Override
	public boolean onLongClick(View view) {		 

		 if(!eggEnabled()){
			 Toast.makeText(mContext, "Uh oh, you broke your buttons!", 3000).show();
			 eggEnable(true);
		 }else{
			 Toast.makeText(mContext, "Wasn't that fun?", 3000).show();
			 eggEnable(false);
		 }	 
		 
		return true;
	}
}