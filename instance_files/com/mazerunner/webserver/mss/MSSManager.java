package com.mazerunner.webserver.mss;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

public class MSSManager {
	private static final String METRICS_TABLE_NAME = "MazerunnerMetrics";

	private static MSSManager mssmanager = null;
	private static AmazonDynamoDB dynamoDB;
	private MSSManager() {
		System.out.println("WTFF");
		initDB();
	}
	
	public static MSSManager getInstance() {
      		if(mssmanager == null) {
	        	mssmanager = new MSSManager();
      		}
		mssmanager.initDB();
      		return mssmanager;
	}
	
	private void initDB() {
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
		    credentialsProvider.getCredentials();
		} catch (Exception e) {
		    throw new AmazonClientException(
			    "Cannot load the credentials from the credential profiles file. " +
			    "Please make sure that your credentials file is at the correct " +
			    "location (~/.aws/credentials), and is in valid format.",
			    e);
		}
		dynamoDB = AmazonDynamoDBClientBuilder.standard()
		    .withCredentials(credentialsProvider)
		    .withRegion("us-east-1a")
		    .build();
		try {
			System.out.println("CreatingDB...");
			// Create a table with a primary hash key named 'name', which holds a string
		    	CreateTableRequest createTableRequest = new CreateTableRequest()
				.withTableName(METRICS_TABLE_NAME)
				.withKeySchema(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH))
				.withAttributeDefinitions(new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S))
				.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

			TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
			TableUtils.waitUntilActive(dynamoDB, METRICS_TABLE_NAME);
			System.out.println("Created!");
		} catch (AmazonServiceException ase) {
            		System.out.println("Caught an AmazonServiceException, which means your request made it "
                    		+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
		    	System.out.println("Caught an AmazonClientException, which means the client encountered "
			    + "a serious internal problem while trying to communicate with AWS, "
			    + "such as not being able to access the network.");
		    	System.out.println("Error Message: " + ace.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
