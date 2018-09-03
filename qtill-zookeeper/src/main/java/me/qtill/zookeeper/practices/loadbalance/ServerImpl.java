package me.qtill.zookeeper.practices.loadbalance;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ServerImpl implements Server {

    private EventLoopGroup bossGroup   = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private ServerBootstrap bootstrap = new ServerBootstrap();

    private ChannelFuture cf;
    private String        zkAddress;
    private String        serversPath;
    private String        currentServerPath;
    private ServerData    serverData;


    private volatile boolean binded = false;

    private final ZkClient        zc;
    private final RegiserProvider regiserProvider;

    private static final Integer SESSION_TIME_OUT = 10000;
    private static final Integer CONNECT_TIME_OUT = 10000;

    public String getCurrentServerPath() {
        return currentServerPath;
    }

    public String getZkAddress() {
        return zkAddress;
    }

    public String getServersPath() {
        return serversPath;
    }

    public ServerData getSd() {
        return serverData;
    }

    public void setSd(ServerData sd) {
        this.serverData = sd;
    }


    public ServerImpl(String zkAddress, String serversPath, ServerData serverData) {
        this.zkAddress = zkAddress;
        this.serversPath = serversPath;
        this.serverData = serverData;

        this.zc = new ZkClient(this.zkAddress, SESSION_TIME_OUT, CONNECT_TIME_OUT, new SerializableSerializer());
        this.regiserProvider = new DefaultRegisterProvider();
    }

    private void initRunning() throws Exception {
        String mePath = serversPath.concat("/").concat(serverData.getHost() + ":" + serverData.getPort().toString());

        // 将需要注册的信息包装为ZookeeperRegisterContext对象，并单独传递到另一个类中，解耦
        regiserProvider.register(new ZookeeperRegisterContext(mePath, zc, serverData));
        currentServerPath = mePath;
    }

    @Override
    public void bind() {
        if (binded) {
            return;
        }

        System.out.println("server binding");
        try {
            initRunning();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, 1024)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new ServerHandler(new DefaultBalanceUpdateProvider(currentServerPath, zc)));
                }
            })
        ;
        try {
            cf = bootstrap.bind(serverData.getPort()).sync();
            binded = true;
            System.out.println("sever binded");
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
