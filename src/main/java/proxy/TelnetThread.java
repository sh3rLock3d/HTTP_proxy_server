package proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class TelnetThread extends Thread{
    Socket client;
    public TelnetThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            runTelnetThread();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runTelnetThread() throws IOException {
        Scanner scanner = new Scanner(client.getInputStream());
        Formatter formatter = new Formatter(client.getOutputStream());
        while (true) {
            String command = scanner.nextLine();
            System.out.println(command);
            if (command.equals("packet length stats")) {
                String response = "packet length received from server(mean, std): (" + getMean(ThreadProxy.packet_length_server) +", " + getStd(ThreadProxy.packet_length_server) + ")\n";
                response += "packet length received from client(mean, std): (" + getMean(ThreadProxy.packet_length_client) +", " + getStd(ThreadProxy.packet_length_client) + ")\n";
                response += "packet length received from server(mean, std): (" + getMean(ThreadProxy.body_length_server) +", " + getStd(ThreadProxy.body_length_server) + ")\n";
                formatter.format(response).flush();
            } else if (command.equals("type count")){
                String response = "";
                for(String type: ThreadProxy.typeCount.keySet()){
                    int count = ThreadProxy.typeCount.get(type);
                    response += type + ": " + count + "\n";
                }
                formatter.format(response).flush();
            } else if(command.equals("status count")){
                String response = "";
                for(String type: ThreadProxy.statusCount.keySet()){
                    int count = ThreadProxy.statusCount.get(type);
                    response += type + ": " + count + "\n";
                }
                formatter.format(response).flush();
            } else if(command.startsWith("top ") && command.endsWith(" visited hosts")){
                int k = Integer.parseInt(command.substring(command.indexOf(" ") + 1, command.lastIndexOf("v") - 1));
                k = Math.max(k, ThreadProxy.visitedHost.size());
                ArrayList<String> keys = new ArrayList<>();
                HashMap<String, Integer> hashMap = ThreadProxy.visitedHost;
                keys.addAll(hashMap.keySet());
                Collections.sort(keys, (t1, t2) -> hashMap.get(t1) - hashMap.get(t2));
                List<String> list = keys.subList(keys.size() - k, keys.size());
                Collections.reverse(list);
                String response = "";
                for (int i = 1; i <= k; i++) {
                    response += i + ". " + list.get(i) + "\n";
                }
                formatter.format(response).flush();
            } else if (command.equals("exit")){
                String response = "bye\n";
                formatter.format(response).flush();
                client.close();
                scanner.close();
                formatter.close();
                System.out.println("telnet connection close");
                break;
            } else {
                formatter.format("not supported\n").flush();
            }
        }
    }

    public static double getStd (ArrayList<Integer> table)
    {
        if (table.size() == 0) {
            return 0;
        }
        double mean = getMean(table);
        double temp = 0;

        for (int i = 0; i < table.size(); i++)
        {
            int val = table.get(i);
            double squrDiffToMean = Math.pow(val - mean, 2);
            temp += squrDiffToMean;
        }
        double meanOfDiffs = temp / (double) (table.size());
        return Math.sqrt(meanOfDiffs);
    }

    private static double getMean(ArrayList<Integer> numbers) {
        Integer sum = 0;
        if(!numbers.isEmpty()) {
            for (Integer number : numbers) {
                sum += number;
            }
            return sum.doubleValue() / numbers.size();
        }
        return sum;
    }
}
