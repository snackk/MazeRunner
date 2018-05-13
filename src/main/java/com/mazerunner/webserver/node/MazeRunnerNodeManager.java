package com.mazerunner.webserver.node;

import com.mazerunner.webserver.ws.MazeRunnerImplService;
import com.mazerunner.webserver.ws.MazeRunnerService;
import com.mazerunner.webserver.exceptions.NoActiveNodesException;
import com.mazerunner.webserver.mss.InstancesOperations;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MazeRunnerNodeManager {

    private static MazeRunnerNodeManager nodesManager = null;
    private static InstancesOperations instancesOps = null;
    private static List<String> machinesIp = new ArrayList<>();

    public static List<String> getMachinesIp() {
        return machinesIp;
    }
    
    public static int currentMachineIndex = 0;

    protected MazeRunnerNodeManager(){

    }

    public static MazeRunnerNodeManager getInstance(){
        if(nodesManager == null)
            nodesManager = new MazeRunnerNodeManager();
        return nodesManager;
    }

    public String solveMazeOnNode(String request) throws NoActiveNodesException{

        if(machinesIp.size() == 0){
            throw new NoActiveNodesException();
        }

        String workerIp = machinesIp.get(currentMachineIndex);

        //FIXME node assigned to solve
        if(currentMachineIndex == machinesIp.size() -1)
            currentMachineIndex = 0;
        else
            currentMachineIndex++;
        System.out.println(currentMachineIndex);

        URL newEndpoint = null;
        try {
            newEndpoint = new URL("http://" + workerIp + ":8888/MazeRunnerNodeWS?wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        System.out.println("Sending request " + request + " to " + workerIp);

        QName qname = new QName("http://ws.node.mazerunner.com/", "MazeRunnerImplService");
        MazeRunnerService mazeRunnerService = new MazeRunnerImplService(newEndpoint, qname).getMazeRunnerImplPort();
        return mazeRunnerService.solveMaze(request);
    }

    public void registerIp(String ip){
    	if(ip == null){
    		instancesOps.getInstancesIPs();
    		Map<String, String> iprivate = instancesOps.getInstancesPrivateIPs();
    		Map<String, String> ipublic = instancesOps.getInstancesPublicIPs();
    		for(String key : iprivate.keySet()){
    			getMachinesIp().add(iprivate.get(key));
    			System.out.println("MazeRunnerNode with public ip "+ipublic.get(key) + " is up.");
    		}
    		return;
    	}
        for(String e : getMachinesIp()){
            if(e.equals(ip)){
                System.out.println(e + " was already up.");
                return;
            }
        }
        getMachinesIp().add(ip);

        System.out.println(ip + " is up.");
    }
}