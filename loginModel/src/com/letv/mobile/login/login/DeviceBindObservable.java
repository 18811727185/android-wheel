package com.letv.mobile.login.login;

import java.util.Observable;

import com.letv.mobile.login.model.LoginConstants;

/**
 * This class extends from Observable. Just ensure notify is working in ui
 * thread
 * @author shibin
 */
public class DeviceBindObservable extends Observable {
    @Override
    public void notifyObservers() {
        LoginConstants.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                DeviceBindObservable.super.notifyObservers();
            }
        });
    }

    @Override
    public void notifyObservers(final Object object) {
        LoginConstants.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                DeviceBindObservable.super.notifyObservers(object);
            }
        });
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }
}
