package webserver.node;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
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

public class InstancesOperations {

	private Map<String, String> instancesPrivateIPs = new HashMap<String, String>();
	private Map<String, String> instancesPublicIPs = new HashMap<String, String>();
	
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
		            listInstance(instance);
		            instancesPrivateIPs.put(instance.getInstanceId(), instance.getPrivateIpAddress());
		            instancesPublicIPs.put(instance.getInstanceId(), instance.getPublicIpAddress());
		        }
		    }

		    if(response.getNextToken() == null) {
		        done = true;
		    }
		}
	}

	public void listInstance(Instance instance) {
		System.out.printf(
                "Found reservation with id %s, " +
                "AMI %s, " +
                "type %s, " +
                "state %s " +
                "monitoring state %s" +
                "with private ip %s" +
                "and public ip %s",
                instance.getInstanceId(),
                instance.getImageId(),
                instance.getInstanceType(),
                instance.getState().getName(),
                instance.getMonitoring().getState(),
                instance.getPrivateIpAddress(),
                instance.getPublicIpAddress());
            System.out.println("");
	}
	
	public void createInstance(){
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
		String ami_id = ""; //FIXME
		RunInstancesRequest run_request = new RunInstancesRequest();
		run_request.setImageId(ami_id);
		run_request.setInstanceType(InstanceType.T1Micro);
		run_request.setMaxCount(1);
		run_request.setMinCount(1);
		
		AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentialsProvider.getCredentials())).build();

		RunInstancesResult response = ec2.runInstances(run_request);

		String instance_id = response.getReservation().getReservationId();

		//FIX ME TAG
		Tag tag = new Tag();
		tag.setKey("Name");
	    tag.setValue(instance_id); 

		CreateTagsRequest tag_request = new CreateTagsRequest();
		    tag_request.withTags(tag);

		ec2.createTags(tag_request);
		startInstance(instance_id);
	    System.out.printf(
	        "Successfully started EC2 instance %s based on AMI %s",
	        instance_id, ami_id);		
	}
	
	public void rebootInstance(String instance_id){
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

        RebootInstancesRequest request = new RebootInstancesRequest();
        request.withInstanceIds(instance_id);

        RebootInstancesResult response = ec2.rebootInstances(request);

        if(response!=null)
        	System.out.printf("Successfully rebooted instance %s", instance_id);
	}
	
	public void startInstance(String instance_id){
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

		StartInstancesRequest request = new StartInstancesRequest();
		request.withInstanceIds(instance_id);

		ec2.startInstances(request);
	}
	
	public void stopInstance(String instance_id){
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
		
		StopInstancesRequest request = new StopInstancesRequest();
		request.withInstanceIds(instance_id);

		ec2.stopInstances(request);
		
		 System.out.printf("Successfully stop instance %s", instance_id);
	}
}
