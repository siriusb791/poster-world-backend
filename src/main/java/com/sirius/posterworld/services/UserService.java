package com.sirius.posterworld.services;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import com.sirius.posterworld.configs.DynamoDBConfig;
import com.sirius.posterworld.models.User;
import com.sirius.posterworld.models.dto.UserProfile;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private AmazonDynamoDB amazonDynamoDB;
    private DynamoDBMapper dynamoDBMapper;

    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${aws.dynamodb.table}")
    private String tableName; // It's good practice to define table name as a constant

    @Autowired
    public UserService(AmazonDynamoDB amazonDynamoDB, BCryptPasswordEncoder passwordEncoder) {
        this.amazonDynamoDB = amazonDynamoDB;
        this.dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
        this.passwordEncoder = passwordEncoder;

    }



//    @PostConstruct
//    public void init() {
//        try {
//            amazonDynamoDB.describeTable(new DescribeTableRequest().withTableName(tableName));
//            System.out.println("Table " + tableName + " exists.");
//        } catch (ResourceNotFoundException e) {
//            // Table doesn't exist, create it
//            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(User.class);
//            tableRequest.setTableName(tableName); // Ensure table name is set in the creation request
//            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L)); // Adjust as needed
//
//            // Add logging to inspect the CreateTableRequest
//            System.out.println("CreateTableRequest Attribute Definitions:");
//            if (tableRequest.getAttributeDefinitions() != null) {
//                tableRequest.getAttributeDefinitions().forEach(attrDef ->
//                        System.out.println("  - " + attrDef.getAttributeName() + ": " + attrDef.getAttributeType()));
//            }
//            System.out.println("CreateTableRequest Key Schema:");
//            if (tableRequest.getKeySchema() != null) {
//                tableRequest.getKeySchema().forEach(keySchemaElement ->
//                        System.out.println("  - " + keySchemaElement.getAttributeName() + ": " + keySchemaElement.getKeyType()));
//            }
//
//            amazonDynamoDB.createTable(tableRequest);
//            System.out.println("Table " + tableName + " created successfully.");
//        }
//    }

    @PostConstruct
    public void init() {
        try {
            amazonDynamoDB.describeTable(new DescribeTableRequest().withTableName(tableName));
            System.out.println("Table " + tableName + " exists.");
        } catch (ResourceNotFoundException e) {
            // Table doesn't exist, create it
            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(User.class);
            tableRequest.setTableName(tableName); // Ensure table name is set
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L)); // Adjust as needed
            amazonDynamoDB.createTable(tableRequest);
            System.out.println("Table " + tableName + " created successfully.");
        }
        dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
    }

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        dynamoDBMapper.save(user);
        return user;
    }

    public User getUserById(String userId) {
        return dynamoDBMapper.load(User.class, userId);
    }

    public UserProfile getUserProfileById(String userId) {
        User user = dynamoDBMapper.load(User.class, userId);
        System.out.println("Retrieved User object: " + user);
        if (user != null) {
            return new UserProfile(
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoles(),
                    user.getRegistrationDate(),
                    user.getShippingAddresses()
            );
        }
        return null;
    }

    public UserProfile getUserProfileByUsername(String username) {
        User user = getUserByUsername(username);// You already have this method
        System.out.println("Retrieved User object: " + user);
        if (user != null) {
            return new UserProfile(
                    user.getUserId(), // You might still need the ID in the DTO
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoles(),
                    user.getRegistrationDate(),
                    user.getShippingAddresses()
            );
        }
        return null;
    }


    public User getUserByUsername(String username) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":v1", new AttributeValue().withS(username));

        DynamoDBQueryExpression<User> queryExpression = new DynamoDBQueryExpression<User>()
                .withIndexName("UsernameIndexV2")
                .withConsistentRead(false) // GSIs are eventually consistent
                .withKeyConditionExpression("username = :v1")
                .withExpressionAttributeValues(eav);

        return dynamoDBMapper.query(User.class, queryExpression).stream().findFirst().orElse(null);
    }

    // You can add more methods here for updating and deleting users
}
