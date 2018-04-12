package com.mazerunner.webserver.node;

import com.mazerunner.webserver.ws.MazeRunnerImplService;
import com.mazerunner.webserver.ws.MazeRunnerService;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MazeRunnerNodeManager {

    private static MazeRunnerNodeManager nodesManager = null;
    private static List<String> machinesIp = new ArrayList<>();

    public static List<String> getMachinesIp() {
        return machinesIp;
    }

    protected MazeRunnerNodeManager(){

    }

    public static MazeRunnerNodeManager getInstance(){
        if(nodesManager == null)
            nodesManager = new MazeRunnerNodeManager();
        return nodesManager;
    }

    public String solveMazeOnNode(String request){
        String workerIp = getMachinesIp().get(getMachinesIp().size() - 1);

        URL newEndpoint = null;
        try {
            newEndpoint = new URL("http://" + workerIp + ":8888/MazeRunnerNodeWS?wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        QName qname = new QName("http://ws.node.mazerunner.com/", "MazeRunnerImplService");
        MazeRunnerService mazeRunnerService = new MazeRunnerImplService(newEndpoint, qname).getMazeRunnerImplPort();

        return mazeRunnerService.solveMaze(request);
    }

    public void registerIp(String ip){

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
