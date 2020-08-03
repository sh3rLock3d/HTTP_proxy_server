package proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {
    private static final int PORT_FORWARD = 8090, PORT_TELNET = 8091;

    public static void main(String[] args) {
        // forward
        new Thread(() -> {
            try {
                runProxyThread();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        // telnet
        new Thread(() -> {
            try {
                runTelnetThread();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void runTelnetThread() throws IOException {
        ServerSocket server = new ServerSocket(PORT_TELNET);
        while (true) {
            System.out.println("waiting for new telnet client...");
            Socket client = server.accept();
            TelnetThread telnetThread = new TelnetThread(client);
            telnetThread.start();
        }
    }

    private static void runProxyThread() throws IOException {
        ServerSocket server = new ServerSocket(PORT_FORWARD);
        while (true) {
            System.out.println("waiting for new proxy client...");
            Socket client = server.accept();
            ThreadProxy threadProxy = new ThreadProxy(client);
            threadProxy.start();
        }
    }
}
