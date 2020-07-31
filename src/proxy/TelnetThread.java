package proxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TelnetThread extends Thread{
    Socket client;
    public TelnetThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {

    }
}
