package webserver.node;

import java.util.HashMap;
import java.util.Map;

public class NodeInfo {

    private double cpuLoad = 0;
    private List<double> cpuLoadList = new ArrayList<double>();

    private String lastRequest = "";

    /*<BasicBlock, CpuUsage>*/
    private Map<Double, Double> cpuUsageByBasicBlocks = new HashMap<>();

    private String instanceId;

    public NodeInfo(String instanceId) {
        this.instanceId = instanceId;
    }

    public double getCpuLoad() {
        return cpuLoad;
    }
    
    public List<double> getCpuLoadList() {
        return cpuLoadList;
    }

    public void setCpuLoad(double cpuLoad) {
        this.cpuLoad = cpuLoad;
        this.cpuLoadList.add(cpuLoad);
    }
    
    public void resetCpuLoadList(){
    	this.cpuLoadList = new ArrayList<double>();
    }

    public String getLastRequest() {
        return lastRequest;
    }

    public void setLastRequest(String lastRequest) {
        this.lastRequest = lastRequest;
    }

    public Map<Double, Double> getCpuUsageByBasicBlocks() {
        return cpuUsageByBasicBlocks;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
