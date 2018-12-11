package me.qtill.commons.misc;

/**
 * +-----+----------------+---------------+-----------+------------- +
 * | (1) | timestamp(41)  | datacenter(5) | worker(5) | sequence(12) |
 * +-----+----------------+---------------+-----------+------------- +
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class SnowflakeUniqueId {

    // 开始时间戳
    private static final long twepoch           = 1420041600000L;
    // 机器id位数
    private static final long workerIdBits      = 5L;
    // 数据中心id位数
    private static final long dataCenterIdBits  = 5L;
    // 支持最大机器id
    private static final long maxWorkerId       = -1L ^ (-1L << workerIdBits);
    // 支持最大数据中心id
    private static final long maxDataCenterId   = -1L ^ (-1L << dataCenterIdBits);
    // 序号占用位数
    private static final long sequenceBits      = 12L;
    // 机器id自增位操作时的左移位数
    private static final long workerIdShift     = sequenceBits;
    // 数据中心id自增位操作时的左移位数
    private static final long dataCenterIdShift = workerIdShift + workerIdBits;
    // 时间戳自增位操作时的左移位数
    private static final long timestampShift    = dataCenterIdShift + dataCenterIdBits;
    // 序列号位操作掩码
    private static final long sequenceMask      = -1L ^ (-1L << sequenceBits);


    private long workerId;
    private long dataCenterId;
    private long sequence      = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeUniqueId(long workerId, long dataCenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDataCenterId));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }


    /**
     * 获取下一个ID
     * <p>
     * 同步调用
     *
     * @return
     */
    public synchronized long nextId() {
        long timestamp = currentTimeMillis();

        // 时钟回调，可能造成重复ID，直接抛异常
        // TODO
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        // 同一毫秒内，自增sequence
        if (lastTimestamp == timestamp) {
            // 只有2的n次幂可以通过这种方式取余
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) { // 取到最大值+1
                timestamp = waitUntilNextTick(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;

        // 位移运算拼接成64位ID
        return ((timestamp - twepoch) << timestampShift)    // 存储时间戳的差值，而不是绝对时间戳
            | (dataCenterId << dataCenterIdShift)
            | (workerId << workerIdShift)
            | sequence;

    }

    /**
     * 返回当前时间的毫秒值
     *
     * @return
     */
    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 循环等待，知道下一个毫米获得新的时间戳才返回
     */
    private long waitUntilNextTick(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

}