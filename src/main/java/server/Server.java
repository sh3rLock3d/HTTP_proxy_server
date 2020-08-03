package server;

import util.GZIP;
import util.HTTP_request;
import util.HTTP_response;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

public class Server extends Thread {
    private static final int PORT = 8080;
    private final Socket client;
    private boolean clientIsAlive = true;

    public Server(Socket client) {
        this.client = client;
    }

    private static String getRes(String path, boolean compress) throws IOException {
        File file = new File(path);
        byte[] data = null;
        String result = null;
        boolean isImage = path.endsWith("png") || path.endsWith("jpeg") || path.endsWith("jpg");
        try {
            if (isImage) {
                byte[] fileBytes = Files.readAllBytes(Paths.get(path));
                result = new String(fileBytes, StandardCharsets.ISO_8859_1);
            } else {
                data = new byte[(int) file.length()];
                FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.read(data);
                result = new String(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (compress) {
            if (isImage) {
                byte[] fileBytes = Files.readAllBytes(Paths.get(path));
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(fileBytes.length);
                GZIPOutputStream outputStream = new GZIPOutputStream(byteArrayOutputStream);
                outputStream.write(fileBytes);
                outputStream.flush();
                outputStream.close();
                result = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.ISO_8859_1);
            } else {
                result = new String(GZIP.compress(result), StandardCharsets.ISO_8859_1);
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        while (true) {
            System.out.println("waiting for new client...");
            Socket client = server.accept();
            Thread t = new Server(client);
            t.start();
        }
    }

    @Override
    public void run() {
        try {
            runServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runServer() throws IOException {
        while (clientIsAlive) {
            // 1. Read HTTP request from the client socket
            String HTTPRequest;
            try {
                HTTPRequest = readClientRequest();
            } catch (Exception e) {
                // time out
                break;
            }
            // 2. Prepare an HTTP response
            String HTTPResponse = handleClientRequest(HTTPRequest);
            // 3. Send HTTP response to the client
            client.getOutputStream().write(HTTPResponse.getBytes(StandardCharsets.ISO_8859_1));
            client.getOutputStream().flush();
            // log
            String dateLog = HTTP_response.getServerTime();
            String requestLog = HTTPRequest.split("\n")[0];
            String responseLog = HTTPResponse.split("\r\n")[0].substring(9);
            String log = "[" + dateLog + "] \"" + requestLog + "\" \"" + responseLog + "\"";
            System.out.println(log);
        }
        // 4. Close the socket
        client.close();
        System.out.println("socket closed");

    }

    private String readClientRequest() throws IOException {
        InputStreamReader isr = new InputStreamReader(client.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        String line = reader.readLine();
        String HTTPRequest = "";
        while (!line.isEmpty()) {
            HTTPRequest += line + '\n';
            line = reader.readLine();
        }
        return HTTPRequest;
    }

    private String handleClientRequest(String httpRequest) {
        HTTP_request request = null;
        try {
            request = new HTTP_request(httpRequest);
        } catch (Exception e) {
            clientIsAlive = false;
            return HTTP_response.badRequest(e.getMessage());
        }

        ArrayList<String> methodTypes = new ArrayList<>(Arrays.asList("GET", "POST", "HEAD", "PUT", "DELETE"));
        if (!methodTypes.contains(request.getMethod())) {
            clientIsAlive = false;
            return HTTP_response.notImplemented(request.getMethod());
        }

        if (!request.getMethod().equals("GET")) {
            clientIsAlive = false;
            return HTTP_response.methodNotAllowed();
        }

        String res = null;
        String url = null;
        boolean gzipComp = false;
        try {
            url = request.getURL();
            if (url.equals("/")) {
                url += "allFiles.html";
            }
            String Path = "src/main/java/res" + url;
            gzipComp = request.getAcceptEncoding() == HTTP_request.acceptEncodingStatus.gzip;
            res = getRes(Path, gzipComp);
        } catch (FileNotFoundException e) {
            clientIsAlive = false;
            return HTTP_response.fileNotFound();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String connectionState = "close";
        if (request.getConnection() == HTTP_request.connectionStatus.keep_alive) {
            clientIsAlive = true;
            connectionState = "keep-alive";
            try {
                client.setSoTimeout(request.getKeep_alive() * 1000);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        } else {
            clientIsAlive = false;
        }

        return HTTP_response.OK_response(res, HTTP_response.findType(url), connectionState, gzipComp);
    }
}