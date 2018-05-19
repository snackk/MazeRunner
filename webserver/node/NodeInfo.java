package webserver.node;

import java.util.HashMap;
import java.util.Map;

public class NodeInfo {

    private int cpuLoad = 0;

    private String lastRequest = "";

    /*<BasicBlock, CpuUsage>*/
    private Map<Double, Long> cpuUsageByBasicBlocks = new HashMap<>();

    public NodeInfo() {

    }

    public int getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(int cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public String getLastRequest() {
        return lastRequest;
    }

    public void setLastRequest(String lastRequest) {
        this.lastRequest = lastRequest;
    }

    public Map<Double, Long> getCpuUsageByBasicBlocks() {
        return cpuUsageByBasicBlocks;
    }
}
