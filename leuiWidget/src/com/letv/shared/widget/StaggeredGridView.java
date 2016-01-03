package com.letv.shared.widget;

/*
 * Copyright (C) 2012 The Android Open Source Project
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
 * 
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.StateSet;
import android.view.ActionMode;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.widget.Checkable;
import android.widget.ListAdapter;
import android.widget.OverScroller;

import com.letv.shared.R;

/**
 * ListView and GridView just not complex enough? Try StaggeredGridView!
 *
 * <p>StaggeredGridView presents a multi-column grid with consistent column sizes
 * but varying row sizes between the columns. Each successive item from a
 * {@link android.widget.ListAdapter ListAdapter} will be arranged from top to bottom,
 * left to right. The largest vertical gap is always filled first.</p>
 *
 * <p>Item views may span multiple columns as specified by their {@link com.letv.shared.widget.StaggeredGridView.LayoutParams}.
 * The attribute <code>android:layout_span</code> may be used when inflating
 * item views from xml.
 * 
 * @author wangziming
 * </p>
 */
@SuppressLint("NewApi")
public class StaggeredGridView extends ViewGroup {
    private static final String TAG = "StaggeredGridView";
    private static final boolean DEBUG = false;
    /*
     * There are a few things you should know if you're going to make modifications
     * to StaggeredGridView.
     *
     * Like ListView, SGV populates from an adapter and recycles views that fall out
     * of the visible boundaries of the grid. A few invariants always hold:
     *
     * - mFirstPosition is the adapter position of the View returned by getChildAt(0).
     * - Any child index can be translated to an adapter position by adding mFirstPosition.
     * - Any adapter position can be translated to a child index by subtracting mFirstPosition.
     * - Views for items in the range [mFirstPosition, mFirstPosition + getChildCount()) are
     *   currently attached to the grid as children. All other adapter positions do not have
     *   active views.
     *
     * This means a few things thanks to the staggered grid's nature. Some views may stay attached
     * long after they have scrolled offscreen if removing and recycling them would result in
     * breaking one of the invariants above.
     *
     * LayoutRecords are used to track data about a particular item's layout after the associated
     * view has been removed. These let positioning and the choice of column for an item
     * remain consistent even though the rules for filling content up vs. filling down vary.
     *
     * Whenever layout parameters for a known LayoutRecord change, other LayoutRecords before
     * or after it may need to be invalidated. e.g. if the item's height or the number
     * of columns it spans changes, all bets for other items in the same direction are off
     * since the cached information no longer applies.
     */

    public static final int ACTION_TYPE_NORMAL = 0;
    public static final int ACTION_TYPE_WARNING = 1;
    /**
     * Normal list that does not indicate choices
     */
    public static final int CHOICE_MODE_NONE = 0;
    /**
     * The list allows multiple choices in a modal selection mode for leui
     */
    public static final int CHOICE_MODE_MULTIPLE_MODAL = 3;
    private static final int COLUMN_COUNT_AUTO = -1;
    private static final int TOUCH_MODE_IDLE = 0;
    private static final int TOUCH_MODE_DRAGGING = 1;
    private static final int TOUCH_MODE_FLINGING = 2;
    private static final int TOUCH_MODE_DOWN = 3;
    private static final int TOUCH_MODE_TAP = 4;
    private static final int TOUCH_MODE_DONE_WAITING = 5;
    private static final int TOUCH_MODE_REST = 6;
    /**
     * 表示滑动越界的情况
     */
    static final int TOUCH_MODE_OVERSCROLL = 7;
    
    private static final int INVALID_POSITION = -1;
    
    /**
     * How many positions in either direction we will search to try to
     * find a checked item with a stable ID that moved position across
     * a data set change. If the item isn't found it will be unselected.
     */
    private static final int CHECK_POSITION_SEARCH_DISTANCE = 20;
    
    private boolean mBeginClick;
    private boolean mDataChanged;
    //是否允许selector绘制在最上层
    private boolean mDrawSelectorOnTop = true;
    private boolean mFastChildLayout;
    private boolean mHasStableIds;
    private boolean mInLayout;
    private boolean mPopulating;
    private boolean mEnableAnimation = true;
    /**
     * The select child's view (from the adapter's getView) is enabled.
     */
    private boolean mIsChildViewEnabled;
    private boolean mSync = false;
    
    //是否正在做选项删除的动画
    private boolean mFadeOuting = false;
    //是否正在做选项更新的动画
    private boolean mUpdateing = false;
    //是否正在做Layout的动画
    private boolean mLayoutAnim = false;
    private int mActivePointerId;
    private int mColCountSetting = 2;
    private int mColCount = 2;
    private int mColWidth;
    private int mFirstPosition;
    private int mFlingVelocity;
    private int mItemMargin = 8;
    private int mItemCount;
    private int mMinColWidth = 0;
    private int mMaximumVelocity;
    private int mMotionPosition;
    private int mMoveBy;
    private int mNumCols;
    private int mSpecificTop;
    private int mTouchMode;
    private int mTouchSlop;
    /**
     * The selection's left padding
     */
    int mSelectionLeftPadding = 0;
    
    /**
     * The selection's top padding
     */
    int mSelectionTopPadding = 0;
    
    /**
     * The selection's right padding
     */
    int mSelectionRightPadding = 0;
    
    /**
     * The selection's bottom padding
     */
    int mSelectionBottomPadding = 0;
    /**
     * The current position of the selector in the list.
     */
    int mSelectorPosition = INVALID_POSITION;
    private int mSyncPosition = INVALID_POSITION;
    /**
     * The X value associated with the the down motion event
     */
    int mMotionX;
    
    /**
     * The Y value associated with the the down motion event
     */
    int mMotionY;
    /**
     * Controls if/how the user may choose/check items in the list
     */
    int mChoiceMode = CHOICE_MODE_NONE;
    /**
     * Running count of how many items are currently checked
     */
    int mCheckedItemCount;
    
    //当前越界划出的距离
    private int mCurrentOverScrollDistance;
    //可越界划出的最大距离
    private int mMaxOverScrollDistance;
    //上次越界后的距离
    private int mLastOverScrollX = 0;
    
    private int[] mItemTops;
    private int[] mItemBottoms;
    private int[] mRestoreOffsets;
    
    private float mLastTouchY;
    private float mLastTouchX;
    private float mTouchRemainderY;
    
    private long mFirstAdapterId;
	
    private ListAdapter mAdapter;
    
    private final VelocityTracker mVelocityTracker = VelocityTracker.obtain();
    private final OverScroller mScroller;
    private final RecycleBin mRecycler = new RecycleBin();
    
    private final AdapterDataSetObserver mObserver = new AdapterDataSetObserver();
    
    //不使用边缘发光效果
    //private final EdgeEffectCompat mTopEdge;
    //private final EdgeEffectCompat mBottomEdge;
    
    private ArrayList<ArrayList<Integer>> mColMappings = new ArrayList<ArrayList<Integer>>(); 
    
    private Runnable mPendingCheckForTap;
    /**
     * Delayed action for touch mode.
     */
    private Runnable mTouchModeReset;
    
    private ContextMenuInfo mContextMenuInfo = null;
    
    /**
     * The drawable used to draw the selector
     */
    Drawable mSelector;
    
    /**
     * Defines the selector's location and dimension at drawing time
     */
    Rect mSelectorRect = new Rect();
    
    /**
     * The listener that receives notifications when an item is clicked.
     */
    OnItemClickListener mOnItemClickListener;

    /**
     * The listener that receives notifications when an item is long clicked.
     */
    OnItemLongClickListener mOnItemLongClickListener;
    
    /**
     * The last CheckForLongPress runnable we posted, if any
     */
    private CheckForLongPress mPendingCheckForLongPress;
    
    /**
     * Acts upon click
     */
    private PerformClick mPerformClick;
    
    /**
     * Rectangle used for hit testing children
     */
    private Rect mTouchFrame;
    private final SparseArray<LayoutRecord> mLayoutRecords = new SparseArray<LayoutRecord>();
   
    /**
     * Controls CHOICE_MODE_MULTIPLE_MODAL. null when inactive.
     */
    ActionMode mChoiceActionMode;
    
    /**
     * Wrapper for the multiple choice mode callback; AbsListView needs to perform
     * a few extra actions around what application code does.
     */
    MultiChoiceModeWrapper mMultiChoiceModeCallback;
    
    /**
     * Running state of which positions are currently checked
     */
    SparseBooleanArray mCheckStates;
    
    /**
     * Running state of which IDs are currently checked.
     * If there is a value for a given key, the checked state for that ID is true
     * and the value holds the last known position in the adapter for that id.
     */
    LongSparseArray<Integer> mCheckedIdStates;
    
    //在数据更新的时候，通过该hashmap存储当前界面所有选项的相关位置信息，以便后续做动画的时候使用
    private final Map<Long, ViewRectPair> mChildRectsForAnimation = new HashMap<Long, ViewRectPair>();
    ArrayList<View> mDeleteViews = new ArrayList<View>();
    
    
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private Context mContext;
    private ArrayList<Long> mDeleteItemId;
    
    Runnable mPositionScrollAfterLayout;
    
    //选项更新的动画
    private AnimatorSet mUpdateAnimatorSet;
    //选项删除的动画
    private AnimatorSet mFadeOutViewAnimatorSet;
    
    /**
     * Used for smooth scrolling at a consistent rate
     */
    static final Interpolator sLinearInterpolator = new LinearInterpolator();
    
    private static final class LayoutRecord {
        public int column;
        public long id = -1;
        public int height;
        public int span;
        private int[] mMargins;

        private final void ensureMargins() {
            if (mMargins == null) {
                // Don't need to confirm length;
                // all layoutrecords are purged when column count changes.
                mMargins = new int[span * 2];
            }
        }

        public final int getMarginAbove(int col) {
            if (mMargins == null) {
                return 0;
            }
            return mMargins[col * 2];
        }

        public final int getMarginBelow(int col) {
            if (mMargins == null) {
                return 0;
            }
            return mMargins[col * 2 + 1];
        }

        public final void setMarginAbove(int col, int margin) {
            if (mMargins == null && margin == 0) {
                return;
            }
            ensureMargins();
            mMargins[col * 2] = margin;
        }

        public final void setMarginBelow(int col, int margin) {
            if (mMargins == null && margin == 0) {
                return;
            }
            ensureMargins();
            mMargins[col * 2 + 1] = margin;
        }

        @Override
        public String toString() {
            String result = "LayoutRecord{c=" + column + ", id=" + id + " h=" + height +
                    " s=" + span;
            if (mMargins != null) {
                result += " margins[above, below](";
                for (int i = 0; i < mMargins.length; i += 2) {
                    result += "[" + mMargins[i] + ", " + mMargins[i+1] + "]";
                }
                result += ")";
            }
            return result + "}";
        }
    }

    public StaggeredGridView(Context context) {
        this(context, null);
        
        setVerticalScrollBarEnabled(true); 
        /*TypedArray a = context.obtainStyledAttributes(R.styleable.View);
        initializeScrollbars(a);
        a.recycle();*/
    }

