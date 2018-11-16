package me.qtill.commons.concurrent.sample;

import java.util.concurrent.locks.LockSupport;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class LockSupportSample {

    public static void main(String[] args) throws InterruptedException {


        final Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("sleep完了，开始park");
                LockSupport.park(this);
                System.out.println("thread1 被唤醒了");
            }
        });


        final Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                LockSupport.unpark(thread1);
                System.out.println("thread2 唤醒 thread1");

            }
        });

        thread1.start();
        thread2.start();


    }
}
