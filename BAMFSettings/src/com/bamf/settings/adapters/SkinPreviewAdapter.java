package com.bamf.settings.adapters;

import java.util.ArrayList;

import com.bamf.settings.R;
import com.bamf.settings.widgets.CoverFlow;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class SkinPreviewAdapter extends BaseAdapter {

	private ArrayList<PackageInfo> mPackages;
	private Context mContext;	
	
	public SkinPreviewAdapter(Context context,ArrayList<PackageInfo> packages) {
		
		mContext = context;
		mPackages = packages;
		
	}

	@Override
	public int getCount() {		
		return mPackages.size();
	}

	@Override
	public Object getItem(int position) {		
		return position;
	}

	@Override
	public long getItemId(int position) {		
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ImageView i = new ImageView(mContext);
		
		if(position == 0){			
			i.setImageDrawable(mContext.getResources().getDrawable(R.drawable.skin_preview));
		}else if(mPackages.size() > 0){
			Resources res = null;
			Drawable d = null;
			try{
				res = mContext.getPackageManager().getResourcesForApplication(mPackages.get(position).packageName);
			}catch (Exception e){
				
			}			
			if(res != null)
				d = res.getDrawable(res.getIdentifier("skin_preview", "drawable", mPackages.get(position).packageName));
			if(d != null){
				i.setImageDrawable(d);
			}else{
				i.setImageDrawable(mContext.getResources().getDrawable(R.drawable.skin_preview));
			}
		}		
		
        i.setLayoutParams(new CoverFlow.LayoutParams(380, 760));
        i.setScaleType(ImageView.ScaleType.CENTER_INSIDE); 
        
        //Make sure we set anti-aliasing otherwise we get jaggies
        BitmapDrawable drawable = (BitmapDrawable) i.getDrawable();
        drawable.setAntiAlias(true);
        return i;   
     
    }
  
	/** Returns the size (0.0f to 1.0f) of the views 
     * depending on the 'offset' to the center. */ 
     public float getScale(boolean focused, int offset) { 
       /* Formula: 1 / (2 ^ offset) */ 
         return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset))); 
     } 

}
