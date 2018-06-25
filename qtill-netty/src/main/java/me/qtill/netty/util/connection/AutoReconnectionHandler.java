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

    private ConnectCommand connectCommand;

    public AutoReconnectionHandler(ConnectCommand connectCommand) {
        this.connectCommand = connectCommand;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("*** Connection active");
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.warn("*** Connection inactive");
        logger.warn("*** Try reconnecting  ...");
        try {
            connectCommand.invoke();
        } catch (Throwable throwable) {
            logger.warn("*** Try reconnecting failed. Cause: [" + throwable.toString() + "]");
        }
        ctx.fireChannelInactive();
    }
}
