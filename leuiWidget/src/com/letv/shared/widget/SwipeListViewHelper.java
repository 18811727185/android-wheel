package com.letv.shared.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.letv.shared.R;

public class SwipeListViewHelper extends BaseSwipeHelper implements OnScrollListener {
    
    public static final String TAG = "SWPIE_LISTVIEW_HELPER";
    
    /** Internal listener for common swipe events */
    private SwipeListViewListener mSwipeListViewListener;
    /** Internal listener for swipe-switch events */
    private SwipeListViewSwitchListener mSwipeListViewSwitchListener;
    
    // the position within the adapter's data set of the view, and which is touched.
    private int mDownPosition = ListView.INVALID_POSITION;
    
    private List<Boolean> mOpeneds = new ArrayList<Boolean>();
    private List<Boolean> mOpenedRights = new ArrayList<Boolean>();
    
    private boolean mSwipeClosesAllItemsWhenListMoves = true;
    
    private boolean mListViewMoving = false;
    private boolean mRemoveFirstTouchWhenFling = false;
    
    private boolean isFirstItem = false;
    private boolean isLastItem = false;
    
    private Callback mCallback;

    public SwipeListViewHelper(Context context, LeListView listView, Callback callback) {
        this(context, null, listView, callback);
    }
    
    public SwipeListViewHelper(Context context, TypedArray typedArray, LeListView listView, Callback callback) {
        super(context, typedArray, null);
        init(context, typedArray, listView, callback);
    }
    
