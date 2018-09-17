package me.qtill.zookeeper.practices.delayTask.curatorImpl;

import com.google.common.util.concurrent.MoreExecutors;

import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class DelayTaskTest {

    public static void main(String[] args) throws Exception {
        DelayTaskProducer producer = new DelayTaskProducer();

        long now = new Date().getTime();
        System.out.println(MessageFormat.format("start time - {0}", now));
        producer.produce("1", now + TimeUnit.SECONDS.toMillis(5));
        producer.produce("2", now + TimeUnit.SECONDS.toMillis(10));
        producer.produce("3", now + TimeUnit.SECONDS.toMillis(15));
        producer.produce("4", now + TimeUnit.SECONDS.toMillis(20));
        producer.produce("5", now + TimeUnit.SECONDS.toMillis(25));

        TimeUnit.HOURS.sleep(1);
    }
}
