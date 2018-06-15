package me.qtill.netty.client;

import io.netty.channel.ChannelFuture;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public abstract class MsgSendCallback {

    public void onSuccess(ChannelFuture future) {
    }

    public void onFailed(ChannelFuture future) {

    }
}
