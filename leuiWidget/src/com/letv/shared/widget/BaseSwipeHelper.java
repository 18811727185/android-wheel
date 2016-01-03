package com.letv.shared.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import com.letv.shared.R;

public class BaseSwipeHelper {
    
    public static final String TAG = "SWPIE_HELPER";
    
    /** Used when user want change swipe list mode on some rows */
    public final static int SWIPE_MODE_DEFAULT = -1;

    /** Disables all swipes */
    public final static int SWIPE_MODE_NONE = 0;

    /** Enables both left and right swipe */
    public final static int SWIPE_MODE_BOTH = 1;

    /** Enables right swipe */
    public final static int SWIPE_MODE_RIGHT = 2;

    /** Enables left swipe */
    public final static int SWIPE_MODE_LEFT = 3;

    /** Binds the swipe gesture to reveal a view behind the row (Drawer style) */
    public final static int SWIPE_ACTION_REVEAL = 0;

    /** Dismisses the cell when swiped over */
    public final static int SWIPE_ACTION_DISMISS = 1;

    /** Like switch when swiped to the switch line. */
    public final static int SWIPE_ACTION_SWITCH = 2;

    /** No action when swiped */
    public final static int SWIPE_ACTION_NONE = 3;
    
    public final static int VIEW_SWIPED_FIXED = 0;
    public final static int VIEW_SWIPED_LEFT = 1;
    public final static int VIEW_SWIPED_RIGHT = 2;
    
    public final static int EVENT_SWIPING_FIXED = 0;
    public final static int EVENT_SWIPING_LEFT = 1;
    public final static int EVENT_SWIPING_RIGHT = 2;
    
    public static final int DEFAULT_ANIM_TIME = 280;
    //private static final int SCROLL_BACK_OFFSET = 30;
    private static final float VELOCITY_FACTOR = 1.5f;
    
    protected static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    protected float mLeftOffset = 0;
    protected float mRightOffset = 0;
    protected float mSwipeLeftSwitchLine = 0;
    protected float mSwipeRightSwitchLine = 0;
    protected boolean mIsSwitch;

    // Fixed properties
    protected int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero

    protected int mSwipeMode = SWIPE_MODE_LEFT;
    protected int mSwipeCurrentAction = SWIPE_ACTION_NONE;
    protected int mSwipeActionLeft = SWIPE_ACTION_NONE;
    protected int mSwipeActionRight = SWIPE_ACTION_NONE;

    protected View mFrontView;
    protected View mBackView;
    
    protected boolean mOpened;
    protected boolean mOpenedRight;
    protected boolean mPaused;
    
    // Cached ViewConfiguration and system-wide constant values
    private int mPagingSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private long mAnimationTime = DEFAULT_ANIM_TIME;
    private float mEventDownX;
    
    private float mEventStartX;
    private float mEventPreEventX;
    private float mStartFrontViewX;
    private float mStartBackViewX;
    
    // when swiping, frontView can? over leftOffset/rightOffset.
    private boolean mIsOverOffsetEnabled = true;
    
    // If isTouchOpened == true, don't perform our onInterceptTouchEvent and onTouchEvent.
    //boolean mIsTouchOpened = false;
    // when swiping，Whether can slide to left or right.Stop ACTION_MOVE.
    private boolean mIsSwipeEnable = true;
    private boolean mIsAnimating; 
    private boolean mSwiping;
    // If frontView is left, the value is true.
    private boolean mStartToLeftLock;
    // If frontView is right, the value is true.
    private boolean mStartToRightLock;
    
    // The direction of finger is swiping.
    private int mEventSwipingDirection = EVENT_SWIPING_FIXED;
    // The direction of view is swiped.
    private int mViewSwipedDirection = VIEW_SWIPED_FIXED;
    
    private VelocityTracker mVelocityTracker;
    
    private SwipeListener mSwipeListener;
    protected SwipeSwitchListener mSwipeSwitchListener;
    
    private Callback mCallback;

    public BaseSwipeHelper(){
    }

    public BaseSwipeHelper(Context context, TypedArray typedArray, Callback callback) {
        init(context, typedArray, callback);
    }
    
