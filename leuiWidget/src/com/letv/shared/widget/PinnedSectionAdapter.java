package com.letv.shared.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;

import java.util.HashMap;

public abstract class PinnedSectionAdapter extends BaseAdapter implements OnItemClickListener {

    private int mCount = -1;

    public abstract int numberOfSections();

    public abstract int numberOfRows(int section);

    public abstract View getRowView(int section, int row, View convertView, ViewGroup parent);

    public abstract Object getRowItem(int section, int row);

    public boolean hasSectionHeaderView(int section) {
        return false;
    }

    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
        return null;
    }

    public Object getSectionHeaderItem(int section) {
        return null;
    }

    public int getRowViewTypeCount() {
        return 1;
    }

    public int getSectionHeaderViewTypeCount() {
        return 0;
    }

    /**
     * Must return a value between 0 and getRowViewTypeCount() (excluded)
     */
    public int getRowItemViewType(int section, int row) {
        return 0;
    }

    /**
     * Must return a value between 0 and getSectionHeaderViewTypeCount() (excluded, if > 0)
     */
    public int getSectionHeaderItemViewType(int section) {
        return 0;
    }

    @Override
    /**
     * Dispatched to call onRowItemClick
     */
    public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onRowItemClick(parent, view, getSection(position), getRowInSection(position), id);
    }

    public void onRowItemClick(AdapterView<?> parent, View view, int section, int row, long id) {

    }

    @Override
    /**
     * Counts the amount of cells = headers + rows
     */
    public final int getCount() {
        if (mCount < 0) {
            mCount = numberOfCellsBeforeSection(numberOfSections());
        }
        return mCount;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    /**
     * Dispatched to call getRowItem or getSectionHeaderItem
     */
    public final Object getItem(int position) {
        int section = getSection(position);
        if (isSectionHeader(position)) {
            if (hasSectionHeaderView(section)) {
                return getSectionHeaderItem(section);
            }
            return null;
        }
        return getRowItem(section, getRowInSection(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    /**
     * Dispatched to call getRowView or getSectionHeaderView
     */
    public final View getView(int position, View convertView, ViewGroup parent) {
//        Log.e("steven_getView",""+(position));
        int section = getSection(position);
        if (isSectionHeader(position)) {
            if (hasSectionHeaderView(section)) {
                return getSectionHeaderView(section, convertView, parent);
            }
            return null;
        }
        return getRowView(section, getRowInSection(position), convertView, parent);
    }


    /**
     * Returns the section number of the indicated cell
     */
    protected int getSection(int position) {
        if(!sectionCache.containsKey(position)) {
            int section = 0;
            while (numberOfCellsBeforeSection(section) <= position) {
                section++;
            }
            sectionCache.put(position,section-1);
        }
//        return section - 1;
        return sectionCache.get(position);
    }

    /**
     * Returns the row index of the indicated cell Should not be call with
     * positions directing to section headers
     */
    protected int getRowInSection(int position) {
        int section = getSection(position);
        int row = position - numberOfCellsBeforeSection(section);
        if (hasSectionHeaderView(section)) {
            return row - 1;
        } else {
            return row;
        }
    }

    /**
     * Returns true if the cell at this index is a section header
     */
    protected boolean isSectionHeader(int position) {
        int section = getSection(position);
        return hasSectionHeaderView(section) && numberOfCellsBeforeSection(section) == position;
    }

    private HashMap<Integer,Integer> numberOfCellsSection = new HashMap<Integer, Integer>();

    private HashMap<Integer,Integer> sectionCache = new HashMap<Integer, Integer>();

    /**
     * Returns the number of cells (= headers + rows) before the indicated
     * section
     */
    protected int numberOfCellsBeforeSection(int section) {
        long startTime = System.currentTimeMillis();
        if(!numberOfCellsSection.containsKey(section)){

            int count = 0;
            for (int i = 0; i < Math.min(numberOfSections(), section); i++) {
                if (hasSectionHeaderView(i)) {
                    count += 1;
                }
                count += numberOfRows(i);
            }
            numberOfCellsSection.put(section,count);
        }
        long endTime = System.currentTimeMillis();
//        Log.e("steven_numberOfCellsBeforeSection",""+(startTime - endTime));
        return numberOfCellsSection.get(section);
    }

    @Override
    public void notifyDataSetChanged() {
        numberOfCellsSection.clear();
        sectionCache.clear();
        mCount = numberOfCellsBeforeSection(numberOfSections());
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        mCount = numberOfCellsBeforeSection(numberOfSections());
        super.notifyDataSetInvalidated();
    }

    @Override
    /**
     * Dispatched to call getRowItemViewType or getSectionHeaderItemViewType
     */
    public int getItemViewType(int position) {
        int section = getSection(position);
        if (isSectionHeader(position)) {
            return getSectionHeaderItemViewType(section);
        } else {
            return getRowItemViewType(section, getRowInSection(position));
        }
    }

    @Override
    /**
     * Dispatched to call getRowViewTypeCount and getSectionHeaderViewTypeCount
     */
    public final int getViewTypeCount() {
        return getRowViewTypeCount() + getSectionHeaderViewTypeCount();
    }

    @Override
    /**
     * By default, disables section headers
     */
    public boolean isEnabled(int position) {
        return !isSectionHeader(position);
    }
}
