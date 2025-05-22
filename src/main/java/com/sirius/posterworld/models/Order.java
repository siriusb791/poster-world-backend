package com.sirius.posterworld.models;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.sirius.posterworld.utils.CartItemListConverter;
import com.sirius.posterworld.utils.LocalDateTimeConverter;
import com.sirius.posterworld.utils.ShippingAddressConverter;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@DynamoDBTable(tableName = "Orders")
public class Order {

    @DynamoDBHashKey
    private String orderId;

    @DynamoDBAttribute
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "UserIdIndex", attributeName = "userId")
    private String userId; // ID of the user who placed the order

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    private LocalDateTime orderDate;

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = ShippingAddressConverter.class)
    private ShippingAddress shippingAddress;

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = CartItemListConverter.class)
    private List<CartItem> items;

    @DynamoDBAttribute
    private double totalAmount;// Total amount of the order

    @DynamoDBAttribute
    private String razorpayOrderId; // For storing the Razorpay Order ID

    @DynamoDBAttribute
    private String paymentId;     // For storing the Razorpay Payment ID

    @DynamoDBAttribute
    private String status;        // For storing the order status (e.g., "pending", "paid")

    public Order() {
        this.orderId = UUID.randomUUID().toString(); // Generate a unique order ID on creation
        this.orderDate = LocalDateTime.now();
    }

    // Constructors with arguments can be added if needed
}
