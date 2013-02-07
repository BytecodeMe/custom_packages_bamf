package com.bamf.settings.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.util.QuickTileToken;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bamf.settings.R;
import com.bamf.settings.utils.QuickTileHelper;
import com.bamf.settings.utils.QuickTileHelper.QuickSettingInfo;
import com.bamf.settings.widgets.QuickTilePicker;
import com.bamf.settings.widgets.QuickTilePicker.OnItemSelectListener;
import com.bamf.settings.widgets.ReorderListView;

public class QuickTileOrderFragment extends ListFragment implements
		OnClickListener, OnItemLongClickListener {

	private static final String TAG = QuickTileOrderFragment.class.getSimpleName();

	private ListView mQuickTileList;
	private QuickSettingAdapter mSettingAdapter;
	private QuickTileHelper mQuickTileHelper;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.reorder_list_activity, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(mQuickTileHelper == null){
			mQuickTileHelper = new QuickTileHelper(getActivity());
		}

		mQuickTileList = getListView();
		mQuickTileList.setFastScrollEnabled(true);
		mQuickTileList.setOnItemLongClickListener(this);

		((ReorderListView) mQuickTileList).setDropListener(mDropListener);

		if (mSettingAdapter == null) {
			mSettingAdapter = new QuickSettingAdapter(getActivity());
		}
		setListAdapter(mSettingAdapter);

		final Button addButton = (Button) getView().findViewById(R.id.lockscreen_button_add);
		addButton.setText(R.string.quick_tile_add);
		addButton.setOnClickListener(this);
		
		final Button defaultsButton = (Button) getView().findViewById(R.id.lockscreen_button_clear);
		defaultsButton.setText(R.string.quick_tile_set_defaults);
		defaultsButton.setEnabled(true);
		defaultsButton.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.lockscreen_button_add:
			pickQuickTile();
			break;
		case R.id.lockscreen_button_clear:
			new AlertDialog.Builder(getActivity())
					.setTitle(getString(R.string.quick_tile_set_defaults))
					.setMessage(getString(R.string.quick_tile_default_description))
					.setPositiveButton(getString(android.R.string.ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// clear the setting to force defaults
									mQuickTileHelper.saveQuickSettings(null);
									// tell our adapter/listview to reload
				                    reload(); 
								}
							})
					.setNegativeButton(getString(android.R.string.cancel),null).show();
			break;
		}
	}
	
	private void pickQuickTile() {
		final QuickTilePicker picker = new QuickTilePicker(getActivity(), new OnItemSelectListener(){
			@Override
			public void onItemSelect(String name){
				Log.d(TAG, "picked "+name+", size:"+mQuickTileHelper.getAvailableSettings().size());
				if(mQuickTileHelper.getAvailableSettings().containsKey(name)){
					mQuickTileHelper.addSetting(new QuickTileToken(name,1,1));
					// tell our adapter/listview to reload
                    reload();
				}
			}
		});
		
		// populate available tiles in the list
		List<QuickTileToken> quickSettings = mQuickTileHelper.getCurrentQuickSettings();
		
		Iterator<Entry<String, QuickSettingInfo>> it = mQuickTileHelper.getAvailableSettings().entrySet().iterator();
	    while (it.hasNext()) {
	        Entry<String, QuickSettingInfo> pairs = it.next();
	        boolean match = false;
	        for(QuickTileToken token: quickSettings){
	        	if(token.getName().equals(pairs.getKey())){
	        		match=true;
	        	}
	        }
	        if(!match){
	        	picker.addItem(pairs.getValue());
	        }
	        //it.remove();
	    }

		picker.setTitle(R.string.title_choose_tile);
		
		if(picker.getItemCount()>0){
			picker.show();
		}else{
			new AlertDialog.Builder(getActivity())
			.setTitle(R.string.title_choose_tile)
			.setMessage(R.string.message_no_more_tiles)
			.setPositiveButton(android.R.string.ok, null)
			.show();
		}
	}

	private ReorderListView.DropListener mDropListener = new ReorderListView.DropListener() {
        public void drop(int from, int to) {
            // get the current button list
            List<QuickTileToken> quickSettings = mQuickTileHelper.getCurrentQuickSettings();

            // move the button
            if(from < quickSettings.size()) {
                QuickTileToken setting = quickSettings.remove(from);

                if(to <= quickSettings.size()) {
                    quickSettings.add(to, setting);

                    // save our quick settings in a temp variable in case the user cancels
                    mQuickTileHelper.saveQuickSettings(quickSettings);

                    reload();                      
                }
            }
        }
    };

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			final int position, long id) {
		
		new AlertDialog.Builder(getActivity())
        .setTitle("Delete")
        .setMessage("Are you sure you want to remove this tile?")
        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	QuickSettingInfo qs = (QuickSettingInfo)mSettingAdapter.getItem(position);
                	mQuickTileHelper.removeSetting(qs.getId());
					// tell our adapter/listview to reload
                    reload();
                }
        })
        .setNegativeButton(getString(android.R.string.cancel), null)
        .show();   
		
		return true;
	}
	
	/**
	 * tell our adapter/listview to reload
	 */
	private void reload(){
        mSettingAdapter.reloadQuickSettings();
        mSettingAdapter.notifyDataSetChanged();
        mQuickTileList.invalidateViews();  
	}
	
	private class QuickSettingAdapter extends BaseAdapter {
        private Context mContext;
        private Resources mSystemUIResources = null;
        private LayoutInflater mInflater;
        private List<QuickSettingInfo> mQuickSettings;

        public QuickSettingAdapter(Context c) {
            mContext = c;
            mInflater = LayoutInflater.from(mContext);
            mQuickSettings = new ArrayList<QuickSettingInfo>();
            
            final PackageManager pm = mContext.getPackageManager();
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
            List<QuickTileToken> mSettingsListArray = mQuickTileHelper.getCurrentQuickSettings();

            mQuickSettings.clear();
            for(QuickTileToken qs : mSettingsListArray) {
                if(mQuickTileHelper.getAvailableSettings().containsKey(qs.getName())) {
                    mQuickSettings.add(mQuickTileHelper.getAvailableSettings().get(qs.getName()));
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

            QuickSettingInfo qsPref = mQuickSettings.get(position);
            
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
            
            if(qsPref.getId().equals(Settings.System.QUICK_CUSTOM)){
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
			String mCustomURI = Settings.System.getString(mContext.getContentResolver(), 
			        Settings.System.QUICK_SETTINGS_CUSTOM);
			final PackageManager pm = mContext.getPackageManager();
	        try {
	        	vh.line1.setText("Custom ("+pm.resolveActivity(
	        			Intent.parseUri(mCustomURI, 0),0).activityInfo.loadLabel(pm)+")");
	        	Drawable customIcon = QuickTileHelper.CustomIconUtil.loadFromFile(mContext);
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
