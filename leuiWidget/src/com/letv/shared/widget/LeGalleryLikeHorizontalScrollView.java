
package com.letv.shared.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.HorizontalScrollView;
import android.widget.OverScroller;
import com.letv.shared.util.LeReflectionUtils;

import java.util.ArrayList;

public class LeGalleryLikeHorizontalScrollView extends HorizontalScrollView {
    private static final int MAX_X_OVERSCROLL_DISTANCE = 70;
    private int mMaxXOverscrollDistance;

    private int displayWidth;
    public boolean isStandardView = false;
    private boolean moveRatioSet = false;

    private int subChildCount = 0;
         private ViewGroup firstChild = null;
         private int downX = 0;
         private int currentPage = 0;
         private ArrayList<Integer> pointList = new ArrayList<Integer>();
         private VelocityTracker mVelocityTracker;
         private OverScroller overScroller;
         private static int mMaximumVelocity;
         private static int mMinimumVelocity;
         private int scrollViewPadding;
         private static final int DURATION_MIN = 200;
         private static final int DURATION_UNIT = 500;
         private static final double INFLEXION = 0.35;
         private static double mPhysicalCoeff ;
         private static final double DECELERATION_RATE = (Math.log(0.78) / Math.log(0.9));
         private static final double mFlingFriction = 0.015f;
         private int itemViewWidth = 0;
         private int margin = 0;
         private int screenWidth;
        private float time_factor;
    private static final double HORIZONTAL_DEG = 40;

    public int getMargin() {
            return margin;
        }
         private int lastScrollX;
        private Interpolator interpolator = new DecelerateInterpolator();
    private OnScrollListener onScrollListener = null;
    private static float moveRatio = 1f;
    private static int standardWidth = 0;
    private static float fling_Decelerat_factor;


    private static int DEFAULT_ITEMSHOWINSCREEN = 3;
    private TouchEventListener touchEventListener = null;
    public int index;
    public int eventIndex = 0;
    private static int MODE_FLING = 0;

    private static int MODE_SLOWMOVE = 1;

    private int itemShowInScreen = DEFAULT_ITEMSHOWINSCREEN;


    public void addViews(ArrayList<View> views){
        if(views==null||views.isEmpty()){
            return;
        }
        firstChild = (ViewGroup)getChildAt(0);
        if(firstChild!=null){
            for(View view:views){
                firstChild.addView(view);
            }

            requestLayout();
        }
    }


    public void setTouchEventListener(TouchEventListener touchEventListener) {
        this.touchEventListener = touchEventListener;
    }

        public void setInterpolator(Interpolator interpolator) {
            this.interpolator = interpolator;
            if (interpolator!=null&&overScroller!=null){
                LeReflectionUtils.invokeMethod(overScroller,"setInterpolator",new Class[]{
                        Interpolator.class},new Object[]{this.interpolator});
            }
        }

