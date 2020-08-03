package client;

import java.io.IOException;
import java.net.Socket;
import java.util.Formatter;
import java.util.Scanner;

public class Client {
    private static final int PORT = 8080;
    static Scanner scanner;

    public static void main(String[] args) throws IOException {
        scanner = new Scanner(System.in);
        Formatter formatter = null;
        Scanner scanner1 = null;
        Socket socket = null;
        while (true) {
            String l = scanner.nextLine();
            if (l.equals("send r 1")) {
                request(req1, scanner1, formatter);
            } else if (l.equals("send r 2")) {
                request(req2, scanner1, formatter);
            } else if (l.equals("start")) {
                socket = new Socket("127.0.0.1", 8080);
                scanner1 = new Scanner(socket.getInputStream());
                formatter = new Formatter(socket.getOutputStream());
            } else if (l.equals("end")) {
                socket.close();
            } else if (l.equals("proxy")) {
                proxy();
            } else if (l.equals("send r 3")) {
                request(req3, scanner1, formatter);
            }
        }
    }

    private static void proxy() throws IOException {
        Formatter formatter = null;
        Scanner scanner1 = null;
        Socket proxy = null;
        while (true) {
            String l = scanner.nextLine();
            if (l.equals("start")) {
                proxy = new Socket("127.0.0.1", 8090);
                scanner1 = new Scanner(proxy.getInputStream());
                formatter = new Formatter(proxy.getOutputStream());
            }  else if (l.equals("send r 1")) {
                request(reqp1, scanner1, formatter);
            } else if (l.equals("send r 2")) {
                request(reqp2, scanner1, formatter);
            } else if (l.equals("end")) {
                proxy.close();
            } else if (l.equals("send r 3")) {
                request(reqp3, scanner1, formatter);
            }
        }
    }

    public static void request(String req, Scanner scanner, Formatter formatter){
        formatter.format(req);
        formatter.flush();
        formatter.format("\n");
        formatter.flush();
        String line = scanner.nextLine();
        while (!line.isEmpty()){
            System.out.println(line);
            line = scanner.nextLine();
        }
        line = scanner.nextLine();
        while (!line.isEmpty()){
            System.out.println(line);
            line = scanner.nextLine();
        }
        System.out.println("..........");
    }

    static String req1 = "GET / HTTP/1.1\n" +
            "Connection: keep-alive\n" +
            "Keep-Alive: 10\n" +
            "Accept-Encoding: gzip\n";

    static String req2 = "GET /first.html HTTP/1.1\n" +
            "Connection: keep-alive\n" +
            "Keep-Alive: 60\n" +
            "Accept-Encoding: gzip\n";

    static String req3 = "GET / HTTP/1.1\n" +
            "Connection: close\n" +
            "Keep-Alive: 10\n" +
            "Accept-Encoding: gzip\n";

    static String reqp1 = "GET http://127.0.0.1:8080/ HTTP/1.1\n" +
            "Connection: keep-alive\n" +
            "Keep-Alive: 10\n" +
            "Accept-Encoding: gzip\n";

    static String reqp2 = "GET http://127.0.0.1:8080/first.html HTTP/1.1\n" +
            "Connection: keep-alive\n" +
            "Keep-Alive: 60\n" +
            "Accept-Encoding: gzip\n";

    static String reqp3 = "GET http://127.0.0.1:8080/ HTTP/1.1\n" +
            "Connection: close\n" +
            "Keep-Alive: 10\n" +
            "Accept-Encoding: gzip\n";

}
