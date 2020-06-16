package xyz.ariesfish.nio;

import io.netty.util.concurrent.EventExecutor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static xyz.ariesfish.nio.BasicHandler.MAX_IN;
import static xyz.ariesfish.nio.BasicHandler.MAX_OUT;

public class MultithreadedHandler implements Runnable {
    ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    SelectionKey sk;
    SocketChannel socket;
    ByteBuffer input = ByteBuffer.allocate(MAX_IN);
    ByteBuffer output = ByteBuffer.allocate(MAX_OUT);
    final static int READING = 0, SENDING = 1, PROCESSING = 2;
    int state = READING;

    public MultithreadedHandler(Selector selector, SocketChannel socketChannel) throws IOException {
        socket = socketChannel;
        sk = socket.register(selector, SelectionKey.OP_READ);
        socket.configureBlocking(false);
        sk.attach(this);
        selector.wakeup();
    }

    @Override
    public void run() {
        try {
            if (state == READING) {
                read();
            } else if (state == SENDING) {
                send();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void read() throws IOException {
        socket.read(input);
        if (inputIsComplete()) {
            // 多线程处理process
            state = PROCESSING;
            // 使用lambda
            pool.execute(this::processAndHandOff);
        }
    }

    private void send() throws IOException {
        socket.write(output);
        if (outputIsComplete()) {
            sk.cancel();
        }
    }

    synchronized void processAndHandOff() {
        process();
        state = SENDING;
        sk.interestOps(SelectionKey.OP_WRITE);
    }

    private boolean inputIsComplete() { return true; }
    private boolean outputIsComplete() { return true; }

    private void process() {

    }
}
