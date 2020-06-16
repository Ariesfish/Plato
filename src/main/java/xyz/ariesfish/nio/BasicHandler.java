package xyz.ariesfish.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class BasicHandler implements Runnable {
    final static int MAX_IN = 1024;
    final static int MAX_OUT = 4096;
    final static int READING = 0, SENDING = 1;

    final SocketChannel socket;
    final SelectionKey key;

    ByteBuffer input = ByteBuffer.allocate(MAX_IN);
    ByteBuffer output = ByteBuffer.allocate(MAX_OUT);
    int state = READING;

    BasicHandler(Selector selector, SocketChannel socketChannel) throws IOException {
        socket = socketChannel;
        // 注册socket和selector
        key = socket.register(selector, SelectionKey.OP_READ);
        socket.configureBlocking(false);
        // 绑定本处理单元对象
        key.attach(this);
        selector.wakeup();
    }

    public void run() {
        try {
            if (state == READING)
                read();
            else if (state == SENDING)
                send();
        } catch (IOException e) {

        }
    }

    synchronized private void read() throws IOException {
        socket.read(input);
        if (inputIsComplete()) {
            process();
            state = SENDING;
            key.interestOps(SelectionKey.OP_WRITE);

        }
    }

    private void send() throws IOException {
        socket.write(output);
        if (outputIsComplete()) {
            key.cancel();
        }
    }

    private boolean inputIsComplete() { return true; }
    private boolean outputIsComplete() { return true; }

    private void process() {

    }
}
