package me.qtill.quasar.samples;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.strands.Strand;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class FiberSample {
    public static void main(String[] args) throws InterruptedException {
        int n = 1_000_000;
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < n; i++) {
            new Fiber<Integer>( () -> {
                counter.incrementAndGet();
                if (counter.get() == n) {
                    System.out.println("created success");
                }
                Strand.sleep(1000000);
            }).start();
        }
        latch.await();
    }
}
