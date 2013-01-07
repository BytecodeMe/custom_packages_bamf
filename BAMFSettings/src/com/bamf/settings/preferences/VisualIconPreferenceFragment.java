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

public class VisualIconPreferenceFragment extends PreferenceFragment implements OnPreferenceChangeListener,OnPreferenceClickListener {
	
	private static final String PREF_DEBUG_ICON = "pref_visual_basic_debugging";
	private static final String PREF_SHOW_CLOCK = "pref_visual_basic_clock_selector";
	private static final String PREF_SHOW_ALARM = "pref_visual_basic_alarm";
	private static final String PREF_BATTERY_STYLE = "pref_battery_style";	
	private static final String PREF_BATTERY_TEXT = "pref_battery_text_options";
		
	private static final String TAG = VisualIconPreferenceFragment.class.getSimpleName();
	
	private Activity mSettings;
	private ContentResolver mResolver;	
	private PackageManager pm;
	private BAMFSwitchPreference mDebugHide;
	private ListPreference mClock;
	private BAMFSwitchPreference mAlarmHide;	
	private IconPreference mBatteryStyle;	
	private Preference mBatteryText;
	
	private Drawable mBatteryIcon = null;
	private String mBatteryPackage = "";
	private ArrayList<PackageInfo> mPackages;
	
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
    	mClock.setSummary(mSettings.getResources().getStringArray(R.array.status_clock_entries)[Integer.parseInt(mClock.getValue())]);
    	
    	mAlarmHide = (BAMFSwitchPreference) findPreference(PREF_SHOW_ALARM);
    	mAlarmHide.setChecked(Settings.System.getInt(mResolver, Settings.System.SHOW_STATUSBAR_ALARM,1) == 1);  
    	
    	mDebugHide = (BAMFSwitchPreference) findPreference(PREF_DEBUG_ICON);    	
    	mDebugHide.setChecked(Settings.System.getInt(mResolver,Settings.System.SHOW_DEBUG_ICON,1) == 1);  
    	
    	mBatteryStyle = (IconPreference) findPreference(PREF_BATTERY_STYLE);
    	mBatteryStyle.setSummary(getCurrentBatteryStyle());
    	mBatteryStyle.setIcon(mBatteryIcon);
    	mBatteryStyle.setOnPreferenceClickListener(this);
    	
    	mBatteryText = findPreference(PREF_BATTERY_TEXT);
    	mBatteryText.setOnPreferenceClickListener(this);
    	
    	mDebugHide.setOnPreferenceChangeListener(this);
    	mDebugHide.setOnPreferenceClickListener(this);
    	
    	mAlarmHide.setOnPreferenceChangeListener(this);
    	mAlarmHide.setOnPreferenceClickListener(this);
    	
