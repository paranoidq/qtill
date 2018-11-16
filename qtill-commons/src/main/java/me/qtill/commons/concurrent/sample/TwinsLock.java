package me.qtill.commons.concurrent.sample;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class TwinsLock implements Lock {

    // 最多两个线程并发
    private final Sync sync = new Sync(2);

    private static final class Sync extends AbstractQueuedSynchronizer {

        public Sync(int count) {
            if (count <= 0) {
                throw new IllegalArgumentException("count must larger than 0");
            }
            setState(count);
        }

        @Override
        protected int tryAcquireShared(int releaseCount) {
            for (; ; ) {
                int current = getState();
                int newCount = current - releaseCount;
                // CAS设置新的state值，两种情况可以返回
                /*
                 * 1. newCount已经小于0了，返回newCount小于0，表示获取锁失败
                 * 2. compareSetState设置成功了，返回newCount表示设置成功
                 *
                 * 其实这里也可以不检查newCount < 0，但是通过在CAS之前检查newCount的大小，可以避免多余的CAS操作，
                 * 因为小于0的情况下，显然无法获取锁（即使后面CAS的时候，可能因为其他线程release导致大于0了，也会因为CAS操作的失败而直接失败）
                 */
                if (newCount < 0 || compareAndSetState(current, newCount)) {
                    return newCount;
                }
            }
        }

        @Override
        protected boolean tryReleaseShared(int returnCount) {
            for (; ; ) {
                int current = getState();
                int newCount = current + returnCount;
                if (compareAndSetState(current, newCount)) {
                    return true;
                }
            }
        }

        // 只用于ConditionObject中，如果没有用到Condition，无需重写
        @Override
        protected boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }


        public Condition newCondition() {
            return new ConditionObject();
        }
    }


    @Override
    public void lock() {
        sync.acquireShared(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquireShared(1) < 0;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.releaseShared(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }


    public static void main(String[] args) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }
}
