package me.qtill.dl;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁接口
 *
 * @author paranoidq
 * @since 1.0.0
 */
public interface DistributedLock {


    /**
     * 阻塞加锁，无过期时间
     */
    void lock() throws Exception;

    /**
     * 非阻塞加锁，无过期时间
     * <p>
     * 加锁成功，返回true；加锁尝试失败，则返回false
     *
     * @return
     */
    boolean tryLock() throws Exception;

    /**
     * 超时阻塞加锁，无过期时间
     * <p>
     * 加锁成功，返回true；指定时间内多次加锁尝试失败，则返回false
     *
     * @param tryUntil
     * @param unit
     * @return
     */
    boolean tryLock(long tryUntil, TimeUnit unit) throws Exception;

    /**
     * 解锁
     * <p>
     * 解锁成功或无锁，返回true；解锁失败，返回false
     *
     * @return
     */
    boolean unlock() throws Exception;

    /**
     * 阻塞加锁，过期时间后锁自动释放锁
     *
     * @param time
     * @param unit
     */
    void lockWithExpire(long expire, TimeUnit unit) throws Exception;


    /**
     * 非阻塞加锁，过期时间后锁自动释放锁
     * <p>
     * 加锁成功，返回true；加锁尝试失败，则返回false
     *
     * @param expire
     * @param unit
     * @return
     */
    boolean tryLockWithExpire(long expire, TimeUnit unit) throws Exception;


    /**
     * 超时阻塞加锁，过期时间后锁自动释放锁
     * <p>
     * 加锁成功，返回true；指定时间内多次加锁尝试失败，则返回false
     *
     * @param tryUntil
     * @param unit
     * @return
     */
    boolean tryLockWithExpire(long tryUntil, long expire, TimeUnit unit) throws Exception;

}
