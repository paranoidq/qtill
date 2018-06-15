package me.qtill.netty.client;

import io.netty.channel.ChannelFuture;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public interface SendCallback {

    void onSuccess(ChannelFuture future);

    void onFailed(ChannelFuture future);
}
