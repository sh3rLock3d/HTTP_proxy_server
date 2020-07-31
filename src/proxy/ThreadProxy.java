package proxy;

import util.HTTP_response;

import java.io.*;
import java.net.Socket;
import java.util.Formatter;

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
            } catch (Exception e) {
                // client is closed
                break;
            }
            clientRequest += '\n';
            // initialize server
            if (server == null) {
                findHost(clientRequest);
                server = new Socket(SERVER_URL, SERVER_PORT);
                writeToServer = new Formatter(server.getOutputStream());
                readerFromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
            }
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
            //send request to server
            writeToServer.format(clientRequest).flush();
            //receive server response
            String serverResponse = "";
            try {
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

            } catch (Exception e) {
                // server is close
                break;
            }
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
            // send response to client
            writeToClient.format(serverResponse).flush();
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
}

