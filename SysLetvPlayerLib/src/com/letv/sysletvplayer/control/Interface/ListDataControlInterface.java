package com.letv.sysletvplayer.control.Interface;

import java.util.List;

/**
 * 列表数据接口
 * @author caiwei
 */
public interface ListDataControlInterface {
    /**
     * 循环模式
     */
    public enum LOOPTYPE {
        TYPE_SEQUENCE, // 顺序
        TYPE_REVERSE, // 倒序
        TYPE_RANDOM, // 随机
        TYPE_SINGLE// 单曲
    }

    public void setLoopType(LOOPTYPE type);// 设置循环模式

    /**
     * 初始化播放列表（当前索引为0,循环模式为顺序）
     */
    public void loadList(List list);

    /**
     * 初始化播放列表，当前索引（循环模式为顺序）
     */
    public void loadList(List list, int currentPosition);

    /**
     * 初始化播放列表，循环模式（当前索引为0）
     */
    public void loadList(List list, LOOPTYPE type);

    /**
     * 初始化播放列表，当前索引，循环模式
     */
    public void loadList(List list, int currentPosition, LOOPTYPE type);

    public void remove(Object item);// 移除某个元素

    public void remove(int index);// 移除某处的元素

    public void remove(int start, int end);// 从index=start开始，移除end-start项

    public void insert(Object item);// 添加某个元素

    public void insert(List apendList);// 添加多个元素

    public void insert(Object item, int index);// 添加元素到指定位置

    public void insert(List list, int start);// 从start开始， 添加多个元素

    public void setCurrrentIndex(int position);// 设置当前正在播放的元素索引

    public int getCurrentIndex();// 获取当前正在播放的元素索引

    public int getIndexOf(Object item);// 获取指定元素的索引

    public Object getCurrentItem();// 获取当前正在播放的元素

    public Object goTo(int position);// 跳转到某处元素,并作为当前元素

    public Object goToNext();// 跳转到下一个元素,并作为当前元素

    public Object goToPrevious();// 跳转到前一个元素,并作为当前元素

    public List getSubList(int start, int end);// 从index=start开始，获取end-start项元素

    public List getList();// 获取列表所有元素

    public void clear();// 清除元素

    public void resetData();// 数据重置
}
