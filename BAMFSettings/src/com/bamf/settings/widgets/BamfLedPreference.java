package com.bamf.settings.widgets;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bamf.settings.R;

public class BamfLedPreference extends DialogPreference {
	private static final String TAG = BamfLedPreference.class.getSimpleName();
	private static final boolean DEBUG = false;
	
	private int mFlags;
	
	private Context mContext;
	private Drawable mIcon;
	private int mOrigColor;
    private int mColor;
    private int mType;
    private int[] mRate;
	
	private Spinner mPresetPicker;
	private static final String[] mPresetLabels = new String[]{
		"Default",
		"Special",
		"Blue",
		"Cyan",
		"Green",
		"Magenta",
		"Red",
		"Purple",
		"Orange",
		"Yellow"
	};
	private static final int[] mPresetValues = new int[]{
		0,
		0,
	    Color.BLUE,
	    Color.CYAN,
	    Color.GREEN,
	    Color.MAGENTA,
	    Color.RED,
	    0xFF2E0854, // purple
	    0xFFFF6B00, // orange
	    0xFFC8FF00, // yellow
	};
	
	private Spinner mSpecialPicker;
	private ImageView mColorPreview;
	
	private Spinner mSpeedPicker;
	private Button mSpeedButton;
	
	private static final ArrayList<int[]> mPresetRateValues = new ArrayList<int[]>();
	
	static{
		mPresetRateValues.add(new int[]{0,1});
		mPresetRateValues.add(new int[]{50,100});
		mPresetRateValues.add(new int[]{1000,1000});
		mPresetRateValues.add(new int[]{500,500});
		mPresetRateValues.add(new int[]{100,100});
	}

