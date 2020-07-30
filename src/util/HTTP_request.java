package util;

import java.util.Arrays;

public class HTTP_request {
    private methodStatus method;
    enum methodStatus{GET, POST}
    private String URL;
    private String HTTP_version;
    private connectionStatus connection = connectionStatus.close;
    enum connectionStatus{close, keep_alive}
    private int keep_alive = 60;
    private acceptEncodingStatus acceptEncoding = acceptEncodingStatus.no_encode;
    enum acceptEncodingStatus{gzip, no_encode}
    String data;

    public HTTP_request(methodStatus method, String URL, String HTTP_version, connectionStatus connection,
                        int keep_alive, acceptEncodingStatus acceptEncoding, String data) {
        this.method = method;
        this.URL = URL;
        this.HTTP_version = HTTP_version;
        this.connection = connection;
        this.keep_alive = keep_alive;
        this.acceptEncoding = acceptEncoding;
        this.data = data;
    }
    public HTTP_request(String request){
        String[] request_split = request.split("\n");
        int header_len = request_split.length - 1;
        for (int i = 0; i < request_split.length; i++) {
            if (request_split[i].length() == 0) {
                header_len = i;
                break;
            }
        }

        data = "";
        for(int i = header_len + 1; i < request_split.length; i++){
            data += request_split[i] + "\n";
        }
        data = data.substring(0, data.length() - 1);

        String[] firstLine = request_split[0].split(" ");

        if(firstLine[0].equals("GET")) {
            method = methodStatus.GET;
        } else if (firstLine[0].equals("POST")) {
            method = methodStatus.POST;
        } else {
            //error
        }
        URL = firstLine[1];
        HTTP_version = firstLine[2];
        // if len first line != 3 error
        for (int i = 1; i < header_len; i++){
            String command = request_split[i];
            if (command.startsWith("Connection: ")){
                if (command.endsWith("close")){
                    connection = connectionStatus.close;
                } else if (command.endsWith("keep_alive")) {
                    connection = connectionStatus.keep_alive;
                } else {
                    //error
                }
            } else if (command.startsWith("Accept-Encoding: ")) {
                if (command.endsWith("gzip")) {
                    acceptEncoding = acceptEncodingStatus.gzip;
                } else {
                    //error
                }
            } else if (command.startsWith("Keep-Alive: ")){
                keep_alive = Integer.parseInt(command.substring(command.indexOf(' ') + 1));
            } else {
                //error
                System.out.println(command);
            }
        }

    }

    @Override
    public String toString() {
        String result =
                method + " " + URL + " " + HTTP_version + '\n' +
                "Connection: " + connection + "\n";
        if (acceptEncoding == acceptEncodingStatus.gzip) {
            result += "Accept-Encoding: " + acceptEncoding + '\n';
        }
        if (connection == connectionStatus.keep_alive) {
            result += "Keep-Alive: " + keep_alive + "\n";
        }
        result += '\n';
        result += data;
        return result;


    }
}