    private void init(Context context, TypedArray typedArray, Callback callback) {
        if (typedArray != null) {
            mSwipeMode = typedArray.getInt(R.styleable.LeListView_leSwipeMode, SWIPE_MODE_LEFT);
            mSwipeActionLeft = typedArray.getInt(R.styleable.LeListView_leSwipeActionLeft, SWIPE_ACTION_NONE);
            mSwipeActionRight = typedArray.getInt(R.styleable.LeListView_leSwipeActionRight, SWIPE_ACTION_NONE);
            
            //[+LEUI] [REQ][LEUI-1067] [wangziming] modify leSwipeOffsetLeft&leSwipeOffsetLeft meaning.
            //mLeftOffset = typedArray.getDimension(R.styleable.LeListView_leSwipeOffsetLeft, 0);
            //mRightOffset = typedArray.getDimension(R.styleable.LeListView_leSwipeOffsetRight, 0);
            float tempLeftOffset = typedArray.getDimension(R.styleable.LeListView_leSwipeOffsetLeft, 0);
            float tempRightOffset = typedArray.getDimension(R.styleable.LeListView_leSwipeOffsetRight, 0);
            
            DisplayMetrics dm = new DisplayMetrics();
            dm = context.getResources().getDisplayMetrics();
            int width = dm.widthPixels;
            mLeftOffset = (float) width - tempLeftOffset;
            mRightOffset = (float) width - tempRightOffset;
            //[-LEUI]
            
            mSwipeLeftSwitchLine = typedArray.getDimension(R.styleable.LeListView_leSwipeLeftSwitchLine, 0);
            mSwipeRightSwitchLine = typedArray.getDimension(R.styleable.LeListView_leSwipeRightSwitchLine, 0);
            mAnimationTime = typedArray.getInteger(R.styleable.LeListView_leSwipeAnimationTime, DEFAULT_ANIM_TIME);
            mIsOverOffsetEnabled = typedArray.getBoolean(R.styleable.LeListView_leSwipeOverOffsetEnabled, true);
        }
        
        mCallback = callback;
        
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mPagingSlop = configuration.getScaledPagingTouchSlop();
        mMinFlingVelocity = configuration .getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    /**
     * Open ListView's item
     */
    public void openAnimate() {
        if (!mOpened) {
            generateRevealAnimate(mFrontView, true, false);
        }
    }

    /**
     * Close ListView's item
     *
     * @param position Position that you want open
     */
    public void closeAnimate() {
        if (mOpened) {
            generateRevealAnimate(mFrontView, true, false);
        }
    }
    
    /**
     * Close ListView's item
     *
     * @param position Position that you want open
     */
    public void closeNoAnimate() {
        if (mOpened && mFrontView != null) {
            onSwipeRevealed(true, false);
        }
    }
    
    /**
     * Sets the Listener
     *
     * @param mSwipeListViewListener Listener
     */
    public void setSwipeListener(SwipeListener swipeListener) {
        this.mSwipeListener = swipeListener;
    }
    
    /**
     * Get the Listener
     */
    public SwipeListener getSwipeListener() {
        return mSwipeListener;
    }
    
    /**
     * Sets the SwipeSwitchListener
     *
     * @param mSwipeListViewListener Listener
     */
    public void setSwipeSwitchListener(SwipeSwitchListener swipeListener) {
        this.mSwipeSwitchListener = swipeListener;
    }
    
    /**
     * Get the SwipeSwitchListener
     */
    public SwipeSwitchListener getSwipeSwitchListener() {
        return mSwipeSwitchListener;
    }
    
    /**
     * Set swipe mode
     *
     * @param swipeMode
     */
    public void setSwipeMode(int swipeMode) {
        this.mSwipeMode = swipeMode;
    }

    /**
     * Return action on left
     *
     * @return Action
     */
    public int getSwipeActionLeft() {
        return mSwipeActionLeft;
    }

    /**
     * Set action on left
     *
     * @param swipeActionLeft Action
     */
    public void setSwipeActionLeft(int swipeActionLeft) {
        this.mSwipeActionLeft = swipeActionLeft;
    }

    /**
     * Return action on right
     *
     * @return Action
     */
    public int getSwipeActionRight() {
        return mSwipeActionRight;
    }

    /**
     * Set action on right
     *
     * @param swipeActionRight Action
     */
    public void setSwipeActionRight(int swipeActionRight) {
        this.mSwipeActionRight = swipeActionRight;
    }

    /**
     * Set enabled
     *
     * @param enabled
     */
    public void setPaused(boolean paused) {
        mPaused = paused;
    }
    
    /**
     * Set offset on right
     *
     * @param offsetRight Offset
     */
    public void setOffsetRight(float offsetRight) {
        this.mRightOffset = offsetRight;
    }

    /**
     * Set offset on left
     *
     * @param offsetLeft Offset
     */
    public void setOffsetLeft(float offsetLeft) {
        this.mLeftOffset = offsetLeft;
    }
    
    /**
     * Set switch-line, when swipe to right.
     *
     * @param  swipeRightSwitchLine
     */
    public void setSwipeRightSwitchLine(float swipeRightSwitchLine) {
        this.mSwipeRightSwitchLine = swipeRightSwitchLine;
    }

    /**
     * Set switch-line, when swipe to left.
     *
     * @param swipeLeftSwitchLine
     */
    public void setSwipeLeftSwitchLine(float swipeLeftSwitchLine) {
        this.mSwipeLeftSwitchLine = swipeLeftSwitchLine;
    }

    public void setOverOffsetEnabled(boolean overOffsetEnabled) {
        this.mIsOverOffsetEnabled = overOffsetEnabled;
    }

    /**
     * Set swipe enabled
     *
     * @param enabled
     */
    public void setSwipeEnabled(boolean enabled) {
        mIsSwipeEnable = enabled;
    }
    
    public boolean isAnimating() {
        return mIsAnimating;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        final float x = ev.getX();
        final float y =  ev.getY();
        boolean intercept = false;
        
        if (action == MotionEvent.ACTION_DOWN) {
            // Set backView to not click, frontView stay its original state
            setBackViewClickable(false, (int) x, (int) y);
        }
        
        if (isSwipeTouchEnabled()) {
            switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mPaused || isAnimating()) {
                    return true;
                }
                
                final View fView = getFrontView();
                if (fView != null) {
                    setFrontView(fView);
                }

                final View bView = getBackView();
                if (bView !=null) {
                    setBackView(bView);
                }
                
                mSwipeCurrentAction = SWIPE_ACTION_NONE;
                mEventDownX = ev.getRawX();
                mEventStartX = mEventDownX;
                mEventPreEventX = mEventDownX;
                mVelocityTracker = VelocityTracker.obtain();
                mStartFrontViewX = fView.getTranslationX();

                /**
                 * cancel touch effect(selector). @see AbsListView#onTouchUp(MotionEvent) 
                 * <code>if (inList && !child.hasFocusable())</code>
                 */
                setBackViewChildFocusable((int) x, (int) y, false);
                mBackView.setFocusable(false);
                if (mOpened) {
                    setBackViewChildFocusable((int) x, (int) y, true);
                    mBackView.setFocusable(true);
                }
                
                intercept = false;
                break;
            case MotionEvent.ACTION_MOVE:
                // Fix bug for Dialer App: When User touch-down and touch-move on the view, 
                // touch-move return true, and the touch-event is intercepted, so the view's onClick is invalid.
                if (Math.abs(x - mEventStartX) >= mPagingSlop) {
                    intercept = true;
                } else {
                    intercept = false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetEventValue();// reset
                
                if (mOpened && closeOpenedItem((int) x)) {
                    return false;// if close opened item, finish the function.
                }
                intercept = false;
                break;
            default:
                break;
            }
        }
        return intercept;
    }
    
