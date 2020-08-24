package xyz.ariesfish.webserver;

import xyz.ariesfish.oio.Handler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlainServer2 {
    private final ServerSocket socket;
    private final ExecutorService service;

    public PlainServer2(int port) throws IOException {
        this.socket = new ServerSocket(port);
        this.service = Executors.newCachedThreadPool();
    }

    public void serve() {
        try {
            while (true) {
                Socket client = this.socket.accept();
                System.out.println("Host is " + this.socket);
                System.out.println("Connect from " + client);
                service.submit(new PlainHandler(client));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            PlainServer2 server = new PlainServer2(8888);
            server.serve();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
