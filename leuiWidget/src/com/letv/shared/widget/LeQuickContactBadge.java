/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.letv.shared.widget;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.*;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.letv.shared.R;

import java.util.Locale;

/**
 * Widget used to show an image with the standard QuickContact badge
 * and on-click behavior.
 */
public class LeQuickContactBadge extends BorderedRoundedCornersImageView implements OnClickListener {
    private Uri mContactUri;
    private String mContactEmail;
    private String mContactPhone;
    //private Drawable mOverlay;
    private QueryHandler mQueryHandler;
    private Drawable mDefaultAvatar;
    private Bundle mExtras = null;

    protected String[] mExcludeMimes = null;

    static final private int TOKEN_EMAIL_LOOKUP = 0;
    static final private int TOKEN_PHONE_LOOKUP = 1;
    static final private int TOKEN_EMAIL_LOOKUP_AND_TRIGGER = 2;
    static final private int TOKEN_PHONE_LOOKUP_AND_TRIGGER = 3;

    static final private String EXTRA_URI_CONTENT = "uri_content";

    static final String[] EMAIL_LOOKUP_PROJECTION = new String[] {
        RawContacts.CONTACT_ID,
        Contacts.LOOKUP_KEY,
    };
    static final int EMAIL_ID_COLUMN_INDEX = 0;
    static final int EMAIL_LOOKUP_STRING_COLUMN_INDEX = 1;

    static final String[] PHONE_LOOKUP_PROJECTION = new String[] {
        PhoneLookup._ID,
        PhoneLookup.LOOKUP_KEY,
    };
    static final int PHONE_ID_COLUMN_INDEX = 0;
    static final int PHONE_LOOKUP_STRING_COLUMN_INDEX = 1;
    
    // ---- static -----
    static int[] sBackgroundColorList;
    static Object sObject = new Object();
    
    public static int[] getBackgroundColorList(Resources res) {
        if (res == null) {
            return null;
        }
        
        synchronized (sObject) {
            if (sBackgroundColorList == null) {
                TypedArray ta = res.obtainTypedArray(R.array.le_quick_contact_badge_background_color_lists);
                int N = ta.length();
                sBackgroundColorList = new int[N];
                for (int i=0; i<N; i++) {
                    int id = ta.getResourceId(i, 0);
                    if (id != 0)
                        sBackgroundColorList[i] = res.getColor(id);
                }
                ta.recycle();
            }
        }
        
        int[] ret = new int[sBackgroundColorList.length];
        System.arraycopy(sBackgroundColorList, 0, ret, 0, sBackgroundColorList.length);
        return ret;
    }
    // ----
    
    public static int getRandomBackgroundColor(Resources res) {
        if (res == null) {
            return 0;
        }
        
        int colorsList[] = getBackgroundColorList(res);
        if (colorsList == null) {
            return 0;
        }
        
        
        int random = (int)(Math.random() * colorsList.length);
        return colorsList[random];
    }

    public static int getBackgroundColorByName(Resources res, String displayName) {
        if (res == null) {
            return 0;
        }

        int colorsList[] = getBackgroundColorList(res);
        if (colorsList == null) {
            return 0;
        }
        int colorSize = colorsList.length;

        int position = 0;
        if (displayName != null) {
            int length = displayName.length();
            char nameValue = 0;
            for (int i = 0; i < length; i++) {
                if (i > 10) {
                    break;
                }
                nameValue += displayName.charAt(i);
            }
            int code = length * 17 + nameValue * 13;
            position = code % colorSize;
        } else {
            position = (int) (Math.random() * colorsList.length);
        }

        return colorsList[position];
    }

    public LeQuickContactBadge(Context context) {
        this(context, null);
    }

