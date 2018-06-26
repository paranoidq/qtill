package me.qtill.emps.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.qtill.netty.handler.ChannelHandlerAutoBind;
import org.apache.commons.codec.binary.Hex;

import java.util.List;

/**
 * @author paranoidq
 * @since 1.0.0
 */
@ChannelHandlerAutoBind(indexAtChannel = 1)
public class InboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到应答报文");
        System.out.println(msg);
    }
}
