/**
 *
 * Copyright 2012 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.core.utils;

import android.os.Bundle;

/**
 * This class support a new Runnable that allow user to call with Bundle
 * parameter.
 * @author qingxia
 */
public abstract class MessageRunnable extends AbstractMessageRunnable<Bundle> {
    public MessageRunnable(Bundle data) {
        super(data);
    }
}
