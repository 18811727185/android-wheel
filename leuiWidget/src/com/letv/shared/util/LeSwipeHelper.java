package com.letv.shared.util;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

public class LeSwipeHelper {
    private final static String TAG = LeSwipeHelper.class.getSimpleName();
    private final boolean DEBUG = false;
    
    public final static int DIRECTION_X = 0x01;
    public final static int DIRECTION_Y = 0x02;
    
    protected static final int INVALID_POINTER = -1;
    protected static final int INVALID_RAW_POS = -1;
    
    protected int mDirection;
    
    protected boolean mDragging;
    
    protected float mInitialTouchPosRawX = INVALID_RAW_POS;
    protected float mInitialTouchPosRawY = INVALID_RAW_POS;
    
    protected float mLastRawX = INVALID_RAW_POS;
    protected float mLastRawY = INVALID_RAW_POS;

    protected int mActivePointerId = INVALID_POINTER;
    
    protected int mTouchSlop;
    protected int mMinimumVelocity;
    protected int mMaximumVelocity;
    
    protected OnSwipeListener mOnSwipeListener;
    
    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;

    
    public LeSwipeHelper(Context context) {
        getViewConfiguration(context);
        mDirection = DIRECTION_X | DIRECTION_Y;
    }
    
    public void setOnSwipeListener(OnSwipeListener l) {
        mOnSwipeListener = l;
    }
    
    public void setDirection(int direction) {
        mDirection = (direction & DIRECTION_X) | (direction & DIRECTION_Y);
    }
    
    public int getDirection() {
        return mDirection;
    }
    
    public void setTouchSlop(int touchSlop) {
        if (touchSlop >= 0) {
            mTouchSlop = touchSlop;
        }
    }
    
    public int getTouchSlop() {
        return mTouchSlop;
    }
    
    /**
     * Get View configuration
     */
    private final void getViewConfiguration(Context context) {
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        
        if (DEBUG) {
            Log.d(TAG, "mTouchSlop = " + mTouchSlop +
                    " mMinimumVelocity = " + mMinimumVelocity +
                    " mMaximumVelocity = " + mMaximumVelocity);
        }
    }
    
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

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
    
    private int getPointerIndex(MotionEvent ev) {
        int pointerIndex = ev.findPointerIndex(mActivePointerId);
        if (pointerIndex == -1) {
            if (DEBUG) {
                Log.d(TAG, "can't find pointerIndex for mActivePointerId " + mActivePointerId);
            }
            
            pointerIndex = 0;
            mActivePointerId = ev.getPointerId(pointerIndex);
        }
        return pointerIndex;
    }
    
    private void initTouchPosition(MotionEvent ev) {
        mActivePointerId = ev.getPointerId(ev.getActionIndex());
        int pointerIndex = getPointerIndex(ev);
        
        mInitialTouchPosRawX = ev.getRawX() + ev.getX(pointerIndex) - ev.getX();
        mInitialTouchPosRawY = ev.getRawY() + ev.getY(pointerIndex) - ev.getY();
        mLastRawX = mInitialTouchPosRawX;
        mLastRawY = mInitialTouchPosRawY;
        
        if (DEBUG) {
            Log.d(TAG, "initTouchPosition() mActivePointerId = " + mActivePointerId + 
                    " initTouchPosition mInitialTouchPosRawX = " + mInitialTouchPosRawX + 
                    " mInitialTouchPosRawY = " + mInitialTouchPosRawY);
        }
    }
    
    private void onSecondaryPointerUp(MotionEvent ev) {
        if (ev.getPointerId(ev.getActionIndex()) == mActivePointerId) {
            // This was our active pointer going up. Choose a new active pointer randomly and adjust accordingly.
            final int pointerIndex = ev.getActionIndex();
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            
            mActivePointerId = ev.getPointerId(newPointerIndex);
            
            mInitialTouchPosRawX = ev.getX(newPointerIndex) - ev.getX() + ev.getRawX();
            mInitialTouchPosRawY = ev.getY(newPointerIndex) - ev.getY() + ev.getRawY(); 
            
            mLastRawX = mInitialTouchPosRawX;
            mLastRawY = mInitialTouchPosRawY;
            
            initOrResetVelocityTracker();
        }
    }
    
