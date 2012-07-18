/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.bamf.settings.adapters;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

	/**
	 * 
	 * This is just a helper class for managing packages in the system apps management screen.
	 *
	 */

public class PackageDescription {
    final ApplicationInfo applicationInfo;
    final String packageName; 
    final String description;

    private Drawable mIcon; // application package icon
    private boolean mEnabled;

    public PackageDescription(
            ApplicationInfo _applicationInfo,
            String _packageName, String _description) {
        applicationInfo = _applicationInfo;

        description = _description;
        packageName = _packageName;
    }
    
    public String getPackageName(){
        return packageName;
    }
    
    public String getLabel() {
        return description;
    }
    
    public ApplicationInfo getInfo(){
    	return applicationInfo;
    }
    
    public boolean getEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
    }
}
