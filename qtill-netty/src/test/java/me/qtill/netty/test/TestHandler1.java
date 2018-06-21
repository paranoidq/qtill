package me.qtill.netty.test;

import io.netty.channel.ChannelInboundHandlerAdapter;
import me.qtill.netty.handler.ChannelHandlerAutoBind;

/**
 * @author paranoidq
 * @since 1.0.0
 */
@ChannelHandlerAutoBind(indexAtChannel = 1)
public class TestHandler1 extends ChannelInboundHandlerAdapter {


}
