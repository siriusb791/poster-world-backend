package com.sirius.posterworld.services;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import com.sirius.posterworld.models.CartItem;
import com.sirius.posterworld.models.Order;
import com.sirius.posterworld.models.Poster;
import com.sirius.posterworld.models.ShippingAddress;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final AmazonDynamoDB amazonDynamoDB;
    private final DynamoDBMapper dynamoDBMapper;
    private final PosterService posterService; // To fetch poster details for total calculation (optional for now)

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    public OrderService(AmazonDynamoDB amazonDynamoDB, PosterService posterService) {
        this.amazonDynamoDB = amazonDynamoDB;
        this.dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
        this.posterService = posterService;
    }

    @Value("${aws.dynamodb.table.orders:Orders}") // Default to "Orders" if not configured
    private String ordersTableName;


    @PostConstruct
    public void init() {
        try {
            amazonDynamoDB.describeTable(new DescribeTableRequest().withTableName(ordersTableName));
            logger.info("Table {} exists.", ordersTableName);
        } catch (ResourceNotFoundException e) {
            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(Order.class);
            tableRequest.setTableName(ordersTableName);
            // Set provisioned throughput - adjust as needed
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));

            // Add Global Secondary Index for userId (if not already defined in the model annotations)

            if (tableRequest.getGlobalSecondaryIndexes() != null) {
                for (GlobalSecondaryIndex gsi : tableRequest.getGlobalSecondaryIndexes()) {
                    if (gsi.getIndexName().equals("UserIdIndex")) {
                        gsi.withProvisionedThroughput(new ProvisionedThroughput(5L, 5L)); // Set ProvisionedThroughput for the index
                        break;
                    }
                }
            }

//            GlobalSecondaryIndex userIdIndex = new GlobalSecondaryIndex()
//                        .withIndexName("UserIdIndex")
//                        .withKeySchema(new KeySchemaElement("userId", KeyType.HASH))
//                        .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
//                        .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
//
//            tableRequest.withGlobalSecondaryIndexes(userIdIndex);

            logger.debug("CreateTableRequest (from annotation): {}",tableRequest);




            amazonDynamoDB.createTable(tableRequest);
            logger.info("Table {} created successfully.", ordersTableName);
        } catch (Exception e) {
            logger.error("Error creating table {}: {}", ordersTableName, e.getMessage(), e);
        }
    }

    public Order saveOrder(String userId, ShippingAddress shippingAddress, List<CartItem> items) {
        Order order = new Order();
        order.setUserId(userId);
        order.setShippingAddress(shippingAddress);
        order.setItems(items);
        order.setTotalAmount(calculateTotal(items)); // Implement total calculation

        order.setStatus("pending_payment");

//        dynamoDBMapper.save(order);
//        return order;
        try {
            dynamoDBMapper.save(order);
            return order;
        } catch (Exception e) {
            System.err.println("Error saving order: " + e.getMessage());
            e.printStackTrace();
            return null; // Or throw the exception
        }
    }

    public void saveOrder(Order order) { // Overloaded method for updates
        try {
            dynamoDBMapper.save(order);
        } catch (Exception e) {
            System.err.println("Error updating order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Order getOrderById(String orderId) {
        return dynamoDBMapper.load(Order.class, orderId);
    }

    // You can add methods for querying orders by user, etc., later
    private double calculateTotal(List<CartItem> items) {
        double total = 0.0;
        for (CartItem item : items) {
            Poster poster = posterService.getPosterById(item.getPosterId());
            if (poster != null) {
                total += poster.getPrice() * item.getQuantity();
            } else {
                System.err.println("Poster not found: " + item.getPosterId());
                // Consider how you want to handle this: skip the item, throw an error, etc.
                // For now, we'll just skip it.
            }
        }
        return total;
    }

    public Order getOrderByRazorpayOrderId(String razorpayOrderId) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":razorpayOrderId", new AttributeValue().withS(razorpayOrderId));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("razorpayOrderId = :razorpayOrderId")
                .withExpressionAttributeValues(expressionAttributeValues);

        List<Order> results = dynamoDBMapper.scan(Order.class, scanExpression);
        return results.isEmpty() ? null : results.get(0); // Assuming razorpayOrderId is unique per order
    }
    public void updateOrderStatus(String orderId, String paymentId, String status) {
        Order order = dynamoDBMapper.load(Order.class, orderId);
        if (order != null) {
            if (paymentId != null) {
                order.setPaymentId(paymentId);
            }
            order.setStatus(status); // Use orderStatus instead of status
            dynamoDBMapper.save(order);
            System.out.println("Order ID: " + orderId + " updated with status: " + status + (paymentId != null ? " and Payment ID: " + paymentId : ""));
        } else {
            System.out.println("Order not found with ID: " + orderId);
        }
    }

//    public void updateOrderStatus(String orderId, String paymentId, String status) {
//        Order order = dynamoDBMapper.load(Order.class, orderId);
//        if (order != null) {
//            order.setPaymentId(paymentId);
//            order.setStatus(status);
//            dynamoDBMapper.save(order);
//            System.out.println("Order ID: " + orderId + " updated with status: " + status + " and Payment ID: " + paymentId);
//        } else {
//            System.out.println("Order not found with ID: " + orderId);
//        }
//    }


//    private double calculateTotal(List<CartItem> items) {
//        double total = 0.0;
//        // In a real application, you would fetch the price of each poster
//        // from the PosterService based on the posterId and multiply by the quantity.
//        // For this basic implementation, we'll just return 0.0.
//        // Example (requires fetching poster prices):
//        /*
//        for (CartItem item : items) {
//            Poster poster = posterService.getPosterById(item.getPosterId());
//            if (poster != null) {
//                total += poster.getPrice() * item.getQuantity();
//            }
//        }
//        */
//        return total;
//    }

    public List<Order> getOrdersByUserId(String userId) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":userId", new AttributeValue().withS(userId));

        DynamoDBQueryExpression<Order> queryExpression = new DynamoDBQueryExpression<Order>()
                .withIndexName("UserIdIndex")
                .withKeyConditionExpression("userId = :userId")
                .withExpressionAttributeValues(expressionAttributeValues)
                .withConsistentRead(false); // Assuming userId is not the primary key

        return dynamoDBMapper.query(Order.class, queryExpression);
    }
}
