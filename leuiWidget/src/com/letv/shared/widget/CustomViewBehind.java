package com.letv.shared.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.letv.shared.widget.SlidingMenu.CanvasTransformer;

public class CustomViewBehind extends ViewGroup {

    private static final String TAG  = "CustomViewBehind";
    private static final int MARGIN_THRESHOLD = 48;// dips
    private int mTouchMode = SlidingMenu.TOUCHMODE_MARGIN;
    private CustomViewAbove mViewAbove;
    private View mContent;
    private View mSecondaryContent;
    private int mMarginThreshold;
    private int mWidthOffset;
    private CanvasTransformer mTransformer;
    private boolean mChildrenEnabled;
    private boolean mVisibleAlways = false;
    private boolean mIsBeingDragged ;// 是否允许处理拖拉滚动
    private int mActivePointerId = INVALID_POINTER;
    private static final int INVALID_POINTER = -1;
    private float mLastMotionX;
    private float mLastMotionY;
    private int mTouchSlop;// 移动事件最小距离
    private VelocityTracker mVelocityTracker;
    //private int mMinimumVelocity;
    private int  mMaximumVelocity;
    private float mInitialMotionX; // 用来记录Touch事件的起点（down)位置
    private boolean mWidthChanged;

    public CustomViewBehind(Context context) {
        this(context, null);
    }

