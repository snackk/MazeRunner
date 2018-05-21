package webserver.node;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

public class AutoScalerCheck extends TimerTask {

    private MazeRunnerNodeManager mazeRunnerNodeManager;
    private static double lthreshold = 20; //FIXME
    private static double hthreshold = 80; //FIXME

    public void run() {
        mazeRunnerNodeManager = MazeRunnerNodeManager.getInstance();

        Map<String, NodeInfo> nodesByIp = mazeRunnerNodeManager.getNodesByIp();
        
        List<String> ipsToRemove = new ArrayList<String>();
        /* Find instances below threshold and stop them */
        for(String ip: nodesByIp.getKeySet()){
        	NodeInfo node = nodesByIp.get(ip);
        	List<double> cpuLoadList = node.getCpuLoadList();
        	if(isBelowThreshold(cpuLoadList)){
        		String instanceId = node.getInstanceId();
        		InstanceOperations instanceOps = mazeRunnerNodeManager.getInstanceOperationsInstance();
        		instanceOps.stopInstance(instanceId);
        		ipsToRemove.add(ip);
        	}
        	else if(isUpperThreshold(cpuLoadList)){
        		InstanceOperations instanceOps = mazeRunnerNodeManager.getInstanceOperationsInstance();
        		instanceOps.createInstance();
        		instanceOps.getInstanceIps();
        	}
        	mazeRunnerNodeManager.getNodesByIp().get(ip).resetCpuLoadList();
        }
        /* Remove nodes from list who were stopped */
        for(String ip : ipsToRemove){
        	mazeRunnerNodeManager.getNodesByIp().remove(ip);
        }
    }
    
    public Boolean isBelowThreshold(List<double> cpuLoadList){
    	int count = 0;
    	for(Double d: cpuLoadList){
    		if(d < threshold)
    			count++
    	}
    	if(count == cpuLoadList.size())
    		return true;
    	return false;
    }
    
    public Boolean isUpperThreshold(List<double> cpuLoadList){
    	int count = 0;
    	for(Double d: cpuLoadList){
    		if(d > threshold)
    			count++
    	}
    	if(count == cpuLoadList.size())
    		return true;
    	return false;
    }
}
