package me.qtill.netty.util.connection;

import io.netty.channel.ChannelFuture;
import io.netty.util.internal.ThrowableUtil;

import java.util.logging.Logger;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public abstract class ConnectCommand {

    private static final Logger logger = Logger.getLogger(ConnectCommand.class.getName());


    public void call() {
        try {
            ChannelFuture connectFuture = doConnect();
            if (connectFuture != null) {
                connectFuture.sync();
                if (connectFuture.isSuccess()) {
                    logger.info("*** Connection success");
                }
            }
        } catch (Exception e) {
            logger.warning("*** Try connecting failed. Causes: [" + ThrowableUtil.stackTraceToString(e) + "]");
        }
    }

    /**
     * 该方法必须是同步的
     * @return
     * @throws Exception
     */
    public abstract ChannelFuture doConnect() throws Exception;

}
