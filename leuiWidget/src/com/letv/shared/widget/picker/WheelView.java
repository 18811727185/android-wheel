
package com.letv.shared.widget.picker;

import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import com.letv.shared.R;
import com.letv.shared.widget.picker.adapters.AbstractWheelTextAdapter;
import com.letv.shared.widget.picker.adapters.DayArrayAdapter;
import com.letv.shared.widget.picker.adapters.NumericWheelAdapter;
import com.letv.shared.widget.picker.adapters.WheelViewAdapter;
import java.util.LinkedList;
import java.util.List;
//import com.letv.leui.R;

/**
 *WheelView
 * @author mengfengxiao@letv.com
 */
public class WheelView extends View {

	/** Top and bottom shadows colors */
	private static final int[] SHADOWS_COLORS = new int[] { 0x70FFFFFF, 0x70FFFFFF, 0x70FFFFFF };

	/** Top and bottom items offset (to hide that) */
	private static final int ITEM_OFFSET_PERCENT = 10;

	/** Left and right padding value */
	private static final int PADDING = 0;

	/** Default count of visible items */
//	private static final int DEF_VISIBLE_ITEMS = 7;

    //liangchao change DEF_VISIBLE_ITEMS
    private static final int DEF_VISIBLE_ITEMS = 5;
	/** Wheel Values*/
	private int currentItem = 0;
    private int curItem_uncyclic=0;
	
	/** Count of visible items(odd number)*/
	private int visibleItems = DEF_VISIBLE_ITEMS;
	
	/**Item height*/
	private int itemHeight = 0;
	private int itemWidth = 0;

	/**Center Line*/
	private Drawable centerDrawable;

	/** Shadows drawables*/
	private GradientDrawable topShadow;
	private GradientDrawable bottomShadow;

    public WheelScroller getScroller() {
        return scroller;
    }

    /**Scrolling*/
	private WheelScroller scroller;
    private boolean isScrollingPerformed; 
    private int scrollingOffset;
    private int uScrollingOffset;

	/**Cyclic*/
	boolean isCyclic = false;
	
	/**Items layout*/


	private MyLinearLayout itemsLayout;

	/**The number of first item in layout*/
	private int firstItem;

	/**View adapter*/
	private WheelViewAdapter viewAdapter;

	/**Recycle*/
	private WheelRecycle recycle = new WheelRecycle(this);

	/**itemlayout direction*/
	private boolean isVertical = true;

	private int itemsWidth;

	/**label*/
	private int itemTextSize;
	private int labelTextSize;
	private int centerColor;
	private int normalColor;
	private float itemStrokeWidth;
    private float labelStrokeWidth;
	private Paint labelPaint;
	private String label;
	private int labelOffset = 0;
	float labelWidth;
	float labelHeight;
	FontMetricsInt fontMetrics;


	/**Listeners*/
	private List<OnWheelChangedListener> changingListeners = new LinkedList<OnWheelChangedListener>();
	private List<OnWheelScrollListener> scrollingListeners = new LinkedList<OnWheelScrollListener>();
    private List<OnWheelClickedListener> clickingListeners = new LinkedList<OnWheelClickedListener>();

