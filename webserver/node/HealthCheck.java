package webserver.node;

import java.io.IOException;
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
            URL newEndpoint = null;
            try {
                newEndpoint = new URL("http://" + ip + ":8888/MazeRunnerNodeWS?wsdl");
                newEndpoint.openConnection();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException ex) {  /*Node is down*/
                ipsToDelete.add(ip);
            }
        }
        mazeRunnerNodeManager.updateNodeInstances(ipsToDelete);
    }
}
