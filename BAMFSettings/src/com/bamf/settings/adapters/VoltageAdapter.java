package com.bamf.settings.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import com.bamf.settings.R;
import com.bamf.settings.preferences.PerformanceVoltageFragment;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class VoltageAdapter extends BaseAdapter {
	
	private ArrayList<HashMap<String, String>> mListData;
	private LayoutInflater mLayoutInflater;

	public VoltageAdapter(Context context) {		
		mLayoutInflater = LayoutInflater.from(context);		
	}
	
	public void setData(ArrayList<HashMap<String, String>> list) {		
		mListData = list;
	}

	public int getCount() {
		return mListData.size();
	}

	public Object getItem(int position) {		
		return mListData.get(position);		
	}

	public long getItemId(int position) {		
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		TextView freq;
		TextView defaultVolt;
		TextView volt;	
		TextView currentVolt;
		
		
		
			convertView = mLayoutInflater.inflate(R.layout.voltage_item, null);
			
			View seek = convertView.findViewById(R.id.voltage_seek);
			seek.setVisibility(View.GONE);
			seek.setFocusable(false);
			
			View set = convertView.findViewById(R.id.set_button);
			set.setFocusable(false);
			set.setVisibility(View.INVISIBLE);
			
			View cancel = convertView.findViewById(R.id.cancel_button);
			cancel.setFocusable(false);	
			cancel.setVisibility(View.INVISIBLE);
			
			freq = (TextView) convertView.findViewById(R.id.current_frequency);
			defaultVolt = (TextView) convertView.findViewById(R.id.voltage_default);
			volt = (TextView) convertView.findViewById(R.id.current_voltage);
			currentVolt = (TextView) convertView.findViewById(R.id.voltage_selected);
			
			freq.setText(mListData.get(position).get("freq"));				
			defaultVolt.setText("Default: " + PerformanceVoltageFragment.getNearestVoltage(mListData.get(position).get("freq")));				
			volt.setText(mListData.get(position).get("currentMv"));				
			currentVolt.setText(mListData.get(position).get("currentMv"));
					
				
		return convertView;
	}

	

}
