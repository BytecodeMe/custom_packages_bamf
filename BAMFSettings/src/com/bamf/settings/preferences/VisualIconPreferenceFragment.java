package com.bamf.settings.preferences;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.bamf.settings.R;
import com.bamf.settings.adapters.BatteryThemeAdapter;
import com.bamf.settings.widgets.BAMFSwitchPreference;
import com.bamf.settings.widgets.ColorPanelView;
import com.bamf.settings.widgets.ColorPickerDialog;
import com.bamf.settings.widgets.IconPreference;

public class VisualIconPreferenceFragment extends PreferenceFragment implements
		OnPreferenceChangeListener, OnPreferenceClickListener {

	private static final int TYPE_BATTERY = 0;
	private static final int TYPE_SIGNAL = 1;

	private static final String PREF_DEBUG_ICON = "pref_visual_basic_debugging";
	private static final String PREF_SHOW_CLOCK = "pref_visual_basic_clock_selector";

	private static final String PREF_CLOCK_TEXT = "pref_visual_basic_clock_text";
	private static final String PREF_SHOW_ALARM = "pref_visual_basic_alarm";
	private static final String PREF_BATTERY_STYLE = "pref_battery_style";
	private static final String PREF_SIGNAL_STYLE = "pref_signal_style";
	private static final String PREF_BATTERY_TEXT = "pref_battery_text_options";

	private static final String TAG = VisualIconPreferenceFragment.class
			.getSimpleName();

	private Activity mSettings;
	private ContentResolver mResolver;
	private PackageManager pm;
	private BAMFSwitchPreference mDebugHide;
	private ListPreference mClock;

	private BAMFSwitchPreference mAlarmHide;
	private IconPreference mBatteryStyle;
	private IconPreference mSignalStyle;
	private Preference mBatteryText;
	private Preference mClockText;

	private Drawable mBatteryIcon = null;
	private String mBatteryPackage = "";

	private Drawable mSignalIcon = null;
	private String mSignalPackage = "";

	private ArrayList<PackageInfo> mBatteryPackages;
	private ArrayList<PackageInfo> mSignalPackages;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.visual_icon_preference);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mSettings = getActivity();
		mResolver = mSettings.getContentResolver();
		pm = mSettings.getPackageManager();

		mClock = (ListPreference) findPreference(PREF_SHOW_CLOCK);
		mClock.setValue(getCurrentClockStyle());
		mClock.setSummary(mSettings.getResources().getStringArray(
				R.array.status_clock_entries)[Integer.parseInt(mClock
				.getValue())]);

		mAlarmHide = (BAMFSwitchPreference) findPreference(PREF_SHOW_ALARM);
		mAlarmHide.setChecked(Settings.System.getInt(mResolver,
				Settings.System.SHOW_STATUSBAR_ALARM, 1) == 1);

		mDebugHide = (BAMFSwitchPreference) findPreference(PREF_DEBUG_ICON);
		mDebugHide.setChecked(Settings.System.getInt(mResolver,
				Settings.System.SHOW_DEBUG_ICON, 1) == 1);

		mBatteryStyle = (IconPreference) findPreference(PREF_BATTERY_STYLE);
		mBatteryStyle.setSummary(getCurrentStyle(TYPE_BATTERY));
		mBatteryStyle.setIcon(mBatteryIcon);
		mBatteryStyle.setOnPreferenceClickListener(this);

		mSignalStyle = (IconPreference) findPreference(PREF_SIGNAL_STYLE);
		mSignalStyle.setSummary(getCurrentStyle(TYPE_SIGNAL));
		mSignalStyle.setIcon(mSignalIcon);
		mSignalStyle.setOnPreferenceClickListener(this);

		mBatteryText = findPreference(PREF_BATTERY_TEXT);
		mBatteryText.setOnPreferenceClickListener(this);

		mClockText = findPreference(PREF_CLOCK_TEXT);
		mClockText.setOnPreferenceClickListener(this);

		mDebugHide.setOnPreferenceChangeListener(this);
		mDebugHide.setOnPreferenceClickListener(this);

		mAlarmHide.setOnPreferenceChangeListener(this);
		mAlarmHide.setOnPreferenceClickListener(this);

		mClock.setOnPreferenceChangeListener(this);

	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {

		if (pref == mDebugHide) {
			int val = (Boolean) newValue ? 1 : 0;
			Settings.System.putInt(mResolver, Settings.System.SHOW_DEBUG_ICON,
					val);
			return true;
		} else if (pref == mClock) {
			switch (Integer.parseInt((String) newValue)) {
			case 0:
				Settings.System.putInt(mResolver,
						Settings.System.CENTER_STATUSBAR_CLOCK, 0);
				Settings.System.putInt(mResolver,
						Settings.System.SHOW_STATUSBAR_CLOCK, 1);
				break;
			case 1:
				Settings.System.putInt(mResolver,
						Settings.System.CENTER_STATUSBAR_CLOCK, 1);
				Settings.System.putInt(mResolver,
						Settings.System.SHOW_STATUSBAR_CLOCK, 1);
				break;
			case 2:
				Settings.System.putInt(mResolver,
						Settings.System.SHOW_STATUSBAR_CLOCK, 0);
				Settings.System.putInt(mResolver,
						Settings.System.CENTER_STATUSBAR_CLOCK, 0);
				break;
			}
			mClock.setValue(getCurrentClockStyle());
			mClock.setSummary(mSettings.getResources().getStringArray(
					R.array.status_clock_entries)[Integer.parseInt(mClock
					.getValue())]);
			return true;
		} else if (pref == mAlarmHide) {
			int val = (Boolean) newValue ? 1 : 0;
			Settings.System.putInt(mResolver,
					Settings.System.SHOW_STATUSBAR_ALARM, val);
			return true;
		} else
			return false;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {

		if (preference == mBatteryStyle) {
			showSkinDialog(TYPE_BATTERY);
		} else if (preference == mBatteryText) {
			showBatteryTextDialog();
		} else if (preference == mClockText) {
			showColorPicker(Settings.System.getInt(
					mResolver,
					Settings.System.STATUS_CLOCK_COLOR,
					mSettings.getResources().getColor(
							android.R.color.holo_blue_light)),
					R.string.title_clock_picker_dialog);
			return true;
		} else if(preference == mSignalStyle){
			showSkinDialog(TYPE_SIGNAL);
		} else if (preference instanceof TwoStatePreference)
			((TwoStatePreference) preference)
					.setChecked(!((TwoStatePreference) preference).isChecked());
		return false;
	}

	private void showBatteryTextDialog() {

		boolean isShown = Settings.System.getInt(mResolver,
				Settings.System.SHOW_BATTERY_TEXT, 0) == 1;
		int currentSize = Settings.System.getInt(mResolver,
				Settings.System.BATTERY_TEXT_SIZE, 7);

		final Switch textSwitch;
		final SeekBar sizeSeek;
		final ColorPanelView colorView;
		final ImageView imagePreview;
		final Spinner showCharge;
		final Button ok;
		final TextView textPreview;

		final String PREVIEW_TEXT = "75";

		final Dialog d = new Dialog(mSettings);
		d.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		d.setContentView(R.layout.battery_text_dialog);

		d.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.dialog_title_with_switch);
		View titleBar = d.getWindow().getDecorView();
		((TextView) titleBar.findViewById(R.id.title))
				.setText(R.string.acc_battery_text);

		textSwitch = (Switch) titleBar.findViewById(R.id.accurate_switch);
		imagePreview = (ImageView) d.findViewById(R.id.preview);
		textPreview = (TextView) d.findViewById(R.id.preview_text);
		sizeSeek = (SeekBar) d.findViewById(R.id.size_seek);
		colorView = (ColorPanelView) d.findViewById(R.id.preview_color_panel);
		showCharge = (Spinner) d.findViewById(R.id.vis_spinner);
		ok = (Button) d.findViewById(R.id.ok_button);

		colorView.setColor(Settings.System.getInt(mResolver,
				Settings.System.BATTERY_TEXT_COLOR, Color.WHITE));

		imagePreview.setImageDrawable(mBatteryIcon);

		textPreview.setText(PREVIEW_TEXT);
		textPreview
				.setTextSize(TypedValue.COMPLEX_UNIT_DIP, currentSize * 2.0f);
		textPreview.setTextColor(colorView.getColor());

		textSwitch.setChecked(isShown);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				mSettings, R.array.battery_text_values,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		showCharge.setAdapter(adapter);
		showCharge.setSelection(Settings.System.getInt(mResolver,
				Settings.System.BATTERY_TEXT_SHOW_CHARGE, 0));

		sizeSeek.setMax(9);
		sizeSeek.setProgress(currentSize);

		d.findViewById(R.id.content).setVisibility(
				textSwitch.isChecked() ? View.VISIBLE : View.GONE);
		d.findViewById(R.id.disabled).setVisibility(
				textSwitch.isChecked() ? View.GONE : View.VISIBLE);

		textSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Settings.System.putInt(mResolver,
						Settings.System.SHOW_BATTERY_TEXT, isChecked ? 1 : 0);

				d.findViewById(R.id.content).setVisibility(
						isChecked ? View.VISIBLE : View.GONE);
				d.findViewById(R.id.disabled).setVisibility(
						isChecked ? View.GONE : View.VISIBLE);
			}
		});

		showCharge.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Settings.System.putInt(mResolver,
						Settings.System.BATTERY_TEXT_SHOW_CHARGE, position);
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}

		});

		colorView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showColorPicker((ColorPanelView) v, textPreview,
						Settings.System
								.getInt(mResolver,
										Settings.System.BATTERY_TEXT_COLOR,
										Color.WHITE),
						R.string.title_color_picker_dialog);
			}
		});

		sizeSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				float size = textPreview.getTextSize();
				textPreview.setScaleX(dpToPx((progress + 0.01f) * 2) / size);
				textPreview.setScaleY(dpToPx((progress + 0.01f) * 2) / size);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}

		});

		ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Settings.System.putInt(mResolver,
						Settings.System.BATTERY_TEXT_SIZE,
						sizeSeek.getProgress());
				mSettings.sendBroadcast(new Intent()
						.setAction(Intent.ACTION_BATTERY_ICON_CHANGED));
				d.dismiss();
			}
		});

		d.show();
	}

	private float dpToPx(float dp) {
		Resources r = mSettings.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				r.getDisplayMetrics());
		return px;
	}

	private void showColorPicker(int color, final int title) {
		final ColorPickerDialog d = new ColorPickerDialog(mSettings, color);
		d.setAlphaSliderVisible(true);
		d.setTitle(title);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Settings.System.putInt(mResolver,
								Settings.System.STATUS_CLOCK_COLOR,
								d.getColor());
					}
				});

		d.setButton(DialogInterface.BUTTON_NEUTRAL, "Default",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Settings.System.putInt(
								mResolver,
								Settings.System.STATUS_CLOCK_COLOR,
								mSettings.getResources().getColor(
										android.R.color.holo_blue_light));

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

	private void showColorPicker(final ColorPanelView v, final TextView tv,
			int color, final int title) {
		final ColorPickerDialog d = new ColorPickerDialog(mSettings, color);
		d.setAlphaSliderVisible(true);
		d.setTitle(title);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO: save new color setting
						Settings.System.putInt(mResolver,
								Settings.System.BATTERY_TEXT_COLOR,
								d.getColor());
						v.setColor(d.getColor());
						tv.setTextColor(d.getColor());
					}
				});

		d.setButton(DialogInterface.BUTTON_NEUTRAL, "Default",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						Settings.System
								.putInt(mResolver,
										Settings.System.BATTERY_TEXT_COLOR,
										Color.WHITE);
						v.setColor(Color.WHITE);
						tv.setTextColor(Color.WHITE);

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

	private void showSkinDialog(final int type) {

		boolean battery = (type == TYPE_BATTERY);
		View progress;
		ListView lv;
		final Dialog d = new Dialog(mSettings);
		d.setContentView(R.layout.battery_style_dialog);
		d.setTitle(mSettings
				.getString(battery ? R.string.battery_style_dialog_title
						: R.string.signal_style_dialog_title));
		progress = d.findViewById(R.id.style_progress);
		lv = (ListView) d.findViewById(R.id.style_list);
		d.show();

		final BatteryThemeAdapter adapter = new BatteryThemeAdapter(mSettings,
				battery ? mBatteryPackages : mSignalPackages,type);
		lv.setAdapter(adapter);
		progress.setVisibility(View.GONE);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				if (position == 0)
					setDefault(type);
				else {
					setStyle(adapter.getItem(position), type);
				}
				d.cancel();
			}

		});
	}

	protected void setStyle(PackageInfo item, int type) {

		String name = "";
		Preference p = null;
		String intent = "";
		Drawable d = null;
		boolean battery = false;

		switch (type) {
		case TYPE_BATTERY:
			name = Settings.System.CUSTOM_BATTERY_PACKAGE;
			p = mBatteryStyle;
			intent = Intent.ACTION_BATTERY_ICON_CHANGED;
			d = mBatteryIcon;
			battery = true;
			break;
		case TYPE_SIGNAL:
			name = Settings.System.CUSTOM_SIGNAL_PACKAGE;
			p = mSignalStyle;
			intent = Intent.ACTION_SIGNAL_ICON_CHANGED;
			d = mSignalIcon;
			break;
		}

		if (item != null) {
			Settings.System.putString(mResolver, name, item.packageName);
			p.setSummary(getCurrentStyle(type));
			p.setIcon(battery ? mBatteryIcon : mSignalIcon);
		} else {
			setDefault(type);
		}
		mSettings.sendBroadcast(new Intent().setAction(intent));
	}

	protected void setDefault(int type) {

		String name = "";
		Preference p = null;
		String intent = "";
		Drawable d = null;
		boolean battery = false;

		switch (type) {
		case TYPE_BATTERY:
			name = Settings.System.CUSTOM_BATTERY_PACKAGE;
			p = mBatteryStyle;
			intent = Intent.ACTION_BATTERY_ICON_CHANGED;
			d = mBatteryIcon;
			battery = true;
			break;
		case TYPE_SIGNAL:
			name = Settings.System.CUSTOM_SIGNAL_PACKAGE;
			p = mSignalStyle;
			intent = Intent.ACTION_SIGNAL_ICON_CHANGED;
			d = mSignalIcon;
			break;
		}

		Settings.System.putString(mResolver, name, "");
		p.setSummary(getCurrentStyle(type));
		p.setIcon(battery ? mBatteryIcon : mSignalIcon);
		mSettings.sendBroadcast(new Intent().setAction(intent));
	}

	private CharSequence getCurrentStyle(int type) {

		String style = "Stock";

		String name = "";
		Drawable d = null;
		ArrayList<PackageInfo> packages = null;
		String pkgName = "";
		String skinType = "";

		switch (type) {
		case TYPE_BATTERY:
			name = Settings.System.CUSTOM_BATTERY_PACKAGE;			
			packages = mBatteryPackages;
			pkgName = mBatteryPackage;
			skinType = "battery";
			break;
		case TYPE_SIGNAL:
			name = Settings.System.CUSTOM_SIGNAL_PACKAGE;			
			packages = mSignalPackages;
			pkgName = mSignalPackage;
			skinType = "signal";
			break;
		}

		packages = new ArrayList<PackageInfo>();
		pkgName = Settings.System.getString(mResolver, name);
		List<PackageInfo> packs = pm.getInstalledSkinPackages();
		for (int i = 0; i < packs.size(); i++) {
			PackageInfo pi = packs.get(i);
			if ((pkgName != null) && !pkgName.isEmpty()
					&& (pi.packageName).equals(pkgName)) {
				style = pi.applicationInfo.loadLabel(pm).toString();
				d = getCurrentIcon(pi,type);
			}
			if (pi.skinType.contains(skinType)) {
				packages.add(pi);
			}
		}
		if (d == null) {
			d = getCurrentIcon(null,type);
		}

		switch (type) {
		case TYPE_BATTERY:
			mBatteryIcon = d;
			mBatteryPackages = packages;
			mBatteryPackage = pkgName;
			break;
		case TYPE_SIGNAL:
			mSignalIcon = d;
			mSignalPackages = packages;
			mSignalPackage = pkgName;
			break;
		}
		return style;
	}

	private Drawable getCurrentIcon(PackageInfo pi,int type) {

		boolean battery = (type == TYPE_BATTERY);
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
				d = res.getDrawable(res.getIdentifier(battery ? "default_preview_battery" : "default_preview_signal", "drawable",
						pi.packageName));
				} catch(Exception e){					
				}
			if (d != null) {
				return d;
			} else {
				// return the default preview if there is a problem or it
				// doesn't exist
				return mSettings.getResources().getDrawable(battery ? 
						R.drawable.default_preview_battery : R.drawable.default_preview_signal);
			}
		} else {
			// return the default preview if we aren't using a custom icon
			return mSettings.getResources().getDrawable(battery ? 
					R.drawable.default_preview_battery : R.drawable.default_preview_signal);
		}
	}

	private String getCurrentClockStyle() {
		String style = "0";
		boolean centerClock = Settings.System.getInt(
				mSettings.getContentResolver(),
				Settings.System.CENTER_STATUSBAR_CLOCK, 0) == 1;
		boolean visible = Settings.System.getInt(
				mSettings.getContentResolver(),
				Settings.System.SHOW_STATUSBAR_CLOCK, 1) == 1;
		if (centerClock)
			style = "1";
		else if (!visible)
			style = "2";
		return style;
	}
}
