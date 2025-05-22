package com.sirius.posterworld.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.sirius.posterworld.utils.LocalDateTimeConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "Posters") // You might want to configure this via properties later
public class Poster {

    @DynamoDBHashKey
    private String posterId;

    @DynamoDBAttribute
    private String title;

    @DynamoDBAttribute
    private String description;

    @DynamoDBAttribute
    private String imageUrl;

    @DynamoDBAttribute
    private Double price;

    @DynamoDBAttribute
    private String category;

    @DynamoDBAttribute
    private String artist;

    @DynamoDBAttribute
    private String dimensions;

    @DynamoDBAttribute
    private Integer stockQuantity;

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    private LocalDateTime uploadDate;

    // You can add more attributes as needed (e.g., tags, rating)
}
