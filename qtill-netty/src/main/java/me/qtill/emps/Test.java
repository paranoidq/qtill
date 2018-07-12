package me.qtill.emps;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import me.qtill.netty.client.NettyClient;
import me.qtill.netty.client.NettyClientBootstrapBuilder;
import me.qtill.netty.client.SendCallback;
import me.qtill.netty.server.NettyServer;
import me.qtill.netty.server.NettyServerBootstrapBuilder;
import org.apache.commons.codec.binary.Base64;

import java.util.Scanner;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Test {

    public static Stopwatch stopwatch = Stopwatch.createUnstarted();

    public static void main(String[] args) {
//        Bootstrap bootstrap = NettyClientBootstrapBuilder.getInstance().build();
//        NettyClient client = NettyClient.builder(bootstrap, "0.0.0.0", 61301)
//            .handlerAutoBindProcessor("me.qtill.emps").build();


        ServerBootstrap serverBootstrap =  NettyServerBootstrapBuilder.getInstance().build();
        NettyServer server = NettyServer.builder(serverBootstrap, 61301).handlerAutoBindProcessor("me.qtill.emps.server")
            .build();

        server.start();


//        String msgBase64 = "QEoBARUAnU9OTF9TSCAgICAgICAgIFNIX09OTF9BUFAxICAgIEFEUF9PdXRfaW4gICAgIAAAAAAAAAAAAAAAAQAAAQBFTVBTX1NIICAgICAgICAgICAgICAgICAgICAgICBlbXBzX3JlcXVlc3QgICAgICAgICAgICAgICAgICAgAAAAtgAAAAADAwAAAAAAAAAAACAgICAgICAAADw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04Ij8+Cjxyb290Pgo8b3BlcmF0aW9ucz5CQVNFNjRFbmNvZGU8L29wZXJhdGlvbnM+CjxzZXJ2aWNlPkFEUF9PdXRfaW48L3NlcnZpY2U+CjxzZXJpYWxObz4xPC9zZXJpYWxObz4KPG1zZz5ZV0ZoWVdFPTwvbXNnPgo8cGFyYW1zPjwvcGFyYW1zPgo8L3Jvb3Q+";
//
//        client.start();
//
//        Scanner scanner = new Scanner(System.in);
//        while (scanner.hasNext()) {
//            String any = scanner.next();
//            if (!"stop".equals(any)) {
//                client.send(Base64.decodeBase64(msgBase64), new SendCallback() {
//                    @Override
//                    public void onSuccess(ChannelFuture future) {
//                        System.out.println("send success");
//                    }
//
//                    @Override
//                    public void onFailed(ChannelFuture future) {
//                        System.err.println("send error: " + Throwables.getStackTraceAsString(future.cause()));
//                    }
//                });
//            } else {
//                client.shutdown();
//                return;
//            }
//        }
    }
}
