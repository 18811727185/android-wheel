/**
 *
 * Copyright 2015 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.common;

/**
 * @author xiaqing
 */
public interface AsynRequestListener {

    public void onSuccess(Object object);

    public void onFailed(String errorCode, String errorMsg);
}
