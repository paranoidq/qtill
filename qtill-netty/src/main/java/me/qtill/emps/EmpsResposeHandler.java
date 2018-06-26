package me.qtill.emps;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.qtill.netty.handler.ChannelHandlerAutoBind;
import org.apache.commons.codec.binary.Base64;

/**
 * @author paranoidq
 * @since 1.0.0
 */
@ChannelHandlerAutoBind(indexAtChannel = 1)
public class EmpsResposeHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.out.println(ByteBufUtil.hexDump(msg));

        String msgBase64 = "QEoBARUAnU9OTF9TSCAgICAgICAgIFNIX09OTF9BUFAxICAgIEFEUF9PdXRfaW4gICAgIAAAAAAAAAAAAAAAAQAAAQBFTVBTX1NIICAgICAgICAgICAgICAgICAgICAgICBlbXBzX3JlcXVlc3QgICAgICAgICAgICAgICAgICAgAAAAtgAAAAADAwAAAAAAAAAAACAgICAgICAAADw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04Ij8+Cjxyb290Pgo8b3BlcmF0aW9ucz5CQVNFNjRFbmNvZGU8L29wZXJhdGlvbnM+CjxzZXJ2aWNlPkFEUF9PdXRfaW48L3NlcnZpY2U+CjxzZXJpYWxObz4xPC9zZXJpYWxObz4KPG1zZz5ZV0ZoWVdFPTwvbXNnPgo8cGFyYW1zPjwvcGFyYW1zPgo8L3Jvb3Q+";

        ctx.writeAndFlush(Base64.decodeBase64(msgBase64)).sync();
    }
}
