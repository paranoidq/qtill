package me.qtill.dl.redis;

import me.qtill.dl.DistributedLock;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class RedisDistributedLock implements DistributedLock {

    private static final String SET_IF_NOT_EXIST     = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    private static final String LOCK_VALUE           = "_locked_";
    private static final String LOCK_PREFIX          = "dlock_";
    private static final String LOCK_RESP            = "OK";
    private static final Long   UNLOCK_RESP          = 1L;

    private String    key;
    private JedisPool jedisPool;

    /**
     * 自旋测试超时阈值，考虑到网络的延时性，这里设为1000毫秒
     */
    private final        long spinForTimeoutThreshold = 1000;
    private static final long SLEEP_TIME              = 100L;

    @Override
    public void lock() {
        lockWithExpire(-1, TimeUnit.SECONDS);
    }

    @Override
    public boolean tryLock() {
        return tryLockWithExpire(-1, TimeUnit.SECONDS);
    }

    @Override
    public boolean tryLock(long tryUntil, TimeUnit unit) throws Exception {
        return tryLockWithExpire(tryUntil, -1, TimeUnit.SECONDS);
    }

    @Override
    public boolean unlock() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object result = jedis.eval(script, Collections.singletonList(lockKey()), Collections.singletonList(lockValue()));
            return UNLOCK_RESP.equals(result);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public void lockWithExpire(long expire, TimeUnit unit) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            while (true) {
                if (doLock(jedis, unit.toMillis(expire))) {
                    return;
                }
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean tryLockWithExpire(long expire, TimeUnit unit) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (doLock(jedis, unit.toMillis(expire))) {
                return true;
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }

    @Override
    public boolean tryLockWithExpire(long tryUntil, long expire, TimeUnit unit) throws Exception {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            long expireMilllsTimeout = unit.toMillis(expire);

            long tryMillisTimeout = unit.toMillis(tryUntil);
            final long deadline = System.currentTimeMillis() + tryMillisTimeout;
            for (; ; ) {
                if (doLock(jedis, expireMilllsTimeout)) {
                    return true;
                }

                // 在容忍范围内，进行sleep，避免连续while空循环消耗CPU
                if (tryMillisTimeout > spinForTimeoutThreshold) {
                    Thread.sleep(SLEEP_TIME);
                }

                // sleep唤醒之后，需要检测一下是否已经到期，如果已经过期，应停止进入后续自旋立即返回false
                tryMillisTimeout = deadline - System.currentTimeMillis();
                if (tryMillisTimeout <= 0L) {
                    return false;
                }
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 执行lock指令
     *
     * @param jedis
     * @param timeout
     * @return
     */
    private boolean doLock(Jedis jedis, long timeout) {
        String rst = timeout > 0
            ? jedis.set(lockKey(), lockValue(), SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, timeout)
            : jedis.set(lockKey(), lockValue(), SET_IF_NOT_EXIST);
        return LOCK_RESP.equals(rst);
    }

    /**
     * 构造redis key
     *
     * @return
     */
    private String lockKey() {
        return LOCK_PREFIX + key;
    }

    private String lockValue() {
        return LOCK_VALUE;
    }
}
