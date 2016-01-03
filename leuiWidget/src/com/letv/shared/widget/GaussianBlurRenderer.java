/*
   Copyright 2013 Harri Smatt

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.letv.shared.widget;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import java.text.DecimalFormat;

/**
 * Class for handling RenderScript based blur
 */
public class GaussianBlurRenderer {
    private static final String TAG = "BlurRenderer";

    private boolean mLogTime = false;
    
    // constant blur radius which is supported by ScriptIntrinsicBlur class
    private static final float MAX_BLUR_RADIUS = 25f;

    // Constant used for scaling the off-screen bitmap
    private static final float MIN_BITMAP_SCALE_FACTOR = 0.01f;
    private static final float MAX_BITMAP_SCALE_FACTOR = 0.25f;
    private float mBitmapScaleFactor = 0.25f;

    private View mView;
    private Canvas mCanvas;
    private Matrix mMatrixScale;
    private Matrix mMatrixScaleInv;
    
    private int[] mLocationInWindow;
    private Bitmap mBitmap;
    
    private boolean mBlurEnabled = true;
    private int mBlurRadius;

    private RenderScript mRS;
    private ScriptIntrinsicBlur mScript;
    private Allocation mInputAllocation;
    private Allocation mOutputAllocation;
    
    private View mAfterView; 
    private int[] mLocInScreenOfAfterView;
    private int[] mLocInScreenOfView;

    public GaussianBlurRenderer(View view, View afterView) {
        this(view);
        mAfterView = afterView;
    }
    
    /**
     * Default constructor
     */
    public GaussianBlurRenderer(View view) {
        mView = view;
    }
    
    private void init() {
        mCanvas = new Canvas();
        // mRectVisibleGlobal = new Rect();
        mLocationInWindow = new int[2];
        
        mLocInScreenOfAfterView = new int[2];
        mLocInScreenOfView = new int[2];

        // Prepare matrices for scaling up/down the off-screen bitmap
        mMatrixScale = new Matrix();
        mMatrixScaleInv = new Matrix();

        // RenderScript related variables
        mRS = RenderScript.create(mView.getContext());
        mScript = ScriptIntrinsicBlur.create(mRS, Element.U8_4(mRS));
    }
    
    private void recycle() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        
        if (mInputAllocation != null) {
            mInputAllocation.destroy();
            mInputAllocation = null;
        }
        
        if (mOutputAllocation != null) {
            mOutputAllocation.destroy();
            mOutputAllocation = null;
        }
        
        if (mScript != null) {
            mScript.destroy();
            mScript = null;
        }
        
        if (mRS != null) {
            mRS.destroy();
            mRS = null;
        }
        
