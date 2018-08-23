package me.qtill.commons.concurrent.tryit;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public abstract class DefaultPromise<T> implements Promise<T> {

    private T outcome;

    private          Lock      lock     = new ReentrantLock();
    private          Condition finished = lock.newCondition();
    private volatile boolean   isDone   = false;


    @Override
    public void setResult(T outcome) {
        try {
            lock.lock();
            this.outcome = outcome;
            isDone = true;
            finished.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T getResult() throws InterruptedException {
        try {
            lock.lock();
            while (!isDone) {
                finished.await();
            }
            return outcome;
        } finally {
            lock.unlock();
        }
    }


    public abstract void onSuccess();

    public abstract void onFailure(Throwable throwable);
}
