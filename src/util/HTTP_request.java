package util;

import java.util.Arrays;

public class HTTP_request {
    private String method;

    public String getURL() {
        return URL;
    }

    //enum methodStatus{GET, POST, HEAD, PUT, DELETE}
    private String URL;
    private String HTTP_version;
    private connectionStatus connection = connectionStatus.close;
    enum connectionStatus{close, keep_alive}
    private int keep_alive = 60;

    public String getMethod() {
        return method;
    }

    public connectionStatus getConnection() {
        return connection;
    }

    public int getKeep_alive() {
        return keep_alive;
    }

    public acceptEncodingStatus getAcceptEncoding() {
        return acceptEncoding;
    }

    private acceptEncodingStatus acceptEncoding = acceptEncodingStatus.no_encode;
    enum acceptEncodingStatus{gzip, no_encode}

    public HTTP_request(String method, String URL, String HTTP_version, connectionStatus connection,
                        int keep_alive, acceptEncodingStatus acceptEncoding, String data) {
        this.method = method;
        this.URL = URL;
        this.HTTP_version = HTTP_version;
        this.connection = connection;
        this.keep_alive = keep_alive;
        this.acceptEncoding = acceptEncoding;
    }
    public HTTP_request(String request) throws Exception {
        String[] request_split = request.split("\n");
        int header_len = request_split.length;
        //start
        String[] firstLine = request_split[0].split(" ");
        if (firstLine.length != 3) {
            firstLine[2] = firstLine[2].substring(0, firstLine[2].indexOf("GET")); // todo should i throw exception?
        }
        method = firstLine[0];
        URL = firstLine[1];
        HTTP_version = firstLine[2];
        //header
        for (int i = 1; i < header_len; i++){
            String command = request_split[i];
            if (command.startsWith("Connection: ")){
                if (command.endsWith("close")){
                    connection = connectionStatus.close;
                } else if (command.endsWith("keep-alive")) {
                    connection = connectionStatus.keep_alive;
                } else {
                    throw new Exception("-> error: " + command);
                }
            } else if (command.startsWith("Accept-Encoding: ")) {
                if (command.contains("gzip")) {
                    acceptEncoding = acceptEncodingStatus.gzip;
                } else {
                    throw new Exception("-> error: " + command);
                }
            } else if (command.startsWith("Keep-Alive: ")){
                keep_alive = Integer.parseInt(command.substring(command.indexOf(' ') + 1));
            } else {
                // not supported
            }
        }

    }

    @Override
    public String toString() {
        String result = method + " " + URL + " " + HTTP_version + "\n";

        if (connection == connectionStatus.close){
            result += "Connection: " + connection + "\n";
        } else {
            result += "Connection: keep-alive" + "\n";
            result += "Keep-Alive: " + keep_alive + "\n";
        }

        if (acceptEncoding == acceptEncodingStatus.gzip) {
            result += "Accept-Encoding: " + acceptEncoding + "\n";
        }

        result = result.substring(0, result.length() - 1);

        return result;
    }
}
