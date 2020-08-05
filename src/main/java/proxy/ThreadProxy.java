package proxy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;


public class ThreadProxy extends Thread {
    Socket client;
    String SERVER_URL;

    ThreadProxy(Socket client) {
        super();
        this.client = client;
    }

    @Override
    public void run() {
        try {
            BufferedReader readFromClient = new BufferedReader(new InputStreamReader((client.getInputStream())));
            DataOutputStream writeToClient = new DataOutputStream(client.getOutputStream());
            HTTPRequest request;
            try {
                request = new HTTPRequest(readFromClient);
            } catch (Exception e) {
                System.out.println("client closed connection");
                client.close();
                return;
            }

            if (!"GET".equals(request.method)) {
                client.close();
                return;
            }
            // log
            System.out.println(
                    "Request: [" +
                            getServerTime() +
                            "] [" +
                            client.getRemoteSocketAddress().toString().replace("/","") +
                            "] [" +
                            request.headers.get("Host") + ":80" +
                            "]\n\t\t \"" +
                            request.method + ' ' + request.path + ' ' + request.version +
                            "\""
            );
            SERVER_URL = request.headers.get("Host");
            //
            HTTPResponse response = request.getResponse();
            // log
            System.out.println(
                    "Response: [" +
                            getServerTime() +
                            "] [" +
                            client.getRemoteSocketAddress().toString().replace("/","") +
                            "] [" +
                            request.headers.get("Host") + ":80" +
                            "]\n\t\t \"" +
                            response.lines.get(0)+
                            "\" for \""+
                            request.method + ' ' + request.path + ' ' + request.version +
                            "\""
            );

            try {
                response.send(writeToClient);
            } catch (IOException e) {
                client.close();
                System.out.println("server closed connection");
            }
            // update telnetInfo
            updateTelnetInfo(request, response);
            readFromClient.close();
            writeToClient.close();

        } catch (Exception e) {
            System.err.println("* " + e);
            e.printStackTrace();
        }

        try {
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    //telnet info
    public static HashMap<String, Integer> statusCount = new HashMap<String, Integer>(){
        {
            put("200 OK", 0);
            put("301 Moved Permanently", 0);
            put("304 Not Modified", 0);
            put("400 Bad Request", 0);
            put("404 Not Found", 0);
            put("405 Method Not Allowed", 0);
            put("501 Not Implemented", 0);
        }
    };
    public static HashMap<String, Integer> typeCount = new HashMap<String, Integer>(){
        {
            put("text/plain", 0);
            put("text/html", 0);
            put("image/png", 0);
            put("image/jpeg", 0);
            put("image/jpg", 0);
        }
    };
    public static HashMap<String, Integer> visitedHost = new HashMap<>();
    public static ArrayList<Integer> packet_length_server = new ArrayList<>();
    public static ArrayList<Integer> packet_length_client = new ArrayList<>();
    public static ArrayList<Integer> body_length_server = new ArrayList<>();
    private void updateTelnetInfo(HTTPRequest clientRequest, HTTPResponse serverResponse) {
        //status count
        String statusCountStr = serverResponse.lines.get(0);
        statusCountStr = statusCountStr.substring(statusCountStr.indexOf(" ") + 1);
        statusCount.put(statusCountStr, statusCount.getOrDefault(statusCountStr, 0) + 1);
        //type count
        for (String str:serverResponse.lines){
            if (str.startsWith("Content-Type")) {
                for (String type: typeCount.keySet()){
                    if (str.contains(type)){
                        typeCount.put(type, typeCount.get(type)+1);
                        break;
                    }
                }
                break;
            }
        }

        // visited host
        visitedHost.put(SERVER_URL, visitedHost.getOrDefault(SERVER_URL, 0) + 1);
        // packet length
        int serverLen = 0;
        packet_length_server.add(serverResponse.packrtLen());
        packet_length_client.add(clientRequest.length());
        body_length_server.add(serverResponse.bodyLen());
    }
}
