package com.bamf.copy;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

public class BAMFCopyActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String action = getIntent().getAction();
        if(action.equals(Intent.ACTION_SEND)){
        	handleCopy(getIntent());
        }
        
        finish();
    }
    
    private void handleCopy(Intent intent){
    	ClipboardManager clipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    	
    	Bundle extras = intent.getExtras();
    	if(extras != null){
    		String text = extras.getString(Intent.EXTRA_TEXT);
    		ClipData clip = new ClipData(text, new String[]{"text/plain"}, new ClipData.Item(text));
    		clipMan.setPrimaryClip(clip);
    		
    		Toast.makeText(getApplicationContext(), "text copied to clipboard", Toast.LENGTH_SHORT).show();
    	}
    	
    	finish();
    }
}