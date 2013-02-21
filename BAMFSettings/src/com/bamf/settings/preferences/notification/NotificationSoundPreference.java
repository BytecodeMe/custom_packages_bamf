/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.bamf.settings.preferences.notification;


import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.util.AttributeSet;
import android.util.Log;

public class NotificationSoundPreference extends RingtonePreference {
    private static final String TAG = "NotificaitonSoundPreference";
    
    private Uri mSelected;
    
    public NotificationSoundPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        super.onPrepareRingtonePickerIntent(ringtonePickerIntent);
        
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
    }

    @Override
    protected void onSaveRingtone(Uri ringtoneUri) {
        mSelected = ringtoneUri;
        this.getOnPreferenceChangeListener().onPreferenceChange(this, ringtoneUri);
    }

    @Override
    protected Uri onRestoreRingtone() {
        return mSelected;
    }
    
    public void setNotification(Uri sound){
    	mSelected = sound;
    }
    
    public Uri getNotification(){
    	return mSelected;
    }
    
}
