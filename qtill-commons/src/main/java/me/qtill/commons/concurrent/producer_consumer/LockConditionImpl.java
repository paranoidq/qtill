package me.qtill.commons.concurrent.producer_consumer;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class LockConditionImpl<T> implements Buffer<T> {

    private int cap;
    private int size = 0;
    private Object[] buffer;

    private ReentrantLock lock = new ReentrantLock();
    private Condition notFull = lock.newCondition();
    private Condition notEmpty = lock.newCondition();


    public LockConditionImpl(int cap) {
        this.cap = cap;
        buffer = new Object[cap];
    }

    @Override
    public void produce(T t) throws InterruptedException {
        lock.lock();
        try {
            while (size == cap) {
                System.out.println("Buffer满了，等待consumer消费");
                // 等待notFull条件满足
                notFull.await();
            }

            buffer[size++] = t;

            // produce之后，buffer可能不为空了，因此唤醒notEmpty等待的线程
            // 由于分开了condition，因此这里只要选择一个线程唤醒，减少了竞争
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T consume(T t) throws InterruptedException {
        lock.lock();
        try {
            while (size == 0) {
                System.out.println("Buffer空了，等待producer生产");
                notEmpty.await();
            }

            T ret = (T) buffer[--size];

            // consume之后，buffer可能不满了，因此唤醒notFull等待的线程
            // 由于分开了condition，因此这里只要选择一个线程唤醒，减少了竞争
            notFull.signal();

            return ret;
        } finally {
            lock.unlock();
        }
    }
}
