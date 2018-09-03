package me.qtill.zookeeper.practices.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ExclusiveLock implements DistributedLock {

    private ZooKeeper      zooKeeper;
    private LockStatus     lockStatus;
    private CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private CyclicBarrier  lockBarrier        = new CyclicBarrier(2);

    /**
     * 自旋测试超时阈值，考虑到网络的延时性，这里设为1000毫秒
     */
    private static final long spinForTimeoutThreshold = 1000L;

    private static final long SLEEP_TIME = 100L;


    private String LOCK_NODE_FULL_PATH = "/exclusive_lock/lock";

    private String id = String.valueOf(new Random(System.nanoTime()).nextInt(10000000));

    public ExclusiveLock() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper("127.0.0.1:2181", 1000,
            new LockNodeWatcher());
        lockStatus = LockStatus.UNLOCK;

        connectedSemaphore.await();
    }

    @Override
    public void lock() throws Exception {
        if (lockStatus != LockStatus.UNLOCK) {
            return;
        }


        // 1. 创建节点
        if (createLockNode()) {
            // 2.1 获取到锁，直接返回
            System.out.println("[" + id + "] 获取锁");
            lockStatus = LockStatus.LOCKED;
            return;
        }
        lockStatus = LockStatus.TRY_LOCK;
        // 2.2 没有获取到锁，阻塞等待
        lockBarrier.await();
    }

    @Override
    public boolean tryLock() throws Exception {
        if (lockStatus == LockStatus.LOCKED) {
            return true;
        }

        boolean created = createLockNode();
        lockStatus = created ? LockStatus.LOCKED : LockStatus.TRY_LOCK;
        return created;
    }

    @Override
    public boolean tryLock(long milliSeconds) throws Exception {
        long millisTimeout = milliSeconds;
        if (millisTimeout <= 0L) {
            return false;
        }
        final long deadline = System.currentTimeMillis() + millisTimeout;
        for (; ; ) {
            if (tryLock()) {
                return true;
            }

            if (millisTimeout > spinForTimeoutThreshold) {
                Thread.sleep(SLEEP_TIME);
            }

            millisTimeout = deadline - System.currentTimeMillis();
            if (millisTimeout <= 0) {
                return false;
            }
        }
    }

    @Override
    public void unlock() throws Exception {
        if (lockStatus == LockStatus.UNLOCK) {
            return;
        }

        deleteLockNode();
        lockStatus = LockStatus.UNLOCK;
        lockBarrier.reset();
        System.out.println("[" + id + "] 释放锁");
    }

    private boolean createLockNode() {
        try {
            zooKeeper.create(
                LOCK_NODE_FULL_PATH, "".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL
            );
        } catch (KeeperException | InterruptedException e) {
            return false;
        }
        return true;
    }

    private void deleteLockNode() throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(LOCK_NODE_FULL_PATH, false);
        if (stat != null) {
            zooKeeper.delete(LOCK_NODE_FULL_PATH, stat.getVersion());
        }
    }


    private class LockNodeWatcher implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            if (Event.KeeperState.SyncConnected != event.getState()) {
                return;
            }

            // 设置监视器
            try {
                zooKeeper.exists(LOCK_NODE_FULL_PATH, this);
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }

            if (Event.EventType.None == event.getType() && event.getPath() == null) {
                connectedSemaphore.countDown();
            } else if (Event.EventType.NodeDeleted == event.getType()
                && event.getPath().equals(LOCK_NODE_FULL_PATH)) {

                if (lockStatus == LockStatus.TRY_LOCK && createLockNode()) {
                    lockStatus = LockStatus.LOCKED;
                    try {
                        lockBarrier.await();
                        System.out.println("[" + id + "] 获取锁");
                        return;
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                } else {

                    // 重新绑定
                    try {
                        zooKeeper.exists(LOCK_NODE_FULL_PATH, this);
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

}
