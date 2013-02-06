package com.bamf.settings.widgets;

import java.util.ArrayList;

import com.bamf.settings.R;
import com.bamf.settings.activities.SettingsActivity;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.Toast;

public class SettingsPagedView extends ViewGroup {

	private static final String TAG = "SettingsPagedView";
	private static final boolean DEBUG = false;

	protected static final int INVALID_PAGE = -1;
	public static final float PANEL_BIT_DEPTH = 24;
	public static final float ALPHA_THRESHOLD = 0.5f / PANEL_BIT_DEPTH;
	// the min drag distance for a fling to register, to prevent random page
	// shifts
	private static final int MIN_LENGTH_FOR_FLING = 25;
	private static final int PAGE_SNAP_ANIMATION_DURATION = 550;
	protected static final float NANOTIME_DIV = 1000000000.0f;
	private static final int MINIMUM_SNAP_VELOCITY = 2200;
	private static final int MIN_FLING_VELOCITY = 250;
	private static final float RETURN_TO_ORIGINAL_PAGE_THRESHOLD = 0.33f;
	// The page is moved more than halfway, automatically move to the next page
	// on touch up.
	private static final float SIGNIFICANT_MOVE_THRESHOLD = 0.4f;
	protected static final int INVALID_POINTER = -1;

	// the velocity at which a fling gesture will cause us to snap to the next
	// page
	protected int mSnapVelocity = 500;

	protected float mDensity;
	protected float mSmoothingTime;
	protected float mTouchX;

	protected boolean mFirstLayout = true;

	protected int mCurrentPage;
	protected int mNextPage = INVALID_PAGE;
	protected int mMaxScrollX;
	protected Scroller mScroller;
	private VelocityTracker mVelocityTracker;

	private float mDownMotionX;
	protected float mLastMotionX;
	protected float mLastMotionXRemainder;
	protected float mLastMotionY;
	protected float mTotalMotionX;
	private int mLastScreenCenter = -1;
	private int[] mChildOffsets;
	private int[] mChildRelativeOffsets;
	private int[] mChildOffsetsWithLayoutScale;

	protected final static int TOUCH_STATE_REST = 0;
	protected final static int TOUCH_STATE_SCROLLING = 1;
	protected final static int TOUCH_STATE_PREV_PAGE = 2;
	protected final static int TOUCH_STATE_NEXT_PAGE = 3;
	protected final static float ALPHA_QUANTIZE_LEVEL = 0.0001f;

	protected int mTouchState = TOUCH_STATE_REST;
	protected boolean mForceScreenScrolled = false;

	protected int mTouchSlop;
	private int mPagingTouchSlop;
	private int mMaximumVelocity;
	private int mMinimumWidth;
	protected int mPageSpacing;
	protected int mPageLayoutPaddingTop;
	protected int mPageLayoutPaddingBottom;
	protected int mPageLayoutPaddingLeft;
	protected int mPageLayoutPaddingRight;
	protected int mPageLayoutWidthGap;
	protected int mPageLayoutHeightGap;
	protected int mCellCountX = 0;
	protected int mCellCountY = 0;
	protected boolean mCenterPagesVertically;
	protected boolean mAllowOverScroll = true;
	protected int mUnboundedScrollX;
	protected int[] mTempVisiblePagesRange = new int[2];

	// parameter that adjusts the layout to be optimized for pages with that
	// scale factor
	protected float mLayoutScale = 1.0f;

	protected int mActivePointerId = INVALID_POINTER;

	protected ArrayList<Boolean> mDirtyPageContent;

	private View mPerformanceView1;
	private View mSystemView1;
	// private View mSystemView2;
	private View mVisualView1;
	private View mVisualView2;
	private View mVisualView3;

	// If true, modify alpha of neighboring pages as user scrolls left/right
	protected boolean mFadeInAdjacentScreens = true;
	// It true, use a different slop parameter (pagingTouchSlop = 2 * touchSlop)
	// for deciding
	// to switch to a new page
	protected boolean mUsePagingTouchSlop = true;
	// If true, the subclass should directly update mScrollX itself in its
	// computeScroll method
	// (SmoothPagedView does this)
	protected boolean mDeferScrollUpdate = false;
	protected boolean mIsPageMoving = false;
	// If set, will defer loading associated pages until the scrolling settles
	private boolean mDeferLoadAssociatedPagesUntilScrollCompletes;

	/**
	 * The different content types that this paged view can show.
	 */
	public enum ContentType {
		Performance, System, Visual
	}

	// Refs
	private SettingsActivity mSettings;
	private final LayoutInflater mLayoutInflater;
	private Context mContext;

	private int mNumPerformancePages = 1;
	private int mNumSystemPages = 1;
	private int mNumVisualPages = 3;

	private IWindowManager mWindowManager;

	// Relating to the scroll and overscroll effects
	ZInterpolator mZInterpolator = new ZInterpolator(0.5f);
	private static float CAMERA_DISTANCE = 6500;
	private static float TRANSITION_SCALE_FACTOR = 0.74f;
	private static float TRANSITION_PIVOT = 0.65f;
	private static float TRANSITION_MAX_ROTATION = 22;
	private static final boolean PERFORM_OVERSCROLL_ROTATION = true;
	private AccelerateInterpolator mAlphaInterpolator = new AccelerateInterpolator(
			0.9f);
	private DecelerateInterpolator mLeftScreenAlphaInterpolator = new DecelerateInterpolator(
			4);

