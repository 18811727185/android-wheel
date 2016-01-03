package com.letv.shared.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.letv.shared.widget.SlidingMenu.OnClosedListener;
import com.letv.shared.widget.SlidingMenu.OnOpenedListener;

public class CustomViewAbove extends ViewGroup {

    private static final String TAG = "CustomViewAbove";
    private static final boolean DEBUG = false;
    private static final boolean USE_CACHE = false;
    private static final int INVALID_POINTER = -1;
    private static final int MAX_SETTLE_DURATION = 600; // ms
    private static final int MIN_DISTANCE_FOR_FLING = 25; // dips
    
    private View mContent;
    private int mCurItem;
    private Scroller mScroller;
    private boolean mScrollingCacheEnabled;
    private boolean mScrolling;
    private boolean mIsBeingDragged;
    private boolean mIsUnableToDrag;
    private int mTouchSlop;// 触发最小距离
    private float mInitialMotionX;
    private float mLastMotionX;
    private float mLastMotionY;
    private int mActivePointerId = INVALID_POINTER;
    protected VelocityTracker mVelocityTracker;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mFlingDistance;
    private boolean mEnabled = true;// 能否滑动 
    private CustomViewBehind mViewBehind;
    private OnPageChangeListener mOnPageChangeListener;
    private OnPageChangeListener mInternalPageChangeListener;
    private OnClosedListener mClosedListener;
    private OnOpenedListener mOpenedListener;
    private List<View> mIgnoredViews = new ArrayList<View>();
    private int mViewOffsetLeft = 0;
    
    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    /**
     * Callback interface for responding to changing state of the selected page.
     */
    public interface OnPageChangeListener {

        /**
         * This method will be invoked when the current page is scrolled, either
         * as part of a programmatically initiated smooth scroll or a user
         * initiated touch scroll.
         * 
         * @param position Position index of the first page currently being
         *            displayed. Page position+1 will be visible if
         *            positionOffset is nonzero.
         * @param positionOffset Value from [0, 1) indicating the offset from
         *            the page at position.
         * @param positionOffsetPixels Value in pixels indicating the offset
         *            from position.
         */
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        /**
         * This method will be invoked when a new page becomes selected.
         * Animation is not necessarily complete.
         * 
         * @param position Position index of the new selected page.
         */
        public void onPageSelected(int position);

    }

    /**
     * Simple implementation of the {@link com.letv.shared.widget.CustomViewAbove.OnPageChangeListener} interface with
     * stub implementations of each method. Extend this if you do not intend to
     * override every method of {@link com.letv.shared.widget.CustomViewAbove.OnPageChangeListener}.
     */
    public static class SimpleOnPageChangeListener implements OnPageChangeListener {

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // This space for rent
        }

        public void onPageSelected(int position) {
            // This space for rent
        }

