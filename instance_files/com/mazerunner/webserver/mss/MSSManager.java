package com.mazerunner.webserver.mss;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
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

import com.amazonaws.client.builder.AwsClientBuilder;

import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

public class MSSManager {
	private static final String METRICS_TABLE_NAME = "MazerunnerMetrics";
	private static final String LINEAR_REGRESSION_TARGET = "bbl";

	private static MSSManager mssmanager = null;
	private static AmazonDynamoDB dynamoDB;

	private enum Features {x0, y0, x1, y1, v}

	private MSSManager() {
		initDB();
	}
	
	public static MSSManager getInstance() {
      		if(mssmanager == null) {
	        	mssmanager = new MSSManager();
      		}
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
			System.out.println("Initializing DynamoDB...");
		    	
			CreateTableRequest createTableRequest = new CreateTableRequest()
				.withTableName(METRICS_TABLE_NAME)
				.withKeySchema(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH))
				.withAttributeDefinitions(new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S))
				.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
	
			TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
			TableUtils.waitUntilActive(dynamoDB, METRICS_TABLE_NAME);

			System.out.println("DynamoDB is ready!");

			/*DELETEME: TESTING PURPOSES*/
			ScanResult sr = fetchDB("bfs","Maze50.maze");
			LinearRegression lr = parseScanResult(sr);
			/*END OF DELETEME*/
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

	/**/
	public ScanResult fetchDB(String strategy, String mazefile){
		ScanResult scanResult = null;
		try {

			HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
			
			Condition strategyCondition = new Condition()
				.withComparisonOperator(ComparisonOperator.EQ.toString())
				.withAttributeValueList(new AttributeValue().withS(strategy));			
			Condition mazefileCondition = new Condition()
				.withComparisonOperator(ComparisonOperator.EQ.toString())
				.withAttributeValueList(new AttributeValue().withS(mazefile));
		
			scanFilter.put("s", strategyCondition);
			scanFilter.put("m", mazefileCondition);

			ScanRequest scanRequest = new ScanRequest(METRICS_TABLE_NAME).withScanFilter(scanFilter);
			scanResult = dynamoDB.scan(scanRequest);
			System.out.println("Result :" + scanResult);

		} catch (Exception e) {
		    System.err.println("Unable to scan the table: " + e.getMessage());
		}

		return scanResult;			
	}
	/*Transforms a ScanResult into a LinearRegression object*/
	public LinearRegression parseScanResult(ScanResult scanResult) {
		
		LinearRegression lr = new LinearRegression();	
		int[][] features = new int[scanResult.getCount()][Features.values().length]; 

		int[] target = new int[scanResult.getCount()];

		int countMap, countEnum;
		countMap = countEnum = 0;

		for (Map<String, AttributeValue> map : scanResult.getItems()) {
			for (Features feature : Features.values()){
				features[countMap][countEnum] = Integer.parseInt(map.get(feature.toString()).getS());
				target[countMap] = Integer.parseInt(map.get(LINEAR_REGRESSION_TARGET).getS());
				countEnum++;
			}		
			countEnum = 0;				
			countMap ++;
		}
	
		lr.setFeatures(features);
		lr.setTarget(target);	
		
		//System.out.println(scanResult.getItems().get(0));
		//System.out.println(lr);

		return lr;
	}	
		
}
