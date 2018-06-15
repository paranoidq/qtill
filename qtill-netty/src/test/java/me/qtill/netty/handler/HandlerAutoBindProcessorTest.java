package me.qtill.netty.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import me.qtill.netty.client.MsgSendCallback;
import me.qtill.netty.client.NettyClient;
import me.qtill.netty.client.NettyClientBootstrapBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class HandlerAutoBindProcessorTest {

    public static void main(String[] args) throws Exception {
        Bootstrap bootstrap = NettyClientBootstrapBuilder.getInstance().build();
        NettyClient client = NettyClient.newBuilder(bootstrap, "0.0.0.0", 16001)
            .enableHeartbeatSend(true)
            .heartBeatSendPeriodMillis(2000)
            .enableHeartbeatCheck(true)
            .heartBeatCheckPeriodMillis(2000)
            .enableAutoReconnect(true)
            .autoReconnectMaxTimes(2)
            .handlerAutoBindProcessor(new HandlerAutoBindProcessor("me.qtill.netty.test"))
            .build();

        client.start();
        client.send("ab".getBytes(), new MsgSendCallback() {
            @Override
            public void onSuccess(ChannelFuture future) {
                System.out.println("call back is invoked");
            }
        });

    }
}