    public LeQuickContactBadge(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeQuickContactBadge(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (!isInEditMode()) {
            mQueryHandler = new QueryHandler(getContext().getContentResolver());
        }
        setOnClickListener(this);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
    }

    /** This call has no effect anymore, as there is only one QuickContact mode */
    @SuppressWarnings("unused")
    public void setMode(int size) {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBadgeText(canvas);
    }

    /** True if a contact, an email address or a phone number has been assigned */
    private boolean isAssigned() {
        return mContactUri != null || mContactEmail != null || mContactPhone != null;
    }

    /**
     * Resets the contact photo to the default state.
     */
    public void setImageToDefault() {
        if (mDefaultAvatar == null) {
            mDefaultAvatar = getResources().getDrawable(R.drawable.le_ic_quick_contact_default_picture);
        }
        setImageDrawable(mDefaultAvatar);
        setContactBadgeText(null);
        setBackground(null);
    }

    /**
     * Assign the contact uri that this QuickContactBadge should be associated
     * with. Note that this is only used for displaying the QuickContact window and
     * won't bind the contact's photo for you. Call {@link #setImageDrawable(android.graphics.drawable.Drawable)} to set the
     * photo.
     *
     * @param contactUri Either a {@link android.provider.ContactsContract.Contacts#CONTENT_URI} or
     *            {@link android.provider.ContactsContract.Contacts#CONTENT_LOOKUP_URI} style URI.
     */
    public void assignContactUri(Uri contactUri) {
        mContactUri = contactUri;
        mContactEmail = null;
        mContactPhone = null;
        onContactUriChanged();
    }

    /**
     * Assign a contact based on an email address. This should only be used when
     * the contact's URI is not available, as an extra query will have to be
     * performed to lookup the URI based on the email.
     *
     * @param emailAddress The email address of the contact.
     * @param lazyLookup If this is true, the lookup query will not be performed
     * until this view is clicked.
     */
    public void assignContactFromEmail(String emailAddress, boolean lazyLookup) {
        assignContactFromEmail(emailAddress, lazyLookup, null);
    }

    /**
     * Assign a contact based on an email address. This should only be used when
     * the contact's URI is not available, as an extra query will have to be
     * performed to lookup the URI based on the email.

     @param emailAddress The email address of the contact.
     @param lazyLookup If this is true, the lookup query will not be performed
     until this view is clicked.
     @param extras A bundle of extras to populate the contact edit page with if the contact
     is not found and the user chooses to add the email address to an existing contact or
     create a new contact. Uses the same string constants as those found in
     {@link android.provider.ContactsContract.Intents.Insert}
    */

    public void assignContactFromEmail(String emailAddress, boolean lazyLookup, Bundle extras) {
        mContactEmail = emailAddress;
        mExtras = extras;
        if (!lazyLookup && mQueryHandler != null) {
            mQueryHandler.startQuery(TOKEN_EMAIL_LOOKUP, null,
                    Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(mContactEmail)),
                    EMAIL_LOOKUP_PROJECTION, null, null, null);
        } else {
            mContactUri = null;
            onContactUriChanged();
        }
    }


    /**
     * Assign a contact based on a phone number. This should only be used when
     * the contact's URI is not available, as an extra query will have to be
     * performed to lookup the URI based on the phone number.
     *
     * @param phoneNumber The phone number of the contact.
     * @param lazyLookup If this is true, the lookup query will not be performed
     * until this view is clicked.
     */
    public void assignContactFromPhone(String phoneNumber, boolean lazyLookup) {
        assignContactFromPhone(phoneNumber, lazyLookup, new Bundle());
    }

