package me.qtill.dl.zookeeper;

import me.qtill.dl.DistributedLock;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * zk锁的建模
 * <p>
 * 写锁：
 * - 第一个临时顺序节点
 * <p>
 * 读锁：
 * - 第一个顺序临时节点
 * - 前面没有读锁节点
 * <p>
 * 锁等待和唤醒：
 * - 采用CyclicBarrier，当收到Watcher通知后，检查自己是否满足加锁条件，然后执行barrier.await
 * 所以，采用CyclicBarrier中的parties值为2，就是为了能够唤醒后自动恢复到重置状态
 * 但是似乎直接用ReentrantLock更为直观？
 * <p>
 * <p>
 * <p>
 * [思路]
 * 1. 这里是否获取读写锁的时候有一个特点：即建立连接后已经创建了节点，读写锁的获取并不改变状态，而仅仅只是判断一下自己在顺序节点中相对位置而已
 * 这与通常JUC中的lock有着本质的区别，JUC中的lock是会使用一个volatile类似的变量通过CAS操作状态来控制加锁还是阻塞等待的！！！
 * 2. 这里的非阻塞锁很容易实现，主要的处理细节都在阻塞锁上，技术包括：watcher、JUC lock、排序节点等，都是为了阻塞锁和唤醒而设计的
 * <p>
 * <p>
 * [设计]
 * 1. 超时tryLock中的自旋方式，这种方式避免了连续空循环造成的CPU消耗，是一种常见的超时尝试方法的设计模式
 * <p>
 * <p>
 * [优化]
 * 1. 这里的watcher是一种浪费，因为唤醒的时候其他没必要通知所有的watcher，往往只需要唤醒一个watcher就可以了
 * 具体为：
 * readlock时如果不能获取成功，应该watch小于自己的最后一个写锁节点，因为读锁获取的话，只要前面没有写锁即可
 * writelock时如果不能获取成功，应该watch上一个节点（无论是写锁还是读锁），因为写锁需要独占
 * 2. 并不是准确的实现，其中有很多并发问题没有处理干净，比如是否要用volatile
 * <p>
 * <p>
 * <p>
 * reference : https://github.com/code4wt/distributed_lock
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class ZkReadWriteLock1 implements ReadWriteLock {

    private              ZooKeeper          zooKeeper;
    private static final String             LOCK_NODE_PARENT_PATH   = "/share_lock";
    // 自旋测试超时阈值，考虑到网络的延时性，这里设为1000毫秒
    private static final long               spinForTimeoutThreshold = 1000L;
    private static final long               SLEEP_TIME              = 100L;
    private              CountDownLatch     connectedSemaphore      = new CountDownLatch(1);
    private              ReadLock           readLock                = new ReadLock();
    private              WriteLock          writeLock               = new WriteLock();
    private              Comparator<String> nameComparator;

    public ZkReadWriteLock1() throws Exception {

        Watcher watcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (Event.KeeperState.SyncConnected == event.getState()) {
                    connectedSemaphore.countDown();
                }
            }
        };
        zooKeeper = new ZooKeeper("127.0.0.1:2181", 1000, watcher);
        connectedSemaphore.await();

        nameComparator = (x, y) -> {
            Integer xs = getSequence(x);
            Integer ys = getSequence(y);
            return xs > ys ? 1 : (xs < ys ? -1 : 0);
        };

    }

    @Override
    public DistributedLock readLock() {
        return readLock;
    }

    @Override
    public DistributedLock writeLock() {
        return writeLock;
    }


    private class ReadLock implements DistributedLock, Watcher {

        private LockStatus    lockStatus  = LockStatus.UNLOCK;
        private CyclicBarrier lockBarrier = new CyclicBarrier(2);
        private String        prefix      = new Random(System.nanoTime()).nextInt(10000000) + "-read-";
        private String        name;

        @Override
        public void lock() throws Exception {
            if (lockStatus != LockStatus.UNLOCK) {
                return;
            }


            // 创建节点
            if (name == null) {
                name = createLockNode(prefix);
                // 截取末尾
                name = name.substring(name.lastIndexOf('/') + 1);
                System.out.println("创建锁节点 " + name);
            }

            List<String> nodes = zooKeeper.getChildren(LOCK_NODE_PARENT_PATH, this);
            nodes.sort(nameComparator);

            // 检查是否能获取锁
            if (canAcquireLock(name, nodes)) {
                System.out.println(name + " 获取锁");
                lockStatus = LockStatus.LOCKED;
                return;
            }

            lockStatus = LockStatus.TRY_LOCK;
            lockBarrier.await();
        }

        @Override
        public boolean tryLock() throws Exception {
            if (lockStatus == LockStatus.LOCKED) {
                return true;
            }

            if (name == null) {
                name = createLockNode(prefix);
                name = name.substring(name.lastIndexOf('/') + 1);
                System.out.println("创建锁节点 " + name);
            }
            List<String> nodes = zooKeeper.getChildren(LOCK_NODE_PARENT_PATH, this);
            nodes.sort(nameComparator);

            if (
                canAcquireLock(name, nodes)
                ) {
                lockStatus = LockStatus.LOCKED;
                return true;
            }
            return false;
        }

        @Override
        public boolean tryLock(long tryUntil, TimeUnit unit) throws Exception {
            long millisTimeout = unit.toMillis(tryUntil);
            if (millisTimeout <= 0L) {
                return false;
            }

            final long deadline = System.currentTimeMillis() + millisTimeout;
            for (; ; ) {
                if (tryLock()) {
                    return true;
                }

                // 休眠，避免连续循环消耗CPU
                if (millisTimeout > spinForTimeoutThreshold
                    ) {
                    Thread.sleep(SLEEP_TIME);
                }

                millisTimeout = deadline - System.currentTimeMillis();
                if (millisTimeout <= 0L) {
                    return false;
                }
            }
        }

        @Override
        public boolean unlock() throws Exception {
            if (lockStatus == LockStatus.UNLOCK) {
                return true;
            }

            try {
                deleteLockNode(name);
                lockStatus = LockStatus.UNLOCK;
                lockBarrier.reset();
                System.out.println(name + " 释放锁");
                name = null;
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public void lockWithExpire(long expire, TimeUnit unit) throws Exception {

        }

        @Override
        public boolean tryLockWithExpire(long expire, TimeUnit unit) throws Exception {
            return false;
        }

        @Override
        public boolean tryLockWithExpire(long tryUntil, long expire, TimeUnit unit) throws Exception {
            return false;
        }

        @Override
        public void process(WatchedEvent event) {
            // 不处理SyncConnected事件
            if (Event.KeeperState.SyncConnected == event.getState()) {
                return;
            }

            // 连接成功
            if (Event.EventType.None == event.getType()
                && event.getPath() == null) {
                connectedSemaphore.countDown();
            } else if (Event.EventType.NodeChildrenChanged == event.getType()
                && event.getPath().equals(LOCK_NODE_PARENT_PATH)) {

                // 只处理阻塞等待锁的情况，也即lock调用的情况，tryLock不会阻塞
                if (lockStatus != LockStatus.TRY_LOCK) {
                    return;
                }

                List<String> nodes = null;
                try {
                    nodes = zooKeeper.getChildren(LOCK_NODE_PARENT_PATH, this);
                    nodes.sort(nameComparator);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                // 是否可以获取
                if (canAcquireLock(name, nodes)) {
                    lockStatus = LockStatus.LOCKED;
                    try {
                        // 唤醒等待锁的线程，该线程一定指定了lock方法
                        lockBarrier.await();
                        System.out.println(name + " 获取锁");
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }


            }

        }

    }

    private class WriteLock implements DistributedLock, Watcher {

        // TODO: 不用volatile么？
        private LockStatus    lockStatus  = LockStatus.UNLOCK;
        private CyclicBarrier lockBarrier = new CyclicBarrier(2);
        private String        prefix      = new Random(System.nanoTime()).nextInt(1000000) + "_write_";
        private String        name;


        @Override
        public void lock() throws Exception {
            if (lockStatus != LockStatus.UNLOCK) {
                return;
            }
            if (name == null) {
                name = createLockNode(prefix);
                name = name.substring(name.lastIndexOf('/') + 1);
                System.out.println("创建锁节点 " + name);
            }

            List<String> nodes = zooKeeper.getChildren(LOCK_NODE_PARENT_PATH, this);
            nodes.sort(nameComparator);

            if (isFirstNode(name, nodes)) {
                System.out.println(name + " 获取锁");
                lockStatus = LockStatus.LOCKED;
                return;
            }

            lockStatus = LockStatus.TRY_LOCK;
            lockBarrier.await();
        }

        @Override
        public boolean tryLock() throws Exception {
            if (lockStatus == LockStatus.LOCKED) {
                return true;
            }

            if (name == null) {
                name = createLockNode(prefix);
                name = name.substring(name.lastIndexOf('/') + 1);
                System.out.println("创建锁节点 " + name);
            }

            List<String> nodes = zooKeeper.getChildren(LOCK_NODE_PARENT_PATH, this);
            nodes.sort(nameComparator);

            if (isFirstNode(name, nodes)) {
                lockStatus = LockStatus.LOCKED;
                return true;
            }
            return false;
        }

        @Override
        public boolean tryLock(long tryUntil, TimeUnit unit) throws Exception {
            long millisTimeout = unit.toMillis(tryUntil);
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
                if (millisTimeout < 0L) {
                    return false;
                }
            }
        }

        @Override
        public boolean unlock() throws Exception {
            if (lockStatus == LockStatus.UNLOCK) {
                return true;
            }

            deleteLockNode(name);
            lockStatus = LockStatus.UNLOCK;
            lockBarrier.reset();
            System.out.println(name + " 释放锁");
            name = null;
            return true;
        }

        @Override
        public void lockWithExpire(long expire, TimeUnit unit) throws Exception {

        }

        @Override
        public boolean tryLockWithExpire(long expire, TimeUnit unit) throws Exception {
            return false;
        }

        @Override
        public boolean tryLockWithExpire(long tryUntil, long expire, TimeUnit unit) throws Exception {
            return false;
        }

        @Override
        public void process(WatchedEvent event) {

        }
    }


    private String createLockNode(String name) {
        String path = null;
        try {
            path = zooKeeper.create(LOCK_NODE_PARENT_PATH + "/" + name, "".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        } catch (KeeperException | InterruptedException e) {
            System.out.println(" failed to create lock node");
            return null;
        }

        return path;
    }

    private void deleteLockNode(String name) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(LOCK_NODE_PARENT_PATH + "/" + name, false);
        zooKeeper.delete(LOCK_NODE_PARENT_PATH + "/" + name, stat.getVersion());
    }

    private String getPrefix(String name) {
        return name.substring(0, name.lastIndexOf('-') + 1);
    }

    private Integer getSequence(String name) {
        return Integer.valueOf(name.substring(name.lastIndexOf('-') + 1));
    }

    /**
     * 获得锁的条件：（二选一）
     * <p>
     * 1. 是第一个节点
     * 2. 该节点前面无写锁节点
     * <p>
     * 前提条件是：lock时创建临时顺序节点，unlock需要删除临时顺序节点
     *
     * @param name
     * @param nodes
     * @return
     */
    private boolean canAcquireLock(String name, List<String> nodes) {
        // 1. 是第一个顺序节点
        if (isFirstNode(name, nodes)) {
            return true;
        }

        // 2. 该节点前面无写锁节点
        boolean hasPreceedingWriteOps = false;
        for (String n : nodes) {
            if (!n.equals(name) && n.contains("write")) {
                return false;
            } else if (n.equals(name)) {
                return true;
            }
        }
        return !hasPreceedingWriteOps;
    }

    private boolean isFirstNode(String name, List<String> nodes) {
        return nodes.get(0).equals(name);
    }


}
