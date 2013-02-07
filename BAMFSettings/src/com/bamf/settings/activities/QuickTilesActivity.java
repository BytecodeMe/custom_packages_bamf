package com.bamf.settings.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.bamf.settings.R;
import com.bamf.settings.preferences.QuickTileOrderFragment;
import com.bamf.settings.preferences.VisualLockscreenFragment;
import com.bamf.settings.utils.QuickTileHelper.CustomIconUtil;
import com.slidingmenu.lib.SlidingMenu;


public class QuickTilesActivity extends FragmentActivity /*BaseSlidingActivity*/ {

	/*public QuickTilesActivity() {
		super(R.string.quick_tiles);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSlidingMenu().setMode(SlidingMenu.LEFT);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		
		setContentView(R.layout.content_frame);
		getFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, new QuickLaunchFragment())
		.commit();
		
		getSlidingMenu().setSecondaryMenu(R.layout.menu_frame);
		getSlidingMenu().setSecondaryShadowDrawable(R.drawable.shadowright);
		getFragmentManager()
		.beginTransaction()
		.replace(R.id.menu_frame, new VisualLockscreenFragment())
		.commit();
	}*/
	
	private SlidingMenu menu;

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
		menu = new SlidingMenu(this);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setBehindScrollScale(0);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		menu.setMenu(R.layout.menu_frame);
		getFragmentManager()
		.beginTransaction()
		.replace(R.id.menu_frame, new VisualLockscreenFragment())
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
		if (menu.isMenuShowing()) {
			menu.showContent();
		} else {
			super.onBackPressed();
		}
	}

}