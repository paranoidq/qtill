package me.qtill.netty.util.connection;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 管理连接事件
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class AutoReconnectionHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(AutoReconnectionHandler.class);

    private boolean autoReconnect = false;
    private int autoReconnectMaxTimes = 3;
    private ConnectCommand connectCommand;
    private AtomicInteger autoReconnectCurrentTimes = new AtomicInteger(0);


    public AutoReconnectionHandler() {

    }

    public AutoReconnectionHandler enableAutoReconnect(int autoReconnectMaxTimes, ConnectCommand connectCommand) {
        this.autoReconnect = true;
        this.autoReconnectMaxTimes = autoReconnectMaxTimes;
        this.connectCommand = connectCommand;
        return this;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("*** Connection active");
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.warn("*** Connection inactive");
        if (autoReconnect) {
            if (autoReconnectCurrentTimes.incrementAndGet() < autoReconnectMaxTimes) {
                logger.warn("*** Try connecting [" + autoReconnectCurrentTimes.get() +"] ...");
                try {
                    connectCommand.call();
                } catch (Throwable throwable) {
                    logger.warn("*** Try connecting failed. Cause: [" + throwable.toString() + "]");
                }
            } else {
                logger.warn("*** Try connect more than [" + autoReconnectMaxTimes + "] times. Stop trying");
            }
        }
        ctx.fireChannelInactive();
    }
}