    public CustomViewBehind(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMarginThreshold = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MARGIN_THRESHOLD,
                getResources().getDisplayMetrics());
        
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        //移动事件最小距离
        mTouchSlop = configuration.getScaledTouchSlop();
        //mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    public void setCustomViewAbove(CustomViewAbove customViewAbove) {
        mViewAbove = customViewAbove;
    }

    public void setCanvasTransformer(CanvasTransformer t) {
        mTransformer = t;
    }

    public void setWidthOffset(int i) {
        mWidthChanged = (mWidthOffset != i)? true : false;
        mWidthOffset = i;
        requestLayout();
    }

    public int getBehindWidth() {
        return mContent.getWidth();
    }

    public void setContent(View v) {
        if (mContent != null)
            removeView(mContent);
        mContent = v;
        addView(mContent);
    }

    public View getContent() {
        return mContent;
    }

    /**
     * Sets the secondary (right) menu for use when setMode is called with SlidingMenu.LEFT_RIGHT.
     * @param v the right menu
     */
    public void setSecondaryContent(View v) {
        if (mSecondaryContent != null)
            removeView(mSecondaryContent);
        mSecondaryContent = v;
        addView(mSecondaryContent);
    }

    public View getSecondaryContent() {
        return mSecondaryContent;
    }

    public void setChildrenEnabled(boolean enabled) {
        mChildrenEnabled = enabled;
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        if (mTransformer != null)
            invalidate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /* 若事件分发至此，此处将根据CustomViewAbove当前的滑动状态，判读符合条件的侧滑手势事件，
        事件被拦截后交由onTouchEvent处理 */

        // SlidingMenu不能滚动情况下，此处也不必拦截事件，按照正常流程分发事件
        if(!mViewAbove.isSlidingEnabled()){
          return false;  
        }
        
        // 若CustomViewAbvoe 正在处理拖拉滚动事件，该view则拦截事件，
        // 但不会做具体拖拉处理，只确保事件不分发至子view
        if(mViewAbove.IsBeingDragged()){
            mIsBeingDragged = false;
            return true;
        }
        
        final int action = ev.getAction();

        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            // 移动手势且当前允许拖拉滚动，默认拦截手势，在onTouchEvent处理该事件
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                final int x = (int) ev.getX(pointerIndex);
                final int y = (int) ev.getY(pointerIndex);
                final int xDiff = (int) Math.abs(mLastMotionX - x);
                final int yDiff = (int) Math.abs(mLastMotionY - y);
                // 侧滑距离至少达到最小距离，侧滑距离比纵滑距离范围大，且当前未滑到边界
                if (xDiff > mTouchSlop && (xDiff - yDiff > 0) && mViewAbove.slideAllowed(x - mLastMotionX)
                        ) {
                    mIsBeingDragged = true;
                    mLastMotionX = x;
                    mLastMotionY = y;
                    // 初始化速度跟踪
                    initVelocityTrackerIfNotExists();
                    mVelocityTracker.addMovement(ev);
                    //不允许上层view拦截事件
                    if (getParent() != null)
                        getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                //final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                mIsBeingDragged = false;
                mLastMotionX = mInitialMotionX = (int) ev.getX();
                mLastMotionY = y;
                mActivePointerId = ev.getPointerId(0);
                // 初始化速度跟踪
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // 手指松开，释放拖拉滚动
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                recycleVelocityTracker();
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                mLastMotionX = (int) ev.getX(index);
                mLastMotionY = (int) ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                mLastMotionX = (int) ev.getX(ev.findPointerIndex(mActivePointerId));
                mLastMotionY = (int) ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        /* 处理在onInterceptTouchEvent(event)中被拦截下来的事件，
        此处将根据move距离，调用CustomViewAbove中的方法，使其按照距离和方向滑动
        如果SlidingMenu关闭滑动功能，此处也不会处理touch事件 */
        
        // Slidingmenu不能滚动，不处理拖拉滚动事件，事件继续按流程分发
        if(!mViewAbove.isSlidingEnabled()){
            return super.onTouchEvent(ev);  
          }
        
        // 若CustomViewAbvoe 正在处理拖拉滚动事件，此view则不处理
        if(mViewAbove.IsBeingDragged()){
            mIsBeingDragged = false;
            return false;
        }
        // 手势速度跟踪
        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(ev);
        
        int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if (getChildCount() == 0) {
                    return false;
                }

                mLastMotionX = mInitialMotionX = (int) ev.getX();
                mLastMotionY = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                final int x = (int) ev.getX(activePointerIndex);
                final int y = (int) ev.getY(activePointerIndex);
                float deltaX = mLastMotionX - x;
                float deltaY = mLastMotionY - y;
                // 侧滑距离至少达到最小距离，侧滑距离比纵滑距离范围大，且当前未滑到边界
                if (!mIsBeingDragged && Math.abs(deltaX) > mTouchSlop / 2
                        && (Math.abs(deltaX) - Math.abs(deltaY) > 0) && 
                        mViewAbove.slideAllowed(x - mLastMotionX)) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    mIsBeingDragged = true;
                    if (deltaX > 0) {
                        deltaX -= mTouchSlop;
                    } else {
                        deltaX += mTouchSlop;
                    }
                }
                if (mIsBeingDragged) {
                    mLastMotionX = x;
                    mLastMotionY = y;
                    float oldScrollX = mViewAbove.getScrollX();
                    float scrollX = oldScrollX + deltaX;
                    //获取滑动边界线
                    final float leftBound = mViewAbove.getLeftBound();
                    final float rightBound = mViewAbove.getRightBound();
                    //滚动目标位置超出边界线，调整至边界线位置
                    if (scrollX < leftBound) {
                        scrollX = leftBound;
                    } else if (scrollX > rightBound) {
                        scrollX = rightBound;
                    }
                    // Don't lose the rounded component
                    mLastMotionX += scrollX - (int) scrollX;
                    mViewAbove.scrollTo((int) scrollX, getScrollY());
                    //更新滚动状态，通知监听器
                    mViewAbove.pageScrolled((int) scrollX);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    //取得当前速度
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getXVelocity(mActivePointerId);
                    final int scrollX = mViewAbove.getScrollX();
                    final float pageOffset = (float) (scrollX - mViewAbove.getDestScrollX(mViewAbove.getCurrentItem()))
                            / getBehindWidth();
                    final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                    if (mActivePointerId != INVALID_POINTER) {
                        final int totalDelta = (int) (ev.getX(pointerIndex) - mInitialMotionX);
                        //根据手势速度和起始距离决定目标页面
                        int nextPage = mViewAbove.determineTargetPage(pageOffset, initialVelocity, totalDelta);
                        mViewAbove.setCurrentItemInternal(nextPage, true, true, initialVelocity);
                    } else {
                        mViewAbove.setCurrentItemInternal(mViewAbove.getCurrentItem(), true, true, initialVelocity);
                    }
                    mActivePointerId = INVALID_POINTER;
                    mIsBeingDragged = false;
                    recycleVelocityTracker();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        return true;
    }
    
    /**
     * 该view正在处理拖拉滚动事件
     * @return
     */
    public boolean IsBeingDragged() {
        return mIsBeingDragged;
    }
    
    /**
     * 该属性用于设置当SlidingMenu处于关闭状态下，该view是否保持可见或隐藏
     * @param visible
     */
    public void setVisibleAlways(boolean visible){
        mVisibleAlways = visible;
    }
    
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // 第一个激活的触点松开，激化新触点.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            recycleVelocityTracker();
        }
    }
    
    /**
     * 初始化速度跟踪器
     */
    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    /**
     * 回收速度跟踪器
     */
    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mTransformer != null)
        {
            canvas.save();
            mTransformer.transformCanvas(canvas, mViewAbove.getPercentOpen());
            super.dispatchDraw(canvas);
            canvas.restore();
        }
        else
            super.dispatchDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;
        mContent.layout(0, 0, width - mWidthOffset , height);
        if (mSecondaryContent != null)
            mSecondaryContent.layout(0, 0, width - mWidthOffset, height);
        
        // 若侧边栏打开状态，重新调整侧边栏及主界面显示位置
        if(mWidthChanged){
            int currentPage = mViewAbove.getCurrentItem();
            switch (currentPage) {
                case 0:              
                    mViewAbove.setCurrentItem(0, false, true);
                    break;
                case 2:
                    mViewAbove.setCurrentItem(2, false, true);
                    break;
                default:
                    break;
            }
            mWidthChanged = false;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
        final int contentWidth = getChildMeasureSpec(widthMeasureSpec, 0, width - mWidthOffset);
        final int contentHeight = getChildMeasureSpec(heightMeasureSpec, 0, height);
        mContent.measure(contentWidth, contentHeight);
        if (mSecondaryContent != null)
            mSecondaryContent.measure(contentWidth, contentHeight);
    }

    private int mMode;
    private boolean mFadeEnabled;
    private final Paint mFadePaint = new Paint();
    private float mScrollScale;
    private Drawable mShadowDrawable;
    private Drawable mSecondaryShadowDrawable;
    private int mShadowWidth;
    private float mFadeDegree;

    public void setMode(int mode) {
        if (mode == SlidingMenu.LEFT || mode == SlidingMenu.RIGHT) {
            if (mContent != null)
                mContent.setVisibility(View.VISIBLE);
            if (mSecondaryContent != null)
                mSecondaryContent.setVisibility(View.INVISIBLE);
        }
        mMode = mode;
    }

    public int getMode() {
        return mMode;
    }

    public void setScrollScale(float scrollScale) {
        mScrollScale = scrollScale;
    }

    public float getScrollScale() {
        return mScrollScale;
    }

    public void setShadowDrawable(Drawable shadow) {
        mShadowDrawable = shadow;
        invalidate();
    }

    public void setSecondaryShadowDrawable(Drawable shadow) {
        mSecondaryShadowDrawable = shadow;
        invalidate();
    }

    public void setShadowWidth(int width) {
        mShadowWidth = width;
        invalidate();
    }

    public void setFadeEnabled(boolean b) {
        mFadeEnabled = b;
    }

    public void setFadeDegree(float degree) {
        if (degree > 1.0f || degree < 0.0f)
            throw new IllegalStateException("The BehindFadeDegree must be between 0.0f and 1.0f");
        mFadeDegree = degree;
    }

    public int getMenuPage(int page) {
        page = (page > 1) ? 2 : ((page < 1) ? 0 : page);
        if (mMode == SlidingMenu.LEFT && page > 1) {
            return 0;
        }
        else if (mMode == SlidingMenu.RIGHT && page < 1) {
            return 2;
        } else {
            return page;
        }
    }

    /**
     * 该方法用于CustomViewBehind跟随滑动，以及滑动和停止滑动状态下，设置侧边栏布局是否可见
     * 
     * @param content
     * @param x
     * @param y
     */
    public void scrollBehindTo(View content, int x, int y) {
        int vis = View.VISIBLE;
        if (mMode == SlidingMenu.LEFT) {
            if (x >= content.getLeft() && !mVisibleAlways)
                vis = View.INVISIBLE;
            scrollTo((int) ((x + getBehindWidth()) * mScrollScale), y);
        } else if (mMode == SlidingMenu.RIGHT) {
            if (x <= content.getLeft() && !mVisibleAlways)
                vis = View.INVISIBLE;
            scrollTo((int) (getBehindWidth() - getWidth() + (x - getBehindWidth()) * mScrollScale), y);
        } else if (mMode == SlidingMenu.LEFT_RIGHT) {
            mContent.setVisibility(x >= content.getLeft() ? View.INVISIBLE : View.VISIBLE);
            mSecondaryContent.setVisibility((x <= content.getLeft() && !mVisibleAlways)? View.INVISIBLE : View.VISIBLE);
            vis = x == 0 ? View.INVISIBLE : View.VISIBLE;
            if (x <= content.getLeft()) {
                scrollTo((int) ((x + getBehindWidth()) * mScrollScale), y);
            } else {
                scrollTo((int) (getBehindWidth() - getWidth() + (x - getBehindWidth()) * mScrollScale), y);
            }
        }
        if (vis == View.INVISIBLE)
            Log.v(TAG, "behind INVISIBLE");
        setVisibility(vis);
    }

    public int getMenuLeft(View content, int page) {
        int widthOffset = 0;
        //如果主界面有边距，需要减掉边距
        if(mViewAbove != null){
            widthOffset = mViewAbove.getAboveOffsetLeft();
        }
        if (mMode == SlidingMenu.LEFT) {
            switch (page) {
                case 0:
                    return content.getLeft() + widthOffset - getBehindWidth();
                case 2:
                    return content.getLeft() + widthOffset;
            }
        } else if (mMode == SlidingMenu.RIGHT) {
            switch (page) {
                case 0:
                    return content.getLeft() + widthOffset ;
                case 2:
                    return content.getLeft() + widthOffset + getBehindWidth();
            }
        } else if (mMode == SlidingMenu.LEFT_RIGHT) {
            switch (page) {
                case 0:
                    return content.getLeft() + widthOffset - getBehindWidth();
                case 2:
                    return content.getLeft() + widthOffset + getBehindWidth();
            }
        }
        return content.getLeft();
    }
    
    public int getAbsLeftBound(View content) {
        int widthOffset = 0;
        // 如果主界面有边距，需要减掉边距
        if(mViewAbove != null) {
            widthOffset = mViewAbove.getAboveOffsetLeft();
        }
        
        if (mMode == SlidingMenu.LEFT || mMode == SlidingMenu.LEFT_RIGHT) {
            return content.getLeft() - getBehindWidth() + widthOffset;
        }
        else if (mMode == SlidingMenu.RIGHT) {
            return content.getLeft();
        }
        return 0;
    }

    public int getAbsRightBound(View content) {
        
        if (mMode == SlidingMenu.LEFT) {
            return content.getLeft();
        } else if (mMode == SlidingMenu.RIGHT || mMode == SlidingMenu.LEFT_RIGHT) {
            return content.getLeft() + getBehindWidth();
        }
        return 0;
    }

    public boolean marginTouchAllowed(View content, int x) {
        int left = content.getLeft();
        int right = content.getRight();
        if (mMode == SlidingMenu.LEFT) {
            return (x >= left && x <= mMarginThreshold + left);
        } else if (mMode == SlidingMenu.RIGHT) {
            return (x <= right && x >= right - mMarginThreshold);
        } else if (mMode == SlidingMenu.LEFT_RIGHT) {
            return (x >= left && x <= mMarginThreshold + left) || (x <= right && x >= right - mMarginThreshold);
        }
        return false;
    }

    public void setTouchMode(int i) {
        mTouchMode = i;
    }

    public boolean menuOpenTouchAllowed(View content, int currPage, float x) {
        switch (mTouchMode) {
            case SlidingMenu.TOUCHMODE_FULLSCREEN:
                return true;
            case SlidingMenu.TOUCHMODE_MARGIN:
                return menuTouchInQuickReturn(content, currPage, x);
        }
        return false;
    }

    public boolean menuTouchInQuickReturn(View content, int currPage, float x) {
        if (mMode == SlidingMenu.LEFT || (mMode == SlidingMenu.LEFT_RIGHT && currPage == 0)) {
            return x >= content.getLeft();
        } else if (mMode == SlidingMenu.RIGHT || (mMode == SlidingMenu.LEFT_RIGHT && currPage == 2)) {
            return x <= content.getRight();
        }
        return false;
    }

    public boolean menuClosedSlideAllowed(float dx) {
        if (mMode == SlidingMenu.LEFT) {
            return dx > 0;
        } else if (mMode == SlidingMenu.RIGHT) {
            return dx < 0;
        } else if (mMode == SlidingMenu.LEFT_RIGHT) {
            return true;
        }
        return false;
    }

    public boolean menuOpenSlideAllowed(float dx) {
        if (mMode == SlidingMenu.LEFT) {
            return dx < 0;
        } else if (mMode == SlidingMenu.RIGHT) {
            return dx > 0;
        } else if (mMode == SlidingMenu.LEFT_RIGHT) {
            return true;
        }
        return false;
    }

    /**
     * 绘制边缘阴影，阴影在位置跟随在主界面边缘
     * 
     * @param content
     * @param canvas
     */
    public void drawShadow(View content, Canvas canvas) {
        if (mShadowDrawable == null || mShadowWidth <= 0)
            return;
        int left = 0;
        if (mMode == SlidingMenu.LEFT) {
            left = content.getLeft() - mShadowWidth;
        } else if (mMode == SlidingMenu.RIGHT) {
            left = content.getRight();
        } else if (mMode == SlidingMenu.LEFT_RIGHT) {
            if (mSecondaryShadowDrawable != null) {
                left = content.getRight();
                mSecondaryShadowDrawable.setBounds(left, 0, left + mShadowWidth, getHeight());
                mSecondaryShadowDrawable.draw(canvas);
            }
            left = content.getLeft() - mShadowWidth;
        }
        mShadowDrawable.setBounds(left, 0, left + mShadowWidth, getHeight());
        mShadowDrawable.draw(canvas);
    }

    
    /**
     * 绘制淡入淡出效果，根据当前侧边栏打开比例
     * 
     * @param content
     * @param canvas
     * @param openPercent 当前侧边栏打开比例
     */
    public void drawFade(View content, Canvas canvas, float openPercent) {
        if (!mFadeEnabled)
            return;
        final int alpha = (int) (mFadeDegree * 255 * Math.abs(1 - openPercent));
        mFadePaint.setColor(Color.argb(alpha, 0, 0, 0));
        int left = 0;
        int right = 0;
        if (mMode == SlidingMenu.LEFT) {
            left = content.getLeft() - getBehindWidth();
            right = content.getLeft();
        } else if (mMode == SlidingMenu.RIGHT) {
            left = content.getRight();
            right = content.getRight() + getBehindWidth();
        } else if (mMode == SlidingMenu.LEFT_RIGHT) {
            left = content.getLeft() - getBehindWidth();
            right = content.getLeft();
            canvas.drawRect(left, 0, right, getHeight(), mFadePaint);
            left = content.getRight();
            right = content.getRight() + getBehindWidth();
        }
        canvas.drawRect(left, 0, right, getHeight(), mFadePaint);
    }

    private boolean mSelectorEnabled = true;
    private Bitmap  mSelectorDrawable;
    private View    mSelectedView;

    public void drawSelector(View content, Canvas canvas, float openPercent) {
        if (!mSelectorEnabled)
            return;
        if (mSelectorDrawable != null && mSelectedView != null) {
            String tag = (String) mSelectedView.getTag();
            if (tag.equals(TAG + "SelectedView")) {
                canvas.save();
                int left, right, offset;
                offset = (int) (mSelectorDrawable.getWidth() * openPercent);
                if (mMode == SlidingMenu.LEFT) {
                    right = content.getLeft();
                    left = right - offset;
                    canvas.clipRect(left, 0, right, getHeight());
                    canvas.drawBitmap(mSelectorDrawable, left, getSelectorTop(), null);
                } else if (mMode == SlidingMenu.RIGHT) {
                    left = content.getRight();
                    right = left + offset;
                    canvas.clipRect(left, 0, right, getHeight());
                    canvas.drawBitmap(mSelectorDrawable, right - mSelectorDrawable.getWidth(), getSelectorTop(), null);
                }
                canvas.restore();
            }
        }
    }

    public void setSelectorEnabled(boolean b) {
        mSelectorEnabled = b;
    }

    public void setSelectedView(View v) {
        if (mSelectedView != null) {
            mSelectedView.setTag("");
            mSelectedView = null;
        }
        if (v != null && v.getParent() != null) {
            mSelectedView = v;
            mSelectedView.setTag(TAG + "SelectedView");
            invalidate();
        }
    }

    private int getSelectorTop() {
        int y = mSelectedView.getTop();
        y += (mSelectedView.getHeight() - mSelectorDrawable.getHeight()) / 2;
        return y;
    }

    public void setSelectorBitmap(Bitmap b) {
        mSelectorDrawable = b;
        refreshDrawableState();
    }
    
    public void setMenuVisibleAlways(boolean visibleAlways) {
        mVisibleAlways= visibleAlways;
    }

}
