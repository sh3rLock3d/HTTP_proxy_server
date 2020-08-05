package proxy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.util.ArrayList;


public class HTTPResponse {
    ArrayList<String> lines = new ArrayList<>();


    HTTPResponse(BufferedReader reader) throws Exception {
        String line;

        while((line = reader.readLine()) != null) {
            lines.add(line);
        }

    }

    void send (DataOutputStream stream) throws Exception {
        for (String line : lines) {
            stream.writeBytes(line + "\r\n");
            stream.flush();
        }
        stream.writeBytes("\r\n");
        stream.flush();
    }

    public Integer packrtLen() {
        int sum = 0;
        for (String s:lines){
            sum += s.length() + 1;
        }
        return sum;
    }

    public Integer bodyLen() {
        int sum_header = 0;
        for (String str:lines) {
            sum_header += str.length() + 1;
            if (str.equals("") || str.equals("\n") || str.equals("\r\n")) {
                break;
            }
        }
        return packrtLen() - sum_header;
    }
}
