package com.bamf.settings.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.bamf.settings.R;
import com.bamf.settings.widgets.ColorPanelView;

public class AppAdapter extends BaseAdapter implements SectionIndexer {
	
	Context mContext;
	int mLayout;
	List<? extends PackageDescription> mResults;
	
	String[] mSections;
    HashMap<String, Integer> mAlphaIndexer;

	public AppAdapter(Context context,
	       List<? extends PackageDescription> appResult, int appLayout) {
		
		mContext = context;
		mLayout = appLayout;
		mResults = appResult;
		
		// added for fastscroll alpha thumb
		mAlphaIndexer = new HashMap<String, Integer>();
		
		int size = mResults.size();
        for (int i = size - 1; i >= 0; i--) {
                String name = mResults.get(i).getLabel();
                mAlphaIndexer.put(name.substring(0, 1).toUpperCase(), i); 
        }
        
        Set<String> keys = mAlphaIndexer.keySet();
        Iterator<String> it = keys.iterator();
        ArrayList<String> keyList = new ArrayList<String>();

        while (it.hasNext()) {
                String key = it.next();
                keyList.add(key);
        }

        Collections.sort(keyList);

        mSections = new String[keyList.size()]; 
        keyList.toArray(mSections);
	}

	public int getCount() {
		
		return mResults.size();
	}

	public PackageDescription getItem(int position) {
		
		return mResults.get(position);
	}

	public long getItemId(int position) {
		
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = null;		
		ImageView mImage;
		TextView mTitle;
		TextView mState;
		TextView mDetail;
		
		View mDetailArea;
		View mNotificationImages;
		
		v = View.inflate(mContext, mLayout, null);		
		v.setTag(position);
		
		mTitle = (TextView) v.findViewById(R.id.title_text);
		mDetail = (TextView) v.findViewById(R.id.detail_text);
		mState = (TextView) v.findViewById(R.id.state_text);
		mImage = (ImageView) v.findViewById(R.id.icon_app);
		
		mDetailArea = v.findViewById(R.id.detail_area);
		mNotificationImages = v.findViewById(R.id.notification_images);
		
		mImage.setImageDrawable(mResults.get(position).getIcon());
        mTitle.setText(mResults.get(position).getLabel());
        mDetail.setText(mResults.get(position).getPackageName());
		mDetail.setSelected(true);
		
		if(mResults.get(position).getEnabled()){
			mState.setText(R.string.enabled);
			mState.setTextColor(mContext.getResources().getColor(R.color.color_enabled));
		}else{
			mState.setText(R.string.disabled);
			mState.setTextColor(mContext.getResources().getColor(R.color.color_disabled));
		}
		
		if(mResults.get(position) instanceof NotificationDescription){
			//TODO: enhance with other options selected
			mImage.setScaleType(ScaleType.CENTER_CROP);
			final NotificationDescription nd = (NotificationDescription)mResults.get(position);
			
			// TODO: change this to dynamically add these views
			if(nd.getEnabled()){
				mDetailArea.setVisibility(View.GONE);
				mNotificationImages.setVisibility(View.VISIBLE);
				setNotificationImage(v, R.id.sound, com.android.internal.R.drawable.ic_volume_small, position, (nd.sound!=null));
				setNotificationImage(v, R.id.hide, com.android.internal.R.drawable.ic_menu_end_conversation, position, (nd.hide));
				setNotificationImage(v, R.id.filter, com.android.internal.R.drawable.ic_menu_cut_holo_dark, position,(nd.filters!=null));
				setNotificationImage(v, R.id.vibrate, com.android.internal.R.drawable.ic_vibrate_small, position, (nd.vibrateFlags!=0));
				setNotificationImage(v, R.id.vibrate_repeat, R.drawable.ic_settings_display, position, nd.wakeLockMS>0);
				setNotificationImage(v, R.id.led_color, 0, position, true);
				setNotificationImage(v, R.id.background, 0, position, true);
				setNotificationImage(v, R.id.notify_once, R.drawable.ic_notify_once, position, nd.notifyOnce);
			}else{
				mDetail.setVisibility(View.GONE);
				mState.setText(R.string.disabled);
				mState.setTextColor(Color.RED);
			}
			
		}
		
		return v;
	}
	
	private void setNotificationImage(View v, int id, int image, int position, boolean enabled){
		View tmp = v.findViewById(id);
		final NotificationDescription nd = (NotificationDescription)mResults.get(position);
		switch(id){
			case R.id.background:
				if(nd.background<0){
					tmp.setBackground(
							getPreviewColorBackground(false, new int[]{nd.background,nd.background}));
					tmp.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.led_color:
				if(nd.led.color<0){
					tmp.setBackground(
							getLEDColorBackground(nd.led.getType(), nd.led.color));
					tmp.setVisibility(View.VISIBLE);
				}
				break;
			default:
				if(enabled){
					((ImageView)tmp).setImageResource(image);
					tmp.setVisibility(View.VISIBLE);
				}
		}	
		
	}
	
	private GradientDrawable getLEDColorBackground(int type, int color){
		switch(type){
			case 1:
				return getPreviewColorBackground(true, new int[]{
						Color.BLUE,Color.BLUE,Color.CYAN,Color.GREEN,Color.YELLOW,Color.RED,0xFFFF6B00});
			case 2:
				return getPreviewColorBackground(true, new int[]{
						Color.BLUE,Color.BLUE,Color.WHITE,Color.RED,Color.RED});
			default:
				return getPreviewColorBackground(true, new int[]{color,color, Color.LTGRAY});
		}
	}
        
    private GradientDrawable getPreviewColorBackground(boolean circle, int[] colors){
    	GradientDrawable colorButtonBackground = new GradientDrawable(
    			GradientDrawable.Orientation.TL_BR, colors);
		if(circle)colorButtonBackground.setCornerRadius(45.0f);
		colorButtonBackground.setStroke(2, Color.LTGRAY);
		return colorButtonBackground;
    }

    @Override
    public Object[] getSections() {
        return mSections;
    }

    @Override
    public int getPositionForSection(int section) {
        // Log.v("getPositionForSection", ""+section);
        String letter = mSections[section];

        return mAlphaIndexer.get(letter);
    }

    @Override
    public int getSectionForPosition(int position) {

        String letter = mResults.get(position).getLabel().substring(0,1).toUpperCase();
        int size = mSections.length;
        for (int i = 0; i < size; i++) {
            if(mSections[i].equals(letter)){
                return i;
            }
        }
        
        return 0;
    }

}
