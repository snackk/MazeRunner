package webserver.node;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class HealthCheck extends TimerTask {

    private MazeRunnerNodeManager mazeRunnerNodeManager;

    public void run() {
        mazeRunnerNodeManager = MazeRunnerNodeManager.getInstance();

        List<String> ipsToDelete = new ArrayList<>();

        for(String ip : mazeRunnerNodeManager.getMachinesIp()) {
            System.out.println("Health check on node: " + ip);
            URL newEndpoint = null;
            HttpURLConnection conn = null;
            try {
                newEndpoint = new URL("http://" + ip + ":8888/MazeRunnerNodeWS?wsdl");
                conn = (HttpURLConnection) newEndpoint.openConnection();
                conn.setRequestMethod("HEAD");
                conn.getInputStream();
                System.out.println(conn.getResponseCode());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException ex) {  /*Node is down*/
                System.out.println("Node with ip: " + ip + " is down.");
                ipsToDelete.add(ip);
            }
        }
        mazeRunnerNodeManager.updateNodeInstances(ipsToDelete);
    }
}
