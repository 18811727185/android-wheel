package com.letv.shared.widget;


/**
 * Created by liangchao on 14-11-3.
 */
public interface TouchEventListener {
    public void onTouchEventHappen(LeGalleryLikeHorizontalScrollView view, int eventIndex, MotionHolder motionHolder);
    public void registerView(LeGalleryLikeHorizontalScrollView view);
}
