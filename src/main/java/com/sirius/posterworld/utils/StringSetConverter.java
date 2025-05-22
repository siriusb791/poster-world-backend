package com.sirius.posterworld.utils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StringSetConverter implements DynamoDBTypeConverter<String, Set<String>> {

    private static final String DELIMITER = ",";

    @Override
    public String convert(Set<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return String.join(DELIMITER, attribute);
    }

    @Override
    public Set<String> unconvert(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(dbData.split(DELIMITER)));
    }
}
