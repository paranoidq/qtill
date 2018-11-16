package me.qtill.commons.collection;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = -2242501793295012479L;

    private static final int initialCapacity = 16;
    private              int maxCapacity;

    public LRUCache(int maxCapacity) {
        super(initialCapacity, 0.75f, true);
        this.maxCapacity = maxCapacity;
    }


    /**
     * 定制{@code removeEldestEntry}函数，实现LRU的逻辑
     * 该函数在addEntry函数中进行判断，如果返回true，则移除链表尾部的元素
     * LinkedHashMap中默认返回false
     *
     * @param eldest
     * @return
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        if (super.size() > maxCapacity) {
            return true;
        }
        return false;
    }
}
