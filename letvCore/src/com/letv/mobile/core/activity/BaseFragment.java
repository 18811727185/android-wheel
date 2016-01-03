/**
 *
 * Copyright 2013 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.core.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * All the fragment should extends from this class
 *
 * @author xiaqing
 */
public class BaseFragment extends Fragment {

    /**
     * called to do initial creation of the fragment
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // NOTE(baiwenlong):如果不支持重建，就调用remove的方法，需要在子类里边的onCreateView方法里边也添加类似的判断
        if (savedInstanceState != null && !this
                .canRecreateFromSavedInstance()) {
            this.getActivity().getFragmentManager().beginTransaction()
                    .remove(this).commit();
        }
    }

    /**
     * 是否可以从savedInstance里边重建
     *
     * @return
     */
    protected boolean canRecreateFromSavedInstance() {
        return true;
    }
}
