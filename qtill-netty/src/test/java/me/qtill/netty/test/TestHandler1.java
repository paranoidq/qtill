package me.qtill.netty.test;

import io.netty.channel.ChannelInboundHandlerAdapter;
import me.qtill.netty.handler.HandlerAutoBind;

/**
 * @author paranoidq
 * @since 1.0.0
 */
@HandlerAutoBind(indexAtChannel = 1)
public class TestHandler1 extends ChannelInboundHandlerAdapter {


}
