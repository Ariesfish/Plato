package xyz.ariesfish.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Reactor implements Runnable {
    final Selector selector;
    final ServerSocketChannel serverSocket;

    Reactor(int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        // 注册serverSocket和selector，监听ACCEPT事件
        SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        // 绑定ACCEPT事件对应的处理单元Acceptor
        sk.attach(new Acceptor());
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
                }
                selected.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dispatch(SelectionKey key) {
        // 获得已经绑定的处理单元, Acceptor或各种Handler
        Runnable r = (Runnable) key.attachment();
        if (r != null) {
            // 执行处理单元
            r.run();
        }
    }

    class Acceptor implements Runnable {

        @Override
        public void run() {
            try {
                SocketChannel c = serverSocket.accept();
                if (c != null) {
                    // 以得到的socket，创建一个事件的处理单元
                    new BasicHandler(selector, c);
                    // 多线程的处理单元
                    // new MultithreadedHandler(selector, c);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
