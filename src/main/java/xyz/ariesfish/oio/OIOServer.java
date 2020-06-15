package xyz.ariesfish.oio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

class Receiver implements Runnable {
    private ServerSocket ss;

    Receiver() throws Exception{
        this.ss = new ServerSocket(8000);
    }

    @Override
    public void run() {
        while(true) {
            try {
                Socket socket = ss.accept();

                new Thread(() -> {
                    try {
                        byte[] data = new byte[1024];
                        InputStream inputStream = socket.getInputStream();
                        while (true) {
                            int len = inputStream.read(data);
                            while (len != -1) {
                                System.out.println(new String(data, 0, len));
                                len = inputStream.read(data);
                            }
                        }
                    } catch (IOException e) {

                    }
                }).start();
            } catch (IOException e) {

            }
        }
    }
}

public class OIOServer {
    public static void main(String[] args) {
        try {
            Receiver receiver = new Receiver();
            new Thread(receiver).start();
        } catch (Exception e) {

        }
    }
}
