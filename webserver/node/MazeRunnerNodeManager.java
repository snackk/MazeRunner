package webserver.node;

import webserver.exceptions.MazeRunnerException;
import webserver.exceptions.NotEnoughNodesException;
import webserver.mss.MSSManager;
import webserver.node.util.HashMapSort;
import webserver.ws.MazeRunnerImplService;
import webserver.ws.MazeRunnerService;
import webserver.exceptions.NoActiveNodesException;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


public class MazeRunnerNodeManager {

    /*Singleton Definition*/
    private static MazeRunnerNodeManager nodesManager = null;

    private static InstancesOperations instancesOps = null;

    /*Nodes*/
    private static Map<String, NodeInfo> nodesByIp = new HashMap<>();
    private static Long nodeMaxLoad = 90L;


    private MSSManager mssmanager;

    protected MazeRunnerNodeManager(){ 
	mssmanager = MSSManager.getInstance();	
    }

    public static MazeRunnerNodeManager getInstance(){
        if(nodesManager == null)
            nodesManager = new MazeRunnerNodeManager();
        return nodesManager;
    }
    
    public static InstancesOperations getInstancesOperationsInstance(){
        if(instancesOps == null)
        	instancesOps = new InstancesOperations();
        return instancesOps;
    }

    public static Map<String, NodeInfo> getNodesByIp() {
        return nodesByIp;
    }

    public String solveMazeOnNode(String request) throws MazeRunnerException {

        String workerIp = loadBalanceRequest(request);

        URL newEndpoint = null;
        try {
            newEndpoint = new URL("http://" + workerIp + ":8888/MazeRunnerNodeWS?wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        System.out.println("Sending request " + request + " to " + workerIp);

        QName qname = new QName("http://ws.mazerunnernode/", "MazeRunnerImplService");
        MazeRunnerService mazeRunnerService = new MazeRunnerImplService(newEndpoint, qname).getMazeRunnerImplPort();
        return mazeRunnerService.solveMaze(request);
    }

    public String loadBalanceRequest(String request) throws MazeRunnerException {

        /*If there aren't any Nodes registered*/
        if(getNodesByIp().size() == 0){
            throw new NoActiveNodesException();
        }

        /*Estimate using LinearRegression the Basic Blocks of the Request*/
        Long requestBasicBlocksEstimation = 1L;

        /*Order Nodes by their CPU usage*/
        nodesByIp = HashMapSort.sortByCpuUsage(getNodesByIp(), HashMapSort.DESC);

        /*Iterate Nodes and estimate the CPU cost of the request*/
        NodeInfo nodeInfoToRequest = null;
        for(NodeInfo nodeInfo : nodesByIp.values()) {
            Long estimateCpuUsage = (requestBasicBlocksEstimation * nodeInfo.getCpuLoad()) / nodeInfo.getEstimateBasicBlocks();

            if((nodeInfo.getCpuLoad() + estimateCpuUsage) <= nodeMaxLoad) {
                nodeInfo.setEstimateBasicBlocks(requestBasicBlocksEstimation);
                nodeInfo.setLastRequest(request);
                nodeInfoToRequest = nodeInfo;
            }
        }

        /*If the Request can't be process by the Nodes*/
        if(nodeInfoToRequest == null) {
            throw new NotEnoughNodesException();
        }

        /*Getting the actual IP for the Node*/
        String nodeToProcessRequest = "";
        for(String node : nodesByIp.keySet()) {
            nodeToProcessRequest = node;
            break;
            /*Mock because havent got real numbers to predict*/
//            if(nodesByIp.get(node).equals(nodeInfoToRequest)) {
//                nodeToProcessRequest = node;
//            }
        }

        return nodeToProcessRequest;
    }

    public void registerIp(String ip){
    	if(ip == null){
    		InstancesOperations instancesOps = getInstancesOperationsInstance();
    		instancesOps.getInstancesIPs();
    		Map<String, String> iprivate = instancesOps.getInstancesPrivateIPs();
    		Map<String, String> ipublic = instancesOps.getInstancesPublicIPs();
    		for(String key : iprivate.keySet()){
    			nodesByIp.put(iprivate.get(key), new NodeInfo());
    			System.out.println("MazeRunnerNode with public ip " + ipublic.get(key) + " is up.");
    		}
    		return;
    	}
        for(String e : nodesByIp.keySet()){
            if(e.equals(ip)){
                System.out.println(e + " was already up.");
                return;
            }
        }
        nodesByIp.put(ip, new NodeInfo());

        System.out.println(ip + " is up.");
    }

    public void updateNodeInstances(List<String> downNodes) {
        for(String downNode : downNodes) {
            nodesByIp.remove(downNode);
        }
    }
}
