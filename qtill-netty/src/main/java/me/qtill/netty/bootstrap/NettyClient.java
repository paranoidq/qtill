package me.qtill.netty.bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import me.qtill.netty.util.connection.ConnectCommand;
import me.qtill.netty.util.connection.AutoReconnectionHandler;
import me.qtill.netty.util.heartbeat.HeartbeatHandler;
import me.qtill.netty.util.msg.MsgSendCallback;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class NettyClient {
    private static final Logger logger = Logger.getLogger(NettyClient.class.getName());

    private Bootstrap bootstrap;
    private String remoteIp;
    private int remotePort;

    // 自动断线重连
    private boolean enableAutoReconnect = false;
    private int autoReconnectMaxTimes = 5;
    private long autoReconnectDelayMillis = 2000;
    private AtomicInteger autoReconnectCurrentTimes = new AtomicInteger(0);


    // 自动心跳包发送
    private boolean enableHeartbeatSend = false;
    private boolean enableHeartbeatCheck = false;
    private boolean normalMsgAsHeartbeat = false;
    private ByteBuf heartbeatSendToken = Unpooled.directBuffer().writeBytes(new byte[]{0x00, 0x00});
    private ByteBuf heartbeatCheckToken = Unpooled.directBuffer().writeBytes(new byte[]{0x00, 0x00});
    private long heartBeatSendPeriod = 5000;
    private long heartBeatCheckPeriod = 5000;
    private int heartBeatCheckTolerance = 3;

    // 自动报文重发
    private boolean enableAutoResend = false;
    private int autoResendMaxTime = 3;

    private SocketChannel channel;

    private NettyClient(Bootstrap bootstrap, String remoteIp, int remotePort) {
        this.bootstrap = bootstrap;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
    }

    public static NettyClientBuilder newBuilder(Bootstrap bootstrap, String remoteIp, int remotePort) {
        return new NettyClientBuilder(bootstrap, remoteIp, remotePort);
    }


    /**
     * NettyClient构造器
     *
     *
     */
    public static class NettyClientBuilder {
        private NettyClient client;

        public NettyClientBuilder(Bootstrap bootstrap, String remoteIp, int remotePort) {
            this.client = new NettyClient(bootstrap, remoteIp, remotePort);
        }

        public NettyClientBuilder enableAutoReconnect(boolean enableReconnect) {
            client.enableAutoReconnect = enableReconnect;
            return this;
        }

        public NettyClientBuilder autoReconnectMaxTimes(int reconnectMaxTimes) {
            client.autoReconnectMaxTimes = reconnectMaxTimes;
            return this;
        }

        public NettyClientBuilder autoReconnectDelayMillis(long autoReconnectDelayMillis) {
            client.autoReconnectDelayMillis = autoReconnectDelayMillis;
            return this;
        }

        public NettyClientBuilder enableHeartbeatSend(boolean enableHeartbeatSend) {
            client.enableHeartbeatSend = enableHeartbeatSend;
            return this;
        }

        public NettyClientBuilder enableHeartbeatCheck(boolean enableHeartbeatCheck) {
            client.enableHeartbeatCheck = enableHeartbeatCheck;
            return this;
        }

        public NettyClientBuilder normalMsgAsHeartbeat(boolean normalMsgAsHeartbeat) {
            client.normalMsgAsHeartbeat = normalMsgAsHeartbeat;
            return this;
        }

        public NettyClientBuilder heartbeatSendToken(String heartbeatToken) {
            client.heartbeatSendToken = Unpooled.directBuffer().writeBytes(heartbeatToken.getBytes());
            return this;
        }

        public NettyClientBuilder heartbeatCheckToken(String heartbeatToken) {
            client.heartbeatCheckToken = Unpooled.directBuffer().writeBytes(heartbeatToken.getBytes());
            return this;
        }

        public NettyClientBuilder heartBeatSendPeriodMillis(long heartBeatSendPeriod) {
            client.heartBeatSendPeriod = heartBeatSendPeriod;
            return this;
        }

        public NettyClientBuilder heartBeatCheckPeriodMillis(long heartBeatCheckPeriod) {
            client.heartBeatCheckPeriod = heartBeatCheckPeriod;
            return this;
        }

        public NettyClientBuilder heartBeatCheckTolerance(int heartBeatCheckTolerance) {
            client.heartBeatCheckTolerance = heartBeatCheckTolerance;
            return this;
        }

        public NettyClient build() {
            client.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    // 增加自动重连
                    if (client.enableAutoReconnect) {
                        ch.pipeline().addLast(new AutoReconnectionHandler().enableAutoReconnect(client.autoReconnectMaxTimes,
                            new ConnectCommand() {
                                @Override
                                public ChannelFuture doConnect() {
                                    return client.doConnect();
                                }
                            })
                        );
                    }

                    // 增加心跳包handler
                    ch.pipeline().addLast(new IdleStateHandler(client.heartBeatCheckPeriod, client.heartBeatSendPeriod, Math.max(
                        client.heartBeatCheckPeriod, client.heartBeatSendPeriod), TimeUnit.MILLISECONDS));
                    ch.pipeline().addLast(new HeartbeatHandler(
                        client.enableHeartbeatSend,
                        client.enableHeartbeatCheck,
                        client.normalMsgAsHeartbeat,
                        client.heartbeatSendToken,
                        client.heartbeatCheckToken,
                        client.heartBeatCheckTolerance
                    ));
                }
            });

            return this.client;
        }
    }


    public void start() {
        logger.info("*** NettyClient started");
        doConnect();
    }


    private ChannelFuture doConnect() {
        if (channel != null && channel.isActive()) {
            return null;
        }
        // 同步
        ChannelFuture future = bootstrap.connect(remoteIp, remotePort);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info("*** NettyClient connect to server success");
                    autoReconnectCurrentTimes.set(0);
                } else {
                    if (!enableAutoReconnect) {
                        logger.severe("*** NettyClient connect to server failed");
                        return;
                    }
                    if (autoReconnectCurrentTimes.incrementAndGet() > autoReconnectMaxTimes) {
                        logger.severe("*** NettyClient trying connect more than [" + autoReconnectMaxTimes + "] times. Stop trying");
                    } else {
                        logger.severe("*** NettyClient connect failed, retry...");
                        future.channel().eventLoop().schedule(new Runnable() {
                            @Override
                            public void run() {
                                doConnect();
                            }
                        }, autoReconnectDelayMillis, TimeUnit.MILLISECONDS);
                    }
                }
            }
        });
        future.syncUninterruptibly();
        channel = (SocketChannel) future.channel();
        return future;
    }

    /**
     * 异步发送
     * @param msg
     */
    public void send(byte[] msg) {
        final ByteBuf buf = Unpooled.copiedBuffer(msg);
        ChannelFuture writeFuture = channel.writeAndFlush(buf);
        writeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info("*** NettyClient send success: [" + ByteBufUtil.hexDump(buf.retain()) + "]");
                } else {
                    logger.warning("*** NettyClient send failed: [" + ByteBufUtil.hexDump(buf.retain()) + "]. Cause: [" + future.cause().toString() +"]");
                }
            }
        });
    }

    /**
     * 异步发送，并通过回调获取结果
     * @param msg
     */
    public void send(byte[] msg, final MsgSendCallback callback) {
        final ByteBuf buf = Unpooled.copiedBuffer(msg).retain();
        ChannelFuture writeFuture = channel.writeAndFlush(buf);
        writeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info("*** NettyClient send success: [" + ByteBufUtil.hexDump(buf) + "]");
                    callback.onSuccess(future);
                } else {
                    logger.warning("*** NettyClient send failed: [" + ByteBufUtil.hexDump(buf) + "]. Cause: [" + future.cause().toString() +"]");
                    callback.onFailed(future);
                }
            }
        });
    }
}
