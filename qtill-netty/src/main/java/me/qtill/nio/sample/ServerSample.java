package me.qtill.nio.sample;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ServerSample implements Runnable {

    public static void main(String[] args) throws Exception {

    }

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private volatile boolean stopped;

    public ServerSample() {
        try {
            // 1. open
            serverSocketChannel = ServerSocketChannel.open();

            // 2. 设置非阻塞等参数
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().setSoTimeout(1000);
            // 3. 绑定ip、端口
            serverSocketChannel.socket().bind(new InetSocketAddress("", 1024));

            // 创建Selector多路复用器
            selector = Selector.open();

            // channel绑定Selector，并注册感兴趣的事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                // 阻塞等待select
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey selectionKey;
                while (iterator.hasNext()) {
                    selectionKey = iterator.next();
                    // 主动remove掉
                    iterator.remove();
                    try {
                        handler(selectionKey);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (selectionKey != null) {
                            selectionKey.cancel();
                            if (selectionKey.channel() != null) {
                                selectionKey.channel().close();
                            }
                        }
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void handler(SelectionKey selectionKey) throws Exception {
        if (selectionKey.isValid()) {
            // 新的连接
            if (selectionKey.isAcceptable()) {
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);

                // 将新建的连接注册到selector上，并监听读事件
                socketChannel.register(selector, SelectionKey.OP_READ);
            }
            // 读事件
            if (selectionKey.isReadable()) {
                // read data
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                // ...
            }
        }
    }
}
