package com.letv.shared.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import com.letv.shared.R;
import com.letv.shared.util.LeReflectionUtils;

/**
 * Created by snile on 9/23/14.
 */
public class LeProgressBar extends ProgressBar {

    private String LOG_TAG = LeProgressBar.class.getSimpleName();

    private boolean DEBUG = true;

    private int loadingColor;
    private int loadingZebra;
    private int progressColor;
    private int progressBackgroundColor;

    //zhangyd add for port ,as the padding is protected but was hide by android
    protected int mPaddingLeft = 0;
    protected int mPaddingRight = 0;
    protected int mPaddingTop = 0;
    protected int mPaddingBottom = 0;
    //zhangyd add end
    private int minWidth;
    private int maxWidth;
    private int minHeight;
    private int maxHeight;

    private boolean isPause = false;
    private boolean onlyIndeterminate;
    private boolean isCopyIphone = false;

    private int copyIphoneDividerWidth;

    private Paint iphonePaint;

    private static final int CIRCLE_TYPE=0;
    private static final int HORIZONTAL_TYPE=1;

    private int type = CIRCLE_TYPE;


    ShapeDrawable shapeDrawable ;
    ShapeDrawable progressShapeDrawable ;
    ShapeDrawable backgroundProgressShapeDrawable ;


    ValueAnimator animator;

    int horizontalProgressHeight ;

    int zebraLength ;

    int roundRadius;

    int circleRadius;

    int offset = 0 ;

    private RectF oval = new RectF();

    boolean isAnimated = false;

    BitmapShader shader;

    public LeProgressBar(Context context) {
        this(context, null);
    }

