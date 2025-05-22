package com.sirius.posterworld.utils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeConverter implements DynamoDBTypeConverter<String, LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public String convert(LocalDateTime attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.format(FORMATTER);
    }

    @Override
    public LocalDateTime unconvert(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dbData, FORMATTER);
    }

//    @Override
//    public LocalDateTime unconvert(String dbData) {
//        if (dbData == null || dbData.isEmpty()) {
//            return null;
//        }
//        try {
//            return LocalDateTime.parse(dbData);
//        } catch (Exception e) {
//            // If default parsing fails, try with your formatter (or a more lenient one)
//            return LocalDateTime.parse(dbData, FORMATTER);
//        }
//    }

}
