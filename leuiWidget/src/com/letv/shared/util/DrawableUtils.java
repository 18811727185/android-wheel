package com.letv.shared.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.PictureDrawable;
import android.graphics.drawable.RotateDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import com.letv.shared.R;
/**
 * Created by wangziming on 14-9-26.
 */
public class DrawableUtils {
    
    private final static int DEFAULT_SHADOW_RADIUS = 4;
    private final static int DEFAULT_STROKE_RADIUS = 1;
    private final static int DEFAULT_SHADOW_OFFSETY = 2;
    private final static int STROKE_ALPHA_VALUE = 77;
    private final static int SHADOW_ALPHA_VALUE = 77;

    /**
     * OriginalDrawable transforms into a drawable with shadow
     * 
     * @param originalDrawable
     * @param resources
     * @return a drawable with shadow
     */
    public static Drawable createShadowDrawable(Drawable originalDrawable, Resources resources) {
        return createShadowDrawable(originalDrawable, resources, 
                DEFAULT_STROKE_RADIUS, STROKE_ALPHA_VALUE,
                DEFAULT_SHADOW_RADIUS, SHADOW_ALPHA_VALUE, 
                0, DEFAULT_SHADOW_OFFSETY);
    }
    
    /**
     * OriginalDrawable transforms into a drawable with shadow, You can set the effect.
     * 
     * @param originalDrawable 
     * @param resources
     * @param strokeRadius the stroke of the default radius.
     * @param strokeAlpha the stroke of the default alpha.
     * @param shadowRadius the shadow of the default radius.
     * @param shadowAlpha the shadow of the default alpha.
     * @param offsetX The shadow of the default in the x direction of the offset value.
     * @param offsetY The shadow of the default in the y direction of the offset value.
     * @return a drawable with shadow
     */
    public static Drawable createShadowDrawable(Drawable originalDrawable, Resources resources, 
            final int strokeRadius, final int strokeAlpha,
            final int shadowRadius, final int shadowAlpha, 
            final int offsetX, final int offsetY) {
        Bitmap mapBitmap = drawable2Bitmap(originalDrawable);
        BitmapDrawable newDrawable = null;
        if (mapBitmap != null) {
            int createWidth =  shadowRadius * 2 + originalDrawable.getIntrinsicWidth();
            int createHeight = shadowRadius * 2 + originalDrawable.getIntrinsicHeight();
            Bitmap createBitmap = Bitmap.createBitmap(createWidth, createHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(createBitmap);
            Paint bitmapPaint = new Paint();
            Paint paint = new Paint();
            int color = resources.getColor(R.color.le_drawable_shadow);
            paint.setColor(color);
            int offsetXY[] = new int[2];
            
            BlurMaskFilter blurFilterStroke = new BlurMaskFilter(strokeRadius, BlurMaskFilter.Blur.NORMAL);// stroke_effect
            paint.setMaskFilter(blurFilterStroke);
            Bitmap strokeBitmap = mapBitmap.extractAlpha(paint, offsetXY);
            bitmapPaint.setAlpha(strokeAlpha);
            canvas.drawBitmap(strokeBitmap, (createWidth - strokeBitmap.getWidth()) >> 1,
                                            ((createHeight - strokeBitmap.getHeight()) >> 1) - offsetY, bitmapPaint);
            
            BlurMaskFilter blurFilterOutter = new BlurMaskFilter(shadowRadius, BlurMaskFilter.Blur.NORMAL);// shadow_effect
            paint.setMaskFilter(blurFilterOutter);
            Bitmap shadowBitmap = mapBitmap.extractAlpha(paint, offsetXY);
            bitmapPaint.setAlpha(shadowAlpha);
            canvas.drawBitmap(shadowBitmap, (createWidth - shadowBitmap.getWidth()) >> 1,
                                            (createHeight - shadowBitmap.getHeight()) >> 1, bitmapPaint);
            
            canvas.drawBitmap(mapBitmap, ((createWidth - mapBitmap.getWidth()) >> 1) - offsetX,
                                         ((createHeight - mapBitmap.getHeight()) >> 1) - offsetY, new Paint());
            newDrawable = new BitmapDrawable(resources, createBitmap);
            newDrawable.setBounds(0, 0, createWidth, createHeight);
        }
        return newDrawable == null ? originalDrawable : newDrawable;
    }

    // Drawable transforms into Bitmap
    private static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable ||
                drawable instanceof StateListDrawable ||
                drawable instanceof GradientDrawable ||
                drawable instanceof InsetDrawable || // 表示一个drawable嵌入到另外一个drawable内部
                drawable instanceof LayerDrawable || // 可以将多个图片按照顺序层叠起来
                drawable instanceof LevelListDrawable || // 有一组交替的drawable资源
                drawable instanceof PaintDrawable ||
                drawable instanceof PictureDrawable ||
                drawable instanceof RotateDrawable ||
                drawable instanceof ScaleDrawable ||
                drawable instanceof ShapeDrawable ||
                drawable instanceof ClipDrawable) {
            if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                return null;// Ensure that can create shadow Bitmap.
            }
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ?
                            Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        // Include: AnimatedRotateDrawable,AnimationDrawable,TransitionDrawable和ColorDrawable
        return null;
    }
    
}
