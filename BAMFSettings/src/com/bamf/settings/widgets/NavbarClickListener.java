package com.bamf.settings.widgets;

import com.bamf.settings.R;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;

public class NavbarClickListener implements OnLongClickListener {

	private View mParent;
	private Context mContext;

	public NavbarClickListener(NavbarDragView view) {
		mParent = view;
		mContext = view.getContext();
	}

	public boolean onLongClick(View view) {

		ClipData data = ClipData.newPlainText("", "");
		Shadow shadow = new Shadow(view, mContext);
		view.startDrag(data, shadow, view, 0);

		view.setVisibility(View.INVISIBLE);
		return true;
	}

	public static class Shadow extends View.DragShadowBuilder {
		Drawable d;

		public Shadow(View v, Context context) {
			super(v);
			// TODO: get the correct image for the button
			d = ((ImageView) v).getDrawable();
		}

		@Override
		public void onProvideShadowMetrics(Point shadowSize,
				Point shadowTouchPoint) {
			int width, height;
			// This will make the drag image twice as large
			width = getView().getWidth()*2;
			height = getView().getHeight()*2;
			d.setBounds(0, 0, width, height);
			shadowSize.set(width, height);
			shadowTouchPoint.set(width / 2, height / 2);
		}

		public void onDrawShadow(Canvas canvas) {
			canvas.save();
			d.draw(canvas);
			canvas.restore();
		}
	}
}
