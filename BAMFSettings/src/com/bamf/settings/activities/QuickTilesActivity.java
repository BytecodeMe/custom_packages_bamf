package com.bamf.settings.activities;

import java.io.FileOutputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.bamf.settings.R;
import com.bamf.settings.preferences.QuickTileOrderFragment;
import com.bamf.settings.preferences.QuickTilePreferenceFragment;
import com.bamf.settings.utils.CustomIconUtil;
import com.slidingmenu.lib.SlidingMenu;


public class QuickTilesActivity extends FragmentActivity {
	
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
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
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

}