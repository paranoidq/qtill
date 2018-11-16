package me.qtill.commons.concurrent.producer_consumer;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public interface Buffer<T> {

    void produce(T t) throws InterruptedException;

    T consume(T t) throws InterruptedException;
}
