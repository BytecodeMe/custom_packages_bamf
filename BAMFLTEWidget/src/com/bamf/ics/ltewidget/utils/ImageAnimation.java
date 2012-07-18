package com.bamf.ics.ltewidget.utils;

import android.util.Log;

public class ImageAnimation {
	private ImageAnimator mAnimator;
	private int mFrameCount;
	private AnimationPlay mPlayListener;
	private boolean mRepeat;
	private boolean mReverse;
	
	private static final String TAG = ImageAnimation.class.getSimpleName();
	
	public ImageAnimation(){
		mFrameCount = 0;
		mRepeat = false;
		mReverse = false;
	}
	
	public void clearAnimation(){
		if(mAnimator != null){
			mAnimator.Terminate();
			mAnimator = null;
		}
	}
	
	public void setAnimationPlayListener(AnimationPlay listener){
		mPlayListener = listener;
	}
	
	public void setFrameCount(int count){
		mFrameCount = count;
	}
	
	public void setReverse(boolean bReverse){
		mReverse = bReverse;
	}
	
	public void startAnimation(long millis, boolean bRepeat){
		mAnimator = new ImageAnimator(millis);
		mRepeat = bRepeat;
		mAnimator.start();
	}
	
	public abstract interface AnimationPlay{
		public abstract void onAnimationStart();
		public abstract void onAnimationStop();
		public abstract void onAnimationUpdate(int curFrame);
	}
	
	public class ImageAnimator extends Thread{
		private boolean mDone;
		private final long mDuration;
		private long mFrameDelay;
		private int nCurFrame;
		
	
		public ImageAnimator(long dur){
			mFrameDelay = 0;
			nCurFrame = -1;
			mDone = false;
			mDuration = dur;
			
			if(mFrameCount > 1){
				mFrameDelay = mDuration / (mFrameCount - 1);
			}
			
		}
		
		public synchronized void Terminate(){
			synchronized(this){
				try{
					mDone = true;
				}catch(Throwable t){
					Log.e(TAG, "Exception thrown", t);
				}
			}
		}
		
		public void run(){
			if(mPlayListener != null){
				if(mFrameCount > 0){
					if(!mReverse){
						nCurFrame = 0;
					}else{
						nCurFrame = mFrameCount - 1;						
					}
					
					mPlayListener.onAnimationStart();
					
					while(!mDone){
						mPlayListener.onAnimationUpdate(nCurFrame);
						
						if(nCurFrame < mFrameCount && nCurFrame >= 0){
							try{
								ImageAnimator.sleep(mFrameDelay);
							}catch(InterruptedException eX){
								eX.printStackTrace();
							}
							
							if(!mReverse){
								nCurFrame++;
							}else{
								nCurFrame--;
							}
						}else{
							if(mRepeat){
								if(!mReverse){
									nCurFrame = 0;
								}else{
									nCurFrame = mFrameCount - 1;						
								}
							}else{
								mDone = true;
							}			
						}
					}
					mPlayListener.onAnimationStop();
				}
			}
		}
	}
}