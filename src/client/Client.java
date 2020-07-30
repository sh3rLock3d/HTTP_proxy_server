package client;

import java.io.*;
import java.net.Socket;
import java.util.Formatter;
import java.util.Scanner;

public class Client {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Formatter formatter = null;
        Scanner scanner1 = null;
        Socket socket = null;
        while (true){
            String l = scanner.nextLine();
            if (l.equals("send r 1")) {
                request1(scanner1, formatter);
            } else if (l.equals("send r 2")) {
                request2(scanner1, formatter);
            } else if (l.equals("start")) {
                socket = new Socket("127.0.0.1", 8080);
                scanner1 = new Scanner(socket.getInputStream());
                formatter = new Formatter(socket.getOutputStream());
            } else if (l.equals("end")) {
                socket.close();
            }
        }
    }

    public static void request1(Scanner scanner, Formatter formatter){

        String req = "GET / HTTP/1.1\n" +
                "Connection: keep-alive\n" +
                "Keep-Alive: 60\n" +
                "Accept-Encoding: gzip\n";

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

    public static void request2(Scanner scanner, Formatter formatter){
        String req = "GET /first.html HTTP/1.1\n" +
                "Connection: keep-alive\n" +
                "Keep-Alive: 60\n" +
                "Accept-Encoding: gzip\n";
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
}