    public StaggeredGridView(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.Widget_Leui_StaggeredGridView);
    }

    public StaggeredGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.StaggeredGridView);
        mColCount = a.getInteger(R.styleable.StaggeredGridView_leNumColumns, 2);
        mDrawSelectorOnTop = a.getBoolean(R.styleable.StaggeredGridView_leDrawSelectorOnTop, false);
        mItemMargin = a.getDimensionPixelSize(R.styleable.StaggeredGridView_leItemMargin, 0);
        
        Drawable d = a.getDrawable(R.styleable.StaggeredGridView_leSelector);
        if (d != null) {
            setSelector(d);
        }
        
        a.recycle();
        //setSelector(getResources().getDrawable(R.drawable.item_background_holo_dark));
        mChoiceMode = CHOICE_MODE_NONE;
        final ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
        mMaximumVelocity = vc.getScaledMaximumFlingVelocity();
        mFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mScroller = new OverScroller(context);
        mMaxOverScrollDistance = 127;//getResources().getDimensionPixelSize(/*R.dimen.over_scroll_distance*/);
        
        //不使用边缘发光效果
        //mTopEdge = new EdgeEffectCompat(context);
        //mBottomEdge = new EdgeEffectCompat(context);
        setWillNotDraw(false);
        setClipToPadding(false);
        this.setFocusableInTouchMode(false);
        
        if (mSelector == null) {
            useDefaultSelector();
        }
        
        //添加启动动画
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.le_staggeredgridview_loading_layout_anim);
        setLayoutAnimation(controller);
        mLayoutAnim = true;
        setLayoutAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                mLayoutAnim = false;
            }
        });
    }
    
    /**
     * Set a fixed number of columns for this grid. Space will be divided evenly
     * among all columns, respecting the item margin between columns.
     * The default is 2. (If it were 1, perhaps you should be using a
     * {@link android.widget.ListView ListView}.)
     *
     * @param colCount Number of columns to display.
     * @see #setMinColumnWidth(int)
     */
    public void setColumnCount(int colCount) {
        if (colCount < 1 && colCount != COLUMN_COUNT_AUTO) {
            throw new IllegalArgumentException("Column count must be at least 1 - received " +
                    colCount);
        }
        final boolean needsPopulate = colCount != mColCount;
        mColCount = mColCountSetting = colCount;
        if (needsPopulate) {
            populate(false);
        }
    }

    public int getColumnCount() {
        return mColCount;
    }

    /**
     * Set a minimum column width for
     * @param minColWidth
     */
    public void setMinColumnWidth(int minColWidth) {
        mMinColWidth = minColWidth;
        setColumnCount(COLUMN_COUNT_AUTO);
    }

    /**
     * Set the margin between items in pixels. This margin is applied
     * both vertically and horizontally.
     *
     * @param marginPixels Spacing between items in pixels
     */
    public void setItemMargin(int marginPixels) {
        final boolean needsPopulate = marginPixels != mItemMargin;
        mItemMargin = marginPixels;
        if (needsPopulate) {
            populate(false);
        }
    }

    /**
     * Return the first adapter position with a view currently attached as
     * a child view of this grid.
     *
     * @return the adapter position represented by the view at getChildAt(0).
     */
    public int getFirstPosition() {
        return mFirstPosition;
    }

    /**
     * @return the total number of items in the grid displayed or not
     */
    public int getItemCount() {
        return mItemCount;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mVelocityTracker.addMovement(ev);
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mVelocityTracker.clear();
                mScroller.abortAnimation();
                mLastTouchY = ev.getY();
                mActivePointerId = ev.getPointerId(0);
                mTouchRemainderY = 0;
                if (mTouchMode == TOUCH_MODE_FLINGING) {
                    // Catch!
                    mTouchMode = TOUCH_MODE_DRAGGING;
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE: {
                final int index = ev.findPointerIndex(mActivePointerId);
                if (index < 0) {
                    Log.e(TAG, "onInterceptTouchEvent could not find pointer with id " +
                            mActivePointerId + " - did StaggeredGridView receive an inconsistent " +
                            "event stream?");
                    return false;
                }
                final float y = ev.getY(index);
                final float dy = y - mLastTouchY + mTouchRemainderY;
                final int deltaY = (int) dy;
                mTouchRemainderY = dy - deltaY;

                if (Math.abs(dy) > mTouchSlop) {
                    mTouchMode = TOUCH_MODE_DRAGGING;
                    return true;
                }
            }
        }

        return false;
    }

    void hideSelector() {
        if (this.mSelectorPosition != INVALID_POSITION) {
            // TODO: hide selector properly
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mVelocityTracker.addMovement(ev);
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        
        int motionPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
        
        switch (action) {
            case MotionEvent.ACTION_DOWN:{

                mVelocityTracker.clear();
                mScroller.abortAnimation();

                mLastTouchY = ev.getY();
                mLastTouchX = ev.getX();
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();

                //int Intercept = pointToPosition(x, y);
                mActivePointerId = ev.getPointerId(0);
                mTouchRemainderY = 0;
                
                if((mTouchMode != TOUCH_MODE_FLINGING && mTouchMode != TOUCH_MODE_OVERSCROLL)&& !mDataChanged && motionPosition >= 0 && getAdapter().isEnabled(motionPosition)){
                    mTouchMode = TOUCH_MODE_DOWN;
                    
//                  mBeginClick = true;

                    if (mPendingCheckForTap == null) {
                        mPendingCheckForTap = new CheckForTap();
                    }
                    
                    postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());
                }
                
                mMotionPosition = motionPosition;
                mMotionX = x;
                mMotionY = y;

                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int index = ev.findPointerIndex(mActivePointerId);
                if (index < 0) {
                    Log.e(TAG, "onInterceptTouchEvent could not find pointer with id " +
                            mActivePointerId + " - did StaggeredGridView receive an inconsistent " +
                            "event stream?");
                    return false;
                }
                final float y = ev.getY(index);
                float dy = y - mLastTouchY + mTouchRemainderY;
                int deltaY = (int) dy;
                mTouchRemainderY = dy - deltaY;

                if (Math.abs(dy) > mTouchSlop) {
                    mTouchMode = TOUCH_MODE_DRAGGING;
                    
                    final Handler handler = getHandler();
                    // Handler should not be null unless the AbsListView is not attached to a
                    // window, which would make it very hard to scroll it... but the monkeys
                    // say it's possible.
                    if (handler != null) {
                        handler.removeCallbacks(mPendingCheckForLongPress);
                    }
                    setPressed(false);
                    View motionView = getChildAt(mMotionPosition - mFirstPosition);
                    if (motionView != null) {
                        motionView.setPressed(false);
                    }
                    
                    // Time to start stealing events! Once we've stolen them, don't let anyone
                    // steal from us
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    
                }
                if (mTouchMode == TOUCH_MODE_DRAGGING) {
                    mLastTouchY = y;
                    
                    if (mCurrentOverScrollDistance != 0 && mMaxOverScrollDistance != 0) {
                        mTouchMode = TOUCH_MODE_OVERSCROLL;
                        if (Math.abs(mCurrentOverScrollDistance) >= mMaxOverScrollDistance) {
                            dy = 0;
                        } else {
                            float coeff = 1 - 1.0f * Math.abs(mCurrentOverScrollDistance) / mMaxOverScrollDistance;
                            dy *=coeff;                
                        } 
                        deltaY = (int)dy;
                        mTouchRemainderY = dy - deltaY;
                    } 

                    if (!mDataChanged && !mUpdateing && !mFadeOuting && trackMotionScroll(deltaY, true) ) {
                        // Break fling velocity if we impacted an edge.
                        mVelocityTracker.clear();
                    }
                }
                
                updateSelectorState();
                
                break;
            } 

            case MotionEvent.ACTION_CANCEL:{
                if (mCurrentOverScrollDistance != 0 && mScroller.springBack(0, mCurrentOverScrollDistance, 0, 0, 0, 0)) {
                    mTouchMode = TOUCH_MODE_OVERSCROLL;
                    mLastOverScrollX = mCurrentOverScrollDistance;
                    invalidate();
                }else {
                    mTouchMode = TOUCH_MODE_REST;
                    updateSelectorState();
                    setPressed(false);
                    View motionView = this.getChildAt(mMotionPosition - mFirstPosition);
                    if (motionView != null) {
                        motionView.setPressed(false);
                    }
                    final Handler handler = getHandler();
                    if (handler != null) {
                        handler.removeCallbacks(mPendingCheckForLongPress);
                    }
                    //不使用边缘发光效果
//                if (mTopEdge != null) {
//                	mTopEdge.onRelease();
//                	mBottomEdge.onRelease();
//                }
                    
                    recycleVelocityTracker();
                    mActivePointerId = INVALID_POSITION;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                final float velocity = mVelocityTracker.getYVelocity(mActivePointerId);
                final int prevTouchMode = mTouchMode;
                
                if (mCurrentOverScrollDistance != 0 &&  !mDataChanged && mScroller.springBack(0, mCurrentOverScrollDistance, 0, 0, 0, 0)) {
                    mTouchMode = TOUCH_MODE_OVERSCROLL;
                    mLastOverScrollX = mCurrentOverScrollDistance;
                    invalidate();
                    break;
                } else if (Math.abs(velocity) > mFlingVelocity && !mDataChanged) { // TODO
                    mTouchMode = TOUCH_MODE_FLINGING;
                    mScroller.fling(0, 0, 0, (int) velocity, 0, 0,
                            Integer.MIN_VALUE, Integer.MAX_VALUE);
                    mLastTouchY = 0;
                    invalidate();
                } else if (!mDataChanged && mAdapter.isEnabled(mMotionPosition)) {
                    // TODO : handle
                    mTouchMode = TOUCH_MODE_TAP;
                } else {
                    mTouchMode = TOUCH_MODE_REST;
                }
                
                switch(prevTouchMode){
                    case TOUCH_MODE_DOWN:
                    case TOUCH_MODE_TAP:
                    case TOUCH_MODE_DONE_WAITING:
                        motionPosition = mMotionPosition;
                        final View child = getChildAt(motionPosition - mFirstPosition);
                        final float x = ev.getX();
                        final boolean inList = x > getPaddingLeft() && x < getWidth() - getPaddingRight();
                        if (child != null && !child.hasFocusable() && inList) {
                        	if (mTouchMode != TOUCH_MODE_DOWN) {
                                child.setPressed(false);
                            }
                        	
                        	if (mPerformClick == null) {
                                mPerformClick = new PerformClick();
                            }
                        	
                        	final PerformClick performClick = mPerformClick;
                            performClick.mClickMotionPosition = motionPosition;
                            performClick.rememberWindowAttachCount();
                            
                            
                            if (mTouchMode == TOUCH_MODE_DOWN || mTouchMode == TOUCH_MODE_TAP) {
                                final Handler handlerTouch = getHandler();
                                if (handlerTouch != null) {
                                	handlerTouch.removeCallbacks(mTouchMode == TOUCH_MODE_DOWN ?
                                            mPendingCheckForTap : mPendingCheckForLongPress);
                                }
                                
                                if (!mDataChanged && mAdapter.isEnabled(motionPosition)) {
                                    mTouchMode = TOUCH_MODE_TAP;
                                    
                                    layoutChildren(mDataChanged);
                                    child.setPressed(true);
                                    positionSelector(mMotionPosition, child);
                                    setPressed(true);
                                    //通过此方法可以触发drawSelector（），以保证每次selector状态改变后都能绘制出来
                                    invalidate();
                                    if (mSelector != null) {
                                        Drawable d = mSelector.getCurrent();
                                        if (d != null && d instanceof TransitionDrawable) {
                                            ((TransitionDrawable) d).resetTransition();
                                        }
                                    }
                                    if (mTouchModeReset != null) {
                                        removeCallbacks(mTouchModeReset);
                                    }
                                    mTouchModeReset = new Runnable() {
                                        @Override
                                        public void run() {
                                            mTouchMode = TOUCH_MODE_REST;
                                            child.setPressed(false);
                                            setPressed(false);
                                            if (!mDataChanged) {
                                                performClick.run();
                                            }
                                        }
                                    };
                                    postDelayed(mTouchModeReset, ViewConfiguration.getPressedStateDuration());
                                    
                                } else {
                                    mTouchMode = TOUCH_MODE_REST;
                                    updateSelectorState();
                                }
                                return true;
                            } else if (!mDataChanged && mAdapter.isEnabled(motionPosition)) {
                                performClick.run();
                            }
                        } else if (child != null && !child.hasFocusable()) {
                            if (mTouchMode != TOUCH_MODE_DOWN) {
                                child.setPressed(false);
                            }
                        }
                        mTouchMode = TOUCH_MODE_REST;
                }
//                mBeginClick = false;
                updateSelectorState();
                break;
            } 
        }
        return true;
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
    }
    
    /**
     * 根据传入的偏移量来上下平移控件里面的所有item
     *
     * @param deltaY Pixels that content should move by
     * @return true if the movement completed, false if it was stopped prematurely.
     */
    private boolean trackMotionScroll(int deltaY, boolean allowOverScroll) {
        //final boolean contentFits = contentFits();

        //final int overScrolledBy;
        final int movedBy;
        
        int childCount = getChildCount();
        if (childCount == 0) {
            return true;
        }
        //在某些情况下，传入的deltaY太大或者太小，超出了控件的高度，此时需要先纠正一下，否则可能导致所有的选项被移出屏幕
        final int height = getHeight() - getPaddingBottom() - getPaddingTop();
        if (deltaY < 0) {
            deltaY = Math.max(-(height - 1), deltaY);
        } else {
            deltaY = Math.min(height - 1, deltaY);
        }
        final int allowOverhang = Math.abs(deltaY);
        int firstPosition = mFirstPosition;
        
        int mostTop = Integer.MAX_VALUE;
        int mostBottom = Integer.MIN_VALUE;
        for (int i = 0; i < mColCount; i++) {
            if (mItemTops[i] < mostTop) {
                mostTop = mItemTops[i];
            }
            if (mItemBottoms[i] > mostBottom) {
                mostBottom = mItemBottoms[i];
            }
        }
        
        boolean isAtEdge = false;
        boolean dontRecycle = false;
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        boolean cannotScrollDown = (firstPosition == 0 && mostTop >= paddingTop && deltaY >= 0);
        boolean cannotScrollUp = (firstPosition + childCount == mItemCount && mostBottom <= getHeight() - paddingBottom && deltaY <= 0);
        if (cannotScrollDown || cannotScrollUp || (mCurrentOverScrollDistance != 0)) {
            dontRecycle = true;
        }
        
        final int overhang;
        final boolean up;
        mPopulating = true;
        
        if (deltaY > 0) {
            overhang = fillUp(mFirstPosition - 1, allowOverhang);
            up = true;
        } else {
            overhang = fillDown(mFirstPosition + getChildCount(), allowOverhang) + mItemMargin;
            up = false;
        }
        offsetChildren(deltaY);
        
        if (DEBUG) {
            int cc = getChildCount();
            Log.i("SGV", "trackMotionScroll mFirstPosition = " + mFirstPosition + "--deltaY = " + deltaY);
            for (int i = 0; i < cc; i++) {
                View v = getChildAt(i);
                Log.i("SGV", "i  = " + i + "--top = " + v.getTop() + "--alpha = " + v.getAlpha());
            }
        }
        
        
        if(!dontRecycle) {
            recycleOffscreenViews();
        }
        movedBy = deltaY;
        
        mPopulating = false;
        childCount = getChildCount();
        mostTop = Integer.MAX_VALUE;
        mostBottom = Integer.MIN_VALUE;
        for (int i = 0; i < mColCount; i++) {
            if (mItemTops[i] < mostTop) {
                mostTop = mItemTops[i];
            }
            if (mItemBottoms[i] > mostBottom) {
                mostBottom = mItemBottoms[i]; 
            }
        }
        
        mCurrentOverScrollDistance = 0;
        if (mFirstPosition == 0 && mostTop > paddingTop) {
            mCurrentOverScrollDistance = paddingTop - mostTop;
            isAtEdge = true;
        } else if (firstPosition + childCount == mItemCount && mostBottom < getHeight() - paddingBottom && mItemCount > childCount) {
            //bottom over scroll,mItemCount > childCount
            mCurrentOverScrollDistance = getHeight() - paddingBottom - mostBottom;
            isAtEdge = true;
        } else if (firstPosition + childCount == mItemCount && mostBottom < getHeight() - paddingBottom && mItemCount == childCount){
            //top over scroll,mItemCount == childCount
            if (mostBottom - mostTop < getHeight() - paddingBottom - paddingTop) {
                mCurrentOverScrollDistance = paddingTop - mostTop;
                isAtEdge = true;
            } else {
                mCurrentOverScrollDistance = getHeight() - paddingBottom - mostBottom;
                isAtEdge = true;
            }
        }
        mCurrentOverScrollDistance = - mCurrentOverScrollDistance;
        
        //不使用边缘发光效果
//        if (allowOverScroll) {
//            final int overScrollMode = ViewCompat.getOverScrollMode(this);
// 
//            if (overScrollMode == ViewCompat.OVER_SCROLL_ALWAYS ||
//                    (overScrollMode == ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS && !contentFits)) {
//                if (overScrolledBy > 0) {
//                    EdgeEffectCompat edge = deltaY > 0 ? mTopEdge : mBottomEdge;
//                    edge.onPull((float) Math.abs(deltaY) / getHeight());
//                    invalidate();
//                }
//            }
//        }
        awakenScrollBars(0, true);
        if (mSelectorPosition != INVALID_POSITION) {
            final int childIndex = mSelectorPosition - mFirstPosition;
            if (childIndex >= 0 && childIndex < getChildCount()) {
                positionSelector(INVALID_POSITION, getChildAt(childIndex));
            }
        } else {
            mSelectorRect.setEmpty();
        }
        
        mMoveBy = movedBy;
        
        return isAtEdge;
    }

    /*private final boolean contentFits() {
        if (mFirstPosition != 0 || getChildCount() != mItemCount) {
            return false;
        }

        int topmost = Integer.MAX_VALUE;
        int bottommost = Integer.MIN_VALUE;
        for (int i = 0; i < mColCount; i++) {
            if (mItemTops[i] < topmost) {
                topmost = mItemTops[i];
            }
            if (mItemBottoms[i] > bottommost) {
                bottommost = mItemBottoms[i];
            }
        }

        return topmost >= getPaddingTop() && bottommost <= getHeight() - getPaddingBottom();
    }*/

    private void recycleAllViews() {
        int size = mDeleteViews.size();
        View view;
        for (int i = 0; i < getChildCount(); i++) {
            view = getChildAt(i);
            view.setAlpha(1.0f);
            if (size > 0 && mDeleteViews.contains(view)) {
                //将要删除的ｖｉｅｗ不进行回收，等最后动画做完了再回收
            } else {
                mRecycler.addScrap(view);
            }
        }

        if (mInLayout) {
            removeAllViewsInLayout();
        } else {
            removeAllViews();
        }
    }

    /**
     * Important: this method will leave offscreen views attached if they
     * are required to maintain the invariant that child view with index i
     * is always the view corresponding to position mFirstPosition + i.
     */
    private void recycleOffscreenViews() {
        final int height = getHeight();
        final int clearAbove = -mItemMargin;
        final int clearBelow = height + mItemMargin;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getTop() <= clearBelow)  {
                // There may be other offscreen views, but we need to maintain
                // the invariant documented above.
                break;
            }

            if (mInLayout) {
                removeViewsInLayout(i, 1);
            } else {
                removeViewAt(i);
            }

            mRecycler.addScrap(child);
        }

        while (getChildCount() > 0) {
            final View child = getChildAt(0);
            if (child.getBottom() >= clearAbove) {
                // There may be other offscreen views, but we need to maintain
                // the invariant documented above.
                break;
            }

            if (mInLayout) {
                removeViewsInLayout(0, 1);
            } else {
                removeViewAt(0);
            }

            mRecycler.addScrap(child);
            mFirstPosition++;
        }

        final int childCount = getChildCount();
        if (childCount > 0) {
            // Repair the top and bottom column boundaries from the views we still have
            Arrays.fill(mItemTops, Integer.MAX_VALUE);
            Arrays.fill(mItemBottoms, Integer.MIN_VALUE);

            for (int i = 0; i < childCount; i++){
                final View child = getChildAt(i);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final int top = child.getTop() - mItemMargin;
                final int bottom = child.getBottom();
                LayoutRecord rec = mLayoutRecords.get(mFirstPosition + i);
                
                if (rec == null) {
                    rec = new LayoutRecord();
                    mLayoutRecords.put(mFirstPosition + i, rec);
                    rec.height = child.getHeight();
                    rec.column = lp.column;
                    rec.id = lp.id;
                    rec.span = Math.min(mColCount, lp.span);
                }
                
                final int colEnd = lp.column + Math.min(mColCount, lp.span);
                for (int col = lp.column; col < colEnd; col++) {
                    final int colTop = top - rec.getMarginAbove(col - lp.column);
                    final int colBottom = bottom + rec.getMarginBelow(col - lp.column);
                    if (colTop < mItemTops[col]) {
                        mItemTops[col] = colTop;
                    }
                    if (colBottom > mItemBottoms[col]) {
                        mItemBottoms[col] = colBottom;
                    }
                }
            }

            for (int col = 0; col < mColCount; col++) {
                if (mItemTops[col] == Integer.MAX_VALUE) {
                    // If one was untouched, both were.
                    mItemTops[col] = 0;
                    mItemBottoms[col] = 0;
                }
            }
        } else {
            int top = getPaddingTop();
            Arrays.fill(mItemTops, top);
            Arrays.fill(mItemBottoms, top);
            
            if(mRestoreOffsets != null)
//            	Arrays.fill(mRestoreOffsets, 0);
            	mRestoreOffsets = null;
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            final int y = mScroller.getCurrY();
            final int dy;
            if (mTouchMode == TOUCH_MODE_OVERSCROLL) {
                dy = y - mLastOverScrollX;
                mLastOverScrollX = y;
                trackMotionScroll(dy, false);
            } else {
                dy = (int) (y - mLastTouchY);
                mLastTouchY = y;
                final boolean isAtEdge = trackMotionScroll(dy, false);
                if (isAtEdge) {
                    mScroller.notifyHorizontalEdgeReached(0, 0, 0);
                    mScroller.notifyVerticalEdgeReached(mCurrentOverScrollDistance, 0, mMaxOverScrollDistance);
                    mLastOverScrollX = mCurrentOverScrollDistance;
                    mTouchMode = TOUCH_MODE_OVERSCROLL;
                    postInvalidate();
                    return;
                }
            }

            if (!mScroller.isFinished()) {
                postInvalidate();
            } else {
                mScroller.abortAnimation();
                mTouchMode = TOUCH_MODE_IDLE;
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        final boolean drawSelectorOnTop = mDrawSelectorOnTop;
        if (!drawSelectorOnTop) {
            drawSelector(canvas);
        }

        super.dispatchDraw(canvas);

        if (drawSelectorOnTop) {
            drawSelector(canvas);
        }
    }
    
    private void drawSelector(Canvas canvas) {
        if (!mSelectorRect.isEmpty() && mSelector != null /*&& mBeginClick*/ ) {
            final Drawable selector = mSelector;
            selector.setBounds(mSelectorRect);
            selector.draw(canvas);
        }
    }
    /*
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        //不使用边缘发光效果
//        if (mTopEdge != null) {
//            boolean needsInvalidate = false;
//            if (!mTopEdge.isFinished()) {
//                mTopEdge.draw(canvas);
//                needsInvalidate = true;
//            }
//            if (!mBottomEdge.isFinished()) {
//                final int restoreCount = canvas.save();
//                final int width = getWidth();
//                canvas.translate(-width, getHeight());
//                canvas.rotate(180, width, 0);
//                mBottomEdge.draw(canvas);
//                canvas.restoreToCount(restoreCount);
//                needsInvalidate = true;
//            }
//
//            if (needsInvalidate) {
//                invalidate();
//            }
//        }
        
//        drawSelector(canvas);
    }*/

    public void beginFastChildLayout() {
        mFastChildLayout = true;
    }

    public void endFastChildLayout() {
        mFastChildLayout = false;
        populate(false);
    }

    @Override
    public void requestLayout() {
        if (!mPopulating && !mFastChildLayout) {
            super.requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY) {
            widthMode = MeasureSpec.EXACTLY;
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            heightMode = MeasureSpec.EXACTLY;
        }

        setMeasuredDimension(widthSize, heightSize);

        if (mColCountSetting == COLUMN_COUNT_AUTO) {
            final int colCount = widthSize / mMinColWidth;
            if (colCount != mColCount) {
                mColCount = colCount;
            }
        }
    }
    
    /**
     * 是否允许控件内部各选项做切换动画，主要方便在某些情况下加载数据不需要动画的时候
     * @param flag
     */
    public void enableAnimation(boolean flag) {
    	mEnableAnimation = flag;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mInLayout = true;
        populate(false);
        mInLayout = false;

        //final int width = r - l;
        //final int height = b - t;
        //不使用边缘发光效果
        //mTopEdge.setSize(width, height);
        //mBottomEdge.setSize(width, height);
    }

    private void populate(boolean clearData) {

        if (getWidth() == 0 || getHeight() == 0 || mItemCount == 0) {
            return;
        }

        if (mFadeOuting) {
            //此时正在做选项删除的动画，不应当进行layout操作。
            return;
        }
        
        if (mColCount == COLUMN_COUNT_AUTO) {
            final int colCount = getWidth() / mMinColWidth;
            if (colCount != mColCount) {
                mColCount = colCount;
            }
        }

        final int colCount = mColCount;
        
        // setup arraylist for mappings
        if(mColMappings.size() != mColCount){
        	mColMappings.clear();
        	for(int i=0; i < mColCount; i++){
        		mColMappings.add(new ArrayList<Integer>());
        	}
        }
        
        if (mItemTops == null || mItemTops.length != colCount) {
        	mItemTops = new int[colCount];
            mItemBottoms = new int[colCount];
            
            mLayoutRecords.clear();
            if (mInLayout) {
                removeAllViewsInLayout();
            } else {
                removeAllViews(); 
            }
        }
        
        if((mRestoreOffsets != null) && mRestoreOffsets.length == colCount){
            mItemTops = mRestoreOffsets;
        }
        
        mPopulating = true;
        
        layoutChildren(mDataChanged);
        fillDown(mFirstPosition + getChildCount(), 0);
        fillUp(mFirstPosition - 1, 0);
        
        mPopulating = false;
        mDataChanged = false;
        
        //每次数据更新或者做删除等类似操作后，用户调用了notifyDataSetChanged()，为了保证重新layout后还能维持在原来显示的页面而不是重新从第一个item开始显示，这里根据之前记录的位置和偏移量将控件里面的内容
        //移动到之前记录的位置处
        if(mSync) {
            setPositionAndTop(mSyncPosition, mSpecificTop);
            
            if (mChoiceMode != CHOICE_MODE_NONE && mAdapter != null && mAdapter.hasStableIds() && mEnableAnimation) {
                mUpdateAnimatorSet = new AnimatorSet();
                mUpdateAnimatorSet.playTogether(getUpdateDataAnimation()); 
                //updateAnimatorSet.setStartDelay(150);
                mUpdateAnimatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        mUpdateing = true;
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mUpdateing = false;
                    }
                });
                
                mFadeOutViewAnimatorSet = new AnimatorSet();
                mFadeOutViewAnimatorSet.playTogether(getFadeOutAnimationsForStaleViews());
                mFadeOutViewAnimatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        mFadeOuting = true;
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mFadeOuting = false;
                        requestLayout();
                    }
                });
                
                mFadeOutViewAnimatorSet.start();
                mUpdateAnimatorSet.start();
                
            }
            
            mSync = false;
            mSyncPosition = INVALID_POSITION;
        }
        
        if(clearData){ 
            if(mRestoreOffsets != null)
                //Arrays.fill(mRestoreOffsets,0);
                mRestoreOffsets = null;
        }
    }
    
    /**
     * 将选项移动到给定的position
     * @param position 需要移动到的位置
     * @param top position对应的top
     */
    public void setPositionAndTop(int position, int top) {
        int firstPosition = getFirstPosition();
        int lastVisiblePosition = getLastVisiblePosition();
        int syncPosition = position;
        int sncTop = top;
//        if (syncPosition < 0 ) {
//            syncPosition = 0;
//        } else if (syncPosition >= mItemCount ) {
//            syncPosition = mItemCount - 1;
//        }
        //如果发现传入的position超出了adapter提供的count范围，则没必要进行下面的操作
        if (syncPosition < 0 || syncPosition >= mItemCount) {
        	return;
        }
        while(syncPosition < firstPosition || syncPosition > lastVisiblePosition) {
            if (syncPosition > lastVisiblePosition) {
                trackMotionScroll(-300, false);
            } else if (syncPosition < firstPosition ) {
                trackMotionScroll(300, false); 
            }
            if (mMoveBy == 0) {
                break;
            }
            firstPosition = getFirstPosition();
            lastVisiblePosition = getLastVisiblePosition();
        }
        View view = getChildAt(syncPosition - firstPosition);
        int delta = 0;
        if (view != null) {
            delta = view.getTop() - sncTop;
        }
        
        if (delta != 0) {
            trackMotionScroll(-delta, false);
            //在某些情况下delta的值可能超过了控件的高度，而在trackMotionScroll对移动的值做了限制，就可能出现一次移动不到位，所以需要再检测一次以保证移到相应位置
            view = getChildAt(syncPosition - mFirstPosition);
            if (view != null) {
                delta = view.getTop() - sncTop;
                if (delta != 0) {
                    trackMotionScroll(-delta, false);
                }
            }
        }
    }
    
    /**
     * 当瀑布流中有选项删除或者添加选项后，其他位置的选项也要相应的移动位置，此函数用来对其他已经存在的选项做移动动画，使得过渡比较平滑
     * @return
     */
    private List<Animator> getUpdateDataAnimation() {
        int size = mChildRectsForAnimation.size();
        List<Animator> locaList = new ArrayList<Animator>();
        if (size > 0) {
            int childCount = getChildCount();
            int position;
            if (childCount > 0) {
                for (int i = 0; i < childCount; i++) {
                    View view = getChildAt(i);
                    position = mFirstPosition + i;
                    LayoutParams params = (LayoutParams) view.getLayoutParams();
                    long id = params.id;
                    if (mChildRectsForAnimation.containsKey(id)) {
                        ViewRectPair viewRectPair = mChildRectsForAnimation.get(id);
                        Rect localRect = viewRectPair.rect;
                        //对于已经存在的选项，由于已经将其更新到新的位置了，为了能够做动画，需要先将其移回旧的位置，再将其从旧的位置做动画移动到新的位置
                        view.setTranslationX(localRect.left - view.getLeft());
                        view.setTranslationY(localRect.top - view.getTop());
                        
                        PropertyValuesHolder translationXHolder = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.getTranslationX(), 0.0f);
                        PropertyValuesHolder translationYHolder = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, view.getTranslationY(), 0.0f);
                        ObjectAnimator translationAnimator = ObjectAnimator.ofPropertyValuesHolder(view, translationXHolder, translationYHolder);
                        translationAnimator.setDuration(350); 
                        translationAnimator.setStartDelay(150 + i * 40);
                        translationAnimator.setInterpolator(mInterpolator);
                        locaList.add(translationAnimator);
                        mChildRectsForAnimation.remove(id);
                    } else {
                        //对于新添加到当前界面的选项，根据其在当前界面的位置移动先移动到左上角或者左下角。
                        if (i < childCount / 2) {
                            view.setTranslationX(0 - view.getLeft());
                            view.setTranslationY(-view.getHeight() - view.getTop());
                        } else {
                            view.setTranslationX(0 - view.getLeft());
                            view.setTranslationY(getHeight() - view.getTop());
                        }
                        
                        PropertyValuesHolder translationXHolder = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.getTranslationX(), 0.0f);
                        PropertyValuesHolder translationYHolder = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, view.getTranslationY(), 0.0f);
                        ObjectAnimator translationAnimator = ObjectAnimator.ofPropertyValuesHolder(view, translationXHolder, translationYHolder);
                        translationAnimator.setDuration(350);
                        translationAnimator.setStartDelay(150 + i * 40);
                        translationAnimator.setInterpolator(mInterpolator);
                        locaList.add(translationAnimator);
                    }
                }
            }
        }
        return locaList;
    }
    
    /**
     * 如果用户执行了删除选项的操作，需要给该选项制作一个删除的过渡动画，缩放的同时改变其透明度
     * @return
     */
    private List<Animator> getFadeOutAnimationsForStaleViews() {
        
        int size = mDeleteViews.size();
        List<Animator> locaList = new ArrayList<Animator>();
        
        for (int i = 0; i < size; i ++) {
            final View view = mDeleteViews.get(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            //经过之前的更新，删除的选项已经不存在瀑布流中，为了能够制作出删除的过渡动画，所以此处先将删除的选项重新添加到瀑布流中，等动画做完后再将其从当前的瀑布流中删除。
            if (mInLayout) {
                addViewInLayout(view, -1, lp);
            } else {
                addView(view);
            }
            
            AnimatorSet animatorSet = (AnimatorSet)AnimatorInflater.loadAnimator(mContext, R.anim.le_staggeredgridview_fade_out_anim);
            animatorSet.setInterpolator(new DecelerateInterpolator(1.1f));
            animatorSet.setTarget(view);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (mInLayout) {
                        removeViewInLayout(view);
                    } else {
                        removeView(view);
                    }
                    //为了能让该View在回收后以后可以继续适用，需要恢复它的默认状态再添加到回收器中
                    view.setAlpha(1.0f);
                    view.setScaleX(1.0f);
                    view.setScaleY(1.0f);
                    mRecycler.addScrap(view);
                }
            });           

            locaList.add(animatorSet);
            ObjectAnimator alphAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0.0f);
            alphAnimator.setDuration(200);
            alphAnimator.setInterpolator(new AccelerateInterpolator());
            alphAnimator.setStartDelay(50);
            locaList.add(alphAnimator);
        }
        
        return locaList;
    }
    
    
    final void offsetChildren(int offset) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            child.layout(child.getLeft(), child.getTop() + offset,
                    child.getRight(), child.getBottom() + offset);
        }

        final int colCount = mColCount;
        for (int i = 0; i < colCount; i++) {
            mItemTops[i] += offset;
            mItemBottoms[i] += offset;
        }
    }

    /**
     * Measure and layout all currently visible children.
     *
     * @param queryAdapter true to requery the adapter for view data
     */
    final void layoutChildren(boolean queryAdapter) {
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        final int itemMargin = mItemMargin;
        final int colWidth = (getWidth() - paddingLeft - paddingRight - itemMargin * (mColCount - 1)) / mColCount;
        mColWidth = colWidth;
        int rebuildLayoutRecordsBefore = -1;
        int rebuildLayoutRecordsAfter = -1;
        int[] tops = new int[mColCount];
        
        final int childCount = getChildCount();
        if (childCount != 0) {
            View view;
            LayoutParams tempLp;
            for (int i = 0, j = 0; j < mColCount && i < childCount; i++) {
                view = getChildAt(i); 
                tempLp = (LayoutParams) view.getLayoutParams();
                tops[tempLp.column] = view.getTop();
                j = j + tempLp.span;
            }
        }
        
        Arrays.fill(mItemBottoms, Integer.MIN_VALUE);

        int amountRemoved = 0;
        
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int col = lp.column;
            final int position = mFirstPosition + i;
            final boolean needsLayout = queryAdapter || child.isLayoutRequested();
            
            if (queryAdapter) {
                View newView = obtainView(position, child);
                if (newView == null) {
                    // child has been removed
                    removeViewAt(i);
                    if (i - 1 >= 0)
                        invalidateLayoutRecordsAfterPosition(i - 1);
                    amountRemoved++;
                    continue;
                } else if (newView != child) {
                    removeViewAt(i);
                    addView(newView, i);
                    child = newView;
                }
                lp = (LayoutParams) child.getLayoutParams(); // Might have changed
                lp.column = col;
                child.setLayoutParams(lp);
            }

            final int span = Math.min(mColCount, lp.span);
            final int widthSize = colWidth * span + itemMargin * (span - 1);

            if (needsLayout) {
                final int widthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);

                final int heightSpec;
                if (lp.height == LayoutParams.WRAP_CONTENT) {
                    heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                } else {
                    heightSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
                }

                child.measure(widthSpec, heightSpec);
            }

            int childTop = mItemBottoms[col] > Integer.MIN_VALUE ? mItemBottoms[col] + mItemMargin : tops[col];
            
            if (span > 1) {
                int lowest = childTop;
                for (int j = col + 1; j < col + span; j++) {
                    final int bottom = mItemBottoms[j] + mItemMargin;
                    if (bottom > lowest) {
                        lowest = bottom;
                    }
                }
                childTop = lowest; 
            }
            final int childHeight = child.getMeasuredHeight();
            final int childBottom = childTop + childHeight;
            final int childLeft = paddingLeft + col * (colWidth + itemMargin);
            final int childRight = childLeft + child.getMeasuredWidth();
            child.layout(childLeft, childTop, childRight, childBottom);
            
            for (int j = col; j < col + span; j++) {
                mItemBottoms[j] = childBottom;
            }
            
            final LayoutRecord rec = mLayoutRecords.get(position);
            if (rec != null && rec.height != childHeight) {
                // Invalidate our layout records for everything before this.
                rec.height = childHeight;
                rebuildLayoutRecordsBefore = position;
            }

            if (rec != null && rec.span != span) {
                // Invalidate our layout records for everything after this.
                rec.span = span;
                rebuildLayoutRecordsAfter = position;
            }
            
            if (rec != null && rec.column != col) {
                rec.column = col;
            }
        }

        // Update mItemBottoms for any empty columns
        for (int i = 0; i < mColCount; i++) {
            if (mItemBottoms[i] == Integer.MIN_VALUE) {
                mItemBottoms[i] = mItemTops[i];
            }
        }

        if (rebuildLayoutRecordsBefore >= 0 || rebuildLayoutRecordsAfter >= 0) {
            if (rebuildLayoutRecordsBefore >= 0) {
                invalidateLayoutRecordsBeforePosition(rebuildLayoutRecordsBefore);
            }
            if (rebuildLayoutRecordsAfter >= 0) {
                invalidateLayoutRecordsAfterPosition(rebuildLayoutRecordsAfter);
            }
            for (int i = 0; i < (childCount - amountRemoved); i++) {
                final int position = mFirstPosition + i;
                final View child = getChildAt(i);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                LayoutRecord rec = mLayoutRecords.get(position);
                if (rec == null) {
                    rec = new LayoutRecord();
                    mLayoutRecords.put(position, rec);
                }
                rec.column = lp.column;
                rec.height = child.getHeight();
                rec.id = lp.id;
                rec.span = Math.min(mColCount, lp.span);
            }
        }
        
        if(this.mSelectorPosition != INVALID_POSITION){
        	View child = getChildAt(mMotionPosition - mFirstPosition);
        	if (child != null) positionSelector(mMotionPosition, child);
         } else if (mTouchMode > TOUCH_MODE_DOWN) {
             View child = getChildAt(mMotionPosition - mFirstPosition);
             if (child != null) positionSelector(mMotionPosition, child);
         } else {
             mSelectorRect.setEmpty();
         }
    }

    final void invalidateLayoutRecordsBeforePosition(int position) {
        int endAt = 0;
        while (endAt < mLayoutRecords.size() && mLayoutRecords.keyAt(endAt) < position) {
            endAt++;
        }

        final int end = Math.min(mLayoutRecords.size(), endAt);
        for (int i = 0; i < end; i++) {
            mLayoutRecords.removeAt(i);
        }
        //mLayoutRecords.removeAtRange(0, endAt);
    }

    final void invalidateLayoutRecordsAfterPosition(int position) {
        int beginAt = mLayoutRecords.size() - 1;
        while (beginAt >= 0 && mLayoutRecords.keyAt(beginAt) > position) {
            beginAt--;
        }
        beginAt++;
        
        final int end = mLayoutRecords.size();
        for (int i = beginAt + 1; i < end; i++) {
            mLayoutRecords.removeAt(i);
        }
        
        //mLayoutRecords.removeAtRange(beginAt + 1, mLayoutRecords.size() - beginAt);
    }
    
    /**
     * Should be called with mPopulating set to true
     *
     * @param fromPosition Position to start filling from
     * @param overhang the number of extra pixels to fill beyond the current top edge
     * @return the max overhang beyond the beginning of the view of any added items at the top
     */
    final int fillUp(int fromPosition, int overhang) {
    	
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        final int itemMargin = mItemMargin;
        final int colWidth =
                (getWidth() - paddingLeft - paddingRight - itemMargin * (mColCount - 1)) / mColCount;
        mColWidth = colWidth;
        final int gridTop = getPaddingTop();
        final int fillTo = gridTop - overhang;
        int nextCol = getNextColumnUp();
        int position = fromPosition;

        while (nextCol >= 0 && mItemTops[nextCol] > fillTo && position >= 0) {
            // make sure the nextCol is correct. check to see if has been mapped 
            // otherwise stick to getNextColumnUp()
            if (!mColMappings.get(nextCol).contains((Integer) position)) {
                for (int i = 0; i < mColMappings.size(); i++) {
                    if (mColMappings.get(i).contains((Integer) position)) {
                        nextCol = i;
                        break;
                    }
                }
            }
            // displayMapping();
            final View child = obtainView(position, null);
            if (child == null)
                continue;

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            
            if (lp == null) {
                lp = this.generateDefaultLayoutParams();
                child.setLayoutParams(lp);
            }

            if (child.getParent() != this) {
                if (mInLayout) {
                    addViewInLayout(child, 0, lp);
                } else {
                    addView(child, 0);
                }
            }

            final int span = Math.min(mColCount, lp.span);
            final int widthSize = colWidth * span + itemMargin * (span - 1);
            final int widthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);

            LayoutRecord rec;
            if (span > 1) {
                rec = getNextRecordUp(position, span);
//                nextCol = rec.column;
            } else {
                rec = mLayoutRecords.get(position);
            }

            boolean invalidateBefore = false;
            if (rec == null) {
                rec = new LayoutRecord();
                mLayoutRecords.put(position, rec);
                rec.column = nextCol;
                rec.span = span;
            } else if (span != rec.span) {
                rec.span = span;
                rec.column = nextCol;
                invalidateBefore = true;
            } else {
//                nextCol = rec.column;
            }

            if (mHasStableIds) {
                final long id = mAdapter.getItemId(position);
                rec.id = id;
                lp.id = id;
            }

            lp.column = nextCol;

            final int heightSpec;
            if (lp.height == LayoutParams.WRAP_CONTENT) {
                heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            } else {
                heightSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
            }
            child.measure(widthSpec, heightSpec);

            final int childHeight = child.getMeasuredHeight();
            if (invalidateBefore || (childHeight != rec.height && rec.height > 0)) {
                invalidateLayoutRecordsBeforePosition(position);
            }
            rec.height = childHeight;
            
            int itemTop = mItemTops[nextCol];
            
            final int startFrom;
            if (span > 1) {
                int highest = mItemTops[nextCol];
                for (int i = nextCol + 1; i < nextCol + span; i++) {
                    final int top = mItemTops[i];
                    if (top < highest) {
                        highest = top;
                    }
                }
                startFrom = highest;
            } else {
                startFrom = mItemTops[nextCol];
            }
            
            
            int childBottom = startFrom;
            int childTop = childBottom - childHeight;
            final int childLeft = paddingLeft + nextCol * (colWidth + itemMargin);
            final int childRight = childLeft + child.getMeasuredWidth();
            
//            if(position == 0){
//              if(this.getChildCount()>1 && this.mColCount>1){
//                 childTop = this.getChildAt(1).getTop();
//                 childBottom = childTop + childHeight;
//              }
//            }
            
            child.layout(childLeft, childTop, childRight, childBottom);
            if (DEBUG) {
            	Log.i("SGV", "fillUp position = " + position + "--childTop = " + childTop + "--alpha = " + child.getAlpha());
            }
            
            for (int i = nextCol; i < nextCol + span; i++) { 
                mItemTops[i] = childTop - rec.getMarginAbove(i-nextCol) - itemMargin;
            }

            nextCol = getNextColumnUp();
            mFirstPosition = position--;
        }

        int highestView = getHeight();
        
        for (int i = 0; i < mColCount; i++) {
            /*
            final View child = getFirstChildAtColumn(i);
            if(child == null){
                highestView = 0;
                break;
            }
            final int top = child.getTop();

            if (top < highestView) {
                highestView = top;
            }*/

            if (mItemTops[i] < highestView) {
                highestView = mItemTops[i];
            }
        }
        
        return gridTop - highestView;
    }

    
    /*private View getFirstChildAtColumn(int column) {
        if (this.getChildCount() > column) {
            for (int i = 0; i < this.mColCount; i++) {
                final View child = getChildAt(i);
                final int left = child.getLeft();
                if (child != null) {
                    int col = 0;
                    // determine the column by cycling widths
                    while (left > col * (this.mColWidth + mItemMargin * 2)
                            + getPaddingLeft()) {
                        col++;
                    }
                    if (col == column) {
                        return child;
                    }
                }
            }
        }
        return null;
    }*/
    
    /**
     * Should be called with mPopulating set to true
     *
     * @param fromPosition Position to start filling from
     * @param overhang the number of extra pixels to fill beyond the current bottom edge
     * @return the max overhang beyond the end of the view of any added items at the bottom
     */
    final int fillDown(int fromPosition, int overhang) {
    	
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        final int itemMargin = mItemMargin;
        final int colWidth = (getWidth() - paddingLeft - paddingRight - itemMargin * (mColCount - 1)) / mColCount;
        final int gridBottom = getHeight() - getPaddingBottom();
        final int fillTo = gridBottom + overhang;
        int nextCol = getNextColumnDown();
        int position = fromPosition;

        while (nextCol >= 0 && mItemBottoms[nextCol] < fillTo && position < mItemCount) {
        	
        	final View child = obtainView(position, null);
        	
        	if(child == null) continue;
        	
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if(lp == null){
            	lp = this.generateDefaultLayoutParams();
            	child.setLayoutParams(lp);
            }
            if (child.getParent() != this) {
                if (mInLayout) {
                    addViewInLayout(child, -1, lp);
                } else {
                    addView(child);
                }
            }

            final int span = Math.min(mColCount, lp.span);
            final int widthSize = colWidth * span + itemMargin * (span - 1);
            final int widthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);

            LayoutRecord rec;
            if (span > 1) {
                rec = getNextRecordDown(position, span);
//                nextCol = rec.column;
            } else {
                rec = mLayoutRecords.get(position);
            }

            boolean invalidateAfter = false;
            if (rec == null) {
                rec = new LayoutRecord();
                mLayoutRecords.put(position, rec);
                rec.column = nextCol;
                rec.span = span;
            } else if (span != rec.span) {
                rec.span = span;
                rec.column = nextCol;
                invalidateAfter = true;
            } else {
//                nextCol = rec.column;
            }

            if (mHasStableIds) {
                final long id = mAdapter.getItemId(position);
                rec.id = id;
                lp.id = id;
            }

            lp.column = nextCol;

            final int heightSpec;
            if (lp.height == LayoutParams.WRAP_CONTENT) {
                heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            } else {
                heightSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
            }
            child.measure(widthSpec, heightSpec);

            final int childHeight = child.getMeasuredHeight();
            if (invalidateAfter || (childHeight != rec.height && rec.height > 0)) {
                invalidateLayoutRecordsAfterPosition(position);
            }
            rec.height = childHeight;

            final int startFrom;
            if (span > 1) {
                int lowest = mItemBottoms[nextCol];
                for (int i = nextCol + 1; i < nextCol + span; i++) {
                    final int bottom = mItemBottoms[i];
                    if (bottom > lowest) {
                        lowest = bottom;
                    }
                }
                startFrom = lowest;
            } else {
                startFrom = mItemBottoms[nextCol];
            }
            
            final int childTop = startFrom + itemMargin;
            final int childBottom = childTop + childHeight;
            final int childLeft = paddingLeft + nextCol * (colWidth + itemMargin);
            final int childRight = childLeft + child.getMeasuredWidth();
            child.layout(childLeft, childTop, childRight, childBottom);
            if (DEBUG) {
            	Log.i("SGV", "fillDown position = " + position + "--childTop = " + childTop + "--alpha = " + child.getAlpha());
            }
            // add the position to the mapping
            if(!mColMappings.get(nextCol).contains(position)){
            	
            	// check to see if the mapping exists in other columns
            	// this would happen if list has been updated
            	for(ArrayList<Integer> list : mColMappings){
            		if(list.contains(position)){
            			list.remove((Integer) position);
            		}
            	}
            	
            	mColMappings.get(nextCol).add(position);
            }
            	
            for (int i = nextCol; i < nextCol + span; i++) {
                mItemBottoms[i] = childBottom + rec.getMarginBelow(i - nextCol);
            }
            
			nextCol = getNextColumnDown();
            position++;
        }

        int lowestView = 0;
        for (int i = 0; i < mColCount; i++) {
            if (mItemBottoms[i] > lowestView) {
                lowestView = mItemBottoms[i];
            }
        }
        return lowestView - gridBottom;
    }

    /**
     * for debug purposes
     */
    /*private void displayMapping() {
        Log.w("DISPLAY", "MAP ****************");
        StringBuilder sb = new StringBuilder();
        int col = 0;

        for (ArrayList<Integer> map : this.mColMappings) {
            sb.append("COL" + col + ":");
            sb.append(' ');
            for (Integer i : map) {
                sb.append(i);
                sb.append(" , ");
            }
            Log.w("DISPLAY", sb.toString());
            sb = new StringBuilder();
            col++;
        }
        Log.w("DISPLAY", "MAP END ****************");
    }*/
    
    /**
     * @return column that the next view filling upwards should occupy. This is the bottom-most
     *         position available for a single-column item.
     */
    final int getNextColumnUp() {
        int result = -1;
        int bottomMost = Integer.MIN_VALUE;

        final int colCount = mColCount;
        for (int i = colCount - 1; i >= 0; i--) {
            final int top = mItemTops[i];
            if (top > bottomMost) {
                bottomMost = top;
                result = i;
            }
        }
        return result;
    }

    /**
     * 当跨度span超过一列的时候，从当前所有的列中，从左到右依次找出在跨度span范围内的所有列中最长的那一个列
     * 
     * Return a LayoutRecord for the given position
     * @param position
     * @param span
     * @return
     */
    final LayoutRecord getNextRecordUp(int position, int span) {
        LayoutRecord rec = mLayoutRecords.get(position);
        if (rec == null) {
            rec = new LayoutRecord();
            rec.span = span;
            mLayoutRecords.put(position, rec);
        } else if (rec.span != span) {
            throw new IllegalStateException("Invalid LayoutRecord! Record had span=" + rec.span +
                    " but caller requested span=" + span + " for position=" + position);
        }
        int targetCol = -1;
        int bottomMost = Integer.MIN_VALUE;

        final int colCount = mColCount;
        for (int i = colCount - span; i >= 0; i--) {
            int top = Integer.MAX_VALUE;
            for (int j = i; j < i + span; j++) {
                final int singleTop = mItemTops[j];
                if (singleTop < top) {
                    top = singleTop;
                }
            }
            if (top > bottomMost) {
                bottomMost = top;
                targetCol = i;
            }
        }

        rec.column = targetCol;

        for (int i = 0; i < span; i++) {
            rec.setMarginBelow(i, mItemTops[i + targetCol] - bottomMost);
        }

        return rec;
    }

    /**
     * @return column that the next view filling downwards should occupy. This is the top-most
     *         position available.
     */
    final int getNextColumnDown() {
        int result = -1;
        int topMost = Integer.MAX_VALUE;

        final int colCount = mColCount;
        
        for (int i = 0; i < colCount; i++) {
            final int bottom = mItemBottoms[i];
            if (bottom < topMost) {
                topMost = bottom;
                result = i;
            }
        }
        
        return result;
    }

    /**
     * 当跨度span超过一列的时候，从当前所有的列中，从左到右依次找出在跨度span范围内的所有列中最长的那一个列
     */
    final LayoutRecord getNextRecordDown(int position, int span) {
        LayoutRecord rec = mLayoutRecords.get(position);
        if (rec == null) {
            rec = new LayoutRecord();
            rec.span = span;
            mLayoutRecords.put(position, rec);
        } else if (rec.span != span) {
            throw new IllegalStateException("Invalid LayoutRecord! Record had span=" + rec.span +
                    " but caller requested span=" + span + " for position=" + position);
        }
        int targetCol = -1;
        int topMost = Integer.MAX_VALUE;

        final int colCount = mColCount;
        for (int i = 0; i <= colCount - span; i++) {
            int bottom = Integer.MIN_VALUE;
            for (int j = i; j < i + span; j++) {
                final int singleBottom = mItemBottoms[j];
                if (singleBottom > bottom) {
                    bottom = singleBottom;
                }
            }
            if (bottom < topMost) {
                topMost = bottom;
                targetCol = i;
            }
        }

        rec.column = targetCol;

        for (int i = 0; i < span; i++) {
            rec.setMarginAbove(i, topMost - mItemBottoms[i + targetCol]);
        }

        return rec;
    }

    /**
     * Obtain a populated view from the adapter. If optScrap is non-null and is not
     * reused it will be placed in the recycle bin.
     *
     * @param position position to get view for
     * @param optScrap Optional scrap view; will be reused if possible
     * @return A new view, a recycled view from mRecycler, or optScrap
     */
    final View obtainView(int position, View optScrap) {
        View view = mRecycler.getTransientStateView(position);
        if (view != null) {
            return view;
        }
        
        if(position >= mAdapter.getCount()){
            view = null;
            return null;
        }

        // Reuse optScrap if it's of the right type (and not null)
        final int optType = optScrap != null ?
                ((LayoutParams) optScrap.getLayoutParams()).viewType : -1;
        final int positionViewType = mAdapter.getItemViewType(position);
        final View scrap = optType == positionViewType ?
                optScrap : mRecycler.getScrapView(positionViewType, mAdapter.getItemId(position));
        

        view = mAdapter.getView(position, scrap, this);
        
        if (view != scrap && scrap != null) {
            // The adapter didn't use it; put it back.
            mRecycler.addScrap(scrap);
        }

        ViewGroup.LayoutParams vlp = view.getLayoutParams();
        LayoutParams lp = null;
        if (view.getParent() != this) {
            if (vlp == null) {
                lp = generateDefaultLayoutParams();
            } else if (!checkLayoutParams(vlp)) {
                lp = generateLayoutParams(vlp);
            } else {
                lp = (LayoutParams)vlp;
            }
            lp.id = mAdapter.getItemId(position);
            view.setLayoutParams(lp);
        }

        LayoutParams sglp;
        if (vlp != null) {
            sglp = (LayoutParams) vlp;
        } else {
            sglp = lp;
        }
        sglp.position = position;
        sglp.viewType = positionViewType;

        return view;
    }

    public ListAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mObserver);
        }
        // TODO: If the new adapter says that there are stable IDs, remove certain layout records
        // and onscreen views if they have changed instead of removing all of the state here.
        clearAllState();
        mAdapter = adapter;
        mDataChanged = true;
        
        if (adapter != null) {
            mItemCount = adapter.getCount();
            adapter.registerDataSetObserver(mObserver);
            mRecycler.setViewTypeCount(adapter.getViewTypeCount());
            mHasStableIds = adapter.hasStableIds();
            if (mChoiceMode != CHOICE_MODE_NONE && mHasStableIds &&
                    mCheckedIdStates == null) {
                mCheckedIdStates = new LongSparseArray<Integer>();
            }
        } else {
            mHasStableIds = false;
        }
        if (mCheckStates != null) {
            mCheckStates.clear();
        }

        if (mCheckedIdStates != null) {
            mCheckedIdStates.clear();
        }
        populate(adapter!=null);
    }

    /**
     * Clear all state because the grid will be used for a completely different set of data.
     */
    private void clearAllState() {
        // Clear all layout records and views
        mLayoutRecords.clear();
        removeAllViews();

        // Reset to the top of the grid
        resetStateForGridTop();

        // Clear recycler because there could be different view types now
        mRecycler.clear();
        
        mSelectorRect.setEmpty();
        mSelectorPosition = INVALID_POSITION;
    }

    /**
     * Reset all internal state to be at the top of the grid.
     */
    private void resetStateForGridTop() {
        // Reset mItemTops and mItemBottoms
        final int colCount = mColCount;
        if (mItemTops == null || mItemTops.length != colCount) {
            mItemTops = new int[colCount];
            mItemBottoms = new int[colCount];
        }
        final int top = getPaddingTop();
        Arrays.fill(mItemTops, top);
        Arrays.fill(mItemBottoms, top);

        // Reset the first visible position in the grid to be item 0
        mFirstPosition = 0;
        if(mRestoreOffsets != null)
//        	Arrays.fill(mRestoreOffsets, 0);
        	mRestoreOffsets = null;
        mPositionScrollAfterLayout = null;
        mCurrentOverScrollDistance = 0;
    }

    /**
     * Scroll the list so the first visible position in the grid is the first item in the adapter.
     */
    public void setSelectionToTop() {
        // Clear out the views (but don't clear out the layout records or recycler because the data
        // has not changed)
        if (mLayoutAnim) { 
            return;
        }
        
        if (mFadeOuting && mFadeOutViewAnimatorSet != null && mFirstPosition != 0) {
            mFadeOutViewAnimatorSet.end();
        }
        
        if (mUpdateing && mUpdateAnimatorSet != null && mFirstPosition != 0) {
            mUpdateAnimatorSet.end();
        }
        
        if (mDataChanged || mSync) {
            // Wait until we're back in a stable state to try this.
            Runnable runnable = new Runnable() {
                @Override 
                public void run() {
                    setSelectionToTop();
                }
            };
            Handler handler = getHandler();
            if (handler != null) {
                handler.postDelayed(runnable, 200);
            }
            return;
        }
        int firstPosition = mFirstPosition;

        if (firstPosition == 0) {
            requestLayout();
        } else {
            recycleAllViews();
            // Reset to top of grid
            resetStateForGridTop();
            if (mEnableAnimation) {
            	startLayoutAnimation();
            } else {
            	requestLayout();
            }
        }
        
    }
    
