package com.bamf.settings.activities;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListFragment;
import android.graphics.Canvas;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MenuItem;

import com.bamf.settings.R;
import com.bamf.settings.adapters.SettingsPagerAdapter;
import com.bamf.settings.preferences.SettingsListFragment;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.CanvasTransformer;

public class BaseSettingsActivity extends Activity {

	private int mTitleRes;
	protected ListFragment mFrag;
	private SlidingMenu mMenu;
	private ViewPager mPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("BAMFSettings");

		getActionBar().setDisplayHomeAsUpEnabled(true);

		mMenu = new SlidingMenu(this);

		// set the Above View
		setContentView(R.layout.pager_content_frame);

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setId("VP".hashCode());
		mPager.setAdapter(new SettingsPagerAdapter(getFragmentManager()));

		mPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int position) {
				switch (position) {
				case 0:
					mMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
					break;
				default:
					mMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
					break;
				}
			}

		});

		mPager.setCurrentItem(0);

		// configure the SlidingMenu

		// mMenu.setOnClosedListener(this);
		// mMenu.setOnOpenedListener(this);
		mMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mMenu.setShadowWidthRes(R.dimen.shadow_width);
		mMenu.setShadowDrawable(R.drawable.shadow);
		mMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mMenu.setBehindScrollScale(0);
		mMenu.setFadeDegree(0.35f);
		mMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		mMenu.setMenu(R.layout.menu_frame);
		
		getFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new SettingsListFragment())
				.commit();
		
		PagerTabStrip tabs = (PagerTabStrip) findViewById(R.id.tabs);
        tabs.setTabIndicatorColorResource(android.R.color.holo_blue_light);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mMenu.toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void switchContent(int position) {
		mPager.setCurrentItem(position, true);
		mMenu.showContent();
	}

}