        private Handler handler = new Handler() {

            public void handleMessage(android.os.Message msg) {
                int scrollX = LeGalleryLikeHorizontalScrollView.this.getScrollX();
                int currentItem = setPage(scrollX,pointList,itemViewWidth);
                if(lastScrollX != scrollX){
                    lastScrollX = scrollX;
                    handler.sendMessageDelayed(handler.obtainMessage(), 5);
                }else{
                    if (onScrollListener!=null){
                        onScrollListener.onStop(scrollX,currentItem);
                    }
                }

                if(onScrollListener != null){
                    onScrollListener.onScroll(scrollX,currentItem);
                }

            };

        };
        /*private boolean needMeasureWidth = true;
        private int measureWidth;*/
        private OnHierarchyChangeListener mlistener = new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                subChildCount++;
                
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                if (subChildCount>0){
                    subChildCount--;
                }
            }
        };

        /**
         * 设置滚动接口
         * @param onScrollListener
         */
        public void setOnScrollListener(OnScrollListener onScrollListener) {
            this.onScrollListener = onScrollListener;
        }

         public LeGalleryLikeHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
             super(context, attrs, defStyle);
             init(context);

         }
     
     
         public LeGalleryLikeHorizontalScrollView(Context context, AttributeSet attrs) {
             super(context, attrs);
             init(context);
         }
         public LeGalleryLikeHorizontalScrollView(Context context) {
             super(context);
             init(context);
         }
        public LeGalleryLikeHorizontalScrollView(Context context,Interpolator interpolator) {
            super(context);
            this.interpolator = interpolator;
            init(context);
        }
         private void init(Context context) {
             setHorizontalScrollBarEnabled(false);
             mVelocityTracker = VelocityTracker.obtain();
             final ViewConfiguration configuration = ViewConfiguration.get(context);
             
             mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
             mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
             overScroller = (OverScroller)LeReflectionUtils.getFieldValue(this, "mScroller");
             WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
             if (interpolator!=null&&overScroller!=null){
                 LeReflectionUtils.invokeMethod(overScroller,"setInterpolator",new Class[]{
                         Interpolator.class},new Object[]{interpolator});
             }
             Point outSize = new Point();
             wm.getDefaultDisplay().getSize(outSize);
             screenWidth = outSize.x;
             
             float mPpi = context.getResources().getDisplayMetrics().density * 160.0f;
             mPhysicalCoeff = 9.80665f* 39.37f*mPpi*0.84f;
             time_factor = (float)DEFAULT_ITEMSHOWINSCREEN/itemShowInScreen;

             fling_Decelerat_factor = (float)(2*screenWidth/Math.pow(DURATION_UNIT,2));
             setOverScrollMode(View.OVER_SCROLL_ALWAYS);
             final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
             density = metrics.density;

             mMaxXOverscrollDistance = (int) (density * MAX_X_OVERSCROLL_DISTANCE);

         }
    private float density;
    private int mOverscrollDistance;
    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY,mOverscrollDistance , maxOverScrollY, isTouchEvent);
    }

    public boolean isNeedOffset() {
        return needOffset;
    }

    public void setNeedOffset(boolean needOffset) {
        this.needOffset = needOffset;
    }

    
    private boolean needOffset = true;

    public int getCenterPage() {
        return centerPage;

    }

    public void setCenterPage(int centerPage) {
        this.centerPage = centerPage;
    }

    private int centerPage;


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        needOffset = true;
    }
    private int layoutIndex = 0;

    @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {

             super.onLayout(changed, l, t, r, b);
        mMaxXOverscrollDistance = (int) (density * MAX_X_OVERSCROLL_DISTANCE);
             displayWidth = r-l;
//             Log.e("test","displayWidth>>"+displayWidth);
             firstChild = (ViewGroup)getChildAt(0);

             if(firstChild!=null){
                 firstChild.setOnHierarchyChangeListener(mlistener);
//                 LayoutParams lp = (LayoutParams) firstChild .getLayoutParams();
//                 Log.e("test","lp.leftMargin>>"+lp.leftMargin+"  lp.rightMargin>>"+lp.rightMargin);
                 subChildCount = firstChild.getChildCount();
                 if(subChildCount>0){
                     View itemView = (View)firstChild.getChildAt(0);
                     int scrollViewPaddingLeft = Math.round((displayWidth-itemView.getWidth())/2f/*-lp.leftMargin*/);
                     scrollViewPaddingLeft = scrollViewPaddingLeft>=0?scrollViewPaddingLeft:0;
                     int scrollViewPaddingRight = Math.round((displayWidth-itemView.getWidth())/2f/*-lp.rightMargin*/);
                     scrollViewPaddingRight = scrollViewPaddingRight>=0?scrollViewPaddingRight:0;
                     setPadding(scrollViewPaddingLeft, 0, scrollViewPaddingRight, 0);


                         if(subChildCount==1){
                             initItemWidth(firstChild.getWidth());
                         }else{
                             initItemWidth(((View)(firstChild.getChildAt(1))).getLeft());
                         }


                 }


             }
            mOverscrollDistance = mMaxXOverscrollDistance;
            layoutIndex++;
            if(needOffset&&subChildCount>=2&&centerPage>=0&&centerPage<subChildCount){
                scrollTo(itemViewWidth* centerPage,0);
            }
            if(layoutIndex>=2){
                setVisibility(VISIBLE);
            }
            
        }

    @Override
    protected void onDraw(Canvas canvas) {
        if(layoutIndex ==1){
            setVisibility(INVISIBLE);
            requestLayout();
        }else{
            setVisibility(VISIBLE);
        }
        super.onDraw(canvas);
    }

         private void initItemWidth(int width){
             itemViewWidth = width;
             if(isStandardView){
                 standardWidth = itemViewWidth;
//                 Log.d("test","isStandardView  mMaxXOverscrollDistance: "+mMaxXOverscrollDistance);
             }else{
                 moveRatio = (float) itemViewWidth / (float)standardWidth;
//                 Log.d("test","moveRation: "+moveRatio);
                 mMaxXOverscrollDistance = (int)(mMaxXOverscrollDistance*moveRatio);
//                 Log.d("test","  mMaxXOverscrollDistance: "+mMaxXOverscrollDistance);
             }


         }
         public void receiveChildInfo() {
             if (!pointList.isEmpty()){
                 pointList.clear();
             }
             firstChild = (ViewGroup) getChildAt(0);
             if(firstChild != null){
                 subChildCount = firstChild.getChildCount();
                 if(itemViewWidth==0){
                     if(subChildCount==1){
                         initItemWidth(firstChild.getWidth());
                     }else{
                         initItemWidth(((View)(firstChild.getChildAt(1))).getLeft());
                     }

                 }
                 for(int i = 0;i < subChildCount;i++){
                     View itemView = (View)firstChild.getChildAt(i);
                     int left = itemView.getLeft();

                     pointList.add(left);
                 }

             }

         }
         
         private void initVelocityTrackerIfNotExists() {
             if (mVelocityTracker == null) {
                 mVelocityTracker = VelocityTracker.obtain();
             }
         }
    public boolean dispathTouchEvent(MotionHolder motionHolder) {
        needOffset = motionHolder.needOffset;
        if (pointList.size()!=subChildCount) {
            receiveChildInfo();
        }
        eventIndex++;
//        Log.d("test","main eventIndex: "+eventIndex);
        if(!motionHolder.isCancel){
            isHorizonMove = true;
            switch (motionHolder.motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
//                    Log.d("test","sub down");
                case MotionEvent.ACTION_MOVE:
//                    Log.d("test","sub move");
                    if (!isStandardView){
                        motionHolder.motionEvent.setLocation(motionHolder.motionEvent.getX()*moveRatio,motionHolder.motionEvent.getY());
                    }else{
                        motionHolder.motionEvent.setLocation(motionHolder.motionEvent.getX()/moveRatio,motionHolder.motionEvent.getY());
                    }
                    return super.onTouchEvent(motionHolder.motionEvent);
                case MotionEvent.ACTION_CANCEL:
                    break;
                case MotionEvent.ACTION_UP:
//                    Log.d("test","sub up");
                    currentPage = motionHolder.currentPage;
                    if (motionHolder.mode==MODE_FLING){
                        if (!motionHolder.isOverscroll){
                            flingToPage(currentPage,motionHolder.duration);
                        }else{
                            int dst = !isStandardView?(int)(motionHolder.dst_for_overscroll*moveRatio):(int)(motionHolder.dst_for_overscroll/moveRatio);

                            flingOverScroll(dst, motionHolder.duration, currentPage);
                        }
                    }else if(motionHolder.mode==MODE_SLOWMOVE){
                        gotoPage(currentPage);
                    }
                    return true;
            }

            return super.onTouchEvent(motionHolder.motionEvent);
        }else {
            isHorizonMove = false;
            return super.onTouchEvent(motionHolder.motionEvent);
        }

    }


    private MotionEvent tempEv;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        needOffset = false;
        eventIndex++;
