package webserver.node;

public class NodeInfo {

    private int cpuLoad = 0;

    private String lastRequest = "";

    private Long estimateBasicBlocks = 0L;

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

    public Long getEstimateBasicBlocks() {
        return estimateBasicBlocks;
    }

    public void setEstimateBasicBlocks(Long estimateBasicBlocks) {
        this.estimateBasicBlocks = estimateBasicBlocks;
    }
}
