package xyz.ariesfish.oio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClassicServer implements Runnable {
    private int port;
    private ServerSocket socket;

    public ClassicServer(int port) {
        this.port = port;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Failed to create server socket.");
        }
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                // 阻塞，直到收到客户端连接请求
                Socket client = socket.accept();
                // 为该客户端的连接单独开一个线程处理，也可以使用线程池
                new Thread().start();
            } catch (IOException e) {
                System.err.println("Failed to accept client socket.");
            }
        }
    }
}
