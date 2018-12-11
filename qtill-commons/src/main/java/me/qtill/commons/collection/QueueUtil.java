package me.qtill.commons.collection;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Queue工具集.
 *
 * 各种Queue，Dequeue的创建
 */
public class QueueUtil {

    /**
     * 创建ArrayDeque (JDK无ArrayQueue)
     *
     * 需设置初始长度，默认为16，数组满时成倍扩容
     * 没有最大长度限制，但受限于长度用int表示，最大不超过{@link Integer#MAX_VALUE}
     */
    public static <E> ArrayDeque<E> newArrayDequeUnlimit(int initSize) {
        return new ArrayDeque<E>(initSize);
    }


    /**
     * 创建ArrayDeque (JDK无ArrayQueue)
     *
     * 需设置初始长度，默认为16，数组满时成倍扩容
     * 有最大长度限制
     */
    public static <E> ArrayDeque<E> newArrayDequeLimit(int initSize, int capacity) {
        return new ArrayDeque<E>() {

            private int limit = capacity;

            @Override
            public void addFirst(E e) {
                checkSize();
                super.addFirst(e);
            }

            @Override
            public void addLast(E e) {
                checkSize();
                super.addLast(e);
            }

            @Override
            public boolean offerFirst(E e) {
                checkSize();
                return super.offerFirst(e);
            }

            @Override
            public boolean offerLast(E e) {
                checkSize();
                return super.offerLast(e);
            }

            @Override
            public boolean add(E e) {
                checkSize();
                return super.add(e);
            }

            @Override
            public boolean offer(E e) {
                checkSize();
                return super.offer(e);
            }

            @Override
            public void push(E e) {
                checkSize();
                super.push(e);
            }

            @Override
            public boolean addAll(Collection<? extends E> c) {
                checkSize();
                return super.addAll(c);
            }

            private void checkSize() {
                if (super.size() >= limit) {
                    throw new IllegalStateException("ArrayDeque is full, capacity=[" + limit + "]");
                }
            }
        };
    }

    /**
     * 创建LinkedDeque (LinkedList实现了Deque接口)
     */
    public static <E> LinkedList<E> newLinkedDequeUnlimit() {
        return new LinkedList<E>();
    }

    /**
     * 创建无阻塞情况下，性能最优的并发队列
     */
    public static <E> ConcurrentLinkedQueue<E> newConcurrentNonBlockingQueueUnlimit() {
        return new ConcurrentLinkedQueue<E>();
    }

    /**
     * 创建无阻塞情况下，性能最优的并发双端队列
     */
    public static <E> Deque<E> newConcurrentNonBlockingDequeUnlimit() {
        return new java.util.concurrent.ConcurrentLinkedDeque<E>();
    }

    /**
     * 创建并发阻塞情况下，长度不受限的队列.
     *
     * 长度不受限，即生产者不会因为满而阻塞，但消费者会因为空而阻塞.
     */
    public static <E> LinkedBlockingQueue<E> newBlockingQueueUnlimit() {
        return new LinkedBlockingQueue<E>();
    }

    /**
     * 创建并发阻塞情况下，长度不受限的双端队列.
     *
     * 长度不受限，即生产者不会因为满而阻塞，但消费者会因为空而阻塞.
     */
    public static <E> LinkedBlockingDeque<E> newBlockingDequeUnlimit() {
        return new LinkedBlockingDeque<E>();
    }

    /**
     * 创建并发阻塞情况下，长度受限，更节约内存，但共用一把锁的队列（无双端队列实现）.
     */
    public static <E> ArrayBlockingQueue<E> newArrayBlockingQueueLimit(int capacity) {
        return new ArrayBlockingQueue<E>(capacity);
    }

    /**
     * 创建并发阻塞情况下，长度受限，头队尾两把锁, 但使用更多内存的队列.
     */
    public static <E> LinkedBlockingQueue<E> newLinkedBlockingQueueLimit(int capacity) {
        return new LinkedBlockingQueue<E>(capacity);
    }

    /**
     * 创建并发阻塞情况下，长度受限，头队尾两把锁, 但使用更多内存的双端队列.
     */
    public static <E> LinkedBlockingDeque<E> newBlockingDequeLimit(int capacity) {
        return new LinkedBlockingDeque<E>(capacity);
    }


}