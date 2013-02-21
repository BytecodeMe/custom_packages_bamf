package com.bamf.settings.adapters;

import java.util.ArrayList;

import com.bamf.settings.R;
import com.bamf.settings.preferences.SettingsListFragment;
import com.bamf.settings.preferences.FragmentBase;
import com.bamf.settings.widgets.compatibility.FragmentPagerAdapter;

import android.app.Fragment;
import android.app.FragmentManager;

public class SettingsPagerAdapter extends FragmentPagerAdapter {
	
	private ArrayList<Fragment> mFragments;

	public SettingsPagerAdapter(FragmentManager fm) {
		super(fm);
		mFragments = new ArrayList<Fragment>();
		
		mFragments.add(new FragmentBase(R.layout.performance_settings_kernel));
		if(SettingsListFragment.hasVoltageOptions()){
			mFragments.add(new FragmentBase(R.layout.performance_settings_voltage));
		}
		mFragments.add(new FragmentBase(R.layout.system_settings_basic));
		mFragments.add(new FragmentBase(R.layout.system_settings_notification));
		mFragments.add(new FragmentBase(R.layout.visual_settings_basic));
		mFragments.add(new FragmentBase(R.layout.visual_settings_sysui));
		mFragments.add(new FragmentBase(R.layout.visual_settings_navbar));
		
	}

	@Override
	public Fragment getItem(int position) {
		return mFragments.get(position);
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}
	
	@Override
	public CharSequence getPageTitle(int position){
		final FragmentBase frag = (FragmentBase) mFragments.get(position);
		
		if(frag != null){
			int id = frag.getLayoutId();
			switch(id){
			case R.layout.performance_settings_kernel:
				return "Kernel";
			case R.layout.performance_settings_voltage:
				return "Voltage";
			case R.layout.system_settings_basic:
				return "System";
			case R.layout.system_settings_notification:
				return "Notifications";
			case R.layout.visual_settings_basic:
				return "Visual";
			case R.layout.visual_settings_sysui:
				return "Status Bar";
			case R.layout.visual_settings_navbar:
				return "Navbar";
			default:
				return "";	
			}
		}
		return "";
		 
	}

}
