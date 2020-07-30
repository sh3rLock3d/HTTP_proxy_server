package util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class HTTP_response {
    public static String OK_response(String resp, String type){
        String start = "HTTP/1.0 200 OK\r\n";
        String header = "Connection: close\r\n";
        header += "Date: "+ getServerTime() +"\r\n";
        header+= "Content-Type: "+ type +"\r\n";
        header+= "Content-length: "+resp.length()+"\r\n";
        header+="\r\n";
        return start+header+resp;
    }

    public static String findType(String path) {
        if (path.endsWith("txt")) {
            return "text/plain";
        } else if (path.endsWith("html")){
            return "text/html";
        } else if (path.endsWith("png")){
            return "image/png";
        } else if (path.endsWith("jpeg")){
            return "image/jpeg";
        } else if (path.endsWith("jpg")){
            return "image/jpg";
        }
        return null;
    }

    public static String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    public static String badRequest(String message) {
        String start = "HTTP/1.0 400 Bad Request\r\n";
        String header = "Connection: close\r\n";
        header+= "Content-length: "+message.length()+"\r\n";
        header+= "Content-Type: text/html\r\n";
        header += "Date: "+ getServerTime() +"\r\n";
        header+="\r\n";
        return start+header+message;
    }

    public static String notImplemented(String method) {
        method = "method " + method + " is not implemented";

        String start = "HTTP/1.0 501 Not Implemented\r\n";
        String header = "Connection: close\r\n";
        header+= "Content-length: "+method.length()+"\r\n";
        header+= "Content-Type: text/html\r\n";
        header += "Date: "+ getServerTime() +"\r\n";
        header+="\r\n";
        return start+header+method;
    }

    public static String methodNotAllowed() {
        String message = "only method Get is Allowed";

        String start = "HTTP/1.0 405 Method Not Allowed\r\n";
        String header = "Connection: close\r\n";
        header+= "Content-length: "+message.length()+"\r\n";
        header+= "Content-Type: text/html\r\n";
        header+= "Allow: GET\r\n";
        header += "Date: "+ getServerTime() +"\r\n";
        header+="\r\n";
        return start+header+message;
    }

    public static String fileNotFound() {
        String message = "file not found";

        String start = "HTTP/1.0 404 Not Found\r\n";
        String header = "Connection: close\r\n";
        header+= "Content-length: "+message.length()+"\r\n";
        header+= "Content-Type: text/html\r\n";
        header += "Date: "+ getServerTime() +"\r\n";
        header+="\r\n";
        return start+header+message;
    }
}
