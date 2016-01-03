/**
 *
 * Copyright 2015 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.login.login;

import java.util.Observable;

import com.letv.mobile.login.model.LoginConstants;

/**
 * This class extends from Observable. Just ensure notify is working in ui
 * thread
 * @author xiaqing
 */
public class LoginObservable extends Observable {
    @Override
    public void notifyObservers() {
        // NOTE(qingxia): We always notify observer in ui thread
        LoginConstants.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                LoginObservable.super.notifyObservers();
            }
        });
    }

    @Override
    public void notifyObservers(final Object object) {
        // NOTE(qingxia): We always notify observer in ui thread
        LoginConstants.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                LoginObservable.super.notifyObservers(object);
            }
        });
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }
}
