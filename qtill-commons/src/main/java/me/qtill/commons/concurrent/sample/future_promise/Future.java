package me.qtill.commons.concurrent.sample.future_promise;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Future<V> {

    private ReentrantLock lock     = new ReentrantLock();
    private Condition     finished = lock.newCondition();

    private volatile boolean   isDone    = false;
    private          V         result;
    private          Throwable exception = null;

    public void setResult(V result) {
        this.result = result;
        this.isDone = true;
        try {
            lock.lock();
            finished.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public V getResult() throws InterruptedException {
        try {
            lock.lock();
            finished.await();
        } finally {
            lock.unlock();
        }
        return result;
    }

    public V getResult(long timeout, TimeUnit timeUnit) throws InterruptedException {
        if (isDone) {
            return result;
        }
        try {
            lock.lock();
            while (!isDone()) {
                finished.await(timeout, timeUnit);
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

    public boolean isDone() {
        return this.isDone;
    }

    public Throwable getUncaughtException() {
        return this.exception;
    }

    public void setUncaughtException(Throwable throwable) {
        this.exception = throwable;
    }
}