	public BamfLedPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setLayoutResource(R.layout.preference_icon);
		setDialogLayoutResource(R.layout.led_dialog);
		createActionButtons();
	}

	public BamfLedPreference(Context context, AttributeSet attrs) {
		this(context, attrs, com.android.internal.R.attr.dialogPreferenceStyle);
	}
	
	public BamfLedPreference(Context context) {
		this(context, null);
	}
	
	public void createActionButtons() {
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }
	
	public void setFlags(int flags){
		mFlags = flags;
		persistInt(flags);
	}
	
	public int getFlags(){
		return mFlags;
	}
	
	@Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        if (positiveResult) {
            int value = 0;
            if (callChangeListener(value)) {
                setFlags(value);
            }
        }
    }
	
	@Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setFlags(restoreValue ? getPersistedInt(mFlags) : (Integer) defaultValue);
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
    
    public void setLED(int type, int color, int[] rate) {
    	mColor = color;
    	mOrigColor = color;
    	mType = type;
    	mRate = rate;
    	if(type==0){
    		setPreviewColorBackground(new int[]{color,color,Color.LTGRAY});
    	}else{
    		setSpecialPreviewColor(type);
    	}
    }

    public int getType() {
        return mType;
    }
    
    public int getColor(){
    	return mColor;
    }
    
    public int[] getRate(){
    	return mRate;
    }
    
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        View customTitleView = View.inflate(mContext, R.layout.dialog_title_with_image, null);
        builder.setCustomTitle(customTitleView);

        ((TextView)customTitleView.findViewById(R.id.title)).setText("LED Color");
        mColorPreview = (ImageView)customTitleView.findViewById(R.id.led_color_preview);
        mColorPreview.setBackgroundDrawable(mIcon);
        mColorPreview.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mSpecialPicker.getSelectedItemPosition()==1 &&
						mSpecialPicker.isEnabled())showColorPicker(mColor);							
			}
		});
    }
	
	@Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
       
        // setup the spinners
        mPresetPicker = setupPresetSpinner(view);        
        mPresetPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				if(mSpecialPicker==null || mSpeedPicker==null)return;
				// default
				if(position==0){
					mSpecialPicker.setSelection(0);
					mSpecialPicker.setEnabled(false);
					mSpeedPicker.setSelection(0);
					mSpeedPicker.setEnabled(false);
					resetPreviewColor();
				// special
				}else if(position==1){
					mSpecialPicker.setEnabled(true);
					mSpeedPicker.setEnabled(true);
					// set the correct option based on the type
					mSpecialPicker.setSelection(mType+position);
				// preset color
				}else{
					mSpeedPicker.setEnabled(true);
					mSpecialPicker.setSelection(1);
					mSpecialPicker.setEnabled(false);

					mColor = mPresetValues[mPresetPicker.getSelectedItemPosition()];
					setPreviewColorBackground(new int[]{
							mPresetValues[mPresetPicker.getSelectedItemPosition()],
							mPresetValues[mPresetPicker.getSelectedItemPosition()],
							Color.LTGRAY});
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
        mSpecialPicker = setupSpecialSpinner(view);
        mSpecialPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mType = 0;
				if(position > 1){
					mType = position-1;
					setSpecialPreviewColor(mType);
				}else if(position == 0){
					mPresetPicker.setSelection(0);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
        mSpeedPicker = setupSpeedSpinner(view);   
        mSpeedButton = (Button)view.findViewById(R.id.led_blink_button);
        mSpeedButton.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				if(mSpeedPicker.isEnabled() && mSpeedPicker.getSelectedItemPosition()==1){
					try{
						String rate = String.valueOf(mRate[0])+","+String.valueOf(mRate[1]);
						showCustomSpeedPicker(rate);
					}catch(Exception e){}
				}
			}
		});
        mSpeedPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// force the custom option if the user selects one of the special leds
				if(mSpecialPicker.isEnabled() && mSpecialPicker.getSelectedItemPosition() > 1
						&& mSpeedPicker.getSelectedItemPosition() != 1){
					mSpeedPicker.setSelection(1);
				}
				// set the proper preset or default value
				if((position==1 && mRate==null) || position!=1){
					mRate = mPresetRateValues.get(position);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}
	
	
	protected Spinner setupPresetSpinner(View dialogView) {
        final Spinner preset = (Spinner) dialogView.findViewById(R.id.led_preset);
        preset.setAdapter(new PresetAdapter(
        		mContext, 
        		android.R.layout.simple_spinner_dropdown_item, 
        		mPresetLabels));
        
        int index = 0;
        if(mColor < 0 || mType > 0){
        	index = 1;
        }
        if(DEBUG)Toast.makeText(mContext, "preset index: "+getPresetColorIndex(mColor)+ " for "+mColor, Toast.LENGTH_SHORT).show();
        if(getPresetColorIndex(mColor) > 1){
        	index = getPresetColorIndex(mColor);
        }
        preset.setSelection(index);
        return preset;
    }
	
	protected Spinner setupSpecialSpinner(View dialogView) {
        final Spinner special = (Spinner) dialogView.findViewById(R.id.led_special);
        special.setAdapter(ArrayAdapter.createFromResource(
        		mContext,
        		R.array.led_special_labels,
        		android.R.layout.simple_spinner_dropdown_item));
        
        int index = 0;
        special.setSelection(index);
        return special;
    }
	
	protected Spinner setupSpeedSpinner(View dialogView) {
        final Spinner speed = (Spinner) dialogView.findViewById(R.id.led_blink_rate);
        speed.setAdapter(ArrayAdapter.createFromResource(
        		mContext,
        		R.array.led_speed_labels,
        		android.R.layout.simple_spinner_dropdown_item));
        
        int index = getPresetRateIndex();
        speed.setSelection(index);
        return speed;
    }
	
	private int getPresetColorIndex(int color){
		for(int i = 2;i < mPresetValues.length;i++){
			if(mPresetValues[i]==color)return i;
		}
		return -1;
	}
	
	private int getPresetRateIndex(){
		for(int i=0;i<mPresetRateValues.size();i++){
			if(Arrays.equals(mPresetRateValues.get(i),mRate))return i;
		}
		return 0;
	}
	
	private void showColorPicker(int color){
		final ColorPickerDialog d = new ColorPickerDialog(mContext, color);
        d.setAlphaSliderVisible(false);
        d.setTitle("Choose a custom LED color");
        d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            	mColor = d.getColor();
            	setPreviewColorBackground(new int[]{d.getColor(), d.getColor(), Color.LTGRAY});	
            }
        });
        d.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        d.show();
	}
	
	private void setSpecialPreviewColor(int type){
		if(type==2){
			setPreviewColorBackground(new int[]{Color.BLUE,Color.BLUE,Color.WHITE,Color.RED,Color.RED});
		}else{
			setPreviewColorBackground(new int[]{Color.BLUE,Color.BLUE,Color.CYAN,Color.GREEN,Color.YELLOW,Color.RED,0xFFFF6B00});
		}
		mColor = -1;
		if(mSpeedPicker!=null)mSpeedPicker.setSelection(1);
	}
        
    private void setPreviewColorBackground(int[] colors){
    	if(colors[0]<0){
	    	GradientDrawable colorButtonBackground = new GradientDrawable(
	    			GradientDrawable.Orientation.TL_BR, colors);
			colorButtonBackground.setCornerRadius(dpToPx(45.0f));
			colorButtonBackground.setStroke(2, Color.LTGRAY);
			mIcon = colorButtonBackground;
    	}else{
    		mIcon = null;
    	}
		if(mColorPreview!=null)mColorPreview.setBackgroundDrawable(mIcon);
		notifyChanged();
    }
    
    private void resetPreviewColor(){
    	setLED(0, 0, mPresetRateValues.get(1));
    }
    
    private void showCustomSpeedPicker(String defValue){
    	final EditText mInput = new EditText(mContext);
    	mInput.setText(defValue);
    	mInput.setPadding(40, 10, 40, 10);
    	mInput.setHint("50,100");
    	
    	AlertDialog d = new AlertDialog.Builder(mContext)
    		.setTitle("Set custom flash speed")
    		.setMessage("Enter values in milliseconds. You need two values for on and off."
    				+"\ni.e. 50,100 - on for 50ms, off for 100ms.")
    		.setView(mInput)
    		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String rate = mInput.getText().toString();
					int[] tempRate = new int[2];
					// TODO: this should suffice if the user inputs garbage
					try{
						String[] temp = rate.split("\\,", 2);
						tempRate[0] = Integer.parseInt(temp[0]);
						tempRate[1] = Integer.parseInt(temp[1]);
						mRate = tempRate;
					}catch(Exception e){
						e.printStackTrace();
					}
					if(DEBUG)Toast.makeText(mContext, "New Rate: "+mRate[0]+","+mRate[1],Toast.LENGTH_SHORT).show();
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			}).create();
    	d.show();
    }
	
	private float dpToPx(float dp) {
		Resources r = mContext.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
		return px;
	}
	
	@Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }
        
        final SavedState myState = new SavedState(superState);
        myState.flags = getFlags();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }
         
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setFlags(myState.flags);
    }
    
    private static class SavedState extends BaseSavedState {
        int flags;
        
        public SavedState(Parcel source) {
            super(source);
            flags = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(flags);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
    
    private class PresetAdapter extends ArrayAdapter<String> {
    	private Context mContext;
    	
		public PresetAdapter(Context context, int resource, String[] labels) {
			super(context, resource, labels);
			mContext = context;
		}
		
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent){
			View v = View.inflate(mContext, R.layout.list_item_row, null);
			
			TextView label = (TextView)v.findViewById(R.id.active_label_name);
			label.setText(mPresetLabels[position]);
			
			((TextView)v.findViewById(R.id.empty_label_name)).setVisibility(View.GONE);
			
			GradientDrawable colorPreset = (GradientDrawable)mContext.getResources()
					.getDrawable(R.drawable.label_count_background).mutate();
			
			if(position > 1){
				colorPreset.setColor(mPresetValues[position]);
				colorPreset.setStroke(2, Color.LTGRAY);
				TextView colorText = (TextView)v.findViewById(R.id.label_count);
				colorText.setVisibility(View.VISIBLE);
				colorText.setBackgroundDrawable(colorPreset);
			}
			
			return v;
		}
    	
    }

}