	/**
	 * Constructor
	 */
	public WheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initData(context);
	}

	/**
	 * Constructor
	 */
	public WheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData(context);
	}

	/**
	 * Constructor
	 */
	public WheelView(Context context) {
		super(context);
		initData(context);
	}

	public int getUOffset() {
		return uScrollingOffset;
	}

    public void setUOffset(int uOffset) {
        uScrollingOffset = uOffset;
    }

	/**
	 * Initializes class data
	 * @param context the context
	 */
	private void initData(Context context) {
	    scroller = new WheelScroller(getContext(), scrollingListener,isVertical);
        NumericWheelAdapter view = new NumericWheelAdapter(getContext(), 0, 10, "%02d");
        //labelTextSize = itemTextSize = ((AbstractWheelTextAdapter) view).getTextSize();
        //liangchao change labelTextSize
        labelTextSize = 16;
        labelStrokeWidth = itemStrokeWidth = ((AbstractWheelTextAdapter) view).getStrokeWidth();
//    	centerColor = getResources().getColor(R.color.le_color_wheel_picker_center);
//		normalColor = getResources().getColor(R.color.le_color_wheel_picker_normal);

        //liangchao change color
        centerColor = 0xff21aece;
        normalColor = 0xffbcbcbc;
	}

	public void setIsVertical(boolean direction) {
		this.isVertical = direction;
		int orientation = isVertical ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL;
		if(itemsLayout!=null)
			itemsLayout.setOrientation(orientation);
        if(viewAdapter instanceof AbstractWheelTextAdapter && viewAdapter!=null) {
            AbstractWheelTextAdapter adapter = (AbstractWheelTextAdapter)viewAdapter;
            adapter.setOritentation(isVertical);
        }
        if(scroller!=null)
            scroller.setOrientation(isVertical);

	}

	public boolean getIsVertical() {
		return isVertical;
	}

	// Scrolling listener
	WheelScroller.ScrollingListener scrollingListener = new WheelScroller.ScrollingListener() {
        public void onStarted() {
            isScrollingPerformed = true;
            notifyScrollingListenersAboutStart();
        }

        public void onScroll(int distance) {
            doScroll(distance);
            if(isVertical) {
                int height = getHeight();
                if (scrollingOffset > height) {
                    scrollingOffset = height;
                    scroller.stopScrolling();
                } else if (scrollingOffset < -height) {
                    scrollingOffset = -height;
                    scroller.stopScrolling();
                }
            } else {
                int width = getWidth();
                if (scrollingOffset > width) {
                    scrollingOffset = width;
                    scroller.stopScrolling();
                } else if (scrollingOffset < -width) {
                    scrollingOffset = -width;
                    scroller.stopScrolling();
                }
            }
        }

        public void onFinished() {
            if (isScrollingPerformed) {
                notifyScrollingListenersAboutEnd();
                isScrollingPerformed = false;
            }
            scrollingOffset = 0;
            invalidate();
        }

        public void onJustify() {
            if (Math.abs(scrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING) {
                scroller.scroll(scrollingOffset, 0);
            }
        }
    };

	/**
	 * Set the the specified scrolling interpolator
	 * @param interpolator the interpolator
	 */
	public void setInterpolator(Interpolator interpolator) {
		scroller.setInterpolator(interpolator);
	}

	/**
	 * Gets count of visible items
	 *
	 * @return the count of visible items
	 */
	public int getVisibleItems() {
		return visibleItems;
	}

	/**
	 * Sets the desired count of visible items.
	 * Actual amount of visible items depends on wheel layout parameters.
	 * To apply changes and rebuild view call measure().
	 *
	 * @param count the desired count for visible items
	 */
	public void setVisibleItems(int count) {
		visibleItems = count;
	}

	/**
	 * set label
	 * @param text
	 */
	public void setLabel(String text) {
		if (label == null || !label.equals(text)) {
            label = text;

            initLabelPaint();
        }
	}

	public String getLable(){
        return label;
	}
	/**
	 * set text size
	 * @param size   the text size
	 */
	public void setTextSize(int size) {
        if(size>0) {
            itemTextSize = labelTextSize = size;
            initLabelPaint();
            initItem();
        }
	}

   /**
     * set strokeWidth
     */
    public void setItemStrokeWidth(float width) {
        if(width>0) {
            itemStrokeWidth = width;
            initItem();
        }
    }

    public void setLabelStrokeWidth(float width) {
        if(width>0) {
            labelStrokeWidth = width;
            initLabelPaint();
        }
    }

	public void setItemTextSize(int size) {
        if(size>0) {
            itemTextSize = size;
            initItem();
        }
	}

	public void setLabelTextSize(int size) {
        if(size>0) {
            labelTextSize = size;
            initLabelPaint();
        }
	}

	public int getItemTextSize() {
        return itemTextSize;
	}

	public int getLabelTextSize() {
		return labelTextSize;
	}

	public void setNormalTextColor(int color) {
        normalColor = color;
        invalidate();
	}

	public void setCenterTextColor(int color) {
        centerColor = color;
        initLabelPaint();
        invalidate();
	}

    public void setCenterTextColot(int color) {
        centerColor = color;
        initLabelPaint();
        invalidate();
    }

	public int getNormalTextColor() {
		return normalColor;
	}

	public int getCenterTextColot() {
		return centerColor;
	}

	/**get
	 * get text size
	 * @return textsize
	 */
	public int getTextSize() {
		return itemTextSize;
	}
	/**
	 * set label offset
	 * @param offset the distance between label and itemlayout
	 */
	public void setLabelOffset(int offset) {
		labelOffset = offset;
	}
	/**
	 * get the label offset
	 */
	public int getLabelOffset() {
		return labelOffset;
	}
	/**
	 *
	 */
	public int getScrollingOffset() {
		return scrollingOffset;
	}

	/**
	 * Gets view adapter
	 * @return the view adapter
	 */
	public WheelViewAdapter getViewAdapter() {
		return viewAdapter;
	}

	// Adapter listener
    private DataSetObserver dataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            invalidateWheel(false);
        }

        @Override
        public void onInvalidated() {
            invalidateWheel(true);
        }
    };

	/**
	 * Sets view adapter. Usually new adapters contain different views, so
	 * it needs to rebuild view by calling measure().
	 *
	 * @param viewAdapter the view adapter
	 */
	public void setViewAdapter(WheelViewAdapter viewAdapter) {
	    if (this.viewAdapter != null) {
	        this.viewAdapter.unregisterDataSetObserver(dataObserver);
	    }
        this.viewAdapter = viewAdapter;
        if (this.viewAdapter != null) {
            this.viewAdapter.registerDataSetObserver(dataObserver);
        }

        if(viewAdapter instanceof AbstractWheelTextAdapter) {
            AbstractWheelTextAdapter adapter = (AbstractWheelTextAdapter)viewAdapter;
            boolean adapterIsVertical = adapter.getOritentaion();
            if(isVertical!=adapterIsVertical)
                adapter.setOritentation(isVertical);
            int orientation = isVertical ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL;
            if(itemsLayout!=null)
                itemsLayout.setOrientation(orientation);
        }
        initItem();
        invalidateWheel(true);
	}

	/**
	 * Adds wheel changing listener
	 * @param listener the listener
	 */
	public void addChangingListener(OnWheelChangedListener listener) {
		changingListeners.add(listener);
	}

	/**
	 * Removes wheel changing listener
	 * @param listener the listener
	 */
	public void removeChangingListener(OnWheelChangedListener listener) {
		changingListeners.remove(listener);
	}

	/**
	 * Notifies changing listeners
	 * @param oldValue the old wheel value
	 * @param newValue the new wheel value
	 */
	protected void notifyChangingListeners(int oldValue, int newValue) {
		for (OnWheelChangedListener listener : changingListeners) {
			listener.onChanged(this, oldValue, newValue);
		}
	}

    protected void notifyChangingListeners(int diff) {
        for (OnWheelChangedListener listener : changingListeners) {
            listener.onChangedDiff(this, diff);
        }
    }


	/**
	 * Adds wheel scrolling listener
	 * @param listener the listener
	 */
	public void addScrollingListener(OnWheelScrollListener listener) {
		scrollingListeners.add(listener);
	}

	/**
	 * Removes wheel scrolling listener
	 * @param listener the listener
	 */
	public void removeScrollingListener(OnWheelScrollListener listener) {
		scrollingListeners.remove(listener);
	}

	/**
	 * Notifies listeners about starting scrolling
	 */
	protected void notifyScrollingListenersAboutStart() {
		for (OnWheelScrollListener listener : scrollingListeners) {
			listener.onScrollingStarted(this);
		}
	}

	/**
	 * Notifies listeners about ending scrolling
	 */
	protected void notifyScrollingListenersAboutEnd() {
		for (OnWheelScrollListener listener : scrollingListeners) {
			listener.onScrollingFinished(this);
		}
	}

    /**
     * Adds wheel clicking listener
     * @param listener the listener
     */
    public void addClickingListener(OnWheelClickedListener listener) {
        clickingListeners.add(listener);
    }

    /**
     * Removes wheel clicking listener
     * @param listener the listener
     */
    public void removeClickingListener(OnWheelClickedListener listener) {
        clickingListeners.remove(listener);
    }

    /**
     * Notifies listeners about clicking
     */
    protected void notifyClickListenersAboutClick(int item) {
        for (OnWheelClickedListener listener : clickingListeners) {
            listener.onItemClicked(this, item);
        }
    }

	/**
	 * Gets current value
	 *
	 * @return the current value
	 */
	public int getCurrentItem() {
		return currentItem;
	}

	/**
	 * Sets the current item. Does nothing when index is wrong.
	 *
	 * @param index the item index
	 * @param
	 */
	public void setCurrentItem(int index, boolean isNotify) {
		if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
			return; // throw?
		}

		int itemCount = viewAdapter.getItemsCount();
		if (index < 0 || index >= itemCount) {
			if (isCyclic) {
				while (index < 0) {
					index += itemCount;
				}
				index %= itemCount;
			} else{
				return;
			}
		}

		if (index != currentItem) {
			if (isNotify) {
                scrollingOffset = 0;
                int old = currentItem;
                currentItem = index;
                notifyChangingListeners(old, currentItem);
                invalidate();
			} else {
				scrollingOffset = 0;
				currentItem = index;
				invalidate();
			}
		}
	}

	/**
	 * Sets the current item w/o animation. Does nothing when index is wrong.
	 *
	 * @param index the item index
	 */
	public void setCurrentItem(int index) {
		setCurrentItem(index, false);
	}

	/**
	 * Tests if wheel is cyclic. That means before the 1st item there is shown the last one
	 * @return true if wheel is cyclic
	 */
	public boolean isCyclic() {
		return isCyclic;
	}

	/**
	 * Set wheel cyclic flag
	 * @param isCyclic the flag to set
	 */
	public void setCyclic(boolean isCyclic) {
		this.isCyclic = isCyclic;
		invalidateWheel(false);
	}

	/**
	 * Invalidates wheel
	 * @param clearCaches if true then cached views will be clear
	 */
    public void invalidateWheel(boolean clearCaches) {
        if (clearCaches) {
            recycle.clearAll();
            if (itemsLayout != null) {
                itemsLayout.removeAllViews();
            }
            scrollingOffset = 0;
        } else if (itemsLayout != null) {
            // cache all items
	        recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());
        }

        invalidate();
	}

	/**
	 * Initializes resources
	 */
	private void initResourcesIfNecessary() {
		if (centerDrawable == null) {
			centerDrawable = getContext().getResources().getDrawable(R.drawable.le_wheel_val);
		}

		if (topShadow == null) {
			topShadow = new GradientDrawable(Orientation.TOP_BOTTOM, SHADOWS_COLORS);
		}

		if (bottomShadow == null) {
			bottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, SHADOWS_COLORS);
		}
	}

	/**
	 * Calculates desired height for layout
	 *
	 * @param layout
	 *            the source layout
	 * @return the desired layout height
	 */
	private int getDesiredHeight(LinearLayout layout) {
		int desired = 0;
		if (layout != null && layout.getChildAt(0) != null) {
			itemHeight = layout.getChildAt(0).getMeasuredHeight()+ layout.getChildAt(0).getPaddingBottom()
                    + layout.getChildAt(0).getPaddingTop();
		}
		if(isVertical)
			desired = itemHeight * visibleItems;
		else
			desired = itemHeight;
		int minHeight = getSuggestedMinimumHeight();
		return Math.max(desired, minHeight);
	}

	/**
	 * Returns height of wheel item
	 * @return the item height
	 */
	public int getItemHeight() {
		if (itemHeight != 0) {
			return itemHeight;
		}

		if (itemsLayout != null && itemsLayout.getChildAt(0) != null) {
			itemHeight = itemsLayout.getChildAt(0).getMeasuredHeight() + itemsLayout.getChildAt(0).getPaddingBottom()
                    + itemsLayout.getChildAt(0).getPaddingTop();
			return itemHeight;
		}

		return getHeight() / visibleItems;
	}

	/**
	 * Returnswidth of wheel item
	 * @return the item width
	 */
	//private int getItemHeight() {
	public int getItemWidth() {
        if (itemWidth != 0) {
            return itemWidth;
        }

        if (itemsLayout != null && itemsLayout.getChildAt(0) != null) {
            itemWidth = itemsLayout.getChildAt(0).getMeasuredWidth();
            return itemWidth;
        }
		return itemsWidth/visibleItems;
	}

	/**
	 * Calculates control width and creates text layouts
	 * @param widthSize the input layout width
	 * @param mode the layout mode
	 * @return the calculated control width
	 */
	private int calculateLayoutWidth(int widthSize, int mode) {
		initResourcesIfNecessary();
		itemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	    itemsLayout.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
	    int width = itemsLayout.getMeasuredWidth();

		if (mode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else {
			width += 2 * PADDING;
			// Check against our minimum width
			width = Math.max(width, getSuggestedMinimumWidth());

			if (mode == MeasureSpec.AT_MOST && widthSize < width) {
				width = widthSize;
			}
		}
        itemsLayout.measure(MeasureSpec.makeMeasureSpec(width - 2 * PADDING, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		return width;
	}

    private void initItem() {
        if (viewAdapter instanceof AbstractWheelTextAdapter && viewAdapter!=null) {
            AbstractWheelTextAdapter textAdapter = (AbstractWheelTextAdapter)viewAdapter;
            if(itemTextSize>0)
                textAdapter.setTextSize(itemTextSize);
            if(itemStrokeWidth>0)
                textAdapter.setStrokeWidth(itemStrokeWidth);
        }
    }

    private void initLabelPaint() {
        if (labelPaint==null) {
            labelPaint = new TextPaint();
            labelPaint.setTypeface(Typeface.create("hans-sans-light", 0));
        }

        labelPaint.setAntiAlias(true);
        if(labelStrokeWidth>0) {
            labelPaint.setStrokeWidth(labelStrokeWidth);
            labelPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        }
        labelPaint.setColor(centerColor);
        if (labelTextSize>0) {
            float scale = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, labelTextSize, Resources.getSystem().getDisplayMetrics());
            labelPaint.setTextSize((int)(scale + 0.5f));
            fontMetrics = labelPaint.getFontMetricsInt();
        }
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		buildViewForMeasuring();

        int paddingRight = getPaddingRight();
        int paddingLeft = getPaddingLeft();
		int width = calculateLayoutWidth(widthSize, widthMode) + paddingRight + paddingLeft;
            itemsWidth = width;

		int height;
		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height = getDesiredHeight(itemsLayout);

			if (heightMode == MeasureSpec.AT_MOST) {
				height = Math.min(height, heightSize);
			}
		}

		if(!TextUtils.isEmpty(label)&&(labelPaint!=null)&&isVertical) {
			Rect rect = new Rect();
			labelPaint.getTextBounds(label, 0, label.length(), rect);
			labelHeight = rect.height();
			labelWidth = rect.width();
			width+=labelOffset+labelWidth;
		}
        if(isVertical)
            setMeasuredDimension(width += 2 * PADDING, height);
        else
            setMeasuredDimension(width, height);
	}

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	layout(itemsWidth, b - t);
    }

    /**
     * Sets layouts width and height
     * @param width the layout width
     * @param height the layout height
     */
    private void layout(int width, int height) {
		itemsLayout.layout(0, 0, itemsWidth, height);
    }

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (viewAdapter != null && viewAdapter.getItemsCount() > 0) {
	        updateView();
	        if(label==null||label.length()==0)
	        	drawItems(canvas);
	        else {
	        	drawItems(canvas);
	        	drawLabel(canvas);
	        }
		}
	}
	/**
	 * draw label
	 */
	private void drawLabel(Canvas canvas) {
		if(label!=null||label.isEmpty()) {
			float x = itemsLayout.getWidth()-getPaddingRight();
			int top = getHeight()/2 - getItemHeight()/2;
			int bottom = getHeight()/2 + getItemHeight()/2;
			int baseline =top + (bottom - top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;

			canvas.drawText(label, x+labelOffset, baseline, labelPaint);
		}
	}
	/**
	 * Draws shadows on top and bottom of control
	 * @param canvas the canvas for drawing
	 */
	private void drawShadows(Canvas canvas) {
		int height = (int)(1.5 * getItemHeight());
		topShadow.setBounds(0, 0, getWidth(), height);
		topShadow.draw(canvas);
		bottomShadow.setBounds(0, getHeight() - height, getWidth(), getHeight());
		bottomShadow.draw(canvas);
	}

	/**
	 * Draws items
	 * @param canvas the canvas for drawing
	 */
	private void drawItems(Canvas canvas) {
      if(isVertical) {
          canvas.save();
          int top = 0;
          top = (currentItem - firstItem) * getItemHeight() + (getItemHeight() - getHeight()) / 2;
          canvas.translate(PADDING, - top + scrollingOffset);
          if(itemsLayout!=null)
              itemsLayout.draw(canvas);
          canvas.restore();
      } else {
          if(itemsLayout!=null)
              itemsLayout.draw(canvas);
      }

	}

	/**
	 * Draws rect for current value
	 * @param canvas the canvas for drawing
	 */
	private void drawCenterRect(Canvas canvas) {
		int center = getHeight() / 2;
		int offset = (int) (getItemHeight() / 2 );
		Paint mPaint = new Paint();
		mPaint.setStrokeWidth(1);
		int childHeight = getItemHeight();
		if(isVertical) {
			mPaint.setColor(0xF0777777);
			canvas.drawLine(0, center - offset, getWidth(), center - offset, mPaint);
			canvas.drawLine(0, center + offset, getWidth(), center + offset, mPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled() || getViewAdapter() == null) {
			return true;
		}

		switch (event.getAction()) {
		    case MotionEvent.ACTION_DOWN:
		        if (getParent() != null) {
		            getParent().requestDisallowInterceptTouchEvent(true);
		        }
		        break;

		    case MotionEvent.ACTION_UP:
		        if (!isScrollingPerformed&&isVertical) {
		            int distance = (int) event.getY() - getHeight() / 2;
		            if (distance > 0) {
		                distance += getItemHeight() / 2;
		            } else {
                        distance -= getItemHeight() / 2;
		            }
		            int items = distance / getItemHeight();
		            if (items != 0 && isValidItemIndex(currentItem + items)) {
	                    notifyClickListenersAboutClick(currentItem + items);
		            }
		        } else if(!isScrollingPerformed&&!isVertical) {
                    int distance = (int) event.getX() - getWidth() / 2;
                    if (distance > 0) {
                        distance += getItemWidth() / 2;
                    } else {
                        distance -= getItemWidth() / 2;
                    }
                    int items = distance / getItemWidth();
                    if (items !=0 && isValidItemIndex(currentItem + items)) {
                        notifyClickListenersAboutClick(currentItem + items);
                    }
                }
		        break;
		}

		return scroller.onTouchEvent(event);
	}

	/**
	 * Scrolls the wheel
	 * @param delta the scrolling value
	 */
	private void doScroll(int delta) {
		scrollingOffset += delta;
		uScrollingOffset += delta;
        if(isVertical) {
            int itemHeight = getItemHeight();
            int count = scrollingOffset / itemHeight;

            int pos = currentItem - count;
            int itemCount = viewAdapter.getItemsCount();

            int fixPos = scrollingOffset % itemHeight;
            if (Math.abs(fixPos) <= itemHeight / 2) {
                fixPos = 0;
            }
            if (isCyclic && itemCount > 0) {
                if (fixPos > 0) {
                    pos--;
                    count++;
                } else if (fixPos < 0) {
                    pos++;
                    count--;
                }
                // fix position by rotating
                while (pos < 0) {
                    pos += itemCount;
                }
                pos %= itemCount;
            } else {
                //
                if (pos < 0) {
                    count = currentItem;
                    pos = 0;
                } else if (pos >= itemCount) {
                    count = currentItem - itemCount + 1;
                    pos = itemCount - 1;
                } else if (pos > 0 && fixPos > 0) {
                    pos--;
                    count++;
                } else if (pos < itemCount - 1 && fixPos < 0) {
                    pos++;
                    count--;
                }
            }

            int offset = scrollingOffset;
            if (pos != currentItem) {
                curItem_uncyclic-=count;
                setCurrentItem(pos, true);
                notifyChangingListeners(count);
            } else {
                invalidate();
            }
            // update offset
            scrollingOffset = offset - count * itemHeight;
            if (scrollingOffset > getHeight()) {
                scrollingOffset = scrollingOffset % getHeight() + getHeight();
            }
            //update UOffset
            int temUScrollingOffset = uScrollingOffset % itemHeight;
            if(0==scrollingOffset) {
                uScrollingOffset=0;
            }
            else if(Math.abs(uScrollingOffset)>itemHeight||(Math.abs(scrollingOffset)+Math.abs(temUScrollingOffset))==itemHeight||scrollingOffset==temUScrollingOffset) {
                uScrollingOffset = uScrollingOffset % itemHeight;
            }

        } else {
            int itemWidth = getItemWidth();
            int count = scrollingOffset / itemWidth;

            int pos = currentItem - count;
            int itemCount = viewAdapter.getItemsCount();

            int fixPos = scrollingOffset % itemWidth;
            if (Math.abs(fixPos) <= itemWidth / 2) {
                fixPos = 0;
            }
            if (isCyclic && itemCount > 0) {
                if (fixPos > 0) {
                    pos--;
                    count++;
                } else if (fixPos < 0) {
                    pos++;
                    count--;
                }
                // fix position by rotating
                while (pos < 0) {
                    pos += itemCount;
                }
                pos %= itemCount;
            } else {
                //
                if (pos < 0) {
                    count = currentItem;
                    pos = 0;
                } else if (pos >= itemCount) {
                    count = currentItem - itemCount + 1;
                    pos = itemCount - 1;
                } else if (pos > 0 && fixPos > 0) {
                    pos--;
                    count++;
                } else if (pos < itemCount - 1 && fixPos < 0) {
                    pos++;
                    count--;
                }
            }

            int offset = scrollingOffset;
            if (pos != currentItem) {
                curItem_uncyclic+=count;
                setCurrentItem(pos, true);
                notifyChangingListeners(count);
            } else {
                invalidate();
            }
            // update offset
            scrollingOffset = offset - count * itemWidth;
            if (scrollingOffset > getWidth()) {
                scrollingOffset = scrollingOffset % getWidth() + getWidth();
            }
            //update UOffset
            int temUScrollingOffset = uScrollingOffset % itemWidth;
            if(0==scrollingOffset) {
                uScrollingOffset=0;
            }
            else if(Math.abs(uScrollingOffset)>itemWidth||(Math.abs(scrollingOffset)+Math.abs(temUScrollingOffset))==itemWidth||scrollingOffset==temUScrollingOffset)
                uScrollingOffset = uScrollingOffset % itemWidth;
        }
	}

	/**
	 * Scroll the wheel
	 * @param itemsToScroll items to scroll
	 * @param time scrolling duration
	 */
	public void scroll(int itemsToScroll, int time) {
		int distance = itemsToScroll * getItemHeight() - scrollingOffset;
        scroller.scroll(distance, time);
	}

	/**
	 * Calculates range for wheel items
	 * @return the items range
	 */
	public ItemsRange getItemsRange() {
        int first = 0;
        if(viewAdapter instanceof DayArrayAdapter)
            first = curItem_uncyclic;
        else
            first = currentItem;
        int count = 1;
        if(isVertical) {
            if (getItemHeight() == 0) {
                return null;
            }
            while (count * getItemHeight() < getHeight()) {
                first--;
                count += 2; // top + bottom items
            }

            if (scrollingOffset != 0) {
                if (scrollingOffset > 0) {
                    first--;
                }
                count++;
                // process empty items above the first or below the second
                int emptyItems = scrollingOffset / getItemHeight();
                first -= emptyItems;
                count += Math.asin(emptyItems);
            }
        } else {
            if (getItemWidth() == 0) {
                return null;
            }
            while (count * getItemWidth() < itemsWidth) {
                first--;
                count += 2; // top + bottom items
            }

            if (scrollingOffset != 0) {
                if (scrollingOffset > 0) {
                    first--;
                }
                count++;
                // process empty items above the first or below the second
                int emptyItems = scrollingOffset / getItemHeight();
                first -= emptyItems;
                count += Math.asin(emptyItems);
            }
        }
        return new ItemsRange(first, count);
	}

    private int getFirstItemCyclic() {
        ItemsRange range = getItemsRange();
        int first = range.getFirst();
        int firstCyclic = first - curItem_uncyclic + currentItem;
        return firstCyclic;
    }

    public void setFirstItemUnCyclic(int first) {
        curItem_uncyclic = first;
    }

	/**
	 * Rebuilds wheel items if necessary. Caches all unused items.
	 *
	 * @return true if items are rebuilt
	 */
	private boolean rebuildItems() {
		boolean updated = false;
		ItemsRange range = getItemsRange();
        int tempFirst = 0;
		if (itemsLayout != null) {
			int temp = recycle.recycleItems(itemsLayout, firstItem, range);
			updated = firstItem != temp;
            tempFirst = temp;
		} else {
			createItemsLayout();
			updated = true;
		}

		if (!updated) {
			updated = tempFirst != range.getFirst() || itemsLayout.getChildCount() != range.getCount();
		}

		if (tempFirst > range.getFirst() && tempFirst <= range.getLast()) {
			for (int i = tempFirst - 1; i >= range.getFirst(); i--) {
				if (!addViewItem(i, true)) {
				    break;
				}
                tempFirst = i;
				}
		} else {
		     tempFirst = range.getFirst();
		}

		int temp = tempFirst;
		for (int i = itemsLayout.getChildCount(); i < range.getCount(); i++) {
			if (!addViewItem(tempFirst + i, false) && itemsLayout.getChildCount() == 0) {
                temp++;
			}

		}
        int c = getFirstItemCyclic();
        if(viewAdapter instanceof DayArrayAdapter)
            firstItem = temp-range.getFirst()+c;
        else
            firstItem = temp;
		itemsLayout.requestLayout();
		return updated;
	}
	/**
	 * Updates view. Rebuilds items and label if necessary, recalculate items sizes.
	 */
	private void updateView() {
		if (rebuildItems()) {
			calculateLayoutWidth(itemsLayout.getWidth(), MeasureSpec.EXACTLY);
			layout(itemsLayout.getWidth(), itemsLayout.getHeight());
		}
	}

	/**
	 * Creates item layouts if necessary
	 */
	private void createItemsLayout() {
		if (itemsLayout == null) {
			itemsLayout = new MyLinearLayout(getContext());

			if(isVertical)
				itemsLayout.setOrientation(LinearLayout.VERTICAL);
			else
				itemsLayout.setOrientation(LinearLayout.HORIZONTAL);
			itemsLayout.setWhellView(this);
		}
	}

	/**
	 * Builds view for measuring
	 */
	private void buildViewForMeasuring() {
		// clear all items
		if (itemsLayout != null) {
			recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());
		} else {
			createItemsLayout();
		}
		// add views
		int addItems = visibleItems / 2;
		for (int i = currentItem + addItems; i >= currentItem - addItems; i--) {
			if (addViewItem(i, true)) {
			    firstItem = i;
			}
		}
	}

	/**
	 * Adds view for item to items layout
	 * @param index the item index
	 * @param first the flag indicates if view should be first
	 * @return true if corresponding item exists and is added
	 */
	private boolean addViewItem(int index, boolean first) {
		View view = getItemView(index);
		if (view != null) {
			if (first) {
				itemsLayout.addView(view, 0);
			} else {
				itemsLayout.addView(view);
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Checks whether intem index is valid
	 * @param index the item index
	 * @return true if item index is not out of bounds or the wheel is cyclic
	 */
	private boolean isValidItemIndex(int index) {

        if(viewAdapter instanceof DayArrayAdapter)
            return true;
        else
            return viewAdapter != null && viewAdapter.getItemsCount() > 0 &&
                    (isCyclic || index >=0 && index < viewAdapter.getItemsCount());
	}
	
	/**
	 * Returns view for specified item
	 * @param index the item index
	 * @return item view or empty view if index is out of bounds
	 */
    private View getItemView(int index) {
    	
    	if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
			return null;
		}
		int count = viewAdapter.getItemsCount();
		
		if (!isValidItemIndex(index)) {
			return viewAdapter.getEmptyItem(recycle.getEmptyItem(), itemsLayout);
			
		} else {
            if(viewAdapter instanceof DayArrayAdapter)
                return viewAdapter.getItem(index, recycle.getItem(), itemsLayout);
            else
                while (index < 0) {
                    index = count + index;
                }
            index %= count;
            return viewAdapter.getItem(index, recycle.getItem(), itemsLayout);
		}
	}
	
	/**
	 * Stops scrolling
	 */
	public void stopScrolling() {
	    scroller.stopScrolling();
	}
}
