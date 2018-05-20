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

        for(String ip : mazeRunnerNodeManager.getNodesByIp().keySet()) {
            System.out.println("Health check on node: " + ip);
            URL newEndpoint = null;
            HttpURLConnection conn = null;
            try {
                newEndpoint = new URL("http://" + ip + ":8888/MazeRunnerNodeWS?wsdl");
                conn = (HttpURLConnection) newEndpoint.openConnection();
                conn.setRequestMethod("HEAD");
                conn.getInputStream();
                System.out.println(conn.getResponseCode() == 200 ? "Node's OK" : "Node's NOK");

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException ex) {  /*Node is down*/
                System.out.println("Node with ip: " + ip + " is down.");
                ipsToDelete.add(ip);
            }
        }
        mazeRunnerNodeManager.updateNodeInstances(ipsToDelete);

        measureCpuUsage();
    }

    public void measureCpuUsage() {
        InstancesOperations instancesOperations = MazeRunnerNodeManager.getInstancesOperationsInstance();

        for(String ip : mazeRunnerNodeManager.getNodesByIp().keySet()) {
            NodeInfo nodeInfo = mazeRunnerNodeManager.getNodesByIp().get(ip);
            double cpuUsage = instancesOperations.getInstanceAverageLoad(nodeInfo.getInstanceId());

            nodeInfo.setCpuLoad(cpuUsage);
            System.out.println("CPU usage: " + cpuUsage);

            double parcialCpuUsage = 0D;
            for(double basicBlock : nodeInfo.getCpuUsageByBasicBlocks().keySet()) {
                double cpuPerBB = nodeInfo.getCpuUsageByBasicBlocks().get(basicBlock);
                if(cpuPerBB != -1D) {
                    parcialCpuUsage += cpuPerBB;
                } else {
                    nodeInfo.getCpuUsageByBasicBlocks().put(basicBlock, (cpuUsage - parcialCpuUsage));
                }
            }
        }
    }
}
