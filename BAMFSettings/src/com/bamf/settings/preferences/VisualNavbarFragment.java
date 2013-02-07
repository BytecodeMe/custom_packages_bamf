package com.bamf.settings.preferences;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.bamf.settings.R;
import com.bamf.settings.activities.SettingsActivity;
import com.bamf.settings.adapters.BatteryThemeAdapter;
import com.bamf.settings.widgets.BAMFPreference;
import com.bamf.settings.widgets.BAMFSetupPreference;
import com.bamf.settings.widgets.BAMFSwitchPreference;
import com.bamf.settings.widgets.ColorPickerDialog;
import com.bamf.settings.widgets.IconPreference;

public class VisualNavbarFragment extends PreferenceFragment implements
		OnPreferenceChangeListener, OnPreferenceClickListener {

	private static final String PREF_NAVBAR_REFLECT = "pref_visual_navbar_reflect";
	private static final String PREF_NAVBAR_FORCE = "pref_visual_navbar_force";
	private static final String PREF_NAVBAR_SETUP = "pref_visual_navbar_setup";
	private static final String PREF_NAVBAR_STYLE = "pref_navbar_style";
	private static final String PREF_COLOR_PICKER = "pref_color_picker";
	private static final String PREF_GLOW_PICKER = "pref_glow_picker";	

	private static final int GLOW_COLOR_DEFAULT = 0x7d00c3ff;

	private SettingsActivity mSettings;
	private ContentResolver mResolver;

	private BAMFSwitchPreference mNavbarReflect;
	private BAMFSwitchPreference mForceNavbar;
	private BAMFSetupPreference mNavbarSetup;
	private IconPreference mNavbarStyle;
	private BAMFPreference mColorPickerPref;
	private Preference mGlowPickerPref;

	private Drawable mNavbarIcon = null;
	private String mNavbarPackage = "";
	private ArrayList<PackageInfo> mNavbarPackages;

	private PackageManager pm;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.visual_navbar);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mSettings = (SettingsActivity) getActivity();
		mResolver = mSettings.getContentResolver();
		pm = mSettings.getPackageManager();
		
		final boolean forceNav = mSettings.getResources().getBoolean(com.android.internal.R.bool.config_canForceNavigationBar);	
		
		mForceNavbar = (BAMFSwitchPreference) findPreference(PREF_NAVBAR_FORCE);
		mForceNavbar.setChecked(Settings.System.getInt(mResolver,
				Settings.System.FORCE_ONSCREEN_NAVBAR, 0) == 1);
		mForceNavbar.setOnPreferenceChangeListener(this);
		mForceNavbar.setOnPreferenceClickListener(this);

		mNavbarReflect = (BAMFSwitchPreference) findPreference(PREF_NAVBAR_REFLECT);		
		mNavbarReflect.setChecked(Settings.System.getInt(mResolver,
				Settings.System.SHOW_NAVBAR_REFLECTION, 0) != 0);
		mNavbarReflect.setOnPreferenceChangeListener(this);
		mNavbarReflect.setOnPreferenceClickListener(this);

		mNavbarSetup = (BAMFSetupPreference) findPreference(PREF_NAVBAR_SETUP);
		mNavbarSetup.setOnPreferenceClickListener(this);

		mColorPickerPref = (BAMFPreference) findPreference(PREF_COLOR_PICKER);
		mGlowPickerPref = (Preference) findPreference(PREF_GLOW_PICKER);

		mNavbarStyle = (IconPreference) findPreference(PREF_NAVBAR_STYLE);
		mNavbarStyle.setSummary(getCurrentStyle());
		mNavbarStyle.setIcon(mNavbarIcon);
		mNavbarStyle.setOnPreferenceClickListener(this);
		
		if(!forceNav){
			getPreferenceScreen().removePreference(mForceNavbar);
		}else{
			mNavbarReflect.setDependency(PREF_NAVBAR_FORCE);
			mNavbarSetup.setDependency(PREF_NAVBAR_FORCE);
			mColorPickerPref.setDependency(PREF_NAVBAR_FORCE);
			mGlowPickerPref.setDependency(PREF_NAVBAR_FORCE);
			mNavbarStyle.setDependency(PREF_NAVBAR_FORCE);
		}

		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView listView = (ListView) parent;
				ListAdapter listAdapter = listView.getAdapter();
				Object obj = listAdapter.getItem(position);
				if (obj != null && obj instanceof View.OnLongClickListener) {
					View.OnLongClickListener longListener = (View.OnLongClickListener) obj;
					return longListener.onLongClick(view);
				}
				return false;
			}
		});
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		if (preference == mNavbarSetup) {
			Intent i = new Intent("com.bamf.settings.visualnavbarsetup");
			startActivity(i);
			return true;
		} else if (preference == mColorPickerPref) {
			showColorPicker(Settings.System.getInt(mResolver,
					Settings.System.NAVBAR_BUTTON_COLOR, Color.WHITE),
					R.string.title_color_picker_dialog);
			return true;
		} else if (preference == mGlowPickerPref) {
			// don't default this and the buttons to the same color
			showColorPicker(Settings.System.getInt(mResolver,
					Settings.System.NAVBAR_GLOW_COLOR, GLOW_COLOR_DEFAULT),
					R.string.title_glow_picker_dialog);
			return true;
		}

		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {

		if (pref == mNavbarReflect) {
			Settings.System.putInt(mResolver,
					Settings.System.SHOW_NAVBAR_REFLECTION,
					(Boolean) newValue ? 1 : 0);
			return true;
		} else if(pref == mForceNavbar){
			boolean enabled = (Boolean) newValue;
			Settings.System.putInt(mResolver, Settings.System.FORCE_ONSCREEN_NAVBAR, enabled ? 1:0);
			Intent restart = new Intent("com.android.settings.RESTART_SYSTEMUI");
            // protect this broadcast in case someone is looking
            mSettings.sendBroadcast(restart, "com.bamf.ics.permission.RESTART_SYSTEMUI");
			return true;
		} else {
			return false;
		}

	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference instanceof TwoStatePreference) {
			((TwoStatePreference) preference)
					.setChecked(!((TwoStatePreference) preference).isChecked());
		} else if (preference == mNavbarStyle) {
			showSkinDialog();
		}
		return false;
	}

	private void showColorPicker(int color, final int title) {
		final ColorPickerDialog d = new ColorPickerDialog(mSettings, color);
		d.setAlphaSliderVisible(true);
		d.setTitle(title);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO: save new color setting
						if (title == R.string.title_color_picker_dialog) {
							Settings.System.putInt(mResolver,
									Settings.System.NAVBAR_BUTTON_COLOR,
									d.getColor());
						} else {
							Settings.System.putInt(mResolver,
									Settings.System.NAVBAR_GLOW_COLOR,
									d.getColor());
						}
					}
				});

		d.setButton(DialogInterface.BUTTON_NEUTRAL, "Default",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (title == R.string.title_color_picker_dialog) {
							Settings.System.putInt(mResolver,
									Settings.System.NAVBAR_BUTTON_COLOR,
									Color.WHITE);
						} else {
							Settings.System.putInt(mResolver,
									Settings.System.NAVBAR_GLOW_COLOR,
									GLOW_COLOR_DEFAULT);
						}
					}
				});

		d.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						d.dismiss();
					}
				});

		d.show();

	}

	private void showSkinDialog() {
		
		View progress;
		ListView lv;
		final Dialog d = new Dialog(mSettings);
		d.setContentView(R.layout.battery_style_dialog);
		d.setTitle(mSettings.getString(R.string.navbar_style_dialog_title));
		progress = d.findViewById(R.id.style_progress);
		lv = (ListView) d.findViewById(R.id.style_list);
		d.show();

		final BatteryThemeAdapter adapter = new BatteryThemeAdapter(mSettings,mNavbarPackages,2);
		lv.setAdapter(adapter);
		progress.setVisibility(View.GONE);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				if (position == 0)
					setDefault();
				else {
					setStyle(adapter.getItem(position));
				}
				d.cancel();
			}

		});
	}
	
	protected void setStyle(PackageInfo item) {

		if (item != null) {
			Settings.System.putString(mResolver, Settings.System.CUSTOM_NAVBAR_PACKAGE, item.packageName);
			mNavbarStyle.setSummary(getCurrentStyle());
			mNavbarStyle.setIcon(mNavbarIcon);
		} else {
			setDefault();
		}
		mSettings.sendBroadcast(new Intent().setAction(Intent.ACTION_NAVBAR_ICON_CHANGED));
	}

	protected void setDefault() {		

		Settings.System.putString(mResolver, Settings.System.CUSTOM_NAVBAR_PACKAGE, "");
		mNavbarStyle.setSummary(getCurrentStyle());
		mNavbarStyle.setIcon(mNavbarIcon);
		mSettings.sendBroadcast(new Intent().setAction(Intent.ACTION_NAVBAR_ICON_CHANGED));
	}

	private CharSequence getCurrentStyle() {

		String style = "Stock";

		mNavbarPackages = new ArrayList<PackageInfo>();
		mNavbarPackage = Settings.System.getString(mResolver,
				Settings.System.CUSTOM_NAVBAR_PACKAGE);
		List<PackageInfo> packs = pm.getInstalledSkinPackages();
		for (int i = 0; i < packs.size(); i++) {
			PackageInfo pi = packs.get(i);
			if ((mNavbarPackage != null) && !mNavbarPackage.isEmpty()
					&& (pi.packageName).equals(mNavbarPackage)) {
				style = pi.applicationInfo.loadLabel(pm).toString();
				mNavbarIcon = getCurrentIcon(pi);
			}
			if (pi.skinType.contains("navbar")) {
				mNavbarPackages.add(pi);
			}
		}
		if (mNavbarIcon == null) {
			mNavbarIcon = getCurrentIcon(null);
		}

		return style;
	}
	
	private Drawable getCurrentIcon(PackageInfo pi) {

		Resources res = null;
		Drawable d = null;
		if (pi != null) {
			try {
				res = pm.getResourcesForApplication(pi.packageName);
			} catch (Exception e) {

			}
			if (res != null)
				try{
				// get the preview from our battery package
					d = res.getDrawable(res.getIdentifier("default_preview_navbar", "drawable",
						pi.packageName));
				}catch(Exception e){					
				}
			if (d != null) {
				return d;
			} else {
				// return the default preview if there is a problem or it
				// doesn't exist
				return mSettings.getResources().getDrawable(
						R.drawable.default_preview_navbar);
			}
		} else {
			// return the default preview if we aren't using a custom icon
			return mSettings.getResources().getDrawable(
					R.drawable.default_preview_navbar);
		}
	}

}
