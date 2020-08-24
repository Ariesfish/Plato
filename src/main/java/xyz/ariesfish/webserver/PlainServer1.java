package xyz.ariesfish.webserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class PlainServer1 {
    private final ServerSocket socket;

    public PlainServer1(int port) throws IOException {
        this.socket = new ServerSocket(port);
    }

    public void serve() {
        try {
            while (true) {
                Socket client = this.socket.accept();
                System.out.println("Host is " + this.socket);
                System.out.println("Connect from " + client);
                new Thread(() -> {
                    String content = "HTTP/1.1 200 OK\r\n"
                            + "\r\n"
                            + "Hello World! This is Plain Server 1.";
                    try {
                        Thread.sleep(1000);
                        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        char[] buffer = new char[1024];
                        br.read(buffer);
                        System.out.println(new String(buffer));

                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                        bw.write(content);
                        bw.flush();
                        client.close();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            PlainServer1 server = new PlainServer1(8888);
            server.serve();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
