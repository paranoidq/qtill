package me.qtill.netty.test;

import io.netty.channel.*;
import me.qtill.netty.handler.ChannelHandlerAutoBind;

/**
 * @author paranoidq
 * @since 1.0.0
 */
@ChannelHandlerAutoBind(indexAtChannel = 2, handlerName = "test2")
public class TestHandler2 extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("##### write message: " + msg);
        super.write(ctx, msg, promise);
    }

}
