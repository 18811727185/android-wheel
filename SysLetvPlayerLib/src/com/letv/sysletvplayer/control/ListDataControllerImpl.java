package com.letv.sysletvplayer.control;

import java.util.List;
import java.util.Random;

import com.letv.sysletvplayer.control.Interface.ListDataControlInterface;
import com.letv.sysletvplayer.list.SyncList;

/**
 * 列表数据接口实现类
 * @author caiwei
 */
public class ListDataControllerImpl implements ListDataControlInterface {
    private int mCurrentPos = 0;// 当前播放的视频索引
    private int[] mRandomArray = null;// 随机数组，用以存储随机数据
    private int mRandomPos = 0;// 随机数索引
    private LOOPTYPE mLoopType = LOOPTYPE.TYPE_SEQUENCE;// 当前循环模式
    private final SyncList mListSync;// 列表同步类，存储列表数据

    public ListDataControllerImpl() {
        this.mListSync = new SyncList();
    }

    /**
     * 设置播放列表(当前正在播放位置为0，循环模式为顺序)
     */
    @Override
    public void loadList(List list) {
        this.loadList(list, 0, LOOPTYPE.TYPE_SEQUENCE);
    }

    /**
     * 设置播放列表，设置当前正在播放位置(循环模式为顺序)
     */
    @Override
    public void loadList(List list, int currentPosition) {
        this.loadList(list, currentPosition, LOOPTYPE.TYPE_SEQUENCE);
    }

    /**
     * 设置播放列表，设置循环模式(当前正在播放位置以循环模式来初始化)
     */
    @Override
    public void loadList(List list, LOOPTYPE type) {
        this.initLoad(type, list);
        this.initPosition();
    }

    /**
     * 设置播放列表，设置当前正在播放位置，设置循环模式
     */
    @Override
    public void loadList(List list, int currentPosition, LOOPTYPE type) {
        this.initLoad(type, list);
        this.mCurrentPos = currentPosition;
    }

    @Override
    public void setLoopType(LOOPTYPE type) {
        this.mLoopType = type;
    }

    @Override
    public void remove(Object item) {
        this.mListSync.remove(item);
    }

    @Override
    public void remove(int index) {
        this.mListSync.remove(index);
    }

    @Override
    public void remove(int start, int end) {
        this.mListSync.removeAll(start, end);
    }

    @Override
    public void insert(Object item, int index) {
        this.mListSync.add(item, index);
    }

    @Override
    public void insert(Object item) {
        this.mListSync.add(item);
    }

    @Override
    public void insert(List apendList, int start) {
        this.mListSync.addAll(start, apendList);
    }

    @Override
    public void insert(List apendList) {
        this.mListSync.addAll(apendList);
    }

    @Override
    public int getCurrentIndex() {
        return this.mCurrentPos;
    }

    @Override
    public void setCurrrentIndex(int position) {
        this.mCurrentPos = position;
    }

    @Override
    public int getIndexOf(Object item) {
        return this.mListSync.indexOf(item);
    }

    @Override
    public Object getCurrentItem() {
        return this.mListSync.get(this.mCurrentPos);
    }

    @Override
    public Object goTo(int position) {
        this.mCurrentPos = position;
        return this.getCurrentItem();
    }

    @Override
    public Object goToPrevious() {
        this.setPreviousPostion();
        return this.getCurrentItem();
    }

    @Override
    public Object goToNext() {
        this.setNextPostion();
        return this.getCurrentItem();
    }

    @Override
    public List getSubList(int start, int end) {
        return this.mListSync.getList(start, end);
    }

    @Override
    public List getList() {
        return this.mListSync.getList();
    }

    @Override
    public void clear() {
        this.mListSync.clear();
    }

    // 数据重置
    @Override
    public void resetData() {
        this.mCurrentPos = 0;
        this.mLoopType = LOOPTYPE.TYPE_SEQUENCE;
    }

