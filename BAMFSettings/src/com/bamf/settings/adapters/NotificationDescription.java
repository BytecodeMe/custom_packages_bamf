package com.bamf.settings.adapters;

import com.bamf.settings.R;

import android.content.pm.ApplicationInfo;
import android.net.Uri;

public class NotificationDescription extends PackageDescription {
	
	public boolean hide;
	public int background;
	public Led led;
	public int wakeLockMS;
	public Uri sound;
	public int vibrateFlags;
	public String filters;
	
	public NotificationDescription(ApplicationInfo appInfo, String pkg, String description) {
		super(appInfo, pkg, description);
	}
	
	public static class Led {
		public int color;
		public int onms;
		public int offms;
		
		public Led(int _color, int _on, int _off){
			color = _color;
			onms = _on;
			offms = _off;
		}
		
		private boolean isSpecial(){
			return (offms==1||offms==2);
		}
		
		public int getType(){
			return (isSpecial()?offms:0);
		}
		
		public int getSpecialImage(){
			return (offms==1)?R.drawable.disco:R.drawable.policesiren;
		}
	}
}
