package xyz.ariesfish.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

class Register implements Runnable {
    private Selector ss, cs;

    Register(Selector serverSelector, Selector clientSelector) {
        this.ss = serverSelector;
        this.cs = clientSelector;
    }

    @Override
    public void run() {
        try {
            ServerSocketChannel listenChannel = ServerSocketChannel.open();
            listenChannel.socket().bind(new InetSocketAddress(8000));
            listenChannel.configureBlocking(false);
            listenChannel.register(ss, SelectionKey.OP_ACCEPT);

            while (true) {
                if (ss.select(1) > 0) {
                    Set<SelectionKey> set = ss.selectedKeys();
                    Iterator<SelectionKey> keyIterator = set.iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isAcceptable()) {
                            try {
                                SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
                                clientChannel.configureBlocking(false);
                                clientChannel.register(cs, SelectionKey.OP_READ);
                            } finally {
                                keyIterator.remove();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {

        }
    }
}

class Reader implements Runnable {
    private Selector cs;

    Reader(Selector clientSelector) {
        this.cs = clientSelector;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (cs.select(1) > 0) {
                    Set<SelectionKey> set = cs.selectedKeys();
                    Iterator<SelectionKey> keyIterator = set.iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();

                        if (key.isReadable()) {
                            try {
                                SocketChannel clientChannel = (SocketChannel) key.channel();
                                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                                clientChannel.read(byteBuffer);
                                byteBuffer.flip();
                                System.out.println(Charset.defaultCharset().newDecoder().decode(byteBuffer).toString());
                            } finally {
                                keyIterator.remove();
                                key.interestOps(SelectionKey.OP_READ);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {

        }
    }
}

public class NIOServer {
    public static void main(String[] args) {
        try {
            Selector serverSelector = Selector.open();
            Selector clientSelector = Selector.open();

            Register register = new Register(serverSelector, clientSelector);
            Reader reader = new Reader(clientSelector);
            new Thread(register).start();
            new Thread(reader).start();
        } catch (IOException e) {
        }
    }
}
