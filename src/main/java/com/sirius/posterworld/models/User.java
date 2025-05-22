package com.sirius.posterworld.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.sirius.posterworld.utils.LocalDateTimeConverter;
import com.sirius.posterworld.utils.StringSetConverter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "Users")
public class User {

    @DynamoDBHashKey(attributeName = "userId")
    private String userId;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @DynamoDBAttribute(attributeName = "username")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @DynamoDBAttribute(attributeName = "password")
    private String password;

    @Email(message = "Invalid email format")
    @DynamoDBAttribute(attributeName = "email")
    private String email;

    @DynamoDBAttribute(attributeName = "roles")
    @DynamoDBTypeConverted(converter = StringSetConverter.class)
    private Set<String> roles;

    @DynamoDBAttribute(attributeName = "registrationDate")
    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    private LocalDateTime registrationDate;

    @DynamoDBAttribute(attributeName = "shippingAddresses")
    private List<String> shippingAddresses; // For simplicity, storing as strings for now
}
