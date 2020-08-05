package com.demo.awss3imagecomparision.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="rekognition")
public class AWSS3Config {
	
	private String s3BucketName;
    private String s3RegionName;
    private String accessKey;
    private String secretKey;
    
    public String getS3BucketName() {
    	return s3BucketName;
    }
    public void setS3BucketName(String s3BucketName) {
    this.s3BucketName = s3BucketName;
    }
    public String getS3RegionName() {
    	return s3RegionName;
    }
    public void setS3RegionName(String s3RegionName) {
    this.s3RegionName = s3RegionName;
    }    
    
    public String getAccessKey() {
    	return accessKey;
    }
    public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
    }    
    
    public String getSecretKey() {
    	return secretKey;
    }
    public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
    }    
}