    /**
     * 默认情况下，初始化首个播放索引
     */
    private int initPosition() {
        switch (this.mLoopType) {
        case TYPE_RANDOM:// 随机
            this.setRandomNext();
            break;
        case TYPE_REVERSE:// 倒序
            this.mCurrentPos = this.mListSync.size() - 1;
            break;
        case TYPE_SEQUENCE:// 顺序
            this.mCurrentPos = 0;
            break;
        case TYPE_SINGLE:// 单曲
            this.mCurrentPos = 0;
            break;
        }
        return this.mCurrentPos;
    }

    private void initLoad(LOOPTYPE type, List list) {
        this.mRandomArray = null;
        this.mLoopType = type;
        this.mListSync.setList(list);
    }

    /**
     * 设置上一个播放索引
     */
    private void setPreviousPostion() {
        switch (this.mLoopType) {
        case TYPE_RANDOM:// 随机
            this.setRandomPrevious();
            break;
        case TYPE_REVERSE:// 倒序
            this.setSequenceNext();
            break;
        case TYPE_SEQUENCE:// 顺序
            this.setSequencePrevious();
            break;
        case TYPE_SINGLE:// 单曲
            break;
        }
    }

    /**
     * 设置下一个播放索引
     */
    private void setNextPostion() {
        switch (this.mLoopType) {
        case TYPE_RANDOM:// 随机
            this.setRandomNext();
            break;
        case TYPE_REVERSE:// 倒序
            this.setSequencePrevious();
            break;
        case TYPE_SEQUENCE:// 顺序
            this.setSequenceNext();
            break;
        case TYPE_SINGLE:// 单曲
            break;
        }
    }

    // 随机模式，下一个索引
    private void setRandomNext() {
        this.initRandomArray();
        if (this.mRandomArray != null && this.mRandomArray.length > 0) {
            if (this.mRandomPos >= this.mRandomArray.length) {
                // 轮循一圈后，需要重新设置随机数
                this.mRandomPos = 0;
                this.mRandomArray = null;
                this.setRandomNext();
                return;
            }
            this.mCurrentPos = this.mRandomArray[this.mRandomPos];
            this.mRandomPos++;
        } else {
            this.mCurrentPos = -1;
        }
    }

    // 随机模式，上一个索引
    private void setRandomPrevious() {
        this.initRandomArray();
        if (this.mRandomArray != null && this.mRandomArray.length > 0) {
            if (this.mRandomPos < 0) {
                // 轮循一圈后，需要重新设置随机数
                this.mRandomPos = this.mRandomArray.length - 1;
                this.mRandomArray = null;
                this.setRandomPrevious();
                return;
            }
            this.mCurrentPos = this.mRandomArray[this.mRandomPos];
            this.mRandomPos--;
        } else {
            this.mCurrentPos = -1;
        }
    }

    // 初始化随机数组
    private void initRandomArray() {
        int max = this.mListSync.size();
        if (max <= 0) {
            this.mRandomArray = null;
            return;
        }
        if (this.mRandomArray != null && this.mRandomArray.length == max) {
            return;
        }
        int[] seed = new int[max];
        for (int i = 0; i < seed.length; i++) {
            seed[i] = i;
        }
        this.mRandomArray = new int[max];
        Random ran = new Random();
        for (int i = 0; i < max; i++) {
            int j = ran.nextInt(max - i);
            this.mRandomArray[i] = seed[j];
            seed[j] = seed[max - 1 - i];
        }
    }

    // 顺序模式，下一个索引
    private void setSequenceNext() {
        this.mCurrentPos++;
        if (this.mCurrentPos >= this.mListSync.size()) {
            this.mCurrentPos = 0;
        }
    }

    // 顺序模式，上一个索引
    private void setSequencePrevious() {
        this.mCurrentPos--;
        if (this.mCurrentPos < 0) {
            this.mCurrentPos = this.mListSync.size() - 1;
        }
    }

}