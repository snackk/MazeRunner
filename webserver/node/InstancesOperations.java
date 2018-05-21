package webserver.node;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.CreateTagsResult;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.DryRunResult;
public class InstancesOperations {

	private Map<String, String> instancesPrivateIPs = new HashMap<String, String>();
	private Map<String, String> instancesPublicIPs = new HashMap<String, String>();
	
	private static final String node_AMI_ID = "ami-3841df47";
	private static final String security_group_ID = "sg-1e70ff57";
	private static final String subnet_ID = "subnet-b9555edd";
	private static final String instance_TYPE = InstanceType.T2Micro.toString();
	private static final String keyName = "CNV-aws";

	
	public InstancesOperations(){ }
	
	public Map<String, String> getInstancesPrivateIPs(){
		return this.instancesPrivateIPs;
	}
	
	public Map<String, String> getInstancesPublicIPs(){
		return this.instancesPublicIPs;
	}
	
	public void getInstancesIPs(){
		AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
		    credentialsProvider.getCredentials();
		} catch (Exception e) {
		    throw new AmazonClientException(
			    "Cannot load the credentials from the credential profiles file. " +
			    "Please make sure that your credentials file is at the correct " +
			    "location (~/.aws/credentials), and is in valid format.",
			    e);
		}
		AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentialsProvider.getCredentials())).build();
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		
		Boolean done = false;
		while(!done) {
		    DescribeInstancesResult response = ec2.describeInstances(request);

		    for(Reservation reservation : response.getReservations()) {
		        for(Instance instance : reservation.getInstances()) {
		        	//TODO must also check if wsdl has been published
		        	if(instance.getImageId().equals(node_AMI_ID) && instance.getState().getCode() == 16) {
						listInstance(instance);
						instancesPrivateIPs.put(instance.getInstanceId(), instance.getPrivateIpAddress());
						instancesPublicIPs.put(instance.getInstanceId(), instance.getPublicIpAddress());
					}
		        }
		    }

		    if(response.getNextToken() == null) {
		        done = true;
		    }
		}
	}
	
	public String createInstance(){
		AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
		    credentialsProvider.getCredentials();
		} catch (Exception e) {
		    throw new AmazonClientException(
			    "Cannot load the credentials from the credential profiles file. " +
			    "Please make sure that your credentials file is at the correct " +
			    "location (~/.aws/credentials), and is in valid format.",
			    e);
		}
		RunInstancesRequest run_request = new RunInstancesRequest();
		run_request.withImageId(node_AMI_ID);
		run_request.withInstanceType(instance_TYPE);
		run_request.withMaxCount(1);
		run_request.withMinCount(1);
		//run_request.withSubnetId(subnet_ID);
		run_request.withSecurityGroups("Omega");
		run_request.withMonitoring(true);
		run_request.withRequestCredentialsProvider(credentialsProvider);
		run_request.withKeyName(keyName);
		
		AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentialsProvider.getCredentials())).build();

		RunInstancesResult response = ec2.runInstances(run_request);

		String instance_id = response.getReservation().getInstances()
                                      .get(0).getInstanceId();
		System.out.println("New instance -> " + instance_id);
		
		System.out.printf(
		        "Successfully created EC2 instance %s based on AMI %s",
		        instance_id, node_AMI_ID);
		
		//startInstance(instance_id);
		
		return instance_id;
	}
	
	public void startInstance(String instance_id) {
		AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
		    credentialsProvider.getCredentials();
		} catch (Exception e) {
		    throw new AmazonClientException(
			    "Cannot load the credentials from the credential profiles file. " +
			    "Please make sure that your credentials file is at the correct " +
			    "location (~/.aws/credentials), and is in valid format.",
			    e);
		}
		AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentialsProvider.getCredentials())).build();

        StartInstancesRequest request = new StartInstancesRequest()
            .withInstanceIds(instance_id);

        try{
	        ec2.startInstances(request);
			System.out.println("Starting a  instance " + instance_id + "...");
			Thread.sleep(60000);
		    System.out.printf("Successfully started instance %s", instance_id);
        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }
    }
	
	public void terminateInstance(String instance_id) {
		AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
		    credentialsProvider.getCredentials();
		} catch (Exception e) {
		    throw new AmazonClientException(
			    "Cannot load the credentials from the credential profiles file. " +
			    "Please make sure that your credentials file is at the correct " +
			    "location (~/.aws/credentials), and is in valid format.",
			    e);
		}
		AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentialsProvider.getCredentials())).build();

		System.out.println("Terminating the instance %s", instance_id);
        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(instance_id);
        ec2.terminateInstances(termInstanceReq);
	}

	public double getInstanceAverageLoad(String instanceId) {
		AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
							"Please make sure that your credentials file is at the correct " +
							"location (~/.aws/credentials), and is in valid format.",
					e);
		}
		AmazonCloudWatchClient cloudWatchClient = new AmazonCloudWatchClient(new AWSStaticCredentialsProvider(credentialsProvider.getCredentials()));

		long offsetInMilliseconds = 1000 * 60 * 60;
		GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
				.withStartTime(new Date(new Date().getTime() - offsetInMilliseconds))
				.withNamespace("AWS/EC2")
				.withPeriod(60 * 60)
				.withDimensions(new Dimension().withName("InstanceId").withValue(instanceId))
				.withMetricName("CPUUtilization")
				.withStatistics("Average", "Maximum")
				.withEndTime(new Date());
		GetMetricStatisticsResult getMetricStatisticsResult = cloudWatchClient.getMetricStatistics(request);

		double avgCPUUtilization = 0;
		List dataPoint = getMetricStatisticsResult.getDatapoints();
		for (Object aDataPoint : dataPoint) {
			Datapoint dp = (Datapoint) aDataPoint;
			avgCPUUtilization = dp.getAverage();
		}
		
		return avgCPUUtilization;
	}
}
