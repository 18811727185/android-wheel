package com.letv.shared.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

/**
 * Simple example of ScriptIntrinsicBlur Renderscript gaussion blur. In
 * production always use this algorithm as it is the fastest on Android.
 */
public class RenderScriptGaussianBlur {
    // constant blur radius which is supported by ScriptIntrinsicBlur class
    private static final int MAX_BLUR_RADIUS = 25;
    
    private RenderScript rs;

    public RenderScriptGaussianBlur(RenderScript rs) {
        this.rs = rs;
    }

    public Bitmap blur(int radius, Bitmap bitmapOriginal) {
        if (radius < 0 || bitmapOriginal == null) {
            return bitmapOriginal;
        }
        
        Bitmap ret;
        int width = bitmapOriginal.getWidth();
        int height = bitmapOriginal.getHeight();
        Bitmap.Config config = bitmapOriginal.getConfig();
        
        if (bitmapOriginal.isMutable()) {
            ret = bitmapOriginal;
        } else {
            ret = Bitmap.createBitmap(width, height, config);      
        }
        
        if (radius <= MAX_BLUR_RADIUS) {
            if (ret != bitmapOriginal) {
                Canvas canvas = new Canvas();
                canvas.setBitmap(ret);
                canvas.drawBitmap(bitmapOriginal, new Matrix(), null);
            }
            return blur2(radius, ret);
        } else  {
            
            int n = Math.round(radius / MAX_BLUR_RADIUS + 0.5f);
            float bitmapScaleFactor = 1f/n;

            int smallWidth = width / n;
            int smallHeight = height / n;
            
            
            Bitmap smallBitmap = Bitmap.createBitmap(smallWidth, smallHeight, config);
            Canvas canvas = new Canvas();
            canvas.setBitmap(smallBitmap);
            
            Matrix matrixScale = new Matrix();
            Matrix matrixScaleInv = new Matrix();
            matrixScale.setScale((float)smallWidth/width, (float)smallHeight/height);
            matrixScale.invert(matrixScaleInv);
            
            canvas.drawBitmap(bitmapOriginal, matrixScale, null);
            
            int resizedRadius = Math.round(radius * bitmapScaleFactor + 0.5f);
            blur2(resizedRadius, smallBitmap);
            
            Canvas canvas2 = new Canvas();
            canvas2.setBitmap(ret);
            canvas2.drawBitmap(smallBitmap, matrixScaleInv, null);
            
            smallBitmap.recycle();
            
            return ret;
        }
    }
    
    public Bitmap blur2(int radius, Bitmap bitmapOriginal) {
        if (radius > MAX_BLUR_RADIUS) {
            radius = MAX_BLUR_RADIUS;
        } else if (radius < 1) {
            radius = 1;
        }
        
        final Allocation input = Allocation.createFromBitmap(rs, bitmapOriginal);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bitmapOriginal);
        
        if (input != null) {
            input.destroy();
        }
        
        if (output != null) {
            output.destroy();
        }
        
        if (script != null) {
            script.destroy();
        }
        
        return bitmapOriginal;
    }
}