    private void init(Context context, TypedArray typedArray, LeListView listView, Callback callback) {
        if (typedArray != null) {
            mSwipeClosesAllItemsWhenListMoves = typedArray.getBoolean(R.styleable.LeListView_leSwipeCloseAllItemsWhenMoveList, true);
        }
        
        mCallback = callback;
        //mCallback.setDismissAnimationListener(this);
        
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        setPaused(scrollState == SCROLL_STATE_TOUCH_SCROLL);
        if (mSwipeClosesAllItemsWhenListMoves
                && scrollState == SCROLL_STATE_TOUCH_SCROLL && !isAnimating()) {
            closeOpenedItems();
        }
        if (scrollState == SCROLL_STATE_TOUCH_SCROLL || scrollState == SCROLL_STATE_FLING) {
            mListViewMoving = true;
            setPaused(true);
        }
        if (scrollState != SCROLL_STATE_FLING && scrollState != SCROLL_STATE_TOUCH_SCROLL) {
            mListViewMoving = false;
            //mDownPosition = ListView.INVALID_POSITION;
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    setPaused(false);
                }
            }, 500);
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if (isFirstItem) {
            boolean onSecondItemList = firstVisibleItem == 1;
            if (onSecondItemList) {
                isFirstItem = false;
            }
        } else {
            boolean onFirstItemList = firstVisibleItem == 0;
            if (onFirstItemList) {
                isFirstItem = true;
                onFirstListItem();
            }
        }
        if (isLastItem) {
            boolean onBeforeLastItemList = firstVisibleItem + visibleItemCount == totalItemCount - 1;
            if (onBeforeLastItemList) {
                isLastItem = false;
            }
        } else {
            boolean onLastItemList = firstVisibleItem + visibleItemCount >= totalItemCount;
            if (onLastItemList) {
                isLastItem = true;
                onLastListItem();
            }
        }
    }

    /**
     * @return true if the list is in motion
     */
    public boolean isListViewMoving() {
        return mListViewMoving;
    }
    
    public boolean isAnimating() {
        return super.isAnimating() || mCallback.isDismissAnimating();
    }
    
    /**
     * Set if all item mOpened will be close when the user move ListView
     *
     * @param mSwipeClosesAllItemsWhenListMoves
     */
    public void setSwipeClosesAllItemsWhenListMoves(boolean swipeClosesAllItemsWhenListMoves) {
        this.mSwipeClosesAllItemsWhenListMoves = swipeClosesAllItemsWhenListMoves;
    }
    
    public boolean getSwipeClosesAllItemsWhenListMoves(){
        return mSwipeClosesAllItemsWhenListMoves;
    }

    /**
     * one item or more items is Opened?
     * 
     * @return true: one item or more items is Opened, otherwise false.
     **/
    private boolean isItemsOpened () {
        for (boolean opened : mOpeneds) {
            if (opened) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Adds new items when adapter is modified
     */
    public void resetItems() {
        final ListAdapter adapter = mCallback.getAdapterSwipe();
        if (adapter != null) {
            int count = adapter.getCount();
            for (int i = mOpeneds.size(); i <= count; i++) {
                mOpeneds.add(false);
                mOpenedRights.add(false);
            }
        }
    }
    
    /**
     * Close all opened items
     */
    public void closeOpenedItems() {
        if (mOpeneds != null) {
            int start = mCallback.getFirstVisiblePositionSwipe();
            int end = mCallback.getLastVisiblePositionSwipe();
            for (int i = start; i <= end; i++) {
                if (!mOpeneds.isEmpty() && mOpeneds.get(i)) {
                    closeAnimateInternal(i);
                }
            }
        }
    }
    
    /**
     * Close the opened item
     */
    public void closeTheOpenedItem(int position) {
        if (mOpeneds != null) {
                if (!mOpeneds.isEmpty() && mOpeneds.get(position)) {
                    closeNoAnimateInternal(position);
                }
        }
    }

    /**
     * Sets the Listener
     *
     * @param mSwipeListViewListener Listener
     */
    public void setSwipeListViewListener(SwipeListViewListener swipeListViewListener) {
        this.mSwipeListViewListener = swipeListViewListener;
    }
    
    /**
     * Get the Listener
     */
    public SwipeListViewListener getSwipeListViewListener() {
        return mSwipeListViewListener;
    }
    
    /**
     * Sets the switch Listener
     *
     * @param swipeListViewSwitchListener Listener
     */
    public void setSwipeListViewSwitchListener(SwipeListViewSwitchListener swipeListViewSwitchListener) {
        this.mSwipeListViewSwitchListener = swipeListViewSwitchListener;
    }
    
    /**
     * Get the switch Listener
     */
    public SwipeListViewSwitchListener getSwipeListViewSwitchListener() {
        return mSwipeListViewSwitchListener;
    }
    
    /**
     * Open item
     *
     * @param position Position of list
     */
    public void openAnimate(int position) {
        int numHeaders = mCallback.getHeaderViewsCountSwipe();
        position += numHeaders;// position is external, so +numHeaders
        
        mDownPosition = position;
        mOpened = mOpeneds.get(position);
        mFrontView = mCallback.getFrontView(position);
        super.openAnimate();
    }
    
    /** @deprecated */
    @Override
    public void openAnimate(){}

    /**
     * Close item
     *
     * @param position Position of list
     */
    public void closeAnimate(int position) {
        int numHeaders = mCallback.getHeaderViewsCountSwipe();
        position += numHeaders;// position is external, so +numHeaders
        
        mDownPosition = position;
        mOpened = mOpeneds.get(position);
        mFrontView = mCallback.getFrontView(position);
        super.closeAnimate();
    }
    
    private void closeAnimateInternal(int position) {
        mDownPosition = position;
        mOpened = mOpeneds.get(position);
        mFrontView = mCallback.getFrontView(position);
        super.closeAnimate();
    }
    
    private void closeNoAnimateInternal(int position) {
        mDownPosition = position;
        mOpened = mOpeneds.get(position);
        mFrontView = mCallback.getFrontView(position);
        super.closeNoAnimate();
    }
    
    /** @deprecated */
    public void closeAnimate(){
        closeAnimateInternal(mDownPosition);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int numHeaders = mCallback.getHeaderViewsCountSwipe();
        final int numFooters = mCallback.getFooterViewsCountSwipe();
        final int countOfAll = mCallback.getAdapterSwipe().getCount();
        final int position = mCallback.pointToPositionSwipe((int) ev.getX(), (int) ev.getY());
        final int action = ev.getActionMasked();
        
        // When at the edges of ListView and user moves touchPoint, touchPoint is move, but ListView isn't Scroll.
        // At the time, user move touchPoint to open frontView, the touchPoint's position isn't mDownPosition.
        // We can see a frontView open, but it isn't the frontView touched by user. 
        if (mListViewMoving) {
            return false;
        }
        
        if (action == MotionEvent.ACTION_DOWN) {
            if (mPaused || isAnimating()) {
                return true;
            }

            // when user touch one item of headers or footers, finish the
            // function. and reset member variables.
            if (position < numHeaders || position >= countOfAll - numFooters) {
                boolean intercept = false;
                if (isItemsOpened()) {
                    intercept = true;
                }

                mDownPosition = position;
                mOpened = false;
                mOpenedRight = false;
                mFrontView = null;
                mBackView = null;
                return intercept;
            }

            if (!mOpeneds.get(position) && isItemsOpened()) {
                closeOpenedItems();
                return true;
            }

            final ListAdapter adapter = mCallback.getAdapterSwipe();
            // Don't allow mSwiping if this is on the header or footer
            // or IGNORE_ITEM_VIEW_TYPE or enabled is false on the adapter
            if (adapter.isEnabled(position) && adapter.getItemViewType(position) >= 0) {
                mDownPosition = position;
                mOpened = mOpeneds.get(position);
                mOpenedRight = mOpenedRights.get(position);
            }
        }
        
        return super.onInterceptTouchEvent(ev);
    }
    
    @SuppressLint("Recycle")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        final int x = (int) ev.getX();
        final int y = (int) ev.getY();
        
        if (!isSwipeTouchEnabled()) {
            return true;
        }
        
        // When ListView is Fling, user touch-down & touch-move & touch-up should invalid.
        // fix bug start
        if (mListViewMoving && action == MotionEvent.ACTION_DOWN) {
            mRemoveFirstTouchWhenFling = true;
            return false;
        }
        if (mRemoveFirstTouchWhenFling && action == MotionEvent.ACTION_UP) {
            mRemoveFirstTouchWhenFling = false;
            return false;
        }
        if (mRemoveFirstTouchWhenFling && action == MotionEvent.ACTION_MOVE) {
            return false;
        }
        // fix bug end
        
        // When at the edges of ListView and user moves touchPoint, touchPoint is move, but ListView isn't Scroll.
        // At the time, user move touchPoint to open frontView, the touchPoint's position isn't mDownPosition.
        // We can see a frontView open, but it isn't the frontView touched by user. 
        if (mListViewMoving) {
            return false;
        }

        if (mViewWidth < 2) {
            mViewWidth = mCallback.getSwipeViewWidth();
        }
        
        int numHeaders = mCallback.getHeaderViewsCountSwipe();
        int numFooters = mCallback.getFooterViewsCountSwipe();
        int countOfAll = mCallback.getAdapterSwipe().getCount();
        final int touchedPosition = mCallback.pointToPositionSwipe(x, y);
        
        // When user touch one item of headers or footers. 
        // touchedPosition != ListView.INVALID_POSITION, To solve,
        // when touch outside of ListView, and can continue to handle frontView.
        if (touchedPosition != ListView.INVALID_POSITION 
                && (touchedPosition < numHeaders || touchedPosition >= countOfAll - numFooters)) {
            if (action == MotionEvent.ACTION_DOWN) {
                closeOpenedItems();
            }
            // If any item is open, return true, without touch effect.
            //return isItemsOpened() ? true : false;
        } else if (touchedPosition == ListView.INVALID_POSITION && mFrontView == null) {
            // When touch outside of ListView, avoid NullPointerException.
            return true;
        }
        
        // see BaseSwipeHelper#onTouchEvent(ev), it's different from this:
        // this return false, super.ontouchEvent(ev) return true.
        if (action == MotionEvent.ACTION_DOWN) {
            if (mPaused || isAnimating()){
                return true;
            }
            
            if (!mOpened && closeOpenedItem((int) x)) {
                return true;// if close opened item, finish the function.for ListView
            }
            
            return false;
        }

        return super.onTouchEvent(ev);
    }
    
    public void onDismissAnimationStart(View dismissView, int dismissPosition) {
        setPaused(true);
    }

    public void onDismissAnimationEnd(View dismissView, int dismissPosition) {
        if (mFrontView != null) {
            mFrontView.setTranslationX(0);
        }
        resetCell();
        
        new Handler().postDelayed(new Runnable() {
            public void run() {
                setPaused(false);
            }
        }, 500);
        
        mOpeneds.set(dismissPosition, false);
    }
    
    /**
     * Create animation
     *
     * @param view      affected view
     * @param swap      If state should change. If "false" returns to the original position
     * @param swapRight If swap is true, this parameter tells if move is to the right or left
     * @param position  Position of list
     */
    @Override
    protected void generateAnimate(final View view, final boolean swap, final boolean swapRight) {
        mOpened = mOpeneds.get(mDownPosition);
        mOpenedRight = mOpenedRights.get(mDownPosition);
        
        super.generateAnimate(view, swap, swapRight);
    }

    @Override
    protected void onSwipeDismissed(boolean swap, View view) {
        if (swap) {
            closeOpenedItems();
            mCallback.onDismissedSwipe(view, mDownPosition);
        }
        super.onSwipeDismissed(swap, view);
    }
    
    /**
     * Notifies swipeDismiss animation is end
     */
    protected void onSwipeRevealed(boolean swap, boolean swapRight) {
        if (swap) {
            mOpeneds.set(mDownPosition, mOpened);
            if (mOpened) {
                mOpenedRights.set(mDownPosition, swapRight);
            }
        }
        super.onSwipeRevealed(swap, swapRight);
    }
    
    /** 
     * Close open item.
     * If user touchs outside(no touch menu), ListView will close the open item.
     * 
     * @param x touchX
     * @return the item is open, and close it.
     */
    protected boolean closeOpenedItem(int x) {
       if (mOpeneds == null || !isItemsOpened()) {
           return false;
       }
       
       final boolean opened = mOpeneds.get(mDownPosition);

       if (!opened) {
           closeOpenedItems();
           return true;
       }
       
       mOpened = opened;
       mOpenedRight = mOpenedRights.get(mDownPosition);
       mFrontView = mCallback.getFrontView(mDownPosition);
       
       return super.closeOpenedItem(x);
    }
    
    /**
     * Set backView to not click, frontView stay its original state.
     * Because frontView wrapped in backView, so when frontView covered backView,
     * set the backView's childView to not click.
     * 
     * @param forceUnClickable Force backView's childView can not click 
     * @param x touchX
     * @param y touchY
     * @return whether Item is open. true open，false closed.
     */
    protected boolean setBackViewClickable(boolean forceUnClickable, int x, int y) {
        final int position = mCallback.pointToPositionSwipe(x, y);
        ViewGroup backView = (ViewGroup) mCallback.getBackView(position);
        
        if (position < 0 || backView == null) {
            return false;
        }
        
        // Don't allow mSwiping if this is on the header or footer
        // or IGNORE_ITEM_VIEW_TYPE or enabled is false on the adapter
        if (mCallback.getAdapterSwipe().isEnabled(position) 
                && mCallback.getAdapterSwipe().getItemViewType(position) >= 0) {
            boolean opened = false;
            final int openedsSize = mOpeneds.size();
            if (position >= 0 && position < openedsSize) {
                opened = mOpeneds.get(position);
            }
            
            for (int j = 0; j < backView.getChildCount(); j++) {
                View backChildView = backView.getChildAt(j);
                backChildView.setClickable(forceUnClickable ? false : opened);
                backChildView.setLongClickable(forceUnClickable ? false : opened);
                backChildView.setFocusable(false);
            }
        }

        return mOpened;
    }
    
    protected void setBackViewChildFocusable(int x, int y) {
        final int position = mCallback.pointToPositionSwipe(x, y);
        ViewGroup backView = (ViewGroup) mCallback.getBackView(position);
        
        if (position < 0 || backView == null) {
            return;
        }
        
        // Don't allow mSwiping if this is on the header or footer
        // or IGNORE_ITEM_VIEW_TYPE or enabled is false on the adapter
        if (mCallback.getAdapterSwipe().isEnabled(position) 
                && mCallback.getAdapterSwipe().getItemViewType(position) >= 0) {
            for (int j = 0; j < backView.getChildCount(); j++) {
                View backChildView = backView.getChildAt(j);
                backChildView.setFocusable(false);
            }
        }
        
    }
    
    @Override
    protected View getFrontView() {
        return mCallback.getFrontView(mDownPosition);
    }
    
    @Override
    protected View getBackView() {
        return mCallback.getBackView(mDownPosition);
    }
    
    protected void resetCell() {
        if (mDownPosition != ListView.INVALID_POSITION && !isAnimating()) {
            if (mOpeneds != null) {
                mOpened = mOpeneds.get(mDownPosition);
                super.resetCell();
            }
            mDownPosition = ListView.INVALID_POSITION;
        }
    }
    
    /**
     * Start open item
     *
     * @param action   current action
     * @param right    to right
     */
    protected void onStartOpen(int action, boolean right) {
        // retPosition is external, so -numHeaders
        int retPosition = mDownPosition - mCallback.getHeaderViewsCountSwipe();
        
        if (mSwipeListViewListener != null && retPosition != ListView.INVALID_POSITION) {
            mSwipeListViewListener.onStartOpen(retPosition, action, right);
        }
    }

    /**
     * Start close item
     *
     * @param right
     */
    protected void onStartClose(boolean right) {
        // retPosition is external, so -numHeaders
        int retPosition = mDownPosition - mCallback.getHeaderViewsCountSwipe();
        
        if (mSwipeListViewListener != null && retPosition != ListView.INVALID_POSITION) {
            mSwipeListViewListener.onStartClose(retPosition, right);
        }
    }

    /**
     * Notifies onClickFrontView
     */
    /*protected void onClickFrontView() {
        // retPosition is external, so -numHeaders
        int retPosition = mDownPosition - mCallback.getHeaderViewsCountSwipe();
        
        if (mSwipeListViewListener != null && retPosition != ListView.INVALID_POSITION) {
            mSwipeListViewListener.onClickFrontView(retPosition);
        }
    }*/

    /**
     * Notifies onClickBackView
     */
    /*protected void onClickBackView() {
        // retPosition is external, so -numHeaders
        int retPosition = mDownPosition - mCallback.getHeaderViewsCountSwipe();
        
        if (mSwipeListViewListener != null && retPosition != ListView.INVALID_POSITION) {
            mSwipeListViewListener.onClickBackView(retPosition);
        }
    }*/

    /**
     * Notifies onOpened
     *
     * @param toRight  If should be opened toward the right
     */
    protected void onOpened(boolean toRight) {
        mOpeneds.set(mDownPosition, mOpened);
        mOpenedRights.set(mDownPosition, toRight);
        // retPosition is external, so -numHeaders
        int retPosition = mDownPosition - mCallback.getHeaderViewsCountSwipe();
        
        if (mSwipeListViewListener != null && retPosition != ListView.INVALID_POSITION) {
            mSwipeListViewListener.onOpened(retPosition, toRight);
        }
    }

    /**
     * Notifies onClosed
     *
     * @param fromRight If open from right
     */
    protected void onClosed(boolean fromRight) {
        mOpeneds.set(mDownPosition, mOpened);
        mOpenedRights.set(mDownPosition, fromRight);
        // retPosition is external, so -numHeaders
        int retPosition = mDownPosition - mCallback.getHeaderViewsCountSwipe();
        
        if (mSwipeListViewListener != null && retPosition != ListView.INVALID_POSITION) {
            mSwipeListViewListener.onClosed(retPosition, fromRight);
        }
    }

    /**
     * User is in first item of list
     */
    protected void onFirstListItem() {
        if (mSwipeListViewListener != null) {
            mSwipeListViewListener.onFirstListItem();
        }
    }

    /**
     * User is in last item of list
     */
    protected void onLastListItem() {
        if (mSwipeListViewListener != null) {
            mSwipeListViewListener.onLastListItem();
        }
    }

    /**
     * Notifies onListChanged
     */
    protected void onListChanged() {
        if (mSwipeListViewListener != null) {
            mSwipeListViewListener.onListChanged();
        }
    }

    /**
     * Notifies onMove
     *
     * @param x        Current position
     */
    protected void onMove(float x) {
        if (mSwipeListViewListener != null && mDownPosition != ListView.INVALID_POSITION) {
            mSwipeListViewListener.onMove(mDownPosition, x);
        }
    }

    /**
     * Used when user want to change swipe list mode on some rows. Return SWIPE_MODE_DEFAULT
     * if you don't want to change swipe list mode
     * 
     * @return type
     */
    protected int onChangeSwipeMode() {
        if (mSwipeListViewListener != null && mDownPosition != ListView.INVALID_POSITION) {
            return mSwipeListViewListener.onChangeSwipeMode(mDownPosition);
        }
        return SWIPE_MODE_DEFAULT;
    }
    
    /**
     * Notifies swipe cross switch-line.
     */
    protected void onSwipeSwitching(boolean isChanged) {
        if (mDownPosition != ListView.INVALID_POSITION && mSwipeListViewSwitchListener != null) {
            mSwipeListViewSwitchListener.onSwitching(mDownPosition, isChanged);
        }
        super.onSwipeSwitching(isChanged);
    }
    
    /**
     * Notifies swipe animation is end
     */
    protected void onSwipeSwitched(boolean isSwitched) {
        if (mDownPosition != ListView.INVALID_POSITION && mSwipeListViewSwitchListener != null) {
            mSwipeListViewSwitchListener.onSwitched(mDownPosition, isSwitched);
        }
        super.onSwipeSwitched(isSwitched);
    }
    
    @Override
    protected boolean superOnTouchEvent(MotionEvent ev) {
        return mCallback.superOnTouch(ev);
    }
    
    public interface Callback extends BaseCallback {
        
        View getFrontView(int position);
        
        View getBackView(int position);
        
        void onDismissedSwipe(View view, int position);
        
        int pointToPositionSwipe(int x, int y);
        
        int getFirstVisiblePositionSwipe();
        
        int getLastVisiblePositionSwipe();
        
        int getHeaderViewsCountSwipe();
        
        int getFooterViewsCountSwipe();
        
        boolean isDismissAnimating();
        
        ListAdapter getAdapterSwipe();
 
    }

}
