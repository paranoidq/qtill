package me.qtill.zookeeper.practices.lock;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public interface DistributedLock {

    void lock() throws Exception;

    boolean tryLock() throws Exception;

    boolean tryLock(long milliSeconds) throws Exception;

    void unlock() throws Exception;
}
