package com.bamf.settings.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bamf.settings.R;

public class ColorHexView extends LinearLayout {

	private EditText mEditBox;
	private Button mDoneButton;
	private int mColor;
	private TextView mError;
	
	private OnColorChangedListener mListener;
	
	public ColorHexView(Context context) {
		super(context);
		init();
	}
	
	public ColorHexView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = inflater.inflate(R.layout.dialog_hex_picker, null);
		addView(content, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	
		mError = (TextView)content.findViewById(R.id.color_hex_error);
		mDoneButton = (Button)content.findViewById(R.id.done_button);
		
		mEditBox = (EditText)content.findViewById(R.id.color_hex_edit);
		mEditBox.setOnEditorActionListener(
		        new EditText.OnEditorActionListener() {
		            @Override
		            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		            	if (actionId == EditorInfo.IME_ACTION_DONE ||
		                        event.getAction() == KeyEvent.ACTION_DOWN &&
		                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
		                    if(validation()){
		                    	return true;
		                    }else
		                    	return false;
		                }
		                return false;
		            }
		        });
		
		mDoneButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				validation();				
			}
		});

	}
	
	public View getEditView() {
		return mEditBox;
	}
	
	public boolean validation() {
		try
		{
			String hex = mEditBox.getText().toString();
			if(hex.startsWith("0x")){
				hex = hex.substring(2);
			}
			if(hex.startsWith("#")){
				hex = hex.substring(1);
			}
			if(hex.length() == 6){
				hex = "FF" + hex;
			}
			if(hex.length() != 8)
				throw new Exception();
			
			mColor = (int)Long.parseLong(hex, 16);
			mError.setVisibility(GONE);
			onColorChanged();
			
			return true;
		}
		catch(Exception e)
		{
			mError.setVisibility(VISIBLE);
			return false;
		}
	}
	
	public int getColor() {
		return mColor;
	}
	
	public void setColor(int color) {
		if(color == this.mColor)
			return;
		this.mColor = color;
		mEditBox.setText(padLeft(Integer.toHexString(color).toUpperCase(), '0', 8));
		mError.setVisibility(GONE);
	}
	
	private String padLeft(String text, char padChar, int size){
		if(text.length() >= size)
			return text;
		StringBuilder result = new StringBuilder();
		for(int i=text.length(); i<size; i++)
			result.append(padChar);
		result.append(text);
		return result.toString();
	}
	
	private void onColorChanged() {
		if(mListener != null)
			mListener.onColorChanged(getColor(), this);
	}
	
	public void setOnColorChangedListener(OnColorChangedListener listener) {
		this.mListener = listener;
	}
	
	public interface OnColorChangedListener {
		public void onColorChanged(int color, View sender);
	}

}
