package webserver.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeInfo {

    private double cpuLoad = 0;
    private List<Double> cpuLoadList = new ArrayList<Double>();

    private String lastRequest = "";
    private BasicBlock lastBlock = null;

    /*<BasicBlock, CpuUsage>*/
    private Map<BasicBlock, Double> cpuUsageByBasicBlocks = new HashMap<>();

    private String instanceId;

    public NodeInfo(String instanceId) {
        this.instanceId = instanceId;
    }

    public double getCpuLoad() {
        return cpuLoad;
    }
    
    public List<Double> getCpuLoadList() {
        return cpuLoadList;
    }

    public void setCpuLoad(double cpuLoad) {
        this.cpuLoad = cpuLoad;
        this.cpuLoadList.add(cpuLoad);
    }
    
    public void resetCpuLoadList(){
    	this.cpuLoadList = new ArrayList<Double>();
    }

    public String getLastRequest() {
        return lastRequest;
    }

    public void setLastRequest(String lastRequest) {
        this.lastRequest = lastRequest;
    }

    public Map<BasicBlock, Double> getCpuUsageByBasicBlocks() {
        return cpuUsageByBasicBlocks;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public BasicBlock getLastBlock() {
        return lastBlock;
    }

    public void setLastBlock(BasicBlock lastBlock) {
        this.lastBlock = lastBlock;
    }
}

class BasicBlock {

    private Double basicBlock;
    private Boolean isUsed = true;

    public BasicBlock(Double basicBlock) {
        this.basicBlock = basicBlock;
    }

    public Double getBasicBlock() {
        return basicBlock;
    }

    public Boolean isUsed() {
        return isUsed;
    }

    public void setUsed(Boolean used) {
        isUsed = used;
    }
}
