package com.bamf.settings.activities;

import java.util.ArrayList;
import java.util.List;

import com.bamf.settings.R;
import com.bamf.settings.adapters.SkinPreviewAdapter;
import com.bamf.settings.widgets.CoverFlow;
//import com.bamf.utils.SkinEngine;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.IActivityManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
//import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.TextView;

public class VisualSkinActivity extends Activity implements OnClickListener,OnItemClickListener,OnItemSelectedListener{

	private PackageManager pm;
	private ArrayList<PackageInfo> mSkinPackages;
	private TextView mSkinText;	
	private int mSkinIndex = 0;
	private CoverFlow mFlow;
	private Button mUninstall;
	
	private static final String TAG = VisualSkinActivity.class.getSimpleName();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        
        pm = getPackageManager();
        
        setupViews();     
        
        setupActionBar();      
    	
    }	

	private void setupViews() {
		
		setContentView(R.layout.visual_settings_skin_chooser);
		
		mSkinPackages = getSkinPackages();
        
        mFlow = (CoverFlow)findViewById(R.id.skins_content);
        mFlow.setAdapter(new SkinPreviewAdapter(this,mSkinPackages));
        mFlow.setSpacing(-50);
        mFlow.setSelection(mSkinIndex, true);        
        mFlow.setAnimationDuration(500); 
        mFlow.setOnItemClickListener(this);
        mFlow.setOnItemSelectedListener(this); 
        
        mSkinText = (TextView)findViewById(R.id.skin_name);        
        
        Button apply = (Button)findViewById(R.id.apply_button);
        apply.setOnClickListener(this);
        
        mUninstall = (Button)findViewById(R.id.uninstall_button);
        mUninstall.setOnClickListener(this); 
        
        updateSkinText(mSkinIndex);     
		
	}
	
	private void updateSkinText(int position) {
		
        StringBuilder sb = new StringBuilder();
		String name = "Default";
		if(mSkinPackages.get(position).applicationInfo != null){
        	name = mSkinPackages.get(position).applicationInfo.loadLabel(pm).toString();
        	sb.append(name);
//			if((mSkinPackages.get(position).packageName).equals(getResources().getConfiguration().skin)){
//				sb.append(" (Applied)");
//			}
		}else{
			sb.append(name);
//			if(getResources().getConfiguration().skin.equals(SkinEngine.DEFAULT))
//				sb.append(" (Applied)");
		}			
        mSkinText.setText(sb.toString());
        if(name.equals("Default")){
        	mUninstall.setEnabled(false);        	
        } else{
        	mUninstall.setEnabled(true);
        }
	}

	private ArrayList<PackageInfo> getSkinPackages() {
		
		mSkinIndex = 0;
		mSkinPackages = new ArrayList<PackageInfo>();
		mSkinPackages.add(new PackageInfo());		
		
//		List<PackageInfo> packs = pm.getInstalledSkinPackages();		
//		for(int i = 0;i < packs.size();i++){
//			PackageInfo pi = packs.get(i);					
//			if(pi.applicationInfo.skinType.equals("skin")){
//				Log.w(TAG, "Adding package " +pi.packageName);
//				mSkinPackages.add(pi);
//			}
//			if(pi.packageName.equals(getResources().getConfiguration().skin)){
//				mSkinIndex = i+1;
//			}
//		}	
		return mSkinPackages;
	}

	private void applySkin(String name){
		
		
		
		IActivityManager aM = ActivityManagerNative.getDefault();
//		try{
//			Configuration newConfig = aM.getConfiguration();
//			newConfig.skin = name;
//			SystemProperties.set("persist.sys.skin", name);					
//			sendBroadcast(new Intent(Intent.ACTION_SKIN_CHANGED));					
//			aM.updateConfiguration(newConfig);	
//			Intent i = new Intent("com.bamf.settings.progressactivity");
//			startActivity(i);
//			finish();
//		}catch(RemoteException e){
//			
//		}
	}
    
//    @Override
//	public void onConfigurationChanged(Configuration newConfig){		
//		super.onConfigurationChanged(newConfig);
//		
//	}
    
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setTitle(R.string.theme_settings);             
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked;
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void onClick(View v) {
		
//		switch(v.getId()){
//			case R.id.apply_button:
//				String name = SkinEngine.DEFAULT;				
//				if(mSkinPackages.get(mFlow.getSelectedItemPosition()).packageName != null)
//		        	name = mSkinPackages.get(mFlow.getSelectedItemPosition()).packageName;
//		        applySkin(name);
//				break;
//			case R.id.uninstall_button:
//				String pkg = mSkinPackages.get(mFlow.getSelectedItemPosition()).packageName;
//				Uri packageURI = Uri.parse("package:" + pkg);
//	    		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
//	    		startActivityForResult(uninstallIntent,1);	    		
//				break;
//			
//		}
		
	}
	
	@Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode == 0) {	
//			if(mSkinPackages.get(mFlow.getSelectedItemPosition()).packageName.equals(getResources().getConfiguration().skin)){
//    			applySkin(SkinEngine.DEFAULT);
//    		}else
//    			setupViews();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {		
		
		if((mSkinPackages.get(position).packageName != null) && (mFlow.getSelectedItemPosition() == position)){
        	applySkin(mSkinPackages.get(position).packageName);        	
		}else if(mFlow.getSelectedItemPosition() == position){
			//applySkin(SkinEngine.DEFAULT);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		
		updateSkinText(position);
        	
	}
	@Override
	public void onNothingSelected(AdapterView<?> parent) {	
	}    
}