    @SuppressLint("Recycle")
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        final int x = (int) ev.getX();
        float deltaX = 0;
        float velocityX;
        float velocityY;
        
        if (!isSwipeTouchEnabled()) {
            return false;
        }
        
        if (mViewWidth < 2 && mCallback != null) {
            mViewWidth = mCallback.getSwipeViewWidth();
        }
        
        float eventFrontViewX = 0;
        if (mFrontView != null) {
            eventFrontViewX = mFrontView.getTranslationX();
        }
        if (eventFrontViewX > 0) {
            mViewSwipedDirection = VIEW_SWIPED_RIGHT;
        } else if (eventFrontViewX < 0) {
            mViewSwipedDirection = VIEW_SWIPED_LEFT;
        } else {
            mViewSwipedDirection = VIEW_SWIPED_FIXED;
        }
        
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            if (mPaused || isAnimating()) {
                return true;
            }
            
            if (!mOpened && closeOpenedItem((int) x)) {
                return true;// if close opened item, finish the function.for ListView
            }
            
            return true;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP: 
            if (mVelocityTracker == null || isAnimating() || mFrontView == null) {
                break;
            }
            
            // Close the opened item.
            boolean isToCloseOpenedItem = false;
            // In the ACTION_UP, if user don't move frontView, also make sure item to close.
            if (mSwipeActionLeft == SWIPE_ACTION_REVEAL && Math.abs(eventFrontViewX + mViewWidth - mLeftOffset) < mPagingSlop) {
                isToCloseOpenedItem = true;
            } else if (mSwipeActionRight == SWIPE_ACTION_REVEAL && Math.abs(mViewWidth - eventFrontViewX - mRightOffset) < mPagingSlop) {
                isToCloseOpenedItem = true;
            }
            
            if (mOpened && isToCloseOpenedItem) {
                closeAnimate();
                return false;// if close opened item, finish the function.
            }
            
            if (!mSwiping) {
                break;
            }
            
            deltaX = ev.getRawX() - mEventStartX;
            
            mVelocityTracker.addMovement(ev);
            mVelocityTracker.computeCurrentVelocity(1000);
            velocityX = Math.abs(mVelocityTracker.getXVelocity());
            velocityY = Math.abs(mVelocityTracker.getYVelocity());
            
            if (!mOpened) {
                if (mSwipeMode == SWIPE_MODE_LEFT && mVelocityTracker.getXVelocity() > 0) {
                    velocityX = 0;
                }
                if (mSwipeMode == SWIPE_MODE_RIGHT && mVelocityTracker.getXVelocity() < 0) {
                    velocityX = 0;
                }
            }
            
            boolean swap = false;
            boolean swapRight = false;
            final boolean velocityEnable = mMinFlingVelocity <= velocityX && velocityX <= mMaxFlingVelocity && velocityY * VELOCITY_FACTOR < velocityX;
            
