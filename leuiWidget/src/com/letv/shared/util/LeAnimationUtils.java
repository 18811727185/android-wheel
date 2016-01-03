package com.letv.shared.util;

import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.widget.ImageView;

import java.security.InvalidParameterException;

/**
 * Created by dongshangyong on 14-7-29.
 */
public class LeAnimationUtils {

    private static Handler mAnimationHandler = new Handler();

    /**
     * Method using to show a AnimationDrawable Image and do something after the animation finish.
     *
     * @param imageView View to show the Image
     * @param animationDrawableResId AnimationDrawable resource id
     * @param listener listener to listen to the animation finish.
     *
     */
    public static void play(ImageView imageView, int animationDrawableResId, final AnimationListener listener) {
        if (imageView == null) {
            return;
        }

        int duration = 0;
        final Resources res = imageView.getResources();
        Drawable d = res.getDrawable(animationDrawableResId);
        if (!(d instanceof AnimationDrawable)) {
            throw new InvalidParameterException(
                    "You should pass a AnimationDrawable Resource Id to paramater animationDrawableResId.");
        }

        AnimationDrawable drawable = (AnimationDrawable) d;
        for (int i = 0; i < drawable.getNumberOfFrames(); i++) {
            duration += drawable.getDuration(i);
        }

        imageView.setImageDrawable(drawable);
        drawable.start();
        mAnimationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onFinish();
                }
            }
        }, duration);
    }

    public interface AnimationListener {
        public void onFinish();
    }
}
