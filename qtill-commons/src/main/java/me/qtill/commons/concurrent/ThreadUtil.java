package me.qtill.commons.concurrent;

import me.qtill.commons.base.annotation.NotNull;
import org.apache.commons.lang3.Validate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 线程相关工具类.
 *
 * 1. 处理了InterruptedException的sleep
 *
 * 2. 正确的InterruptedException处理方法
 */
public class ThreadUtil {

    /**
     * sleep等待, 单位为毫秒, 已捕捉并处理InterruptedException.
     */
    public static void sleep(long durationMillis) {
        try {
            Thread.sleep(durationMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * sleep等待，已捕捉并处理InterruptedException.
     */
    public static void sleep(long duration, TimeUnit unit) {
        try {
            Thread.sleep(unit.toMillis(duration));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 纯粹为了提醒下处理InterruptedException的正确方式，除非你是在写不可中断的任务.
     */
    public static void handleInterruptedException() {
        Thread.currentThread().interrupt();
    }

    /**
     * 阻塞当前线程，同时释放持有的锁
     *
     * @param parkNanos 阻塞纳秒数，超时自动唤醒
     */
    public static void park(long parkNanos) {
        LockSupport.parkNanos(parkNanos);
    }

    /**
     * 阻塞当前线程，同时释放持有的锁
     *
     * @param blocker   同步对象，线程通过该同步对象进行阻塞
     * @param parkNanos 阻塞纳秒数，超时自动唤醒
     */
    public static void parkOnObject(@NotNull Object blocker, long parkNanos) {
        LockSupport.parkNanos(blocker, parkNanos);
    }

    /**
     * 唤醒阻塞线程
     *
     * @param thread
     */
    public static void unpark(@NotNull Thread thread) {
        Validate.notNull(thread);
        LockSupport.unpark(thread);
    }

    /**
     * 阻塞当前线程，同时释放持有的锁
     *
     * @param deadline 从epoch算起的绝对时间，单位ms
     */
    public static void parkUntil(long deadline) {
        LockSupport.parkUntil(deadline);
    }

    /**
     * 阻塞当前线程，同时释放持有的锁
     *
     * @param blocker  同步对象，线程通过该同步对象进行阻塞
     * @param deadline 从epoch算起的绝对时间，单位ms
     */
    public static void parkUntil(@NotNull Object blocker, long deadline) {
        LockSupport.parkUntil(blocker, deadline);
    }
}