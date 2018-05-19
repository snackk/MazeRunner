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

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

public class MazeRunnerNodeManager {

    /*Singleton Definition*/
    private static MazeRunnerNodeManager nodesManager = null;

    private static InstancesOperations instancesOps = null;

    /*Nodes*/
    private static Map<String, NodeInfo> nodesByIp = new HashMap<>();
    private static Long nodeMaxLoad = 90L;
	 
    private enum paramsType {x0, y0, x1, y1, v, s, m}
    private enum Features {x0, y0, x1, y1, v}
    private static final String LINEAR_REGRESSION_TARGET = "bbl";
    
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

        /*If there aren't any Nodes registered*/
        if(getNodesByIp().size() == 0){
            throw new NoActiveNodesException();
        }

	double predictedBBL = predictBBL(request);
	System.out.println(predictedBBL);
        String workerIp = "";
        try {
            workerIp = loadBalanceRequest(request);

        } catch (NotEnoughNodesException e) {
            /*The AutoScaler should kick in here to start a new instance*/
        }

        URL newEndpoint = null;
        try {
            newEndpoint = new URL("http://" + workerIp + ":8888/MazeRunnerNodeWS?wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        System.out.println("Sending request " + request + " to " + workerIp);

        QName qname = new QName("http://ws.mazerunnernode/", "MazeRunnerImplService");
        MazeRunnerService mazeRunnerService = new MazeRunnerImplService(newEndpoint, qname).getMazeRunnerImplPort();

        String toReturn =  mazeRunnerService.solveMaze(request);

        /*The request has already been processed here.*/
        return toReturn;
    }

    public String loadBalanceRequest(String request) throws NotEnoughNodesException {

        /*Estimate using LinearRegression the Basic Blocks of the Request*/
        Long requestBasicBlocksEstimation = 1L;

        /*Order Nodes by their CPU usage*/
        nodesByIp = HashMapSort.sortByCpuUsage(getNodesByIp(), HashMapSort.DESC);

        /*Iterate Nodes and estimate the CPU cost of the request*/
        NodeInfo nodeInfoToRequest = null;
        for(NodeInfo nodeInfo : nodesByIp.values()) {

            /*Choose the closest Basic Block to Our Request*/
            Long basicBlock = 0L;
            Long deviation = null;
            for(Long nodeBasicBlock : nodeInfo.getCpuUsageByBasicBlocks().keySet()) {
                Long temp = Math.abs(requestBasicBlocksEstimation - nodeBasicBlock);
                if(deviation == null || temp < deviation) {
                    deviation = temp;
                    basicBlock = nodeBasicBlock;
                }
            }

            /*Compute what could be the Cpu usage of our Request*/
            Long cpuUsageOfBasicBlock = nodeInfo.getCpuUsageByBasicBlocks().get(basicBlock);
            Long estimateCpuUsage = (requestBasicBlocksEstimation * cpuUsageOfBasicBlock) / basicBlock;

            if((nodeInfo.getCpuLoad() + estimateCpuUsage) <= nodeMaxLoad) {
                nodeInfo.getCpuUsageByBasicBlocks().put(requestBasicBlocksEstimation, -1L);
                nodeInfo.setLastRequest(request);
                nodeInfoToRequest = nodeInfo;
                break;
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

   public double predictBBL(String request) {
	Map<String,String> mapQuery = new HashMap<String, String>();
	double[] requestArguments = new double[5];

	int countArgs = 0;

	for(String param : request.split("&")) {
		String pair[] = param.split("=");	
		if(pair.length > 1){ 
			mapQuery.put(pair[0], pair[1]);
			if(!pair[0].equals("s") && !pair[0].equals("m")){
				requestArguments[countArgs] = Double.parseDouble(pair[1]);
				countArgs++;
			}	
		}
		else
			mapQuery.put(pair[0], "");	
	}	

	ScanResult scanResult = mssmanager.fetchDB(mapQuery.get("s"), mapQuery.get("m"));
	
	OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
	double[][] features = new double[scanResult.getCount()][Features.values().length]; 
	double[] target = new double[scanResult.getCount()];        
	double[] beta;
	
	RealMatrix coef;	
	double predictedBBL = 0;

	int countMap, countEnum;
	countMap = countEnum = 0;

	for (Map<String, AttributeValue> map : scanResult.getItems()) {
		for (Features feature : Features.values()){
			features[countMap][countEnum] = Double.parseDouble(map.get(feature.toString()).getS());
			countEnum++;
		}		
		target[countMap] = Double.parseDouble(map.get(LINEAR_REGRESSION_TARGET).getS());
		countEnum = 0;				
		countMap ++;
	}
	try{
		model.newSampleData(target, features);
        	coef = MatrixUtils.createColumnRealMatrix(model.estimateRegressionParameters()); 	
		beta = coef.getData()[0];

		predictedBBL = beta[0];
		for(int i = 0; i < requestArguments.length; i++)
			predictedBBL += beta[i+1] * requestArguments[i];		
   	
		return predictedBBL;
	} catch(MathIllegalArgumentException e) {
		System.out.println("Not enough data yet...");
	}
	return -1.0;
   }
}
