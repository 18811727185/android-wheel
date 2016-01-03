package com.letv.shared.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.*;
import android.widget.AdapterView;

public class PinnedHeaderListView extends LeListView {

    private PinnedSectionAdapter mAdapter;
    private RelativeLayout mHeader;
    private View pinnedHeader;
    private int mPinnedHeaderMode = PINNED_HEADER_VISIBLE;
    
    public final static int PINNED_HEADER_VISIBLE = 0;
    public final static int PINNED_HEADER_INVISIBLE = 1;

    public PinnedHeaderListView(Context context) {
        super(context);
        init(context);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOnScrollListener(new HeaderListViewOnScrollListener());
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAdapter != null) {
                    mAdapter.onItemClick(parent, view, position, id);
                }
            }
        });

        mHeader = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams headerParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        headerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mHeader.setLayoutParams(headerParams);
        mHeader.setGravity(Gravity.BOTTOM);
    }

    public void setAdapter(ListAdapter adapter) {
        mAdapter = (PinnedSectionAdapter) adapter;
        super.setAdapter(adapter);
    }

    private class HeaderListViewOnScrollListener implements OnScrollListener {

        private int previousFirstVisibleItem = -1;
        private int direction = 0;
        private int actualSection = 0;
        private boolean scrollingStart = false;
        private boolean doneMeasuring = false;
        private int lastResetSection = -1;
        private int nextH;
        private int prevH;
        private View previous;
        private View next;
        private AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        private boolean noHeaderUpToHeader = false;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            firstVisibleItem -= getHeaderViewsCount();
            if (firstVisibleItem < 0) {
                mHeader.removeAllViews();
                return;
            }
            if (mHeader != null) {
                if (firstVisibleItem == 0) {
                    View firstView = getChildAt(firstVisibleItem);
                    if (null==firstView || firstView.getTop() >= 0) {
                        mHeader.setVisibility(View.INVISIBLE);
                    } else {
                        mHeader.setVisibility(View.VISIBLE);
                    }

                } else {
                    mHeader.setVisibility(View.VISIBLE);
                }
            }

            if (totalItemCount > 0 && firstVisibleItem == 0) {
                addSectionHeader(0);
            }

            int realFirstVisibleItem = getRealFirstVisibleItem(firstVisibleItem, visibleItemCount);
            if (totalItemCount > 0 && previousFirstVisibleItem != realFirstVisibleItem) {
                direction = realFirstVisibleItem - previousFirstVisibleItem;

                actualSection = mAdapter.getSection(realFirstVisibleItem);

                boolean currIsHeader = mAdapter.isSectionHeader(realFirstVisibleItem);
                boolean prevHasHeader = mAdapter.hasSectionHeaderView(actualSection - 1);
                boolean nextHasHeader = mAdapter.hasSectionHeaderView(actualSection + 1);
                boolean currHasHeader = mAdapter.hasSectionHeaderView(actualSection);
                boolean currIsLast = mAdapter.getRowInSection(realFirstVisibleItem) == mAdapter.numberOfRows(actualSection) - 1;
                boolean currIsFirst = mAdapter.getRowInSection(realFirstVisibleItem) == 0;

                boolean needScrolling = currIsFirst && !currHasHeader && prevHasHeader && realFirstVisibleItem != firstVisibleItem;
                boolean needNoHeaderUpToHeader = currIsLast && currHasHeader && !nextHasHeader && realFirstVisibleItem == firstVisibleItem
                        && Math.abs(getChildAt(0).getTop()) >= getChildAt(0).getHeight() / 2;

                noHeaderUpToHeader = false;
                if (currIsHeader && !prevHasHeader && firstVisibleItem >= 0) {
                    resetHeader(direction < 0 ? actualSection - 1 : actualSection);
                } else if ((currIsHeader && firstVisibleItem > 0) || needScrolling) {
                    startScrolling();
                } else if (needNoHeaderUpToHeader) {
                    noHeaderUpToHeader = true;
                } else if (lastResetSection != actualSection) {
                    resetHeader(actualSection);
                }

                previousFirstVisibleItem = realFirstVisibleItem;
            }

            if (scrollingStart && null != pinnedHeader) {
                int scrolled = realFirstVisibleItem >= firstVisibleItem ? getChildAt(realFirstVisibleItem - firstVisibleItem).getTop() : 0;

                if (!doneMeasuring) {
                    setMeasurements(realFirstVisibleItem, firstVisibleItem);
                }

                int headerH = doneMeasuring ? (prevH - nextH) * direction * Math.abs(scrolled) / (direction < 0 ? nextH : prevH) + (direction > 0 ? nextH : prevH) : 0;
                int scrollHeight = -Math.min(0, scrolled - headerH);

                if (doneMeasuring && headerH != mHeader.getLayoutParams().height) {
                    mHeader.layout(0, 0, pinnedHeader.getMeasuredWidth(), headerH);
                    pinnedHeader.layout(0, 0, pinnedHeader.getMeasuredWidth(), direction > 0 ? prevH : nextH);
                    RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) pinnedHeader.getLayoutParams();
                    p.topMargin = headerH - pinnedHeader.getHeight();
                    mHeader.setGravity(Gravity.BOTTOM);
                    mHeader.requestLayout();
                }
                RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) pinnedHeader.getLayoutParams();
                p.topMargin = headerH - pinnedHeader.getHeight();
                mHeader.scrollTo(0, scrollHeight - p.topMargin);
            }

            if (noHeaderUpToHeader) {
                if (lastResetSection != actualSection) {
                    addSectionHeader(actualSection);
                    lastResetSection = actualSection + 1;
                }
                mHeader.scrollTo(0, mHeader.getLayoutParams().height - (getChildAt(0).getHeight() + getChildAt(0).getTop()));
            }
        }

        private void startScrolling() {
            scrollingStart = true;
            doneMeasuring = false;
            lastResetSection = -1;
        }

        private void resetHeader(int section) {
            scrollingStart = false;
            addSectionHeader(section);
            mHeader.requestLayout();
            lastResetSection = section;
        }

        private void setMeasurements(int realFirstVisibleItem, int firstVisibleItem) {

            if (direction > 0) {
                nextH = realFirstVisibleItem >= firstVisibleItem ? getChildAt(realFirstVisibleItem - firstVisibleItem).getMeasuredHeight() : 0;
            }

            previous = mHeader.getChildAt(0);
            prevH = previous != null ? previous.getMeasuredHeight() : mHeader.getHeight();

            if (direction < 0) {
                if (lastResetSection != actualSection - 1) {
                    addSectionHeader(Math.max(0, actualSection - 1));
                    next = mHeader.getChildAt(0);
                }
                nextH = mHeader.getChildCount() > 0 ? mHeader.getChildAt(0).getMeasuredHeight() : 0;
                mHeader.scrollTo(0, prevH);
            }
            doneMeasuring = previous != null && prevH > 0 && nextH > 0;
        }

        private void addSectionHeader(int actualSection) {
            View previousHeader = mHeader.getChildAt(0);
            if (previousHeader != null) {
                mHeader.removeViewAt(0);
            }

            if (mAdapter.hasSectionHeaderView(actualSection)) {

                pinnedHeader = mAdapter.getSectionHeaderView(actualSection, null, null);
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                int heightMode = MeasureSpec.getMode(params.height);
                int heightSize = MeasureSpec.getSize(params.height);
                if (heightMode == MeasureSpec.UNSPECIFIED)
                    heightMode = MeasureSpec.EXACTLY;
                int maxHeight = getHeight() - getListPaddingTop() - getListPaddingBottom();
                if (heightSize > maxHeight)
                    heightSize = maxHeight;

                int ws = MeasureSpec.makeMeasureSpec(getWidth() - getListPaddingLeft() - getListPaddingRight(), MeasureSpec.EXACTLY);
                int hs = MeasureSpec.makeMeasureSpec(heightSize, heightMode);

                pinnedHeader.measure(ws, hs);
                pinnedHeader.layout(0, 0, pinnedHeader.getMeasuredWidth(), pinnedHeader.getMeasuredHeight());
                mHeader.layout(0, 0, pinnedHeader.getMeasuredWidth(), pinnedHeader.getMeasuredHeight());

                pinnedHeader.scrollTo(0, 0);
                mHeader.scrollTo(0, 0);
                mHeader.addView(pinnedHeader, 0);
            } else {
                mHeader.getLayoutParams().height = 0;
                mHeader.scrollTo(0, 0);
            }
            if (mPinnedHeaderMode == PINNED_HEADER_INVISIBLE) {
                mHeader.setVisibility(INVISIBLE);
            }

        }

        private int getRealFirstVisibleItem(int firstVisibleItem, int visibleItemCount) {
            if (visibleItemCount == 0) {
                return -1;
            }
            int relativeIndex = 0, totalHeight = getChildAt(0).getTop();
            for (relativeIndex = 0; relativeIndex < visibleItemCount && totalHeight < mHeader.getHeight(); relativeIndex++) {
                totalHeight += getChildAt(relativeIndex).getHeight();
            }
            int realFVI = Math.max(firstVisibleItem, firstVisibleItem + relativeIndex - 1);
            return realFVI;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mPinnedHeaderMode == PINNED_HEADER_VISIBLE && mHeader != null && mHeader.getVisibility() == View.VISIBLE && pinnedHeader != null) {
            int pLeft = getListPaddingLeft();
            int pTop = getListPaddingTop();
            canvas.save();
            int clipHeight = pinnedHeader.getMeasuredHeight();
            canvas.clipRect(pLeft, pTop, pLeft + pinnedHeader.getMeasuredWidth(), pTop + clipHeight);
            canvas.translate(pLeft, pTop);
            drawChild(canvas, mHeader, getDrawingTime());
            canvas.restore();
        }
    }

    @Override
    protected int computeVerticalScrollExtent() {
        return super.computeVerticalScrollExtent();
    }

    @Override
    protected int computeVerticalScrollOffset() {
        return super.computeVerticalScrollOffset();
    }

    @Override
    protected int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    public int getPinnedHeaderMode() {
        return mPinnedHeaderMode;
    }

    public void setPinnedHeaderMode(int pinnedHeaderMode) {
        this.mPinnedHeaderMode = pinnedHeaderMode;
    }
}