    /**
     * Assign a contact based on a phone number. This should only be used when
     * the contact's URI is not available, as an extra query will have to be
     * performed to lookup the URI based on the phone number.
     *
     * @param phoneNumber The phone number of the contact.
     * @param lazyLookup If this is true, the lookup query will not be performed
     * until this view is clicked.
     * @param extras A bundle of extras to populate the contact edit page with if the contact
     * is not found and the user chooses to add the phone number to an existing contact or
     * create a new contact. Uses the same string constants as those found in
     * {@link android.provider.ContactsContract.Intents.Insert}
     */
    public void assignContactFromPhone(String phoneNumber, boolean lazyLookup, Bundle extras) {
        mContactPhone = phoneNumber;
        mExtras = extras;
        if (!lazyLookup && mQueryHandler != null) {
            mQueryHandler.startQuery(TOKEN_PHONE_LOOKUP, null,
                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, mContactPhone),
                    PHONE_LOOKUP_PROJECTION, null, null, null);
        } else {
            mContactUri = null;
            onContactUriChanged();
        }
    }

    private void onContactUriChanged() {
        setEnabled(isAssigned());
    }

    @Override
    public void onClick(View v) {
        // If contact has been assigned, mExtras should no longer be null, but do a null check
        // anyway just in case assignContactFromPhone or Email was called with a null bundle or
        // wasn't assigned previously.
        final Bundle extras = (mExtras == null) ? new Bundle() : mExtras;
        if (mContactUri != null) {
            //QuickContact.showQuickContact(getContext(), LeQuickContactBadge.this, mContactUri,
            //        QuickContact.MODE_LARGE, mExcludeMimes);
            
            // According to LEUI UX, once clicking a LeQuickContactBadge, contacts detail activity will be shown
            final Intent intent = new Intent(Intent.ACTION_VIEW, mContactUri);
            final int intentFlags = (getContext() instanceof Activity)
                    ? Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                    : Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK;
            intent.addFlags(intentFlags);
            getContext().startActivity(intent);
        } else if (mContactEmail != null && mQueryHandler != null) {
            extras.putString(EXTRA_URI_CONTENT, mContactEmail);
            mQueryHandler.startQuery(TOKEN_EMAIL_LOOKUP_AND_TRIGGER, extras,
                    Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(mContactEmail)),
                    EMAIL_LOOKUP_PROJECTION, null, null, null);
        } else if (mContactPhone != null && mQueryHandler != null) {
            extras.putString(EXTRA_URI_CONTENT, mContactPhone);
            mQueryHandler.startQuery(TOKEN_PHONE_LOOKUP_AND_TRIGGER, extras,
                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, mContactPhone),
                    PHONE_LOOKUP_PROJECTION, null, null, null);
        } else {
            // If a contact hasn't been assigned, don't react to click.
            return;
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(LeQuickContactBadge.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(LeQuickContactBadge.class.getName());
    }

    /**
     * Set a list of specific MIME-types to exclude and not display. For
     * example, this can be used to hide the {@link android.provider.ContactsContract.Contacts#CONTENT_ITEM_TYPE}
     * profile icon.
     */
    public void setExcludeMimes(String[] excludeMimes) {
        mExcludeMimes = excludeMimes;
    }

    private class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Uri lookupUri = null;
            Uri createUri = null;
            boolean trigger = false;
            Bundle extras = (cookie != null) ? (Bundle) cookie : new Bundle();
            try {
                switch(token) {
                    case TOKEN_PHONE_LOOKUP_AND_TRIGGER:
                        trigger = true;
                        createUri = Uri.fromParts("tel", extras.getString(EXTRA_URI_CONTENT), null);

                        //$FALL-THROUGH$
                    case TOKEN_PHONE_LOOKUP: {
                        if (cursor != null && cursor.moveToFirst()) {
                            long contactId = cursor.getLong(PHONE_ID_COLUMN_INDEX);
                            String lookupKey = cursor.getString(PHONE_LOOKUP_STRING_COLUMN_INDEX);
                            lookupUri = Contacts.getLookupUri(contactId, lookupKey);
                        }

                        break;
                    }
                    case TOKEN_EMAIL_LOOKUP_AND_TRIGGER:
                        trigger = true;
                        createUri = Uri.fromParts("mailto",
                                extras.getString(EXTRA_URI_CONTENT), null);

                        //$FALL-THROUGH$
                    case TOKEN_EMAIL_LOOKUP: {
                        if (cursor != null && cursor.moveToFirst()) {
                            long contactId = cursor.getLong(EMAIL_ID_COLUMN_INDEX);
                            String lookupKey = cursor.getString(EMAIL_LOOKUP_STRING_COLUMN_INDEX);
                            lookupUri = Contacts.getLookupUri(contactId, lookupKey);
                        }
                        break;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            mContactUri = lookupUri;
            onContactUriChanged();

            if (trigger && lookupUri != null) {
                // Found contact, so trigger QuickContact
                //QuickContact.showQuickContact(getContext(), LeQuickContactBadge.this, lookupUri,
                //        QuickContact.MODE_LARGE, mExcludeMimes);
                
                // According to LEUI UX, once clicking a LeQuickContactBadge, contacts detail activity will be shown
                final Intent intent = new Intent(Intent.ACTION_VIEW, mContactUri);
                final int intentFlags = (getContext() instanceof Activity)
                        ? Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                        : Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK;
                intent.addFlags(intentFlags);
                getContext().startActivity(intent);
            } else if (createUri != null) {
                // Prompt user to add this person to contacts
                final Intent intent = new Intent(Intents.SHOW_OR_CREATE_CONTACT, createUri);
                if (extras != null) {
                    extras.remove(EXTRA_URI_CONTENT);
                    intent.putExtras(extras);
                }
                final int intentFlags = (getContext() instanceof Activity)
                        ? Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                        : Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK;
                intent.addFlags(intentFlags);
                getContext().startActivity(intent);
            }
        }
    }
    
    private String mBadgeText;                 // Contact text
    private int mBadgeTextSize;                // Contact text size
    private int mBadgeTextColor = Color.WHITE; // Contact text color
    private int mBadgeTextShadowRadius;        // Contact shadow radius
    private int mBadgeTextShadowColor;         // Contact shadow color
    private Paint mBadgeTextPaint;
    private float DEFAULT_TEXT_SIZE_RATIO = 0.618f;
    private static final String MULTI_BLANK = "[ ]+";
    
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) {
            //|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
            //|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            //|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    private String getName(String name) {
        int len = name.length();
        char c;
        for (int i=len-1; i>=0; i--) {
            c = name.charAt(i);
            if (isChinese(c)) {
                return "" + c;
            }
        }

        String firstLetter = name.substring(0, 1);
        c = firstLetter.charAt(0);
        if(Character.isLetter(c)) {
            String[] subString = name.split(MULTI_BLANK);
            firstLetter = firstLetter.toUpperCase(Locale.getDefault());
            int length = subString.length;
            if(length >=2) {
                String lastLetter = subString[1].substring(0, 1);
                c = lastLetter.charAt(0);
                if(Character.isLetter(c)) {
                    lastLetter = lastLetter.toUpperCase(Locale.getDefault());
                    return firstLetter + lastLetter;
                } else
                    return firstLetter;

            } else {
                return firstLetter;
            }
        } else {
            return "";
        }
    }
    
    /**
     *  Set Contact text, only use the first character
     */
    public void setContactBadgeText(String badgeText) {
        if (TextUtils.isEmpty(badgeText)) {
            mBadgeText = "";
        } else {
            String text = badgeText.trim();
            if (TextUtils.isEmpty(text)) {
                mBadgeText = "";
            } else {
                mBadgeText = getName(badgeText);
            }
        }
        invalidate();
    }

    public boolean shouldUseDefaultImage(String badgeText) {
        if (TextUtils.isEmpty(badgeText)) {
            return true;
        } else {
            String text = badgeText.trim();
            if (TextUtils.isEmpty(text)) {
                return true;
            } else {
                int len = text.length();
                char c;
                for (int i = len - 1; i >= 0; i--) {
                    c = text.charAt(i);
                    if (isChinese(c))
                        return false;
                }

                String firstLetter = text.substring(0, 1);
                c = firstLetter.charAt(0);
                if (Character.isLetter(c))
                    return false;
                else
                    return true;
            }
        }
    }
    
    public void setContactBudageTextSizeResource(int res) {
        setContactBudageTextSize(getContext().getResources().getDimensionPixelSize(res));
    }
    
    /**
     *  Set contact text size
     *  You can obtain text size from xml like below:
     *      getResources().getDimensionPixelSize(R.dimen.xxx);
     */
    public void setContactBudageTextSize(int badgeTextSize) {
        if (badgeTextSize <= 0 || badgeTextSize == mBadgeTextSize) {
            return;
        }
        
        mBadgeTextSize = badgeTextSize;
        if (mBadgeTextPaint != null) {
            mBadgeTextPaint.setTextSize(mBadgeTextSize);
            
            if (!TextUtils.isEmpty(mBadgeText)) {
                invalidate();
            }
        }
    }
    
    public void setContactBudageTextColorResource(int res) {
        setContactBudageTextColor(getContext().getResources().getColor(res));
    }
    
    public void setContactBudageTextColor(int color) {
        if (color == mBadgeTextColor) {
            return;
        }
        
        mBadgeTextColor = color;
        if (mBadgeTextPaint != null) {
            mBadgeTextPaint.setColor(mBadgeTextColor);
            
            if (!TextUtils.isEmpty(mBadgeText)) {
                invalidate();
            }
        }
    }
    
    public void setContactBudageTextShadowResource(int shadowRadiusRes, int shdowColorRes) {
        Resources res = this.getContext().getResources();
        
        setContactBudageTextShadow(res.getDimensionPixelSize(shadowRadiusRes), 
                res.getColor(shdowColorRes));
    }
    
    /**
     *  Set contact text shadow
     *  You can obtain text size and color from xml like below:
     *      getResources().getDimensionPixelSize(R.dimen.xxx);
     *      getResources().getColor(R.color.xxx);
     */
    public void setContactBudageTextShadow(int shadowRadius, int shdowColor) {
        if (shadowRadius <= 0 || 
                (shadowRadius == mBadgeTextShadowRadius && mBadgeTextShadowColor == shdowColor) ) {
            return;
        }
        
        mBadgeTextShadowRadius = shadowRadius;
        mBadgeTextShadowColor = shdowColor;

        if (mBadgeTextPaint != null) {
            mBadgeTextPaint.setShadowLayer(mBadgeTextShadowRadius, 0, 0, mBadgeTextShadowColor);
            
            if (!TextUtils.isEmpty(mBadgeText)) {
                invalidate();
            }
        }
    }
    
    private void drawBadgeText(Canvas canvas) {
        if(TextUtils.isEmpty(mBadgeText)) {
            return;
        }
        
        Rect rect = new Rect();
        rect.set(0, 0, 
                this.getWidth() - this.getPaddingLeft() - this.getPaddingRight(), 
                this.getHeight() - this.getPaddingTop() - this.getPaddingBottom());
        
        // Set Paint which is used to draw contact text
        if (mBadgeTextPaint == null) {
            mBadgeTextPaint = new Paint();
            mBadgeTextPaint.setAntiAlias(true);
            mBadgeTextPaint.setTextAlign(Paint.Align.CENTER);
            
            mBadgeTextPaint.setColor(mBadgeTextColor);
            
            if (mBadgeTextSize == 0) {
                mBadgeTextSize = (int)(this.getWidth() * DEFAULT_TEXT_SIZE_RATIO);
            }
            mBadgeTextPaint.setTextSize(mBadgeTextSize);
            
            if (mBadgeTextShadowRadius != 0 && mBadgeTextShadowColor != 0) {
                mBadgeTextPaint.setShadowLayer(mBadgeTextShadowRadius, 0, 0, mBadgeTextShadowColor);
            }
        }
        
        // Draw contact text
        float baseX = (rect.left + rect.right) / 2;
        float baseY = (rect.top + rect.bottom) / 2;
        FontMetrics fontMetrics = mBadgeTextPaint.getFontMetrics();
        float fontTotalHeight = fontMetrics.bottom - fontMetrics.top;
        float offY = fontTotalHeight / 2 - fontMetrics.bottom;
        float newY = baseY + offY - 2;
        canvas.drawText(mBadgeText, baseX, newY, mBadgeTextPaint);
    }

}
