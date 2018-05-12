package com.mazerunner.webserver.mss;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

public class InstancesOperations {

	private Map<String, String> instancesPrivateIPs = new HashMap<String, String>();
	private Map<String, String> instancesPublicIPs = new HashMap<String, String>();
	
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
}
