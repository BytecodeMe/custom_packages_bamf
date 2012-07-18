package com.bamf.settings.adapters;

import java.util.List;

import com.bamf.settings.R;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BatteryThemeAdapter extends BaseAdapter {

	private Context mContext;
	private List<PackageInfo> mPackages;	
	private PackageManager pm;
	
	public BatteryThemeAdapter(Context context, List<PackageInfo> packages) {
		mContext = context;
		mPackages = packages;		
		pm = context.getPackageManager();
	}

	@Override
	public int getCount() {
		if(mPackages != null)
			return (mPackages.size() + 1);
		else return 1;
	}

	@Override
	public PackageInfo getItem(int position) {
		if(position == 0)
			return null;
		else return mPackages.get(position - 1);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = null;		
		ImageView mImage;
		TextView mTitle;
		
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = inflater.inflate(R.layout.battery_style_item,null);	
		
		mTitle = (TextView) v.findViewById(R.id.style_name);
		mImage = (ImageView) v.findViewById(R.id.style_preview);
		
		if(position == 0){
			mTitle.setText("Stock");
			mImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.default_preview));
		}else if(mPackages.size() > 0){
			Resources res = null;
			Drawable d = null;
			try{
				res = pm.getResourcesForApplication(mPackages.get(position -1).packageName);
			}catch (Exception e){
				
			}
			mTitle.setText(mPackages.get(position-1).applicationInfo.loadLabel(pm).toString());
			if(res != null)
				d = res.getDrawable(res.getIdentifier("preview", "drawable", mPackages.get(position -1).packageName));
			if(d != null){
				mImage.setImageDrawable(d);
			}
		}
		return v;
	}

}
