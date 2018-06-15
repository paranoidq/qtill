package me.qtill.netty.bootstrap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import me.qtill.netty.server.NettyServerBootstrapBuilder;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class NettyServerSample {

    public static void main(String[] args) {
        ServerBootstrap serverBootstrap = NettyServerBootstrapBuilder.getInstance().build();
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("---- receive client connected");
                        }
                    });
                }
            });
            serverBootstrap.bind("0.0.0.0", 16001);
    }
}
