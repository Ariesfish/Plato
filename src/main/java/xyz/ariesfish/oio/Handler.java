package xyz.ariesfish.oio;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;

public class Handler implements Runnable {
    final static int MAX_INPUT = 1024;
    private Socket socket;

    public Handler(Socket s) { socket = s; }

    @Override
    public void run() {
        try {
            byte[] input = new byte[MAX_INPUT];
            socket.getInputStream().read(input);
            byte[] output = process(input);
            socket.getOutputStream().write(output);
            socket.getOutputStream().flush();
        } catch (IOException e) {

        }
    }

    private byte[] process(byte[] cmd) {
        return cmd.toString().toUpperCase().getBytes(Charset.forName("UTF-8"));
    }
}
