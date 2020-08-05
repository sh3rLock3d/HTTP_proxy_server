package proxy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;

class HTTPRequest {
    int port = 80;
    String method = "GET";
    String path = "/";
    String version = "HTTP/1.0";

    HashMap<String, String> headers = new HashMap<>();

    int packetLen = 0;

    HTTPRequest (BufferedReader reader) throws Exception {
        String reqLine = reader.readLine();

        if (reqLine == null) {
            throw new Exception("Invalid request!");
        }

        String[] split = reqLine.split(" ");
        method = split[0];
        path = split[1];
        version = split[2];

        try {
            URL url = new URL(path);
            path = url.getPath();
            headers.put("Host", url.getHost());

        } catch (Exception e) {

        }
        try {
            String re = reqLine;
            while (!re.isEmpty()) {
                packetLen += re.length();
                re = reader.readLine();
            }
        } catch (Exception e){
            //System.out.println("here");
        }


        headers.put("Connection", "close");
    }



    Socket socket;
    DataOutputStream outputStream;
    BufferedReader reader;

    HTTPResponse getResponse() throws Exception {
        InetAddress address = InetAddress.getByName((headers.get("Host")));
        socket = new Socket(address, port);
        outputStream = new DataOutputStream(socket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        write(method + " " + path + " " + version);
        for(String key : headers.keySet()) {
            write(key + ": " + headers.get(key));
        }
        write("");
        HTTPResponse response = new HTTPResponse(reader);
        return response;
    }

    private void write (String line) throws Exception {
        outputStream.writeBytes(line + "\r\n");
        outputStream.flush();
    }

    public Integer length() {
        return packetLen;
    }
}