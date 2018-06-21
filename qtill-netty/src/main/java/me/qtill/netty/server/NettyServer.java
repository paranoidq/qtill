package me.qtill.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import me.qtill.netty.handler.ChannelHandlerAutoBindProcessor;
import me.qtill.netty.util.heartbeat.HeartbeatHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private ServerBootstrap serverBootstrap;

    private InetSocketAddress listenAddress;

    private volatile boolean running = false;

    // 自动心跳包发送
    private boolean enableHeartbeatSend     = false;
    private boolean enableHeartbeatCheck    = false;
    private boolean normalMsgAsHeartbeat    = false;
    private ByteBuf heartbeatSendToken      = Unpooled.directBuffer().writeBytes(new byte[]{0x00, 0x00});
    private ByteBuf heartbeatCheckToken     = Unpooled.directBuffer().writeBytes(new byte[]{0x00, 0x00});
    private long    heartBeatSendPeriod     = 5000;
    private long    heartBeatCheckPeriod    = 10000;
    private int     heartBeatCheckTolerance = 3;

    // Handler自动绑定处理器处理的package
    private String channelHandlerAutoBindScanPackage;

    /**
     * 构造函数
     *
     * @param serverBootstrap
     * @param listenAddress
     */
    private NettyServer(ServerBootstrap serverBootstrap, InetSocketAddress listenAddress) {
        this.serverBootstrap = serverBootstrap;
        this.listenAddress = listenAddress;
    }


    /**
     * Builder模式
     *
     * @param serverBootstrap
     * @param listenAddress
     * @return
     */
    public static Builder builder(ServerBootstrap serverBootstrap, InetSocketAddress listenAddress) {
        return new Builder(serverBootstrap, listenAddress);
    }

    /**
     * Builder模式
     *
     * @param serverBootstrap
     * @param listenIp
     * @param listenPort
     * @return
     */
    public static Builder builder(ServerBootstrap serverBootstrap, String listenIp, int listenPort) {
        return builder(serverBootstrap, new InetSocketAddress(listenIp, listenPort));
    }

    /**
     * Builder模式
     *
     * @param serverBootstrap
     * @param listenPort
     * @return
     */
    public static Builder builder(ServerBootstrap serverBootstrap, int listenPort) {
        return builder(serverBootstrap, new InetSocketAddress(listenPort));
    }


    /**
     * 启动NettyServer
     */
    public void start() {
        ChannelFuture bindFuture = null;
        try {
            bindFuture = this.serverBootstrap.bind(listenAddress).sync();
            if (bindFuture.isSuccess()) {
                running = true;
                logger.info("*** NettyServer started now !");

            } else {
                logger.error("*** NettyServer start failed. Cause: ", bindFuture.cause());
            }
        } catch (Exception e) {
            logger.error("*** NettyServer start failed. Cause: ", e);
        }
    }


    /**
     * 关闭NettyServer
     */
    public void shutdown() {
        if (running) {
            serverBootstrap.config().group().shutdownGracefully().syncUninterruptibly();
            serverBootstrap.config().childGroup().shutdownGracefully().syncUninterruptibly();
            serverBootstrap = null;
            running = false;
            logger.info("*** NettyServer is shutdown");
        } else {
            logger.error("*** NettyServer is not running");
        }
    }


    /**
     * NettyServer Builder
     */
    public static class Builder {
        private NettyServer server;

        public NettyServer build() {
            server.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    // 心跳包
                    ch.pipeline().addLast(new IdleStateHandler(server.heartBeatCheckPeriod, server.heartBeatSendPeriod, Math.max(
                        server.heartBeatCheckPeriod, server.heartBeatSendPeriod), TimeUnit.MILLISECONDS));
                    ch.pipeline().addLast(new HeartbeatHandler(
                        server.enableHeartbeatSend,
                        server.enableHeartbeatCheck,
                        server.normalMsgAsHeartbeat,
                        server.heartbeatSendToken,
                        server.heartbeatCheckToken,
                        server.heartBeatCheckTolerance
                    ));

                    // 自动添加handler
                    if (!StringUtils.isEmpty(server.channelHandlerAutoBindScanPackage)) {
                        new ChannelHandlerAutoBindProcessor(server.channelHandlerAutoBindScanPackage).autoBind(ch.pipeline());
                    }
                }
            });
            return server;
        }


        public Builder(ServerBootstrap serverBootstrap, InetSocketAddress listenAddress) {
            this.server = new NettyServer(serverBootstrap, listenAddress);
        }

        public NettyServer.Builder enableHeartbeatSend(boolean enableHeartbeatSend) {
            server.enableHeartbeatSend = enableHeartbeatSend;
            return this;
        }

        public NettyServer.Builder enableHeartbeatCheck(boolean enableHeartbeatCheck) {
            server.enableHeartbeatCheck = enableHeartbeatCheck;
            return this;
        }

        public NettyServer.Builder normalMsgAsHeartbeat(boolean normalMsgAsHeartbeat) {
            server.normalMsgAsHeartbeat = normalMsgAsHeartbeat;
            return this;
        }

        public NettyServer.Builder heartbeatSendToken(java.lang.String heartbeatToken) {
            server.heartbeatSendToken = Unpooled.directBuffer().writeBytes(heartbeatToken.getBytes());
            return this;
        }

        public NettyServer.Builder heartbeatCheckToken(java.lang.String heartbeatToken) {
            server.heartbeatCheckToken = Unpooled.directBuffer().writeBytes(heartbeatToken.getBytes());
            return this;
        }

        public NettyServer.Builder heartBeatSendPeriodMillis(long heartBeatSendPeriod) {
            server.heartBeatSendPeriod = heartBeatSendPeriod;
            return this;
        }

        public NettyServer.Builder heartBeatCheckPeriodMillis(long heartBeatCheckPeriod) {
            server.heartBeatCheckPeriod = heartBeatCheckPeriod;
            return this;
        }

        /**
         * 如果heartBeatCheckTolerance大于0，则超过次数没有收到心跳包会主动断开连接
         * 如果heartBeatCheckTolerance小于等于0，则不会主动断开连接
         * @param heartBeatCheckTolerance
         * @return
         */
        public NettyServer.Builder heartBeatCheckTolerance(int heartBeatCheckTolerance) {
            server.heartBeatCheckTolerance = heartBeatCheckTolerance;
            return this;
        }

        public NettyServer.Builder handlerAutoBindProcessor(String handleAutoProcessPackage) {
            server.channelHandlerAutoBindScanPackage = handleAutoProcessPackage;
            return this;
        }
    }


}
