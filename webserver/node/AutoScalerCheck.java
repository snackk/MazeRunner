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
        for(String ip: nodesByIp.keySet()){
        	System.out.println(ip);
		NodeInfo node = nodesByIp.get(ip);
        	List<Double> cpuLoadList = node.getCpuLoadList();
        	if(isBelowThreshold(cpuLoadList)){
        		String instanceId = node.getInstanceId();
        		InstancesOperations instanceOps = mazeRunnerNodeManager.getInstancesOperationsInstance();
        		instanceOps.terminateInstance(instanceId);
        		ipsToRemove.add(ip);
        	}
        	else if(isUpperThreshold(cpuLoadList)){
        		InstancesOperations instanceOps = mazeRunnerNodeManager.getInstancesOperationsInstance();
        		instanceOps.createInstance();
        		instanceOps.getInstancesIPs();
        	}
		System.out.println(mazeRunnerNodeManager.getNodesByIp());
        	mazeRunnerNodeManager.getNodesByIp().get(ip).resetCpuLoadList();
        }
        /* Remove nodes from list who were stopped */
        for(String ip : ipsToRemove){
        	mazeRunnerNodeManager.getNodesByIp().remove(ip);
        }
    }
    
    public Boolean isBelowThreshold(List<Double> cpuLoadList){
    	int count = 0;
    	for(Double d: cpuLoadList){
    		if(d < lthreshold)
    			count++;
    	if(count == cpuLoadList.size())
    		return true;
    	return false;
    }
    
    public Boolean isUpperThreshold(List<Double> cpuLoadList){
    	int count = 0;
    	for(Double d: cpuLoadList){
    		if(d > hthreshold)
    			count++;
    	}
    	if(count == cpuLoadList.size())
    		return true;
    	return false;
    }
}