            switch (mSwipeCurrentAction) {
            case SWIPE_ACTION_DISMISS:// For Dismiss
                if (velocityEnable && !mOpened) {// For Velocity
                    swapRight = mVelocityTracker.getXVelocity() > 0;
                    if (swapRight && mSwipeActionRight == SWIPE_ACTION_DISMISS) {
                        swap = true;
                    } else if (!swapRight && mSwipeActionLeft == SWIPE_ACTION_DISMISS) {
                        swap = true;
                    } else {
                        swap = false;
                    }
                } else if (Math.abs(mBackView.getTranslationX()) > mViewWidth / 2) {// For Offset
                    swapRight = mEventSwipingDirection == EVENT_SWIPING_RIGHT;
                    // when SWIPE_ACTION_DISMISS, if swiping towards the Direction which can't dismiss, swap = false.
                    if (swapRight && mSwipeActionRight == SWIPE_ACTION_DISMISS) {
                        swap = true;
                    } else if (!swapRight && mSwipeActionLeft == SWIPE_ACTION_DISMISS) {
                        swap = true;
                    } else {
                        swap = false;
                    }
                }
                break;
            case SWIPE_ACTION_REVEAL:// For SWIPE_ACTION_REVEAL
                if (velocityEnable) {// For Velocity
                    swapRight = mVelocityTracker.getXVelocity() > 0;
                    
                    if (!mOpened && swapRight && mSwipeActionRight == SWIPE_ACTION_REVEAL) {
                        swap = true;
                    } else if (!mOpened && !swapRight && mSwipeActionLeft == SWIPE_ACTION_REVEAL) {
                        swap = true;
                    } else if (mOpened && mOpenedRight && !swapRight) {
                        swap = true;
                    } else if (mOpened && !mOpenedRight && swapRight) {
                        swap = true;
                    } else {
                        swap = false;
                    }
                } else {// For Offset
                    swapRight = mEventSwipingDirection == EVENT_SWIPING_RIGHT;
                    
                    if (!mOpened && swapRight && mSwipeActionRight == SWIPE_ACTION_REVEAL) {
                        swap = eventFrontViewX > (mViewWidth - mRightOffset) / 2 ? true : false;
                    } else if (!mOpened && !swapRight && mSwipeActionLeft == SWIPE_ACTION_REVEAL) {
                        swap = eventFrontViewX < (mLeftOffset - mViewWidth) / 2 ? true : false;
                    } else if (mOpened && mOpenedRight) {
                        swap = eventFrontViewX > (mViewWidth - mRightOffset) / 2 ? false : true;
                    } else if (mOpened && !mOpenedRight) {
                        swap = eventFrontViewX < (mLeftOffset - mViewWidth) / 2 ? false : true;
                    }
                }
                break;
            case SWIPE_ACTION_SWITCH:// For SWIPE_ACTION_SWITCH, Only offset
                swapRight = mEventSwipingDirection == EVENT_SWIPING_RIGHT;
                boolean isSwitched = false;
                if (mSwipeActionRight == SWIPE_ACTION_SWITCH && eventFrontViewX >= mSwipeRightSwitchLine) {
                    isSwitched = true;
                } else if (mSwipeActionLeft == SWIPE_ACTION_SWITCH && eventFrontViewX + mViewWidth <= mSwipeLeftSwitchLine) {
                    isSwitched = true;
                } else {
                    isSwitched = false;
                }
                generateSwitchAnimate(mFrontView, isSwitched);
                break;
            default:
                break;
            }
            
            if (mSwipeCurrentAction != SWIPE_ACTION_SWITCH) {
                generateAnimate(mFrontView, swap, swapRight);
            }
            
            // Because actionDirection changed, and mSwipeCurrentAction == SwipeListView.SWIPE_ACTION_NONE,
            // So animate mFrontView's translationX to 0. See resetForChangeActionDirection.
            if (mSwipeCurrentAction == SWIPE_ACTION_NONE && eventFrontViewX > 0) {
                generateRevealAnimate(mFrontView, false, false);
            }
            
            mVelocityTracker.recycle();
            mVelocityTracker = null;
            resetEventValue();// reset
            break;
        case MotionEvent.ACTION_MOVE: 
            if (mVelocityTracker == null || mPaused || isAnimating() || !mIsSwipeEnable || mFrontView == null) {
                break;
            }
            
            mVelocityTracker.addMovement(ev);
            mVelocityTracker.computeCurrentVelocity(1000);
            velocityX = Math.abs(mVelocityTracker.getXVelocity());
            velocityY = Math.abs(mVelocityTracker.getYVelocity());
            
            final float moveRawX = ev.getRawX();
            
            if (mEventPreEventX > moveRawX) {
                if (mEventSwipingDirection == EVENT_SWIPING_RIGHT) {
                    mEventStartX = moveRawX;
                    mStartFrontViewX = eventFrontViewX;
                    if (mBackView != null) {
                        mStartBackViewX = mBackView.getTranslationX();
                    }
                }
                mEventSwipingDirection = EVENT_SWIPING_LEFT;
                mEventPreEventX = moveRawX;
            } else if (mEventPreEventX < moveRawX) {
                if (mEventSwipingDirection == EVENT_SWIPING_LEFT) {
                    mEventStartX = moveRawX;
                    mStartFrontViewX = eventFrontViewX;
                    if (mBackView != null) {
                        mStartBackViewX = mBackView.getTranslationX();
                    }
                }
                mEventSwipingDirection = EVENT_SWIPING_RIGHT;
                mEventPreEventX = moveRawX;
            } else {
                mEventPreEventX = moveRawX;
            }
            
