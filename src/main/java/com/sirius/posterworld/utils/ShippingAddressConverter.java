package com.sirius.posterworld.utils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.sirius.posterworld.models.ShippingAddress;
import java.util.HashMap;
import java.util.Map;

public class ShippingAddressConverter implements DynamoDBTypeConverter<Map<String, AttributeValue>, ShippingAddress> {

    @Override
    public Map<String, AttributeValue> convert(ShippingAddress shippingAddress) {
        if (shippingAddress == null) {
            return null;
        }
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("addressLine1", new AttributeValue().withS(shippingAddress.getAddressLine1()));
        map.put("addressLine2", new AttributeValue().withS(shippingAddress.getAddressLine2()));
        map.put("city", new AttributeValue().withS(shippingAddress.getCity()));
        map.put("state", new AttributeValue().withS(shippingAddress.getState()));
        map.put("postalCode", new AttributeValue().withS(shippingAddress.getPostalCode()));
        map.put("country", new AttributeValue().withS(shippingAddress.getCountry()));
        return map;
    }

    @Override
    public ShippingAddress unconvert(Map<String, AttributeValue> attributeValue) {
        if (attributeValue == null || attributeValue.isEmpty()) {
            return null;
        }
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setAddressLine1(attributeValue.get("addressLine1").getS());
        shippingAddress.setAddressLine2(attributeValue.get("addressLine2") != null ? attributeValue.get("addressLine2").getS() : null);
        shippingAddress.setCity(attributeValue.get("city").getS());
        shippingAddress.setState(attributeValue.get("state").getS());
        shippingAddress.setPostalCode(attributeValue.get("postalCode").getS());
        shippingAddress.setCountry(attributeValue.get("country").getS());
        return shippingAddress;
    }
}
