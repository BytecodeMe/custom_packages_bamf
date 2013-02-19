package com.bamf.settings.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bamf.settings.R;
import com.bamf.settings.utils.CustomIconUtil;
import com.bamf.settings.utils.QuickTileHelper;
import com.bamf.settings.utils.QuickTileHelper.QuickSettingInfo;

public class QuickTilePicker extends AlertDialog implements OnItemClickListener {
	
	public interface OnItemSelectListener {

        /**
         * @param name The name of the tile setting
         */
        void onItemSelect(String name);
    }
	
	private static final String TAG = QuickTilePicker.class.getSimpleName();

	private Context mContext;
	private Resources mSystemUIResources;
	private List<QuickSettingInfo> mItems;
	private OnItemSelectListener mCallback;
	private PackageManager mPm;
            
	public QuickTilePicker(Context context, OnItemSelectListener callback) {
		this(context, 0, callback);
	}
	
	public QuickTilePicker(Context context, int theme, OnItemSelectListener callback) {
		super(context, theme);
		mContext = context;
		mCallback = callback;
		mPm = context.getPackageManager();
		mItems = new ArrayList<QuickSettingInfo>();
		try {
			mSystemUIResources = mPm.getResourcesForApplication("com.android.systemui");
		} catch(Exception e) {
			mSystemUIResources = null;
			Log.e(TAG, "Could not load SystemUI resources", e);
		}
        
	}
	
	public void addItem(QuickSettingInfo item){
		mItems.add(item);
	}
	
	public void setItems(List<QuickSettingInfo> items){
		mItems = items;
	}
	
	public int getItemCount(){
		return mItems.size();
	}
	
	@Override
	public void show(){
		final Resources res = mContext.getResources();
		Comparator<QuickSettingInfo> comparator = new Comparator<QuickSettingInfo>() {    
            @Override
            public int compare(QuickSettingInfo lhs, QuickSettingInfo rhs) {
                return res.getString(lhs.getTitleResId()).compareToIgnoreCase(res.getString(rhs.getTitleResId()));
            }
		};      
		Collections.sort(mItems, comparator);
		
		final ListAdapter adapter = new ArrayAdapter<QuickSettingInfo>(
	            mContext,
	            R.layout.select_icon_source_item,
	            R.id.select_photo_which,
	            mItems){
	                public View getView(int position, View convertView, ViewGroup parent) {
	                    //User super class to create the View
	                	View v = convertView;
	                	if(v==null){
		                    v = super.getView(position, convertView, parent);
	                	}
	                    TextView tv = (TextView)v.findViewById(R.id.select_photo_which);
	                    ImageView iv = (ImageView)v.findViewById(R.id.select_photo_icon);
	                    iv.setScaleX(0.75f);
	                    iv.setScaleY(0.75f);
	                    if(mItems.get(position).getId().equals(Settings.System.QUICK_CUSTOM)){
	                    	tv.setText(getCustomText());
	                    	iv.setBackground(getCustomDrawable());
	                    }else{
	                    	tv.setText(mItems.get(position).getTitleResId());
	                    	iv.setBackground(getIconDrawable(mItems.get(position).getIcon()));
	                    }
	                    
	                    return v;
	                }
	            };
	            
		ListView view = new ListView(mContext);
		view.setAdapter(adapter);
		view.setOnItemClickListener(this);
		setView(view);
		
		super.show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(mCallback!=null){
        	mCallback.onItemSelect(mItems.get(position).getId());
        }
		dismiss();
	}
	
	private Drawable getIconDrawable(String id) {
    	Drawable icon = null;
    	try {
		    if(mSystemUIResources.getIdentifier(id, null, null) > 0){
		    		icon = mSystemUIResources.getDrawable(mSystemUIResources.getIdentifier(id, null, null));
		    }
		    
		} catch (Throwable t) {	
				Log.e(TAG, "Could not load icon for ["+id+"]", t);
		}
    	
    	if(icon == null){
            icon = mContext.getResources().getDrawable(android.R.drawable.sym_def_app_icon);
        }
		return icon;
	}
	
	private String getCustomText() {
		String mCustomURI = Settings.System.getString(mContext.getContentResolver(), 
				Settings.System.QUICK_SETTINGS_CUSTOM);
		String text = "";
        try {
        	text = "Custom ("+mPm.resolveActivity(
        			Intent.parseUri(mCustomURI, 0),0).activityInfo.loadLabel(mPm)+")";
		} catch (Exception e) {
			return "Custom";
		}
        
        return text;
	}
	
	private Drawable getCustomDrawable() {
		String mCustomURI = Settings.System.getString(mContext.getContentResolver(), 
				Settings.System.QUICK_SETTINGS_CUSTOM);
		Drawable customIcon = null;
        try {
        	customIcon = CustomIconUtil.getInstance(mContext).loadFromFile();
        	if(customIcon==null){
        		customIcon = mPm.getActivityIcon(Intent.parseUri(mCustomURI, 0));
        	}
		} catch (Exception e) {
			e.printStackTrace();
			return getIconDrawable("");
		}
        
        return customIcon;
	}
	
	public static class Item{
        public final int text;
        public final String setting;
        public final String icon;
        public Item(String setting, int text, String icon) {
            this.text = text;
            this.setting = setting;
            this.icon = icon;
        }
        @Override
        public String toString() {
            return setting;
        }
    }
}
