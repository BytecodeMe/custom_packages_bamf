package com.bamf.settings.preferences;

import com.bamf.settings.R;
import com.bamf.settings.activities.QuickSettingsActivity;
import com.bamf.settings.utils.QuickSettingsUtil;
import com.bamf.settings.widgets.ReorderListView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.internal.view.menu.ActionMenuItem;
import com.android.internal.view.menu.ActionMenuItemView;
import com.android.internal.view.menu.MenuItemImpl;

import java.util.ArrayList;

public class QuickSettingsOrderFragment extends ListFragment
{
    private static final String TAG = "QuickSettingsOrderActivity";
    private static final String SYSTEM_CUSTOM_QUICKSETTING = "custom_quicksetting";
    
    private static final String QUICK_CUSTOM = "QuickCustom";
    
    private static final int MENU_SAVE = 0;
    private static final int MENU_CANCEL = 1;

    private ListView mSettingList;
    private QuickSettingAdapter mSettingAdapter;
    private QuickSettingsActivity mActivity;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setRetainInstance(true);
        this.setHasOptionsMenu(true);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
        //inflater.inflate(R.menu.menu_actionbar, menu);
        menu.add(Menu.NONE, MENU_SAVE, Menu.NONE, "Save")
        	//.setIcon(android.R.drawable.ic_menu_save)
        	.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);        
        
        menu.add(Menu.NONE, MENU_CANCEL, Menu.NONE, "Cancel")
        	.setIcon(com.android.internal.R.drawable.ic_menu_stop)
        	.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case MENU_SAVE:
            	QuickSettingsUtil.saveCurrentQuickSettings(mActivity);
    			mActivity.getFragmentManager().popBackStack();
            	return true;
            case MENU_CANCEL:
            	QuickSettingsUtil.clear();
            	mActivity.getFragmentManager().popBackStack();
            	return true;
            default:
            	return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Use a custom layout
        View root = inflater.inflate(R.layout.reorder_list_activity,null);
        
        View cmdButtons = root.findViewById(R.id.button_container);
        Button doneButton = (Button)cmdButtons.findViewById(R.id.lockscreen_button_add);
        doneButton.setVisibility(View.GONE);
        
        Button cancelButton = (Button)cmdButtons.findViewById(R.id.lockscreen_button_clear);
        cancelButton.setVisibility(View.GONE);
        
        return root;
    }

    /** Called when the activity is first created. */
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);  
        mActivity = (QuickSettingsActivity) getActivity();

        this.mSettingList = getListView();
        this.mSettingList.setFastScrollEnabled(true);
        //this.mSettingList.setCacheColorHint(0);
        ((ReorderListView) mSettingList).setDropListener(mDropListener);
        
        if(mSettingAdapter == null){
        	mSettingAdapter = new QuickSettingAdapter(mActivity);
        }
        setListAdapter(mSettingAdapter);
    }

    @Override
    public void onDestroy() {
        ((ReorderListView) mSettingList).setDropListener(null);
        setListAdapter(null);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        // reload our buttons and invalidate the views for redraw
        mSettingAdapter.reloadQuickSettings();
        mSettingList.invalidateViews();
    }
    
    @Override
    public void onPause() {
    	QuickSettingsUtil.clear();
        super.onPause();
    }

    private ReorderListView.DropListener mDropListener = new ReorderListView.DropListener() {
            public void drop(int from, int to) {
                // get the current button list
                ArrayList<String> quickSettings = QuickSettingsUtil.getQuickSettingListFromString(
                        QuickSettingsUtil.getCurrentQuickSettings(mActivity));

                // move the button
                if(from < quickSettings.size()) {
                    String setting = quickSettings.remove(from);

                    if(to <= quickSettings.size()) {
                        quickSettings.add(to, setting);

                        // save our quick settings in a temp variable in case the user cancels
                        QuickSettingsUtil.saveCurrentQuickSettingsTemp(
                                QuickSettingsUtil.getQuickSettingsFromList(quickSettings));

                        // tell our adapter/listview to reload
                        mSettingAdapter.reloadQuickSettings();
                        mSettingAdapter.notifyDataSetChanged();
                        mSettingList.invalidateViews();                        
                    }
                }
            }
        };

    private class QuickSettingAdapter extends BaseAdapter {
        private Context mContext;
        private Resources mSystemUIResources = null;
        private LayoutInflater mInflater;
        private ArrayList<QuickSettingsUtil.QuickSettingInfo> mQuickSettings;

        public QuickSettingAdapter(Context c) {
            mContext = c;
            mInflater = LayoutInflater.from(mContext);
            
            PackageManager pm = mContext.getPackageManager();
            if(pm != null) {
                try {
                    mSystemUIResources = pm.getResourcesForApplication("com.android.systemui");
                } catch(Exception e) {
                    mSystemUIResources = null;
                    Log.e(TAG, "Could not load SystemUI resources", e);
                }
            }

            reloadQuickSettings();
        }

        public void reloadQuickSettings() {
            ArrayList<String> mSettingsListArray = QuickSettingsUtil.getQuickSettingListFromString(
                    QuickSettingsUtil.getCurrentQuickSettings(mContext));

            mQuickSettings = new ArrayList<QuickSettingsUtil.QuickSettingInfo>();
            for(String qs : mSettingsListArray) {
                if(QuickSettingsUtil.SETTINGS.containsKey(qs)) {
                    mQuickSettings.add(QuickSettingsUtil.SETTINGS.get(qs));
                }
            }
        }

        public int getCount() {
            return mQuickSettings.size();
        }

        public Object getItem(int position) {
            return mQuickSettings.get(position);
        }

        public long getItemId(int position) {
            return position;
        }       

        public View getView(int position, View convertView, ViewGroup parent) {
            final View v;
            if(convertView == null) {
            	v = mInflater.inflate(R.layout.list_item_image_text_image, null);
            } else {
                v = convertView;
            }   

            QuickSettingsUtil.QuickSettingInfo qsPref = mQuickSettings.get(position);
            
            QuickSettingAdapter local = this;
            final ViewHolder vh;
            
            if(v.getTag() == null){
            	vh = new ViewHolder(local);
            }else{
            	vh = (ViewHolder)v.getTag();
            }
            
            vh.line1 = (TextView)v.findViewById(R.id.txt_1x1);
            vh.icon = (ImageView)v.findViewById(R.id.img_indicator);
           
            vh.line1.setText(qsPref.getTitleResId());

            // assume no icon first
            vh.icon.setVisibility(View.GONE);

            // attempt to load the icon for this button
            if(mSystemUIResources != null) {
                int resId = mSystemUIResources.getIdentifier(qsPref.getIcon(), null, null);
                if(resId > 0) {
                    try {
                        Drawable d = mSystemUIResources.getDrawable(resId);
                        vh.icon.setVisibility(View.VISIBLE);
                        vh.icon.setImageDrawable(d);
                    } catch(Exception e) {
                        //Log.e(TAG, "Error retrieving icon drawable", e);
                    }
                }
            }
            
            if(qsPref.getId().equals(QUICK_CUSTOM)){
            	getCustomData(vh);
            }
            
            v.setTag(vh);

            return v;
        }
    
        public class ViewHolder{
          ImageView icon;
          TextView line1;
    
          ViewHolder(QuickSettingAdapter quickSettingAdapter){ }
        } 

		private void getCustomData(ViewHolder vh) {
			String mCustomURI = Settings.System.getString(mActivity.getContentResolver(), 
			        SYSTEM_CUSTOM_QUICKSETTING);
			PackageManager pm = mActivity.getPackageManager();
	        try {
	        	vh.line1.setText("Custom ("+pm.resolveActivity(
	        			Intent.parseUri(mCustomURI, 0),0).activityInfo.loadLabel(pm)+")");
	        	Drawable customIcon = QuickSettingsUtil.CustomIconUtil.loadFromFile(mContext);
	        	if(customIcon==null){
	        	    vh.icon.setImageDrawable(pm.getActivityIcon(Intent.parseUri(mCustomURI, 0)));
	        	}else{
	        	    vh.icon.setImageDrawable(customIcon);
	        	}
	        	vh.icon.setVisibility(View.VISIBLE);
			} catch (Throwable t) {}
		}

    }
}

