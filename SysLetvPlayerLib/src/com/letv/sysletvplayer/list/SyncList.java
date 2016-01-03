package com.letv.sysletvplayer.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 列表数据的线程安全实现类
 * @author caiwei
 */
public class SyncList {
    private List playList;
    private final Object mutex;

    public SyncList() {
        this.mutex = this;
    }

    /**
     * 设置列表
     * @param list
     */
    public void setList(List list) {
        synchronized (this.mutex) {
            this.playList = list;
        }
    }

    /**
     * 获取列表
     * @return
     */
    public List getList() {
        synchronized (this.mutex) {
            return this.playList;
        }
    }

    /**
     * 从索引start开始，获取playList.size()-start个元素
     * 如果start>=playList.size()或start<0,返回空
     */
    public List getList(int start) {
        if (start < 0) {
            return null;
        }
        synchronized (this.mutex) {
            if (this.playList == null) {
                return null;
            }
            int size = this.playList.size();
            if (start >= size) {
                return null;
            }
            ArrayList<Object> desList = new ArrayList<Object>();
            for (int i = start; i < size; i++) {
                Object obj = this.playList.get(i);
                desList.add(obj);
            }
            return desList;
        }
    }

    /**
     * 从索引start开始，获取end-start个元素
     * 如果end>playList.size(),返回空
     */
    public List getList(int start, int end) {
        if (start < 0 || start >= end) {
            return null;
        }
        synchronized (this.mutex) {
            if (this.playList == null) {
                return null;
            }
            int size = this.playList.size();
            if (end > size) {
                return null;
            }
            ArrayList<Object> desList = new ArrayList<Object>();
            for (int i = start; i < end; i++) {
                if (i >= size) {
                    desList = null;
                    return null;
                }
                Object obj = this.playList.get(i);
                desList.add(obj);
            }
            return desList;
        }
    }

    /**
     * 插入某元素到列表的指定位置
     */
    public boolean add(Object item, int index) {
        if (index < 0) {
            return false;
        }
        synchronized (this.mutex) {
            if (this.playList == null) {
                return false;
            }
            int s = this.playList.size();
            if (index <= s && index >= 0) {
                this.playList.add(index, item);
                return true;
            }
            return false;
        }
    }

    /**
     * 添加某元素到列表
     */
    public boolean add(Object item) {
        synchronized (this.mutex) {
            if (this.playList == null) {
                return false;
            }
            return this.playList.add(item);
        }
    }

    /**
     * 从列表的start索引开始，插入apendList里的所有元素
     * @param apendList
     * @return true:插入成功；false:插入失败
     */
    public boolean addAll(int start, List apendList) {
        if (start < 0) {
            return false;
        }
        synchronized (this.mutex) {
            if (this.playList == null) {
                return false;
            }
            int s = this.playList.size();
            if (start <= s && start >= 0) {
                return this.playList.addAll(start, apendList);
            }
            return false;
        }
    }

    /**
     * 添加apendList里的所有元素到列表
     * @param apendList
     * @return true:添加成功；false:添加失败
     */
    public boolean addAll(List apendList) {
        synchronized (this.mutex) {
            if (this.playList == null) {
                return false;
            }
            return this.playList.addAll(apendList);
        }
    }

    /**
     * 清空列表
     */
    public void clear() {
        synchronized (this.mutex) {
            if (this.playList != null) {
                this.playList.clear();
            }
        }
    }

    /**
     * 获取某元素在列表中的索引
     */
    public int indexOf(Object item) {
        synchronized (this.mutex) {
            if (this.playList == null) {
                return -1;
            }
            return this.playList.indexOf(item);
        }
    }

    /**
     * 获取列表尺寸
     */
    public int size() {
        synchronized (this.mutex) {
            if (this.playList == null) {
                return 0;
            }
            return this.playList.size();
        }
    }

    /**
     * 获取列表中指定索引的某元素
     */
    public Object get(int position) {
        if (position < 0) {
            return null;
        }
        synchronized (this.mutex) {
            if (this.playList == null) {
                return null;
            }
            try {
                return this.playList.get(position);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * 移除某元素
     * @param item
     * @return true:移除成功；false:无移除
     */
    public boolean remove(Object item) {
        synchronized (this.mutex) {
            if (this.playList == null) {
                return false;
            }
            return this.playList.remove(item);
        }
    }

    /**
     * 移除指定索引的某元素
     * @param index
     * @return 被移除的元素对象
     */
    public Object remove(int index) {
        if (index < 0) {
            return null;
        }
        synchronized (this.mutex) {
            if (this.playList == null) {
                return null;
            }
            try {
                return this.playList.remove(index);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * 从索引start开始，移除playList.size()-start个元素
     * 如果start>=playList.size()或者start<0，不移除
     * @param start
     * @return true:有元素被移除；false:无移除
     */
    public boolean removeAll(int start) {
        if (start < 0) {
            return false;
        }
        boolean result = false;
        synchronized (this.mutex) {
            if (this.playList == null) {
                return false;
            }
            int size = this.playList.size();
            if (start >= size) {
                return false;
            }
            for (int i = start; i < size; i++) {
                this.playList.remove(start);
                result = true;
            }
        }
        return result;
    }

    /**
     * 从索引start开始，移除end-start个元素
     * 如果end>playList.size()，不移除
     * 如果start<0，不移除
     * @param start
     * @param end
     * @return true:有元素被移除；false:无移除
     */
    public boolean removeAll(int start, int end) {
        if (start < 0 || start >= end) {
            return false;
        }
        boolean result = false;
        synchronized (this.mutex) {
            if (this.playList == null) {
                return false;
            }
            if (end > this.playList.size()) {
                return false;
            }
            for (int i = start; i < end; i++) {
                this.playList.remove(start);
                result = true;
            }
        }
        return result;
    }

    /**
     * 移除存在于列表中的所有subList元素
     * @param subList
     * @return true:有元素被移除；false:无移除
     */
    public boolean removeAll(List subList) {
        boolean result = false;
        Iterator<?> it = this.playList.iterator();
        while (it.hasNext()) {
            if (subList.contains(it.next())) {
                it.remove();
                result = true;
            }
        }
        return result;
    }
}