        mCanvas = null;
        mLocationInWindow = null;
        mLocInScreenOfAfterView = null;
        mLocInScreenOfView = null;
        mMatrixScale = null;
        mMatrixScaleInv = null;
    }
    
    public void setBitmapScaleFactor(float scaleFactor) {
        if (scaleFactor < MIN_BITMAP_SCALE_FACTOR) {
            scaleFactor = MIN_BITMAP_SCALE_FACTOR;
        } else if (scaleFactor > MAX_BITMAP_SCALE_FACTOR) {
            scaleFactor = MAX_BITMAP_SCALE_FACTOR;
        }
        
        mBitmapScaleFactor = scaleFactor;
    }
    
    public boolean getBlurEnabled() {
        return mBlurEnabled;
    }
    
    public void setBlurEnabled(boolean blurEnabled) {
        mBlurEnabled = blurEnabled;
    }

    /**
     * Must be called from owning View.onAttachedToWindow
     */
    public void onAttachedToWindow() {
        init();
        
        // Start listening to onDraw calls
        mView.getViewTreeObserver().addOnPreDrawListener(onPreDrawListener);
    }

    /**
     * Must be called from owning View.onDetachedFromWindow
     */
    @SuppressLint("MissingSuperCall")
    public void onDetachedFromWindow() {
        // Remove listener
        mView.getViewTreeObserver().removeOnPreDrawListener(onPreDrawListener);
        
        recycle();
    }
    
    private void adjustBitmapScaleFactor(int radius) {
        if (radius <= (int)(MAX_BLUR_RADIUS / MAX_BITMAP_SCALE_FACTOR)) {
            mBitmapScaleFactor = MAX_BITMAP_SCALE_FACTOR;
        } else {
            int n = Math.round(radius / MAX_BLUR_RADIUS + 0.5f);
            mBitmapScaleFactor = 1f/n;
        }
    }

    /**
     * Set blur radius in screen pixels. Value is mapped in range [1, 254].
     */
    public void setBlurRadius(int radius) {
        if (radius < 1) {
            return;
        }
        
        Log.d(TAG, "radius = " + radius);
        
        adjustBitmapScaleFactor(radius);
        
        // Map radius into scaled down off-screen bitmap size
        radius = Math.round(radius * mBitmapScaleFactor + 0.5f);
   
        if (radius > 25) {
            radius = 25;
        } 
        
        mBlurRadius = radius;
        
        if (mScript != null) {
            mScript.setRadius(radius);
        }
    }
    
    public int getBlurRadius() {
        return mBlurRadius;
    }

    /**
     * Returns true if this draw call originates from this class and is meant to
     * be an off-screen drawing pass.
     */
    public boolean isOffscreenCanvas(Canvas canvas) {
        return canvas == mCanvas;
    }

    /**
     * Applies blur to current off-screen bitmap
     */
    public void applyBlur() {
        long startTime = 0l, endTime = 0l;

        if (mLogTime) {
            startTime = System.nanoTime();
        }

        // Copy current bitmap into allocation
        mInputAllocation.copyFrom(mBitmap);

        mScript.setInput(mInputAllocation);
        mScript.forEach(mOutputAllocation);
        
        // Copy bitmap allocation back to off-screen bitmap
        mOutputAllocation.copyTo(mBitmap);

        if (mLogTime) {
            endTime = System.nanoTime();
            double time = (endTime - startTime) / 1000000.0;
            DecimalFormat df = new DecimalFormat("0.00");

            Log.d(TAG, "pic width = " + mBitmap.getWidth() +
                    "px, height = " + mBitmap.getHeight() +
                    "px, blur time: " + df.format(time) + " milliseconds");
        }
    }

    /**
     * Draws off-screen bitmap into current canvas
     */
    public void drawToCanvas(Canvas canvas) {
        if (mBlurEnabled && mBitmap != null) {
            // Draw off-screen bitmap using inverse of the scale matrix
            canvas.drawBitmap(mBitmap, mMatrixScaleInv, null);
        }
    }

    /**
     * Private method for grabbing a "screenshot" of screen content
     */
    private void drawOffscreenBitmap() {
        long startTime = 0l, endTime = 0l;

        if (mLogTime) {
            startTime = System.nanoTime();
        }

        // Grab global visible rect for later use
        // mView.getGlobalVisibleRect(mRectVisibleGlobal);

        // Calculate scaled off-screen bitmap width and height
        int width = Math.round(mView.getWidth() * mBitmapScaleFactor);
        int height = Math.round(mView.getHeight() * mBitmapScaleFactor);

        // This is added due to RenderScript limitations I faced.
        // If bitmap width is not multiple of 4 - in RenderScript
        // index = y * width
        // does not calculate correct index for line start index.
        //width = width & ~0x03;

        // Width and height must be > 0
        width = Math.max(width, 1);
        height = Math.max(height, 1);

        // Allocate new off-screen bitmap only when needed
        if (mBitmap == null || mBitmap.getWidth() != width
                || mBitmap.getHeight() != height) {
            if (mBitmap != null) {
                mBitmap.recycle();
            }
            
            mBitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);

            mInputAllocation = Allocation.createFromBitmap(mRS, mBitmap);
            mOutputAllocation = Allocation.createTyped(mRS, mInputAllocation.getType());
            
            // Due to adjusting width into multiple of 4 calculate scale matrix
            // only here
            mMatrixScale.setScale((float) width / mView.getWidth(),
                    (float) height / mView.getHeight());
            mMatrixScale.invert(mMatrixScaleInv);
        }

        
        if (mAfterView != null) {
            mAfterView.getLocationOnScreen(mLocInScreenOfAfterView);
            mView.getLocationOnScreen(mLocInScreenOfView);

            mAfterView.getLocationInWindow(mLocationInWindow);
            mLocationInWindow[0] = mLocationInWindow[0]
                    + (mLocInScreenOfView[0] - mLocInScreenOfAfterView[0]);
            mLocationInWindow[1] = mLocationInWindow[1]
                    + (mLocInScreenOfView[1] - mLocInScreenOfAfterView[1]);

        } else {
            mView.getLocationInWindow(mLocationInWindow);
        }
        
        // Restore canvas to its original state
        mCanvas.restoreToCount(1);
        mCanvas.setBitmap(mBitmap);
        // Using scale matrix will make draw call to match
        // resized off-screen bitmap size
        mCanvas.setMatrix(mMatrixScale);
        // Off-screen bitmap does not cover the whole screen
        // Use canvas translate to match its position on screen
        mCanvas.translate(-mLocationInWindow[0], -mLocationInWindow[1]);
        // Clip rect is the same as we have
        // TODO: Why does this not work on API 18?
        // mCanvas.clipRect(mRectVisibleGlobal);
        // Save current canvas state
        mCanvas.save();

        // Start drawing from the root view
        if (mAfterView != null) {
            mAfterView.getRootView().draw(mCanvas);

            int[] locationInOwnWindow = new int[2];
            mView.getLocationInWindow(locationInOwnWindow);

            mCanvas.restoreToCount(1);
            mCanvas.translate(mLocationInWindow[0] - locationInOwnWindow[0],
                    mLocationInWindow[1] - locationInOwnWindow[1]);
            mCanvas.save();
            
            applyBlur();
        } else {
        
            mView.getRootView().draw(mCanvas);
        }

        if (mLogTime) {
            endTime = System.nanoTime();
            double time = (endTime - startTime) / 1000000.0;
            DecimalFormat df = new DecimalFormat("0.00");

            Log.d(TAG, "take background consumes: " + df.format(time) + " milliseconds");
        }
    }

    /**
     * Listener for receiving onPreDraw calls from underlying ui
     */
    private final ViewTreeObserver.OnPreDrawListener onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            // Only care if View we are doing work for is visible
            if (mBlurEnabled && mView.getVisibility() == View.VISIBLE) {
                drawOffscreenBitmap();
            }
            return true;
        }
    };
    
    public void setBlurAfterView(View view) {
        mAfterView = view;
    }

    public View getBlurAfterView() {
        return mAfterView;
    }

}