            deltaX = moveRawX - mEventStartX;
            float deltaMode = Math.abs(deltaX);
            
            int swipeMode = this.mSwipeMode;
            int changeSwipeMode = onChangeSwipeMode();
            if (changeSwipeMode >= 0) {
                swipeMode = changeSwipeMode;
            }
            
            // Limited slip 
            if (swipeMode == SWIPE_MODE_NONE) {
                deltaMode = 0;
            } else if (swipeMode != SWIPE_MODE_BOTH) {
                if (mOpened) {
                    if (swipeMode == SWIPE_MODE_LEFT && deltaX < 0) {
                        //deltaMode = 0;
                    } else if (swipeMode == SWIPE_MODE_RIGHT && deltaX > 0) {
                        //deltaMode = 0;
                    }
                } else {
                    if (swipeMode == SWIPE_MODE_LEFT && deltaX > 0 && mViewSwipedDirection == VIEW_SWIPED_FIXED) {
                        deltaMode = 0;
                    } else if (swipeMode == SWIPE_MODE_RIGHT && deltaX < 0 && mViewSwipedDirection == VIEW_SWIPED_FIXED) {
                        deltaMode = 0;
                    }
                }
            }
            
            // Limited slip
            if (!mIsOverOffsetEnabled && mSwipeCurrentAction == SWIPE_ACTION_REVEAL) {
                if (swipeMode == SWIPE_MODE_LEFT && deltaX < - mViewWidth + mLeftOffset) {
                    deltaX = - mViewWidth + mLeftOffset;
                } else if (swipeMode == SWIPE_MODE_RIGHT && deltaX > mViewWidth - mRightOffset) {
                    deltaX = mViewWidth - mRightOffset;
                } else if (swipeMode == SWIPE_MODE_BOTH && (deltaX < - mViewWidth + mLeftOffset || deltaX > mViewWidth - mRightOffset)) {
                    // TODO
                }
            }
            
