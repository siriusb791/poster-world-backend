package com.sirius.posterworld.utils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.sirius.posterworld.models.CartItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartItemListConverter implements DynamoDBTypeConverter<List<Map<String, AttributeValue>>, List<CartItem>> {

    @Override
    public List<Map<String, AttributeValue>> convert(List<CartItem> cartItems) {
        if (cartItems == null) {
            return null;
        }
        List<Map<String, AttributeValue>> list = new ArrayList<>();
        for (CartItem item : cartItems) {
            Map<String, AttributeValue> map = new HashMap<>();
            map.put("posterId", new AttributeValue().withS(item.getPosterId()));
            map.put("quantity", new AttributeValue().withN(String.valueOf(item.getQuantity())));
            list.add(map);
        }
        return list;
    }

    @Override
    public List<CartItem> unconvert(List<Map<String, AttributeValue>> attributeValue) {
        if (attributeValue == null || attributeValue.isEmpty()) {
            return null;
        }
        List<CartItem> list = new ArrayList<>();
        for (Map<String, AttributeValue> map : attributeValue) {
            CartItem item = new CartItem();
            item.setPosterId(map.get("posterId").getS());
            item.setQuantity(Integer.parseInt(map.get("quantity").getN()));
            list.add(item);
        }
        return list;
    }
}
