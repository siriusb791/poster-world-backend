package com.sirius.posterworld.services;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Value("${razorpay.keyId}")
    private String keyId;

    @Value("${razorpay.keySecret}")
    private String keySecret;

    public Order createRazorpayOrder(double amountInRupees) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInRupees * 100); // Amount in paise
        orderRequest.put("currency", "INR");
        // Optional:
        // orderRequest.put("receipt", "your_receipt_id_" + System.currentTimeMillis());
        // orderRequest.put("notes", new JSONObject().put("key1", "value1").put("key2", "value2"));

        return client.orders.create(orderRequest);
    }
}
