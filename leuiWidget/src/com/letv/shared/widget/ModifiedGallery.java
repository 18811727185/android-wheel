/*
 * Copyright (C) 2007 The Android Open Source Project
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
package com.letv.shared.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

import com.letv.shared.R;

public class ModifiedGallery extends AbsSpinner implements GestureDetector.OnGestureListener {

    /**
     * Normal ModifiedGallery allows up to one choice
     */
    public static final int CHOICE_MODE_SINGLE = 0;
    /**
     * The ModifiedGallery allows multiple choices in a modal selection mode
     * 
     */
    public static final int CHOICE_MODE_MULTIPLE = 1;
    
    private static final int  MIN_FLING_VELOCITY = 1500;
    /**
     * Duration in milliseconds from the start of a scroll during which we're
     * unsure whether the user is scrolling or flinging.
     */
    private static final int SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT = 250;
    
    /**
     * Indicates that we are not in the middle of a touch gesture
     */
    static final int TOUCH_MODE_REST = -1;

    /**
     * Indicates we just received the touch event and we are waiting to see if the it is a tap or a
     * scroll gesture.
     */
    static final int TOUCH_MODE_DOWN = 0;

    /**
     * Indicates the touch gesture is a scroll
     */
    static final int TOUCH_MODE_SCROLL = 1;

    /**
     * Indicates the view is in the process of being flung
     */
    static final int TOUCH_MODE_FLING = 2;

    /**
     * Indicates the touch gesture is an overscroll - a scroll beyond the beginning or end.
     */
    static final int TOUCH_MODE_OVERSCROLL = 3;

    /**
     * Indicates the view is being flung outside of normal content bounds
     * and will spring back.
     */
    static final int TOUCH_MODE_OVERFLING = 4;
    
    
    private int mTouchMode = TOUCH_MODE_REST;

    /**
     * Horizontal spacing between items.
     */
    private int mSpacing = 0;

    /**
     * How long the transition animation should run when a child view changes
     * position, measured in milliseconds.
     */
    private int mAnimationDuration = 250;

    private int mGravity;

    /**
     * Helper for detecting touch gestures.
     */
    private GestureDetector mGestureDetector;

    /**
     * The position of the item that received the user's down touch.
     */
    private int mDownTouchPosition;

    /**
     * The view of the item that received the user's down touch.
     */
    private View mDownTouchView;
    
    /**
     * Executes the delta scrolls from a fling or scroll movement. 
     */
    private FlingRunnable mFlingRunnable = new FlingRunnable();

    /**
     * Sets mSuppressSelectionChanged = false. This is used to set it to false
     * in the future. It will also trigger a selection changed.
     */
    private Runnable mDisableSuppressSelectionChangedRunnable = new Runnable() {
        @Override
        public void run() {
            mSuppressSelectionChanged = false;
            selectionChanged();
        }
    };
    
    /**
     * When fling runnable runs, it resets this to false. Any method along the
     * path until the end of its run() can set this to true to abort any
     * remaining fling. For example, if we've reached either the leftmost or
     * rightmost item, we will set this to true.
     */
    private boolean mShouldStopFling;
    
    /**
     * The currently selected item's child.
     */
    private View mSelectedChild;
    
    /**
     * Whether to continuously callback on the item selected listener during a
     * fling.
     */
    private boolean mShouldCallbackDuringFling = true;

    /**
     * If true, do not callback to item selected listener. 
     */
    private boolean mSuppressSelectionChanged;
    
    private AdapterContextMenuInfo mContextMenuInfo;

    /**
     * If true, this onScroll is the first for this user's drag (remember, a
     * drag sends many onScrolls).
     */
    private boolean mIsFirstScroll;

    /**
     * If true, mFirstPosition is the position of the rightmost child, and
     * the children are ordered right to left.
     */
    private boolean mIsRtl = false;
    
    private int mCurrentOverScrollDistance;
    private int mMaxOverScrollDistance;
    private int mDefaultMaxOverScrollDistance;
    private int mChildWidth;
    private int mDownFirstPosition;
    private int mDownLastPosition;
    
    private boolean mScrollEnableWhenLessContent = false;
    private int mDeltaLength;
    
    /**
     * The X value associated with the the down motion event
     */
    int mMotionX;
    
    /**
     * The Y value associated with the the down motion event
     */
    int mMotionY;

    public ModifiedGallery(Context context) {
        this(context, null);
    }

    public ModifiedGallery(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.Leui_ModifiedGalleryStyle);
    }

    public ModifiedGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setIsLongpressEnabled(true);
        
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.ModifiedGallery, defStyle, 0);
        int spacing = a.getDimensionPixelSize(R.styleable.ModifiedGallery_leSpacing, 10);
        setSpacing(spacing);
        mDefaultMaxOverScrollDistance = getResources().getDimensionPixelSize(R.dimen.le_modifiedgallery_max_overscroll_distance);
        mMaxOverScrollDistance = a.getDimensionPixelSize(R.styleable.ModifiedGallery_leMaxOverScrollDistance, mDefaultMaxOverScrollDistance);
        mScrollEnableWhenLessContent = a.getBoolean(R.styleable.ModifiedGallery_leScrollEnableWhenLessContent, false);
        a.recycle();
    }

    /**
     * Whether or not to callback on any {@link #getOnItemSelectedListener()}
     * while the items are being flinged. If false, only the final selected item
     * will cause the callback. If true, all items between the first and the
     * final will cause callbacks.
     * 
     * @param shouldCallback Whether or not to callback on the listener while
     *            the items are being flinged.
     */
    public void setCallbackDuringFling(boolean shouldCallback) {
        mShouldCallbackDuringFling = shouldCallback;
    }

    /**
     * 设置可以越界划出的最远距离
     * @param distance
     */
    public void setMaxOverScrollDistance(int distance) {
    	if (distance < 0) {
    		mMaxOverScrollDistance = mDefaultMaxOverScrollDistance;
    	} else {
    		mMaxOverScrollDistance = distance;
    	}
    }
    
    /**
     * Sets how long the transition animation should run when a child view
     * changes position. Only relevant if animation is turned on.
     * 
     * @param animationDurationMillis The duration of the transition, in
     *        milliseconds.
     * 
     * @attr ref android.R.styleable#Gallery_animationDuration
     */
    public void setAnimationDuration(int animationDurationMillis) {
        mAnimationDuration = animationDurationMillis;
    }

    /**
     * Sets the spacing between items in a ModifiedGallery
     * 
     * @param spacing The spacing in pixels between items in the ModifiedGallery
     * 
     * @attr ref android.R.styleable#Gallery_spacing
     */
    public void setSpacing(int spacing) {
        mSpacing = spacing;
    }
    
    /**
     * 当ModifiedGallery中的选项没有填满整个控件时，是否允许用户进行滑动操作
     * @param enable true表示仍可滑动，false 表示不能滑动
     */    
    public void setScrollEnableWhenLessContent(boolean enable) {
        mScrollEnableWhenLessContent = enable;
    }

    @Override
    protected int computeHorizontalScrollExtent() {
        // Only 1 item is considered to be selected
        return 1;
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        // Current scroll position is the same as the selected position
        return mSelectedPosition;
    }

    @Override
    protected int computeHorizontalScrollRange() {
        // Scroll range is the same as the item count
        return mItemCount;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        /*
         * ModifiedGallery expects ModifiedGallery.LayoutParams.
         */
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        
        /*
         * Remember that we are in layout to prevent more layout request from
         * being generated.
         */
        mInLayout = true;
        layout(0, false);
        mInLayout = false;
    }

    @Override
    int getChildHeight(View child) {
        return child.getMeasuredHeight();
    }

    /**
     * Tracks a motion scroll. In reality, this is used to do just about any
     * movement to items (touch scroll, arrow-key scroll, set an item as selected).
     * 
     * @param deltaX Change in X from the previous event.
     */
    boolean trackMotionScroll(int deltaX) {
        int childCount = getChildCount();
        if (childCount == 0 || deltaX == 0) {
            return false;
        }
        
        boolean toLeft = deltaX < 0; 
        boolean isAtEdge = false;
        boolean dontRecycle = false;
        
        boolean cannotScrollRight;
        boolean cannotScrollLeft;
        if (mIsRtl) {
            cannotScrollLeft = (mFirstPosition == 0 &&
                    getChildAt(0).getRight() >= getWidth() - getPaddingRight() - mSpacing && deltaX <= 0);
            cannotScrollRight = (mFirstPosition + childCount == mItemCount &&
                    getChildAt(childCount - 1).getLeft() >= getPaddingLeft() && deltaX >= 0);
        } else {
            cannotScrollRight = (mFirstPosition == 0 &&
                    getChildAt(0).getLeft() >= getPaddingLeft() + mSpacing && deltaX >= 0);
            cannotScrollLeft = (mFirstPosition + childCount == mItemCount &&
                    getChildAt(childCount - 1).getRight() <= getWidth() - getPaddingRight() && deltaX <= 0);
        }
        
        if (cannotScrollRight || cannotScrollLeft) {
            dontRecycle = true;
        }
        
        offsetChildrenLeftAndRight(deltaX);        
        
        if (!dontRecycle) {
            detachOffScreenChildren(toLeft);
            
            if (toLeft) {
                // If moved left, there will be empty space on the right
                fillToGalleryRight();
            } else {
                // Similarly, empty space on the left
                fillToGalleryLeft();
            }
            // Clear unused views
            mRecycler.clear();
            setSelectionView();
        }
        
        mCurrentOverScrollDistance = 0;
        childCount = getChildCount();
        int first;
        int last;
        
        if (mIsRtl){
           first = getChildAt(0).getRight();
           last = getChildAt(childCount - 1).getLeft();
           int end = getWidth() - getPaddingRight() - mSpacing;
           
           if (mFirstPosition == 0 && first < end) {
               mCurrentOverScrollDistance = end - first;
               isAtEdge = true;
           } else if (mFirstPosition + childCount == mItemCount && last > getPaddingLeft()) {
               mCurrentOverScrollDistance = getPaddingLeft() + mSpacing - last;
               isAtEdge = true;
           }
        } else {
            first = getChildAt(0).getLeft();
            last = getChildAt(childCount - 1).getRight();
            int start = getPaddingLeft() + mSpacing;
            int end = getWidth() - getPaddingRight();
            
            if (mFirstPosition == 0 && first > start) {
                mCurrentOverScrollDistance = start - first;
                isAtEdge = true;
            } else if (mFirstPosition + childCount == mItemCount && last < end){
                mCurrentOverScrollDistance = end - last - mSpacing;
                isAtEdge = true;
            }            
        }
        
        invokeOnItemScrollListener();
        onScrollChanged(0, 0, 0, 0); // dummy values, View's implementation does not use these.

        invalidate();
        return isAtEdge;
    }

    /**
     * Offset the horizontal location of all children of this view by the
     * specified number of pixels.
     * 
     * @param offset the number of pixels to offset
     */
    private void offsetChildrenLeftAndRight(int offset) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).offsetLeftAndRight(offset);
        }
    }
    
    /**
     * @return The center of this ModifiedGallery.
     */
    private int getCenterOfModifiedGallery() {
        return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
    }
    
    /**
     * @return The center of the given view.
     */
    private static int getCenterOfView(View view) {
        return view.getLeft() + view.getWidth() / 2;
    }
    
    /**
     * Detaches children that are off the screen (i.e.: ModifiedGallery bounds).
     * 
     * @param toLeft Whether to detach children to the left of the ModifiedGallery, or
     *            to the right.
     */
    private void detachOffScreenChildren(boolean toLeft) {
        int numChildren = getChildCount();
        int firstPosition = mFirstPosition;
        int start = 0;
        int count = 0;

        if (toLeft) {
            int galleryLeft;
            if(mIsRtl) {
                galleryLeft = getPaddingLeft();
            } else {
                galleryLeft = getPaddingLeft() + mSpacing;
            }
            for (int i = 0; i < numChildren - 1; i++) {
                int n = mIsRtl ? (numChildren - 1 - i) : i;
                View child = mIsRtl ? getChildAt(n - 1) : getChildAt(n + 1);
                if (child.getLeft() > galleryLeft) {
                    break;
                } else {
                    start = n;
                    count++;
                    child = getChildAt(n);
                    mRecycler.put(firstPosition + n, child);
                }
            }
            if (!mIsRtl) {
                start = 0;
            }
        } else {
            int galleryRight;
            if (mIsRtl) {
                galleryRight = getWidth() - getPaddingRight() - mSpacing;
            } else {
                galleryRight = getWidth() - getPaddingRight();
            }
            for (int i = numChildren - 1; i >= 1; i--) {
                int n = mIsRtl ? numChildren - 1 - i : i;
                View child = mIsRtl ? getChildAt(n + 1):getChildAt(n - 1);
                if (child.getRight() < galleryRight) {
                    break;
                } else {
                    start = n;
                    count++;
                    child = getChildAt(n);
                    mRecycler.put(firstPosition + n, child);
                }
            }
            if (mIsRtl) {
                start = 0;
            }
        }

        detachViewsFromParent(start, count);
        
        if (toLeft != mIsRtl) {
            mFirstPosition += count;
        }
    }
    
    /**
     * Scrolls the items so that the selected item is in its 'slot' (its center
     * is the ModifiedGallery's center).
     */
    private void scrollIntoSlots() {
        int selectedCenter;
        int scrollAmount = 0;
        int childCount = getChildCount();
        
        if (childCount == 0 || mSelectedChild == null) {
            if(mLastScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            }
            
            return;
        }
        
        int start;
        int end;
        View startView = getChildAt(0);
        View endView = getChildAt(childCount - 1);
        
        if(mIsRtl) {
            start = getWidth() - getPaddingRight() - mSpacing;
            end = getPaddingLeft();
            if (mTouchMode == TOUCH_MODE_FLING && mFirstPosition + childCount == mItemCount) {
                scrollAmount = end + mSpacing - endView.getLeft();
            } else if (startView.getRight() != start) {
                selectedCenter = getCenterOfView(startView);
                if (selectedCenter >= start) {
                    scrollAmount = start - getChildAt(1).getRight();
                } else {
                    scrollAmount = start - startView.getRight();
                }
                
                if(mFirstPosition + childCount == mItemCount) {
                    if (endView.getLeft() + scrollAmount > end) {
                        scrollAmount = end - endView.getLeft() + mSpacing;
                    }
                }
            }
        } else {
            start = getPaddingLeft() + mSpacing;
            end = getWidth() - getPaddingRight();
            if (mTouchMode == TOUCH_MODE_FLING && mFirstPosition + childCount == mItemCount) {
                scrollAmount = end - endView.getRight() - mSpacing;
            }else  if (startView.getLeft() != start) {
                selectedCenter = getCenterOfView(startView);
                if (selectedCenter < start) {
                    scrollAmount = start - getChildAt(1).getLeft();
                } else {
                    scrollAmount = start - startView.getLeft();
                }
                
                if (mFirstPosition + childCount == mItemCount) {
                    if (endView.getRight() + scrollAmount < end) {
                        scrollAmount = end - endView.getRight() - mSpacing;
                    }
                }
            }
        }
        if (scrollAmount != 0) {
            if(mLastScrollState != OnScrollListener.SCROLL_STATE_FLING) {
                reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
            }
            
            mFlingRunnable.startUsingDistance(scrollAmount);
        } else {
            if(mLastScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            }
            
            onFinishedMovement();
            mTouchMode = TOUCH_MODE_REST;
        }
    }

    private void onFinishedMovement() {
        if (mSuppressSelectionChanged) {
            mSuppressSelectionChanged = false;
            
            // We haven't been callbacking during the fling, so do it now
            super.selectionChanged();
        }
        invalidate();
    }
    
    @Override
    void selectionChanged() {
        if (!mSuppressSelectionChanged) {
            super.selectionChanged();
        }
    }

    /**
     * Looks for the child that is closest to the center and sets it as the
     * selected child.
     */
    private void setSelectionView() {
        
        if (mSelectedChild == null) return;
        int newPos = mFirstPosition;
        
        if (newPos != mSelectedPosition) {
            setSelectedPositionInt(newPos);
            setNextSelectedPositionInt(newPos);
            checkSelectionChanged();
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        postDelayed(new Runnable() {
            
            @Override
            public void run() {
                View lastView = getChildAt(getChildCount() - 1);
                int scrollAmount = 0;
                if (mIsRtl) {
                    if (lastView != null && lastView.getLeft() > getPaddingLeft()) {
                        scrollAmount = getPaddingLeft() - lastView.getLeft();
                    }
                } else {
                    if (lastView != null && lastView.getRight() < getWidth() - getPaddingRight()) {
                        scrollAmount = getWidth() - getPaddingRight() - lastView.getRight();
                    }
                }
                
                mTouchMode = TOUCH_MODE_REST;
                
                if(mLastScrollState != OnScrollListener.SCROLL_STATE_FLING && scrollAmount != 0) {
                    reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
                }
                
                mFlingRunnable.startUsingDistance(scrollAmount);
            }
        }, 200);
    }

    /**
     * Creates and positions all views for this ModifiedGallery.
     * <p>
     * We layout rarely, most of the time {@link #trackMotionScroll(int)} takes
     * care of repositioning, adding, and removing children.
     * 
     * @param delta Change in the selected position. +1 means the selection is
     *            moving to the right, so views are scrolling to the left. -1
     *            means the selection is moving to the left.
     */
    @Override
    void layout(int delta, boolean animate) {
        if (Build.VERSION.SDK_INT >= 17 ) {
            mIsRtl = getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        }
        /*
        if (mIsRtl) {
            mMaxOverScrollDistance = getCenterOfModifiedGallery() - getPaddingRight() - mSpacing;
        } else {
            mMaxOverScrollDistance = getCenterOfModifiedGallery() - getPaddingLeft() - mSpacing;
        }*/
        
        if (mDataChanged) {
            handleDataChanged();
        }

        // Handle an empty ModifiedGallery by removing all views.
        if (mItemCount == 0) {
            invokeOnItemScrollListener();
            resetList();
            return;
        }

        // Update to the new selected position.
        if (mNextSelectedPosition >= 0) {
            setSelectedPositionInt(mNextSelectedPosition);
        }

        // All views go in recycler while we are in layout
        recycleAllViews();

        // Clear out old views
        //removeAllViewsInLayout();
        detachAllViewsFromParent();
        
        /*
         * mFirstPosition will be decreased as we add views to the left later
         * on. The 0 for x will be offset in a couple lines down.
         */  
        mFirstPosition = mSelectedPosition;
        
        layoutChildren();
        
        // Flush any cached views that did not get reused above
        mRecycler.clear();

        invalidate();
        checkSelectionChanged();

        mDataChanged = false;
        mNeedSync = false;
        setNextSelectedPositionInt(mSelectedPosition);
        updateSelectedItemMetadata();
        
        mDeltaLength = 0;
        View view = getChildAt(0);
        if (view != null) {
            mChildWidth = view.getWidth();
            mDeltaLength = getWidth() - getPaddingLeft() - getPaddingRight()- mItemCount * (mChildWidth + mSpacing);
            if (mDeltaLength > 0 && !mScrollEnableWhenLessContent) {
                int distance = 0;
                if (mFirstPosition != 0 && mSelectedPosition < mItemCount) {
                    if (mIsRtl) {
                        distance = -mSelectedPosition * (mChildWidth + mSpacing);
                    } else {
                        distance = mSelectedPosition * (mChildWidth + mSpacing);
                    } 
                    trackMotionScroll(distance);
                    scrollIntoSlots();
                }
            } else if (mDeltaLength <= 0) {
                int childCount = getChildCount();
                if (mIsRtl) {
                    int mostLeft = getPaddingLeft() + mSpacing;
                    if (mFirstPosition + childCount == mItemCount && getChildAt(childCount - 1).getLeft() > mostLeft) {
                        trackMotionScroll(mostLeft - getChildAt(childCount - 1).getLeft());
                        scrollIntoSlots();
                    }
                } else {
                    int mostRight = getWidth() - getPaddingRight() - mSpacing;
                    if (mFirstPosition + childCount == mItemCount && getChildAt(childCount - 1).getRight() < mostRight) {
                        trackMotionScroll(mostRight- getChildAt(childCount - 1).getRight());
                        scrollIntoSlots();
                    }
                }
            }
        }

        invokeOnItemScrollListener();
    }
    
    private void layoutChildren() {
        int itemSpacing = mSpacing;
        int galleryLeft = getPaddingLeft();
        int galleryRight = getRight() - getLeft() - getPaddingRight();
        int numItems = mItemCount;
        int curPosition;
        int curLeftEdge;
        int curRightEdge;
        View curView;
        
        if (mIsRtl) {
            curPosition = mFirstPosition;
            curRightEdge = galleryRight - mSpacing;
            
            while (curRightEdge > galleryLeft && curPosition < numItems) {
                curView = makeAndAddView(curPosition, curPosition - mSelectedPosition,
                        curRightEdge, false);
                
                // Set state for next iteration
                curRightEdge = curView.getLeft() - itemSpacing;
                curPosition++;
            }
            
        } else {
            curPosition = mFirstPosition;
            curLeftEdge = galleryLeft + itemSpacing;
            
            while (curLeftEdge < galleryRight && curPosition < numItems) {
                curView = makeAndAddView(curPosition, curPosition - mSelectedPosition,
                        curLeftEdge, true);
                
                // Set state for next iteration
                curLeftEdge = curView.getRight() + itemSpacing;
                curPosition++;
            }
        }
           
    }

    private void fillToGalleryLeft() {
        if (mIsRtl) {
            fillToGalleryLeftRtl();
        } else {
            fillToGalleryLeftLtr();
        }
    }

    private void fillToGalleryLeftRtl() {
        int itemSpacing = mSpacing;
        int galleryLeft = getPaddingLeft();
        int numChildren = getChildCount();

        // Set state for initial iteration
        View prevIterationView = getChildAt(numChildren - 1);
        int curPosition;
        int curRightEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition + numChildren;
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
        } else {
            // No children available!
            mFirstPosition = curPosition = mItemCount - 1;
            curRightEdge = getRight() - getLeft() - getPaddingRight();
            mShouldStopFling = true;
        }

        while (curRightEdge > galleryLeft && curPosition < mItemCount) {
            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition,
                    curRightEdge, false);

            // Set state for next iteration
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
            curPosition++;
        }
    }

    private void fillToGalleryLeftLtr() {
        int itemSpacing = mSpacing;
        int galleryLeft = getPaddingLeft();
        
        // Set state for initial iteration
        View prevIterationView = getChildAt(0);
        int curPosition;
        int curRightEdge;
        
        if (prevIterationView != null) {
            curPosition = mFirstPosition - 1;
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
        } else {
            // No children available!
            curPosition = 0; 
            curRightEdge = getRight() - getLeft() - getPaddingRight();
            mShouldStopFling = true;
        }
                
        while (curRightEdge > galleryLeft && curPosition >= 0) {
            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition,
                    curRightEdge, false);

            // Remember some state
            mFirstPosition = curPosition;
            
            // Set state for next iteration
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
            curPosition--;
        }
    }
    
    private void fillToGalleryRight() {
        if (mIsRtl) {
            fillToGalleryRightRtl();
        } else {
            fillToGalleryRightLtr();
        }
    }

    private void fillToGalleryRightRtl() {
        int itemSpacing = mSpacing;
        int galleryRight = getRight() - getLeft() - getPaddingRight();

        // Set state for initial iteration
        View prevIterationView = getChildAt(0);
        int curPosition;
        int curLeftEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition -1;
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
        } else {
            curPosition = 0;
            curLeftEdge = getPaddingLeft();
            mShouldStopFling = true;
        }
        while (curLeftEdge < galleryRight && curPosition >= 0) {
            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition,
                    curLeftEdge, true);

            // Remember some state
            mFirstPosition = curPosition;

            // Set state for next iteration
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
            curPosition--;
        }
    }

    private void fillToGalleryRightLtr() {
        int itemSpacing = mSpacing;
        int galleryRight = getRight() - getLeft() - getPaddingRight();
        int numChildren = getChildCount();
        int numItems = mItemCount;
        
        // Set state for initial iteration
        View prevIterationView = getChildAt(numChildren - 1);
        int curPosition;
        int curLeftEdge;
        
        if (prevIterationView != null) {
            curPosition = mFirstPosition + numChildren;
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
        } else {
            mFirstPosition = curPosition = mItemCount - 1;
            curLeftEdge = getPaddingLeft();
            mShouldStopFling = true;
        }
                
        while (curLeftEdge < galleryRight && curPosition < numItems) {
            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition,
                    curLeftEdge, true);

            // Set state for next iteration
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
            curPosition++;
        }
    }

    /**
     * Obtain a view, either by pulling an existing view from the recycler or by
     * getting a new one from the adapter. If we are animating, make sure there
     * is enough information in the view's layout parameters to animate from the
     * old to new positions.
     * 
     * @param position Position in the ModifiedGallery for the view to obtain
     * @param offset Offset from the selected position
     * @param x X-coordinate indicating where this view should be placed. This
     *        will either be the left or right edge of the view, depending on
     *        the fromLeft parameter
     * @param fromLeft Are we positioning views based on the left edge? (i.e.,
     *        building from left to right)?
     * @return A view that has been added to the ModifiedGallery
     */
    private View makeAndAddView(int position, int offset, int x, boolean fromLeft) {

        View child;
        if (!mDataChanged) {
            child = mRecycler.get(position);
            if (child != null) {
                // Position the view
                setUpChild(child, offset, x, fromLeft);
                return child;
            }
        }

        // Nothing found in the recycler -- ask the adapter for a view
        child = mAdapter.getView(position, null, this);

        // Position the view
        setUpChild(child, offset, x, fromLeft);

        return child;
    }

    /**
     * Helper for makeAndAddView to set the position of a view and fill out its
     * layout parameters.
     * 
     * @param child The view to position
     * @param offset Offset from the selected position
     * @param x X-coordinate indicating where this view should be placed. This
     *        will either be the left or right edge of the view, depending on
     *        the fromLeft parameter
     * @param fromLeft Are we positioning views based on the left edge? (i.e.,
     *        building from left to right)?
     */
    private void setUpChild(View child, int offset, int x, boolean fromLeft) {

        // Respect layout params that are already in the view. Otherwise
        // make some up...
        
        final ViewGroup.LayoutParams vlp = child.getLayoutParams();
        LayoutParams lp;
        if (vlp == null) {
            lp = (LayoutParams) generateDefaultLayoutParams();
        } else if (vlp instanceof LayoutParams) {
            lp = (LayoutParams) vlp;
        } else {
            lp = (LayoutParams) generateLayoutParams(vlp);
        }

        addViewInLayout(child, fromLeft != mIsRtl ? -1 : 0, lp);

        // Get measure specs
        int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec,
                mSpinnerPadding.top + mSpinnerPadding.bottom, lp.height);
        int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec,
                mSpinnerPadding.left + mSpinnerPadding.right, lp.width);

        // Measure child
        child.measure(childWidthSpec, childHeightSpec);

        int childLeft;
        int childRight;

        // Position vertically based on gravity setting
        int childTop = calculateTop(child, true);
        int childBottom = childTop + child.getMeasuredHeight();

        int width = child.getMeasuredWidth();
        if (fromLeft) {
            childLeft = x;
            childRight = childLeft + width;
        } else {
            childLeft = x - width;
            childRight = x;
        }

        child.layout(childLeft, childTop, childRight, childBottom);

    }

    /**
     * Figure out vertical placement based on mGravity
     * 
     * @param child Child to place
     * @return Where the top of the child should be
     */
    private int calculateTop(View child, boolean duringLayout) {
        int myHeight = duringLayout ? getMeasuredHeight() : getHeight();
        int childHeight = duringLayout ? child.getMeasuredHeight() : child.getHeight(); 
        
        int childTop = 0;

        switch (mGravity) {
        case Gravity.TOP:
            childTop = mSpinnerPadding.top;
            break;
        case Gravity.CENTER_VERTICAL:
            int availableSpace = myHeight - mSpinnerPadding.bottom
                    - mSpinnerPadding.top - childHeight;
            childTop = mSpinnerPadding.top + (availableSpace / 2);
            break;
        case Gravity.BOTTOM:
            childTop = myHeight - mSpinnerPadding.bottom - childHeight;
            break;
        }
        return childTop;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Give everything to the gesture detector
        boolean retValue = mGestureDetector.onTouchEvent(event);

        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            // Helper method for lifted finger
            onUp();
        } else if (action == MotionEvent.ACTION_CANCEL) {
            onCancel();
        }
        
        return retValue;
    }
    
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (mDeltaLength > 0 && !mScrollEnableWhenLessContent) {
            return false;
        }
        if (!mShouldCallbackDuringFling) {
            // We want to suppress selection changes
            
            // Remove any future code to set mSuppressSelectionChanged = false
            removeCallbacks(mDisableSuppressSelectionChangedRunnable);

            // This will get reset once we scroll into slots
            if (!mSuppressSelectionChanged) mSuppressSelectionChanged = true;
        }
        
        int childCount = getChildCount();
        int distance;
        
        switch (mTouchMode) {
        case TOUCH_MODE_SCROLL:
            if(Math.abs(velocityX) < MIN_FLING_VELOCITY){
                return false;
            }
            
            mTouchMode = TOUCH_MODE_FLING;
            View view;
            int length = getWidth() - getPaddingLeft() - getPaddingRight();
            int scale = (int) Math.floor(length / (mChildWidth + mSpacing));
            int delta = scale * (mChildWidth + mSpacing);
            
            if (velocityX > 0) {
                //to right
                if (mIsRtl) {
                    view = getChildAt(mDownLastPosition - mFirstPosition);
                    int startEdge = getWidth() - getPaddingRight() - mSpacing;
                    
                    if (view != null) {
                        distance = startEdge - view.getRight();
                    } else {
                        distance = startEdge - getChildAt(getChildCount() - 1).getRight();
                    }
                } else {
                    view = getChildAt(mDownFirstPosition - mFirstPosition);
                    if (view != null) {
                        distance = delta - (view.getLeft() - getPaddingLeft() - mSpacing);
                    }else {
                        distance = getPaddingLeft() + mSpacing - getChildAt(0).getLeft() + delta;
                    }
                }
            } else {
                if (mIsRtl) {
                    view = getChildAt(mDownFirstPosition - mFirstPosition);
                    int startEdge = getWidth() - getPaddingRight() - mSpacing;
                    
                    if (view != null) {
                        distance = -(delta - (startEdge - view.getRight()));
                    } else {
                        distance = -(delta - (startEdge - getChildAt(0).getRight()));
                    }
                } else {
                    view = getChildAt(mDownLastPosition - mFirstPosition);
                    if (view != null) {
                        distance = getPaddingLeft() + mSpacing - view.getLeft();
                    }else {
                        distance = getPaddingLeft() + mSpacing - getChildAt(childCount -1).getLeft();
                    }
                }
            }
            
            reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
            mFlingRunnable.startUsingDistance(distance);
            break;
        case TOUCH_MODE_OVERSCROLL:
            mTouchMode = TOUCH_MODE_OVERFLING;
            break;
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mDeltaLength > 0 && !mScrollEnableWhenLessContent) {
            return false;
        }
        getParent().requestDisallowInterceptTouchEvent(true);
        
        // As the user scrolls, we want to callback selection changes so related-
        // info on the screen is up-to-date with the ModifiedGallery's selection
        if (!mShouldCallbackDuringFling) {
            if (mIsFirstScroll) {
                /*
                 * We're not notifying the client of selection changes during
                 * the fling, and this scroll could possibly be a fling. Don't
                 * do selection changes until we're sure it is not a fling.
                 */
                if (!mSuppressSelectionChanged) mSuppressSelectionChanged = true;
                postDelayed(mDisableSuppressSelectionChangedRunnable, SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT);
            }
        } else {
            if (mSuppressSelectionChanged) mSuppressSelectionChanged = false;
        }
        
        if(mIsFirstScroll) {
            reportScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
        }
        
        mTouchMode = TOUCH_MODE_SCROLL;
        
        int incrementalDeltaX = (int) distanceX;

        if (mMaxOverScrollDistance > getWidth()) {
            mMaxOverScrollDistance = mDefaultMaxOverScrollDistance;
        }
        
        if (mCurrentOverScrollDistance != 0 && mMaxOverScrollDistance != 0) {
            mTouchMode = TOUCH_MODE_OVERSCROLL;
            if (Math.abs(mCurrentOverScrollDistance) >= mMaxOverScrollDistance) {
                incrementalDeltaX = 0;
            } else {
                float coeff = 1 - 1.0f * Math.abs(mCurrentOverScrollDistance) / mMaxOverScrollDistance;
                incrementalDeltaX *=coeff;
            }
        }
        
        if (incrementalDeltaX != 0) {
            trackMotionScroll(-1 * incrementalDeltaX);
        }
        mIsFirstScroll = false;
        return true;
    }
    
    @Override
    public boolean onDown(MotionEvent e) {
        
        if(mTouchMode == TOUCH_MODE_FLING || mTouchMode == TOUCH_MODE_OVERFLING) {
            mTouchMode = TOUCH_MODE_SCROLL;
            reportScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
        }else {
            mTouchMode = TOUCH_MODE_DOWN;
        }
        
        // Kill any existing fling/scroll
        mFlingRunnable.stop(false);
        
        // Get the item's view that was touched
        mDownTouchPosition = pointToPosition((int) e.getX(), (int) e.getY());
        
        if (mDownTouchPosition >= 0) {
            mDownTouchView = getChildAt(mDownTouchPosition - mFirstPosition);
            mDownTouchView.setPressed(true);
        }
        
        mMotionX = (int) e.getX();
        mMotionY = (int) e.getY();
        
        mDownFirstPosition = mFirstPosition;
        mDownLastPosition = mFirstPosition + getChildCount() - 1;
        // Reset the multiple-scroll tracking state
        mIsFirstScroll = true;
        // Must return true to get matching events for this down event.
        return true;
    }

    /**
     * Called when a touch event's action is MotionEvent.ACTION_UP.
     */
    void onUp() {
        switch (mTouchMode) {
        case TOUCH_MODE_DOWN:
            scrollIntoSlots();
            break;
        case TOUCH_MODE_SCROLL:
            scrollIntoSlots();
            break;
        case TOUCH_MODE_OVERSCROLL:
        case TOUCH_MODE_OVERFLING:
            if (mCurrentOverScrollDistance != 0){
                if(mLastScrollState != OnScrollListener.SCROLL_STATE_FLING) {
                    reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
                }
                
                mFlingRunnable.startSpringback();
            }
            break;
        case TOUCH_MODE_FLING:
            break;
        }
        dispatchUnpress();
    }
    
    /**
     * Called when a touch event's action is MotionEvent.ACTION_CANCEL.
     */
    void onCancel() {
        onUp();
    }
    
    @Override
    public void onLongPress(MotionEvent e) {
        if (mDownTouchPosition < 0) {
            return;
        }
        
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        long id = getItemIdAtPosition(mDownTouchPosition);
        dispatchLongPress(mDownTouchView, mDownTouchPosition, id);
    }

    // Unused methods from GestureDetector.OnGestureListener below
    
    @Override
    public void onShowPress(MotionEvent e) {
    }

    // Unused methods from GestureDetector.OnGestureListener above
    
    private void dispatchUnpress() {
        
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).setPressed(false);
        }
        
        setPressed(false);
    }
    
    @Override
    public void dispatchSetSelected(boolean selected) {
        /*
         * We don't want to pass the selected state given from its parent to its
         * children since this widget itself has a selected state to give to its
         * children.
         */
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
        
        // Show the pressed state on the selected child
        if (mSelectedChild != null) {
            mSelectedChild.setPressed(pressed);
        }
    }

    @Override
    protected ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {

        final int longPressPosition = getPositionForView(originalView);
        if (longPressPosition < 0) {
            return false;
        }
        
        final long longPressId = mAdapter.getItemId(longPressPosition);
        return dispatchLongPress(originalView, longPressPosition, longPressId);
    }

    @Override
    public boolean showContextMenu() {
        
        if (isPressed() && mSelectedPosition >= 0) {
            int index = mSelectedPosition - mFirstPosition;
            View v = getChildAt(index);
            return dispatchLongPress(v, mSelectedPosition, mSelectedRowId);
        }        
        
        return false;
    }

    private boolean dispatchLongPress(View view, int position, long id) {
        boolean handled = false;
        
        if (mOnItemLongClickListener != null) {
            handled = mOnItemLongClickListener.onItemLongClick(this, mDownTouchView,
                    mDownTouchPosition, id);
        }

        if (!handled) {
            mContextMenuInfo = new AdapterContextMenuInfo(view, position, id);
            handled = super.showContextMenuForChild(this);
        }

        if (handled) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        
        return handled;
    }
    
    @Override
    void setSelectedPositionInt(int position) {
        super.setSelectedPositionInt(position);

        // Updates any metadata we keep about the selected item.
        updateSelectedItemMetadata();
    }

    private void updateSelectedItemMetadata() {
        
        View oldSelectedChild = mSelectedChild;

        View child = mSelectedChild = getChildAt(mSelectedPosition - mFirstPosition);
        if (child == null) {
            return;
        }

        child.setSelected(true);
        child.setFocusable(true);

        if (hasFocus()) {
            child.requestFocus();
        }

        // We unfocus the old child down here so the above hasFocus check
        // returns true
        if (oldSelectedChild != null && oldSelectedChild != child) {

            // Make sure its drawable state doesn't contain 'selected'
            oldSelectedChild.setSelected(false);
            
            // Make sure it is not focusable anymore, since otherwise arrow keys
            // can make this one be focused
            oldSelectedChild.setFocusable(false);
        }
        
    }
    
    /**
     * Describes how the child views are aligned.
     * @param gravity
     * 
     * @attr ref android.R.styleable#Gallery_gravity
     */
    public void setGravity(int gravity)
    {
        if (mGravity != gravity) {
            mGravity = gravity;
            requestLayout();
        }
    }

    /**
     * Responsible for fling behavior. Use {@link #startUsingVelocity(int)} to
     * initiate a fling. Each frame of the fling is handled in {@link #run()}.
     * A FlingRunnable will keep re-posting itself until the fling is done.
     */
    private class FlingRunnable implements Runnable {
        /**
         * Tracks the decay of a fling scroll
         */
        private OverScroller mScroller;
//        private Scroller mScroller;

        /**
         * X value reported by mScroller on the previous fling
         */
        private int mLastFlingX;
        
        private int mLastOverFlingX = 0;
        private int mLastDelta;

        public FlingRunnable() {
            mScroller = new OverScroller(getContext());
//            mScroller = new Scroller(getContext());
        }

        private void startCommon() {
            // Remove any pending flings
            removeCallbacks(this);
        }

        public void startUsingDistance(int distance) {
            if (distance == 0) return;
            
            mTouchMode = TOUCH_MODE_FLING;
            
            startCommon();
            
            mLastFlingX = 0;
//            mScroller.setInterpolator(new DecelerateInterpolator());
            mScroller.startScroll(0, 0, -distance, 0, mAnimationDuration);
            postOnAnimation(this);
        }
        
        
        public void startSpringback() {
            if (mScroller.springBack(mCurrentOverScrollDistance, 0, 0, 0, 0, 0)) {
                mTouchMode = TOUCH_MODE_OVERFLING;
                mLastOverFlingX = mCurrentOverScrollDistance;
                invalidate();
                postOnAnimation(this);
            } else {
                mTouchMode = TOUCH_MODE_REST;
            }
        }
        
        public void stop(boolean scrollIntoSlots) {
            removeCallbacks(this);
            endFling(scrollIntoSlots);
        }
        
        private void endFling(boolean scrollIntoSlots) {
            /*
             * Force the scroller's status to finished (without setting its
             * position to the end)
             */
            mScroller.forceFinished(true);
            
            if (scrollIntoSlots) {
                scrollIntoSlots();
            }else {
                reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            }
        }
        
        @Override
        public void run() {
            if (mItemCount == 0) {
                endFling(true);
                return;
            }
            final OverScroller scroller = mScroller;
            
            switch (mTouchMode) {
            case TOUCH_MODE_SCROLL:
            case TOUCH_MODE_FLING:
                
                mShouldStopFling = false;
                
                boolean more = scroller.computeScrollOffset();
                final int x = scroller.getCurrX();
                
                // Flip sign to convert finger direction to list items direction
                // (e.g. finger moving down means list is moving towards the top)
                int delta = mLastFlingX - x;

                boolean isAtEdge = trackMotionScroll(delta);
                
                if (more && !mShouldStopFling && !isAtEdge) {
                    mLastFlingX = x;
                    mLastDelta = delta;
                    post(this);
                } else if (more && !mShouldStopFling && isAtEdge){
                    //一次Fling没有完成但是此时已经达到边界的情况下，改成startSpringback（）效果
                    endFling(false);
                    if (mTouchMode == TOUCH_MODE_FLING) {
                        mTouchMode = TOUCH_MODE_OVERFLING;
                    } else {
                        mTouchMode = TOUCH_MODE_OVERSCROLL;
                    }
                    
                    if(mLastScrollState != OnScrollListener.SCROLL_STATE_FLING) {
                        reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
                    }
                    
                    startSpringback();
                } else {
                    endFling(true);
                }
                
                break;
            case TOUCH_MODE_OVERFLING:
            case TOUCH_MODE_OVERSCROLL:
                if (scroller.computeScrollOffset()) {
                    int currX = scroller.getCurrX();
                    final int deltaX = currX - mLastOverFlingX;
                    mLastOverFlingX = currX;
                    
                    if(deltaX != 0)
                        trackMotionScroll(-deltaX);
                    invalidate();
                    postOnAnimation(this);
                } else {
                    endFling(false);
                    mTouchMode = TOUCH_MODE_REST;
                }
                break;
            default :
                mTouchMode = TOUCH_MODE_REST;
                if(mLastScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                    reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                }
            }
        }
        
    }
    
    /**
     * ModifiedGallery extends LayoutParams to provide a place to hold current
     * Transformation information along with previous position/transformation
     * info.
     */
    public static class LayoutParams extends ViewGroup.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
    
    /**
     * Optional callback to notify client when scroll position has changed
     */
    private OnScrollListener mOnScrollListener;
    
    /**
     * The last scroll state reported to clients through {@link com.letv.shared.widget.ModifiedGallery.OnScrollListener}.
     */
    private int mLastScrollState = OnScrollListener.SCROLL_STATE_IDLE;
    
    /**
     * Set the listener that will receive notifications every time the list scrolls.
     *
     * @param l the scroll listener
     * 
     * {@hide}
     */
    public void setOnScrollListener(OnScrollListener l) {
        mOnScrollListener = l;
        invokeOnItemScrollListener();
    }


    /**
     * Notify our scroll listener (if there is one) of a change in scroll state
     * 
     * {@hide}
     */
    void invokeOnItemScrollListener() {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(this, mFirstPosition, getChildCount(), mItemCount);
        }
    }
    
    /**
     * Fires an "on scroll state changed" event to the registered
     * {@link com.ModifiedGallery.sdk.ModifiedGallery.OnScrollListener}, if any. The state change
     * is fired only if the specified state is different from the previously known state.
     *
     * @param newState The new scroll state.
     */
    void reportScrollStateChange(int newState) {
        if (newState != mLastScrollState) {
            mLastScrollState = newState;
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(this, newState);
            }
        }
    }
    
    /**
     * Interface definition for a callback to be invoked when the list or grid
     * has been scrolled.
     * 
     * {@hide}
     */
    public interface OnScrollListener {

        /**
         * The view is not scrolling. Note navigating the list using the trackball counts as
         * being in the idle state since these transitions are not animated.
         */
        public static int SCROLL_STATE_IDLE = 0;

        /**
         * The user is scrolling using touch, and their finger is still on the screen
         */
        public static int SCROLL_STATE_TOUCH_SCROLL = 1;

        /**
         * The user had previously been scrolling using touch and had performed a fling. The
         * animation is now coasting to a stop
         */
        public static int SCROLL_STATE_FLING = 2;

        /**
         * Callback method to be invoked while the list view or grid view is being scrolled. If the
         * view is being scrolled, this method will be called before the next frame of the scroll is
         * rendered. In particular, it will be called before any calls to
         * {@link android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)}.
         *
         * @param view The view whose scroll state is being reported
         *
         * @param scrollState The current scroll state. One of {@link #SCROLL_STATE_IDLE},
         * {@link #SCROLL_STATE_TOUCH_SCROLL} or {@link #SCROLL_STATE_IDLE}.
         */
        public void onScrollStateChanged(ModifiedGallery view, int scrollState);

        /**
         * Callback method to be invoked when the list or grid has been scrolled. This will be
         * called after the scroll has completed
         * @param view The view whose scroll state is being reported
         * @param firstVisibleItem the index of the first visible cell (ignore if
         *        visibleItemCount == 0)
         * @param visibleItemCount the number of visible cells
         * @param totalItemCount the number of items in the gallery adaptor
         */
        public void onScroll(ModifiedGallery view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount);
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
    
}
