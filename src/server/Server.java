package server;

import util.HTTP_request;
import util.HTTP_response;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;


public class Server {
    private static final int PORT = 8080;
    private boolean close = false;

    private static String getRes(String path) throws FileNotFoundException {
        File file=new File(path);
        FileInputStream fileInputStream=new FileInputStream(file);
        byte[] data=new byte[(int) file.length()];
        BufferedInputStream bufferedInputStream=new BufferedInputStream(fileInputStream);
        try {
            bufferedInputStream.read(data,0,data.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(data);
    }

    public static void main(String [] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        while (true){
            Socket client = server.accept();
            // 1. Read HTTP request from the client socket
            InputStreamReader isr = new InputStreamReader(client.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            String line = reader.readLine();
            String HTTPRequest = line;
            while (!line.isEmpty()){
                HTTPRequest += line + '\n';
                line = reader.readLine();
            }

            // 2. Prepare an HTTP response
            String HTTPResponse = handleClientRequest(HTTPRequest);
            // 3. Send HTTP response to the client
            client.getOutputStream().write(HTTPResponse.getBytes("UTF-8"));
            // 4. Close the socket
            client.close();
        }
    }

    private static String handleClientRequest(String httpRequest) {
        HTTP_request request = null;
        try {
            request = new HTTP_request(httpRequest);
        } catch (Exception e) {
            return HTTP_response.badRequest(e.getMessage());
        }

        ArrayList<String> methodTypes = new ArrayList<>(Arrays.asList("GET", "POST", "HEAD", "PUT", "DELETE"));
        if (!methodTypes.contains(request.getMethod())){
            return HTTP_response.notImplemented(request.getMethod());
        }

        if (!request.getMethod().equals("GET")){
            return HTTP_response.methodNotAllowed();
        }

        String res = null;
        String url = null;
        try {
            url = request.getURL();
            if (url.equals("/")){
                url += "allFiles.html";
            }
            String Path = "src/res" + url;
            res = getRes(Path);

        } catch (FileNotFoundException e) {
            return HTTP_response.fileNotFound();
        }

        return HTTP_response.OK_response(res, HTTP_response.findType(url));
    }

}

