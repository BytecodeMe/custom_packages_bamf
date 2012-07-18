/*
 * Copyright (C) 2010 Daniel Nilsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bamf.settings.widgets;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost.OnTabChangeListener;

import com.bamf.settings.R;
import com.bamf.settings.widgets.ColorTabHost.ColorPanel;

public class ColorPickerDialog extends BAMFAlertDialog implements
		ColorPickerView.OnColorChangedListener,
		ColorHexView.OnColorChangedListener, 
		OnTabChangeListener {
	
	private static final String HSV_TAG = "HSV";
	private static final String HEX_TAG = "HEX";
	
	private ColorTabHost mContent;

	public ColorPickerDialog(Context context, int initialColor) {
		super(context);
		init(initialColor);
	}

	private void init(int color) {
		// To fight color branding.
		getWindow().setFormat(PixelFormat.RGBA_8888);        
		setUp(color);

	}

	private void setUp(int color) {
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		setTitle("Pick a Color");
		
		mContent = (ColorTabHost)inflater.inflate(R.layout.dialog_color_picker_container, null);	
		mContent.setOnTabChangedListener(this);
		
		setContentView(mContent);
		
		mContent.getColorPicker().setOnColorChangedListener(this);
		mContent.getHexView().setOnColorChangedListener(this);

		mContent.getColorPanel(ColorPanel.OLD_COLOR).setColor(color);
		mContent.getColorPicker().setColor(color, true);
		
	}
		
	@Override
	public void onColorChanged(int color, View sender) {

		mContent.getColorPanel(ColorPanel.NEW_COLOR).setColor(color);
		mContent.getColorPicker().setColor(color, false);
		mContent.getHexView().setColor(color);
		
		if(sender.equals(mContent.getHexView())){
			mContent.setCurrentTabByTag(HSV_TAG);
		}

	}

	public void setAlphaSliderVisible(boolean visible) {
		mContent.getColorPicker().setAlphaSliderVisible(visible);
	}

	public int getColor() {
		return mContent.getColorPicker().getColor();
	}

	@Override
	public void onTabChanged(String tabId){
		if(tabId.equals(HSV_TAG)){
			InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	        imm.hideSoftInputFromWindow(mContent.getWindowToken(), 0);
		}
	}

}