/**
     * Perform a quick, in-place update of the checked or activated state
     * on all visible item views. This should only be called when a valid
     * choice mode is active.
     */
    private void updateOnScreenCheckedViews() {
        final int firstPos = mFirstPosition;
        final int count = getChildCount();
        final boolean useActivated = getContext().getApplicationInfo().targetSdkVersion
                >= Build.VERSION_CODES.HONEYCOMB;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final int position = firstPos + i;

            if (child instanceof Checkable) {
                ((Checkable) child).setChecked(mCheckStates.get(position));
            } else if (useActivated) {
                child.setActivated(mCheckStates.get(position));
            }
        }
    }
    /**
     * @see #setChoiceMode(int)
     *
     * @return The current choice mode
     */
    public int getChoiceMode() {
        return mChoiceMode;
    }


    /**
     * Defines the choice behavior for the StaggeredGridView. By default, StaggeredGridView do not have any choice behavior
     * ({@link #CHOICE_MODE_NONE}). By setting the choiceMode to
     * {@link #CHOICE_MODE_MULTIPLE_MODAL}, the list allows any number of items to be chosen.
     *
     * @param choiceMode One of {@link #CHOICE_MODE_NONE}, {@link #CHOICE_MODE_MULTIPLE_MODAL}
     */
    public void setChoiceMode(int choiceMode) {
        mChoiceMode = choiceMode;
        if (mChoiceActionMode != null) {
            mChoiceActionMode.finish();
            mChoiceActionMode = null;
        }
        if (mChoiceMode != CHOICE_MODE_NONE) {
            if (mCheckStates == null) {
                mCheckStates = new SparseBooleanArray();
            }
            if (mCheckedIdStates == null && mAdapter != null && mAdapter.hasStableIds()) {
                mCheckedIdStates = new LongSparseArray<Integer>();
            }
            // Modal multi-choice mode only has choices when the mode is active. Clear them.
            if (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
                clearChoices();
                setLongClickable(true);
            }
        }
    }
    
    /**
     * Clear any choices previously set
     */
    private void clearChoices() {
        if (mCheckStates != null) {
            mCheckStates.clear();
        }
        if (mCheckedIdStates != null) {
            mCheckedIdStates.clear();
        }
        mCheckedItemCount = 0;
    }
    
    /**
     * Sets the checked state of the specified position. The is only valid if
     * the choice mode has been set to 
     * {@link #CHOICE_MODE_MULTIPLE_MODAL}.
     *
     * @param position The item whose checked state is to be checked
     * @param value The new checked state for the item
     */
    public void setItemChecked(int position, boolean value) {
        if (mChoiceMode == CHOICE_MODE_NONE) {
            return;
        }

        // Start selection mode if needed. We don't need to if we're unchecking something.
        if (value && mChoiceActionMode == null
                && mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
            if (mMultiChoiceModeCallback == null ||
                    !mMultiChoiceModeCallback.hasWrappedCallback()) {
                throw new IllegalStateException("StaggeredGridView: attempted to start selection mode " +
                        "for CHOICE_MODE_MULTIPLE_MODAL but no choice mode callback was " +
                        "supplied. Call setMultiChoiceModeListener to set a callback.");
            }
            mChoiceActionMode = startActionMode(mMultiChoiceModeCallback);
        }

        if (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
            boolean oldValue = mCheckStates.get(position);
            mCheckStates.put(position, value);
            if (mCheckedIdStates != null && mAdapter.hasStableIds()) {
                if (value) {
                    mCheckedIdStates.put(mAdapter.getItemId(position), position);
                } else {
                    mCheckedIdStates.delete(mAdapter.getItemId(position));
                }
            }
            if (oldValue != value) {
                if (value) {
                    mCheckedItemCount++;
                } else {
                    mCheckedItemCount--;
                }
            }
            if (mChoiceActionMode != null) {
                final long id = mAdapter.getItemId(position);
                mMultiChoiceModeCallback.onItemCheckedStateChanged(mChoiceActionMode,
                        position, id, value);
            }
        } else {
            boolean updateIds = mCheckedIdStates != null && mAdapter.hasStableIds();
            // Clear all values if we're checking something, or unchecking the currently
            // selected item
            if (value || isItemChecked(position)) {
                mCheckStates.clear();
                if (updateIds) {
                    mCheckedIdStates.clear();
                }
            }
            // this may end up selecting the value we just cleared but this way
            // we ensure length of mCheckStates is 1, a fact getCheckedItemPosition relies on
            if (value) {
                mCheckStates.put(position, true);
                if (updateIds) {
                    mCheckedIdStates.put(mAdapter.getItemId(position), position);
                }
                mCheckedItemCount = 1;
            } else if (mCheckStates.size() == 0 || !mCheckStates.valueAt(0)) {
                mCheckedItemCount = 0;
            }
        } 

        // Do not generate a data change while we are in the layout phase
        if (!mInLayout) {
//            mDataChanged = true;
            //requestLayout();
            invalidateViews();

        }
    }
    
    /**
     * Set a {@link com.letv.shared.widget.StaggeredGridView.MultiChoiceModeListener} that will manage the lifecycle of the
     * selection {@link android.view.ActionMode}. Only used when the choice mode is set to
     * {@link #CHOICE_MODE_MULTIPLE_MODAL}.
     *
     * @param listener Listener that will manage the selection mode
     *
     * @see #setChoiceMode(int)
     */
    public void setMultiChoiceModeListener(MultiChoiceModeListener listener) {
        if (mMultiChoiceModeCallback == null) {
            mMultiChoiceModeCallback = new MultiChoiceModeWrapper();
        }
        mMultiChoiceModeCallback.setWrapped(listener);
    }

    /**
     * A MultiChoiceModeListener receives events for {@link com.letv.shared.widget.StaggeredGridView#CHOICE_MODE_MULTIPLE_MODAL}.
     * It acts as the {@link android.view.ActionMode.Callback} for the selection mode and also receives
     * {@link #onItemCheckedStateChanged(android.view.ActionMode, int, long, boolean)} events when the user
     * selects and deselects list items.
     */
    public interface MultiChoiceModeListener extends ActionMode.Callback {
        /**
         * Called when an item is checked or unchecked during selection mode.
         *
         * @param mode The {@link android.view.ActionMode} providing the selection mode
         * @param position Adapter position of the item that was checked or unchecked
         * @param id Adapter ID of the item that was checked or unchecked
         * @param checked <code>true</code> if the item is now checked, <code>false</code>
         *                if the item is now unchecked.
         */
        public void onItemCheckedStateChanged(ActionMode mode,
                                              int position, long id, boolean checked);
    }

    class MultiChoiceModeWrapper implements MultiChoiceModeListener {
        private MultiChoiceModeListener mWrapped;

        public void setWrapped(MultiChoiceModeListener wrapped) {
            mWrapped = wrapped;
        }
        
        public boolean hasWrappedCallback() {
            return mWrapped != null;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (mWrapped.onCreateActionMode(mode, menu)) {
                // Initialize checked graphic state?
                if(mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
                    //内部定制，进入选择模式后仍然可以长按
                    setLongClickable(true);
                } else {
                    setLongClickable(false);
                }
                
                return true;
            }
            return false;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return mWrapped.onPrepareActionMode(mode, menu);
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return mWrapped.onActionItemClicked(mode, item);
        }

        public void onDestroyActionMode(ActionMode mode) {
            mWrapped.onDestroyActionMode(mode);
            
            mChoiceActionMode = null;

            // Ending selection mode means deselecting everything.
            clearChoices();

//            mDataChanged = true;
//            requestLayout();
            invalidateViews();

            setLongClickable(true);
        }
        
        public void onItemCheckedStateChanged(ActionMode mode,
                int position, long id, boolean checked) {
            mWrapped.onItemCheckedStateChanged(mode, position, id, checked);

            // If there are no items selected we no longer need the selection mode.
            if (getCheckedItemCount() == 0) {
                mode.finish();
            }
        }
    }

    /**
     * Returns the number of items currently selected. This will only be valid
     * if the choice mode is not {@link #CHOICE_MODE_NONE} (default).
     *
     * <p>To determine the specific items that are currently selected, use one of
     * the <code>getChecked*</code> methods.
     *
     * @return The number of items currently selected
     *
     * @see #getCheckedItemPosition()
     * @see #getCheckedItemPositions()
     * @see #getCheckedItemIds()
     */
    public int getCheckedItemCount() {
        return mCheckedItemCount;
    }

    /**
     * Returns the set of checked items in the list. The result is only valid if
     * the choice mode has not been set to {@link #CHOICE_MODE_NONE}.
     *
     * @return  A SparseBooleanArray which will return true for each call to
     *          get(int position) where position is a position in the list,
     *          or <code>null</code> if the choice mode is set to
     *          {@link #CHOICE_MODE_NONE}.
     */
    public SparseBooleanArray getCheckedItemPositions() {
        if (mChoiceMode != CHOICE_MODE_NONE) {
            return mCheckStates;
        }
        return null;
    }    
    
    /**
     * Returns the checked state of the specified position. The result is only
     * valid if the choice mode has been set to {@link #CHOICE_MODE_MULTIPLE_MODAL}.
     *
     * @param position The item whose checked state to return
     * @return The item's checked state or <code>false</code> if choice mode
     *         is invalid
     *
     * @see #setChoiceMode(int)
     */
    public boolean isItemChecked(int position) {
        if (mChoiceMode != CHOICE_MODE_NONE && mCheckStates != null) {
            return mCheckStates.get(position);
        }

        return false;
    }
    
    /**
     * Returns the set of checked items ids. The result is only valid if the
     * choice mode has not been set to {@link #CHOICE_MODE_NONE} and the adapter
     * has stable IDs. ({@link android.widget.ListAdapter#hasStableIds()} == {@code true})
     *
     * @return A new array which contains the id of each checked item in the
     *         list.
     */
    public long[] getCheckedItemIds() {
        if (mChoiceMode == CHOICE_MODE_NONE || mCheckedIdStates == null || mAdapter == null) {
            return new long[0];
        }

        final LongSparseArray<Integer> idStates = mCheckedIdStates;
        final int count = idStates.size();
        final long[] ids = new long[count];

        for (int i = 0; i < count; i++) {
            ids[i] = idStates.keyAt(i);
        }
        return ids;
    }
    
    
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
    	return new LayoutParams(lp);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams lp) {
        return lp instanceof LayoutParams;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState ss = new SavedState(superState);
        final int position = mFirstPosition;
        ss.position = position;
        if (position >= 0 && mAdapter != null && position < mAdapter.getCount()) {
            ss.firstId = mAdapter.getItemId(position);
        }
        
        int topOffsets[]= new int[this.mColCount];
        if (getChildCount() > 0) {
        	if(this.mColWidth>0){
            	topOffsets = mItemTops;
        	}
            ss.topOffsets = topOffsets;
            
            // convert nested arraylist so it can be parcelable 
            ArrayList<ColMap> convert = new ArrayList<ColMap>();
            for(ArrayList<Integer> cols : mColMappings){
            	convert.add(new ColMap(cols));
            }
            
            ss.mapping = convert;
        } else {
            ss.topOffsets = topOffsets;
            ss.mapping = new ArrayList<ColMap>();
        }
        
        ss.inActionMode = (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) && mChoiceActionMode != null;

        if (mCheckStates != null) {
            ss.checkState = mCheckStates.clone();
        }
        if (mCheckedIdStates != null) {
            final LongSparseArray<Integer> idState = new LongSparseArray<Integer>();
            final int count = mCheckedIdStates.size();
            for (int i = 0; i < count; i++) {
                idState.put(mCheckedIdStates.keyAt(i), mCheckedIdStates.valueAt(i));
            }
            ss.checkIdState = idState;
        }
        ss.checkedItemCount = mCheckedItemCount;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mDataChanged = true;
        mFirstPosition = ss.position;
        mRestoreOffsets = ss.topOffsets;
        
        if (ss.inActionMode && mChoiceActionMode == null && mMultiChoiceModeCallback != null 
                && mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
            mChoiceActionMode = startActionMode(mMultiChoiceModeCallback);
        }

        if (ss.checkState != null) {
            mCheckStates = ss.checkState;
        }
    
        if (ss.checkIdState != null) {
            mCheckedIdStates = ss.checkIdState;
        }
    
        mCheckedItemCount = ss.checkedItemCount;
        ArrayList<ColMap> convert = ss.mapping;
        
        if(convert != null){
            mColMappings.clear();
            for(ColMap colMap : convert){
                mColMappings.add(colMap.values);
            }
        }
        
        if(ss.firstId>=0){
            this.mFirstAdapterId = ss.firstId;
            mSelectorPosition = INVALID_POSITION;	
        }
        
//        requestLayout();
        invalidateViews();
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        private static final int[] LAYOUT_ATTRS = new int[] {android.R.attr.layout_span};

        private static final int SPAN_INDEX = 0;

        /**
         * The number of columns this item should span
         */
        public int span = 1;

        /**
         * Item position this view represents
         */
        int position;

        /**
         * Type of this view as reported by the adapter
         */
        int viewType;

        /**
         * The column this view is occupying
         */
        int column;

        /**
         * The stable ID of the item this view displays
         */
        long id = -1;

        public LayoutParams(int height) {
            super(MATCH_PARENT, height);

            if (this.height == MATCH_PARENT) {
                Log.w(TAG, "Constructing LayoutParams with height FILL_PARENT - " +
                        "impossible! Falling back to WRAP_CONTENT");
                this.height = WRAP_CONTENT;
            }
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            if (this.width != MATCH_PARENT) {
                Log.w(TAG, "Inflation setting LayoutParams width to " + this.width +
                        " - must be MATCH_PARENT");
                this.width = MATCH_PARENT;
            }
            if (this.height == MATCH_PARENT) {
                Log.w(TAG, "Inflation setting LayoutParams height to MATCH_PARENT - " +
                        "impossible! Falling back to WRAP_CONTENT");
                this.height = WRAP_CONTENT;
            }

            TypedArray a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
            span = a.getInteger(SPAN_INDEX, 1);
            a.recycle();
        }

        public LayoutParams(ViewGroup.LayoutParams other) {
            super(other);

            if (this.width != MATCH_PARENT) {
                Log.w(TAG, "Constructing LayoutParams with width " + this.width +
                        " - must be MATCH_PARENT");
                this.width = MATCH_PARENT;
            }
            if (this.height == MATCH_PARENT) {
                Log.w(TAG, "Constructing LayoutParams with height MATCH_PARENT - " +
                        "impossible! Falling back to WRAP_CONTENT");
                this.height = WRAP_CONTENT;
            }
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getViewType() {
            return viewType;
        }

        public void setViewType(int viewType) {
            this.viewType = viewType;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

    }

    private class RecycleBin {
        private ArrayList<View>[] mScrapViews;
        private int mViewTypeCount;
        private int mMaxScrap;

        private SparseArray<View> mTransientStateViews;

        public void setViewTypeCount(int viewTypeCount) {
            if (viewTypeCount < 1) {
                throw new IllegalArgumentException("Must have at least one view type (" +
                        viewTypeCount + " types reported)");
            }
            if (viewTypeCount == mViewTypeCount) {
                return;
            }

            @SuppressWarnings("unchecked")
			ArrayList<View>[] scrapViews = new ArrayList[viewTypeCount];
            
            for (int i = 0; i < viewTypeCount; i++) {
                scrapViews[i] = new ArrayList<View>();
            }
            mViewTypeCount = viewTypeCount;
            mScrapViews = scrapViews;
        }

        public void clear() {
            final int typeCount = mViewTypeCount;
            for (int i = 0; i < typeCount; i++) {
                mScrapViews[i].clear();
            }
            if (mTransientStateViews != null) {
                mTransientStateViews.clear();
            }
        }

        public void clearTransientViews() {
            if (mTransientStateViews != null) {
                mTransientStateViews.clear();
            }
        }

        public void addScrap(View v) {
            final LayoutParams lp = (LayoutParams) v.getLayoutParams();
            if (v.hasTransientState()) {
                if (mTransientStateViews == null) {
                    mTransientStateViews = new SparseArray<View>();
                }
                mTransientStateViews.put(lp.position, v);
                return;
            }

            final int childCount = getChildCount();
            if (childCount > mMaxScrap) {
                mMaxScrap = childCount;
            }

            ArrayList<View> scrap = mScrapViews[lp.viewType];
            if (scrap.size() < mMaxScrap) {
                scrap.add(v);
            }
        }

        public View getTransientStateView(int position) {
            if (mTransientStateViews == null) {
                return null;
            }

            final View result = mTransientStateViews.get(position);
            if (result != null) {
                mTransientStateViews.remove(position);
            }
            return result;
        }

        public View getScrapView(int type, long id) {
            ArrayList<View> scrap = mScrapViews[type];
            if (scrap.isEmpty()) {
                return null;
            }
            
//            int size = scrap.size();
//            View view;
//            LayoutParams lp;
//            for (int i = 0; i < size; i++) {
//                view = scrap.get(i);
//                lp = (LayoutParams) view.getLayoutParams();
//                if (lp.id == id) {
//                    scrap.remove(i);
//                    return view;
//                }
//            }
//            return null;
            
            final int index = scrap.size() - 1;
            final View result = scrap.get(index);
            scrap.remove(index);
            return result;
        }
    }
    
    /**
     * 如果需要做选项删除的动画，需要应用在通知数据变化前先调用该方法告诉哪些选项被删除了
     * @param id 删除选项的id。前提是应用提供了稳定的id
     */
    public void deleteItemId(ArrayList<Long> id) {
        if (mAdapter != null && mAdapter.hasStableIds()) {
            mDeleteItemId = id;
        }
    }
    
    private class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            mDataChanged = true; 
            
            if (!mScroller.isFinished()) {
                mVelocityTracker.clear();
                mScroller.abortAnimation();
            }
            
            if (mFadeOutViewAnimatorSet != null) {
            	mFadeOutViewAnimatorSet.end();
            }
            
            if (mUpdateAnimatorSet != null) {
            	mUpdateAnimatorSet.end();
            }
            
            mItemCount = mAdapter.getCount();
            mChildRectsForAnimation.clear();
            mDeleteViews.clear();
            if (mChoiceMode != CHOICE_MODE_NONE && mAdapter != null && mAdapter.hasStableIds()) {
                //每次数据有更新之后，之前所维护的关于记录选中项的变量也需要同时更新才行，否则可能导致返回的选中项跟选中情况不对应
                confirmCheckedPositionsById();
                
                int childCount = getChildCount();
                View view;
                for (int i = 0; i < childCount; i++) {
                    view = getChildAt(i);
                    int left = (int) view.getX();
                    int top = (int) view.getY();
                    Rect localRect = new Rect(left, top, left + view.getWidth(), top + view.getHeight());
                    LayoutParams params = (LayoutParams) view.getLayoutParams();
                    mChildRectsForAnimation.put(params.id, new ViewRectPair(view, localRect));
                    
                    if (mDeleteItemId != null && mDeleteItemId.contains(params.id)) {
                        //if (mShadowBuilder != null && mShadowBuilder.getDragingState() != ACTION_TYPE_WARNING) {
                            mDeleteViews.add(view);
                        //} else {
                         //   //该选项之前是通过拖动到smartbar上的删除按钮删除的，之前在删除的时候将其设置成了INVISIBLE，为了其回收后可以继续被利用，此处需要将其状态设置回VISIBLE
                         //   view.setVisibility(View.VISIBLE);
                        //}
                    }
                }
            }
            mSync = true;
            mSyncPosition = mFirstPosition;
            View view = getChildAt(0); 
            if (view != null) {
                mSpecificTop = view.getTop();
            } else {
                mSpecificTop = getPaddingTop() + mItemMargin;
            }
            
            // TODO: Consider matching these back up if we have stable IDs.
            mRecycler.clearTransientViews();
            
            //每次数据有更新后，需要将之前的所有view从当前控件中删除，然后重新layout以便，保证获取的所有值都是最新的
            // Clear all layout records and recycle the views
            mLayoutRecords.clear();
            recycleAllViews();

            // Reset item bottoms to be equal to item tops
            final int colCount = mColCount;
            for (int i = 0; i < colCount; i++) {
                mItemBottoms[i] = mItemTops[i];
            }
            
            // reset list if position does not exist or id for position has changed
            if(mFirstPosition > mItemCount - 1 || mAdapter.getItemId(mFirstPosition) != mFirstAdapterId){
            	mFirstPosition = 0;
            	final int top = getPaddingTop();
            	Arrays.fill(mItemTops, top);
            	Arrays.fill(mItemBottoms, top);
            	
            	if(mRestoreOffsets != null)
//            		Arrays.fill(mRestoreOffsets, 0);
            		mRestoreOffsets = null;
            }
            
            
            // TODO: consider repopulating in a deferred runnable instead
            // (so that successive changes may still be batched)
            requestLayout();
        }

        @Override
        public void onInvalidated() {
        }
    }
    
    void confirmCheckedPositionsById() {
        mCheckStates.clear();

        boolean checkedCountChanged = false;
        //前提是要保证获取的id是稳定的才行
        for (int checkedIndex = 0; checkedIndex < mCheckedIdStates.size(); checkedIndex++) {
            final long id = mCheckedIdStates.keyAt(checkedIndex);
            final int lastPos = mCheckedIdStates.valueAt(checkedIndex);
            long lastPosId = -1;
            if (lastPos < mItemCount) {
                lastPosId = mAdapter.getItemId(lastPos);
            }
            if (lastPos >= mItemCount || id != lastPosId) {
                // Look around to see if the ID is nearby. If not, uncheck it.
                final int start = Math.max(0, lastPos - CHECK_POSITION_SEARCH_DISTANCE);
                final int end = Math.min(lastPos + CHECK_POSITION_SEARCH_DISTANCE, mItemCount);
                boolean found = false;
                for (int searchPos = start; searchPos < end; searchPos++) {
                    final long searchId = mAdapter.getItemId(searchPos);
                    if (id == searchId) {
                        found = true;
                        mCheckStates.put(searchPos, true);
                        mCheckedIdStates.setValueAt(checkedIndex, searchPos);
                        break;
                    }
                }

                if (!found) {
                    mCheckedIdStates.delete(id);
                    checkedIndex--;
                    mCheckedItemCount--;
                    checkedCountChanged = true;
                    if (mChoiceActionMode != null && mMultiChoiceModeCallback != null) {
                        mMultiChoiceModeCallback.onItemCheckedStateChanged(mChoiceActionMode,
                                lastPos, id, false);
                    }
                }
            } else {
                mCheckStates.put(lastPos, true);
            }
        }
        
        if (checkedCountChanged && mChoiceActionMode != null) {
            mChoiceActionMode.invalidate();
        }
    }
    
    private static class ViewRectPair {
        public final Rect rect;
        public final View view;

        public ViewRectPair(View paramView, Rect paramRect) {
            this.view = paramView;
            this.rect = paramRect;
        }
    }
    

    static class ColMap implements Parcelable {
        private ArrayList<Integer> values;
        int tempMap[];

        public ColMap(ArrayList<Integer> values) {
            this.values = values;
        }

        private ColMap(Parcel in) {
            tempMap = new int[in.readInt()];
            in.readIntArray(tempMap);
            values = new ArrayList<Integer>();
            for (int index = 0; index < tempMap.length; index++) {
                values.add(tempMap[index]);
            }
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            tempMap = toIntArray(values);
            out.writeInt(tempMap.length);
            out.writeIntArray(tempMap);
        }

        public static final Creator<ColMap> CREATOR = new Creator<ColMap>() {
            public ColMap createFromParcel(Parcel source) {
                return new ColMap(source);
            }

            public ColMap[] newArray(int size) {
                return new ColMap[size];
            }
        };

        int[] toIntArray(ArrayList<Integer> list) {
            int[] ret = new int[list.size()];
            for (int i = 0; i < ret.length; i++)
                ret[i] = list.get(i);
            return ret;
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }
    
    static class SavedState extends BaseSavedState {
        long firstId = -1;
        int position;
        int topOffsets[];
        ArrayList<ColMap> mapping;
        boolean inActionMode;
        int checkedItemCount;
        SparseBooleanArray checkState;
        LongSparseArray<Integer> checkIdState; 

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            firstId = in.readLong();
            position = in.readInt();
            topOffsets = new int[in.readInt()];
            in.readIntArray(topOffsets);
            mapping = new ArrayList<ColMap>();
            in.readTypedList(mapping, ColMap.CREATOR);
            inActionMode = in.readByte() != 0;
            checkedItemCount = in.readInt();
            checkState = in.readSparseBooleanArray();
            final int N = in.readInt();
            if (N > 0) {
                checkIdState = new LongSparseArray<Integer>();
                for (int i=0; i<N; i++) {
                    final long key = in.readLong();
                    final int value = in.readInt();
                    checkIdState.put(key, value);
                }
            }
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(firstId);
            out.writeInt(position);
            out.writeInt(topOffsets.length);
            out.writeIntArray(topOffsets);
            out.writeTypedList(mapping);
            out.writeByte((byte) (inActionMode ? 1 : 0));
            out.writeInt(checkedItemCount);
            out.writeSparseBooleanArray(checkState);
            final int N = checkIdState != null ? checkIdState.size() : 0;
            out.writeInt(N);
            for (int i=0; i<N; i++) {
                out.writeLong(checkIdState.keyAt(i));
                out.writeInt(checkIdState.valueAt(i));
            }
        }

        @Override
        public String toString() {
            return "StaggereGridView.SavedState{"
                        + Integer.toHexString(System.identityHashCode(this))
                        + " firstId=" + firstId
                        + " position=" + position 
						+ " checkState=" + checkState+ "}";
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
    
    /**
     * A base class for Runnables that will check that their view is still attached to
     * the original window as when the Runnable was created.
     *
     */
    private class WindowRunnnable {
        private int mOriginalAttachCount;

        public void rememberWindowAttachCount() {
            mOriginalAttachCount = getWindowAttachCount();
        }

        public boolean sameWindow() {
            return hasWindowFocus() && getWindowAttachCount() == mOriginalAttachCount;
        }
    }
    
    private void useDefaultSelector() {
        setSelector(getResources().getDrawable(android.R.drawable.list_selector_background));
    }
    
    
	void positionSelector(int position, View sel) {
        if (position != INVALID_POSITION) {
            mSelectorPosition = position;
        }

        final Rect selectorRect = mSelectorRect;
        selectorRect.set(sel.getLeft(), sel.getTop(), sel.getRight(), sel.getBottom());
        if (sel instanceof SelectionBoundsAdjuster) {
            ((SelectionBoundsAdjuster)sel).adjustListItemSelectionBounds(selectorRect);
        }
        
        positionSelector(selectorRect.left, selectorRect.top, selectorRect.right,
                selectorRect.bottom);

        final boolean isChildViewEnabled = mIsChildViewEnabled;
        if (sel.isEnabled() != isChildViewEnabled) {
            mIsChildViewEnabled = !isChildViewEnabled;
            if (getSelectedItemPosition() != INVALID_POSITION) {
                refreshDrawableState();
            }
        }
    }
    
	/**
     * Causes all the views to be rebuilt and redrawn.
     */
    public void invalidateViews() {
        mDataChanged = true;
        mItemCount = mAdapter.getCount();
        requestLayout();
        invalidate();
    }
	/**
     * The top-level view of a list item can implement this interface to allow
     * itself to modify the bounds of the selection shown for that item.
     */
    public interface SelectionBoundsAdjuster {
        /**
         * Called to allow the list item to adjust the bounds shown for
         * its selection.
         *
         * @param bounds On call, this contains the bounds the list has
         * selected for the item (that is the bounds of the entire view).  The
         * values can be modified as desired.
         */
        public void adjustListItemSelectionBounds(Rect bounds);
    }
    
    private int getSelectedItemPosition() {
        // TODO: setup mNextSelectedPosition
        return this.mSelectorPosition;
    }
    
    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        // If the child view is enabled then do the default behavior.
        if (mIsChildViewEnabled) {
            // Common case
            return super.onCreateDrawableState(extraSpace);
        }

        // The selector uses this View's drawable state. The selected child view
        // is disabled, so we need to remove the enabled state from the drawable
        // states.
        final int enabledState = ENABLED_STATE_SET[0];

        // If we don't have any extra space, it will return one of the static state arrays,
        // and clearing the enabled state on those arrays is a bad thing!  If we specify
        // we need extra space, it will create+copy into a new array that safely mutable.
        int[] state = super.onCreateDrawableState(extraSpace + 1);
        int enabledPos = -1;
        for (int i = state.length - 1; i >= 0; i--) {
            if (state[i] == enabledState) {
                enabledPos = i;
                break;
            }
        }

        // Remove the enabled state
        if (enabledPos >= 0) {
            System.arraycopy(state, enabledPos + 1, state, enabledPos,
                    state.length - enabledPos - 1);
        }

        return state;
    }

    
    private void positionSelector(int l, int t, int r, int b) {
        mSelectorRect.set(l - mSelectionLeftPadding, t - mSelectionTopPadding, r
                + mSelectionRightPadding, b + mSelectionBottomPadding);
    }
    
    final class CheckForTap implements Runnable {
        public void run() {
            if (mTouchMode == TOUCH_MODE_DOWN) {
                mTouchMode = TOUCH_MODE_TAP;
                final View child = getChildAt(mMotionPosition - mFirstPosition);
                if (child != null && !child.hasFocusable()) {
                    
                    if (!mDataChanged) {
                    	child.setSelected(true);
                    	child.setPressed(true);
                        setPressed(true);
                        layoutChildren(false);
                        positionSelector(mMotionPosition, child);
                        refreshDrawableState();

                        final int longPressTimeout = ViewConfiguration.getLongPressTimeout();
                        final boolean longClickable = isLongClickable();

                        if (mSelector != null) {
                            Drawable d = mSelector.getCurrent();
                            if (d != null && d instanceof TransitionDrawable) {
                                if (longClickable) {
                                    ((TransitionDrawable) d).startTransition(longPressTimeout);
                                } else {
                                    ((TransitionDrawable) d).resetTransition();
                                }
                            }
                        }

                        if (longClickable) {
                            if (mPendingCheckForLongPress == null) {
                                mPendingCheckForLongPress = new CheckForLongPress();
                            }
                            mPendingCheckForLongPress.rememberWindowAttachCount();
                            postDelayed(mPendingCheckForLongPress, longPressTimeout);
                        } else {
                            mTouchMode = TOUCH_MODE_DONE_WAITING;
                        }
                    } else {
                        mTouchMode = TOUCH_MODE_DONE_WAITING;
                    }
                }
            }
        }
    }
    
    private class CheckForLongPress extends WindowRunnnable implements Runnable {
        public void run() {
            final int motionPosition = mMotionPosition;
            final View child = getChildAt(motionPosition - mFirstPosition);
            if (child != null) {
                final int longPressPosition = mMotionPosition;
                final long longPressId = mAdapter.getItemId(mMotionPosition);

                boolean handled = false;
                if (sameWindow() && !mDataChanged) {
                    handled = performLongPress(child, longPressPosition, longPressId);
                }
                if (handled) {
                    mTouchMode = TOUCH_MODE_REST;
                    setPressed(false);
                    child.setPressed(false);
                } else {
                    mTouchMode = TOUCH_MODE_DONE_WAITING;
                }
            }
        }
    }
    
    private class PerformClick extends WindowRunnnable implements Runnable {
        int mClickMotionPosition;
        public void run() {
            // The data has changed since we posted this action in the event queue,
            // bail out before bad things happen
            if (mDataChanged) return;

            final ListAdapter adapter = mAdapter;
            final int motionPosition = mClickMotionPosition;
            if (adapter != null && mItemCount > 0 &&
                    motionPosition != INVALID_POSITION &&
                    motionPosition < adapter.getCount() && sameWindow()) {
                final View view = getChildAt(motionPosition - mFirstPosition);
                // If there is no view, something bad happened (the view scrolled off the
                // screen, etc.) and we should cancel the click
                if (view != null) {
                    performItemClick(view, motionPosition, adapter.getItemId(motionPosition));
                }
            }
        }
    }
    
    public boolean performItemClick(View view, int position, long id) {
        boolean dispatchItemClick = true;

        if (mChoiceMode != CHOICE_MODE_NONE) {
            boolean checkedStateChanged = false;

            if (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL && mChoiceActionMode != null) {
                boolean checked = !mCheckStates.get(position, false);
                mCheckStates.put(position, checked);
                if (mCheckedIdStates != null && mAdapter.hasStableIds()) {
                    if (checked) {
                        mCheckedIdStates.put(mAdapter.getItemId(position), position);
                    } else {
                        mCheckedIdStates.delete(mAdapter.getItemId(position));
                    }
                }
                if (checked) {
                    mCheckedItemCount++;
                } else {
                    mCheckedItemCount--;
                }
                if (mChoiceActionMode != null) {
                    mMultiChoiceModeCallback.onItemCheckedStateChanged(mChoiceActionMode,
                            position, id, checked);
                    dispatchItemClick = false;
                }
                checkedStateChanged = true;
            }

            if (checkedStateChanged) {
                updateOnScreenCheckedViews();
            }
        }
        
        if (dispatchItemClick) {
            if (mOnItemClickListener != null) {
                playSoundEffect(SoundEffectConstants.CLICK);
                if (view != null) {
                    view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
                }
                mOnItemClickListener.onItemClick(this, view, position, id);
                
                //用户操作完后没有选中项，退出选择模式
                if(getCheckedItemCount()<=0 && mChoiceActionMode != null)
                    mChoiceActionMode.finish();
                
                return true;
            }
        }

        return false;
    }
    
    public interface DragItemFilter {
        public boolean isItemDragable(View view, int position, long id);
    }
    
    boolean performLongPress(final View child, final int longPressPosition, final long longPressId) {
        if (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
            if (mChoiceActionMode != null || 
                    (mChoiceActionMode == null && (mChoiceActionMode = startActionMode(mMultiChoiceModeCallback)) != null)) {
                if(mSelector != null) {
                    mSelector.setState(StateSet.NOTHING);
                    mSelector.jumpToCurrentState();
                }
                
                //checkbox 显示当前处于选择状态
                /*View checkbox = child.findViewById(android.R.id.checkbox);
                if(checkbox != null && checkbox instanceof Checkable){
                    ((Checkable)checkbox).setChecked(true);
                }*/
                
                Rect frame = mTouchFrame;
                if (frame == null) {
                    mTouchFrame = new Rect();
                    frame = mTouchFrame;
                }
                child.getHitRect(frame);
                
                child.setActivated(false);
                child.jumpDrawablesToCurrentState();
                
                setItemChecked(longPressPosition, true);
                
                /*if(child instanceof DragShadowItem){
                    DragShadowItem item = (DragShadowItem)child;
                    mShadowBuilder = new ListViewDragShadowBuilder(item.getDragView(),item.needBackground(),item.getDragViewShowPosition());
                }else {
                    mShadowBuilder = new ListViewDragShadowBuilder(child);
                }*/
                
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
            
            return true;
        }
    
        boolean handled = false;
        if (mOnItemLongClickListener != null) {
            handled = mOnItemLongClickListener.onItemLongClick(this, child, longPressPosition, longPressId);
        }
        
        
        if (!handled) {
            mContextMenuInfo = createContextMenuInfo(child, longPressPosition, longPressId);
            handled = super.showContextMenuForChild(this);
        }
        if (handled) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        
        //用户操作完后没有选中项，退出选择模式
        if(getCheckedItemCount()<=0 && mChoiceActionMode != null)
            mChoiceActionMode.finish();
        
        return handled;
    }
    
    @Override
    protected ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }
    
    /**
     * Creates the ContextMenuInfo returned from {@link #getContextMenuInfo()}. This
     * methods knows the view, position and ID of the item that received the
     * long press.
     *
     * @param view The view that received the long press.
     * @param position The position of the item that received the long press.
     * @param id The ID of the item that received the long press.
     * @return The extra information that should be returned by
     *         {@link #getContextMenuInfo()}.
     */
    ContextMenuInfo createContextMenuInfo(View view, int position, long id) {
        return new AdapterContextMenuInfo(view, position, id);
    }
    
    /**
     * Extra menu information provided to the
     * {@link android.view.View.OnCreateContextMenuListener#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo) }
     * callback when a context menu is brought up for this AdapterView.
     *
     */
    public static class AdapterContextMenuInfo implements ContextMenuInfo {

        public AdapterContextMenuInfo(View targetView, int position, long id) {
            this.targetView = targetView;
            this.position = position;
            this.id = id;
        }

        /**
         * The child view for which the context menu is being displayed. This
         * will be one of the children of this AdapterView.
         */
        public View targetView;

        /**
         * The position in the adapter for which the context menu is being
         * displayed.
         */
        public int position;

        /**
         * The row id of the item for which the context menu is being displayed.
         */
        public long id;
    }
    
    /**
     * Returns the selector {@link android.graphics.drawable.Drawable} that is used to draw the
     * selection in the list.
     *
     * @return the drawable used to display the selector
     */
    public Drawable getSelector() {
        return mSelector;
    }
    
    /**
     * Set a Drawable that should be used to highlight the currently selected item.
     *
     * @param resID A Drawable resource to use as the selection highlight.
     */
    public void setSelector(int resID) {
        setSelector(getResources().getDrawable(resID));
    }
    
    @Override
    public boolean verifyDrawable(Drawable dr) {
        return mSelector == dr || super.verifyDrawable(dr);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mSelector != null) mSelector.jumpToCurrentState();
    }
    
    public void setSelector(Drawable sel) {
        if (mSelector != null) {
            mSelector.setCallback(null);
            unscheduleDrawable(mSelector);
        }
        
        mSelector = sel;
        
        if(mSelector==null){
        	return;
        }
         
        Rect padding = new Rect();
        sel.getPadding(padding);
        mSelectionLeftPadding = padding.left;
        mSelectionTopPadding = padding.top;
        mSelectionRightPadding = padding.right;
        mSelectionBottomPadding = padding.bottom;
        sel.setCallback(this);
        updateSelectorState();
    }
    
    void updateSelectorState() {
        if (mSelector != null) {
            if (shouldShowSelector()) {
                mSelector.setState(getDrawableState());
            } else {
                mSelector.setState(StateSet.NOTHING);
            }
        }
    }
    
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateSelectorState();
    }
    
    /**
     * Indicates whether this view is in a state where the selector should be drawn. This will
     * happen if we have focus but are not in touch mode, or we are in the middle of displaying
     * the pressed state for an item.
     *
     * @return True if the selector should be shown
     */
    boolean shouldShowSelector() {
        return ((hasFocus() && !isInTouchMode()) || touchModeDrawsInPressedState()) /*&& (mBeginClick)*/;
    }
    
    /**
     * @return True if the current touch mode requires that we draw the selector in the pressed
     *         state.
     */
    boolean touchModeDrawsInPressedState() {
        // FIXME use isPressed for this
        switch (mTouchMode) {
        case TOUCH_MODE_TAP:
        case TOUCH_MODE_DONE_WAITING:
            return true;
        default:
            return false;
        }
    }
    
    /**
     * Register a callback to be invoked when an item in this AdapterView has
     * been clicked.
     *
     * @param listener The callback that will be invoked.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * @return The callback to be invoked with an item in this AdapterView has
     *         been clicked, or null id no callback has been set.
     */
    public final OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }
    
    public interface OnItemClickListener {

        /**
         * Callback method to be invoked when an item in this AdapterView has
         * been clicked.
         * <p>
         * Implementers can call getItemAtPosition(position) if they need
         * to access the data associated with the selected item.
         *
         * @param parent The AdapterView where the click happened.
         * @param view The view within the AdapterView that was clicked (this
         *            will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         * @param id The row id of the item that was clicked.
         */
        void onItemClick(StaggeredGridView parent, View view, int position, long id);
    }
    
    /**
     * Register a callback to be invoked when an item in this AdapterView has
     * been clicked and held
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        if (!isLongClickable()) {
            setLongClickable(true);
        } 
        mOnItemLongClickListener = listener;
    }

    /**
     * @return The callback to be invoked with an item in this AdapterView has
     *         been clicked and held, or null id no callback as been set.
     */
    public final OnItemLongClickListener getOnItemLongClickListener() {
        return mOnItemLongClickListener;
    }
    
    public interface OnItemLongClickListener {
        /**
         * Callback method to be invoked when an item in this view has been
         * clicked and held.
         *
         * Implementers can call getItemAtPosition(position) if they need to access
         * the data associated with the selected item.
         *
         * @param parent The StaggeredGridView where the click happened
         * @param view The view within the AbsListView that was clicked
         * @param position The position of the view in the list
         * @param id The row id of the item that was clicked
         *
         * @return true if the callback consumed the long click, false otherwise
         */
        boolean onItemLongClick(StaggeredGridView parent, View view, int position, long id);
    }
    
    /**
     * Maps a point to a position in the list.
     *
     * @param x X in local coordinate
     * @param y Y in local coordinate
     * @return The position of the item which contains the specified point, or
     *         {@link #INVALID_POSITION} if the point does not intersect an item.
     */
    public int pointToPosition(int x, int y) {
        Rect frame = mTouchFrame;
        if (frame == null) {
            mTouchFrame = new Rect();
            frame = mTouchFrame;
        }

        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return mFirstPosition + i;
                }
            }
        }
        return INVALID_POSITION;
    }

	public boolean isDrawSelectorOnTop() {
		return mDrawSelectorOnTop;
	}

	public void setDrawSelectorOnTop(boolean drawSelectorOnTop) {
		this.mDrawSelectorOnTop = drawSelectorOnTop;
	}
    
	/**
     * @return The number of items owned by the Adapter associated with this
     *         AdapterView. (This is the number of data items, which may be
     *         larger than the number of visible views.)
     */
    public int getCount() {
        return mItemCount;
    }
    
    /**
     * Returns the position within the adapter's data set for the first item
     * displayed on screen.
     *
     * @return The position within the adapter's data set
     */
    public int getFirstVisiblePosition() {
        return mFirstPosition;
    }
    
    
    /**
     * Returns the position within the adapter's data set for the last item
     * displayed on screen.
     *
     * @return The position within the adapter's data set
     */
    public int getLastVisiblePosition() {
        if (getChildCount() == 0) {
            return 0;
        }
        return mFirstPosition + getChildCount() - 1;
    }

}
