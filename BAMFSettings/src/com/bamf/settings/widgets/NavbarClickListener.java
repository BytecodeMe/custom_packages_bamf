package com.bamf.settings.widgets;

import android.content.ClipData;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnLongClickListener;

public class NavbarClickListener implements OnLongClickListener {
	
	private View mParent;
	
	public NavbarClickListener(NavbarDragView view) {
		mParent = view;
	}

	public boolean onLongClick(View view) {	

		ClipData data = ClipData.newPlainText("", "");
		DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
		view.startDrag(data, shadowBuilder, view, 0);
		view.setVisibility(View.INVISIBLE);
		return true;			
	}
}
