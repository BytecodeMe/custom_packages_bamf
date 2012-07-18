package com.bamf.settings.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bamf.settings.R;
import android.preference.Preference;

public class IconPreference extends Preference {

    private Drawable mIcon;
    private int mPanelColor;
    private boolean mPanelCircle;

    public IconPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.preference_icon);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.IconPreferenceScreen, defStyle, 0);
        mIcon = a.getDrawable(R.styleable.IconPreferenceScreen_icon);
        mPanelCircle = a.getBoolean(R.styleable.IconPreferenceScreen_circle, false);
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        ImageView imageView = (ImageView) view.findViewById(R.id.icon);
        if (imageView != null && mIcon != null) {
            imageView.setImageDrawable(mIcon);
        }
        
        setDisabled(!isEnabled());
    }
    
    private void setDisabled(boolean disable){
    	if(mIcon!=null){
    		if(disable){
	    		ColorMatrix cm = new ColorMatrix();
	    		cm.setSaturation(0);
	    		mIcon.setAlpha(180);
	    		mIcon.setColorFilter(new ColorMatrixColorFilter(cm));
    		}else{
    			mIcon.setAlpha(255);
    			mIcon.clearColorFilter();
    		}
    	}
    }

    public void setIcon(Drawable icon) {
        if ((icon != null && mIcon == null) || (icon != null && !icon.equals(mIcon))) {
            mIcon = icon.mutate();
            notifyChanged();
        }
    }
    
    public void setupPanel(int color, boolean circle) {
    	mPanelColor = color;
    	mPanelCircle = circle;
    	createColorIcon(color, circle);
    	notifyChanged();
    }
    
    private void createColorIcon(int color, boolean circle){
    	GradientDrawable colorButtonBackground = new GradientDrawable(
    			GradientDrawable.Orientation.TL_BR, new int[]{color,color});
    	if(circle)colorButtonBackground.setCornerRadius(45.0f);
		colorButtonBackground.setStroke(2, Color.LTGRAY);
		mIcon = colorButtonBackground;
		notifyChanged();
    }

    public Drawable getIcon() {
        return mIcon;
    }
    
    public int getColor(){
    	return mPanelColor;
    }
}
