package com.letv.shared.widget;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;

/**
 * Created by liangchao on 14-11-10.
 */
public class LePullToSearchListView extends LeListView implements AbsListView.OnScrollListener{

    private int scrollState = SCROLL_STATE_IDLE;
    private final static int DURATION_DEFALT = 150;
    // 
    private int RATIO = 3;
    // 
    private ViewGroup headerView;
    private float showTrigger = 0.75f;

    // 
    private int headerContentHeight;

    private int startY;
    private PullToSearchListener listener;



    private boolean isShowHeader = false;
    private ValueAnimator animator = null;

    public int getRATIO() {
        return RATIO;
    }

    public void setRATIO(int RATIO) {
        this.RATIO = RATIO;
    }

    public float getShowTrigger() {
        return showTrigger;
    }

    public void setShowTrigger(float showTrigger) {
        this.showTrigger = showTrigger;
    }

    public ViewGroup getHeaderView() {
        return headerView;
    }
    public boolean isReleaseToShow() {
        return releaseToShow;
    }

    private boolean releaseToShow;


    public boolean isShowHeader() {
        return isShowHeader;
    }


    public void setListener(PullToSearchListener listener) {
        this.listener = listener;
    }

    public LePullToSearchListView(Context context) {
        super(context);
        init(context);
    }

    public LePullToSearchListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setOnScrollListener(this);
    }


    public void setHeaderView(View v){
        measureView(v);
        headerContentHeight = v.getMeasuredHeight();

        // 
        v.setPadding(0, -headerContentHeight, 0, 0);
        // 
        v.invalidate();
        // 
        addHeaderView(v, null, false);
        headerView = (ViewGroup)v;
    }
    private void resetTopPadding(int padding){
        headerView.setPadding(0,padding,0,0);
        invalidate();
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = (int) ev.getY();// 
                break;
            case MotionEvent.ACTION_UP://do not handle fling state

                if (!isShowHeader){
                    if (!releaseToShow){
                        if(headerView.getBottom()>0){
                            if (scrollState==SCROLL_STATE_IDLE||scrollState==SCROLL_STATE_FLING){
                                doAnimation(headerView.getPaddingTop(),-headerContentHeight,isShowHeader);
                            }else {
                                scrollState = SCROLL_STATE_FLING;
                                smoothScrollToPositionFromTop(1,0,DURATION_DEFALT);
                            }
                        }
                    }else {
                        isShowHeader = true;
                        doAnimation(headerView.getPaddingTop(),0,isShowHeader);

                    }
                }else{
                    if (releaseToShow){

                        doAnimation(headerView.getPaddingTop(),0,isShowHeader);
                        scrollState = SCROLL_STATE_FLING;
                        smoothScrollToPosition(0);

                    }else{
                        isShowHeader = false;
                        if (headerView.getBottom()>0){

                            if (scrollState==SCROLL_STATE_IDLE||scrollState==SCROLL_STATE_FLING){
                                doAnimation(headerView.getPaddingTop(),-headerContentHeight,isShowHeader);
                            }else{
                                scrollState = SCROLL_STATE_FLING;
                                smoothScrollToPositionFromTop(1,0,DURATION_DEFALT);
                            }
                        }
                    }
                }


                break;

            case MotionEvent.ACTION_MOVE:
                int tempY = (int) ev.getY();
                if (!isShowHeader){
                    if (tempY>startY){//pull down
                        if(getFirstVisiblePosition()==0){
                            resetTopPadding(-headerContentHeight + (tempY - startY) / RATIO);
                            if(headerView.getBottom()>=headerContentHeight*showTrigger) {
                                releaseToShow = true;
                            }else {
                                releaseToShow = false;
                            }
                            if (listener!=null){
                                listener.ReleaseToShowStateChange(releaseToShow);
                            }
                        }

                    }else{
                        if(headerView.getPaddingTop()!=-headerContentHeight){
                            resetTopPadding(-headerContentHeight);
                        }

                    }

                }else{
                    if (tempY>startY){//pull down
                        resetTopPadding((tempY-startY)/RATIO);
                    }else{//pull up
                        resetTopPadding((tempY-startY)/RATIO);

                        if (getFirstVisiblePosition()==0){
                            if(headerView.getBottom()>=headerContentHeight*showTrigger) {
                                releaseToShow = true;
                            }else {
                                releaseToShow = false;
                            }

                        }else{
                            resetTopPadding(-headerContentHeight);
                            isShowHeader = false;
                            if (listener!=null){
                                listener.hideSearchBar();
                            }
                        }
                    }
                }
                break;


            default:
                break;
        }
        return super.onTouchEvent(ev);
    }



    private void doAnimation(int start,int end, final boolean isShowHeader) {
        if (animator==null){


            animator = ValueAnimator.ofInt(start, end);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(DURATION_DEFALT);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (listener!=null){
                        if (isShowHeader){
                            listener.showSearchBar();
                        }else{
                            listener.hideSearchBar();
                        }
                    }

                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    resetTopPadding((Integer)animator.getAnimatedValue());

                }
            });
            animator.start();
        }else {

            animator.setIntValues(start,end);
            animator.start();
        }

    }

    // 
    private void measureView(View child) {
        ViewGroup.LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0,
                params.width);
        int lpHeight = params.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
                    MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (this.scrollState==SCROLL_STATE_FLING&&scrollState==SCROLL_STATE_IDLE){
            if (listener!=null){
                if(!isShowHeader){
                    listener.hideSearchBar();

                }else{
                    listener.showSearchBar();
                }
            }
            this.scrollState = scrollState;
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        
        if (scrollState==SCROLL_STATE_FLING&&headerView.getBottom()>0){
            if (headerView.getBottom()<=0){
                headerView.setPadding(0,-headerContentHeight,0,0);
                isShowHeader = false;
                if(listener!=null){
                    listener.hideSearchBar();
                }
            }
        }
    }


    public interface PullToSearchListener{
        public void ReleaseToShowStateChange(boolean releaseToShow);
        public void showSearchBar();
        public void hideSearchBar();
    }

}