        public void onPageScrollStateChanged(int state) {
            // This space for rent
        }

    }

    public CustomViewAbove(Context context) {
        this(context, null);
    }

    public CustomViewAbove(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCustomViewAbove();
    }

    void initCustomViewAbove() {
        setWillNotDraw(false);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setFocusable(true);
        final Context context = getContext();
        mScroller = new Scroller(context, sInterpolator);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledPagingTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        setInternalPageChangeListener(new SimpleOnPageChangeListener() {
            public void onPageSelected(int position) {
                if (mViewBehind != null) {
                    switch (position) {
                        case 0:
                        case 2:
                            mViewBehind.setChildrenEnabled(true);
                            break;
                        case 1:
                            mViewBehind.setChildrenEnabled(true);
                            break;
                    }
                }
            }
        });

        final float density = context.getResources().getDisplayMetrics().density;
        mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);
    }
    
    /**
     * 获取窗口背景
     * 
     * @return resId
     */
    private int getWindowBackgroud(){
        // get the window background
        TypedArray a = getContext().getTheme().obtainStyledAttributes(new int[]
        {
                android.R.attr.windowBackground
        });
        int background = a.getResourceId(0, 0);
        a.recycle();
        
        return background;
    }
    
    /**
     * 缺省情况下，view的背景同窗口背景
     * 
     * @param view
     * @param background
     */
    private void setDefaultBackgroud(View view, int background){
        Drawable drawable = view.getBackground();
        if(drawable == null & view != null){
            view.setBackgroundResource(background);
        }
    }

    /**
     * Set the currently selected page. If the CustomViewPager has already been
     * through its first layout there will be a smooth animated transition
     * between the current item and the specified item.
     * 
     * @param item Item index to select 0为左侧边栏，1为主界面，2为右侧边栏
     */
    public void setCurrentItem(int item) {
        setCurrentItemInternal(item, true, false);
    }

    /**
     * Set the currently selected page.
     * 
     * @param item Item index to select
     * @param smoothScroll True to smoothly scroll to the new item, false to
     *            transition immediately
     */
    public void setCurrentItem(int item, boolean smoothScroll) {
        setCurrentItemInternal(item, smoothScroll, false);
    }
    
    /**
     * 设置当前显示界面
     * 
     * @param item: 0为左侧边栏，1为主界面，2为右侧边栏
     * @param smoothScroll：动画效果
     * @param always：false 如果当前item已是所要设置的item，则无须执行
     *                true 即便当前item已是所要设置的item，仍重新执行
     */
    public void setCurrentItem(int item, boolean smoothScroll, boolean always) {
        setCurrentItemInternal(item, smoothScroll, always);
    }

    public int getCurrentItem() {
        return mCurItem;
    }

    void setCurrentItemInternal(int item, boolean smoothScroll, boolean always) {
        setCurrentItemInternal(item, smoothScroll, always, 0);
    }

    void setCurrentItemInternal(int item, boolean smoothScroll, boolean always, int velocity) {
        if (!always && mCurItem == item) {
            setScrollingCacheEnabled(false);
            return;
        }

        item = mViewBehind.getMenuPage(item);

        final boolean dispatchSelected = mCurItem != item;
        mCurItem = item;
        final int destX = getDestScrollX(mCurItem);
        if (dispatchSelected && mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(item);
        }
        if (dispatchSelected && mInternalPageChangeListener != null) {
            mInternalPageChangeListener.onPageSelected(item);
        }
        if (smoothScroll) {
            smoothScrollTo(destX, 0, velocity);
        } else {
            completeScroll();
            scrollTo(destX, 0);
        }
    }

    /**
     * Set a listener that will be invoked whenever the page changes or is
     * incrementally scrolled. See {@link com.letv.shared.widget.CustomViewAbove.OnPageChangeListener}.
     * 
     * @param listener Listener to set
     */
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    public void setOnOpenedListener(OnOpenedListener l) {
        mOpenedListener = l;
    }

    public void setOnClosedListener(OnClosedListener l) {
        mClosedListener = l;
    }

    /**
     * Set a separate OnPageChangeListener for internal use by the support
     * library.
     * 
     * @param listener Listener to set
     * @return The old listener that was set, if any.
     */
    OnPageChangeListener setInternalPageChangeListener(OnPageChangeListener listener) {
        OnPageChangeListener oldListener = mInternalPageChangeListener;
        mInternalPageChangeListener = listener;
        return oldListener;
    }

    /**
     * 添加需要屏蔽Touch事件的View，在不处理该view上的touch事件
     * @param v
     */
    public void addIgnoredView(View v) {
        if (!mIgnoredViews.contains(v)) {
            mIgnoredViews.add(v);
        }
    }

    public void removeIgnoredView(View v) {
        mIgnoredViews.remove(v);
    }

    public void clearIgnoredViews() {
        mIgnoredViews.clear();
    }

    // We want the duration of the page snap animation to be influenced by the
    // distance that
    // the screen has to travel, however, we don't want this duration to be
    // effected in a
    // purely linear fashion. Instead, we use this method to moderate the effect
    // that the distance
    // of travel has on the overall snap duration.
    @SuppressLint("FloatMath")
    float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) FloatMath.sin(f);
    }

    public int getDestScrollX(int page) {
        switch (page) {
            case 0:
            case 2:
                return mViewBehind.getMenuLeft(mContent, page);
            case 1:
                return mContent.getLeft();
        }
        return 0;
    }

    public int getLeftBound() {
        return mViewBehind.getAbsLeftBound(mContent);
    }

    public int getRightBound() {
        return mViewBehind.getAbsRightBound(mContent);
    }

    public int getContentLeft() {
        return mContent.getLeft() + mContent.getPaddingLeft();
    }

    public boolean isMenuOpen() {
        return mCurItem == 0 || mCurItem == 2;
    }

    /**
     * MotionEvent是否在view的范围内
     * 
     * @param ev
     * @return true在，false不在
     */
    private boolean isInIgnoredView(MotionEvent ev) {
        Rect rect = new Rect();
        for (View v : mIgnoredViews) {
            v.getHitRect(rect);
            if (rect.contains((int) ev.getX(), (int) ev.getY()))
                return true;
        }
        return false;
    }

    /**
     * 判断MotionEvent 是否在主界面的范围内
     * 
     * @param ev
     * @return true在，false不在
     */
    private boolean isInternalContentView(MotionEvent ev) {
        Rect rect = new Rect();
        mContent.getHitRect(rect);
        if (rect.contains((int) ev.getX(), (int) ev.getY())) {
            return true;
        }
        return false;
    }

    public int getBehindWidth() {
        if (mViewBehind == null) {
            return 0;
        } else {
            return mViewBehind.getBehindWidth();
        }
    }

    public int getChildWidth(int i) {
        switch (i) {
            case 0:
                return getBehindWidth();
            case 1:
                return mContent.getWidth();
            default:
                return 0;
        }
    }

    public boolean isSlidingEnabled() {
        return mEnabled;
    }

    public void setSlidingEnabled(boolean b) {
        mEnabled = b;
    }

    /**
     * Like {@link android.view.View#scrollBy}, but scroll smoothly instead of immediately.
     * 
     * @param x the number of pixels to scroll by on the X axis
     * @param y the number of pixels to scroll by on the Y axis
     */
    void smoothScrollTo(int x, int y) {
        smoothScrollTo(x, y, 0);
    }

    /**
     * Like {@link android.view.View#scrollBy}, but scroll smoothly instead of immediately.
     * 
     * @param x the number of pixels to scroll by on the X axis
     * @param y the number of pixels to scroll by on the Y axis
     * @param velocity the velocity associated with a fling, if applicable. (0
     *            otherwise)
     */
    void smoothScrollTo(int x, int y, int velocity) {
        if (getChildCount() == 0) {
            // Nothing to do.
            setScrollingCacheEnabled(false);
            return;
        }
        int sx = getScrollX();
        int sy = getScrollY();
        int dx = x - sx;
        int dy = y - sy;
        if (dx == 0 && dy == 0) {
            completeScroll();
            if (isMenuOpen()) {
                if (mOpenedListener != null)
                    mOpenedListener.onOpened();
            } else {
                if (mClosedListener != null)
                    mClosedListener.onClosed();
            }
            return;
        }

        setScrollingCacheEnabled(true);
        mScrolling = true;

        final int width = getBehindWidth();
        final int halfWidth = width / 2;
        final float distanceRatio = Math.min(1f, 1.0f * Math.abs(dx) / width);
        final float distance = halfWidth + halfWidth *
                distanceInfluenceForSnapDuration(distanceRatio);

        int duration = 0;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            final float pageDelta = (float) Math.abs(dx) / width;
            duration = (int) ((pageDelta + 1) * 100);
            duration = MAX_SETTLE_DURATION;
        }
        duration = Math.min(duration, MAX_SETTLE_DURATION);

        mScroller.startScroll(sx, sy, dx, dy, duration);
        invalidate();
    }

    public void setContent(View v) {
        if (mContent != null)
            this.removeView(mContent);
        mContent = v;
        addView(mContent,LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		 //无背景情况下，设置Content背景同窗口背景
        setDefaultBackgroud(mContent, getWindowBackgroud());
    }

    public View getContent() {
        return mContent;
    }

    public void setCustomViewBehind(CustomViewBehind cvb) {
        mViewBehind = cvb;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);

        final int contentWidth = getChildMeasureSpec(widthMeasureSpec, 0, width);
        final int contentHeight = getChildMeasureSpec(heightMeasureSpec, 0, height);
        mContent.measure(contentWidth, contentHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Make sure scroll position is set correctly.
        if (w != oldw) {
            // [ChrisJ] - This fixes the onConfiguration change for orientation
            // issue..
            // maybe worth having a look why the recomputeScroll pos is screwing
            // up?
            completeScroll();
            scrollTo(getDestScrollX(mCurItem), getScrollY());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = mContent.getMeasuredWidth();
        final int height = mContent.getMeasuredHeight();
        mContent.layout(0, 0, width, height);
    }

    /**
     * 设置主界面与父控件左边边距
     * 
     * @param value
     */
    public void setAboveOffsetLeft(int value) {
        mViewBehind.setVisibleAlways((value > 0? true : false));
        mViewOffsetLeft = value;
        requestLayout();
    }

    public int getAboveOffsetLeft() {
        return this.mViewOffsetLeft;
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished()) {
            if (mScroller.computeScrollOffset()) {
                int oldX = getScrollX();
                int oldY = getScrollY();
                int x = mScroller.getCurrX();
                int y = mScroller.getCurrY();

                if (oldX != x || oldY != y) {
                    scrollTo(x, y);
                    pageScrolled(x);
                }

                // Keep on drawing until the animation has finished.
                invalidate();
                return;
            }
        }

        // Done with scroll, clean up state.
        completeScroll();
    }

    public void pageScrolled(int xpos) {
        final int widthWithMargin = getWidth();
        final int position = xpos / widthWithMargin;
        final int offsetPixels = xpos % widthWithMargin;
        final float offset = (float) offsetPixels / widthWithMargin;

        onPageScrolled(position, offset, offsetPixels);
    }

    /**
     * This method will be invoked when the current page is scrolled, either as
     * part of a programmatically initiated smooth scroll or a user initiated
     * touch scroll. If you override this method you must call through to the
     * superclass implementation (e.g. super.onPageScrolled(position, offset,
     * offsetPixels)) before onPageScrolled returns.
     * 
     * @param position Position index of the first page currently being
     *            displayed. Page position+1 will be visible if positionOffset
     *            is nonzero.
     * @param offset Value from [0, 1) indicating the offset from the page at
     *            position.
     * @param offsetPixels Value in pixels indicating the offset from position.
     */
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrolled(position, offset, offsetPixels);
        }
        if (mInternalPageChangeListener != null) {
            mInternalPageChangeListener.onPageScrolled(position, offset, offsetPixels);
        }
    }

    private void completeScroll() {
        boolean needPopulate = mScrolling;
        if (needPopulate) {
            // Done with scroll, no longer want to cache view drawing.
            setScrollingCacheEnabled(false);
            mScroller.abortAnimation();
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            if (oldX != x || oldY != y) {
                scrollTo(x, y);
            }
            if (isMenuOpen()) {
                if (mOpenedListener != null)
                    mOpenedListener.onOpened();
            } else {
                if (mClosedListener != null)
                    mClosedListener.onClosed();
            }
        }
        mScrolling = false;
    }

    protected int mTouchMode = SlidingMenu.TOUCHMODE_FULLSCREEN;

    public void setTouchMode(int i) {
        mTouchMode = i;
    }

    public int getTouchMode() {
        return mTouchMode;
    }

    /**
     * 是否处理MotionEvent
     * 
     * @param ev
     * @return true 处理，false 不处理
     */
    private boolean thisTouchAllowed(MotionEvent ev) {
        int x = (int) (ev.getX() + mScrollX);
        if (isMenuOpen()) {
            return mViewBehind.menuOpenTouchAllowed(mContent, mCurItem, x);
        } else {
            switch (mTouchMode) {
                case SlidingMenu.TOUCHMODE_FULLSCREEN:
                    //MotionEvent在主界面范围内且不在被屏蔽view的范围内，返回true
                    return !isInIgnoredView(ev) && isInternalContentView(ev);
                case SlidingMenu.TOUCHMODE_NONE:
                    return false;
                case SlidingMenu.TOUCHMODE_MARGIN:
                    return mViewBehind.marginTouchAllowed(mContent, x);
            }
        }
        return false;
    }

    /**
     * 判断是否允许向指定方向滑动: LEFT , RIGHT, LEFT_RIGHT
     * 
     * @param dx
     * @return true 允许，false 不允许
     */
    public boolean slideAllowed(float dx) {
        boolean allowed = false;
        if (isMenuOpen()) {
            allowed = mViewBehind.menuOpenSlideAllowed(dx);
        } else {
            allowed = mViewBehind.menuClosedSlideAllowed(dx);
        }
        if (DEBUG)
            Log.v(TAG, "this slide allowed " + allowed + " dx: " + dx);
        return allowed;
    }

    private int getPointerIndex(MotionEvent ev, int id) {
        int activePointerIndex = ev.findPointerIndex(id);
        if (activePointerIndex == -1)
            mActivePointerId = INVALID_POINTER;
        return activePointerIndex;
    }

    private boolean mQuickReturn = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // slidingmenu 滑动功能关闭，且侧边栏状态关闭的情况下，将touch事件交由子控件
        if (!mEnabled)
            return false;
        
        // CustomViewBehind在侧滑过程中，touch事件在该view将被拦截
        // 确保menu侧滑中，content中子view不能处理touch事件
        if(mViewBehind.IsBeingDragged()){
            mIsBeingDragged = false;
            return true;
        }

        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP
                || (action != MotionEvent.ACTION_DOWN && mIsUnableToDrag)) {
            endDrag();
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                // 在move事件中，当各项条件符合，则拦截事件，然后在onTouchEvent中处理事件，移动view
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER)
                    break;
                final int pointerIndex = this.getPointerIndex(ev, activePointerId);
                if (pointerIndex == INVALID_POINTER)
                    break;
                final float x = ev.getX(pointerIndex);
                final float dx = x - mLastMotionX;
                final float xDiff = Math.abs(dx);
                final float y = ev.getY(pointerIndex);
                final float yDiff = Math.abs(y - mLastMotionY);
                // 侧滑距离至少达到最小距离，侧滑距离比纵滑距离范围大，且当前未滑到边界
                // slideAllowed(dx)主要根据是否到达边缘来判断在该方向上能否继续滑动
                if (xDiff > mTouchSlop && xDiff > yDiff && slideAllowed(dx)) {
                    if (DEBUG)
                        Log.v(TAG, "Starting drag! from onInterceptTouch");
                    // 在该方法中将允许滑动标志位打开
                    startDrag();
                    mLastMotionX = x;
                    setScrollingCacheEnabled(true);
                } else if (yDiff > mTouchSlop) {
                    mIsUnableToDrag = true;
                }
                break;

            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getAction()
                        & ((Build.VERSION.SDK_INT >= 8) ? MotionEvent.ACTION_POINTER_INDEX_MASK :
                                MotionEvent.ACTION_POINTER_INDEX_MASK);
                mLastMotionX = mInitialMotionX = ev.getX(mActivePointerId);
                mLastMotionY = ev.getY(mActivePointerId);
                // 在down事件中不拦截事件，只初始化各个标志位
                if (thisTouchAllowed(ev)) {
                    mIsBeingDragged = false;
                    mIsUnableToDrag = false;
                    if (isMenuOpen()
                            && mViewBehind.menuTouchInQuickReturn(mContent, mCurItem, ev.getX()
                                    + mScrollX)) {
                        mQuickReturn = true;
                    }
                } else {
                    mIsUnableToDrag = true;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        if (!mIsBeingDragged) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(ev);
        }
        return mIsBeingDragged || mQuickReturn;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        
        // mEnabled == false,情况下，该view不拦截事件，如果子控件不处理touch事件，事件又会交由该view处理
        // 该情况下，仍要根据当前滚动距离判断是否处理事件，或是交由下一次view处理
        if (!mEnabled){
            if (!thisTouchAllowed(ev)){
                // 在CustomViewBehind可视区域内
                return false;
            }
            return true;
        }
        
        // 在Menu可视区域内，交由CustomViewBehind处理
        if (!mIsBeingDragged && !thisTouchAllowed(ev))
            return false;
        
        // 此处为确保menu侧滑中，content中子view不能处理touch事件，事件在该view被消耗掉
        if(mViewBehind.IsBeingDragged()){
            mIsBeingDragged = false;
            return false;
        }

        final int action = ev.getAction();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // 停止惯性滑动
                completeScroll();
                // 记录初始位置
                mLastMotionX = mInitialMotionX = ev.getX();
                mActivePointerId = ev.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsBeingDragged) {
                    if (mActivePointerId == INVALID_POINTER)
                        break;
                    final int pointerIndex = getPointerIndex(ev, mActivePointerId);
                    if (pointerIndex == INVALID_POINTER)
                        break;
                    final float x = ev.getX(pointerIndex);
                    final float dx = x - mLastMotionX;
                    final float xDiff = Math.abs(dx);
                    final float y = ev.getY(pointerIndex);
                    final float yDiff = Math.abs(y - mLastMotionY);
                   // 侧滑距离至少达到最小距离，侧滑距离比纵滑距离范围大，且当前未滑到边界
                   // slideAllowed(dx)主要根据是否到达边缘来判断在该方向上能否继续滑动
                    if ((xDiff > mTouchSlop || (mQuickReturn && xDiff > mTouchSlop / 4))
                            && xDiff > yDiff && slideAllowed(dx)) {
                        if (DEBUG)
                            Log.v(TAG, "Starting drag! from onTouch");
                        startDrag();
                        mLastMotionX = x;
                        setScrollingCacheEnabled(true);
                    } else {
                        if (DEBUG)
                            Log.v(TAG, "onTouch returning false");
                        return false;
                    }
                }
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    final int activePointerIndex = getPointerIndex(ev, mActivePointerId);
                    if (mActivePointerId == INVALID_POINTER) {
                        break;
                    }
                    final float x = ev.getX(activePointerIndex);
                    final float deltaX = mLastMotionX - x;
                    mLastMotionX = x;
                    float oldScrollX = getScrollX();
                    float scrollX = oldScrollX + deltaX;
                    //获取滑动边界线
                    final float leftBound = getLeftBound();
                    final float rightBound = getRightBound();
                    //滚动目标位置超出边界线，调整至边界线位置
                    if (scrollX < leftBound) {
                        scrollX = leftBound;
                    } else if (scrollX > rightBound) {
                        scrollX = rightBound;
                    }
                    // Don't lose the rounded component
                    mLastMotionX += scrollX - (int) scrollX;
                    scrollTo((int) scrollX, getScrollY());
                    //更新滚动状态，通知监听器
                    pageScrolled((int) scrollX);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getXVelocity(mActivePointerId);
                    final int scrollX = getScrollX();
                    final float pageOffset = (float) (scrollX - getDestScrollX(mCurItem))
                            / getBehindWidth();
                    final int activePointerIndex = getPointerIndex(ev, mActivePointerId);
                    if (mActivePointerId != INVALID_POINTER) {
                        final float x = ev.getX(activePointerIndex);
                        final int totalDelta = (int) (x - mInitialMotionX);
                        //根据手势速度和起始距离决定目标页面
                        int nextPage = determineTargetPage(pageOffset, initialVelocity, totalDelta);
                        setCurrentItemInternal(nextPage, true, true, initialVelocity);
                    } else {
                        setCurrentItemInternal(mCurItem, true, true, initialVelocity);
                    }
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                } else if (mQuickReturn
                        && mViewBehind.menuTouchInQuickReturn(mContent, mCurItem, ev.getX()
                                + mScrollX)) {
                    // close the menu
                    setCurrentItem(1);
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    setCurrentItemInternal(mCurItem, true, true);
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                final float x = ev.getX(index);
                mLastMotionX = x;
                mActivePointerId = ev.getPointerId(index);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                int pointerIndex = this.getPointerIndex(ev, mActivePointerId);
                if (mActivePointerId == INVALID_POINTER)
                    break;
                mLastMotionX = ev.getX(pointerIndex);
                break;
        }
        return true;
    }

    public boolean IsBeingDragged() {
        return mIsBeingDragged;
    }
    
    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        mScrollX = x;
        // 不需要判断是否能滑动，若CustomViewAbove能调用到scroll方法，则CustomViewBehind也跟随能scroll
        // if (mEnabled) 
        mViewBehind.scrollBehindTo(mContent, x, y);
        // ((SlidingMenu) getParent()).manageLayers(getPercentOpen());
    }

    int determineTargetPage(float pageOffset, int velocity, int deltaX) {
        int targetPage = mCurItem;
        if (Math.abs(deltaX) > mFlingDistance && Math.abs(velocity) > mMinimumVelocity) {
            if (velocity > 0 && deltaX > 0) {
                targetPage -= 1;
            } else if (velocity < 0 && deltaX < 0) {
                targetPage += 1;
            }
        } else {
            targetPage = (int) Math.round(mCurItem + pageOffset);
        }
        return targetPage;
    }

    protected float getPercentOpen() {
        return Math.abs(mScrollX - mContent.getLeft()) / getBehindWidth();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        // Draw the margin drawable if needed.
        // 滑动过程中重绘边缘阴影，淡出淡出阴影效果
        mViewBehind.drawShadow(mContent, canvas);
        mViewBehind.drawFade(mContent, canvas, getPercentOpen());
        mViewBehind.drawSelector(mContent, canvas, getPercentOpen());
    }

    // variables for drawing
    private float mScrollX = 0.0f;

    private void onSecondaryPointerUp(MotionEvent ev) {
        if (DEBUG)
            Log.v(TAG, "onSecondaryPointerUp called");
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private void startDrag() {
        mIsBeingDragged = true;
        mQuickReturn = false;
    }

    private void endDrag() {
        mQuickReturn = false;
        mIsBeingDragged = false;
        mIsUnableToDrag = false;
        mActivePointerId = INVALID_POINTER;

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void setScrollingCacheEnabled(boolean enabled) {
        if (mScrollingCacheEnabled != enabled) {
            mScrollingCacheEnabled = enabled;
            if (USE_CACHE) {
                final int size = getChildCount();
                for (int i = 0; i < size; ++i) {
                    final View child = getChildAt(i);
                    if (child.getVisibility() != GONE) {
                        child.setDrawingCacheEnabled(enabled);
                    }
                }
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Let the focused view and/or our descendants get the key first
        return super.dispatchKeyEvent(event) || executeKeyEvent(event);
    }

    /**
     * You can call this function yourself to have the scroll view perform
     * scrolling from a key event, just as if the event had been dispatched to
     * it by the view hierarchy.
     * 
     * @param event The key event to execute.
     * @return Return true if the event was handled, else false.
     */
    public boolean executeKeyEvent(KeyEvent event) {
        boolean handled = false;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    handled = arrowScroll(FOCUS_LEFT);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    handled = arrowScroll(FOCUS_RIGHT);
                    break;
                case KeyEvent.KEYCODE_TAB:
                    if (Build.VERSION.SDK_INT >= 11) {
                        // The focus finder had a bug handling FOCUS_FORWARD and
                        // FOCUS_BACKWARD
                        // before Android 3.0. Ignore the tab key on those
                        // devices.
                        if (event.hasNoModifiers()) {
                            handled = arrowScroll(FOCUS_FORWARD);
                        } else if (event.hasModifiers(KeyEvent.META_SHIFT_ON)) {
                            handled = arrowScroll(FOCUS_BACKWARD);
                        }
                    }
                    break;
            }
        }
        return handled;
    }

    public boolean arrowScroll(int direction) {
        View currentFocused = findFocus();
        if (currentFocused == this)
            currentFocused = null;

        boolean handled = false;

        View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused,
                direction);
        if (nextFocused != null && nextFocused != currentFocused) {
            if (direction == View.FOCUS_LEFT) {
                handled = nextFocused.requestFocus();
            } else if (direction == View.FOCUS_RIGHT) {
                // If there is nothing to the right, or this is causing us to
                // jump to the left, then what we really want to do is page
                // right.
                if (currentFocused != null && nextFocused.getLeft() <= currentFocused.getLeft()) {
                    handled = pageRight();
                } else {
                    handled = nextFocused.requestFocus();
                }
            }
        } else if (direction == FOCUS_LEFT || direction == FOCUS_BACKWARD) {
            // Trying to move left and nothing there; try to page.
            handled = pageLeft();
        } else if (direction == FOCUS_RIGHT || direction == FOCUS_FORWARD) {
            // Trying to move right and nothing there; try to page.
            handled = pageRight();
        }
        if (handled) {
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
        }
        return handled;
    }

    boolean pageLeft() {
        if (mCurItem > 0) {
            setCurrentItem(mCurItem - 1, true);
            return true;
        }
        return false;
    }

    boolean pageRight() {
        if (mCurItem < 1) {
            setCurrentItem(mCurItem + 1, true);
            return true;
        }
        return false;
    }

}
