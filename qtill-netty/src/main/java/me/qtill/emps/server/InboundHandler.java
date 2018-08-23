package me.qtill.emps.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.qtill.emps.Test;
import me.qtill.netty.handler.ChannelHandlerAutoBind;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author paranoidq
 * @since 1.0.0
 */
@ChannelHandlerAutoBind(indexAtChannel = 1)
public class InboundHandler extends ChannelInboundHandlerAdapter {

    AtomicInteger count = new AtomicInteger(10000);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到应答报文");
        System.out.println(msg);

        if (count.get() > 0) {
            count.decrementAndGet();
        } else {
            Test.stopwatch.stop();
            long mills = Test.stopwatch.elapsed(TimeUnit.MILLISECONDS);
            System.out.println("TPS: " + 10000 / mills * 1000);
        }

    }
}