    private void resetTouchPositionForActionUp() {        
        mInitialTouchPosRawX = INVALID_RAW_POS;
        mInitialTouchPosRawY = INVALID_RAW_POS;
        
        mLastRawX = INVALID_RAW_POS;
        mLastRawY = INVALID_RAW_POS;
        
        mActivePointerId = INVALID_POINTER;
    }
    
    private boolean determineDrag(MotionEvent ev) {
        int pointerIndex = getPointerIndex(ev);

        float currRawX = ev.getRawX() + ev.getX(pointerIndex) - ev.getX();
        float currRawY = ev.getRawY() + ev.getY(pointerIndex) - ev.getY();
        float deltaX = Math.abs(currRawX - mInitialTouchPosRawX);
        float deltaY = Math.abs(currRawY - mInitialTouchPosRawY);
            
        if ((mDirection & DIRECTION_X) != 0 && deltaX > mTouchSlop) {
            return true;
        }
        
        if ((mDirection & DIRECTION_Y) != 0 && deltaY > mTouchSlop) {
            return true;
        }
        
        return false;
    }
    
    private void updateViewPosition(MotionEvent ev) {
        int pointerIndex = getPointerIndex(ev);
        float currRawX = ev.getRawX() + ev.getX(pointerIndex) - ev.getX();
        float currRawY = ev.getRawY() + ev.getY(pointerIndex) - ev.getY();
        float offsetX = currRawX - mLastRawX;
        float offsetY = currRawY - mLastRawY;
        
        boolean needUpdate = false;
        if ((mDirection & DIRECTION_X) != 0 && offsetX != 0f) {
            needUpdate = true;
        }
        
        if ((mDirection & DIRECTION_Y) != 0 && offsetY != 0f) {
            needUpdate = true;
        }
        
        if (mOnSwipeListener != null && needUpdate) {

            mOnSwipeListener.onUpdateViewPositionBy(offsetX, offsetY);
        }
        mLastRawX = currRawX;
        mLastRawY = currRawY;
        
        if (DEBUG) {
            Log.d(TAG, "currRawX = " + currRawX + " currRawY = " + currRawY);
        }
    }
    
    private void endSwipe(MotionEvent ev) {
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        int xVelocity = (int) mVelocityTracker.getXVelocity(mActivePointerId);
        int yVelocity = (int) mVelocityTracker.getYVelocity(mActivePointerId);
        
        if (mOnSwipeListener != null) {
            mOnSwipeListener.onSwipeEnd(xVelocity, yVelocity, mMinimumVelocity);
        }   
    }
    
    private void addMotionEventWithAdjustment(MotionEvent ev) {
        MotionEvent newEvent = MotionEvent.obtain(ev);
            
        int pointerIndex = getPointerIndex(ev);
        float currRawX = ev.getRawX() + ev.getX(pointerIndex) - ev.getX();
        float currRawY = ev.getRawY() + ev.getY(pointerIndex) - ev.getY();
        newEvent.setLocation(currRawX, currRawY);
        mVelocityTracker.addMovement(newEvent);
        newEvent.recycle();
        
        printMotionEvent(ev);
    }
    
    private final void printMotionEvent(MotionEvent ev) {
        if (!DEBUG)
            return;
        
        int pointerIndex = getPointerIndex(ev);
        float currRawX = ev.getRawX() + ev.getX(pointerIndex) - ev.getX();
        float currRawY = ev.getRawY() + ev.getY(pointerIndex) - ev.getY();
        
        Log.d(TAG, "    printMotionEvent currRawX = " + currRawX + " currRawY = " + currRawY);
        Log.d(TAG, "    printMotionEvent ev.getX() = " + ev.getX(pointerIndex) + " ev.getY() = " + ev.getY(pointerIndex));
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        
        final int actionMasked = ev.getActionMasked();
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                if (DEBUG) {
                    Log.d(TAG, "onInterceptTouchEvent ACTION_DOWN, return false");
                }
                mDragging = false;
                initTouchPosition(ev);
                initVelocityTrackerIfNotExists();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mDragging) { 
                   // if mDragging is true, intercept subsequent MotionEvent to onTouchEvent()
                    mDragging = determineDrag(ev);  
                }
                