//        Log.d("test","main eventIndex: "+eventIndex);
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
//                Log.d("test","main down");
                lastDownPoint.set(ev.getX(),ev.getY());
                if(touchEventListener!=null){
                    MotionHolder mHolder = new MotionHolder();

                    mHolder.motionEvent = MotionEvent.obtain(ev);
                    mHolder.needOffset = needOffset;
                    touchEventListener.onTouchEventHappen(this,eventIndex,mHolder);
                }
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.d("test","main move");
                isHorizonMove = checkHorizonMove(ev,lastDownPoint);
                if(!isHorizonMove&&touchEventListener!=null){
                    MotionHolder cancelHolder = new MotionHolder();
                    cancelHolder.isCancel = true;
                    cancelHolder.motionEvent = MotionEvent.obtain(ev);
                    cancelHolder.motionEvent.setAction(MotionEvent.ACTION_CANCEL);
                    cancelHolder.needOffset = needOffset;
                    touchEventListener.onTouchEventHappen(this,eventIndex,cancelHolder);
                    tempEv = MotionEvent.obtain(ev);
                    tempEv.setAction(MotionEvent.ACTION_CANCEL);
                }
                break;
            case MotionEvent.ACTION_UP:
                isHorizonMove = true;
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        if(isHorizonMove){
            dealWithTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    private class MovementParam{
        public int duration;
        public int currentX;
        public int dst;
        public int currentPage;
        public int moveType;

    }
    private MovementParam movementParam = new MovementParam();
    private boolean isHorizonMove = true;
    private PointF lastDownPoint = new PointF();
    private void dealWithTouchEvent(MotionEvent ev){
        MotionHolder motionHolder = new MotionHolder();
        motionHolder.motionEvent = MotionEvent.obtain(ev);

        lastScrollX = getScrollX();
        if (pointList.size()!=subChildCount) {
            receiveChildInfo();
        }
        if (!pointList.isEmpty()) {
            initVelocityTrackerIfNotExists();
            mVelocityTracker.addMovement(ev);
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:

                    break;
                case MotionEvent.ACTION_UP:
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getXVelocity(-1);
                    if (Math.abs(initialVelocity)>mMinimumVelocity*3) {
                        movementParam.moveType = 0;
                        motionHolder.setMotionHolder(CustomFling(initialVelocity,movementParam));
                    }else {
                        movementParam.moveType = 1;
                        motionHolder.setMotionHolder(moveSlow(movementParam));

                    }
                    recycleVelocityTracker();
                    break;

                case MotionEvent.ACTION_CANCEL:
                    break;

            }
            if (touchEventListener!=null){
                motionHolder.needOffset = needOffset;
                touchEventListener.onTouchEventHappen(this,eventIndex,motionHolder);
            }
        }
    }

    private boolean checkHorizonMove(MotionEvent ev, PointF lastDownPoint) {
        if(ev==null||lastDownPoint==null)
            return false;
        if(ev.getX()!=lastDownPoint.x){
            float ytox = Math.abs((ev.getY()-lastDownPoint.y)/(ev.getX()-lastDownPoint.x));
            if(ytox<Math.tan(Math.toRadians(HORIZONTAL_DEG))){
                return true;
            }
        }
        return false;
    }

    @Override
         public boolean onTouchEvent(MotionEvent ev) {

                if (!pointList.isEmpty()) {
                    switch (ev.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if(!isHorizonMove&&tempEv.getAction()==MotionEvent.ACTION_CANCEL){
                                return super.onTouchEvent(tempEv);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if(!isHorizonMove){
                                return true;
                            }
                            if (movementParam.moveType==0) {

                                startFling(movementParam.currentX,movementParam.dst,movementParam.duration);

                            }else {
                                gotoPage(movementParam.currentPage);

                            }
                            return true;
                        case MotionEvent.ACTION_CANCEL:
                            break;

                    }
                }

                return super.onTouchEvent(ev);

         }


    private MotionHolder moveSlow(MovementParam mp){
        currentPage = setPage(getScrollX(), pointList, itemViewWidth);
//        Log.d("test","currentPage: "+currentPage);

        mp.currentPage = currentPage;
//        gotoPage(currentPage);

        return new MotionHolder(currentPage,-1,MODE_SLOWMOVE);
    }
         private void recycleVelocityTracker() {
             if (mVelocityTracker != null) {
                 mVelocityTracker.recycle();
                 mVelocityTracker = null;
             }
         }
         private double getSplineDeceleration(int velocity) {
             
             return Math.log(INFLEXION * Math.abs(velocity) / (mFlingFriction * mPhysicalCoeff));
         }
         
         private double getSplineFlingDistance(int velocity) {
             final double l = getSplineDeceleration(velocity);
             final double decelMinusOne = DECELERATION_RATE - 1.0;
             return mFlingFriction * mPhysicalCoeff * Math.exp(DECELERATION_RATE / decelMinusOne * l);
         }
         private MotionHolder CustomFling(int v,MovementParam mp) {
//             int dst_for_overscroll;
             boolean isOverscroll =false;
             int currentX = getScrollX();
             double distance = getSplineFlingDistance(v)/2f;
             int adjDst = 0;
             if (v<0) {
                 if (pointList.get(pointList.size()-1)<(distance+currentX)) {
                     adjDst = (int)distance;
                     isOverscroll = true;
//                     adjDst = Math.round(pointList.get(pointList.size()-1)-currentX);
                     currentPage = pointList.size()-1;

                 }else {
//                     Log.d("test","distance: "+distance+" currentX: "+currentX);
                     currentPage = setPage(distance+currentX,pointList,itemViewWidth);
//                     Log.d("test","currentPage: "+currentPage+" itemViewWidth: "+itemViewWidth);
                     adjDst = Math.abs(Math.round(currentPage*itemViewWidth-currentX));
                     if (adjDst<itemViewWidth) {
                         currentPage = currentPage+1<pointList.size()?currentPage+1:pointList.size()-1;
                         adjDst = Math.abs(Math.round(currentPage*itemViewWidth-currentX));
                     }
//                     Log.d("test","adjDst: "+adjDst);
                 }
             }else if(v>0){
                 if ((currentX-distance)<0) {
                     adjDst = (int)distance;
                     isOverscroll = true;
//                     adjDst = Math.round(currentX);
                     currentPage = 0;
                 }else {
                     currentPage = setPage(currentX-distance,pointList,itemViewWidth);
                     adjDst = Math.abs(Math.round(currentPage*itemViewWidth-currentX));

                     if (adjDst<itemViewWidth) {
                         currentPage = currentPage-1>=0?currentPage-1:0;
                         adjDst = Math.abs(Math.round(currentPage*itemViewWidth-currentX));
                     }
                 }
             }


             int duration;
             int dst = (int)-Math.signum(v)*adjDst;
             duration = (int)(Math.sqrt(2*Math.abs(dst)/ fling_Decelerat_factor));

             if (duration<DURATION_MIN){
                 duration = DURATION_MIN;
             }
//             resetOverscrollX(dst);
//             Log.d("test","dst: "+dst);
//             Log.d("test",pointList.toString());
//             Log.d("test","currentX: "+currentX+" dst: "+dst);
             mp.currentX = currentX;
             mp.dst = dst;
             mp.duration = duration;


//             startFling(currentX, dst, duration);

             if (isOverscroll){

                 mOverscrollDistance = resetOverscrollX(dst);
                 return new MotionHolder(currentPage,duration,MODE_FLING,dst,isOverscroll);
             }
             return new MotionHolder(currentPage,duration,MODE_FLING);
            
        }
    private int resetOverscrollX(int dst){
//        Log.d("test","dst: "+dst+" currentX: "+getScrollX());
//        Log.d("test",pointList.toString());
        int mOverscrollDistance = mMaxXOverscrollDistance;
        if(pointList!=null&&!pointList.isEmpty()) {

            int overdst;
            if (dst >= 0) {

                overdst = dst+getScrollX()-pointList.get(pointList.size()-1);

            } else {
                overdst = Math.abs(dst+getScrollX());

            }

            if (overdst>0&&overdst<= mMaxXOverscrollDistance) {
                mOverscrollDistance = (int)(overdst*0.8);
            }
//            Log.d("test","overdst: "+overdst+" mOverscrollDistance: "+mOverscrollDistance+" mMaxXOverscrollDistance: "+mMaxXOverscrollDistance);

        }

        return mOverscrollDistance;

    }
    private void startFling(int currentX,int dst,int duration){
        overScroller.startScroll(currentX, 0,dst, 0,duration);
        handler.sendMessageDelayed(handler.obtainMessage(), 5);
        postInvalidate();
    }
    public boolean gotoPage(int page){
        if (pointList.size()!=subChildCount) {
            receiveChildInfo();
        }
//        Log.d("test","subChildCount: "+subChildCount);
        if(page >=0 && page <= subChildCount - 1){
            handler.sendMessageDelayed(handler.obtainMessage(), 5);
//            Log.d("test", "pointList.get(page): " + pointList.get(page));
            smoothScrollTo(pointList.get(page), 0);
            currentPage = page;

            return true;
        }
        return false;
    }
    private int setPage(double endX, ArrayList<Integer> mPointList,int unit) {
            if (mPointList.size()<=1) {
                return 0;
            }else{
                int left = 0;
                int len = mPointList.size();
                int right = len-1;
                int mid;
                int res;
                while (left<=right) {
                    if (right-left==1){
                        return Math.abs(pointList.get(left)-endX)<=Math.abs(pointList.get(right)-endX)?left:right;
                    }
                    mid = (left+right)/2;
                    if (mPointList.get(mid)-endX==0) {
                        return mid;
                    }else if (mPointList.get(mid)-endX>unit) {
                        right = mid;

                    }else if(mPointList.get(mid)-endX<-unit){
                        left = mid;

                    }else {

                        if (mPointList.get(mid)-endX>0) {
                            res = mPointList.get(mid)-endX<=unit/2?mid:mid-1;
                            if (res<0) {
                                res = 0;
                            }
                        }else {
                            res =  mPointList.get(mid)-endX>=-unit/2?mid:mid+1;
                            if (res>=mPointList.size()) {
                                res = mPointList.size()-1;
                            }
                        }
                        return res;
                    }
                }
            }
                
            return 0;
        }

        public void flingOverScroll(int dst,int durationPassIn,int page){
            if (pointList.size()!=subChildCount) {
                receiveChildInfo();
            }
            int currentX = getScrollX();
            int duration;
            if (durationPassIn==-1){
                duration = (int)(Math.sqrt(2*Math.abs(dst))/ fling_Decelerat_factor);
            }else{
                duration = durationPassIn;
            }
            if (duration<DURATION_MIN){
                duration = DURATION_MIN;
            }
            currentPage = page;

            mOverscrollDistance = resetOverscrollX(dst);
//            Log.d("test","currentX>>  "+currentX+"  dst>>  "+dst+" mOverscrollDistance>  "+mOverscrollDistance);
            startFling(currentX, dst, duration);

        }
        public void flingToPage(int page,int durationPassIn){
            if (pointList.size()!=subChildCount) {
                receiveChildInfo();
            }
            if (page>=0&&page<=subChildCount-1){
                int duration;
                int currentX = getScrollX();
                int dst = pointList.get(page)-currentX;
                if (durationPassIn==-1){
                    duration = (int)(Math.sqrt(2*Math.abs(dst))/ fling_Decelerat_factor);
                }else{
                    duration = durationPassIn;
                }
                if (duration<DURATION_MIN){
                    duration = DURATION_MIN;
                }
                currentPage = page;
//                duration = (int)(duration*time_factor);
                startFling(currentX,dst,duration);
            }
        }

        

    public interface OnScrollListener{

        public void onScroll(int scrollX, int currentItem);
        public void onStop(int scrollX, int currentItem);

    }

}