	public SettingsPagedView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);

		mFadeInAdjacentScreens = false;

		setPageSpacing(0);

		setHapticFeedbackEnabled(false);
		init();

	}

	private void init() {

		mCenterPagesVertically = false;

		mDirtyPageContent = new ArrayList<Boolean>();
		mDirtyPageContent.ensureCapacity(32);
		mScroller = new Scroller(getContext(), new ScrollInterpolator());
		mCurrentPage = 0;
		mCenterPagesVertically = true;

		final ViewConfiguration configuration = ViewConfiguration
				.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mPagingTouchSlop = configuration.getScaledPagingTouchSlop();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		mDensity = getResources().getDisplayMetrics().density;

		// mWindowManager = IWindowManager.Stub.asInterface(
		// ServiceManager.getService(Context.WINDOW_SERVICE));
		//
		// final boolean forceNav =
		// mSettings.getResources().getBoolean(com.android.internal.R.bool.config_canForceNavigationBar);
		//
		// try{
		// if(!mWindowManager.hasNavigationBar() && !forceNav){
		// mNumVisualPages = 2;
		// }
		// }catch(RemoteException e){}

		syncPages();
	}

	protected void onUnhandledTap(MotionEvent ev) {
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException(
					"Workspace can only be used in EXACTLY mode.");
		}
		/*
		 * Allow the height to be set as WRAP_CONTENT. This allows the
		 * particular case of the All apps view on XLarge displays to not take
		 * up more space then it needs. Width is still not allowed to be set as
		 * WRAP_CONTENT since many parts of the code expect each page to have
		 * the same width.
		 */
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int maxChildHeight = 0;

		int mPaddingTop = getPaddingTop();
		int mPaddingBottom = getPaddingBottom();
		int mPaddingLeft = getPaddingLeft();
		int mPaddingRight = getPaddingRight();

		final int verticalPadding = mPaddingTop + mPaddingBottom;
		final int horizontalPadding = mPaddingLeft + mPaddingRight;

		// The children are given the same width and height as the workspace
		// unless they were set to WRAP_CONTENT
		if (DEBUG)
			Log.d(TAG, "PagedView.onMeasure(): " + widthSize + ", "
					+ heightSize);
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			// disallowing padding in paged view (just pass 0)
			final View child = getPageAt(i);
			final LayoutParams lp = (LayoutParams) child.getLayoutParams();

			int childWidthMode;
			if (lp.width == LayoutParams.WRAP_CONTENT) {
				childWidthMode = MeasureSpec.AT_MOST;
			} else {
				childWidthMode = MeasureSpec.EXACTLY;
			}

			int childHeightMode;
			if (lp.height == LayoutParams.WRAP_CONTENT) {
				childHeightMode = MeasureSpec.AT_MOST;
			} else {
				childHeightMode = MeasureSpec.EXACTLY;
			}

			final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
					widthSize - horizontalPadding, childWidthMode);
			final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
					heightSize - verticalPadding, childHeightMode);

			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
			maxChildHeight = Math
					.max(maxChildHeight, child.getMeasuredHeight());
			if (DEBUG)
				Log.d(TAG,
						"\tmeasure-child" + i + ": " + child.getMeasuredWidth()
								+ ", " + child.getMeasuredHeight());
		}

		if (heightMode == MeasureSpec.AT_MOST) {
			heightSize = maxChildHeight + verticalPadding;
		}

		setMeasuredDimension(widthSize, heightSize);

		// We can't call getChildOffset/getRelativeChildOffset until we set the
		// measured dimensions.
		// We also wait until we set the measured dimensions before flushing the
		// cache as well, to
		// ensure that the cache is filled with good values.
		invalidateCachedOffsets();

		if (childCount > 0) {
			mMaxScrollX = getChildOffset(childCount - 1)
					- getRelativeChildOffset(childCount - 1);
		} else {
			mMaxScrollX = 0;
		}
	}

	public void setContentType(ContentType type) {
		if (type == ContentType.Visual) {
			invalidatePageData(mNumPerformancePages + mNumSystemPages, true);
		} else if (type == ContentType.System) {
			invalidatePageData(mNumPerformancePages, true);
		} else if (type == ContentType.Performance) {
			invalidatePageData(0, true);
		}
	}

	protected void snapToPage(int whichPage) {
		snapToPage(whichPage, PAGE_SNAP_ANIMATION_DURATION);
	}

	protected void snapToPage(int whichPage, int duration) {
		whichPage = Math.max(0, Math.min(whichPage, getPageCount() - 1));

		if (DEBUG)
			Log.d(TAG, "snapToPage.getChildOffset(): "
					+ getChildOffset(whichPage));
		if (DEBUG)
			Log.d(TAG, "snapToPage.getRelativeChildOffset(): "
					+ getMeasuredWidth() + ", " + getChildWidth(whichPage));
		int newX = getChildOffset(whichPage)
				- getRelativeChildOffset(whichPage);
		final int sX = mUnboundedScrollX;
		final int delta = newX - sX;
		snapToPage(whichPage, delta, duration);
	}

	protected void snapToPage(int whichPage, int delta, int duration) {

		mNextPage = whichPage;

		View focusedChild = getFocusedChild();
		if (focusedChild != null && focusedChild.getRotationY() != 0) {
			screenScrolled(getMeasuredWidth() / 2);
		}

		if (focusedChild != null && whichPage != mCurrentPage
				&& focusedChild == getPageAt(mCurrentPage)) {
			focusedChild.clearFocus();
		}

		pageBeginMoving();

		if (duration == 0) {
			duration = Math.abs(delta);
		}

		if (!mScroller.isFinished())
			mScroller.abortAnimation();
		mScroller.startScroll(mUnboundedScrollX, 0, delta, 0, duration);

		// Load associated pages immediately if someone else is handling the
		// scroll, otherwise defer
		// loading associated pages until the scroll settles
		if (mDeferScrollUpdate) {

		} else {
			mDeferLoadAssociatedPagesUntilScrollCompletes = true;
		}

		updateCurrentTab(whichPage);

	}

	public void updateCurrentTab(int currentPage) {
		SettingsTabHost tabHost = getTabHost();
		if (tabHost != null) {
			String tag = tabHost.getCurrentTabTag();
			if (tag != null) {

				if (currentPage >= mNumPerformancePages + mNumSystemPages
						&& !tag.equals(tabHost
								.getTabTagForContentType(ContentType.Visual))) {
					tabHost.setCurrentTabFromContent(ContentType.Visual);
				} else if (currentPage >= mNumPerformancePages
						&& currentPage < (mNumPerformancePages + mNumSystemPages)
						&& !tag.equals(tabHost
								.getTabTagForContentType(ContentType.System))) {
					tabHost.setCurrentTabFromContent(ContentType.System);
				} else if (currentPage < mNumPerformancePages
						&& !tag.equals(tabHost
								.getTabTagForContentType(ContentType.Performance))) {
					tabHost.setCurrentTabFromContent(ContentType.Performance);
				}
			}
		}
	}

	public void syncPages() {

		if (mVisualView3 == null || mVisualView2 == null
				|| mVisualView1 == null || mSystemView1 == null
				|| mPerformanceView1 == null) {
			// Toast.makeText(mContext, "Setting up Views",
			// Toast.LENGTH_LONG).show();
			removeAllViews();

			if (mVisualView3 == null)
				mVisualView3 = mLayoutInflater.inflate(
						R.layout.visual_settings_navbar, null);
			setupLayout(mVisualView3);
			addView(mVisualView3);

			if (mVisualView2 == null)
				mVisualView2 = mLayoutInflater.inflate(
						R.layout.visual_settings_sysui, null);
			setupLayout(mVisualView2);
			addView(mVisualView2);
			if (mVisualView1 == null)
				mVisualView1 = mLayoutInflater.inflate(
						R.layout.visual_settings_basic, null);
			setupLayout(mVisualView1);
			addView(mVisualView1);
			// if(mSystemView2 == null)
			// mSystemView2 =
			// mLayoutInflater.inflate(R.layout.system_settings_maintenance,
			// null);
			// setupLayout(mSystemView2);
			// addView(mSystemView2);
			if (mSystemView1 == null)
				mSystemView1 = mLayoutInflater.inflate(
						R.layout.system_settings_basic, null);
			setupLayout(mSystemView1);
			addView(mSystemView1);
			if (mPerformanceView1 == null)
				mPerformanceView1 = mLayoutInflater.inflate(
						R.layout.performance_settings_kernel, null);
			setupLayout(mPerformanceView1);
			addView(mPerformanceView1);
		} else {
			// Toast.makeText(mContext, "Views aren't null",
			// Toast.LENGTH_LONG).show();
			return;
		}

	}

	private void setupLayout(View v) {
		LinearLayout layout = (LinearLayout) v.findViewById(R.id.layout);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	// We want our pages to be z-ordered such that the further a page is to the
	// left, the higher
	// it is in the z-order. This is important to insure touch events are
	// handled correctly.
	View getPageAt(int index) {
		return getChildAt(getChildCount() - index - 1);
	}

	protected int indexToPage(int index) {
		return getChildCount() - index - 1;
	}

	// In apps customize, we have a scrolling effect which emulates pulling
	// cards off of a stack.

	protected void screenScrolled(int screenCenter) {

		if (mFadeInAdjacentScreens) {
			for (int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				if (child != null) {
					float scrollProgress = getScrollProgress(screenCenter,
							child, i);
					float alpha = 1 - Math.abs(scrollProgress);
					child.setAlpha(alpha);
					child.invalidate();
				}
			}
			invalidate();
		}

		for (int i = 0; i < getChildCount(); i++) {
			View v = getPageAt(i);
			if (v != null) {
				float scrollProgress = getScrollProgress(screenCenter, v, i);

				float interpolatedProgress = mZInterpolator
						.getInterpolation(Math.abs(Math.min(scrollProgress, 0)));
				float scale = (1 - interpolatedProgress) + interpolatedProgress
						* TRANSITION_SCALE_FACTOR;
				float translationX = Math.min(0, scrollProgress)
						* v.getMeasuredWidth();

				float alpha;

				if (scrollProgress < 0) {
					alpha = scrollProgress < 0 ? mAlphaInterpolator
							.getInterpolation(1 - Math.abs(scrollProgress))
							: 1.0f;
				} else {
					// On large screens we need to fade the page as it nears its
					// leftmost position
					alpha = mLeftScreenAlphaInterpolator
							.getInterpolation(1 - scrollProgress);
				}

				v.setCameraDistance(mDensity * CAMERA_DISTANCE);
				int pageWidth = v.getMeasuredWidth();
				int pageHeight = v.getMeasuredHeight();

				if (PERFORM_OVERSCROLL_ROTATION) {
					if (i == 0 && scrollProgress < 0) {
						// Overscroll to the left
						v.setPivotX(TRANSITION_PIVOT * pageWidth);
						v.setRotationY(-TRANSITION_MAX_ROTATION
								* scrollProgress);
						scale = 1.0f;
						alpha = 1.0f;
						// On the first page, we don't want the page to have any
						// lateral motion
						translationX = 0;
					} else if (i == getChildCount() - 1 && scrollProgress > 0) {
						// Overscroll to the right
						v.setPivotX((1 - TRANSITION_PIVOT) * pageWidth);
						v.setRotationY(-TRANSITION_MAX_ROTATION
								* scrollProgress);
						scale = 1.0f;
						alpha = 1.0f;
						// On the last page, we don't want the page to have any
						// lateral motion.
						translationX = 0;
					} else {
						v.setPivotY(pageHeight / 2.0f);
						v.setPivotX(pageWidth / 2.0f);
						v.setRotationY(0f);
					}
				}

				v.setTranslationX(translationX);
				v.setScaleX(scale);
				v.setScaleY(scale);
				v.setAlpha(alpha);

				// If the view has 0 alpha, we set it to be invisible so as to
				// prevent
				// it from accepting touches
				if (alpha < ALPHA_THRESHOLD) {
					v.setVisibility(INVISIBLE);
				} else if (v.getVisibility() != VISIBLE) {
					v.setVisibility(VISIBLE);
				}
			}
		}
	}

	/**
	 * Used by the parent to get the content width to set the tab bar to
	 * 
	 * @return
	 */
	public int getPageContentWidth() {
		return getMeasuredWidth();
	}

	protected void onPageEndMoving() {
		updatePageCountText();
	}

	public void setup(SettingsActivity settings) {
		mSettings = settings;
		getTabHost().setActivity(mSettings);
		updatePageCountText();
	}

	private SettingsTabHost getTabHost() {
		return (SettingsTabHost) mSettings.findViewById(R.id.settings_pane);
	}

	private void updatePageCountText() {
		getTabHost().updateCountText(getCurrentPageDescription());
	}

	protected String getCurrentPageDescription() {
		int page = (mNextPage != INVALID_PAGE) ? mNextPage : mCurrentPage;
		int stringId = R.string.default_scroll_format;
		int count = 0;

		if (page >= mNumPerformancePages + mNumSystemPages) {
			count = mNumVisualPages;
			page = page - (mNumPerformancePages + mNumSystemPages);
		} else if (page >= mNumPerformancePages
				&& page < (mNumPerformancePages + mNumSystemPages)) {
			count = mNumSystemPages;
			if (count > 1 && mNumPerformancePages > 1)
				page = page - mNumSystemPages;
			else
				page = page - mNumPerformancePages;
		} else if (page < mNumPerformancePages) {
			count = mNumPerformancePages;
		}

		return String.format(getContext().getString(stringId), page + 1, count);
	}

	/**
	 * Returns the index of the currently displayed page.
	 * 
	 * @return The index of the currently displayed page.
	 */
	public int getCurrentPage() {
		return mCurrentPage;
	}

	int getPageCount() {
		return getChildCount();
	}

	/**
	 * Updates the scroll of the current page immediately to its final scroll
	 * position. We use this in CustomizePagedView to allow tabs to share the
	 * same PagedView while resetting the scroll of the previous tab page.
	 */
	protected void updateCurrentPageScroll() {
		int newX = getChildOffset(mCurrentPage)
				- getRelativeChildOffset(mCurrentPage);
		scrollTo(newX, 0);
		mScroller.setFinalX(newX);
	}

	/**
	 * Sets the current page.
	 */
	void setCurrentPage(int currentPage) {
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		// don't introduce any checks like mCurrentPage == currentPage here-- if
		// we change the
		// the default
		if (getChildCount() == 0) {
			return;
		}

		mCurrentPage = Math.max(0, Math.min(currentPage, getPageCount() - 1));
		updateCurrentPageScroll();
		invalidate();
	}

	protected void pageBeginMoving() {
		if (!mIsPageMoving) {
			mIsPageMoving = true;
			onPageBeginMoving();
		}
	}

	protected void pageEndMoving() {
		if (mIsPageMoving) {
			mIsPageMoving = false;
			onPageEndMoving();
		}
	}

	protected boolean isPageMoving() {
		return mIsPageMoving;
	}

	// a method that subclasses can override to add behavior
	protected void onPageBeginMoving() {

	}

	@Override
	public void scrollBy(int x, int y) {
		int mScrollY = getScrollY();
		scrollTo(mUnboundedScrollX + x, mScrollY + y);
	}

	@Override
	public void scrollTo(int x, int y) {
		mUnboundedScrollX = x;

		if (x < 0) {
			if (mAllowOverScroll) {
				screenScrolled(x + (getMeasuredWidth() / 2));
			}
			super.scrollTo(0, y);
		} else if (x > mMaxScrollX) {
			if (mAllowOverScroll) {
				screenScrolled(x + (getMeasuredWidth() / 2));
			}
			super.scrollTo(mMaxScrollX, y);
		} else {
			super.scrollTo(x, y);
		}

		mTouchX = x;
		mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			int mScrollX = getScrollX();
			int mScrollY = getScrollY();
			// Don't bother scrolling if the page does not need to be moved
			if (mScrollX != mScroller.getCurrX()
					|| mScrollY != mScroller.getCurrY()) {
				scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			}
			invalidate();
			return;
		} else if (mNextPage != INVALID_PAGE) {
			mCurrentPage = Math.max(0, Math.min(mNextPage, getPageCount() - 1));
			mNextPage = INVALID_PAGE;

			// Load the associated pages if necessary
			if (mDeferLoadAssociatedPagesUntilScrollCompletes) {
				mDeferLoadAssociatedPagesUntilScrollCompletes = false;
			}

			// We don't want to trigger a page end moving unless the page has
			// settled
			// and the user has stopped scrolling
			if (mTouchState == TOUCH_STATE_REST) {
				pageEndMoving();
			}

			// Notify the user when the page changes

			AccessibilityManager acsm = (AccessibilityManager) getContext()
					.getSystemService(Context.ACCESSIBILITY_SERVICE);
			if (acsm.isEnabled()) {
				AccessibilityEvent ev = AccessibilityEvent
						.obtain(AccessibilityEvent.TYPE_VIEW_SCROLLED);
				ev.getText().add(getCurrentPageDescription());
				sendAccessibilityEventUnchecked(ev);
			}
			return;
		}
		return;
	}

	protected void scrollToNewPageWithoutMovingPages(int newCurrentPage) {
		int newX = getChildOffset(newCurrentPage)
				- getRelativeChildOffset(newCurrentPage);
		int mScrollX = getScrollX();
		int delta = newX - mScrollX;

		final int pageCount = getChildCount();
		for (int i = 0; i < pageCount; i++) {
			View page = (View) getPageAt(i);
			page.setX(page.getX() + delta);
		}
		setCurrentPage(newCurrentPage);
	}

	// A layout scale of 1.0f assumes that the pages, in their unshrunken state,
	// have a
	// scale of 1.0f. A layout scale of 0.8f assumes the pages have a scale of
	// 0.8f, and
	// tightens the layout accordingly
	public void setLayoutScale(float childrenScale) {
		mLayoutScale = childrenScale;
		invalidateCachedOffsets();

		// Now we need to do a re-layout, but preserving absolute X and Y
		// coordinates
		int childCount = getChildCount();
		float childrenX[] = new float[childCount];
		float childrenY[] = new float[childCount];
		for (int i = 0; i < childCount; i++) {
			final View child = getPageAt(i);
			childrenX[i] = child.getX();
			childrenY[i] = child.getY();
		}
		// Trigger a full re-layout (never just call onLayout directly!)
		int widthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(),
				MeasureSpec.EXACTLY);
		int heightSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(),
				MeasureSpec.EXACTLY);
		requestLayout();
		measure(widthSpec, heightSpec);

		layout(getLeft(), getTop(), getRight(), getBottom());
		for (int i = 0; i < childCount; i++) {
			final View child = getPageAt(i);
			child.setX(childrenX[i]);
			child.setY(childrenY[i]);
		}

		// Also, the page offset has changed (since the pages are now smaller);
		// update the page offset, but again preserving absolute X and Y
		// coordinates
		scrollToNewPageWithoutMovingPages(mCurrentPage);
	}

	public void setPageSpacing(int pageSpacing) {
		mPageSpacing = pageSpacing;
		invalidateCachedOffsets();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {

		if (DEBUG)
			Log.d(TAG, "PagedView.onLayout()");
		final int verticalPadding = getPaddingTop() + getPaddingBottom();
		final int childCount = getChildCount();
		int childLeft = 0;
		if (childCount > 0) {
			if (DEBUG)
				Log.d(TAG, "getRelativeChildOffset(): " + getMeasuredWidth()
						+ ", " + getChildWidth(0));
			childLeft = getRelativeChildOffset(0);

			// Calculate the variable page spacing if necessary
			if (mPageSpacing < 0) {
				setPageSpacing(((right - left) - getChildAt(0)
						.getMeasuredWidth()) / 2);
			}
		}

		for (int i = 0; i < childCount; i++) {
			final View child = getPageAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = getScaledMeasuredWidth(child);
				final int childHeight = child.getMeasuredHeight();
				int childTop = getPaddingTop();
				if (mCenterPagesVertically) {
					childTop += ((getMeasuredHeight() - verticalPadding) - childHeight) / 2;
				}

				if (DEBUG)
					Log.d(TAG, "\tlayout-child" + i + ": " + childLeft + ", "
							+ childTop);
				child.layout(childLeft, childTop,
						childLeft + child.getMeasuredWidth(), childTop
								+ childHeight);
				childLeft += childWidth + mPageSpacing;
			}
		}

		if (mFirstLayout && mCurrentPage >= 0 && mCurrentPage < getChildCount()) {
			setHorizontalScrollBarEnabled(false);
			int newX = getChildOffset(mCurrentPage)
					- getRelativeChildOffset(mCurrentPage);
			scrollTo(newX, 0);
			mScroller.setFinalX(newX);
			setHorizontalScrollBarEnabled(true);
			Log.w(TAG, "cancelling first layout flag and updating tab");
			updateCurrentTab(mCurrentPage);
			mFirstLayout = false;
		}
	}

	protected void invalidateCachedOffsets() {
		int count = getChildCount();
		if (count == 0) {
			mChildOffsets = null;
			mChildRelativeOffsets = null;
			mChildOffsetsWithLayoutScale = null;
			return;
		}

		mChildOffsets = new int[count];
		mChildRelativeOffsets = new int[count];
		mChildOffsetsWithLayoutScale = new int[count];
		for (int i = 0; i < count; i++) {
			mChildOffsets[i] = -1;
			mChildRelativeOffsets[i] = -1;
			mChildOffsetsWithLayoutScale[i] = -1;
		}
	}

	protected int getChildOffset(int index) {
		int[] childOffsets = Float.compare(mLayoutScale, 1f) == 0 ? mChildOffsets
				: mChildOffsetsWithLayoutScale;

		if (childOffsets != null && childOffsets[index] != -1) {
			return childOffsets[index];
		} else {
			if (getChildCount() == 0)
				return 0;

			int offset = getRelativeChildOffset(0);
			for (int i = 0; i < index; ++i) {
				offset += getScaledMeasuredWidth(getPageAt(i)) + mPageSpacing;
			}
			if (childOffsets != null) {
				childOffsets[index] = offset;
			}
			return offset;
		}
	}

	protected int getRelativeChildOffset(int index) {
		if (mChildRelativeOffsets != null && mChildRelativeOffsets[index] != -1) {
			return mChildRelativeOffsets[index];
		} else {
			final int padding = getPaddingLeft() + getPaddingRight();
			final int offset = getPaddingLeft()
					+ (getMeasuredWidth() - padding - getChildWidth(index)) / 2;
			if (mChildRelativeOffsets != null) {
				mChildRelativeOffsets[index] = offset;
			}
			return offset;
		}
	}

	protected int getScaledRelativeChildOffset(int index) {
		final int padding = getPaddingLeft() + getPaddingRight();
		final int offset = getPaddingLeft()
				+ (getMeasuredWidth() - padding - getScaledMeasuredWidth(getPageAt(index)))
				/ 2;
		return offset;
	}

	protected int getScaledMeasuredWidth(View child) {
		// This functions are called enough times that it actually makes a
		// difference in the
		// profiler -- so just inline the max() here
		final int measuredWidth = child.getMeasuredWidth();
		final int minWidth = mMinimumWidth;
		final int maxWidth = (minWidth > measuredWidth) ? minWidth
				: measuredWidth;
		return (int) (maxWidth * mLayoutScale + 0.5f);
	}

	protected void getVisiblePages(int[] range) {
		final int pageCount = getChildCount();
		if (pageCount > 0) {
			final int pageWidth = getScaledMeasuredWidth(getPageAt(0));
			final int screenWidth = getMeasuredWidth();
			int x = getScaledRelativeChildOffset(0) + pageWidth;
			int leftScreen = 0;
			int rightScreen = 0;
			while (x <= getScrollX() && leftScreen < pageCount - 1) {
				leftScreen++;
				x += getScaledMeasuredWidth(getPageAt(leftScreen))
						+ mPageSpacing;
			}
			rightScreen = leftScreen;
			while (x < getScrollX() + screenWidth
					&& rightScreen < pageCount - 1) {
				rightScreen++;
				x += getScaledMeasuredWidth(getPageAt(rightScreen))
						+ mPageSpacing;
			}
			range[0] = leftScreen;
			range[1] = rightScreen;
		} else {
			range[0] = -1;
			range[1] = -1;
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		int halfScreenSize = getMeasuredWidth() / 2;
		// mOverScrollX is equal to mScrollX when we're within the normal scroll
		// range. Otherwise
		// it is equal to the scaled overscroll position.
		int screenCenter = getScrollX() + halfScreenSize;

		if (screenCenter != mLastScreenCenter || mForceScreenScrolled) {
			screenScrolled(screenCenter);
			mLastScreenCenter = screenCenter;
			mForceScreenScrolled = false;
		}

		// Find out which screens are visible; as an optimization we only call
		// draw on them
		final int pageCount = getChildCount();
		if (pageCount > 0) {
			getVisiblePages(mTempVisiblePagesRange);
			final int leftScreen = mTempVisiblePagesRange[0];
			final int rightScreen = mTempVisiblePagesRange[1];
			if (leftScreen != -1 && rightScreen != -1) {
				final long drawingTime = getDrawingTime();
				// Clip to the bounds
				canvas.save();
				canvas.clipRect(getScrollX(), getScrollY(), getScrollX()
						+ getRight() - getLeft(), getScrollY() + getBottom()
						- getTop());

				for (int i = rightScreen; i >= leftScreen; i--) {
					drawChild(canvas, getPageAt(i), drawingTime);
				}
				canvas.restore();
			}
		}
	}

	@Override
	public boolean requestChildRectangleOnScreen(View child, Rect rectangle,
			boolean immediate) {
		int page = indexToPage(indexOfChild(child));
		if (page != mCurrentPage || !mScroller.isFinished()) {
			snapToPage(page);
			return true;
		}
		return false;
	}

	@Override
	protected boolean onRequestFocusInDescendants(int direction,
			Rect previouslyFocusedRect) {
		int focusablePage;
		if (mNextPage != INVALID_PAGE) {
			focusablePage = mNextPage;
		} else {
			focusablePage = mCurrentPage;
		}
		View v = getPageAt(focusablePage);
		if (v != null) {
			return v.requestFocus(direction, previouslyFocusedRect);
		}
		return false;
	}

	@Override
	public boolean dispatchUnhandledMove(View focused, int direction) {
		if (direction == View.FOCUS_LEFT) {
			if (getCurrentPage() > 0) {
				snapToPage(getCurrentPage() - 1);
				return true;
			}
		} else if (direction == View.FOCUS_RIGHT) {
			if (getCurrentPage() < getPageCount() - 1) {
				snapToPage(getCurrentPage() + 1);
				return true;
			}
		}
		return super.dispatchUnhandledMove(focused, direction);
	}

	@Override
	public void addFocusables(ArrayList<View> views, int direction,
			int focusableMode) {
		if (mCurrentPage >= 0 && mCurrentPage < getPageCount()) {
			getPageAt(mCurrentPage).addFocusables(views, direction);
		}
		if (direction == View.FOCUS_LEFT) {
			if (mCurrentPage > 0) {
				getPageAt(mCurrentPage - 1).addFocusables(views, direction);
			}
		} else if (direction == View.FOCUS_RIGHT) {
			if (mCurrentPage < getPageCount() - 1) {
				getPageAt(mCurrentPage + 1).addFocusables(views, direction);
			}
		}
	}

	/**
	 * If one of our descendant views decides that it could be focused now, only
	 * pass that along if it's on the current page.
	 * 
	 * This happens when live folders requery, and if they're off page, they end
	 * up calling requestFocus, which pulls it on page.
	 */
	@Override
	public void focusableViewAvailable(View focused) {
		View current = getPageAt(mCurrentPage);
		View v = focused;
		while (true) {
			if (v == current) {
				super.focusableViewAvailable(focused);
				return;
			}
			if (v == this) {
				return;
			}
			ViewParent parent = v.getParent();
			if (parent instanceof View) {
				v = (View) v.getParent();
			} else {
				return;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
		if (disallowIntercept) {
			// We need to make sure to cancel our long press if
			// a scrollable widget takes over touch events
			final View currentPage = getPageAt(mCurrentPage);
			currentPage.cancelLongPress();
		}
		super.requestDisallowInterceptTouchEvent(disallowIntercept);
	}

	/**
	 * Return true if a tap at (x, y) should trigger a flip to the previous
	 * page.
	 */
	protected boolean hitsPreviousPage(float x, float y) {
		return (x < getRelativeChildOffset(mCurrentPage) - mPageSpacing);
	}

	/**
	 * Return true if a tap at (x, y) should trigger a flip to the next page.
	 */
	protected boolean hitsNextPage(float x, float y) {
		return (x > (getMeasuredWidth() - getRelativeChildOffset(mCurrentPage) + mPageSpacing));
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		/*
		 * This method JUST determines whether we want to intercept the motion.
		 * If we return true, onTouchEvent will be called and we do the actual
		 * scrolling there.
		 */
		acquireVelocityTrackerAndAddMovement(ev);

		// Skip touch handling if there are no pages to swipe
		if (getChildCount() <= 0)
			return super.onInterceptTouchEvent(ev);

		/*
		 * Shortcut the most recurring case: the user is in the dragging state
		 * and he is moving his finger. We want to intercept this motion.
		 */
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE)
				&& (mTouchState == TOUCH_STATE_SCROLLING)) {
			return true;
		}

		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_MOVE: {
			/*
			 * mIsBeingDragged == false, otherwise the shortcut would have
			 * caught it. Check whether the user has moved far enough from his
			 * original down touch.
			 */
			if (mActivePointerId != INVALID_POINTER) {
				determineScrollingStart(ev, 1.0f);
				break;
			}
			// if mActivePointerId is INVALID_POINTER, then we must have missed
			// an ACTION_DOWN
			// event. in that case, treat the first occurence of a move event as
			// a ACTION_DOWN
			// i.e. fall through to the next case (don't break)
			// (We sometimes miss ACTION_DOWN events in Workspace because it
			// ignores all events
			// while it's small- this was causing a crash before we checked for
			// INVALID_POINTER)
		}

		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX();
			final float y = ev.getY();
			// Remember location of down touch
			mDownMotionX = x;
			mLastMotionX = x;
			mLastMotionY = y;
			mLastMotionXRemainder = 0;
			mTotalMotionX = 0;
			mActivePointerId = ev.getPointerId(0);

			/*
			 * If being flinged and user touches the screen, initiate drag;
			 * otherwise don't. mScroller.isFinished should be false when being
			 * flinged.
			 */
			final int xDist = Math.abs(mScroller.getFinalX()
					- mScroller.getCurrX());
			final boolean finishedScrolling = (mScroller.isFinished() || xDist < mTouchSlop);
			if (finishedScrolling) {
				mTouchState = TOUCH_STATE_REST;
				mScroller.abortAnimation();
			} else {
				mTouchState = TOUCH_STATE_SCROLLING;
			}

			// check if this can be the beginning of a tap on the side of the
			// pages
			// to scroll the current page
			if (mTouchState != TOUCH_STATE_PREV_PAGE
					&& mTouchState != TOUCH_STATE_NEXT_PAGE) {
				if (getChildCount() > 0) {
					if (hitsPreviousPage(x, y)) {
						mTouchState = TOUCH_STATE_PREV_PAGE;
					} else if (hitsNextPage(x, y)) {
						mTouchState = TOUCH_STATE_NEXT_PAGE;
					}
				}
			}
			break;
		}

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
			mActivePointerId = INVALID_POINTER;
			releaseVelocityTracker();
			break;

		case MotionEvent.ACTION_POINTER_UP:
			onSecondaryPointerUp(ev);
			releaseVelocityTracker();
			break;
		}

		/*
		 * The only time we want to intercept motion events is if we are in the
		 * drag mode.
		 */
		return mTouchState != TOUCH_STATE_REST;
	}

	/*
	 * Determines if we should change the touch state to start scrolling after
	 * the user moves their touch point too far.
	 */
	protected void determineScrollingStart(MotionEvent ev, float touchSlopScale) {
		/*
		 * Locally do absolute value. mLastMotionX is set to the y value of the
		 * down event.
		 */
		final int pointerIndex = ev.findPointerIndex(mActivePointerId);
		if (pointerIndex == -1) {
			return;
		}
		final float x = ev.getX(pointerIndex);
		final float y = ev.getY(pointerIndex);
		final int xDiff = (int) Math.abs(x - mLastMotionX);
		final int yDiff = (int) Math.abs(y - mLastMotionY);

		final int touchSlop = Math.round(touchSlopScale * mTouchSlop);
		boolean xPaged = xDiff > mPagingTouchSlop;
		boolean xMoved = xDiff > touchSlop;
		boolean yMoved = yDiff > touchSlop;

		if (xMoved || xPaged || yMoved) {
			if (mUsePagingTouchSlop ? xPaged : xMoved) {
				// Scroll if the user moved far enough along the X axis
				mTouchState = TOUCH_STATE_SCROLLING;
				mTotalMotionX += Math.abs(mLastMotionX - x);
				mLastMotionX = x;
				mLastMotionXRemainder = 0;
				mTouchX = getScrollX();
				mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
				pageBeginMoving();
			}
		}
	}

	protected float getScrollProgress(int screenCenter, View v, int page) {
		final int halfScreenSize = getMeasuredWidth() / 2;

		int totalDistance = getScaledMeasuredWidth(v) + mPageSpacing;
		int delta = screenCenter
				- (getChildOffset(page) - getRelativeChildOffset(page) + halfScreenSize);

		float scrollProgress = delta / (totalDistance * 1.0f);
		scrollProgress = Math.min(scrollProgress, 1.0f);
		scrollProgress = Math.max(scrollProgress, -1.0f);
		return scrollProgress;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// Skip touch handling if there are no pages to swipe
		if (getChildCount() <= 0)
			return super.onTouchEvent(ev);

		acquireVelocityTrackerAndAddMovement(ev);

		final int action = ev.getAction();

		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			// Remember where the motion event started
			mDownMotionX = mLastMotionX = ev.getX();
			mLastMotionXRemainder = 0;
			mTotalMotionX = 0;
			mActivePointerId = ev.getPointerId(0);
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				pageBeginMoving();
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				// Scroll to follow the motion event
				final int pointerIndex = ev.findPointerIndex(mActivePointerId);
				final float x = ev.getX(pointerIndex);
				final float deltaX = mLastMotionX + mLastMotionXRemainder - x;

				mTotalMotionX += Math.abs(deltaX);

				// Only scroll and update mLastMotionX if we have moved some
				// discrete amount. We
				// keep the remainder because we are actually testing if we've
				// moved from the last
				// scrolled position (which is discrete).
				if (Math.abs(deltaX) >= 1.0f) {
					mTouchX += deltaX;
					mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
					if (!mDeferScrollUpdate) {
						scrollBy((int) deltaX, 0);
						if (DEBUG)
							Log.d(TAG, "onTouchEvent().Scrolling: " + deltaX);
					} else {
						invalidate();
					}
					mLastMotionX = x;
					mLastMotionXRemainder = deltaX - (int) deltaX;
				} else {

				}
			} else {
				determineScrollingStart(ev, 1.0f);
			}
			break;

		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final int activePointerId = mActivePointerId;
				final int pointerIndex = ev.findPointerIndex(activePointerId);
				final float x = ev.getX(pointerIndex);
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int velocityX = (int) velocityTracker
						.getXVelocity(activePointerId);
				final int deltaX = (int) (x - mDownMotionX);
				final int pageWidth = getScaledMeasuredWidth(getPageAt(mCurrentPage));
				boolean isSignificantMove = Math.abs(deltaX) > pageWidth
						* SIGNIFICANT_MOVE_THRESHOLD;
				final int snapVelocity = mSnapVelocity;

				mTotalMotionX += Math.abs(mLastMotionX + mLastMotionXRemainder
						- x);

				boolean isFling = mTotalMotionX > MIN_LENGTH_FOR_FLING
						&& Math.abs(velocityX) > snapVelocity;

				// In the case that the page is moved far to one direction and
				// then is flung
				// in the opposite direction, we use a threshold to determine
				// whether we should
				// just return to the starting page, or if we should skip one
				// further.
				boolean returnToOriginalPage = false;
				if (Math.abs(deltaX) > pageWidth
						* RETURN_TO_ORIGINAL_PAGE_THRESHOLD
						&& Math.signum(velocityX) != Math.signum(deltaX)
						&& isFling) {
					returnToOriginalPage = true;
				}

				int finalPage;
				// We give flings precedence over large moves, which is why we
				// short-circuit our
				// test for a large move if a fling has been registered. That
				// is, a large
				// move to the left and fling to the right will register as a
				// fling to the right.
				if (((isSignificantMove && deltaX > 0 && !isFling) || (isFling && velocityX > 0))
						&& mCurrentPage > 0) {
					finalPage = returnToOriginalPage ? mCurrentPage
							: mCurrentPage - 1;
					snapToPageWithVelocity(finalPage, velocityX);
				} else if (((isSignificantMove && deltaX < 0 && !isFling) || (isFling && velocityX < 0))
						&& mCurrentPage < getChildCount() - 1) {
					finalPage = returnToOriginalPage ? mCurrentPage
							: mCurrentPage + 1;
					snapToPageWithVelocity(finalPage, velocityX);
				} else {
					snapToDestination();
				}
			} else if (mTouchState == TOUCH_STATE_PREV_PAGE) {
				// at this point we have not moved beyond the touch slop
				// (otherwise mTouchState would be TOUCH_STATE_SCROLLING), so
				// we can just page
				int nextPage = Math.max(0, mCurrentPage - 1);
				if (nextPage != mCurrentPage) {
					snapToPage(nextPage);
				} else {
					snapToDestination();
				}
			} else if (mTouchState == TOUCH_STATE_NEXT_PAGE) {
				// at this point we have not moved beyond the touch slop
				// (otherwise mTouchState would be TOUCH_STATE_SCROLLING), so
				// we can just page
				int nextPage = Math.min(getChildCount() - 1, mCurrentPage + 1);
				if (nextPage != mCurrentPage) {
					snapToPage(nextPage);
				} else {
					snapToDestination();
				}
			} else {
				onUnhandledTap(ev);
			}
			mTouchState = TOUCH_STATE_REST;
			mActivePointerId = INVALID_POINTER;
			releaseVelocityTracker();
			break;

		case MotionEvent.ACTION_CANCEL:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				snapToDestination();
			}
			mTouchState = TOUCH_STATE_REST;
			mActivePointerId = INVALID_POINTER;
			releaseVelocityTracker();
			break;

		case MotionEvent.ACTION_POINTER_UP:
			onSecondaryPointerUp(ev);
			break;
		}

		return true;
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_SCROLL: {
				// Handle mouse (or ext. device) by shifting the page depending
				// on the scroll
				final float vscroll;
				final float hscroll;
				if ((event.getMetaState() & KeyEvent.META_SHIFT_ON) != 0) {
					vscroll = 0;
					hscroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
				} else {
					vscroll = -event.getAxisValue(MotionEvent.AXIS_VSCROLL);
					hscroll = event.getAxisValue(MotionEvent.AXIS_HSCROLL);
				}
				if (hscroll != 0 || vscroll != 0) {
					if (hscroll > 0 || vscroll > 0) {
						scrollRight();
					} else {
						scrollLeft();
					}
					return true;
				}
			}
			}
		}
		return super.onGenericMotionEvent(event);
	}

	private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);
	}

	private void releaseVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	private void onSecondaryPointerUp(MotionEvent ev) {
		final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		final int pointerId = ev.getPointerId(pointerIndex);
		if (pointerId == mActivePointerId) {
			// This was our active pointer going up. Choose a new
			// active pointer and adjust accordingly.
			// TODO: Make this decision more intelligent.
			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			mLastMotionX = mDownMotionX = ev.getX(newPointerIndex);
			mLastMotionY = ev.getY(newPointerIndex);
			mLastMotionXRemainder = 0;
			mActivePointerId = ev.getPointerId(newPointerIndex);
			if (mVelocityTracker != null) {
				mVelocityTracker.clear();
			}
		}
	}

	@Override
	public void requestChildFocus(View child, View focused) {
		super.requestChildFocus(child, focused);
		int page = indexToPage(indexOfChild(child));
		if (page >= 0 && page != getCurrentPage() && !isInTouchMode()) {
			snapToPage(page);
		}
	}

	protected int getChildWidth(int index) {
		// This functions are called enough times that it actually makes a
		// difference in the
		// profiler -- so just inline the max() here
		final int measuredWidth = getPageAt(index).getMeasuredWidth();
		final int minWidth = mMinimumWidth;
		return (minWidth > measuredWidth) ? minWidth : measuredWidth;
	}

	int getPageNearestToCenterOfScreen() {
		int minDistanceFromScreenCenter = Integer.MAX_VALUE;
		int minDistanceFromScreenCenterIndex = -1;
		int screenCenter = getScrollX() + (getMeasuredWidth() / 2);
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; ++i) {
			View layout = (View) getPageAt(i);
			int childWidth = getScaledMeasuredWidth(layout);
			int halfChildWidth = (childWidth / 2);
			int childCenter = getChildOffset(i) + halfChildWidth;
			int distanceFromScreenCenter = Math.abs(childCenter - screenCenter);
			if (distanceFromScreenCenter < minDistanceFromScreenCenter) {
				minDistanceFromScreenCenter = distanceFromScreenCenter;
				minDistanceFromScreenCenterIndex = i;
			}
		}
		return minDistanceFromScreenCenterIndex;
	}

	protected void snapToDestination() {
		snapToPage(getPageNearestToCenterOfScreen(),
				PAGE_SNAP_ANIMATION_DURATION);
	}

	// We want the duration of the page snap animation to be influenced by the
	// distance that
	// the screen has to travel, however, we don't want this duration to be
	// effected in a
	// purely linear fashion. Instead, we use this method to moderate the effect
	// that the distance
	// of travel has on the overall snap duration.
	float distanceInfluenceForSnapDuration(float f) {
		f -= 0.5f; // center the values about 0.
		f *= 0.3f * Math.PI / 2.0f;
		return (float) Math.sin(f);
	}

	protected void snapToPageWithVelocity(int whichPage, int velocity) {
		whichPage = Math.max(0, Math.min(whichPage, getChildCount() - 1));
		int halfScreenSize = getMeasuredWidth() / 2;

		if (DEBUG)
			Log.d(TAG, "snapToPage.getChildOffset(): "
					+ getChildOffset(whichPage));
		if (DEBUG)
			Log.d(TAG, "snapToPageWithVelocity.getRelativeChildOffset(): "
					+ getMeasuredWidth() + ", " + getChildWidth(whichPage));
		final int newX = getChildOffset(whichPage)
				- getRelativeChildOffset(whichPage);
		int delta = newX - mUnboundedScrollX;
		int duration = 0;

		if (Math.abs(velocity) < MIN_FLING_VELOCITY) {
			// If the velocity is low enough, then treat this more as an
			// automatic page advance
			// as opposed to an apparent physical response to flinging
			snapToPage(whichPage, PAGE_SNAP_ANIMATION_DURATION);
			return;
		}

		// Here we compute a "distance" that will be used in the computation of
		// the overall
		// snap duration. This is a function of the actual distance that needs
		// to be traveled;
		// we keep this value close to half screen size in order to reduce the
		// variance in snap
		// duration as a function of the distance the page needs to travel.
		float distanceRatio = Math.min(1f, 1.0f * Math.abs(delta)
				/ (2 * halfScreenSize));
		float distance = halfScreenSize + halfScreenSize
				* distanceInfluenceForSnapDuration(distanceRatio);

		velocity = Math.abs(velocity);
		velocity = Math.max(MINIMUM_SNAP_VELOCITY, velocity);

		// we want the page's snap velocity to approximately match the velocity
		// at which the
		// user flings, so we scale the duration by a value near to the
		// derivative of the scroll
		// interpolator at zero, ie. 5. We use 4 to make it a little slower.
		duration = 4 * Math.round(1000 * Math.abs(distance / velocity));

		snapToPage(whichPage, delta, duration);
	}

	public void scrollLeft() {
		if (mScroller.isFinished()) {
			if (mCurrentPage > 0)
				snapToPage(mCurrentPage - 1);
		} else {
			if (mNextPage > 0)
				snapToPage(mNextPage - 1);
		}
	}

	public void scrollRight() {
		if (mScroller.isFinished()) {
			if (mCurrentPage < getChildCount() - 1)
				snapToPage(mCurrentPage + 1);
		} else {
			if (mNextPage < getChildCount() - 1)
				snapToPage(mNextPage + 1);
		}
	}

	public int getPageForView(View v) {
		int result = -1;
		if (v != null) {
			ViewParent vp = v.getParent();
			int count = getChildCount();
			for (int i = 0; i < count; i++) {
				if (vp == getPageAt(i)) {
					return i;
				}
			}
		}
		return result;
	}

	protected void invalidatePageData() {
		invalidatePageData(-1, false);
	}

	protected void invalidatePageData(int currentPage) {
		invalidatePageData(currentPage, false);
	}

	protected void invalidatePageData(int currentPage, boolean immediateAndOnly) {

		// Force all scrolling-related behavior to end
		mScroller.forceFinished(true);
		mNextPage = INVALID_PAGE;

		// We must force a measure after we've loaded the pages to update the
		// content width and
		// to determine the full scroll width
		measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(),
				MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
				getMeasuredHeight(), MeasureSpec.EXACTLY));

		// Set a new page as the current page if necessary
		if (currentPage > -1) {
			setCurrentPage(Math.min(getPageCount() - 1, currentPage));
		}

		// Mark each of the pages as dirty
		final int count = getChildCount();
		mDirtyPageContent.clear();
		for (int i = 0; i < count; ++i) {
			mDirtyPageContent.add(true);
		}
		updatePageCountText();
		requestLayout();

	}

	/* Accessibility */
	@Override
	public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setScrollable(true);
	}

	@Override
	public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setScrollable(true);
		if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
			event.setFromIndex(mCurrentPage);
			event.setToIndex(mCurrentPage);
			event.setItemCount(getChildCount());
		}
	}

	@Override
	public boolean onHoverEvent(android.view.MotionEvent event) {
		return true;
	}

	private static class ScrollInterpolator implements Interpolator {
		public ScrollInterpolator() {
		}

		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t * t * t + 1;
		}
	}

	/*
	 * This interpolator emulates the rate at which the perceived scale of an
	 * object changes as its distance from a camera increases. When this
	 * interpolator is applied to a scale animation on a view, it evokes the
	 * sense that the object is shrinking due to moving away from the camera.
	 */
	static class ZInterpolator implements TimeInterpolator {
		private float focalLength;

		public ZInterpolator(float foc) {
			focalLength = foc;
		}

		public float getInterpolation(float input) {
			return (1.0f - focalLength / (focalLength + input))
					/ (1.0f - focalLength / (focalLength + 1.0f));
		}
	}

}
