package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;

public class Client {
    private static final int PORT = 8080;

    public static void main(String[] args) {

        try {

            String params = URLEncoder.encode("param1", "UTF-8")
                    + "=" + URLEncoder.encode("value1", "UTF-8");
            params += "&" + URLEncoder.encode("param2", "UTF-8")
                    + "=" + URLEncoder.encode("value2", "UTF-8");

            String hostname = "127.0.0.1";

            InetAddress addr = InetAddress.getByName(hostname);
            Socket socket = new Socket(addr, PORT);
            String path = "/myapp";

            // Send headers
            BufferedWriter wr =
                    new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
            wr.write("POST "+path+" HTTP/1.0\r\n");
            wr.write("Content-Length: "+params.length()+"\r\n");
            wr.write("Content-Type: application/x-www-form-urlencodedrn");
            wr.write("\r\n");

            // Send parameters
            wr.write(params);
            wr.flush();

            // Get response
            BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;

            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }

            wr.close();
            rd.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
