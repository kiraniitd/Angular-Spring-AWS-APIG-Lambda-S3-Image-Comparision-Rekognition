package com.demo.awss3imagecomparision.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.amazonaws.Protocol;

public class AWSs3ImageCompareUtil {
	
	// Read all images from S3 bucket to ByteBuffer / HashMap
    public static HashMap<String, ByteBuffer> getImageArrayFromS3(AmazonS3 s3Client, String s3bucket, List<String> results)
    {
    	HashMap<String, ByteBuffer> hasMap = new HashMap<String, ByteBuffer>();    	
    	
    	 for (int i = 0; i < results.size(); i++) {
	        byte[] byteArray;
	        try {
	        	S3Object object = s3Client.getObject(s3bucket, results.get(i)); 
	        	byteArray = IOUtils.toByteArray(object.getObjectContent());
	        } catch (IOException e) {
	            System.err.println("Failed to load image: " + e.getMessage());
	            return null;
	        }
	        hasMap.put(results.get(i), ByteBuffer.wrap(byteArray)); 
    	}
    	return hasMap;
    }
    
    // AWS Rekognition client configuration
    public static ClientConfiguration createClientConfiguration() {
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setConnectionTimeout(30000);
        clientConfig.setRequestTimeout(60000);
        clientConfig.setProtocol(Protocol.HTTPS);
        return clientConfig;
    }

}
