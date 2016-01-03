package com.letv.sysletvplayer.util;

import android.content.Context;

public class DipToPx {

    @SuppressWarnings("unused")
    private final Context context;
    private final float density;

    public DipToPx(Context context) {
        this.context = context;

        this.density = context.getResources().getDisplayMetrics().density;
    }

    public int dipToPx(int dip) {
        return (int) (dip * this.density);
    }

    public int dipToPx(float dip) {
        return (int) (dip * this.density);
    }
}
