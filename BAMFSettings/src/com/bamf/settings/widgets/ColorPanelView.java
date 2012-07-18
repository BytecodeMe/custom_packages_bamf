/*
 * Copyright (C) 2010 Daniel Nilsson
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
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.bamf.settings.utils.AlphaPatternDrawable;

/**
 * This class draws a panel which which will be filled with a color which can be set.
 * It can be used to show the currently selected color which you will get from
 * the {@link ColorPickerView}.
 * @author Daniel Nilsson
 *
 */
public class ColorPanelView extends View{

	/**
	 * The width in pixels of the border 
	 * surrounding the color panel.
	 */
	private final static float	BORDER_WIDTH_PX = 1;
	
	private static float mDensity = 1f;
	
	private int 		mBorderColor = 0xff6E6E6E;
	private int 		mColor = 0xff000000;
	
	private Paint		mBorderPaint;
	private Paint		mColorPaint;
	
	private RectF		mDrawingRect;
	private RectF		mColorRect;
	
	private ColorFilter mColorFilter;
	private int         mAlpha = 0;
	private boolean     mCircle;

	private AlphaPatternDrawable mAlphaPattern;
	
	
	public ColorPanelView(Context context){
		this(context, null);
	}
	
	public ColorPanelView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}
	
	public ColorPanelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init();
	}
	
	private void init(){
		mBorderPaint = new Paint();
		mColorPaint = new Paint();
		mDensity = getContext().getResources().getDisplayMetrics().density;
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		final RectF	rect = mColorRect;
		float cx = (mColorRect.right + getPaddingRight())/2;
		float cy = (mColorRect.bottom + getPaddingBottom())/2;
				
		if(BORDER_WIDTH_PX > 0){
			mBorderPaint.setColor(mBorderColor);
			if(mCircle){
				canvas.drawCircle(cx, cy, 
						cx+BORDER_WIDTH_PX, 
						mBorderPaint);
			}else
				canvas.drawRect(mDrawingRect, mBorderPaint);		
		}
		
		if(mAlphaPattern != null && mAlpha == 0 && !mCircle){
			mAlphaPattern.draw(canvas);
		}
					
		mColorPaint.setColor(mColor);
		if(mAlpha>0){
			mColorPaint.setAlpha(mAlpha);
		}
		if(mColorFilter!=null){
			mColorPaint.setColorFilter(mColorFilter);
		}
		
		if(mCircle){
			canvas.drawCircle(cx, cy, cx-1, mColorPaint);
		}else
			canvas.drawRect(rect, mColorPaint);
	}
		
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		
		setMeasuredDimension(width, height);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		mDrawingRect = new RectF();		
		mDrawingRect.left =  getPaddingLeft();
		mDrawingRect.right  = w - getPaddingRight();
		mDrawingRect.top = getPaddingTop();
		mDrawingRect.bottom = h - getPaddingBottom();
		
		setUpColorRect();
		
	}
	
	private void setUpColorRect(){
		final RectF	dRect = mDrawingRect;		
		
		float left = dRect.left + BORDER_WIDTH_PX;
		float top = dRect.top + BORDER_WIDTH_PX;
		float bottom = dRect.bottom - BORDER_WIDTH_PX;
		float right = dRect.right - BORDER_WIDTH_PX;
		
		mColorRect = new RectF(left,top, right, bottom);
		
		mAlphaPattern = new AlphaPatternDrawable((int)(5 * mDensity));
		
		mAlphaPattern.setBounds(Math.round(mColorRect.left), 
				Math.round(mColorRect.top), 
				Math.round(mColorRect.right), 
				Math.round(mColorRect.bottom));
		
	}
	
	/**
	 * Set the color that should be shown by this view.
	 * @param color
	 */
	public void setColor(int color){
		mColor = color;
		invalidate();
	}
	
	/**
	 * Get the color currently show by this view.
	 * @return
	 */
	public int getColor(){
		return mColor;
	}
	
	/**
	 * Set the color of the border surrounding the panel.
	 * @param color
	 */
	public void setBorderColor(int color){
		mBorderColor = color;
		invalidate();
	}

	/**
	 * Get the color of the border surrounding the panel.
	 */
	public int getBorderColor(){
		return mBorderColor;
	}
	
	/**
	 * Set the enabled state
	 */
	public void setEnabled(boolean enabled){
		if(enabled){
			mColorFilter = null;
			mAlpha = 0;
			mBorderColor = 0xff6E6E6E;
		}else{
			ColorMatrix cm = new ColorMatrix();
    		cm.setSaturation(0);
    		mColorFilter = new ColorMatrixColorFilter(cm);
    		mAlpha = 180;
    		mBorderColor = 0xa06E6E6E;
		}
		invalidate();			
	}
	
	public void setCircle(boolean circle){
		//TODO: add this as a styleable feature
		mCircle = circle;
	}
	
}