    public LeProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeProgressBar(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public LeProgressBar(Context context, AttributeSet attrs, int defStyle, int styleRes) {
        super(context, attrs, defStyle, styleRes);
        TypedArray t = context.obtainStyledAttributes(attrs,R.styleable.LeProgressBar,defStyle,styleRes);

        progressColor = t.getColor(R.styleable.LeProgressBar_le_progress_color,context.getResources().getColor(R.color.le_progress_bar_progress_color));
        loadingColor = t.getColor(R.styleable.LeProgressBar_le_progress_loading_color,context.getResources().getColor(R.color.le_progress_bar_loading_color));
        progressBackgroundColor = t.getColor(R.styleable.LeProgressBar_le_progress_background_color,context.getResources().getColor(R.color.le_progress_bar_background_color));
        loadingZebra = t.getColor(R.styleable.LeProgressBar_le_progress_zebra_color,context.getResources().getColor(R.color.le_progress_bar_zebra_color));

        type = t.getInteger(R.styleable.LeProgressBar_le_type,CIRCLE_TYPE);
        horizontalProgressHeight = t.getDimensionPixelSize(R.styleable.LeProgressBar_le_progress_bar_height,9);
        zebraLength = t.getDimensionPixelSize(R.styleable.LeProgressBar_le_zebra_length,30);
        roundRadius =t.getDimensionPixelSize(R.styleable.LeProgressBar_le_progress_round_radius,5);
        circleRadius = t.getDimensionPixelSize(R.styleable.LeProgressBar_le_progress_circle_radius,50);
        isCopyIphone = t.getBoolean(R.styleable.LeProgressBar_le_copy_iphone, false);
        copyIphoneDividerWidth = t.getDimensionPixelSize(R.styleable.LeProgressBar_le_copy_iphone_divider_width,3);

        t.recycle();

        onlyIndeterminate = (Boolean)LeReflectionUtils.getFieldValue(this,"mOnlyIndeterminate");
        maxWidth = (Integer)LeReflectionUtils.getFieldValue(this,"mMaxWidth");
        maxHeight = (Integer)LeReflectionUtils.getFieldValue(this,"mMaxHeight");
        minWidth =(Integer)LeReflectionUtils.getFieldValue(this,"mMinWidth");
        minHeight =(Integer)LeReflectionUtils.getFieldValue(this,"mMinHeight");



        initAnimator();

        shapeDrawable = new ShapeDrawable(createRoundRectShape(roundRadius));
        backgroundProgressShapeDrawable = new ShapeDrawable(createRoundRectShape(roundRadius));
        progressShapeDrawable = new ShapeDrawable(createRoundRectShape(roundRadius));
        shapeBmp = Bitmap.createBitmap(zebraLength <<1,horizontalProgressHeight, Bitmap.Config.ARGB_8888);
        shader = new BitmapShader(createShaderBitmap(),Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        if(isCopyIphone){
            iphonePaint = new Paint();
            iphonePaint.setColor(progressBackgroundColor);
            iphonePaint.setStrokeWidth(copyIphoneDividerWidth);
        }
    }


    private void initAnimator(){
        animator = ValueAnimator.ofInt(0, zebraLength <<1);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(200);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                offset = (Integer) animation.getAnimatedValue();
                invalidate();
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimated = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimated = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public int getLoadingColor() {
        return loadingColor;
    }

    public void setLoadingColor(int loadingColor) {
        this.loadingColor = loadingColor;
    }

    public int getLoadingZebra() {
        return loadingZebra;
    }

    public void setLoadingZebra(int loadingZebra) {
        this.loadingZebra = loadingZebra;
    }

    public int getProgressColor() {
        return progressColor;
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
    }

    public int getProgressBackgroundColor() {
        return progressBackgroundColor;
    }

    public void setProgressBackgroundColor(int progressBackgroundColor) {
        this.progressBackgroundColor = progressBackgroundColor;
    }

    public int getZebraLength() {
        return zebraLength;
    }

    public void setZebraLength(int zebraLength) {
        this.zebraLength = zebraLength;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getHorizontalProgressHeight() {
        return horizontalProgressHeight;
    }

    public void setHorizontalProgressHeight(int horizontalProgressHeight) {
        this.horizontalProgressHeight = horizontalProgressHeight;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean isPause) {
        this.isPause = isPause;
    }

    public void start(){

    }

    public void pause(){

    }

    float pi = 3.1415926f;

    @Override
    protected synchronized void onDraw(Canvas canvas) {

        if((onlyIndeterminate||isIndeterminate())&&type==HORIZONTAL_TYPE){
            drawLoading(canvas);
        }else if(!onlyIndeterminate&&!isIndeterminate()&&type==HORIZONTAL_TYPE){
            drawProgress(canvas);
        }else if(type == CIRCLE_TYPE&&!(onlyIndeterminate||isIndeterminate())){
            Paint paint = new Paint();
            paint.setStrokeWidth(roundRadius);
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);

            paint.setColor(progressBackgroundColor);
            RectF temp = new RectF(mPaddingLeft,mPaddingTop,getWidth()-mPaddingRight,getHeight()-mPaddingBottom);

            oval.set(temp.left+temp.width()/2-circleRadius,temp.top+temp.height()/2-circleRadius,
                    temp.right-temp.width()/2+circleRadius,temp.bottom-temp.height()/2+circleRadius);

            canvas.drawOval(oval, paint);

            paint.setColor(progressColor);
            float angle = getProgressAngle();
            canvas.drawArc(oval,-90,angle,false,paint);

            paint.setStrokeWidth(0);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);

            canvas.drawOval(new RectF(oval.left+oval.width()/2-roundRadius/2,
                                      oval.top-roundRadius/2,
                                      oval.left+oval.width()/2+roundRadius/2,
                                      oval.top+roundRadius/2),paint);
            canvas.save();

            float offsetY =0;
            float offsetX =0;

            if(angle<=90){
                offsetY = (float)Math.abs((oval.width()/2*(1-Math.sin(2*pi*(angle+90)/360))));
                offsetX = (float)Math.abs((oval.width()/2*Math.cos(2*pi*(angle+270)/360)));
            }else if(angle<=180){
                offsetY = (float)Math.abs((oval.width()/2*(1+Math.sin(2*pi*(angle-90)/360))));

                offsetX = (float)Math.abs((oval.width()/2*Math.cos(2*pi*(angle-90)/360)));
            }else if(angle<=270){
                offsetY = (float)Math.abs((oval.width()/2*(1+Math.sin(2*pi*(angle-90)/360))));
                offsetX = (float)-Math.abs((oval.width()/2*Math.cos(2*pi*(angle-90)/360)));
            }else{
                offsetY = (float)Math.abs((oval.width()/2*(1-Math.sin(2*pi*(angle+90)/360))));
                offsetX = (float)-Math.abs((oval.width()/2*Math.cos(2*pi*(angle+270)/360)));
            }
            canvas.translate(offsetX, offsetY);

            canvas.drawOval(new RectF(oval.left+oval.width()/2-roundRadius/2,
                    oval.top-roundRadius/2,
                    oval.left+oval.width()/2+roundRadius/2,
                    oval.top+roundRadius/2),paint);
            canvas.restore();

        }else if(type == CIRCLE_TYPE){
            super.onDraw(canvas);
        }
    }

    private float getProgressAngle(){
        return 0.5f+360*getProgress()/(float)getMax();
    }

    private RectF mSaveLayerRectF;

    private void drawProgress(Canvas canvas) {

        canvas.saveLayerAlpha(mSaveLayerRectF, isPause?77:255, Canvas.MATRIX_SAVE_FLAG
                | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
        canvas.save();
        updateProgressBounds();
        backgroundProgressShapeDrawable.draw(canvas);
        progressShapeDrawable.draw(canvas);
        if(isCopyIphone){
            Rect bounds = progressShapeDrawable.getBounds();
            float step = bounds.width()/(float)getProgress();
            for (int i=0;i<=getProgress();i++){
                canvas.drawLine(bounds.left+i*step,bounds.top,
                        bounds.left+i*step,bounds.bottom,iphonePaint);
            }
        }
        canvas.restore();
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int dw = 0;
        int dh = 0;
        if(type == CIRCLE_TYPE) {
            if((circleRadius+roundRadius)*2<minHeight && (circleRadius+roundRadius)*2<minWidth) {
                dw = Math.max(minWidth, 0);
                dh = Math.max(minHeight, 0);
            }else{
                dw = dh = (circleRadius+roundRadius)*2;
            }
        }else{
            dw = horizontalProgressHeight;
            dh = horizontalProgressHeight;
            if(isCopyIphone){
                dh += copyIphoneDividerWidth*2;
            }
        }
        dw += mPaddingLeft + mPaddingRight;
        dh += mPaddingTop + mPaddingBottom;

        setMeasuredDimension(resolveSizeAndState(dw, widthMeasureSpec, 0),
                resolveSizeAndState(dh, heightMeasureSpec, 0));
    }

    private void updateProgressBounds() {
        float max = getMax();
        float progress = getProgress();
        Rect bounds = new Rect(mPaddingLeft+(isCopyIphone?copyIphoneDividerWidth/2:0),
                mPaddingTop+(isCopyIphone?copyIphoneDividerWidth:0),
                (int) (mPaddingLeft+(isCopyIphone?copyIphoneDividerWidth/2:0)+
                        (getWidth() - mPaddingRight-mPaddingLeft-(isCopyIphone?copyIphoneDividerWidth:0)) * progress / max),
                getHeight() - mPaddingBottom
                        -(isCopyIphone?copyIphoneDividerWidth:0));
        progressShapeDrawable.setBounds(bounds);
        progressShapeDrawable.getPaint().setColor(progressColor);
        backgroundProgressShapeDrawable.getPaint().setColor(progressBackgroundColor);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float centerX = (getHeight() - mPaddingTop -mPaddingBottom)/2;
        shapeDrawable.setBounds(new Rect(mPaddingLeft,(int)(centerX-horizontalProgressHeight/2),
                             getWidth()-mPaddingRight,(int)(centerX+horizontalProgressHeight/2)));
        backgroundProgressShapeDrawable.setBounds(new Rect(mPaddingLeft,mPaddingTop,getWidth()-mPaddingRight,getHeight()-mPaddingBottom));
        mSaveLayerRectF = new RectF(0, 0, getWidth(), getHeight());
    }

    Bitmap shapeBmp;
    Path p = new Path();

    Bitmap createShaderBitmap(){


        Paint paint1 = new Paint();
        paint1.setAntiAlias(true);

        drawColorBitmap(shapeBmp,loadingColor);

        Canvas canvas = new Canvas(shapeBmp);

        paint1.setColor(loadingZebra);

        p.reset();
        p.moveTo(-zebraLength + offset, 0);
        p.lineTo(-zebraLength - (zebraLength >> 1) + offset, horizontalProgressHeight);
        p.lineTo(-(zebraLength >> 1) + offset, horizontalProgressHeight);
        p.lineTo(offset, 0);
        p.close();

        canvas.drawPath(p, paint1);

        p.reset();
        p.moveTo(zebraLength + offset, 0);
        p.lineTo((zebraLength >> 1) + offset, horizontalProgressHeight);
        p.lineTo(zebraLength + (zebraLength >> 1) + offset, horizontalProgressHeight);
        p.lineTo((zebraLength << 1) + offset, 0);
        p.close();
        canvas.drawPath(p,paint1);

        return shapeBmp;
    }


    private void drawColorBitmap(Bitmap bitmap,int color){
        for(int i=0;i<bitmap.getWidth();i++){
            for(int j=0;j<bitmap.getHeight();j++) {
                bitmap.setPixel(i,j, color);
            }
        }
    }


    private void drawLoading(Canvas canvas) {
//        canvas.save();
        shader = new BitmapShader(createShaderBitmap(),Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
//        shapeDrawable.getPaint().setColor(loadingColor);
        shapeDrawable.getPaint().setShader(shader);
        shapeDrawable.draw(canvas);
//        canvas.restore();
        if(!isAnimated){
            animator.start();
        }
    }

    @Override
    public void setIndeterminateDrawable(Drawable d) {
        super.setIndeterminateDrawable(d);
    }

    private Shape createRoundRectShape(float radiu){
        float[] outerR = new float[] { radiu, radiu, radiu, radiu, radiu, radiu, radiu, radiu };
        RoundRectShape shape = new RoundRectShape(outerR,null,null);
        return shape;
    }

}
