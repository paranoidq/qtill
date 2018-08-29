package me.qtill.dl.zookeeper;

import me.qtill.dl.DistributedLock;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public interface ReadWriteLock {

    DistributedLock readLock();

    DistributedLock writeLock();
}
