package me.qtill.redis.practices.delayTask;

import me.qtill.redis.practices.RedisClient;
import redis.clients.jedis.Jedis;

import java.text.MessageFormat;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 参考资料：http://mp.weixin.qq.com/s?__biz=MzAxNjM2MTk0Ng==&mid=2247485019&idx=1&sn=11c7f5e90d53b3a0f737c46ae312535e&chksm=9bf4b6eeac833ff80d85017f60f51a4cbb62dc5573f6b00e977e4dad4f5ef68b51681b8945fe&mpshare=1&scene=1&srcid=0903BhwawSmoe1x3ZOPawZSU#rd
 *
 * 主要涉及数据结构：sorted set，用timestamp作为score
 *
 * 涉及操作指令：
 * - zrangeByScore：用于拉取到期的延时任务，示例中同时制定了只拉取一个延时任务，可以同时拉取一串
 * - zrem：用于解决多线程的并发问题，只有移除成功的线程才有权执行延时任务，避免重复执行。（也可以通过其他setnx之类的方式，本质是一样的，都是依赖于redis的串行执行机制）
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class DelayTaskConsumer {

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();


    public void start() {
        scheduledExecutorService.scheduleWithFixedDelay(
            new DelayTaskHandler(),
            1,
            1,
            TimeUnit.SECONDS
        );
    }


    public static class DelayTaskHandler implements Runnable {
        @Override
        public void run() {
            Jedis client = RedisClient.getClient();
            try {
                // 1. zrangeByScore指令
                Set<String> ids = client.zrangeByScore(
                    Constants.DELAY_TASK_QUEUE,
                    0, System.currentTimeMillis(), 0, 1);
                if (ids == null || ids.isEmpty()) {
                    return;
                }

                // 2. zrem指令
                for (String id : ids) {
                    Long count = client.zrem(Constants.DELAY_TASK_QUEUE, id);
                    // 并发问题，必须移除成功才可以执行任务
                    if (count != null && count == 1) {
                        System.out.println(MessageFormat.format("发布资讯。id - {0} , timeStamp - {1} , " + "threadName - {2}",
                            id,
                            System.currentTimeMillis(),
                            Thread.currentThread().getName()));
                    }
                }
            } finally {
                client.close();
            }
        }
    }
}

