package xyz.ariesfish.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class PlainNioServer implements Runnable {
    final Selector selector;
    final ServerSocketChannel serverSocket;

    public PlainNioServer(int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        // 注册serverSocket和selector，监听ACCEPT事件
        SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                // 阻塞直到有事件到达
                selector.select();
                Set<SelectionKey> selected = selector.selectedKeys();
                Iterator<SelectionKey> it = selected.iterator();
                // 处理所有已发生的事件
                while (it.hasNext()) {
                    // 将事件分发给对应的处理单元
                    dispatch(it.next());
                    it.remove();
                }
                selected.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dispatch(SelectionKey key) {
        // 分配Handler处理
        try {
            if (key.isAcceptable()) {
                SocketChannel client = serverSocket.accept();
                new BasicHandler(selector, client);
            }
        } catch (IOException e) {
        }
    }
}
