package com.bamf.settings.activities;

import java.io.FileOutputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.bamf.settings.R;
import com.bamf.settings.preferences.QuickTileOrderFragment;
import com.bamf.settings.preferences.QuickTilePreferenceFragment;
import com.bamf.settings.utils.CustomIconUtil;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.slidingmenu.lib.SlidingMenu.OnOpenedListener;


public class QuickTilesActivity extends FragmentActivity 
	implements OnClosedListener, OnOpenedListener {
	
	private static final String TAG = QuickTilesActivity.class.getSimpleName();
	private static final boolean DEBUG = false;
	private SlidingMenu mMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(R.string.quick_tiles);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// set the Above View
		setContentView(R.layout.content_frame);
		getFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, new QuickTileOrderFragment())
		.commit();

		// configure the SlidingMenu
		mMenu = new SlidingMenu(this);
		mMenu.setOnClosedListener(this);
		mMenu.setOnOpenedListener(this);
		mMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mMenu.setShadowWidthRes(R.dimen.shadow_width);
		mMenu.setShadowDrawable(R.drawable.shadow);
		mMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mMenu.setBehindScrollScale(0);
		mMenu.setFadeDegree(0.35f);
		mMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		mMenu.setMenu(R.layout.menu_frame);
		getFragmentManager()
		.beginTransaction()
		.replace(R.id.menu_frame, new QuickTilePreferenceFragment())
		.commit();
		
		Bundle extras = getIntent().getExtras();
		if(extras!=null){
			if(extras.getBoolean("settings",false)){
				mMenu.showMenu();
			}
		}
	}
	
	@Override
	public void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		Bundle extras = intent.getExtras();
		if(extras!=null){
			if(extras.getBoolean("settings",false)){
				mMenu.showMenu();
			}
		}else{
			mMenu.showContent(false);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.quick_tiles, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		
		case android.R.id.home:
			finish();
			return true;
		case R.id.settings:
			if (mMenu.isMenuShowing()) {
				mMenu.showContent();
			} else {
				mMenu.showMenu();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (mMenu.isMenuShowing()) {
			mMenu.showContent();
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { 
		
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                //most likely, the icon is in here
                final Bundle extras = data.getExtras();
                Bitmap b = null;
                
                if (extras!= null) {
                    /* return from a cropped image */
                    if(extras.containsKey("data")) {
                        b = (Bitmap)extras.get("data");
                    /* returns from an icon pack */
                    }else if(extras.containsKey("icon")){
                        b = (Bitmap)extras.get("icon");
                    }
                    if(b == null){
                        /* returns from the gallery */
                        b = CustomIconUtil.getInstance(this).readBitmap(data.getData()); 
                    }
                }else{
                    b = CustomIconUtil.getInstance(this).readBitmap(data.getData());
                }
                
                //if we found an icon, write it to a file
                if(b != null){
                    FileOutputStream outStream;
                    try {
                        outStream = new FileOutputStream(CustomIconUtil.getInstance(this).getTempFile());
                        b.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                        outStream.flush();
                        outStream.close();
                        
                        // reload the main fragment
                        getFragmentManager()
                		.beginTransaction()
                		.replace(R.id.content_frame, new QuickTileOrderFragment())
                		.commit();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
	}

	@Override
	public void onOpened() {
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getActionBar().setHomeButtonEnabled(false);
	}

	@Override
	public void onClosed() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}

}