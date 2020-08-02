package proxy;

import util.HTTP_response;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

public class ThreadProxy extends Thread {
    private Socket client;
    private Socket server;
    private String SERVER_URL;
    private int SERVER_PORT;
    public ThreadProxy(Socket client) {
        this.client = client;
    }
    @Override
    public void run() {
        try {
            runThreadProxy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runThreadProxy() throws IOException {
        server = null;
        Formatter writeToServer = null;
        BufferedReader readerFromServer = null;
        BufferedReader readerFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
        Formatter writeToClient = new Formatter(client.getOutputStream());

        while (true) {
            //read client request
            String clientRequest = "";
            try {
                String line = readerFromClient.readLine();
                while (!line.isEmpty()) {
                    clientRequest += line + '\n';
                    line = readerFromClient.readLine();
                }
                clientRequest += '\n';
                if (clientRequest.contains("Content-Length:")) {
                    line = readerFromClient.readLine();
                    while (!line.isEmpty()) {
                        clientRequest += line + '\n';
                        line = readerFromClient.readLine();
                    }
                    clientRequest += '\n';
                }
            } catch (Exception e) {
                // client is closed
                break;
            }
            clientRequest = setHeaderOfClientRequest(clientRequest);
            System.out.println(clientRequest);
            // initialize server
            if (server == null) {
                findHost(clientRequest);
                server = new Socket(SERVER_URL, SERVER_PORT);
                writeToServer = new Formatter(server.getOutputStream());
                readerFromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
            }
            /*
            // log
            System.out.println(
                    "Request: [" +
                            HTTP_response.getServerTime() +
                            "] [" +
                            client.getRemoteSocketAddress().toString().replace("/","") +
                            "] [" +
                            server.getRemoteSocketAddress().toString().replace("/","") +
                            "]\n\t\t \"" +
                            clientRequest.split("\n")[0]+
                            "\""
            );

             */
            //send request to server
            writeToServer.format(clientRequest).flush();
            //receive server response
            if (server.isClosed()) {
                System.out.println("server is close");
                break;
            }
            String serverResponse = "";
            String line = readerFromServer.readLine();
            while (!line.isEmpty()) {
                serverResponse += line + '\n';
                line = readerFromServer.readLine();
            }
            serverResponse += '\n';
            line = readerFromServer.readLine();
            if (serverResponse.split("\n")[0].contains("OK")) {
                while (!line.isEmpty()) {
                    serverResponse += line + '\n';
                    line = readerFromServer.readLine();
                }
                serverResponse += '\n';
            }
            System.out.println(serverResponse);
            /*
            //log
            System.out.println(
                    "Response: [" +
                            HTTP_response.getServerTime() +
                            "] [" +
                            client.getRemoteSocketAddress().toString().replace("/","") +
                            "] [" +
                            server.getRemoteSocketAddress().toString().replace("/","") +
                            "]\n\t\t \"" +
                            serverResponse.split("\n")[0]+
                            "\" for \""+
                            clientRequest.split("\n")[0]+
                            "\""
            );

             */
            // send response to client
            writeToClient.format(serverResponse).flush();
            // information for telnet
            updateTelnetInfo(clientRequest, serverResponse);
            //check if close
            if (checkIfClose(clientRequest, serverResponse)){
                break;
            }
        }
        System.out.println("close connection");
        server.close();
        client.close();
        readerFromClient.close();
        writeToServer.close();
        readerFromServer.close();
        writeToClient.close();

    }

    private String setHeaderOfClientRequest(String clientRequest) {
        int l = clientRequest.indexOf("\n");
        String firstLine = clientRequest.substring(0, l);
        String[] f = firstLine.split(" ");
        String get = f[1];
        get = get.substring(get.indexOf("://") + 3);
        get = get.substring(get.indexOf("/"));
        String body = clientRequest.substring(l);
        return f[0] + " " + get + " " + f[2] + body;
    }

    private void findHost(String request) {
        for (String s : request.split("\n")){
            if (s.startsWith("Host: ")){
                SERVER_URL = s.substring(s.indexOf(' ') + 1);
                SERVER_PORT = 80;
                return;
            }
        }
        SERVER_PORT = 8080;
        SERVER_URL = "127.0.0.1";
    }

    private boolean checkIfClose(String clientRequest, String serverResponse) {
        if (serverResponse.contains("Connection: close") || clientRequest.contains("Connection: close")) {
            return true;
        }
        return false;
    }

    // telnet information
    public static HashMap<String, Integer> statusCount = new HashMap<>(){
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
    public static HashMap<String, Integer> typeCount = new HashMap<>(){
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
    private void updateTelnetInfo(String clientRequest, String serverResponse) {
        //status count
        for (String status:statusCount.keySet()){
            if (serverResponse.split("\n")[0].contains(status)) {
                statusCount.put(status, statusCount.get(status) + 1);
                break;
            }
        }
        //type count
        for (String type: typeCount.keySet()){
            if (serverResponse.contains(type)){
                typeCount.put(type, typeCount.get(type)+1);
                break;
            }
        }
        // visited host
        visitedHost.put(SERVER_URL, visitedHost.getOrDefault(SERVER_URL, 0) + 1);
        // packet length
        packet_length_server.add(serverResponse.length());
        packet_length_client.add(clientRequest.length());
        if (serverResponse.split("\n")[0].contains("OK")) {
            int startOfBody = serverResponse.indexOf("\n\n") + 1;
            body_length_server.add(serverResponse.substring(startOfBody).length());
        } else {
            body_length_server.add(0);
        }
    }
}

