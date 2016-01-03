package com.letv.shared.widget;

/**
 * LeListView: Swipe-ListView, DragSort-ListView, Normal-ListView, and support Swipe-Item.
 * used class Helper: BaseSwipeHelper, SwipeListViewHelper and DragSortHelper.
 * 
 * @author wangziming
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.letv.shared.R;
import com.letv.shared.widget.DragSortHelper.DragListener;
import com.letv.shared.widget.DragSortHelper.DragScrollProfile;
import com.letv.shared.widget.DragSortHelper.DragSortListener;
import com.letv.shared.widget.DragSortHelper.DropListener;
import com.letv.shared.widget.DragSortHelper.FloatViewManager;

/**
 * ListView subclass that provides the swipe functionality
 */
public class LeListView extends ListView implements SwipeListViewHelper.Callback{
    public static final String TAG = "LeListView";

    /** Normal ListView */
    public final static int LE_NONE = 0;
    /** SwipeListView */
    public final static int LE_SWIPE = 1;
    /** DragSortListView */
    public final static int LE_DRAG_SORT = 2;
    
    public static final int DEFAULT_DISMISS_ANIM_TIME = 200;
    
    /** LeListView's Mode */
    private int mLeListViewMode = LE_NONE;
    
    private LeListViewListener mLeListViewListener;
    
    private List<Boolean> mChecked = new ArrayList<Boolean>();
    private List<PendingDismissData> mPendingDismisses = new ArrayList<PendingDismissData>();
    
    // The number of records do Dismiss animation.
    private int mDismissAnimationRefCount = 0;
    private long mDismissAnimationTime = DEFAULT_DISMISS_ANIM_TIME;
    private boolean mIsDismissAnimating = false;
    
    //private int mSwipeDrawableChecked = 0;
    //private int mSwipeDrawableUnchecked = 0;
    
    private int mSwipeFrontView = 0;
    private int mSwipeBackView = 0;
    
    private OnScrollListener mLeOnScrollListener;
    private AdapterWrapper mAdapterWrapper;

    private SwipeListViewHelper mSwipeHelper;
    private DragSortHelper mDragSortHelper;
    
    private int mMaxYOverscrollDistance;
    
    /**
     * If you create a View programmatically you need send back and front identifier
     *
     * @param context context
     */
    public LeListView(Context context) {
        super(context);
        init(context);
    }

