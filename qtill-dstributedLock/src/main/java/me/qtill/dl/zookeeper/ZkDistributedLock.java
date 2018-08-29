package me.qtill.dl.zookeeper;

import me.qtill.dl.DistributedLock;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ZkDistributedLock implements DistributedLock {

    private static final String LOCK_NODE_FULL_PATH = "/exclusive_lock/lock";

    /**
     * 自旋测试超时阈值，考虑到网络的延时性，这里设为1000毫秒
     */
    private static final long spinForTimeoutThreshold = 1000L;

    private static final long SLEEP_TIME = 100L;


    private ZooKeeper      zooKeeper;
    private LockStatus     lockStatus;
    private CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private CyclicBarrier  lockBarrier        = new CyclicBarrier(2);


    private String id = String.valueOf(new Random(System.nanoTime()).nextInt(10000000));

    public ZkDistributedLock() throws InterruptedException, IOException {
        zooKeeper = new ZooKeeper("127.0.0.1:2181", 1000, new LockNodeWatcher());
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
            System.out.println("[" + id + "] 获取锁");
            lockStatus = LockStatus.LOCKED;
            return;
        }

        lockStatus = LockStatus.TRY_LOCK;
        lockBarrier.await();
    }

    @Override
    public boolean tryLock() {
        if (lockStatus == LockStatus.LOCKED) {
            return true;
        }
        boolean createRst = createLockNode();
        lockStatus = createRst ? LockStatus.LOCKED : LockStatus.UNLOCK;
        return createRst;
    }

    @Override
    public boolean tryLock(long tryUntil, TimeUnit unit) throws Exception {
        long millisTimeout = unit.toMillis(tryUntil);
        if (millisTimeout <= 0) {
            return false;
        }

        final long deadline = System.currentTimeMillis() + millisTimeout;
        for (; ; ) {
            if (tryLock()) {
                return true;
            }

            // 引入一个休眠时间，避免长时间空循环消耗CPU
            if (millisTimeout > spinForTimeoutThreshold) {
                Thread.sleep(SLEEP_TIME);
            }

            millisTimeout = deadline - System.currentTimeMillis();
            if (millisTimeout <= 0L) {
                return false;
            }
        }
    }

    @Override
    public boolean unlock() {
        if (lockStatus == LockStatus.UNLOCK) {
            return true;
        }
        boolean deleteRst = deleteLockNode();
        lockStatus = LockStatus.UNLOCK;

        // TODO
        lockBarrier.reset();
        System.out.println("[" + id + "] 释放锁");
        return deleteRst;
    }

    @Override
    public void lockWithExpire(long expire, TimeUnit unit) {

    }

    @Override
    public boolean tryLockWithExpire(long expire, TimeUnit unit) {
        return false;
    }

    @Override
    public boolean tryLockWithExpire(long tryUntil, long expire, TimeUnit unit) {
        return false;
    }


    private Boolean createLockNode() {
        try {
            zooKeeper.create(LOCK_NODE_FULL_PATH, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (KeeperException | InterruptedException e) {
            return false;
        }
        return true;
    }

    private boolean deleteLockNode() {
        try {
            Stat stat = zooKeeper.exists(LOCK_NODE_FULL_PATH, false);
            zooKeeper.delete(LOCK_NODE_FULL_PATH, stat.getVersion());
        } catch (Exception e) {
            return false;
        }
        return true;
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
                // 连接成功？
                connectedSemaphore.countDown();
            } else if (Event.EventType.NodeDeleted == event.getType()
                && event.getPath().equals(LOCK_NODE_FULL_PATH)) {

                if (lockStatus == LockStatus.TRY_LOCK && createLockNode()) {
                    lockStatus = LockStatus.LOCKED;
                    try {
                        lockBarrier.await();
                        System.out.println("[" + id + "]" + " 获取锁");
                        return;
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
