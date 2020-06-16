package xyz.ariesfish;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;

class Sender implements Runnable {
    @Override
    public void run() {
        try {
            Socket socket = new Socket("127.0.0.1", 8000);
            while (true) {
                try {
                    socket.getOutputStream().write((new Date() + ": hello world").getBytes());
                    socket.getOutputStream().flush();
                    Thread.sleep(2000);
                } catch (Exception e) {

                }
            }
        } catch (IOException e) {

        }
    }
}

public class Client {
    public static void main(String[] args) {
        new Thread(new Sender()).start();
    }
}
