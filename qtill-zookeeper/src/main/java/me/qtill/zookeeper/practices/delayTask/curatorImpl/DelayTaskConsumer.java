package me.qtill.zookeeper.practices.delayTask.curatorImpl;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.state.ConnectionState;

import java.text.MessageFormat;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class DelayTaskConsumer implements QueueConsumer<String> {

    @Override
    public void consumeMessage(String message) throws Exception {
        System.out.println(
            MessageFormat.format(
                "发布资讯. id-{0}, timestamp-{1}, threadName-{2}",
                message, System.currentTimeMillis(), Thread.currentThread().getName())
        );
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        System.out.println(
            MessageFormat.format("State change. New state is - {0}", newState)
        );
    }
}
