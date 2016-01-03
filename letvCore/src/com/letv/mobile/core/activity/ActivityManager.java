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

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;

import com.letv.mobile.core.utils.Utils;

/**
 * @Description: NOTE(qingxia): We use both Activity and FragmentActivity as
 *               base
 *               activity. So we have to move manager logic out of Activity and
 *               FragmentActivity
 * @author qingxia
 */
final class ActivityManager {
    private static final String APLICATION_IN_BACKGROUND_FLAG = "aplication_in_background_flag";
    private static final ActivityManager sInstance = new ActivityManager();
    private static Activity sTopActivity = null;
    private static final List<Activity> sActivities = new LinkedList<Activity>();

    public static ActivityManager getInstance() {
        return sInstance;
    }

    private ActivityManager() {
        // Do nothing now.
    }

    /**
     * @return the sTopActivity
     */
    public Activity getTopActivity() {
        return sTopActivity;
    }

    public List<Activity> getActivities() {
        return sActivities;
    }

    public String getAplicationInBackgroundFlag() {
        return APLICATION_IN_BACKGROUND_FLAG;
    }

    public void finishAllActivities() {
        // TODO(qingxia): Remove top dialogs. Add if needed
        // BaseDialog.dismissAllDialogs();
        for (int i = sActivities.size() - 1; i >= 0; i--) {
            Activity activity = sActivities.get(i);
            if (activity.getParent() != null) {
                activity.getParent().finish();
            }
            activity.finish();
        }
    }

    public void onPause(Activity activity) {
        sTopActivity = null;
        // NOTE(xiaqing) : 检测应用是否在前台，如果不在，就发广播
        if (activity != null && !Utils.isRunningForeground(activity)) {
            Intent intent = new Intent(activity.getPackageName()
                    + APLICATION_IN_BACKGROUND_FLAG);
            activity.sendBroadcast(intent);
        }
    }

    public void onCreate(Activity activity) {
        assert (activity != null);
        sActivities.add(activity);
    }

    public void onResume(Activity activity) {
        assert (activity != null);
        sTopActivity = activity;
    }

    public void onDestroy(Activity activity) {
        assert (activity != null);
        // NOTE(qingxia): LinkedList will handle activity is not in list or null
        sActivities.remove(activity);
    }
}
