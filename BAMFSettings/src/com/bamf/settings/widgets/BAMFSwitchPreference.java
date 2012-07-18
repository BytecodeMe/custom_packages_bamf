/*
 * Copyright (C) 2010 The Android Open Source Project
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
import android.content.res.TypedArray;
import android.preference.TwoStatePreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

/**
 * A {@link Preference} that provides a two-state toggleable option.
 * <p>
 * This preference will store a boolean into the SharedPreferences.
 *
 * @attr ref android.R.styleable#SwitchPreference_summaryOff
 * @attr ref android.R.styleable#SwitchPreference_summaryOn
 * @attr ref android.R.styleable#SwitchPreference_switchTextOff
 * @attr ref android.R.styleable#SwitchPreference_switchTextOn
 * @attr ref android.R.styleable#SwitchPreference_disableDependentsState
 */
public class BAMFSwitchPreference extends TwoStatePreference {
    // Switch text for on and off states
    private CharSequence mSwitchOn;
    private CharSequence mSwitchOff;
    private boolean mSendClickAccessibilityEvent;
    
    private final Listener mListener = new Listener();

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                // Listener didn't like it, change it back.
                // CompoundButton will make sure we don't recurse.
                buttonView.setChecked(!isChecked);
                return;
            }

            BAMFSwitchPreference.this.setChecked(isChecked);
        }
    }

    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context The Context that will style this preference
     * @param attrs Style attributes that differ from the default
     * @param defStyle Theme attribute defining the default style options
     */
    public BAMFSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.SwitchPreference, defStyle, 0);
        setSummaryOn(a.getString(com.android.internal.R.styleable.SwitchPreference_summaryOn));
        setSummaryOff(a.getString(com.android.internal.R.styleable.SwitchPreference_summaryOff));
        setSwitchTextOn(a.getString(
                com.android.internal.R.styleable.SwitchPreference_switchTextOn));
        setSwitchTextOff(a.getString(
                com.android.internal.R.styleable.SwitchPreference_switchTextOff));
        setDisableDependentsState(a.getBoolean(
                com.android.internal.R.styleable.SwitchPreference_disableDependentsState, false));
        a.recycle();
    }

    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context The Context that will style this preference
     * @param attrs Style attributes that differ from the default
     */
    public BAMFSwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.switchPreferenceStyle);
    }

    /**
     * Construct a new SwitchPreference with default style options.
     *
     * @param context The Context that will style this preference
     */
    public BAMFSwitchPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        view.setClickable(false);
        View checkableView = view.findViewById(com.android.internal.R.id.switchWidget);
        if (checkableView != null && checkableView instanceof Checkable) {
            ((Checkable) checkableView).setChecked(isChecked());

//            sendAccessibilityEvent(checkableView);

            if (checkableView instanceof Switch) {
                final Switch switchView = (Switch) checkableView;
                switchView.setTextOn(mSwitchOn);
                switchView.setTextOff(mSwitchOff);
                switchView.setOnCheckedChangeListener(mListener);
            }
        }

        syncSummaryView(view);
    }
    
    @Override
    protected void onClick() {
        super.onClick();

        boolean newValue = !isChecked();

        mSendClickAccessibilityEvent = true;

//        if (!callChangeListener(newValue)) {
//            return;
//        }

        setChecked(newValue);
    }
    
    void sendAccessibilityEvent(View view) {
        // Since the view is still not attached we create, populate,
        // and send the event directly since we do not know when it
        // will be attached and posting commands is not as clean.
        AccessibilityManager accessibilityManager = AccessibilityManager.getInstance(getContext());
        if (mSendClickAccessibilityEvent && accessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain();
            event.setEventType(AccessibilityEvent.TYPE_VIEW_CLICKED);
            view.onInitializeAccessibilityEvent(event);
            view.dispatchPopulateAccessibilityEvent(event);
            accessibilityManager.sendAccessibilityEvent(event);
        }
        mSendClickAccessibilityEvent = false;
    }

    /**
     * Sync a summary view contained within view's subhierarchy with the correct summary text.
     * @param view View where a summary should be located
     */
    void syncSummaryView(View view) {
        // Sync the summary view
        TextView summaryView = (TextView) view.findViewById(com.android.internal.R.id.summary);
        if (summaryView != null) {
            boolean useDefaultSummary = true;
            if (isChecked() && getSummaryOn() != null) {
                summaryView.setText(getSummaryOn());
                useDefaultSummary = false;
            } else if (!isChecked() && getSummaryOff() != null) {
                summaryView.setText(getSummaryOff());
                useDefaultSummary = false;
            }

            if (useDefaultSummary) {
                final CharSequence summary = getSummary();
                if (summary != null) {
                    summaryView.setText(summary);
                    useDefaultSummary = false;
                }
            }

            int newVisibility = View.GONE;
            if (!useDefaultSummary) {
                // Someone has written to it
                newVisibility = View.VISIBLE;
            }
            if (newVisibility != summaryView.getVisibility()) {
                summaryView.setVisibility(newVisibility);
            }
        }
    }

    /**
     * Set the text displayed on the switch widget in the on state.
     * This should be a very short string; one word if possible.
     *
     * @param onText Text to display in the on state
     */
    public void setSwitchTextOn(CharSequence onText) {
        mSwitchOn = onText;
        notifyChanged();
    }

    /**
     * Set the text displayed on the switch widget in the off state.
     * This should be a very short string; one word if possible.
     *
     * @param offText Text to display in the off state
     */
    public void setSwitchTextOff(CharSequence offText) {
        mSwitchOff = offText;
        notifyChanged();
    }

    /**
     * Set the text displayed on the switch widget in the on state.
     * This should be a very short string; one word if possible.
     *
     * @param resId The text as a string resource ID
     */
    public void setSwitchTextOn(int resId) {
        setSwitchTextOn(getContext().getString(resId));
    }

    /**
     * Set the text displayed on the switch widget in the off state.
     * This should be a very short string; one word if possible.
     *
     * @param resId The text as a string resource ID
     */
    public void setSwitchTextOff(int resId) {
        setSwitchTextOff(getContext().getString(resId));
    }

    /**
     * @return The text that will be displayed on the switch widget in the on state
     */
    public CharSequence getSwitchTextOn() {
        return mSwitchOn;
    }

    /**
     * @return The text that will be displayed on the switch widget in the off state
     */
    public CharSequence getSwitchTextOff() {
        return mSwitchOff;
    }
}
