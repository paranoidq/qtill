package me.qtill.netty.bootstrap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import me.qtill.netty.server.NettyServer;
import me.qtill.netty.server.NettyServerBootstrapBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class NettyServerSample {

    public static void main(String[] args) throws InterruptedException {
        ServerBootstrap serverBootstrap = NettyServerBootstrapBuilder.getInstance().build();
        NettyServer server = NettyServer.builder(serverBootstrap, 16001)
            .enableHeartbeatSend(true)
            .heartBeatSendPeriodMillis(2000)
            .heartBeatCheckTolerance(-1)
            .enableHeartbeatCheck(true)
            .heartBeatCheckPeriodMillis(4000)
            .handlerAutoBindProcessor(new String("me.qtill.netty.test"))
            .build();

        server.start();

        TimeUnit.SECONDS.sleep(12324);
    }
}
