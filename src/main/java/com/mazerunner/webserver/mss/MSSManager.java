package com.mazerunner.webserver.mss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
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
import com.amazonaws.services.dynamodbv2.model.UpdateTableRequest;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

public class MSSManager {

	private static MSSManager mssmanager = null;
	private static AmazonDynamoDB dynamoDB;

	private MSSManager() {
		InitializeDB();
	}
	
	public static MSSManager getInstance() {
      		if(mssmanager == null) {
	        	mssmanager = new MSSManager();
      		}
      		return mssmanager;
	}
	
	private void InitializeDB() {
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
			String tableName = "metrics";

			// Create a table with a primary hash key named 'name', which holds a string
		    	CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
				.withKeySchema(new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.HASH))
				.withAttributeDefinitions(new AttributeDefinition().withAttributeName("name").withAttributeType(ScalarAttributeType.S))
				.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
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
		}
	}
	
	public void createEntryMetric(List<String> metrics){

	        final Map<String, Object> infoMap = new HashMap<String, Object>();
	        //FIXME for index in metrics, put <metric name or request parameter, value>
	        infoMap.put("plot", "Nothing happens at all.");
	        infoMap.put("rating", 0);

	        try {
	            System.out.println("Adding a new item...");
	            PutItemRequest itemRequest = new PutItemRequest();
	            //FIXME add metrics and parameters attributes entry to DB
				itemRequest.addItemEntry("x0", new AttributeValue("0"));
	            PutItemResult outcome = dynamoDB.putItem(itemRequest);

	            System.out.println("PutItem succeeded:\n" + outcome.toString());

	        }
	        catch (Exception e) {
	            System.err.println("Unable to add item: " + "x0 " + "0");
	            System.err.println(e.getMessage());
	        }
	}
}