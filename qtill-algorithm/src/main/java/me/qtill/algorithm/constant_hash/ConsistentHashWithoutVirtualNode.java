package me.qtill.algorithm.constant_hash;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 1. 逻辑上的环实现上可以采用多种形式：数组、列表、二叉树、红黑树等。采用TreeMap的实现方式时间复杂度更低
 * 本质上其实就是：排序 + 找区间
 * <p>
 * 2. hash值必须重新计算，string自身的hashcode不够均匀，并且可能为负数
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class ConsistentHashWithoutVirtualNode {

    private static String[] servers = {"192.168.0.0:111", "192.168.0.1:111", "192.168.0.2:111",
        "192.168.0.3:111", "192.168.0.4:111"};

    static SortedMap<Integer, String> ring = new TreeMap<>();

    public static void init() {
        // 将server放入到环中，环中的元素有序
        for (String s : servers) {
            int hash = getHash(s);
            ring.put(hash, s);
            System.out.println("[" + s + "]加入集合中, 其Hash值为" + hash);
        }
    }


    public static String getNode(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key is empty");
        }
        int hash = getHash(key);

        // get node from ring
        SortedMap<Integer, String> gtSet = ring.tailMap(hash);
        if (gtSet.isEmpty()) {
            return ring.get(ring.firstKey());
        } else {
            return ring.get(gtSet.firstKey());
        }
    }


    /**
     * 使用FNV1_32_HASH算法计算服务器的Hash值,这里不使用重写hashCode的方法，最终效果没区别
     *
     * @param key
     * @return
     */
    public static int getHash(String key) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < key.length(); i++)
            hash = (hash ^ key.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }

    public static void main(String[] args) {
        init();
        String[] nodes = {"127.0.0.1:1111", "221.226.0.1:2222", "10.211.0.1:3333"};
        for (int i = 0; i < nodes.length; i++)
            System.out.println("[" + nodes[i] + "]的hash值为" +
                getHash(nodes[i]) + ", 被路由到结点[" + getNode(nodes[i]) + "]");
    }
}

