/**
 * Copyright 2012 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : xiaqing
 * @Description : 定义了top activity逻辑，以及new intent来启动新的activity的逻辑。
 */

package com.letv.mobile.core.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 这个类是所有Activity的基类，其中主要维护了两个变量：sTopActivity和sActivities。
 * 前者是这个APP正在显示的Activity，后者保存程序中所有的Activity，用于对程序的退出。
 * 在APP运行的过程中，可能会需要强制更新。可以通过showUpdateRequiredDialog(String, String)方法进行提示。
 * @author xiaqing
 */
@SuppressLint("NewApi")
public class BaseActivity extends Activity {

    private boolean isDestoryed = false;

    private static final ActivityManager sManager = ActivityManager
            .getInstance();

    public interface ActivityResultListener {
        public void onActivityResult(int resultCode, Intent data);
    }

    private final SparseArray<ActivityResultListener> listenerMap = new SparseArray<ActivityResultListener>();
    private int count = 0;

    public static Activity getTopActivity() {
        return BaseActivity.sManager.getTopActivity();
    }

    public static List<Activity> getActivities() {
        return BaseActivity.sManager.getActivities();
    }

    public static void finishAllActivities() {
        BaseActivity.sManager.finishAllActivities();
    }

    public static String getAplicationInBackgroundFlag() {
        return BaseActivity.sManager.getAplicationInBackgroundFlag();
    }

    /**
     * Start one activity, the requestCode will be handled automatically.
     * @param intent
     *            This intent contains the parameters to be passed to new
     *            activity.
     * @param listener
     *            The listener to call when the target activity returned. null
     *            if no return
     *            required.
     */
    public void transferActivity(Intent intent, ActivityResultListener listener) {
        if (listener == null) {
            this.startActivity(intent);
        } else {
            int requestCode = this.generateRequestCode();
            this.listenerMap.append(requestCode, listener);
            this.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * Start one activity, the requestCode will be handled automatically.
     * @param cls
     *            This class need to be explicit sent.
     * @param listener
     *            The listener to call when the target activity returned. null
     *            if no return
     *            required.
     */
    public void transferActivity(Class<?> cls, ActivityResultListener listener) {
        this.transferActivity(new Intent(this.getApplicationContext(), cls),
                listener);
    }

    /**
     * Start activity with no parameter.
     * @param dest
     *            The destination activity to be transferred.
     * @param listener
     */
    public void transferActivity(Activity dest, ActivityResultListener listener) {
        this.transferActivity(
                new Intent(this.getApplicationContext(), dest.getClass()),
                listener);
    }

    /**
     * Start activity without return expected.
     */
    public void transferActivity(Intent intent) {
        this.transferActivity(intent, null);
    }

    /**
     * Start activity without return expected with no parameter.
     */
    public void transferActivity(Activity dest) {
        this.transferActivity(dest, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ActivityResultListener listener = this.listenerMap.get(requestCode);
        if (listener != null) {
            listener.onActivityResult(resultCode, data);
            this.listenerMap.remove(requestCode);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BaseActivity.sManager.onPause(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseActivity.sManager.onCreate(this);
        this.isDestoryed = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BaseActivity.sManager.onResume(this);
    }

    // NOTE(baiwenlong):先去掉，要是再加上，需要修改"BaseFloatingActivity"，因为BaseFloatingActivity在onDestory时候获取它的前一个activity，要是加上了就获取不到了，需要改成在finish的时候获取前一个actiivty，onDestroy的时候用那个actiivty
    // @Override
    // final public void finish() {
    // super.finish();
    // sManager.onDestroy(this);
    // }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.isDestoryed = true;
        BaseActivity.sManager.onDestroy(this);
    }

    /**
     * One activity may handle the back pressed event itself, otherwise, leave
     * to normal
     * process. Sub classes can override this function to handle their own back
     * pressed
     * event.
     * @return false This activity will handle back pressed event. true This
     *         activity will
     *         not handle this event.
     */
    protected boolean onHandleBackPressed() {
        return false;
    }

    @Override
    public void onBackPressed() {
        if (this.onHandleBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    private int generateRequestCode() {
        return this.count++;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public boolean isDestroyedVersionSafe() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ? this
                .isDestroyed() : this.isDestoryed;
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 如果不需要保存fragment状态，就删除fragment的状态。大部分情况是不需要保存fragment状态的，如果保存了，程序处理不当，就遇到问题了。
        if (!this.canSaveFragmentsState()) {
            outState.remove(this.getFragmentTagForSaveInstance());
        }

    }

    /**
     * 是否保存fragment的状态
     * NOTE(baiwenlong):因为保存fragment的key是不公开的，随着系统变化，值可能会变，所以这个方法不是很可靠，
     * 需要配合BaseFragment来使用
     * @return
     */
    protected boolean canSaveFragmentsState() {
        return true;
    }

    /**
     * 获取onSaveInstanceState的时候，保存fragment用的key
     * NOTE(baiwenlong)：这个key是不公开的，所以通过反射来获取，如果获取失败，会返回一个默认的值
     * @return
     */
    protected String getFragmentTagForSaveInstance() {
        try {
            Field f = Activity.class.getDeclaredField("FRAGMENTS_TAG");
            f.setAccessible(true);
            Object fragmentTagObj = f.get(null);
            if (fragmentTagObj != null) {
                return String.valueOf(fragmentTagObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 如果没有读取到activity保存fragment状态的key，则返回默认的key值（默认的值是通过查看activity源码看到的）
        return "android:fragments";
    }

    /**
     * If the activity's onDestroy have been invoked.
     * @return
     */
    public boolean isDestroy() {
        return this.isDestoryed;
    }
}