    /**
     * @see android.widget.ListView#ListView(android.content.Context, android.util.AttributeSet)
     */
    public LeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * @see android.widget.ListView#ListView(android.content.Context, android.util.AttributeSet, int)
     */
    public LeListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LeListView);
        mLeListViewMode = typedArray.getInt(R.styleable.LeListView_leListViewMode, LE_NONE);

        //mSwipeDrawableChecked = typedArray.getResourceId(R.styleable.LeListView_leSwipeDrawableChecked, 0);
        //mSwipeDrawableUnchecked = typedArray.getResourceId(R.styleable.LeListView_leSwipeDrawableUnchecked, 0);
        mSwipeFrontView = typedArray.getResourceId(R.styleable.LeListView_leSwipeFrontView, 0);
        mSwipeBackView = typedArray.getResourceId(R.styleable.LeListView_leSwipeBackView, 0);
        
        mSwipeHelper = new SwipeListViewHelper(context, typedArray, this, this);
        mDragSortHelper = new DragSortHelper(context, typedArray, this);
        
        mLeOnScrollListener = new OnScrollListenerWrapper();
        super.setOnScrollListener(mLeOnScrollListener);

        typedArray.recycle();
    }

    private void init (Context context) {
        mSwipeHelper = new SwipeListViewHelper(context, this, this);
        mDragSortHelper = new DragSortHelper(context, this);
        
        mLeOnScrollListener = new OnScrollListenerWrapper();
        super.setOnScrollListener(mLeOnScrollListener);
    }

    /**
     * Set ListView Mode
     */
    public void setLeListViewMode(int listViewMode) {
        int preLeMode = mLeListViewMode;
        mLeListViewMode = listViewMode;
        
        ListAdapter adapter = null;
        ListAdapter listAdapter = getAdapter();
        if (listAdapter instanceof HeaderViewListAdapter) {
            adapter = ((HeaderViewListAdapter) listAdapter).getWrappedAdapter();
        } else {
            adapter = listAdapter;
        }
        
        if (preLeMode == LE_DRAG_SORT) {
            if (adapter instanceof AdapterWrapper)
                setAdapter(((AdapterWrapper) adapter).getAdapter());
        } else {
            setAdapter(adapter);
        }
    }
    
    public int getLeListViewMode() {
        return mLeListViewMode;
    }
    
    public DragSortHelper getDragSortHelper() {
        return mDragSortHelper;
    }
    
    private class OnScrollListenerWrapper implements OnScrollListener {
        private OnScrollListener onScrollListener;

        public OnScrollListenerWrapper() {
        }
        
        public OnScrollListenerWrapper(OnScrollListener onScrollListener) {
            this.onScrollListener = onScrollListener;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mSwipeHelper != null) {
                mSwipeHelper.onScrollStateChanged(view, scrollState);
            }
            
            // Dispatch to the outside 
            if (onScrollListener != null) {
                onScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (mSwipeHelper != null) {
                mSwipeHelper.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
            
            // Dispatch to the outside 
            if (onScrollListener != null) {
                onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }
    }
    
    /**
     * Override setOnScrollListener for Avoiding this function invalid
     */
    @Override
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        if (mLeListViewMode == LE_SWIPE) {
            mLeOnScrollListener = new OnScrollListenerWrapper(onScrollListener);
            super.setOnScrollListener(mLeOnScrollListener);
        } else {
            super.setOnScrollListener(onScrollListener);
        }
    }

    /**
     * For each DragSortListView Listener interface implemented by
     * <code>adapter</code>, this method calls the appropriate
     * set*Listener method with <code>adapter</code> as the argument.
     * 
     * @param adapter The ListAdapter providing data to back
     * DragSortListView.
     *
     * @see android.widget.ListView#setAdapter(android.widget.ListAdapter)
     */
    @Override
    public void setAdapter(ListAdapter adapter) {
        if (adapter == null) {
           return;
        }
        
        switch (mLeListViewMode) {
            case LE_SWIPE:
                super.setAdapter(adapter);
                mSwipeHelper.resetItems();
                resetItems();
                adapter.registerDataSetObserver(new DataSetObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        mSwipeHelper.onListChanged();
                        mSwipeHelper.resetItems();
                    }
                });
                break;
    
            case LE_DRAG_SORT:
                if (adapter != null) {
                    mAdapterWrapper = new AdapterWrapper(adapter);
                    mDragSortHelper.setAdapter(adapter);
                } else {
                    mAdapterWrapper = null;
                }
                super.setAdapter(mAdapterWrapper);
                // support: when LE_DRAG_SORT, can use SwipeListView's function as openAnimate/dismiss. 
                //mSwipeHelper.resetItems();
                break;
    
            case LE_NONE:
            default:
                super.setAdapter(adapter);
                break;
        }
    }
    
    /**
     * Close all opened items
     */
    public void closeOpenedItems() {
        if (mSwipeHelper != null)
            mSwipeHelper.closeOpenedItems();
    }
    
    /**
     * Close the opened item
     */
    public void closeTheOpenedItem(int position) {
        if (mSwipeHelper != null)
            mSwipeHelper.closeTheOpenedItem(position);
    }
    
    /**
     * Set offset on left
     *
     * @param offsetLeft Offset
     */
    public void setOffsetLeft(float offsetLeft) {
        if (mSwipeHelper != null)
            mSwipeHelper.setOffsetLeft(offsetLeft);
    }
    
    /**
     * Set offset on right
     *
     * @param offsetRight Offset
     */
    public void setOffsetRight(float offsetRight) {
        if (mSwipeHelper != null)
            mSwipeHelper.setOffsetRight(offsetRight);
    }
    
    /**
     * Set switch-line, when swipe to left.
     *
     * @param  swipeRightSwitchLine
     */
    public void setSwipeLeftSwitchLine(float swipeLeftSwitchLine) {
        if (mSwipeHelper != null)
            mSwipeHelper.setSwipeLeftSwitchLine(swipeLeftSwitchLine);
    }
    
    /**
     * Set switch-line, when swipe to right.
     *
     * @param  swipeRightSwitchLine
     */
    public void setSwipeRightSwitchLine(float swipeRightSwitchLine) {
        if (mSwipeHelper != null)
            mSwipeHelper.setSwipeRightSwitchLine(swipeRightSwitchLine);
    }
    
    
    
    /**
     * Set swipe enabled
     *
     * @param enabled
     */
    public void setSwipeEnabled(boolean enabled) {
        if (mSwipeHelper != null)
            mSwipeHelper.setSwipeEnabled(enabled);
    }
    
    /**
     * Check swipe is enabled
     *
     * @return
     */
    protected boolean isSwipeEnabled() {
        if (mSwipeHelper != null)
            mSwipeHelper.isSwipeEnabled();
        return false;
    }
    
    public void setSwipeListViewListener(SwipeListViewListener swipeListViewListener){
        mLeListViewListener = swipeListViewListener;
        if (mSwipeHelper != null)
            mSwipeHelper.setSwipeListViewListener(swipeListViewListener);
    }
    
    public void setSwipeListViewSwitchListener(SwipeListViewSwitchListener swipeListViewSwitchListener){
        if (mSwipeHelper != null)
            mSwipeHelper.setSwipeListViewSwitchListener(swipeListViewSwitchListener);
    }

    public void setOverOffsetEnabled(boolean overOffsetEnabled) {
        if (mSwipeHelper != null)
            mSwipeHelper.setOverOffsetEnabled(overOffsetEnabled);
    }
    
    /**
     * Open ListView's item
     *
     * @param position Position that you want open
     */
    public void openAnimate(int position) {
        mSwipeHelper.openAnimate(position);
    }

    /**
     * Close ListView's item
     *
     * @param position Position that you want open
     */
    public void closeAnimate(int position) {
        mSwipeHelper.closeAnimate(position);
    }
    
    /**
     * Dismiss item
     *
     * @param position Position that you want open
     * @deprecated
     */
    public void dismiss(int position) {
        int height = performDismiss(position);// +numHeaders inside
        if (height <= 0) {
            int[] dismissPositions = new int[1];
            int retPosition = position - getHeaderViewsCount();// -numHeaders
            dismissPositions[0] = retPosition;
            onDismiss(dismissPositions);
            resetPendingDismisses();
        }
    }
    
    /**
     * Dismiss items selected
     * 
     * @param positions These positions is items which prepare for dismissing.
     * @deprecated
     */
    public void dissmiss(int[] positions) {
        int height = 0;
        for (int i = 0; i < positions.length; i++) {
            int position = positions[i];
            int auxHeight = performDismiss(position);// +numHeaders inside
            if (auxHeight > 0) {
                height = auxHeight;
            }
        }
        if (height <= 0) {
            onDismiss(positions);
            resetPendingDismisses();
        }
    }

    /**
     * Dismiss items selected
     * @deprecated
     */
    public void dismissSelected() {
        List<Integer> list = getPositionsSelected();
        int[] dismissPositions = new int[list.size()];
        int height = 0;
        for (int i = 0; i < list.size(); i++) {
            int position = list.get(i);
            dismissPositions[i] = position;// position unChange
            int auxHeight = performDismiss(position);// +numHeaders inside
            if (auxHeight > 0) {
                height = auxHeight;
            }
        }
        if (height <= 0) {
            onDismiss(dismissPositions);
            resetPendingDismisses();
        }
    }
    
    /**
     * Set if all items opened will be closed when the user moves the ListView
     *
     * @param swipeCloseAllItemsWhenMoveList
     */
    public void setSwipeCloseAllItemsWhenMoveList(boolean swipeCloseAllItemsWhenMoveList) {
        if (mSwipeHelper != null)
            mSwipeHelper.setSwipeClosesAllItemsWhenListMoves(swipeCloseAllItemsWhenMoveList);
    }

    /**
     * Set swipe mode
     *
     * @param swipeMode
     */
    public void setSwipeMode(int swipeMode) {
        if (mSwipeHelper != null)
            mSwipeHelper.setSwipeMode(swipeMode);
    }

    /**
     * Return action on left
     *
     * @return Action
     */
    public int getSwipeActionLeft() {
        if (mSwipeHelper != null)
            return mSwipeHelper.getSwipeActionLeft();
        return BaseSwipeHelper.SWIPE_ACTION_NONE;
    }

    /**
     * Set action on left
     *
     * @param swipeActionLeft Action
     */
    public void setSwipeActionLeft(int swipeActionLeft) {
        if (mSwipeHelper != null)
            mSwipeHelper.setSwipeActionLeft(swipeActionLeft);
    }

    /**
     * Return action on right
     *
     * @return Action
     */
    public int getSwipeActionRight() {
        if (mSwipeHelper != null)
            return mSwipeHelper.getSwipeActionRight();
        return BaseSwipeHelper.SWIPE_ACTION_NONE;
    }

    /**
     * Set action on right
     *
     * @param swipeActionRight Action
     */
    public void setSwipeActionRight(int swipeActionRight) {
        if (mSwipeHelper != null)
            mSwipeHelper.setSwipeActionRight(swipeActionRight);
    }

    /**
     * Sets animation time when user drops cell
     *
     * @param animationTime milliseconds
     */
    public void setDismissAnimationTime(long animationTime) {
        if (animationTime > 0) {
            this.mDismissAnimationTime = animationTime;
        } else {
            this.mDismissAnimationTime = DEFAULT_DISMISS_ANIM_TIME;
        }
    }
    
    /**
     * Adds new items when adapter is modified
     */
    public void resetItems() {
        final ListAdapter adapter = getAdapter();
        if (adapter != null) {
            int count = adapter.getCount();
            for (int i = mChecked.size(); i <= count; i++) {
                mChecked.add(false);
            }
        }
    }

    /**
     * @see android.widget.ListView#onInterceptTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mLeListViewMode == LE_DRAG_SORT) {
            // Set backView to not click, frontView stay its original state
            // The weather app: can pop-up menu, and drag the sort,
            // the situation, the priority to determine whether a menu to open,
            // such as can click open button in the menu, whether or not mLeListViewMode == LE_DRAG_SORT.
            /*if (!mIsAnimating && action == MotionEvent.ACTION_DOWN) {
                mIsTouchOpened = setBackViewClickable(false, (int) x, (int) y);
            }
            if (mIsTouchOpened || !mDragEnabled) {
                mIsTouchOpened = false;
                return super.onInterceptTouchEvent(ev);
            }*/
            if (ev.getAction() == MotionEvent.ACTION_DOWN && mSwipeHelper != null) {
                // Set backView to not click, frontView stay its original state.
                // Sometimes the swipeable item puts in DragSort-ListView.
                mSwipeHelper.setBackViewClickable(true, (int) ev.getX(), (int) ev.getY());
            }
            
            return mDragSortHelper.onInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev);
        }

        if (mLeListViewMode == LE_SWIPE && isEnabled()) {
            return mSwipeHelper.onInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev);
        }
        
        if (mLeListViewMode == LE_NONE) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN && mSwipeHelper != null) {
                //Set backView to not click, frontView stay its original state.
                // Sometimes the swipeable item puts in Normal-ListView.
                mSwipeHelper.setBackViewClickable(true, (int) ev.getX(), (int) ev.getY());
            }
        }
        return super.onInterceptTouchEvent(ev);
    }
    
    boolean onInterceptTouchEventSuper(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }
    
    /**
     * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
     */
    @SuppressLint("Recycle")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLeListViewMode == LE_DRAG_SORT) {
            if (/*mIsTouchOpened && */mIsDismissAnimating) {
                //mIsTouchOpened = false;
                return super.onTouchEvent(ev);
            }
            
            return mDragSortHelper.onTouchEvent(ev) || super.onTouchEvent(ev);
        }
        
        if (mLeListViewMode == LE_SWIPE) {
            return mSwipeHelper.onTouchEvent(ev) || super.onTouchEvent(ev);
        }
        
        return super.onTouchEvent(ev);
    }
    
    boolean onTouchEventSuper(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    public void setFloatViewManager(FloatViewManager manager) {
        if (mDragSortHelper != null)
            mDragSortHelper.setFloatViewManager(manager);
    }
    
    public void setDragListener(DragListener l) {
        if (mDragSortHelper != null)
            mDragSortHelper.setDragListener(l);
    }
    
    public void setDropListener(DropListener l) {
        if (mDragSortHelper != null)
            mDragSortHelper.setDropListener(l);
    }
    
    public void setDragSortListener(DragSortListener l) {
        if (mDragSortHelper != null)
            mDragSortHelper.setDragSortListener(l);
    }

    public boolean isDragSortEnabled() {
        if (mDragSortHelper != null)
            return mDragSortHelper.isDragSortEnabled();
        return false;
    }
    
    /**
     * Usually called from a FloatViewManager. The float alpha
     * will be reset to the xml-defined value every time a drag
     * is stopped.
     */
    public void setFloatAlpha(float alpha) {
        if (mDragSortHelper != null)
            mDragSortHelper.setFloatAlpha(alpha);
    }

    public float getFloatAlpha() {
        if (mDragSortHelper != null)
            return mDragSortHelper.getFloatAlpha();
        return 0;
        
    }
    
    /**
     * Set maximum drag scroll speed in positions/second. Only applies
     * if using default ScrollSpeedProfile.
     * 
     * @param max Maximum scroll speed.
     */
    public void setMaxScrollSpeed(float max) {
        if (mDragSortHelper != null)
            mDragSortHelper.setMaxScrollSpeed(max);
    }
    
    /**
     * Move an item, by passing the drag-sort process. Simply calls
     * through to {@link DropListener#drop(int, int)}.
     * 
     * @param from Position to move (NOTE: headers/footers ignored!
     * this is a position in your input ListAdapter).
     * @param to Target position (NOTE: headers/footers ignored!
     * this is a position in your input ListAdapter).
     */
    public void moveItem(int from, int to) {
        if (mDragSortHelper != null)
            mDragSortHelper.moveItem(from, to);
    }
    
    /**
     * Cancel a drag. Calls {@link #stopDrag(boolean, boolean)} with
     * <code>true</code> as the first argument.
     */
    public void cancelDrag() {
        if (mDragSortHelper != null)
            mDragSortHelper.cancelDrag();
    }
    
    /**
     * Stop a drag in progress. Pass <code>true</code> if you would
     * like to remove the dragged item from the list.
     *
     * @param remove Remove the dragged item from the list. Calls
     * a registered RemoveListener, if one exists. Otherwise, calls
     * the DropListener, if one exists.
     *
     * @return True if the stop was successful. False if there is
     * no floating View.
     */
    public boolean stopDrag() {
        if (mDragSortHelper != null)
            return mDragSortHelper.stopDrag();
        return false;
    }
    
    /**
     * Set the width of each drag scroll region by specifying
     * a fraction of the ListView height.
     *
     * @param heightFraction Fraction of ListView height. Capped at
     * 0.5f.
     * 
     */
    public void setDragScrollStart(float heightFraction) {
        setDragScrollStarts(heightFraction, heightFraction);
    }

    /**
     * Set the width of each drag scroll region by specifying
     * a fraction of the ListView height.
     *
     * @param upperFrac Fraction of ListView height for up-scroll bound.
     * Capped at 0.5f.
     * @param lowerFrac Fraction of ListView height for down-scroll bound.
     * Capped at 0.5f.
     * 
     */
    public void setDragScrollStarts(float upperFrac, float lowerFrac) {
        if (mDragSortHelper != null)
            mDragSortHelper.setDragScrollStarts(upperFrac, lowerFrac);
    }
    
    /**
     * Allows for easy toggling between a DragSortListView
     * and a regular old ListView. If enabled, items are
     * draggable, where the drag init mode determines how
     * items are lifted (see {@link setDragInitMode(int)}).
     * If disabled, items cannot be dragged.
     *
     * @param enabled Set <code>true</code> to enable list
     * item dragging
     */
    public void setDragEnabled(boolean enabled) {
        if (mDragSortHelper != null)
            mDragSortHelper.setDragEnabled(enabled);
    }

    public boolean isDragEnabled() {
        if (mDragSortHelper != null)
            return mDragSortHelper.isDragEnabled();
        return false;
    }
    
    /**
     * Start a drag of item at <code>position</code> using the
     * registered FloatViewManager. Calls through
     * to {@link #startDrag(int, android.view.View,int,int,int)} after obtaining
     * the floating View from the FloatViewManager.
     *
     * @param position Item to drag.
     * @param dragFlags Flags that restrict some movements of the
     * floating View. For example, set <code>dragFlags |= 
     * ~{@link #DRAG_NEG_X}</code> to allow dragging the floating
     * View in all directions except off the screen to the left.
     * @param deltaX Offset in x of the touch coordinate from the
     * left edge of the floating View (i.e. touch-x minus float View
     * left).
     * @param deltaY Offset in y of the touch coordinate from the
     * top edge of the floating View (i.e. touch-y minus float View
     * top).
     *
     * @return True if the drag was started, false otherwise. This
     * <code>startDrag</code> will fail if we are not currently in
     * a touch event, there is no registered FloatViewManager,
     * or the FloatViewManager returns a null View.
     */
    public boolean startDrag(int position, int dragFlags, int deltaX, int deltaY) {
        if (mDragSortHelper != null)
            return mDragSortHelper.startDrag(position, dragFlags, deltaX, deltaY);
        return false;
    }
    
    /**
     * Start a drag of item at <code>position</code> without using
     * a FloatViewManager.
     *
     * @param position Item to drag.
     * @param floatView Floating View.
     * @param dragFlags Flags that restrict some movements of the
     * floating View. For example, set <code>dragFlags |= 
     * ~{@link #DRAG_NEG_X}</code> to allow dragging the floating
     * View in all directions except off the screen to the left.
     * @param deltaX Offset in x of the touch coordinate from the
     * left edge of the floating View (i.e. touch-x minus float View
     * left).
     * @param deltaY Offset in y of the touch coordinate from the
     * top edge of the floating View (i.e. touch-y minus float View
     * top).
     *
     * @return True if the drag was started, false otherwise. This
     * <code>startDrag</code> will fail if we are not currently in
     * a touch event, <code>floatView</code> is null, or there is
     * a drag in progress.
     */
    public boolean startDrag(int position, View floatView, int dragFlags, int deltaX, int deltaY) {
        if (mDragSortHelper != null)
            return mDragSortHelper.startDrag(position, floatView, dragFlags, deltaX, deltaY);
        return false;
    }
    
    /**
     * Completely custom scroll speed profile. Default increases linearly
     * with position and is constant in time. Create your own by implementing
     * {@link DragSortListView.DragScrollProfile}.
     * 
     * @param ssp
     */
    public void setDragScrollProfile(DragScrollProfile ssp) {
        if (mDragSortHelper != null)
            mDragSortHelper.setDragScrollProfile(ssp);
    }
    
    /**
     * Use this to move the check state of an item from one position to another
     * in a drop operation. If you have a choiceMode which is not none, this
     * method must be called when the order of items changes in an underlying
     * adapter which does not have stable IDs (see
     * {@link android.widget.ListAdapter#hasStableIds()}). This is because without IDs, the
     * ListView has no way of knowing which items have moved where, and cannot
     * update the check state accordingly.
     * <p>
     * A word of warning about a "feature" in Android that you may run into when
     * dealing with movable list items: for an adapter that <em>does</em> have
     * stable IDs, ListView will attempt to locate each item based on its ID and
     * move the check state from the item's old position to the new position —
     * which is all fine and good (and removes the need for calling this
     * function), except for the half-baked approach. Apparently to save time in
     * the naive algorithm used, ListView will only search for an ID in the
     * close neighborhood of the old position. If the user moves an item too far
     * (specifically, more than 20 rows away), ListView will give up and just
     * force the item to be unchecked. So if there is a reasonable chance that
     * the user will move items more than 20 rows away from the original
     * position, you may wish to use an adapter with unstable IDs and call this
     * method manually instead.
     * 
     * @param from
     * @param to
     */
    public void moveCheckState(int from, int to) {
        if (mDragSortHelper != null)
            mDragSortHelper.moveCheckState(from, to);
    }
    
    /**
     * Use this when an item has been deleted, to move the check state of all
     * following items up one step. If you have a choiceMode which is not none,
     * this method must be called when the order of items changes in an
     * underlying adapter which does not have stable IDs (see
     * {@link android.widget.ListAdapter#hasStableIds()}). This is because without IDs, the
     * ListView has no way of knowing which items have moved where, and cannot
     * update the check state accordingly.
     * 
     * See also further comments on {@link #moveCheckState(int, int)}.
     * 
     * @param position
     */
    public void removeCheckState(int position) {
        if (mDragSortHelper != null)
            mDragSortHelper.removeCheckState(position);
    }

    /**
     * Unselected choice state in item
     */
    protected void unselectedChoiceStates() {
        int start = getFirstVisiblePosition();
        int end = getLastVisiblePosition();
        for (int i = 0; i < mChecked.size(); i++) {
            if (mChecked.get(i) && i >= start && i <= end) {
                //reloadChoiceStateInView(getChildAt(i - start).findViewById(mSwipeFrontView), i);
            }
            mChecked.set(i, false);
        }
        //returnOldActions();
    }

    /**
     * Unselected choice state in item
     */
    protected int performDismiss(int position) {
        int numHeaders = getHeaderViewsCount();
        position += numHeaders;// + numHeaders
        
        int start = getFirstVisiblePosition();
        int end = getLastVisiblePosition();
        View view = getChildAt(position - start);
        if (position >= start && position <= end) {
            performDismiss(view, position, true);
            int a = view.getHeight();
            return a;
        } else {
            position -= numHeaders;
            mPendingDismisses.add(new PendingDismissData(position, null));
            return 0;
        }
    }

    /**
     * Draw cell for display if item is selected or not
     *
     * @param mFrontView view to draw
     * @param position  position in list
     */
    /*protected void reloadChoiceStateInView(View frontView, int position) {
        if (isChecked(position)) {
            if (mSwipeDrawableChecked > 0) frontView.setBackgroundResource(mSwipeDrawableChecked);
        } else {
            if (mSwipeDrawableUnchecked > 0) frontView.setBackgroundResource(mSwipeDrawableUnchecked);
        }
    }*/

    /**
     * Reset the state of front view when the it's recycled by ListView
     *
     * @param mFrontView view to re-draw
     * 
     */
    protected void reloadSwipeStateInView(View frontView) {
        if (mSwipeHelper != null) {
            if(mSwipeHelper.getSwipeClosesAllItemsWhenListMoves()) {
                frontView.setTranslationX(0f);
            }
        }
    }

    /**
     * Get if item is selected
     *
     * @param position position in list
     * @return
     */
    protected boolean isChecked(int position) {
        return position < mChecked.size() && mChecked.get(position);
    }

    /**
     * Count selected
     *
     * @return
     */
    protected int getCountSelected() {
        int count = 0;
        for (int i = 0; i < mChecked.size(); i++) {
            if (mChecked.get(i)) {
                count++;
            }
        }
        //Log.d(TAG_SWIPE, "selected: " + count);
        return count;
    }

    /**
     * Get positions selected
     *
     * @return
     */
    protected List<Integer> getPositionsSelected() {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < mChecked.size(); i++) {
            if (mChecked.get(i)) {
                list.add(i);
            }
        }
        return list;
    }
    
    /**
     * Set drawable mChecked (only SWIPE_ACTION_CHOICE)
     *
     * @param mSwipeDrawableChecked drawable
     */
    /*protected void setSwipeDrawableChecked(int swipeDrawableChecked) {
        this.mSwipeDrawableChecked = swipeDrawableChecked;
    }*/
    
    /**
     * Set drawable unchecked (only SWIPE_ACTION_CHOICE)
     *
     * @param mSwipeDrawableUnchecked drawable
     */
    /*protected void setSwipeDrawableUnchecked(int swipeDrawableUnchecked) {
        this.mSwipeDrawableUnchecked = swipeDrawableUnchecked;
    }*/

    /**
     * Class that saves pending dismiss data
     */
    class PendingDismissData implements Comparable<PendingDismissData> {
        public int position;
        public View view;

        public PendingDismissData(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public int compareTo(PendingDismissData other) {
            // Sort by descending position
            return other.position - position;
        }
    }

    /**
     * Perform dismiss action
     *
     * @param dismissView     View
     * @param dismissPosition Position of list
     */
    protected void performDismiss(final View dismissView, final int dismissPosition, boolean doPendingDismiss) {
        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        final int originalHeight = dismissView.getHeight();

        // +20是为了避免用户调用closeOpenedItems等方法时冲突。因为generateRevealAnimate会执行
        //boolean aux = !mOpened.get(position);
        //和mOpened.set(position, aux);
        //而此onAnimationEnd有mOpened.set(dismissPosition, false);
        //如果dismiss动画先结束，mOpened中此position先为false，这样generateRevealAnimate最后会把mOpened又给设为true.
        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0).setDuration(0/*mDismissAnimationTime + 20*/);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        if (doPendingDismiss) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mSwipeHelper != null)
                        mSwipeHelper.onDismissAnimationStart(dismissView, dismissPosition);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mSwipeHelper != null)
                        mSwipeHelper.onDismissAnimationEnd(dismissView, dismissPosition);
                    
                    mIsDismissAnimating = false;
                    
                    --mDismissAnimationRefCount;
                    if (mDismissAnimationRefCount == 0) {
                        removePendingDismisses(originalHeight);
                    }
                }
            });
        }

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });

        ++mDismissAnimationRefCount;
        int retposition = dismissPosition - getHeaderViewsCount();// - numHeaders
        mPendingDismisses.add(new PendingDismissData(retposition, dismissView));
        mIsDismissAnimating = true;
        animator.start();
        dismissView.setAlpha(0);
        
    }

    protected void resetPendingDismisses() {
        mPendingDismisses.clear();
    }

    private void removePendingDismisses(int originalHeight) {
        // No active animations, process all pending dismisses.
        // Sort by descending position
        Collections.sort(mPendingDismisses);

        int[] dismissPositions = new int[mPendingDismisses.size()];
        for (int i = mPendingDismisses.size() - 1; i >= 0; i--) {
            dismissPositions[i] = mPendingDismisses.get(i).position;
        }
        
        onDismiss(dismissPositions);

        ViewGroup.LayoutParams lp;
        for (PendingDismissData pendingDismiss : mPendingDismisses) {
            // Reset view presentation
            if (pendingDismiss.view != null) {
                pendingDismiss.view.setAlpha(1);
                pendingDismiss.view.setTranslationX(0);
                lp = pendingDismiss.view.getLayoutParams();
                lp.height = originalHeight;
                pendingDismiss.view.setLayoutParams(lp);
            }
        }
        resetPendingDismisses();
    }
    
    /**
     * Notifies onDismiss
     *
     * @param reverseSortedPositions All dismissed positions
     */
    protected void onDismiss(int[] reverseSortedPositions) {
        if (mLeListViewListener != null) {
            mLeListViewListener.onDismiss(reverseSortedPositions);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mDragSortHelper != null)
            mDragSortHelper.dispatchDraw(canvas);
        
    }

    @SuppressLint("WrongCall")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mDragSortHelper != null)
            mDragSortHelper.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (mDragSortHelper != null)
            mDragSortHelper.layoutChildren();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mDragSortHelper != null)
            mDragSortHelper.updateScrollStarts();
    }
    
    @Override
    public void requestLayout() {
        if (mDragSortHelper != null) {
            if (!mDragSortHelper.isBlockLayoutRequests()) {
                super.requestLayout();
            }
        }
    }
    
    /**
     * As opposed to {@link android.widget.ListView#getAdapter()}, which returns
     * a heavily wrapped ListAdapter (DragSortListView wraps the
     * input ListAdapter {\emph and} ListView wraps the wrapped one).
     *
     * @return The ListAdapter set as the argument of {@link setAdapter()}
     */
    public ListAdapter getInputAdapter() {
        if (mAdapterWrapper == null) {
            return null;
        } else {
            return mAdapterWrapper.getAdapter();
        }
    }

    private class AdapterWrapper extends BaseAdapter {
        private ListAdapter mAdapter;

        public AdapterWrapper(ListAdapter adapter) {
            super();
            mAdapter = adapter;
            
            mAdapter.registerDataSetObserver(new DataSetObserver() {
                public void onChanged() {
                    notifyDataSetChanged();
                }

                public void onInvalidated() {
                    notifyDataSetInvalidated();
                }
            });
        }

        public ListAdapter getAdapter() {
            return mAdapter;
        }

        @Override
        public long getItemId(int position) {
            return mAdapter.getItemId(position);
        }

        @Override
        public Object getItem(int position) {
            return mAdapter.getItem(position);
        }

        @Override
        public int getCount() {
            return mAdapter.getCount();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return mAdapter.areAllItemsEnabled();
        }

        @Override
        public boolean isEnabled(int position) {
            return mAdapter.isEnabled(position);
        }
        
        @Override
        public int getItemViewType(int position) {
            return mAdapter.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return mAdapter.getViewTypeCount();
        }
        
        @Override
        public boolean hasStableIds() {
            return mAdapter.hasStableIds();
        }
        
        @Override
        public boolean isEmpty() {
            return mAdapter.isEmpty();
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            DragSortItemView v;
            View child;
            // Log.d(TAG_DRAG_SORT, "getView: position="+position+" convertView="+convertView);
            if (convertView != null) {
                v = (DragSortItemView) convertView;
                View oldChild = v.getChildAt(0);

                child = mAdapter.getView(position, oldChild, LeListView.this);
                if (child != oldChild) {
                    // shouldn't get here if user is reusing convertViews properly
                    if (oldChild != null) {
                        v.removeViewAt(0);
                    }
                    v.addView(child);
                }
            } else {
                child = mAdapter.getView(position, null, LeListView.this);
                if (child instanceof Checkable) {
                    v = new DragSortItemViewCheckable(getContext());
                } else {
                    v = new DragSortItemView(getContext());
                }
                v.setLayoutParams(new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                v.addView(child);
            }

            // Set the correct item height given drag state; passed
            // View needs to be measured if measurement is required.
            if (mDragSortHelper != null)
                mDragSortHelper.adjustItem(position + getHeaderViewsCount(), v, true);

            return v;
        }
    }

    // callBack start
    @Override
    public View getFrontView(int position) {
        int numHeaders = getHeaderViewsCount();
        int numFooters = getFooterViewsCount();
        int countOfAll = getAdapter().getCount();
        
        if (position < numHeaders) {
            return null;
        }

        if (position < countOfAll - numFooters) {
            View childView = getChildAt(position - getFirstVisiblePosition());
            return childView.findViewById(mSwipeFrontView);
        }

        return null;
    }

    @Override
    public View getBackView(int position) {
        int numHeaders = getHeaderViewsCount();
        int numFooters = getFooterViewsCount();
        int countOfAll = getAdapter().getCount();
        
        if (position < numHeaders) {
            return null;
        }

        if (position < countOfAll - numFooters) {
            View childView = getChildAt(position - getFirstVisiblePosition());
            return childView.findViewById(mSwipeBackView);
        }

        return null;
    }
    
    @Override
    public int getSwipeViewWidth() {
        return getWidth();
    }
    
    @Override
    public void onDismissedSwipe(View view, int position) {
        performDismiss(view, position, true);
    }
    
    @Override
    public int pointToPositionSwipe(int x, int y) {
        return super.pointToPosition(x, y);
    }
    
    @Override
    public int getFirstVisiblePositionSwipe() {
        return super.getFirstVisiblePosition();
    }
    
    @Override
    public int getLastVisiblePositionSwipe() {
        return super.getLastVisiblePosition();
    }
    
    @Override
    public boolean isDismissAnimating() {
        return mIsDismissAnimating;
    }
    
    @Override
    public ListAdapter getAdapterSwipe() {
        return getAdapter();
    }

    @Override
    public int getHeaderViewsCountSwipe() {
        return getHeaderViewsCount();
    }

    @Override
    public int getFooterViewsCountSwipe() {
        return getFooterViewsCount();
    }

    @Override
    public boolean superOnTouch(MotionEvent ev) {
        return onTouchEventSuper(ev);
    }
    // callBack end

}
