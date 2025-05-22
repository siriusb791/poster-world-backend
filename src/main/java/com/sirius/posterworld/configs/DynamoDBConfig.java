package com.sirius.posterworld.configs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class DynamoDBConfig {

    @Value("${aws.accessKeyId}")
    private String awsAccessKeyId;

    @Value("${aws.secretKey}")
    private String awsSecretKey;

    @Value("${aws.dynamodb.region}")
    private String awsRegion;

    @Value("${aws.dynamodb.endpoint}")
    private String awsEndpoint;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new AWSCredentials() {
                    @Override
                    public String getAWSAccessKeyId() {
                        return awsAccessKeyId;
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return awsSecretKey;
                    }
                }));

        if (StringUtils.hasText(awsEndpoint)) {
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsEndpoint, awsRegion));
        } else {
            builder.withRegion(awsRegion);
        }

        return builder.build();
    }
}