    	mClock.setOnPreferenceChangeListener(this);    	
    }		

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		
		if(pref == mDebugHide){
			int val = (Boolean) newValue? 1 : 0;
			Settings.System.putInt(mResolver,Settings.System.SHOW_DEBUG_ICON,val);
			return true;
		}else if(pref == mClock){
			switch(Integer.parseInt((String) newValue)){
				case 0:
					Settings.System.putInt(mResolver,Settings.System.CENTER_STATUSBAR_CLOCK,0);
					Settings.System.putInt(mResolver,Settings.System.SHOW_STATUSBAR_CLOCK,1);					
					break;
				case 1:					
					Settings.System.putInt(mResolver,Settings.System.CENTER_STATUSBAR_CLOCK,1);
					Settings.System.putInt(mResolver,Settings.System.SHOW_STATUSBAR_CLOCK,1);
					break;
				case 2:
					Settings.System.putInt(mResolver,Settings.System.SHOW_STATUSBAR_CLOCK,0);
					Settings.System.putInt(mResolver,Settings.System.CENTER_STATUSBAR_CLOCK,0);
					break;
			}
			mClock.setValue(getCurrentClockStyle());
			mClock.setSummary(mSettings.getResources().getStringArray(R.array.status_clock_entries)[Integer.parseInt(mClock.getValue())]);
			return true;
		}else if(pref == mAlarmHide){
			int val = (Boolean) newValue? 1 : 0;
			Settings.System.putInt(mResolver,Settings.System.SHOW_STATUSBAR_ALARM,val);
			return true;
		}else
			return false;
	}	
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		
		if(preference == mBatteryStyle){
			showBatteryDialog();
		}else if(preference == mBatteryText){
			showBatteryTextDialog();
		}else if(preference instanceof TwoStatePreference)
			((TwoStatePreference) preference).setChecked(!((TwoStatePreference) preference).isChecked());		
		return false;
	}

	private void showBatteryTextDialog() {
		
		boolean isShown = Settings.System.getInt(mResolver,Settings.System.SHOW_BATTERY_TEXT,0) == 1;
		int currentSize = Settings.System.getInt(mResolver,Settings.System.BATTERY_TEXT_SIZE,7);
		
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
		
		d.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.dialog_title_with_switch);
		View titleBar = d.getWindow().getDecorView();
		((TextView)titleBar.findViewById(R.id.title)).setText(R.string.acc_battery_text);
		

		textSwitch = (Switch)titleBar.findViewById(R.id.accurate_switch);
		imagePreview = (ImageView)d.findViewById(R.id.preview);
		textPreview = (TextView)d.findViewById(R.id.preview_text);
		sizeSeek = (SeekBar)d.findViewById(R.id.size_seek);
		colorView = (ColorPanelView)d.findViewById(R.id.preview_color_panel);		
		showCharge = (Spinner)d.findViewById(R.id.vis_spinner);
		ok = (Button)d.findViewById(R.id.ok_button);
		
		colorView.setColor(Settings.System.getInt(mResolver, Settings.System.BATTERY_TEXT_COLOR, Color.WHITE));
		
		imagePreview.setImageDrawable(mBatteryIcon);
		
		textPreview.setText(PREVIEW_TEXT);
		textPreview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, currentSize * 2.0f);
		textPreview.setTextColor(colorView.getColor());
		
		textSwitch.setChecked(isShown);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            mSettings, R.array.battery_text_values, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    showCharge.setAdapter(adapter);
		showCharge.setSelection(Settings.System.getInt(mResolver,Settings.System.BATTERY_TEXT_SHOW_CHARGE,0));
		
		sizeSeek.setMax(9);
		sizeSeek.setProgress(currentSize);
		
		d.findViewById(R.id.content).setVisibility(textSwitch.isChecked() ? View.VISIBLE:View.GONE);
		d.findViewById(R.id.disabled).setVisibility(textSwitch.isChecked() ? View.GONE:View.VISIBLE);
		
		textSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {				
				Settings.System.putInt(mResolver,Settings.System.SHOW_BATTERY_TEXT,isChecked ? 1:0);
				
				d.findViewById(R.id.content).setVisibility(isChecked ? View.VISIBLE:View.GONE);
				d.findViewById(R.id.disabled).setVisibility(isChecked ? View.GONE:View.VISIBLE);
			}			
		});
		
		showCharge.setOnItemSelectedListener(new OnItemSelectedListener(){

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Settings.System.putInt(mResolver,Settings.System.BATTERY_TEXT_SHOW_CHARGE,position);				
			}
			public void onNothingSelected(AdapterView<?> parent) {		
			}
						
		});
		
		colorView.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				showColorPicker((ColorPanelView) v,
						textPreview,
						Settings.System.getInt(mResolver, Settings.System.BATTERY_TEXT_COLOR, Color.WHITE),
		        		R.string.title_color_picker_dialog);				
			}			
		});
		
		sizeSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {				
				
				float size = textPreview.getTextSize();
				textPreview.setScaleX(dpToPx((progress+0.01f)*2)/size);
				textPreview.setScaleY(dpToPx((progress+0.01f)*2)/size);
			}
			public void onStartTrackingTouch(SeekBar seekBar) {		
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
		});
		
		ok.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Settings.System.putInt(mResolver, Settings.System.BATTERY_TEXT_SIZE, sizeSeek.getProgress());
				mSettings.sendBroadcast(new Intent().setAction(Intent.ACTION_BATTERY_ICON_CHANGED));
				d.dismiss();
			}			
		});
		
		d.show();
	}
	
	private float dpToPx(float dp) {
		Resources r = mSettings.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
		return px;
	}
	
	private void showColorPicker(final ColorPanelView v,final TextView tv,int color, final int title){
        final ColorPickerDialog d = new ColorPickerDialog(mSettings, color);
        d.setAlphaSliderVisible(true);
        d.setTitle(title);
        d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: save new color setting
            	Settings.System.putInt(mResolver, Settings.System.BATTERY_TEXT_COLOR, d.getColor());   
            	v.setColor(d.getColor());
            	tv.setTextColor(d.getColor());
            }
        });
        
        d.setButton(DialogInterface.BUTTON_NEUTRAL, "Default", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            	
            	Settings.System.putInt(mResolver, Settings.System.BATTERY_TEXT_COLOR, Color.WHITE);  
            	v.setColor(Color.WHITE);
            	tv.setTextColor(Color.WHITE);
            	
            }
        });

        d.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                d.dismiss();
            }
        });
        
        d.show();

    }

	private void showBatteryDialog() {		
		
		View progress;
		ListView lv;
		final Dialog d = new Dialog(mSettings);
		d.setContentView(R.layout.battery_style_dialog);
		d.setTitle(mSettings.getString(R.string.battery_style_dialog_title));
		progress = d.findViewById(R.id.style_progress);
		lv = (ListView)d.findViewById(R.id.style_list);
		d.show();
		
		final BatteryThemeAdapter adapter = new BatteryThemeAdapter(mSettings,mPackages);
		lv.setAdapter(adapter);
		progress.setVisibility(View.GONE);
		lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				mBatteryIcon = null;
				if(position == 0)
					setDefault();
				else{
					setBatteryStyle(adapter.getItem(position));
				}
				d.cancel();
			}
			
		});
	}	
	
	protected void setBatteryStyle(PackageInfo item) {
		
		if(item != null){
			Settings.System.putString(mResolver, Settings.System.CUSTOM_BATTERY_PACKAGE, item.packageName);
			mBatteryStyle.setSummary(getCurrentBatteryStyle());
			mBatteryStyle.setIcon(mBatteryIcon);
		}else{
			setDefault();
		}	
		mSettings.sendBroadcast(new Intent().setAction(Intent.ACTION_BATTERY_ICON_CHANGED));
	}

	protected void setDefault() {
		
		Settings.System.putString(mResolver, Settings.System.CUSTOM_BATTERY_PACKAGE, "");
		mBatteryStyle.setSummary(getCurrentBatteryStyle());
		mBatteryStyle.setIcon(mBatteryIcon);
		mSettings.sendBroadcast(new Intent().setAction(Intent.ACTION_BATTERY_ICON_CHANGED));
	}

	private CharSequence getCurrentBatteryStyle() {
		String style = "Stock";		
		mPackages = new ArrayList<PackageInfo>();
		mBatteryPackage = Settings.System.getString(mResolver, Settings.System.CUSTOM_BATTERY_PACKAGE);		
		List<PackageInfo> packs = pm.getInstalledSkinPackages();		
		for(int i = 0;i < packs.size();i++){
			PackageInfo pi = packs.get(i);
			if((mBatteryPackage != null) && !mBatteryPackage.isEmpty() && (pi.packageName).equals(mBatteryPackage)){
				style = pi.applicationInfo.loadLabel(pm).toString();
				mBatteryIcon = getCurrentBatteryIcon(pi);
			}		
			if(pi.skinType.equalsIgnoreCase("battery") || pi.skinType.equalsIgnoreCase("full")){				
				mPackages.add(pi);
			}
		}	
		if(mBatteryIcon == null){
			mBatteryIcon = getCurrentBatteryIcon(null);
		}
		return style;
	}
	
	private Drawable getCurrentBatteryIcon(PackageInfo pi) {		
		
		Resources res = null;
		Drawable d = null;
		if(pi != null){
			try{
				res = pm.getResourcesForApplication(pi.packageName);
			}catch (Exception e){
			
			}		
			if(res != null)
				//get the preview from our battery package
				d = res.getDrawable(res.getIdentifier("preview", "drawable", pi.packageName));
			if(d != null){
				return d;
			}else{
				//return the default preview if there is a problem or it doesn't exist
				return mSettings.getResources().getDrawable(R.drawable.default_preview);
			}
		}else{
			//return the default preview if we aren't using a custom icon
			return mSettings.getResources().getDrawable(R.drawable.default_preview);
		}
	}
	private String getCurrentClockStyle() {
		String style = "0";
		boolean centerClock = Settings.System.getInt(mSettings.getContentResolver(), Settings.System.CENTER_STATUSBAR_CLOCK, 0) ==1;
		boolean visible = Settings.System.getInt(mSettings.getContentResolver(), Settings.System.SHOW_STATUSBAR_CLOCK, 1) ==1;
		if(centerClock)
			style = "1";
		else if(!visible)
			style = "2";
		return style;
	}
}
