package com.bamf.settings.widgets;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.bamf.settings.R;

public class NavbarDragView extends LinearLayout {
	
	private static final int KEY_COUNT = 5;
	
	private static final String KEY_BACK = "back";
	private static final String KEY_HOME = "home";
	private static final String KEY_MENU = "menu_large";
	private static final String KEY_RECENT = "recent_apps";
	private static final String KEY_SEARCH = "search";
	
	private Context mContext;
	int mCount = 0;	
	private OnClickListener mListener;

	public NavbarDragView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;			
	}
	
	@Override
	protected void onAttachedToWindow(){
		super.onAttachedToWindow();
		setOnDragListener(new NavbarDragListener(mContext,(View) getParent()));
	}
	
	public void setupViews(String[] keys, boolean force,OnClickListener listener) {	
		setupViews(keys,force,listener,false);
	}
	
	public void setupViews(String[] keys, boolean force,OnClickListener listener,boolean egg) {			
		
		if(listener != null){
			mListener = listener;
		}
		
		ImageView c1 = (ImageView) findViewById(R.id.image_1);
		ImageView c2 = (ImageView) findViewById(R.id.image_2);
		ImageView c3 = (ImageView) findViewById(R.id.image_3);
		ImageView c4 = (ImageView) findViewById(R.id.image_4);
		ImageView c5 = (ImageView) findViewById(R.id.image_5);
		c1.setOnClickListener(mListener);
		c2.setOnClickListener(mListener);
		c3.setOnClickListener(mListener);
		c3.setVisibility(View.VISIBLE);
		c4.setOnClickListener(mListener);
		c5.setOnClickListener(mListener);
			
		mCount = 0;
		if(keys != null)
			mCount = keys.length;
		final int start = (KEY_COUNT - mCount);	
		
		switch(start){
		case 0:
			c1.setImageDrawable(getDrawableForKey(c1,keys[0],egg));
			setParamsAndBackground(c1,true);	
			c1.setOnLongClickListener(new NavbarClickListener(this));
			c2.setImageDrawable(getDrawableForKey(c2,keys[1],egg));
			setParamsAndBackground(c2,true);	
			c2.setOnLongClickListener(new NavbarClickListener(this));
			c3.setImageDrawable(getDrawableForKey(c3,keys[2],egg));
			setParamsAndBackground(c3,true);
			c3.setOnLongClickListener(new NavbarClickListener(this));
			c4.setImageDrawable(getDrawableForKey(c4,keys[3],egg));
			setParamsAndBackground(c4,true);	
			c4.setOnLongClickListener(new NavbarClickListener(this));
			c5.setImageDrawable(getDrawableForKey(c5,keys[4],egg));
			c5.setOnLongClickListener(new NavbarClickListener(this));
			setParamsAndBackground(c5,true);	
			break;	
		case 1:
			c1.setImageDrawable(getDrawableForKey(c1,keys[0],egg));
			setParamsAndBackground(c1,true);	
			c1.setOnLongClickListener(new NavbarClickListener(this));
			c2.setImageDrawable(getDrawableForKey(c2,keys[1],egg));
			setParamsAndBackground(c2,true);	
			c2.setOnLongClickListener(new NavbarClickListener(this));
			c3.setImageDrawable(getDrawableForKey(c3,keys[2],egg));
			setParamsAndBackground(c3,true);
			c3.setOnLongClickListener(new NavbarClickListener(this));
			c4.setImageDrawable(getDrawableForKey(c4,keys[3],egg));
			setParamsAndBackground(c4,true);	
			c4.setOnLongClickListener(new NavbarClickListener(this));
			c5.setImageDrawable(null);
			setParamsAndBackground(c5,false || force);	
			break;		
		case 2:
			c1.setImageDrawable(null);
			setParamsAndBackground(c1,false || force);	
			c2.setImageDrawable(getDrawableForKey(c2,keys[0],egg));
			setParamsAndBackground(c2,true);
			c2.setOnLongClickListener(new NavbarClickListener(this));
			c3.setImageDrawable(getDrawableForKey(c3,keys[1],egg));
			setParamsAndBackground(c3,true);	
			c3.setOnLongClickListener(new NavbarClickListener(this));
			c4.setImageDrawable(getDrawableForKey(c4,keys[2],egg));
			setParamsAndBackground(c4,true);
			c4.setOnLongClickListener(new NavbarClickListener(this));
			c5.setImageDrawable(null);
			setParamsAndBackground(c5,false || force);
			break;		
		case 3:
			c1.setImageDrawable(null);
			setParamsAndBackground(c1,false || force);	
			c2.setImageDrawable(getDrawableForKey(c2,keys[0],egg));
			setParamsAndBackground(c2,true);	
			c2.setOnLongClickListener(new NavbarClickListener(this));
			c3.setImageDrawable(null);
			setParamsAndBackground(c3,false || force);			
			c4.setImageDrawable(getDrawableForKey(c4,keys[1],egg));
			setParamsAndBackground(c4,true);	
			c4.setOnLongClickListener(new NavbarClickListener(this));
			c5.setImageDrawable(null);
			setParamsAndBackground(c5,false || force);
			break;
		case 4:
			c1.setImageDrawable(null);
			setParamsAndBackground(c1,false || force);	
			c2.setImageDrawable(null);
			setParamsAndBackground(c2,false || force);	
			c3.setImageDrawable(getDrawableForKey(c3,keys[0],egg));
			setParamsAndBackground(c3,true);	
			c3.setOnLongClickListener(new NavbarClickListener(this));
			c4.setImageDrawable(null);
			setParamsAndBackground(c4,false || force);	
			c5.setImageDrawable(null);
			setParamsAndBackground(c5,false || force);
			break;
		case 5:			
			c1.setImageDrawable(null);
			setParamsAndBackground(c1,false || force);	
			c2.setImageDrawable(null);
			setParamsAndBackground(c2,false || force);			
			c3.setImageDrawable(null);
			setParamsAndBackground(c3,true);			
			c4.setImageDrawable(null);
			setParamsAndBackground(c4,false || force);	
			c5.setImageDrawable(null);
			setParamsAndBackground(c5,false || force);
			break;
		}		
	}
	
	public String[] getAvailKeys(List<String> list) {
		
		StringBuilder sb = new StringBuilder();
		if(!list.contains(KEY_BACK))
			sb.append(KEY_BACK + " ");
		if(!list.contains(KEY_HOME))
			sb.append(KEY_HOME + " ");
		if(!list.contains(KEY_MENU))
			sb.append(KEY_MENU + " ");
		if(!list.contains(KEY_RECENT))
			sb.append(KEY_RECENT + " ");
		if(!list.contains(KEY_SEARCH))
			sb.append(KEY_SEARCH + " ");		
		sb.append("");
		if(sb.toString().isEmpty()) return null;
		return sb.toString().split(" ");
	}
	
	private void setParamsAndBackground(ImageView view, boolean show) {		
		LayoutParams p;
		View group = (View) view.getParent();
		p = (LayoutParams) group.getLayoutParams();
		p.width = show ? (int) dpToPx(70) : (int) dpToPx(0);
	}

	public Drawable getDrawableForKey(View v,String key,boolean egg) {		
		
		if(key.equals(KEY_BACK)){
			v.setTag(KEY_BACK);
			return egg ? getResources().getDrawable(R.drawable.powpow) : getResources().getDrawable(R.drawable.ic_sysbar_back);
		}
		if(key.equals(KEY_HOME)){
			v.setTag(KEY_HOME);
			return egg ? getResources().getDrawable(R.drawable.powpow) : getResources().getDrawable(R.drawable.ic_sysbar_home);
		}
		if(key.equals(KEY_MENU)){
			v.setTag(KEY_MENU);
			return egg ? getResources().getDrawable(R.drawable.powpow) : getResources().getDrawable(R.drawable.ic_sysbar_menu_large);
		}
		if(key.equals(KEY_RECENT)){
			v.setTag(KEY_RECENT);
			return egg ? getResources().getDrawable(R.drawable.powpow) : getResources().getDrawable(R.drawable.ic_sysbar_recent);
		}
		if(key.equals(KEY_SEARCH)){
			v.setTag(KEY_SEARCH);
			return egg ? getResources().getDrawable(R.drawable.powpow) : getResources().getDrawable(R.drawable.ic_sysbar_search);
		}
		return null;
	}
	
	private float dpToPx(float dp) {
		Resources r = getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
		return px;
	}

	public String getKeyTags() {
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0;i<getChildCount();i++){
			View v = getChildAt(i);
			if(v instanceof FrameLayout){
				ImageView iv = (ImageView) ((FrameLayout) v).getChildAt(0);
				if(iv.getDrawable() != null)
					sb.append(iv.getTag()+ " ");
			}
		}
		return sb.toString().trim();
	}	
	
	public int getEmptyContainer(){
		View v = null;
		for(int i = 0;i<getChildCount();i++){
			v = getChildAt(i);
			if(v instanceof FrameLayout){
				ImageView iv = (ImageView) ((FrameLayout) v).getChildAt(0);
				if(iv.getDrawable() == null)
					return indexOfChild(v);
			}
		}
		return 0;		
	}

	public int getNextRight(float x) {
		
		View v = null;		
		for(int i = 0;i<getChildCount();i++){
			v = getChildAt(i);
			if(v instanceof FrameLayout){
				if(x <= getCenter(v))
					return Integer.parseInt((String) v.getTag());
			}
		}
		return 0;
	}

	private float getCenter(View v) {		
		return v.getLeft() + (v.getWidth()/2);
	}		
}
