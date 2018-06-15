package me.qtill.netty.pool;

import io.netty.channel.Channel;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class NettyChannelPoolHandler extends AbstractChannelPoolHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannelPoolHandler.class);


    @Override
    public void channelCreated(Channel ch) throws Exception {
        logger.info("Channel created. Channel Id: [{}]", ch.id());

    }

}
