package me.qtill.commons.concurrent.producer_consumer;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class SemaphoreImpl<T> implements Buffer<T> {
    private int cap;
    private int size = 0;
    private Object[] buffer;

    // mutex保证实际操作时，只能有一个线程进行操作
    Semaphore mutex = new Semaphore(1);

    // 最多10个许可

    // mutex保证存取缓冲区时必须是线程互斥的
    // isFull保证缓冲区最多元素为initPermits，初始值代表缓冲区开始可以存放多少元素
    // isEmpty保证缓冲区为0时阻塞，初始值代表缓冲区开始有多少元素
    // 也就是isFull和isEmpty的初始化值加起来等于缓冲区的大小
    Semaphore isFull = new Semaphore(cap);
    Semaphore isEmpty = new Semaphore(0);

    public SemaphoreImpl(int cap) {
        this.cap = cap;
        buffer = new Object[cap];
    }

    @Override
    public void produce(T t) throws InterruptedException {
        // 获取成功意味着buffer没有满
        isFull.acquire();

        try {
            mutex.acquire();
            buffer[size++] = t;
        } finally {
            mutex.release();
            isEmpty.release();
        }
    }

    @Override
    public T consume(T t) throws InterruptedException {
        Object x = null;
        isEmpty.acquire();
        try {
            mutex.acquire();
            x = buffer[--size];
            return (T) x;
        } finally {
            mutex.release();
            isFull.release();
        }
    }
}
