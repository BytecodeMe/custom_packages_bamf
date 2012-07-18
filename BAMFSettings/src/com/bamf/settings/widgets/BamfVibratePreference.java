package com.bamf.settings.widgets;

import android.app.Notification.Notifications;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.bamf.settings.R;

public class BamfVibratePreference extends DialogPreference {
	private int mFlags;
	
	private Context mContext;
	
	private Spinner mVibrateLength;
	private Spinner mVibratePause;
	private Spinner mVibrateRepeat;
	
	private static final int[] LENGTH_VALUES = new int[]{
		Notifications.FLAG_VIBRATE_SHORT,
		Notifications.FLAG_VIBRATE_MEDIUM,
		Notifications.FLAG_VIBRATE_LONG,
		Notifications.FLAG_VIBRATE_DISABLED
	};
	
	private static final int[] PAUSE_VALUES = new int[]{
		Notifications.FLAG_PAUSE_SHORT,
		Notifications.FLAG_PAUSE_MEDIUM,
		Notifications.FLAG_PAUSE_LONG
	};
	
	private static final int[] REPEAT_VALUES = new int[]{
		Notifications.FLAG_REPEAT_ONCE,
		Notifications.FLAG_REPEAT_TWICE,
		Notifications.FLAG_REPEAT_THRICE
	};

	public BamfVibratePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setDialogLayoutResource(R.layout.vibrate_dialog);
		createActionButtons();
	}

	public BamfVibratePreference(Context context, AttributeSet attrs) {
		this(context, attrs, com.android.internal.R.attr.dialogPreferenceStyle);
	}
	
	public BamfVibratePreference(Context context) {
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
	
	public int getFlagsFromSpinners(){
		if(LENGTH_VALUES[mVibrateLength.getSelectedItemPosition()]
				== Notifications.FLAG_VIBRATE_DISABLED){
			return Notifications.FLAG_VIBRATE_DISABLED;
		}else{
			return LENGTH_VALUES[mVibrateLength.getSelectedItemPosition()]
				|PAUSE_VALUES[mVibratePause.getSelectedItemPosition()]
				|REPEAT_VALUES[mVibrateRepeat.getSelectedItemPosition()];
		}
	}
	
	public long[] getVibratePattern(){
    	
    	if(mFlags==0)return null;
    	
    	return Notifications.constructVibratePattern(mFlags);
    }
	
	@Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        if (positiveResult) {
            int value = getFlagsFromSpinners();
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
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        // setup the spinners
        mVibrateLength = setupSpeedSpinner(view);
        mVibratePause = setupPauseSpinner(view);
        mVibrateRepeat = setupRepeatSpinner(view);
        
        mVibrateLength.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				final boolean enabled = LENGTH_VALUES[position]!=Notifications.FLAG_VIBRATE_DISABLED;
				mVibratePause.setEnabled(enabled);
				mVibrateRepeat.setEnabled(enabled);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}
	
	protected Spinner setupSpeedSpinner(View dialogView) {
        final Spinner speed = (Spinner) dialogView.findViewById(R.id.vibrate_length);
        speed.setAdapter(
        		ArrayAdapter.createFromResource(mContext, 
        				R.array.vibrate_length, android.R.layout.simple_spinner_dropdown_item));
        int index = 0;
        if((mFlags & Notifications.FLAG_VIBRATE_MEDIUM)!=0)index=1;
        if((mFlags & Notifications.FLAG_VIBRATE_LONG)!=0)index=2;
        if(mFlags==Notifications.FLAG_VIBRATE_DISABLED)index=3;
        
        speed.setSelection(index);
        return speed;
    }
	
	protected Spinner setupPauseSpinner(View dialogView) {
        final Spinner pause = (Spinner) dialogView.findViewById(R.id.vibrate_pause);
        pause.setAdapter(
        		ArrayAdapter.createFromResource(mContext, 
        				R.array.vibrate_pause, android.R.layout.simple_spinner_dropdown_item));
        
        int index = 0;
        if((mFlags & Notifications.FLAG_PAUSE_MEDIUM)!=0)index=1;
        if((mFlags & Notifications.FLAG_PAUSE_LONG)!=0)index=2;
        
        pause.setSelection(index);
        return pause;
    }
	
	protected Spinner setupRepeatSpinner(View dialogView) {
        final Spinner repeat = (Spinner) dialogView.findViewById(R.id.vibrate_repeat);
        repeat.setAdapter(
        		ArrayAdapter.createFromResource(mContext, 
        				R.array.vibrate_repeat, android.R.layout.simple_spinner_dropdown_item));
        
        int index = 0;
        if((mFlags & Notifications.FLAG_REPEAT_TWICE)!=0)index=1;
        if((mFlags & Notifications.FLAG_REPEAT_THRICE)!=0)index=2;
        
        repeat.setSelection(index);
        return repeat;
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

}
