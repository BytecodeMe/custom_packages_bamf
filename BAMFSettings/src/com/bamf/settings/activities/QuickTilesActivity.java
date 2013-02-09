package com.bamf.settings.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.bamf.settings.R;
import com.bamf.settings.preferences.QuickTileOrderFragment;
import com.bamf.settings.preferences.QuickTilePreferenceFragment;
import com.bamf.settings.preferences.VisualLockscreenFragment;
import com.bamf.settings.utils.QuickTileHelper.CustomIconUtil;
import com.slidingmenu.lib.SlidingMenu;


public class QuickTilesActivity extends FragmentActivity {
	
	private SlidingMenu mMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(R.string.quick_tiles);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//TODO: get rid of this
		CustomIconUtil.setContext(this);

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

}