            if (deltaMode > mPagingSlop && mSwipeCurrentAction == SWIPE_ACTION_NONE && velocityY * VELOCITY_FACTOR < velocityX) {
                mSwiping = true;
                mEventStartX = moveRawX;
                deltaX = 0;
                
                if (mOpened) {
                    onStartClose(mEventSwipingDirection == EVENT_SWIPING_RIGHT);
                    mSwipeCurrentAction = SWIPE_ACTION_REVEAL;
                    mStartToLeftLock = mOpenedRight ? false : true;
                    mStartToRightLock = mOpenedRight ? true : false;
                } else {
                    if (mEventSwipingDirection == EVENT_SWIPING_RIGHT) {
                        mSwipeCurrentAction = mSwipeActionRight;
                    } else if (mEventSwipingDirection == EVENT_SWIPING_LEFT) {
                        mSwipeCurrentAction = mSwipeActionLeft;
                    }
                    onStartOpen(mSwipeCurrentAction, mEventSwipingDirection == EVENT_SWIPING_RIGHT);
                    boolean isOpenRight = mEventSwipingDirection == EVENT_SWIPING_RIGHT;
                    mStartToLeftLock = isOpenRight ? false : true;
                    mStartToRightLock = isOpenRight ? true : false;
                }
                
                MotionEvent cancelEvent = MotionEvent.obtain(ev);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                        (ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                superOnTouchEvent(cancelEvent);
            }
            
            if (mSwiping) {
                deltaX += mSwipeCurrentAction == SWIPE_ACTION_DISMISS ? mStartBackViewX : mStartFrontViewX ;
                move(deltaX);
                return true;
            }
            break;
        }
        return false;
    }
    
    protected boolean superOnTouchEvent(MotionEvent ev) {
        return mCallback.superOnTouch(ev);
    }
    
    private void resetEventValue() {
        mEventDownX = 0;
        mEventStartX = 0;
        mEventPreEventX = 0;
        mStartFrontViewX = 0;
        mStartBackViewX = 0;
        mSwiping = false;
        mEventSwipingDirection = EVENT_SWIPING_FIXED;
        mStartToRightLock = false;
        mStartToLeftLock = false;
        mIsSwitch = false;
    }
    
    /**
     * Moves the view
     *
     * @param deltaX delta
     */
    private void move(float deltaX) {
        onMove(deltaX);
        
        // When SWIPE_ACTION_DISMISS, Set values
        if (mSwipeCurrentAction == SWIPE_ACTION_DISMISS) {
            mBackView.setTranslationX(deltaX);
            mBackView.setAlpha(Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth)));
        }
        
        // When SWIPE_ACTION_REVEAL, Set values and Limit sliding region
        if (mSwipeCurrentAction == SWIPE_ACTION_REVEAL) {
            switch(mSwipeMode) {
            case SWIPE_MODE_BOTH:
                if (deltaX < - mViewWidth + mLeftOffset && deltaX > mViewWidth - mRightOffset) {
                    // TODO overSroll
                }
                if (mSwipeActionLeft != mSwipeActionRight) {
                    if (mSwipeActionLeft == SWIPE_ACTION_DISMISS || mSwipeActionRight == SWIPE_ACTION_DISMISS) {
                        if (deltaX > 0 && mStartToLeftLock) {
                            deltaX = 0;
                        } else if (deltaX < 0 && mStartToRightLock) {
                            deltaX = 0;
                        }
                    } else {
                        if (deltaX < 0) {
                            mSwipeCurrentAction = mSwipeActionLeft;
                        } else {
                            mSwipeCurrentAction = mSwipeActionRight;
                        }
                    }
                }
                mFrontView.setTranslationX(deltaX);
                break;
            case SWIPE_MODE_LEFT:
                final float leftThreshold = - mViewWidth + mLeftOffset;
                if (mEventSwipingDirection == EVENT_SWIPING_LEFT && deltaX <= leftThreshold) {
                    //deltaX = leftThreshold;
                    // TODO overSroll
                } else if ((mEventSwipingDirection == EVENT_SWIPING_RIGHT) && deltaX > 0) {
                    deltaX = 0;
                }
                mFrontView.setTranslationX(deltaX);
                break;
            case SWIPE_MODE_RIGHT:
                final float rightThreshold = mViewWidth - mRightOffset;
                if ((mEventSwipingDirection == EVENT_SWIPING_RIGHT) && deltaX >= rightThreshold) {
                    //deltaX = rightThreshold;
                    // TODO overSroll
                } else if (mEventSwipingDirection == EVENT_SWIPING_LEFT && deltaX < 0) {
                    deltaX = 0;
                }
                mFrontView.setTranslationX(deltaX);
                break;
            default:
                break;
            }
        }
        
        // When SWIPE_ACTION_SWITCH, Set values and Limit sliding region
        if (mSwipeCurrentAction == SWIPE_ACTION_SWITCH) {
            switch(mSwipeMode) {
            case SWIPE_MODE_BOTH:
                if (deltaX < - mViewWidth + mLeftOffset && deltaX > mViewWidth - mRightOffset) {
                    // TODO overSroll
                }
                if (mSwipeActionLeft != mSwipeActionRight) {
                    if (mSwipeActionLeft == SWIPE_ACTION_DISMISS || mSwipeActionRight == SWIPE_ACTION_DISMISS) {
                        if (deltaX > 0 && mStartToLeftLock) {
                            deltaX = 0;
                        } else if (deltaX < 0 && mStartToRightLock) {
                            deltaX = 0;
                        }
                    } else {
                        if (deltaX < 0) {
                            mSwipeCurrentAction = mSwipeActionLeft;
                        } else {
                            mSwipeCurrentAction = mSwipeActionRight;
                        }
                    }
                }
                // Call onSwipeSwitching
                if (mSwipeCurrentAction == SWIPE_ACTION_SWITCH) {
                    if (mStartToLeftLock && deltaX <= mSwipeLeftSwitchLine - mViewWidth) {
                        if (!mIsSwitch) {
                            mIsSwitch = true;
                            onSwipeSwitching(mIsSwitch);
                        }
                    } else if (mStartToLeftLock && deltaX > mSwipeLeftSwitchLine - mViewWidth) {
                        if (mIsSwitch) {
                            mIsSwitch = false;
                            onSwipeSwitching(mIsSwitch);
                        }
                    }
                    
                    if (mStartToRightLock && deltaX >= mSwipeRightSwitchLine) {
                        if (!mIsSwitch) {
                            mIsSwitch = true;
                            onSwipeSwitching(mIsSwitch);
                        }
                    } else if (mStartToRightLock && deltaX < mSwipeRightSwitchLine) {
                        if (mIsSwitch) {
                            mIsSwitch = false;
                            onSwipeSwitching(mIsSwitch);
                        }
                    }
                }
                
                mFrontView.setTranslationX(deltaX);
                break;
            case SWIPE_MODE_LEFT:
                if (deltaX <= mSwipeLeftSwitchLine - mViewWidth) {
                    if (!mIsSwitch) {
                        mIsSwitch = true;
                        onSwipeSwitching(mIsSwitch);
                    }
                } else if (deltaX > mSwipeLeftSwitchLine - mViewWidth) {
                    if (mIsSwitch) {
                        mIsSwitch = false;
                        onSwipeSwitching(mIsSwitch);
                    }
                }
                // Limited slip 
                if ((mEventSwipingDirection == EVENT_SWIPING_RIGHT) && deltaX > 0) {
                    deltaX = 0;
                }
                mFrontView.setTranslationX(deltaX);
                break;
            case SWIPE_MODE_RIGHT:
                if (deltaX >= mSwipeRightSwitchLine) {
                    if (!mIsSwitch) {
                        mIsSwitch = true;
                        onSwipeSwitching(mIsSwitch);
                    }
                } else if (deltaX < mSwipeRightSwitchLine) {
                    if (mIsSwitch) {
                        mIsSwitch = false;
                        onSwipeSwitching(mIsSwitch);
                    }
                }
                // Limited slip 
                if ((mEventSwipingDirection == EVENT_SWIPING_LEFT) && deltaX < 0) {
                    deltaX = 0;
                }
                mFrontView.setTranslationX(deltaX);
                break;
            default:
                break;
            }
        }
    }

    /**
     * Start open item
     *
     * @param action   current action
     * @param right    to right
     */
    protected void onStartOpen(int action, boolean right) {
        if (mSwipeListener != null) {
            mSwipeListener.onStartOpen(action, right);
        }
    }

    /**
     * Start close item
     *
     * @param right
     */
    protected void onStartClose(boolean right) {
        if (mSwipeListener != null) {
            mSwipeListener.onStartClose(right);
        }
    }

    /**
     * Notifies onClickFrontView
     */
    /*protected void onClickFrontView() {
        if (mSwipeListener != null) {
            mSwipeListener.onClickFrontView();
        }
    }*/

    /**
     * Notifies onClickBackView
     */
    /*protected void onClickBackView() {
        if (mSwipeListener != null) {
            mSwipeListener.onClickBackView();
        }
    }*/

    /**
     * Notifies onOpened
     *
     * @param toRight  If should be opened toward the right
     */
    protected void onOpened(boolean toRight) {
        if (mSwipeListener != null) {
            mSwipeListener.onOpened(toRight);
        }
    }

    /**
     * Notifies onClosed
     *
     * @param fromRight If open from right
     */
    protected void onClosed(boolean fromRight) {
        if (mSwipeListener != null) {
            mSwipeListener.onClosed(fromRight);
        }
    }

    /**
     * Notifies onMove
     *
     * @param x        Current position
     */
    protected void onMove(float x) {
        if (mSwipeListener != null) {
            mSwipeListener.onMove(x);
        }
    }
    
    /**
     * Used when user want to change swipe list mode on some rows. Return SWIPE_MODE_DEFAULT
     * if you don't want to change swipe list mode
     * 
     * @return type
     */
    protected int onChangeSwipeMode() {
        if (mSwipeListener != null) {
            return mSwipeListener.onChangeSwipeMode();
        }
        return SWIPE_MODE_DEFAULT;
    }
    
    /**
     * Notifies swipeDismiss animation is end
     */
    protected void onSwipeDismissed(boolean swap, View view) {
        resetCell();
        mIsAnimating = false;
    }
    
    /**
     * Notifies swipeReveal animation is end
     */
    protected void onSwipeRevealed(boolean swap, boolean swapRight) {
        boolean aux = mOpened;
        if (swap) {
            aux = !mOpened;
            mOpened = aux;
        }

        if (aux) {
            onOpened(swapRight);
            mOpenedRight = swapRight;
        } else {
            onClosed(swapRight);
        }
        resetCell();
        mIsAnimating = false;
    }
    
    /**
     * Notifies swipe cross switch-line.
     */
    protected void onSwipeSwitching(boolean isChanged) {
        if (mSwipeSwitchListener != null) {
            mSwipeSwitchListener.onSwitching(isChanged);
        }
    }
    
    /**
     * Notifies swipe animation is end
     */
    protected void onSwipeSwitched(boolean isSwitched) {
        if (mSwipeSwitchListener != null) {
            mSwipeSwitchListener.onSwitched(isSwitched);
        }
        resetCell();
        mIsAnimating = false;
    }

    /**
     * Create animation
     *
     * @param view      affected view
     * @param swap      If state should change. If "false" returns to the original position
     * @param swapRight If swap is true, this parameter tells if move is to the right or left
     * @param position  Position of list
     */
    protected void generateAnimate(final View view, final boolean swap, final boolean swapRight) {
        //Log.d(TAG, "swap: " + swap + " - swapRight: " + swapRight + " - mSwipeCurrentAction = " + mSwipeCurrentAction);
        if (mSwipeCurrentAction == SWIPE_ACTION_REVEAL) {
            generateRevealAnimate(view, swap, swapRight);
        }
        if (mSwipeCurrentAction == SWIPE_ACTION_DISMISS) {
            generateDismissAnimate(mBackView, swap, swapRight);
        }
    }

    /**
     * Create dismiss animation
     *
     * @param view      affected view
     * @param swap      If will change state. If is "false" returns to the original position
     * @param swapRight If swap is true, this parameter tells if move is to the right or left
     */
    protected void generateDismissAnimate(final View view, final boolean swap, final boolean swapRight) {
        if (view == null) {
            return;
        }
        // When mode is drag-sort, can use generateDismissAnimate
        if (mViewWidth < 2) {
            mViewWidth = mCallback.getSwipeViewWidth();
        }
        
        int moveTo = 0;
        if (mOpened) {
            if (!swap) {
                moveTo = mOpenedRight ? (int) (mViewWidth - mRightOffset) : (int) (-mViewWidth + mLeftOffset);
            }
        } else {
            if (swap) {
                moveTo = swapRight ? (int) (mViewWidth - mRightOffset) : (int) (-mViewWidth + mLeftOffset);
            }
        }

        int alpha = 1;
        if (swap) {
            alpha = 0;
        }

        view.animate()
                .translationX(moveTo)
                .alpha(alpha)
                .setInterpolator(sInterpolator)
                .setDuration(mAnimationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onSwipeDismissed(swap, view);
                    }
                });
        mIsAnimating = true;
    }

    /**
     * Create reveal animation
     *
     * @param view      affected view
     * @param swap      If will change state. If "false" returns to the original position
     * @param swapRight If swap is true, this parameter tells if movement is toward right or left
     */
    protected void generateRevealAnimate(final View view, final boolean swap, final boolean swapRight) {
        if (view == null) {
            return;
        }
        // When mode is drag-sort, can use generateDismissAnimate
        if (mViewWidth < 2) {
            mViewWidth = mCallback.getSwipeViewWidth();
        }
        
        int moveTo = 0;
        if (mOpened) {
            if (!swap) {
                moveTo = mOpenedRight ? (int) (mViewWidth - mRightOffset) : (int) (-mViewWidth + mLeftOffset);
            }
        } else {
            if (swap) {
                moveTo = swapRight ? (int) (mViewWidth - mRightOffset) : (int) (-mViewWidth + mLeftOffset);
            }
        }

        view.animate()
                .translationX(moveTo)
                .setDuration(mAnimationTime)
                .setInterpolator(sInterpolator)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onSwipeRevealed(swap, swapRight);
                    }
                });
        mIsAnimating = true;
    }

    /**
     * Create switch animation
     *
     * @param view      affected view
     * @param swap      If will change state. If "false" returns to the original position
     * @param swapRight If swap is true, this parameter tells if movement is toward right or left
     */
    protected void generateSwitchAnimate(final View view, final boolean isSwitched) {
        if (view == null) {
            return;
        }
        // When mode is drag-sort, can use generateDismissAnimate
        if (mViewWidth < 2) {
            mViewWidth = mCallback.getSwipeViewWidth();
        }
        
        view.animate()
                .translationX(0)
                .setDuration(mAnimationTime)
                .setInterpolator(sInterpolator)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onSwipeSwitched(isSwitched);
                    }
                });
        mIsAnimating = true;
    }

    protected void resetCell() {
        if (!isAnimating()) {
            //mFrontView = null;
            //mBackView = null;
        }
    }
    
    /**
     * Sets current item's front view
     *
     * @param mFrontView Front view
     */
    protected void setFrontView(View frontView) {
        this.mFrontView = frontView;
    }

    /**
     * Set current item's back view
     *
     * @param mBackView
     */
    protected void setBackView(View backView) {
        this.mBackView = backView;
    }

    /**
     * Check is swipeListView's touch is enabled
     * 
     * @return
     */
    protected boolean isSwipeTouchEnabled() {
        return mSwipeMode != SWIPE_MODE_NONE;
    }
    
    /**
     * Check swipe is enabled
     *
     * @return
     */
    protected boolean isSwipeEnabled() {
        return mIsSwipeEnable;
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
        ViewGroup backView = (ViewGroup) getBackView();

        if (backView == null) {
            return false;
        }

        for (int j = 0; j < backView.getChildCount(); j++) {
            View backChildView = backView.getChildAt(j);
            backChildView.setClickable(forceUnClickable ? false : mOpened);
            backChildView.setLongClickable(forceUnClickable ? false : mOpened);
        }

        return mOpened;
    }
    
    protected void setBackViewChildFocusable(int x, int y, boolean focus) {
        ViewGroup backView = (ViewGroup) getBackView();

        if (backView == null) {
            return;
        }

        for (int j = 0; j < backView.getChildCount(); j++) {
            View backChildView = backView.getChildAt(j);
            backChildView.setFocusable(focus);
        }

        return;
    }

    /** 
     * Close open item.
     * If user touchs outside(no touch menu), ListView will close the open item.
     * 
     * @param x touchX
     * @return the item is open, and close it.
     */
    protected boolean closeOpenedItem(int x) {
        if (mOpened) {
            final float absTX = Math.abs(mFrontView.getTranslationX());

            if (mOpenedRight && x >= absTX) {
                closeAnimate();
                return true;
            }
            if (!mOpenedRight && x <= mFrontView.getWidth() - absTX) {
                closeAnimate();
                return true;
            }
        }
        return false;
    }

    protected View getFrontView() {
        if (mCallback != null) {
            return mCallback.getFrontView();
        }
        return null;
    }

    protected View getBackView() {
        if (mCallback != null) {
            return mCallback.getBackView();
        }
        return null;
    }

    protected interface BaseCallback {

        int getSwipeViewWidth();

        boolean superOnTouch(MotionEvent ev);

    }

    public interface Callback extends BaseCallback {

        View getFrontView();

        View getBackView();

    }

}
