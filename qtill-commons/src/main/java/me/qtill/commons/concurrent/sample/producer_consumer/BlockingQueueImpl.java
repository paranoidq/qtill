package me.qtill.commons.concurrent.sample.producer_consumer;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class BlockingQueueImpl<T> implements Buffer<T> {

    private final int cap;
    private final BlockingQueue<T> buffer;

    public BlockingQueueImpl(int cap) {
        this.cap = cap;
        this.buffer = new LinkedBlockingQueue<T>(cap);
    }

    @Override
    public void produce(T t) throws InterruptedException {
        buffer.put(t);
    }

    @Override
    public T consume(T t) throws InterruptedException {
        return buffer.take();
    }

    private void printBuffer() {
        System.out.println("buffer元素：" + Arrays.toString(buffer.toArray()));
    }
}
