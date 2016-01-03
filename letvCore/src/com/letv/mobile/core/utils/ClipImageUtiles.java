package com.letv.mobile.core.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;

public class ClipImageUtiles {

    /**
     * 创建镜像图片
     * @param originalImage
     */
    public static Bitmap createMirrorImage(Bitmap originalImage, int width,
            int height, boolean mirrorMode, int mirrorHeight, int imageGap,
            int borderWidth, int cornerWidth, int strokeWidth,
            Bitmap highlightDrawable) {

        if (originalImage == null) {
            return null;
        }
        /*
         * if(width==0){ width = getLayoutParams().width; }
         * if(height==0){ height = getLayoutParams().height; }
         */
        int imageWidth = originalImage.getWidth();
        int imageHeight = originalImage.getHeight();

        // 镜像效果
        Matrix matrix = new Matrix();
        float matrix_values[] = { 1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f };
        matrix.setValues(matrix_values);

        if (mirrorHeight > height || mirrorHeight < 0) {
            mirrorHeight = height;
        }
        if (!mirrorMode) {
            mirrorHeight = 0;
            imageGap = 0;
        }

        Bitmap bitmap;
        try {
            bitmap = createClipImage(originalImage, width, height, mirrorMode,
                    mirrorHeight, imageGap, borderWidth, cornerWidth,
                    strokeWidth, highlightDrawable, imageWidth, imageHeight,
                    matrix);
        } catch (Exception e) {
            bitmap = originalImage;
            System.out.println("Clip Image OutOfMemory:" + e.toString());
        }
        return bitmap;
    }

    private static Bitmap createClipImage(Bitmap originalImage, int width,
            int height, boolean mirrorMode, int mirrorHeight, int imageGap,
            int borderWidth, int cornerWidth, int strokeWidth,
            Bitmap highlightDrawable, int imageWidth, int imageHeight,
            Matrix matrix) {
        // 合成图片部分
        Bitmap bitmap;
        if (imageWidth < 500) {
            bitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
        } else {
            bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
        Paint paint = new Paint();
        RectF rect;
        // 画笔设置阴影
        if (borderWidth > 0) {
            paint.setAntiAlias(true);
            paint.setShadowLayer(borderWidth, 0.0f, 0.0f, Color.BLACK);
            paint.setStyle(Style.STROKE);
            rect = new RectF(borderWidth, borderWidth, width - borderWidth,
                    height - borderWidth - mirrorHeight);
            canvas.drawRoundRect(rect, cornerWidth, cornerWidth, paint);
        }

        paint.reset();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        rect = new RectF(borderWidth, borderWidth, width - borderWidth, height
                - borderWidth - mirrorHeight);
        canvas.drawRoundRect(rect, cornerWidth, cornerWidth, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        // 画原图
        Rect dest = new Rect(borderWidth, borderWidth, width - borderWidth,
                height - borderWidth - mirrorHeight);
        canvas.drawBitmap(originalImage, null, dest, paint);

        // 画笔设置描边
        if (strokeWidth > 0) {
            paint.setColor(Color.parseColor("#b4ffffff"));
            paint.setAntiAlias(true);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
            rect = new RectF(borderWidth, borderWidth, width - borderWidth,
                    height - borderWidth - mirrorHeight);
            canvas.drawRoundRect(rect, cornerWidth, cornerWidth, paint);
        }
        // 画间隔线
        if (imageGap > 0) {
            paint.reset();
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawRect(borderWidth + strokeWidth, height - borderWidth
                    - mirrorHeight - strokeWidth, width - borderWidth, height
                    + imageGap - borderWidth - mirrorHeight, new Paint());
        }

        // 画抛光
        if (highlightDrawable != null) {
            paint.reset();
            dest = new Rect(borderWidth, borderWidth,
                    highlightDrawable.getWidth(), highlightDrawable.getHeight());
            if (!highlightDrawable.isRecycled()) {
                canvas.drawBitmap(highlightDrawable, null, dest, paint);
            }
        }

        if (mirrorMode) {
            paint.reset();
            int h = imageHeight * mirrorHeight
                    / (height - 2 * borderWidth - mirrorHeight);
            // 镜像图片部分
            Bitmap mirrorImage = Bitmap.createBitmap(originalImage, 0,
                    imageHeight - h, imageWidth, h, matrix, false);
            // 画镜像
            dest = new Rect(borderWidth, height - borderWidth - mirrorHeight
                    + imageGap, width - borderWidth, height);
            canvas.drawBitmap(mirrorImage, null, dest, paint);
            if (mirrorImage != null) {
                mirrorImage.recycle();
                mirrorImage = null;
            }
            // 创建一个渐变的蒙版放在镜像图片上
            LinearGradient shader;
            shader = new LinearGradient(0, height - borderWidth - mirrorHeight
                    + imageGap, 0, height, 0x80ffffff, 0x00ffffff,
                    TileMode.CLAMP);

            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
            canvas.drawRect(0, height - borderWidth - mirrorHeight + imageGap,
                    width, height, paint);
        }

        canvas.setBitmap(null);
        return bitmap;
    }

}
