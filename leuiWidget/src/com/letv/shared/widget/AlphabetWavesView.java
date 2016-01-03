package com.letv.shared.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.letv.shared.R;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Property;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 1. Pop alphabet wave.
 * 2. Show initial alphabet toast.
 * @author wangziming
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AlphabetWavesView extends LinearLayout {

    /** Duration of fade-out animation. */
    private static final int DURATION_FADE_OUT = 300;

    /** Duration of fade-in animation. */
    private static final int DURATION_FADE_IN = 150;

    /** Duration of transition cross-fade animation. */
    private static final int DURATION_CROSS_FADE = 50;

    /** Inactivity timeout before fading controls. */
    private static final long FADE_TIMEOUT = 1500;
    
    /** Scroll thumb and preview not showing. */
    private static final int STATE_NONE = 0;

    /** Scroll thumb visible and moving along with the scrollbar. */
    private static final int STATE_VISIBLE = 1;
    
    /** Scroll thumb and preview being dragged by user. */
    private static final int STATE_CHANGE_TEXT = 2;
    
    private static final int POP_LIMIT_NUM = 10;

    private List<Alphabet> mAlphabetList;
    private HashMap<Integer, ValueAnimator> mAnimMap;
    private OnAlphabetListener mAlphabetListener;
    private float mSideIndexX;
    private float mSideIndexY;
    private int mSideIndexHeight;
    private int mIndexListSize;
    private int mViewWidth;
    private int mToastOffset;
    private int mToastTextSize;
    private int mAlphabetTextSize;
    private int mPaddingTopBottom;
    private int mAlphabetMaxOffset;
    private int mAlphabetLeftMargin;
    
    private int mMaxOffset;
    private int mMoveCount;
    private long mPopAnimTime;
    private long mBackAnimTime = 0;
    
    private float mLastFocusX;
    private float mLastFocusY;
    
    private Handler mHandler;
    private boolean mInSelect;
    private final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
    private final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
    private final int LONG_SELECT = 0;
    
    private Drawable mSelectedBg;
    private Drawable mToastBg;
    /** Defines the selectedBg's location and dimension at drawing time */
    private Rect mSelectedRect = new Rect();
    
    private ViewGroupOverlay mOverlay;
    private final TextView mPrimaryText;
    private final TextView mSecondaryText;
    private final ImageView mPreviewImage;
    
    private final Rect mContainerRect = new Rect();
    private final Rect mTempBounds = new Rect();
    private final Rect mTempMargins = new Rect();
    private final Rect mTempRect = new Rect();
    
    /** Set containing preview text transition animations. */
    private AnimatorSet mPreviewAnimation;
    /** Set containing decoration transition animations. */
    private AnimatorSet mDecorAnimation;
    
    /** Whether this view is currently performing layout. */
    private boolean mUpdatingLayout;
    
    /** Whether the primary text is showing. */
    private boolean mShowingPrimary;
    
    /** Whether the preview image is visible. */
    private boolean mShowingPreview;
    
    private int mPreSelection = -1;
    /** The index of the current section. */
    private int mCurrentSelection = -1;
    /**
     * Padding in pixels around the preview text. Applied as layout margins to
     * the preview text and padding to the preview image.
     */
    //private final int mPreviewPadding;
    
    private boolean isShowSelected;
    
    private boolean isPoped = false;
    
    boolean isSetList = false;

    /** Whether clip Alphabet, show initial. */
    private boolean mIsClipInitial = true;

    public AlphabetWavesView(Context context) {
        this(context, null);
    }

    public AlphabetWavesView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.leAlphabetWavesViewStyle);
    }
    
    @SuppressLint("UseSparseArrays")
    public AlphabetWavesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        final Resources res = context.getResources();
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AlphabetWavesView, defStyle, 0);
        mMaxOffset = a.getDimensionPixelSize(R.styleable.AlphabetWavesView_leMaxOffset, 54);
        mMoveCount = a.getInteger(R.styleable.AlphabetWavesView_leMoveCount, 7);
        mPopAnimTime = a.getInteger(R.styleable.AlphabetWavesView_lePopAnimTime, 120);
        
        mToastBg = a.getDrawable(R.styleable.AlphabetWavesView_leAlphabetToastBg);
        mSelectedBg = a.getDrawable(R.styleable.AlphabetWavesView_leSelectedBg);
        
        mToastOffset = a.getDimensionPixelSize(R.styleable.AlphabetWavesView_leToastOffset,
                res.getDimensionPixelSize(R.dimen.le_awv_toast_offset));
        mToastTextSize = a.getDimensionPixelSize(R.styleable.AlphabetWavesView_leToastTextSize,
                res.getDimensionPixelSize(R.dimen.le_awv_toast_text_size));
        mAlphabetTextSize = a.getDimensionPixelSize(R.styleable.AlphabetWavesView_leAlphabetTextSize,
                res.getDimensionPixelSize(R.dimen.le_awv_alphabet_text_size));
        mAlphabetMaxOffset = a.getDimensionPixelSize(R.styleable.AlphabetWavesView_leAlphabetMaxOffset,
                res.getDimensionPixelSize(R.dimen.le_awv_alphabet_max_offset));
        mPaddingTopBottom = a.getDimensionPixelSize(R.styleable.AlphabetWavesView_lePaddingTopBottom,
                res.getDimensionPixelSize(R.dimen.le_awv_padding_top_bottom));
        mAlphabetLeftMargin = a.getDimensionPixelSize(R.styleable.AlphabetWavesView_leAlphabetLeftMargin,
                res.getDimensionPixelSize(R.dimen.le_awv_alphabet_left_margin));
        a.recycle();
        
        if (mToastBg == null) {
            mToastBg = res.getDrawable(R.drawable.le_alphabet_toast_bg);
        }
        if (mSelectedBg == null) {
            mSelectedBg = res.getDrawable(R.drawable.le_alphabet_selected_bg);
        }
        
        mViewWidth = res.getDimensionPixelSize(R.dimen.le_awv_width);
        
        mHandler = new GestureHandler();
        mAlphabetList = new ArrayList<Alphabet>();
        mAnimMap = new HashMap<Integer, ValueAnimator>();
        
        mSelectedRect.set(0, 0, mSelectedBg.getIntrinsicWidth(), mSelectedBg.getIntrinsicHeight());
        isShowSelected = true;

        mPreviewImage = new ImageView(context);
        mPreviewImage.setMinimumWidth(mToastBg.getIntrinsicWidth());
        mPreviewImage.setMinimumHeight(mToastBg.getIntrinsicHeight());
        mPreviewImage.setBackground(mToastBg);
        mPreviewImage.setAlpha(0f);

        final int textMinSize = Math.max(0, mToastBg.getIntrinsicHeight() - 0);
        mPrimaryText = createPreviewTextView();
        mPrimaryText.setMinimumWidth(textMinSize);
        mPrimaryText.setMinimumHeight(textMinSize);
        
        mSecondaryText = createPreviewTextView();
        mSecondaryText.setMinimumWidth(textMinSize);
        mSecondaryText.setMinimumHeight(textMinSize);
        
        setGravity(Gravity.CENTER);
        setPadding(0, mPaddingTopBottom, 0, mPaddingTopBottom);
    }
    
    private static final Interpolator mInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };
    
    @SuppressLint("HandlerLeak")
    private class GestureHandler extends Handler {
        GestureHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case LONG_SELECT:
                mInSelect = true;
                popAlphabet();
                break;
            default:
                throw new RuntimeException("Unknown message " + msg); //never
            }
        }
    }

    private class Alphabet {
        String firstAlphabet;
        //int position; // unused
    }

    public static interface OnAlphabetListener {
        /**
         * When alphabet is changed
         * 
         * @param alphabetPosition position in List
         * @param firstAlphabet the first alphabet
         */
        void onAlphabetChanged(int alphabetPosition, String firstAlphabet);
    }

    /**
     * set alphabet listener
     * 
     * @param aListener
     */
    public void setOnAlphabetListener(OnAlphabetListener aListener) {
        mAlphabetListener = aListener;
    }
    
    /**
     * The incoming string List, clip each initials, to be displayed.
     * 
     * @param listStr the list contains initial alphabet.
     */
    public void setAlphabetList(List<String> listStr) {
        mAlphabetList.clear();
        List<String> countries = listStr;
        // Don't sort inside.
        //Collections.sort(countries);

        String previousAlphabet = null;
        Pattern numberPattern = Pattern.compile("[0-9]");

        for (String country : countries) {
            String firstAlphabet = mIsClipInitial ? country.substring(0, 1) : country;

            // Group numbers together in the scroller
            if (mIsClipInitial && numberPattern.matcher(firstAlphabet).matches()) {
                firstAlphabet = "#";
            }

            // If we've changed to a new Alphabet, add the previous Alphabet to the alphabet scroller
            if (previousAlphabet != null && !firstAlphabet.equals(previousAlphabet)) {
                String tempLeter = mIsClipInitial ? previousAlphabet.toUpperCase(Locale.UK) : previousAlphabet;
                Alphabet alphabet = new Alphabet();
                alphabet.firstAlphabet = tempLeter;
                mAlphabetList.add(alphabet);
                //alphabet.position = mAlphabetList.size() - 1;
            }

            previousAlphabet = firstAlphabet;
        }

        if (previousAlphabet != null) {
            // Save the last Alphabet
            Alphabet alphabet = new Alphabet();
            alphabet.firstAlphabet = previousAlphabet;
            mAlphabetList.add(alphabet);
            //alphabet.position = mAlphabetList.size() - 1;
        }

        isSetList = true;
        requestLayout();
    }
    
    /**
     * Removes this FastScroller overlay from the host view.
     */
    public void remove() {
        mOverlay.remove(mPreviewImage);
        mOverlay.remove(mPrimaryText);
        mOverlay.remove(mSecondaryText);
    }
    
    /**
     * Measures and layouts the scrollbar and decorations.
     */
    public void updatePopAlphabetLayout() {
        // Prevent re-entry when RTL properties change as a side-effect of
        // resolving padding.
        if (mUpdatingLayout) {
            return;
        }

        mUpdatingLayout = true;
        
        updateContainerRect();

        final Rect bounds = mTempBounds;
        measurePreview(mPrimaryText, bounds);
        applyLayout(mPrimaryText, bounds);
        measurePreview(mSecondaryText, bounds);
        applyLayout(mSecondaryText, bounds);

        if (mPreviewImage != null) {
            // Apply preview image padding.
            bounds.left -= mPreviewImage.getPaddingLeft();
            bounds.top -= mPreviewImage.getPaddingTop();
            bounds.right += mPreviewImage.getPaddingRight();
            bounds.bottom += mPreviewImage.getPaddingBottom();
            applyLayout(mPreviewImage, bounds);
        }
    }

    /**
     * setSelection, if user is touching on the AWView, the function is invalid.
     * @param index
     */
    public void setSelection(int index) {
        if (index >= 0 && index < mAlphabetList.size()) {
            TextView textV = (TextView) getChildAt(index);
            TextView curTextV = (TextView) getChildAt(mCurrentSelection);
            
            ColorStateList oldColor = null;
            if (textV != null) {
                oldColor = textV.getTextColors();
            }
            if (curTextV != null && oldColor !=null) {
                curTextV.setTextColor(oldColor);// restore
            }
            
            mCurrentSelection = index;
            if (textV != null && oldColor != null) {
                if (isPoped) {
                    textV.setTextColor(oldColor);
                } else {
                    textV.setTextColor(Color.WHITE);
                }
            }
            if (!isPoped) {
                isShowSelected = true;
                invalidate();
            }

            if (mShowingPreview) {
                setState(STATE_CHANGE_TEXT);
                setState(STATE_NONE);
            }
        }
    }
    
    /**
     * Set toast color.
     * 
     * @param argb The color used to fill the shape
     */
    public void setToastBackGroundColor(int argb) {
        if (mToastBg instanceof GradientDrawable) {
            GradientDrawable gDrawable = (GradientDrawable) mToastBg;
            gDrawable.setColor(argb);
        }
    }
    
    /**
     * Set selected color.
     * 
     * @param argb The color used to fill the shape
     */
    public void setSelectedBackGroundColor(int argb) {
        if (mSelectedBg instanceof GradientDrawable) {
            GradientDrawable gDrawable = (GradientDrawable) mSelectedBg;
            gDrawable.setColor(argb);
        }
    }

    /**
     * setIsClipInitial
     * 
     * @param isClipInitial clip is true, no clip is false.
     */
    public void setIsClipInitial(boolean isClipInitial) {
        mIsClipInitial = isClipInitial;
    }

    /**
     * @return isClipIntial
     */
    public boolean isClipIntial() {
        return mIsClipInitial;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final boolean pointerUp = (event.getAction() & MotionEvent.ACTION_MASK)
                                  == MotionEvent.ACTION_POINTER_UP;
        final int skipIndex = pointerUp ? event.getActionIndex() : -1;

        float sumX = 0, sumY = 0;
        final int count = event.getPointerCount();
        for (int i = 0; i < count; i++) {
            if (skipIndex == i) continue;
            sumX += event.getX(i);
            sumY += event.getY(i);
        }
        final int div = pointerUp ? count - 1 : count;
        final float focusX = sumX / div;
        final float focusY = sumY / div;
        getLocalVisibleRect(mTempRect);
        if (focusY < getPaddingTop() || focusY > getHeight() - getPaddingBottom()
                || !mTempRect.contains((int) focusX, (int) focusY)) {
            isShowSelected = true;
            invalidate();
            mHandler.removeMessages(LONG_SELECT);
            backAlphabet(true);// back selected alphabet.
            mAnimMap.clear();
            setState(STATE_NONE);
            if (!isPoped) {
                isShowSelected = true;
                invalidate();
            }
            changeTextColor(mCurrentSelection, true);
            return false;
        }

        if (mCurrentSelection != mPreSelection && event.getAction() == MotionEvent.ACTION_MOVE) {
            changeTextColor(mCurrentSelection, false);
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            mSideIndexX = mLastFocusX = focusX;
            mSideIndexY = mLastFocusY = focusY;
            mInSelect = false;
            selectAlphabet();
            isShowSelected = false;
            int itemPosition = getItemPosition();
            if (mPreSelection == itemPosition) {
                changeTextColor(mCurrentSelection, false);
            }
            invalidate();
            isPoped = false;
            if (mAlphabetList.size() >= POP_LIMIT_NUM) {
                mHandler.removeMessages(LONG_SELECT);
                mHandler.sendEmptyMessageAtTime(LONG_SELECT, event.getDownTime()
                        + TAP_TIMEOUT /*+ LONGPRESS_TIMEOUT*/);// same as LONGPRESS_TIMEOUT
            }
            break;
        
        case MotionEvent.ACTION_MOVE:
            final float scrollX = mLastFocusX - focusX;
            final float scrollY = mLastFocusY - focusY;
            if (!mInSelect) {
                if ((Math.abs(scrollX) >= 1) || (Math.abs(scrollY) >= 1)) {
                    mSideIndexX = mSideIndexX - scrollX;
                    mSideIndexY = mSideIndexY - scrollY;
                    mLastFocusX = focusX;
                    mLastFocusY = focusY;
                    if (mSideIndexX >= 0 && mSideIndexY >= 0) {
                        isShowSelected = false;
                        invalidate();
                        selectAlphabet();
                    }
                    break;
                }
            }
            if ((Math.abs(scrollX) >= 1) || (Math.abs(scrollY) >= 1)) {
                mSideIndexX = mSideIndexX - scrollX;
                mSideIndexY = mSideIndexY - scrollY;
                mLastFocusX = focusX;
                mLastFocusY = focusY;
                isShowSelected = false;
                invalidate();
                if (mSideIndexX >= 0 && mSideIndexY >= 0) {
                    if (selectAlphabet()) {
                        popAlphabet();
                    }
                }
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            if (mInSelect) {
                mInSelect = false;
            }
            isShowSelected = true;
            invalidate();
            mHandler.removeMessages(LONG_SELECT);
            
            backAlphabet(true);// back selected alphabet.
            changeTextColor(mCurrentSelection, true);
            isPoped = false;// PS: mBackAnimTime = 0

            mAnimMap.clear();
            setState(STATE_NONE);
            break;
        default:
            break;
        }
        return true;
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        View childView = getChildAt(mCurrentSelection);
        TextView childTextView = null;
        if (childView instanceof TextView) {
            childTextView = (TextView) childView;
        }
        
        if (childTextView != null && !TextUtils.isEmpty(childTextView.getText()) && getVisibility() == View.VISIBLE && childTextView.getVisibility() == View.VISIBLE) {
            Rect childRect = new Rect();
            childTextView.getLocalVisibleRect(childRect);

            float childViewX = childTextView.getX() + childTextView.getWidth() / 2;
            float childViewY = childTextView.getY() + childTextView.getHeight() / 2;

            int top = (int) (childViewY - mSelectedRect.height() / 2);
            int left = (int) (childViewX - mSelectedRect.width() / 2);

            mSelectedRect.set(left, top, left + mSelectedRect.width(), top + mSelectedRect.height());

            if (isShowSelected && !mSelectedRect.isEmpty()) {
                final Drawable selectedBg = mSelectedBg;
                selectedBg.setBounds(mSelectedRect);
                selectedBg.draw(canvas);
            }
        }
        
        super.dispatchDraw(canvas);
    }
    
    /**
     * Pop alphabet
     */
    protected void popAlphabet() {
        if (mAlphabetList.size() < POP_LIMIT_NUM) {
            return;
        }
        backAlphabet(false);// first, back alphabet, and pop alphabet
        
        isPoped = true;
        changeTextColor(mCurrentSelection, false);
        isShowSelected = false;
        invalidate();
        
        int position;
        int halfMoveCount = (mMoveCount + 1) / 2;
        for (int i = 0; i < mMoveCount; i++) {
            position = mCurrentSelection - halfMoveCount + 1 + i;
            if (position >= 0 && position < getChildCount()) {
                View view = getChildAt(position);
                ValueAnimator tmpAnimator = ObjectAnimator.ofFloat(view, "translationX", view.getTranslationX(),
                        - mMaxOffset * (float) Math.sin((i + 1) * Math.PI / (mMoveCount + 1)));
                //Math.sin((i + 1) * Math.PI / (mMoveCount + 2))区间是[0,1]，如移动的字母数(mMoveCount＝3)，
                //那么取sin曲线上5个点，1和5点无动画，只创建中间3点动画，值是0.7071、1.0、0.7071。
                tmpAnimator.setDuration(mPopAnimTime);
                tmpAnimator.setRepeatCount(0);
                tmpAnimator.setInterpolator(mInterpolator);
                tmpAnimator.start();
                mAnimMap.put(position, tmpAnimator);
            }
        }
    }
    
    /**
     * Back alphabet.
     * 
     * @param isSelected 是否是选中的字母周围弹出的字母
     */
    protected void backAlphabet(boolean isSelected) {
        if (mAlphabetList.size() < POP_LIMIT_NUM) {
            return;
        }
        
        int halfMoveCount = (mMoveCount + 1) / 2;
        for (int i = 0; i < mAlphabetList.size(); i++) {
            ValueAnimator vaAnim = mAnimMap.get(i);
            if (vaAnim != null) {
                vaAnim.cancel();
            }
            // Back around the selected alphabet place.
            if (isSelected && i > mCurrentSelection - halfMoveCount && i < mCurrentSelection + halfMoveCount) {
                View view = getChildAt(i);
                
                float tX = view.getTranslationX();
                if (tX < 0f || tX > 0f) {
                    doBackAnim(view);
                }
            // Back the unselected alphabet place.
            } else if (i <= mCurrentSelection - halfMoveCount || i >= mCurrentSelection + halfMoveCount) {
                View view = getChildAt(i);
                
                float tX = view.getTranslationX();
                if (tX < 0f || tX > 0f) {
                    doBackAnim(view);
                }
            }
        }
    }
    
    private int getItemPosition() {
        mSideIndexHeight = getHeight() - 2 * getPaddingTop();// paddingTop is variational.see updateView()
        // compute number of pixels for every side index item
        double pixelPerIndexItem = (double) mSideIndexHeight / mIndexListSize;
        // compute the item index for given event position belongs to
        int itemPosition = (int) ((mSideIndexY - getPaddingTop()) / pixelPerIndexItem);
        return itemPosition;
    }
    
    protected boolean selectAlphabet() {
        final int itemPosition = getItemPosition();
        // get the item (we can do it since we know item index)
        if (itemPosition < mAlphabetList.size() && mCurrentSelection != itemPosition) {
            mPreSelection = mCurrentSelection;
            String firstAlphabet = mAlphabetList.get(itemPosition).firstAlphabet;
            
            TextView textV = (TextView) getChildAt(itemPosition);
            TextView curTextV = (TextView) getChildAt(mCurrentSelection);
            if (curTextV != null && textV != null) {
                curTextV.setTextColor(textV.getTextColors());// restore
            }
            mCurrentSelection = itemPosition;
            
            if (textV != null) {
                textV.setTextColor(Color.WHITE);
            }
            
            if (mShowingPreview) {
                setState(STATE_CHANGE_TEXT);
            } else {
                setState(STATE_VISIBLE);
            }
            
            // notify alphabet changed.
            if (mAlphabetListener != null) {
                mAlphabetListener.onAlphabetChanged(itemPosition, firstAlphabet);
            }
            return true;
        }
        return false;
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ViewGroup viewGroup = (ViewGroup) getParent();
        mOverlay = viewGroup.getOverlay();
        
        mOverlay.add(mPreviewImage);
        mOverlay.add(mPrimaryText);
        mOverlay.add(mSecondaryText);
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (isSetList) {
            adjustPadding();
            requestLayout();
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                view.requestLayout();
            }
            isPoped = false;//Fix bug: when isPoped, trun off screen, them trun on screne the selected alphabet is black.
            isSetList = false;
        }
        updatePopAlphabetLayout();
        if (!isPoped) {
            changeTextColor(mCurrentSelection, true);
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isSetList) {
            mIndexListSize = mAlphabetList.size();
            addTextView();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    private void setState(int state) {
        removeCallbacks(mDeferHide);

        switch (state) {
            case STATE_NONE:
                postAutoHide();
                break;
            case STATE_VISIBLE:
                String text = mAlphabetList.get(mCurrentSelection).firstAlphabet;
                mPrimaryText.setText(text);
                mSecondaryText.setText("");
                transitionToVisible();
                break;
            case STATE_CHANGE_TEXT:
                if (!transitionPreviewLayout(mCurrentSelection)) {
                    transitionToHidden();
                }
                break;
        }
    }
    
    /**
     * Used to delay hiding fast scroll decorations.
     */
    private final Runnable mDeferHide = new Runnable() {
        @Override
        public void run() {
            transitionToHidden();
        }
    };
    
    /**
     * Constructs an animator for the specified property on a group of views.
     * See {@link android.animation.ObjectAnimator#ofFloat(Object, String, float...)} for
     * implementation details.
     *
     * @param property The property being animated.
     * @param value The value to which that property should animate.
     * @param views The target views to animate.
     * @return An animator for all the specified views.
     */
    private static Animator groupAnimatorOfFloat(
            Property<View, Float> property, float value, View... views) {
        AnimatorSet animSet = new AnimatorSet();
        AnimatorSet.Builder builder = null;

        for (int i = views.length - 1; i >= 0; i--) {
            final Animator anim = ObjectAnimator.ofFloat(views[i], property, value);
            if (builder == null) {
                builder = animSet.play(anim);
            } else {
                builder.with(anim);
            }
        }

        return animSet;
    }
    
    /**
     * Shows nothing.
     */
    private void transitionToHidden() {
        if (mDecorAnimation != null) {
            mDecorAnimation.cancel();
        }

        final Animator fadeOut = groupAnimatorOfFloat(View.ALPHA, 0f, 
                mPreviewImage, mPrimaryText, mSecondaryText).setDuration(DURATION_FADE_OUT);

        mDecorAnimation = new AnimatorSet();
        mDecorAnimation.playTogether(fadeOut);
        mDecorAnimation.start();

        mShowingPreview = false;
    }

    /**
     * Shows the toast.
     */
    private void transitionToVisible() {
        if (mDecorAnimation != null) {
            mDecorAnimation.cancel();
        }

        final Animator fadeIn = groupAnimatorOfFloat(View.ALPHA, 1f, 
                mPreviewImage, mPrimaryText, mSecondaryText).setDuration(DURATION_FADE_IN);
        
        mDecorAnimation = new AnimatorSet();
        mDecorAnimation.playTogether(fadeIn);
        mDecorAnimation.start();

        mShowingPreview = true;
    }

    private void postAutoHide() {
        removeCallbacks(mDeferHide);
        postDelayed(mDeferHide, FADE_TIMEOUT);
    }
    
    private void addTextView() {
        removeAllViews();
        if (mIndexListSize < 1) {
            return;
        }
        
        TextView tmpTV;
        for (double i = 1; i <= mIndexListSize; i++) {
            String tmpAlphabet = mAlphabetList.get((int) i - 1).firstAlphabet;

            tmpTV = new TextView(getContext());
            tmpTV.setText(tmpAlphabet);
            tmpTV.setGravity(Gravity.CENTER);
            tmpTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, mAlphabetTextSize);
            tmpTV.setTextColor(Color.BLACK);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            params.leftMargin = mAlphabetLeftMargin;
            tmpTV.setLayoutParams(params);
            tmpTV.setIncludeFontPadding(false);
            
            addView(tmpTV);
        }
    }
    
    private void adjustPadding() {
        int padding = (getHeight() - mAlphabetMaxOffset * (mIndexListSize - 1) - mAlphabetTextSize * mIndexListSize) / 2;
        if (padding > mPaddingTopBottom) {
            setPadding(getPaddingStart(), padding, getPaddingEnd(), padding);
        } else {
            setPadding(getPaddingStart(), mPaddingTopBottom, getPaddingEnd(), mPaddingTopBottom);
        }
    }
    
    /**
     * Transitions the preview text to a new section. Handles animation,
     * measurement, and layout. If the new preview text is empty, returns false.
     *
     * @param sectionIndex The section index to which the preview should
     *            transition.
     * @return False if the new preview text is empty.
     */
    private boolean transitionPreviewLayout(int sectionIndex) {
        String text = mAlphabetList.get(sectionIndex).firstAlphabet;

        final Rect bounds = mTempBounds;
        final TextView showing;
        final TextView target;
        if (mShowingPrimary) {
            showing = mPrimaryText;
            target = mSecondaryText;
        } else {
            showing = mSecondaryText;
            target = mPrimaryText;
        }

        // Set and layout target immediately.
        target.setText(text);
        measurePreview(target, bounds);
        applyLayout(target, bounds);

        if (mPreviewAnimation != null) {
            mPreviewAnimation.cancel();
        }

        // Cross-fade preview text.
        final Animator showTarget = animateAlpha(target, 1f).setDuration(DURATION_CROSS_FADE);
        final Animator hideShowing = animateAlpha(showing, 0f).setDuration(DURATION_CROSS_FADE);
        hideShowing.addListener(mSwitchPrimaryListener);

        // Apply preview image padding and animate bounds, if necessary.
        bounds.left -= mPreviewImage.getPaddingLeft();
        bounds.top -= mPreviewImage.getPaddingTop();
        bounds.right += mPreviewImage.getPaddingRight();
        bounds.bottom += mPreviewImage.getPaddingBottom();
        /*final Animator resizePreview = animateBounds(preview, bounds);
        resizePreview.setDuration(DURATION_RESIZE);*/

        mPreviewAnimation = new AnimatorSet();
        mPreviewAnimation.play(hideShowing).with(showTarget);
        mPreviewAnimation.start();

        return !TextUtils.isEmpty(text);
    }
    
    /**
     * Used to effect a transition from primary to secondary text.
     */
    private final AnimatorListener mSwitchPrimaryListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            mShowingPrimary = !mShowingPrimary;
        }
    };
    
    /**
     * Returns an animator for the view's alpha value.
     */
    private static Animator animateAlpha(View v, float alpha) {
        return ObjectAnimator.ofFloat(v, View.ALPHA, alpha);
    }
    
    
    /**
     * Measures the preview text bounds, taking preview image padding into
     * account. This method should only be called after {@link #layoutThumb()}
     * and {@link #layoutTrack()} have both been called at least once.
     *
     * @param v The preview text view to measure.
     * @param out Rectangle into which measured bounds are placed.
     */
    private void measurePreview(View v, Rect out) {
        // Apply the preview image's padding as layout margins.
        final Rect margins = mTempMargins;
        margins.left = mPreviewImage.getPaddingLeft();
        margins.top = mPreviewImage.getPaddingTop();
        margins.right = mPreviewImage.getPaddingRight();
        margins.bottom = mPreviewImage.getPaddingBottom();

        measureFloating(v, margins, out);
    }
    
    private void measureFloating(View preview, Rect margins, Rect out) {
        final int marginLeft;
        final int marginTop;
        final int marginRight;
        if (margins == null) {
            marginLeft = 0;
            marginTop = 0;
            marginRight = 0;
        } else {
            marginLeft = margins.left;
            marginTop = margins.top;
            marginRight = margins.right;
        }

        final Rect container = mContainerRect;
        final int containerWidth = container.width();
        final int adjMaxWidth = containerWidth - marginLeft - marginRight;
        final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(adjMaxWidth, MeasureSpec.AT_MOST);
        final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        preview.measure(widthMeasureSpec, heightMeasureSpec);

        // Align at the vertical center, mToastOffset away from this View.
        final int containerHeight = container.height();
        final int width = preview.getMinimumWidth();
        final int top = (containerHeight - width) / 2 + container.top;
        final int bottom = top + preview.getMeasuredHeight();
        final int left = containerWidth - mViewWidth - mToastOffset - width + container.left;
        final int right = left + width;
        out.set(left, top, right, bottom);
    }

    /**
     * Creates a view into which preview text can be placed.
     */
    private TextView createPreviewTextView() {
        final LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //final Resources res = getContext().getResources();
        //final float textSize = res.getDimensionPixelSize(R.dimen.fastscroll_overlay_text_size);
        final TextView textView = new TextView(getContext());
        textView.setLayoutParams(params);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mToastTextSize);
        textView.setSingleLine(true);
        textView.setEllipsize(TruncateAt.MIDDLE);
        textView.setGravity(Gravity.CENTER);
        textView.setAlpha(0f);
        return textView;
    }
    
    /**
     * Updates the container rectangle used for layout.
     */
    private void updateContainerRect() {
        ViewGroup viewGroup = (ViewGroup) getParent();
        
        final Rect container = mContainerRect;
        container.left = viewGroup.getLeft() + viewGroup.getPaddingLeft();
        container.top = viewGroup.getTop() + viewGroup.getPaddingTop();
        container.right = viewGroup.getRight() - viewGroup.getPaddingRight();
        container.bottom = viewGroup.getBottom() - viewGroup.getPaddingBottom();
    }
    
    /**
     * Layouts a view within the specified bounds and pins the pivot point to
     * the appropriate edge.
     *
     * @param view The view to layout.
     * @param bounds Bounds at which to layout the view.
     */
    private void applyLayout(View view, Rect bounds) {
        view.layout(bounds.left, bounds.top, bounds.right, bounds.bottom);
        view.setPivotX(bounds.right - bounds.left);
    }

    private void doBackAnim(final View view) {
        ValueAnimator tmpAnimator = ObjectAnimator.ofFloat(view, "translationX", view.getTranslationX(), 0);
        tmpAnimator.setDuration(mBackAnimTime);
        tmpAnimator.setRepeatCount(0);
        tmpAnimator.start();
    }

    private void changeTextColor(int position, boolean isWhite) {
        TextView curTextV = (TextView) getChildAt(position);
        if (curTextV != null) {
            curTextV.setTextColor(isWhite ? Color.WHITE : Color.BLACK);
        }
    }

}
