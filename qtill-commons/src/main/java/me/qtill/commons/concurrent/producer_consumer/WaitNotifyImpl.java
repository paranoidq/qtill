package me.qtill.commons.concurrent.producer_consumer;

import javax.sound.midi.Soundbank;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class WaitNotifyImpl<T> implements Buffer<T> {

    private int cap;
    private int size = 0;

    private Object[] buffer;

    private Object blocker = new Object();

    public WaitNotifyImpl(int cap) {
        this.cap = cap;
        adjustCap(this.cap);
        buffer = new Object[cap];
    }

    @Override
    public void produce(T t) throws InterruptedException {
        synchronized (blocker) {
            while (size == cap) {
                System.out.println("Buffer满了，等待consumer消费");
                blocker.wait();
            }
            buffer[size++] = t;
            // 唤醒所有等待的线程，其实这里的目的是唤醒consumer，但是由于没有区分阻塞，所以只能唤醒所有的线程
            // 也正是因为这个操作，所以无论是wait还是notify，都必须放在while中，因为唤醒时未必满足条件
            blocker.notifyAll();
        }
    }

    @Override
    public T consume(T t) throws InterruptedException {
        synchronized (blocker) {
            while (size == 0) {
                System.out.println("Buffer空了，等待producer生产");
                blocker.wait();
            }

            T v = (T) buffer[--size];

            //原因同produce函数
            blocker.notifyAll();

            return v;
        }
    }


    private void adjustCap(int initialCap) {

    }
}

