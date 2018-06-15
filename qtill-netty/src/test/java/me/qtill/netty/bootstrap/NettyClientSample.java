package me.qtill.netty.bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import me.qtill.netty.client.NettyPoolClient;
import me.qtill.netty.client.NettyClientBootstrapBuilder;
import me.qtill.netty.client.SendCallback;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class NettyClientSample {
    public static void main(String[] args) throws Exception {
        Bootstrap bootstrap = NettyClientBootstrapBuilder.getInstance().build();
        NettyPoolClient client = NettyPoolClient.builder(bootstrap, "0.0.0.0", 16001)
            .enableHeartbeatSend(true)
            .heartBeatSendPeriodMillis(2000)
            .enableHeartbeatCheck(true)
            .heartBeatCheckPeriodMillis(2000)
            .enableAutoReconnect(true)
            .autoReconnectMaxTimes(2)
            .build();

        client.send("ab".getBytes(), new SendCallback() {
            @Override
            public void onSuccess(ChannelFuture future) {
                System.out.println("call back is invoked");
            }

            @Override
            public void onFailed(ChannelFuture future) {

            }
        });

    }
}
