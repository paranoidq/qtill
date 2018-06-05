package me.qtill.nio.sample;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ClientSample implements Runnable {

    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean stopped;


    public ClientSample() {
        try {
            socketChannel = SocketChannel.open();
            selector = Selector.open();
            socketChannel.configureBlocking(false);
        } catch (Exception e) {

        }
    }

    @Override
    public void run() {
        try {
            connect();
        } catch (Exception e) {

        }

        while (!stopped) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey selectionKey = null;
                while (iterator.hasNext()) {
                    selectionKey = iterator.next();
                    // Selector不会自己从已选择键集中移除SelectionKey实例。必须在处理完通道时自己移除
                    iterator.remove();
                    handler(selectionKey);
                }
                if (selectionKey != null) {
                    selectionKey.cancel();
                    if (selectionKey.channel() != null) {
                        selectionKey.channel().close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void connect() throws Exception {
        if (socketChannel.connect(new InetSocketAddress("", 1024))) {
            socketChannel.register(selector, SelectionKey.OP_READ);
            // .. 打印日志
        } else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    private void handler(SelectionKey selectionKey) throws Exception {
        if (selectionKey.isValid()) {
            // 判断是否连接成功
            SocketChannel sc = (SocketChannel) selectionKey.channel();
            if (selectionKey.isConnectable()) {
                if (sc.finishConnect()) {
                    sc.register(selector, SelectionKey.OP_READ);
                    // ... 其他处理
                } else {
                    // 连接失败
                }

            }
            if (selectionKey.isReadable()) {
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    // 移动limit和pos指针
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String s = new String(bytes, "UTF-8");
                } else if (readBytes < 0) {
                    // 对端关闭链路, 清理selector和channel资源
                    if (selectionKey != null) {
                        selectionKey.cancel();
                    }
                    if (selectionKey.channel() != null) {
                        selectionKey.channel().close();
                    }
                } else {
                    // 读到0字节，忽略
                }
            }
        }
    }
}
