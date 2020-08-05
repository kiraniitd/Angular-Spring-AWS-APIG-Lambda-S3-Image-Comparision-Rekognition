package com.demo.awss3imagecomparision.apiglambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import com.demo.awss3imagecomparision.AwsS3ImageComparisionApplication;
import com.demo.awss3imagecomparision.config.AWSS3Config;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.ComparedFace;
import com.amazonaws.services.rekognition.model.DetectModerationLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectModerationLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.runtime.Context;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.awss3imagecomparision.util.AWSs3ImageCompareUtil;

@Component
public class AWSRekognitionImageComparision implements Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	
	private static Logger logger = LoggerFactory.getLogger(AwsS3ImageComparisionApplication.class);
	private String s3BucketName;
	private String s3RegionName;
	private String accessKey;
    private String secretKey;
	
	@Autowired
	AWSS3Config appConfig;

	
	 	@Override
	    public APIGatewayProxyResponseEvent apply(APIGatewayProxyRequestEvent input) {
	        APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent  = new APIGatewayProxyResponseEvent();
	        List<String> s3FileNames = new ArrayList<String>();
	        try
	        {
	        	//Get info from property file
	        	s3BucketName = appConfig.getS3BucketName();
	        	logger.info("Bucket Name-->"+s3BucketName);
	        	s3RegionName = appConfig.getS3RegionName();
	    		logger.info("Region Name-->"+s3RegionName);
	    		accessKey = appConfig.getAccessKey();
	        	logger.info("accessKey-->"+accessKey);
	        	secretKey = appConfig.getSecretKey();
	    		logger.info("secretKey-->"+secretKey);
	    		
	    		// Establish AWS Credentials
	    		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
	    		
	    		//Create S3Client Object
	            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
	                    .withRegion(s3RegionName)
	                    .build();
	            
	            //Create AWS Rekognition object
	            ClientConfiguration clientConfig = AWSs3ImageCompareUtil.createClientConfiguration();
				AmazonRekognition amazonRekognitionClientBuilder=  AmazonRekognitionClientBuilder.standard()
					.withClientConfiguration(clientConfig)					
					.withCredentials(new AWSStaticCredentialsProvider(credentials))
					.withRegion(s3RegionName)
					.build();
	            
	            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
	                    .withBucketName(s3BucketName);
	    		
	            //List file names from s3 bucket
	            ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);
	            for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {	                
	                logger.info("List is -logger->"+summary.getKey());	 
	                s3FileNames.add(summary.getKey());
	            }
	            	            
	            List<String> finalComparedResult = new ArrayList<String>();
	            HashMap <String, ByteBuffer> s3ImageData = new HashMap<>();
	            s3ImageData= AWSs3ImageCompareUtil.getImageArrayFromS3(s3Client, s3BucketName, s3FileNames);	            
	            
	            //  compares each image to every other image in s3 (total 15 iterations for 6 images)
	            for (int i = 0; i < s3FileNames.size()-1; i++) {
	            	
	                for (int k = i + 1; k < s3FileNames.size(); k++) {
	                	
	                    if (s3FileNames.get(i) != s3FileNames.get(k)) {

	                        String imageComparisionResults = compareImages( amazonRekognitionClientBuilder,  s3ImageData.get(s3FileNames.get(i)), s3FileNames.get(i),  s3ImageData.get(s3FileNames.get(k)),  s3FileNames.get(k));
	       
	                        finalComparedResult.add(imageComparisionResults);
	                    }
	                }
	            }
            
	            logger.info("finalComparedResult -->"+finalComparedResult);
	            
	            apiGatewayProxyResponseEvent .setStatusCode(200);	            
	            apiGatewayProxyResponseEvent.setBody("Image Comparision Data: "+finalComparedResult);
	            
	        } catch(Exception e) {
	        	e.printStackTrace();
	        	throw e;
	        }
	        return apiGatewayProxyResponseEvent ;
	    }
	

	 	 // Compare images in s3 bucket and return scores for the matched images
	    private static String compareImages(AmazonRekognition rekClient, ByteBuffer sourceImage, String sourceKey, ByteBuffer targetImage, String targetKey) {
	    	
	    	String imageComparisionResults = "";
	    	try {	    	
		        	CompareFacesRequest request = new CompareFacesRequest()
		                .withSourceImage(new Image().withBytes(sourceImage))
		                .withTargetImage(new Image().withBytes(targetImage))
		                .withSimilarityThreshold(70F);
		 
			        CompareFacesResult result = rekClient.compareFaces(request);
			         
			        List<CompareFacesMatch> faceMatches = result.getFaceMatches();
			        imageComparisionResults = "Comparing Image: " + sourceKey + " and " + targetKey;			         
			        for (CompareFacesMatch match : faceMatches) {
			            Float similarity = match.getSimilarity();	  
			            ComparedFace face = match.getFace();
			            BoundingBox position = face.getBoundingBox();
			                        
			            imageComparisionResults += " Similarity : "+ similarity + " Confidence: " + face.getConfidence()
			            + " Position: " + " Height -" + position.getHeight() + " Left -" + position.getLeft() + " Top -" + position.getTop() + " Width -" + position.getWidth(); 
			        }	
	        } catch(Exception e) {
	        	e.printStackTrace();
	        	throw e;
	        }		        
	    	 logger.info("Image comparision results ->"+imageComparisionResults);
	        return imageComparisionResults;       

	    }
}
