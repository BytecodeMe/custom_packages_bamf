package com.bamf.settings.widgets;

import com.bamf.settings.R;
import com.bamf.settings.activities.SettingsActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.util.DisplayMetrics;


public class SettingsTabHost extends TabHost implements TabHost.OnTabChangeListener{

	static final String LOG_TAG = "SettingsTabHost";
	
	//Add tags for additional tabs here.
	public static final String PERFORMANCE_TAB_TAG = "PERFORMANCE";
    public static final String SYSTEM_TAB_TAG = "SYSTEM";
    public static final String VISUAL_TAB_TAG = "VISUAL"; 
    
    private final LayoutInflater mLayoutInflater;
	private ViewGroup mTabs;
    private ViewGroup mTabsContainer;
    private SettingsPagedView mSettingsPane;
    private Context mContext;
    private boolean mSuppressContentCallback = false;
        
//    private TextView mPageText;
    private TextView mPageCount;
    private SettingsActivity mSettings;
    
    public SettingsTabHost(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);	
		
	}
	
	/**
     * Convenience methods to select specific tabs.  We want to set the content type immediately
     * in these cases, but we note that we still call setCurrentTabByTag() so that the tab view
     * reflects the new content (but doesn't do the animation and logic associated with changing
     * tabs manually).
     * 
     * Add methods for any additional tabs here.
     */
    private void setContentTypeImmediate(SettingsPagedView.ContentType type) {        
        onTabChangedEnd(type);
    }
    void selectPerformanceTab() {
        setContentTypeImmediate(SettingsPagedView.ContentType.Performance);
        setCurrentTabByTag(PERFORMANCE_TAB_TAG);
    }
    void selectSystemTab() {
        setContentTypeImmediate(SettingsPagedView.ContentType.System);
        setCurrentTabByTag(SYSTEM_TAB_TAG);
    }
    void selectVisualTab() {
        setContentTypeImmediate(SettingsPagedView.ContentType.Visual);
        setCurrentTabByTag(VISUAL_TAB_TAG);
    }   

    /**
     * Setup the tab host and create all necessary tabs.
     */
    @Override
    protected void onFinishInflate() {
        // Setup the tab host
        setup();

        final ViewGroup tabsContainer = (ViewGroup) findViewById(R.id.tabs_container);
        final TabWidget tabs = (TabWidget) findViewById(android.R.id.tabs);
        final SettingsPagedView settingsPane = (SettingsPagedView)
                findViewById(R.id.settings_pane_content);
        mTabs = tabs;
        mTabsContainer = tabsContainer;
        mSettingsPane = settingsPane;        
        mPageCount = (TextView) findViewById(R.id.page_number);
        
        if (tabs == null || mSettingsPane == null) throw new Resources.NotFoundException();

        // Configure the tabs content factory to return the same paged view (that we change the
        // content filter on)
        TabContentFactory contentFactory = new TabContentFactory() {
            public View createTabContent(String tag) {
            	return settingsPane;
            }
        };

        // Create the tabs
        //Add any additional tabs here, and also make sure to add them to strings.xml
        TextView tabView;
        String label;
        label = mContext.getString(R.string.performance_tab_label);        
        tabView = (TextView) mLayoutInflater.inflate(R.layout.tab_widget_indicator, tabs, false);
        tabView.setText(label);
        tabView.setContentDescription(label);
        addTab(newTabSpec(PERFORMANCE_TAB_TAG).setIndicator(tabView).setContent(contentFactory));
        label = mContext.getString(R.string.system_tab_label);
        tabView = (TextView) mLayoutInflater.inflate(R.layout.tab_widget_indicator, tabs, false);
        tabView.setText(label);
        tabView.setContentDescription(label);
        addTab(newTabSpec(SYSTEM_TAB_TAG).setIndicator(tabView).setContent(contentFactory));
        label = mContext.getString(R.string.visual_tab_label);
        tabView = (TextView) mLayoutInflater.inflate(R.layout.tab_widget_indicator, tabs, false);
        tabView.setText(label);
        tabView.setContentDescription(label);
        addTab(newTabSpec(VISUAL_TAB_TAG).setIndicator(tabView).setContent(contentFactory));
        setOnTabChangedListener(this);
        
        // set the width of the first three tabs to fill width the screen
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int screen = dm.widthPixels;

        // make the first tab slightly bigger because the text is longer
        for(int i = 0; i < 3; i++)
            mTabs.getChildAt(i).getLayoutParams().width = (int)((i==0)?screen*0.36F:screen*0.32F);
        
        // Hide the tab bar until we measure
        mTabsContainer.setAlpha(0f);
        
        // should fix the tabs being out of sync after inflating
        this.setCurrentTab(0);
    }  
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean remeasureTabWidth = (mTabs.getLayoutParams().width <= 0);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Set the width of the tab list to the content width
        if (remeasureTabWidth) {            
            // set the size of the widget to the size of all of its children
            // this is in case we add more and they scroll
            int contentWidth = 0;
            int children = mTabs.getChildCount();
            for(int i = 0; i < children; i++)
                contentWidth += mTabs.getChildAt(i).getLayoutParams().width;
            
            if (contentWidth > 0 && mTabs.getLayoutParams().width != contentWidth) {
                // Set the width and show the tab bar
                mTabs.getLayoutParams().width = contentWidth;
                post(new Runnable() {
                    public void run() {
                        mTabs.requestLayout();
                        mTabsContainer.setAlpha(1f);
                    }
                });
            }
        }       
    }   
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Intercept all touch events up to the bottom of the AppsCustomizePane so they do not fall
        // through to the workspace and trigger showWorkspace()
        if (event.getY() < mSettingsPane.getBottom()) {
            return true;
        }
        return super.onTouchEvent(event);
    }  
    
    private void reloadCurrentPage() {    	
        mSettingsPane.requestFocus();
    }

    private void onTabChangedEnd(SettingsPagedView.ContentType type) {
        mSettingsPane.setContentType(type);
    }
    
    public void setActivity(SettingsActivity activity){
        mSettings = activity;
    }

    @Override
    public void onTabChanged(String tabId) {
        final SettingsPagedView.ContentType type = getContentTypeForTabTag(tabId);
        

        // call this to hide/show the menu only on the performance tab for now
        mSettings.onTabChanged(tabId);        
        
        if (mSuppressContentCallback) {
            mSuppressContentCallback = false;
            return;
        }

        // Animate the changing of the tab content by fading pages in and out
        final Resources res = getResources();
        final int duration = res.getInteger(R.integer.config_tabTransitionDuration);

        // We post a runnable here because there is a delay while the first page is loading and
        // the feedback from having changed the tab almost feels better than having it stick
        post(new Runnable() {
            @Override
            public void run() {                  
                
                // Toggle the new content                
                onTabChangedEnd(type);

                // Animate the transition
                ObjectAnimator outAnim = ObjectAnimator.ofFloat(mSettingsPane, "alpha", 1f,0f);
                outAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        
                    }
                });
                ObjectAnimator inAnim = ObjectAnimator.ofFloat(mSettingsPane, "alpha", 0f,1f);
                inAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        reloadCurrentPage();
                    }
                });
                AnimatorSet animSet = new AnimatorSet();
                animSet.playTogether(outAnim, inAnim);
                animSet.setDuration(duration * 2);
                animSet.start();
            }
        });
    }   

	public void setCurrentTabFromContent(SettingsPagedView.ContentType type) {		
        mSuppressContentCallback = true;
        setCurrentTabByTag(getTabTagForContentType(type));
    }    
    /**
     * Returns the content type for the specified tab tag.
     * Make sure to add any new tabs in.
     */
    public SettingsPagedView.ContentType getContentTypeForTabTag(String tag) {
        if (tag.equals(PERFORMANCE_TAB_TAG)) {
            return SettingsPagedView.ContentType.Performance;
        } else if (tag.equals(SYSTEM_TAB_TAG)) {
            return SettingsPagedView.ContentType.System;
        } else if (tag.equals(VISUAL_TAB_TAG)) {
        	return SettingsPagedView.ContentType.Visual;
        }
        return SettingsPagedView.ContentType.Performance;
    }

    /**
     * Returns the tab tag for a given content type.
     */
    public String getTabTagForContentType(SettingsPagedView.ContentType type) {
        if (type == SettingsPagedView.ContentType.Performance) {
            return PERFORMANCE_TAB_TAG;
        } else if (type == SettingsPagedView.ContentType.System) {
            return SYSTEM_TAB_TAG;
        } else if (type == SettingsPagedView.ContentType.Visual) {
            return VISUAL_TAB_TAG;
        }
        return PERFORMANCE_TAB_TAG;
    }  	
	
	public void updateCountText(String count) {
	    if(mPageCount != null){
	        mPageCount.setText(count);
	    }
	}
	
	public void setPageView(TextView v){
	    mPageCount = v;
	}

	

}
