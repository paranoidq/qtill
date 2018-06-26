package me.qtill.emps.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.qtill.netty.handler.ChannelHandlerAutoBind;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
@ChannelHandlerAutoBind(indexAtChannel = 2)
public class ServerHandler extends ChannelDuplexHandler {


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("链路建立");

        // 必须新建一个线程，如果在while中发送的话，eventloop始终得不到释放，因此就没办法处理其他的IO事件了
        // 测试中遇到的现象：channelRead始终无法触发
        // 这是一个典型的业务逻辑没有独立线程，导致的Netty eventloop挂死的场景
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    System.out.println("发送请求报文 ... ");
                    String hex = "30303030303030333339404a010115009d4f4e4c5f534820202020202020202053485f4f4e4c5f41505031202020204144505f4f75745f696e202020202000000000000000000000000100000100454d50535f53482020202020202020202020202020202020202020202020656d70735f7265717565737420202020202020202020202020202020202020000000b60000000003030000000000000000002020202020202000003c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d38223f3e0a3c726f6f743e0a3c6f7065726174696f6e733e426173653634456e636f64653c2f6f7065726174696f6e733e0a3c736572766963653e4144505f4f75745f696e3c2f736572766963653e0a3c73657269616c4e6f3e313c2f73657269616c4e6f3e0a3c6d73673e595746685957453d3c2f6d73673e0a3c706172616d733e3c2f706172616d733e0a3c2f726f6f743e";
                    try {
                        ChannelFuture future = ctx.channel().writeAndFlush(Unpooled.directBuffer().writeBytes(Hex.decodeHex(hex))).sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (DecoderException e) {
                        e.printStackTrace();
                    }

                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("收到应答报文");
//        System.out.println(msg);
//    }


}
