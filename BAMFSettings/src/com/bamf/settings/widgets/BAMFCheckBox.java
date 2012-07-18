package com.bamf.settings.widgets;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.view.View;
import android.widget.ImageView;

import com.bamf.settings.R;

/**
 * 
 * @author ihtfp69
 * This is strictly so we can have a custom layout where
 * the title text is left justified in the same place
 * regardless of the size of the icon
 *
 */
public class BAMFCheckBox extends CheckBoxPreference {
    
    private ImageView mContextButton;
    private OnPrefCreatedListener onPrefCreatedListener = null;
    
    // Define our custom Listener interface
    public interface OnPrefCreatedListener {
        public abstract void onPrefCreated(String key);
    }
    
    public void setOnPrefCreatedListener(OnPrefCreatedListener listener){
        onPrefCreatedListener = listener;
    }

    public BAMFCheckBox(Context context) {
        super(context, null);
        setLayoutResource(R.layout.custom_checkbox_preference);
    }
    
    @Override
    public void onBindView(View view){
        super.onBindView(view);

        mContextButton = (ImageView)view.findViewById(R.id.context_menu);
        if(mContextButton != null){
            if(onPrefCreatedListener != null){
                    onPrefCreatedListener.onPrefCreated(this.getKey());
            }
        }
    }
    
    public ImageView getContextButton(){
        return mContextButton;
    }
    
}
