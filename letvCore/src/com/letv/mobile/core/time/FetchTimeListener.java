/**
 *
 * Copyright 2013 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.core.time;

/**
 * Called while fetch current time asyn.
 * @author xiaqing
 */
public interface FetchTimeListener {
    // Called while fetch success.
    public void onFetchTimeResult(TimeErrorCode errorCode,
            ReferenceTime referenceTime);
}
