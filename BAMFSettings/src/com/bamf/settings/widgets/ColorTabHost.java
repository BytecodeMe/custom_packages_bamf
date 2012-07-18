package com.bamf.settings.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.bamf.settings.R;

public class ColorTabHost extends TabHost {

	private static final String HSV_TAG = "HSV";
	private static final String HEX_TAG = "HEX";

	private RelativeLayout mColorPickerView;
	private ColorPickerView mColorPicker;
	private ColorHexView mHexView;
	private ViewGroup mTabs;
	
	private ColorPanelView mOldColor;
	private ColorPanelView mNewColor;

	public ColorTabHost(Context context) {
		this(context, null);
	}

	public ColorTabHost(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void onFinishInflate(){
		init();
	}

	private void init() {
		setup();
		
		LayoutInflater mLayoutInflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mColorPickerView = (RelativeLayout)mLayoutInflater.inflate(R.layout.dialog_color_picker,
				null);

		mHexView = new ColorHexView(getContext());
		mHexView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		
		mColorPicker = (ColorPickerView) mColorPickerView
				.findViewById(R.id.color_picker_view);
		mOldColor = (ColorPanelView) mColorPickerView.findViewById(R.id.old_color_panel);
		mNewColor = (ColorPanelView) mColorPickerView.findViewById(R.id.new_color_panel);

		((LinearLayout) mOldColor.getParent()).setPadding(Math
				.round(mColorPicker.getDrawingOffset()), 0, Math
				.round(mColorPicker.getDrawingOffset()), 0);

		
		
		final TabWidget tabs = (TabWidget) findViewById(android.R.id.tabs);
		
		mTabs = tabs;
		
		if (tabs == null) throw new Resources.NotFoundException();
		
		
		
		ColorTabContentFactory factory = new ColorTabContentFactory();

		TextView tabView;

		tabView = (TextView) mLayoutInflater.inflate(R.layout.tab_widget_indicator, mTabs, false);
		tabView.setText("HSV");
		tabView.setContentDescription("HSV");
		TabSpec hsvTab = newTabSpec(HSV_TAG).setIndicator(tabView)
				.setContent(factory);

		tabView = (TextView) mLayoutInflater.inflate(R.layout.tab_widget_indicator, mTabs, false);
		tabView.setText("HEX");
		tabView.setContentDescription("HEX");
		TabSpec hexTab = newTabSpec(HEX_TAG).setIndicator(tabView)
				.setContent(factory);
		addTab(hsvTab);
		addTab(hexTab);

	}

	class ColorTabContentFactory implements TabContentFactory {
		@Override
		public View createTabContent(String tag) {
			if (HSV_TAG.equals(tag)) {
				return mColorPickerView;
			}
			if (HEX_TAG.equals(tag)) {
				return mHexView;
			}
			return null;
		}
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		boolean remeasureTabWidth = (mTabs.getLayoutParams().width <= 0);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        // Set the width of the tab list to the content width
        if (remeasureTabWidth) {            
            // set the size of the widget to the size of all of its children
            // this is in case we add more and they scroll
            int contentWidth = getWidth();
            int children = mTabs.getChildCount();
            for(int i = 0; i < children; i++)
                mTabs.getChildAt(i).getLayoutParams().width = (int)(getWidth()*0.5f);
            
            if (contentWidth > 0 && mTabs.getLayoutParams().width != contentWidth) {
                // Set the width and show the tab bar
                mTabs.getLayoutParams().width = contentWidth;
                post(new Runnable() {
                    public void run() {
                        mTabs.requestLayout();
                    }
                });
            }
        }       
    }
	
	public View getColorPickerView(){
		return mColorPickerView;
	}
	
	public ColorPickerView getColorPicker(){
		return mColorPicker;
	}
	
	public ColorHexView getHexView(){
		return mHexView;
	}
	
	public static enum ColorPanel {
		OLD_COLOR,
		NEW_COLOR
	}
	
	public ColorPanelView getColorPanel(ColorPanel panel){
		switch(panel){
			case OLD_COLOR:
				return mOldColor;
			case NEW_COLOR:
			default:
				return mNewColor;
		}
	}

}
