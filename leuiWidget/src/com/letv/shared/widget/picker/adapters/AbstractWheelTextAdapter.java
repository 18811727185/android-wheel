package com.letv.shared.widget.picker.adapters;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.letv.shared.R;

/**
 * Abstract wheel adapter provides common functionality for adapters.
 * @author mengfengxiao@letv.com
 */
public abstract class AbstractWheelTextAdapter extends AbstractWheelAdapter {
    
    /** Text view resource. Used as a default view for adapter. */
    public static final int TEXT_VIEW_ITEM_RESOURCE = -1;
    
    /** No resource constant. */
    protected static final int NO_RESOURCE = 0;
    
    /** Default text color */
    public static final int DEFAULT_TEXT_COLOR = 0xFF111111;
    
    /** Default text size */
    public static final int DEFAULT_TEXT_SIZE = 20;
    
    // Text settings
    private int textColor = DEFAULT_TEXT_COLOR;
    private int textSize = DEFAULT_TEXT_SIZE;
    private float strokeWidth = 1.0f;
    
    // Current context
    protected Context context;
    // Layout inflater
    protected LayoutInflater inflater;
    
    // Items resources
    protected int itemResourceId;
    protected int itemTextResourceId;
    
    // Empty items resources
    protected int emptyItemResourceId;

    protected boolean isVertical = true;
	
    /**
     * Constructor
     * @param context the current context
     */
    protected AbstractWheelTextAdapter(Context context) {
        this(context, TEXT_VIEW_ITEM_RESOURCE);
    }

    /**
     * Constructor
     * @param context the current context
     * @param itemResource the resource ID for a layout file containing a TextView to use when instantiating items views
     */
    protected AbstractWheelTextAdapter(Context context, int itemResource) {
        this(context, itemResource, NO_RESOURCE);
    }
    
    /**
     * Constructor
     * @param context the current context
     * @param itemResource the resource ID for a layout file containing a TextView to use when instantiating items views
     * @param itemTextResource the resource ID for a text view in the item layout
     */
    protected AbstractWheelTextAdapter(Context context, int itemResource, int itemTextResource) {
        this.context = context;
        itemResourceId = itemResource;
        itemTextResourceId = itemTextResource;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    /**
     * Gets text color
     * @return the text color
     */
    public int getTextColor() {
        return textColor;
    }
    
    /**
     * Sets text color
     * @param textColor the text color to set
     */
    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
    
    /**
     * Gets text size
     * @return the text size
     */
    public int getTextSize() {
        return textSize;
    }
    
    /**
     * set strokeWidth
     */
    public float getStrokeWidth() {
    	return strokeWidth;
    }
    
    /**
     * set strokeWidth
     */
    public void setStrokeWidth(float width) {
    	strokeWidth = width;
    }
    /**
     * Sets text size
     * @param textSize the text size to set
     */
    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }
    
    /**
     * Gets resource Id for items views
     * @return the item resource Id
     */
    public int getItemResource() {
        return itemResourceId;
    }
    
    /**
     * Sets resource Id for items views
     * @param itemResourceId the resource Id to set
     */
    public void setItemResource(int itemResourceId) {
        this.itemResourceId = itemResourceId;
    }
    
    /**
     * Gets resource Id for text view in item layout 
     * @return the item text resource Id
     */
    public int getItemTextResource() {
        return itemTextResourceId;
    }
    
    /**
     * Sets resource Id for text view in item layout 
     * @param itemTextResourceId the item text resource Id to set
     */
    public void setItemTextResource(int itemTextResourceId) {
        this.itemTextResourceId = itemTextResourceId;
    }

    /**
     * Gets resource Id for empty items views
     * @return the empty item resource Id
     */
    public int getEmptyItemResource() {
        return emptyItemResourceId;
    }

    /**
     * Sets resource Id for empty items views
     * @param emptyItemResourceId the empty item resource Id to set
     */
    public void setEmptyItemResource(int emptyItemResourceId) {
        this.emptyItemResourceId = emptyItemResourceId;
    }
    
    
    /**
     * Returns text for specified item
     * @param index the item index
     * @return the text of specified items
     */
    protected abstract CharSequence getItemText(int index);

    @Override
    public View getItem(int index, View convertView, ViewGroup parent) {
    	if (index >= 0 && index < getItemsCount() || this instanceof DayArrayAdapter) {
            if (convertView == null) {
                convertView = getView(itemResourceId, parent);
            }
            TextView textView = getTextView(convertView, itemTextResourceId);
            if (textView != null) {
                CharSequence text = getItemText(index);
                if (text == null) {
                    text = "";
                }
                textView.setText(text);
//                if (itemResourceId == TEXT_VIEW_ITEM_RESOURCE) {
                    configureTextView(textView);
                    convertView = textView;
//                }
            }
            return convertView;
        }
    	return null;
    }

    @Override
    public View getEmptyItem(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = getView(emptyItemResourceId, parent);
        }
        if (emptyItemResourceId == TEXT_VIEW_ITEM_RESOURCE && convertView instanceof TextView) {
            configureTextView((TextView)convertView);
        }
            
        return convertView;
	}

    /**
     * Configures text view. Is called for the TEXT_VIEW_ITEM_RESOURCE views.
     * @param view the text view to be configured
     */
    protected void configureTextView(TextView view) {
        view.setTextColor(textColor);
         view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);

//        view.setGravity(Gravity.CENTER);
//        view.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
//        view.setLines(1);
//        TextPaint p = view.getPaint();
//        p.setStrokeWidth(strokeWidth);
//        p.setStyle(Paint.Style.FILL_AND_STROKE);
//        p.setStyle(Paint.Style.);
//        view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
    }
    
    /**
     * Loads a text view from view
     * @param view the text view or layout containing it
     * @param textResource the text resource Id in layout
     * @return the loaded text view
     */
    public TextView getTextView(View view, int textResource) {
    	TextView text = null;
    	try {
            if (textResource == NO_RESOURCE && view instanceof TextView) {
                text = (TextView) view;
            } else if (textResource != NO_RESOURCE) {
                text = (TextView) view.findViewById(textResource);
            }
        } catch (ClassCastException e) {
            Log.e("AbstractWheelAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "AbstractWheelAdapter requires the resource ID to be a TextView", e);
        }
        return text;
    }
    
    /**
     * Loads view from resources
     * @param resource the resource Id
     * @return the loaded view or null if resource is not set
     */
    public View getView(int resource, ViewGroup parent) {
        switch (resource) {
        case NO_RESOURCE:
            return null;
        case TEXT_VIEW_ITEM_RESOURCE:
        	TextView textView = new TextView(context);
        	textView.setTextSize(textSize);
        	return textView;
        default:
            return inflater.inflate(resource, parent, false);    
        }
    }

    public void setOritentation(boolean isVertical) {
        this.isVertical = isVertical;
        if(this.isVertical) {
            setItemResource(R.layout.le_vertical_wheel_text_item);
        } else {
            setItemResource(R.layout.le_horizontal_wheel_text_item);
        }
        setItemTextResource(R.id.text);
    }

    public boolean getOritentaion() {
        return isVertical;
    }
}
