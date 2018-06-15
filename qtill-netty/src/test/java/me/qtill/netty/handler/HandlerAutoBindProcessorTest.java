package me.qtill.netty.handler;

import io.netty.bootstrap.Bootstrap;
import me.qtill.netty.client.NettyPoolClient;
import me.qtill.netty.client.NettyClientBootstrapBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class HandlerAutoBindProcessorTest {

    public static void main(java.lang.String[] args) throws Exception {
        Bootstrap bootstrap = NettyClientBootstrapBuilder.getInstance().build();
        NettyPoolClient client = NettyPoolClient.builder(bootstrap, "0.0.0.0", 16001)
            .enableHeartbeatSend(true)
            .heartBeatSendPeriodMillis(2000)
            .enableHeartbeatCheck(true)
            .heartBeatCheckPeriodMillis(2000)
            .enableAutoReconnect(true)
            .autoReconnectMaxTimes(2)
            .handlerAutoBindProcessor(new String("me.qtill.netty.test"))
            .keptConnections(3)
            .build();

        client.start();


        TimeUnit.SECONDS.sleep(1001223);

//        client.send("ab".getBytes(), new SendCallback() {
//            @Override
//            public void onSuccess(ChannelFuture future) {
//                System.out.println("call back is invoked");
//            }
//
//            @Override
//            public void onFailed(ChannelFuture future) {
//
//            }
//        });

    }
}