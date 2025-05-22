package com.sirius.posterworld.services;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.sirius.posterworld.models.Poster;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;


import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PosterService {

    private static final Logger logger = LoggerFactory.getLogger(PosterService.class);

    private final AmazonDynamoDB amazonDynamoDB;
    private DynamoDBMapper dynamoDBMapper;

    @Autowired
    private  AmazonS3 s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.dynamodb.table.posters:Posters}") // Default to "Posters" if not configured
    private String postersTableName;

    @Autowired
    public PosterService(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
        this.dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
    }

    @PostConstruct
    public void init() {
        try {
            amazonDynamoDB.describeTable(new DescribeTableRequest().withTableName(postersTableName));
            System.out.println("Table " + postersTableName + " exists.");
        } catch (ResourceNotFoundException e) {
            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(Poster.class);
            tableRequest.setTableName(postersTableName);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L)); // Adjust throughput as needed
            amazonDynamoDB.createTable(tableRequest);
            System.out.println("Table " + postersTableName + " created successfully.");
        }
    }

    public Poster addPoster(Poster poster) {
        poster.setPosterId(UUID.randomUUID().toString()); // Generate a unique ID
        poster.setUploadDate(LocalDateTime.now());
        dynamoDBMapper.save(poster);
        return poster;
    }

    public Poster getPosterById(String posterId) {
        return dynamoDBMapper.load(Poster.class, posterId);
    }

//    public List<Poster> getAllPosters() {
//        return dynamoDBMapper.scan(Poster.class, null); // Scan the entire table
//    }

//    public List<Poster> getAllPosters() {
//        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression(); // Create an empty scan expression
//        return dynamoDBMapper.scan(Poster.class, scanExpression);
//    }

//    public List<Poster> getPostersByCategory(String category) {
//        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
//                .withFilterExpression("category = :categoryValue")
//                .withExpressionAttributeValues(Map.of(":categoryValue", new AttributeValue().withS(category)));
//        return dynamoDBMapper.scan(Poster.class, scanExpression);
//    }

    public List<Poster> getAllPosters(String category) {


        logger.info("getall posters servic categry");

        List<Poster> posterList;
        if (category != null && !category.isEmpty()) {
            Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":v1", new AttributeValue().withS(category));

            DynamoDBQueryExpression<Poster> queryExpression = new DynamoDBQueryExpression<Poster>()
                    .withIndexName("CategoryIndex")
                    .withConsistentRead(false) // GSIs are eventually consistent
                    .withKeyConditionExpression("category = :v1")
                    .withExpressionAttributeValues(eav);

            List<Poster> postersWithKeys = dynamoDBMapper.query(Poster.class, queryExpression);

            posterList =  postersWithKeys.stream()
                    .map(poster -> dynamoDBMapper.load(Poster.class, poster.getPosterId()))
                    .collect(Collectors.toList());

        } else {
            List<Poster> scannedPosters = dynamoDBMapper.scan(Poster.class, new DynamoDBScanExpression());
            posterList = scannedPosters.stream()
                    .map(Poster::getPosterId) // Get the IDs of the scanned posters
                    .map(posterId -> dynamoDBMapper.load(Poster.class, posterId)) // Load the full object by ID
                    .collect(Collectors.toList());
        }

        logger.info("Fetched {} posters before pre-signing.", posterList.size());
        for (Poster poster : posterList) {
            if (poster.getImageUrl() != null && !poster.getImageUrl().isEmpty()) {
                String objectKey = poster.getImageUrl().substring(poster.getImageUrl().lastIndexOf("/") + 1);
                java.util.Date expiration = new java.util.Date();
                long expTimeMillis = expiration.getTime();
                expTimeMillis += 1000 * 60 * 5; // URL valid for 5 minutes
                expiration.setTime(expTimeMillis);

                GeneratePresignedUrlRequest generatePresignedUrlRequest =
                        new GeneratePresignedUrlRequest(bucketName, objectKey)
                                .withMethod(HttpMethod.GET)
                                .withExpiration(expiration);

                URL presignedUrl = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
                logger.info("Generated preSigned URL : {}",presignedUrl.toString());
                poster.setImageUrl(presignedUrl.toString()); // Replace with pre-signed URL
            }else{
                logger.warn("Poster ID: {} has no Image URl.", poster.getPosterId());
            }
        }
        logger.info("Returning {} posters with (potentially) presigned URLs.",posterList.size());

        return posterList;
    }

    public Poster updatePoster(Poster poster) {
        dynamoDBMapper.save(poster); // The save method will update an existing item if the hash key (posterId) is present
        return poster;
    }

    public void deletePoster(String posterId) {
        Poster posterToDelete = new Poster();
        posterToDelete.setPosterId(posterId);
        dynamoDBMapper.delete(posterToDelete);
    }


    // You can add methods for listing, updating, and deleting posters later
}
