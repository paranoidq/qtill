package me.qtill.netty.bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import me.qtill.netty.util.msg.MsgSendCallback;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class NettyClientSample {
    public static void main(String[] args) throws Exception {
        Bootstrap bootstrap = NettyClientBootstrapBuilder.getInstance().build();
        NettyClient client = NettyClient.newBuilder(bootstrap, "0.0.0.0", 16001)
            .enableHeartbeatSend(true)
            .heartBeatSendPeriodMillis(2000)
            .enableHeartbeatCheck(true)
            .heartBeatCheckPeriodMillis(2000)
            .enableAutoReconnect(true)
            .autoReconnectMaxTimes(2)
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
