package me.qtill.commons.concurrent.sample;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ReentrantLockSample {
    public static void main(String[] args) throws InterruptedException {

        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();


        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("启动thread-1");
                lock.lock();
                try {
                    System.out.println("thread-1拿到lock，准备sleep");
                    // 等thread2先执行signal
                    Thread.sleep(2000);
                    System.out.println("睡眠完了，开始await");
                    condition.await();
                    System.out.println("condition满足了");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }

            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("启动thread-2");
                lock.lock();
                try {
                    System.out.println("thread-2拿到lock，准备调用唤醒操作");
                    condition.signalAll();
                    System.out.println("thread-2调用唤醒操作完成了");
                } finally {
                    lock.unlock();
                }
            }
        });

        thread2.start();
        thread1.start();

    }

}