                if (DEBUG) {
                    if (mDragging) {
                        Log.d(TAG, "onInterceptTouchEvent ACTION_MOVE return true");
                    } else {
                        Log.d(TAG, "onInterceptTouchEvent ACTION_MOVE return false");
                    }
                }
                break;
            
            // Always take care of the touch gesture being complete
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (DEBUG) {
                    if (actionMasked == MotionEvent.ACTION_CANCEL) {
                        Log.d(TAG, "onInterceptTouchEvent MotionEvent.ACTION_CANCEL return false");
                    } else {
                        Log.d(TAG, "onInterceptTouchEvent ACTION_UP return false");
                    }
                }
                
                if (mDragging) {
                    mDragging = false;  
                }
                resetTouchPositionForActionUp();
                recycleVelocityTracker();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                
                if (DEBUG) {
                    Log.d(TAG, "onInterceptTouchEvent MotionEvent.ACTION_POINTER_UP return " + mDragging);
                }
                break;
            default:
                if (DEBUG) {
                    Log.d(TAG, "onInterceptTouchEvent " + actionMasked + " return " + mDragging);
                }
                break;
        }
        
        if (mVelocityTracker != null) {
            addMotionEventWithAdjustment(ev);
        }
        
        return mDragging;
    }
    
    public boolean onTouchEvent(MotionEvent ev) {
        
        final int actionMasked = ev.getActionMasked();
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: 
                if (DEBUG) {
                    Log.d(TAG, "onTouchEvent ACTION_DOWN, return true");
                }
                mDragging = false;
                initTouchPosition(ev);
                initOrResetVelocityTracker();
                break;

            case MotionEvent.ACTION_MOVE:
                if (DEBUG) {
                    Log.d(TAG, "onTouchEvent ACTION_MOVE, return true");
                }

                if (!mDragging) {
                    mDragging = determineDrag(ev);
                }
                
                if (mDragging) {
                    updateViewPosition(ev);
                }
                
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: 
                if (DEBUG) {
                    if (actionMasked == MotionEvent.ACTION_CANCEL) {
                        Log.d(TAG, "onTouchEvent ACTION_CANCEL, return true");
                    } else {
                        Log.d(TAG, "onTouchEvent ACTION_UP, return true");
                    }
                }
                
                if (mDragging) {
                    mDragging = false;  
                }
                
                if (mVelocityTracker != null) {
                    addMotionEventWithAdjustment(ev);
                }
                
                endSwipe(ev);
                
                resetTouchPositionForActionUp();
                recycleVelocityTracker();
                break;

            case MotionEvent.ACTION_POINTER_UP: {
                if (DEBUG) {
                    Log.d(TAG, "onTouchEvent ACTION_POINTER_UP, return true");
                }
                onSecondaryPointerUp(ev);
                break;
            }
            
            case MotionEvent.ACTION_POINTER_DOWN:
                if (DEBUG) {
                    Log.d(TAG, "onTouchEvent ACTION_POINTER_DOWN, return true");
                }
                initTouchPosition(ev);
                initOrResetVelocityTracker();
                break;
            default:
                if (DEBUG) {
                    Log.d(TAG, "onInterceptTouchEvent " + actionMasked + " return true");
                }
                break;
        }
        
        if (mVelocityTracker != null) {
            addMotionEventWithAdjustment(ev);
        }
        
        return true;
    }
    
    /**
     * Interface definition for a swipe gesture
     */
    public interface OnSwipeListener {
        void onUpdateViewPositionBy(float offsetX, float offsetY);
        void onSwipeEnd(int xVelocity, int yVelocity, int minimumVelocity);
    }
     
}
