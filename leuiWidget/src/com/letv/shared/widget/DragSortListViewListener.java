package com.letv.shared.widget;

public interface DragSortListViewListener extends LeListViewListener{

    public void drag(int from, int to);

    /**
     * Your implementation of this has to reorder your ListAdapter! 
     * Make sure to call
     * {@link android.widget.BaseAdapter#notifyDataSetChanged()} or something like it
     * in your implementation.
     * 
     */
    public void drop(int from, int to);

}
