package me.qtill.commons.concurrent.sample;

import java.util.concurrent.CountDownLatch;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class CountDownLatchSample {

    public static void main(String[] args) throws InterruptedException {
        int n = 10;

        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(n);

        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startLatch.await();
                        // run tasks
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        endLatch.countDown();
                    }
                }
            }).start();
        }

        //
        long start = System.nanoTime();
        startLatch.countDown();
        endLatch.await();
        System.out.println(System.nanoTime() - start);
    }
}
