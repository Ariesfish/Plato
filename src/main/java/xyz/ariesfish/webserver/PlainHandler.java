package xyz.ariesfish.webserver;

import java.io.*;
import java.net.Socket;

public class PlainHandler implements Runnable {
    private final Socket clientSocket;
    private final String content = "HTTP/1.1 200 OK\r\n"
                                    + "\r\n"
                                    + "Hello World! This is Plain Server 2.";

    public PlainHandler(Socket client) {
        this.clientSocket = client;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            char[] buffer = new char[1024];
            br.read(buffer);
            System.out.println(new String(buffer));

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            bw.write(content);
            bw.flush();
            clientSocket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
