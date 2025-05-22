package com.sirius.posterworld.controllers;

import com.razorpay.Utils;
import com.sirius.posterworld.models.Order;
import com.sirius.posterworld.services.OrderService;
import com.sirius.posterworld.services.PaymentService;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Value("${razorpay.keySecret}")
    private String keySecret;

    @PostMapping("/create-razorpay-payment-order")
    public ResponseEntity<Map<String, String>> createRazorpayOrder(@RequestBody Map<String, String> payload) {
        String orderId = payload.get("orderId");
        logger.info("Received request to create Razorpay order for orderId: {}", orderId);
        if (orderId == null || orderId.isEmpty()) {
            logger.warn("Order ID is missing or empty in the request.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Order order = orderService.getOrderById(orderId);
        logger.info("Retrieved order from database: {}", order);
        if (order == null) {
            logger.warn("Order not found with ID: {}", orderId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        logger.info("Attempting to create Razorpay order with amount: {}", order.getTotalAmount());
        try {
            com.razorpay.Order razorpayOrder = paymentService.createRazorpayOrder(order.getTotalAmount());
            String razorpayOrderId = razorpayOrder.get("id").toString();
            order.setRazorpayOrderId(razorpayOrderId); // Save Razorpay Order ID to your Order object
            orderService.saveOrder(order); // Use the overloaded saveOrder method to update the existing order
            logger.info("Successfully created Razorpay order with ID: {}", razorpayOrder.get("id").toString());
            Map<String, String> response = Map.of("razorpayOrderId", razorpayOrder.get("id").toString());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RazorpayException e) {
            logger.error("Error creating Razorpay order: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PostMapping("/verify-payment")
    public ResponseEntity<Map<String, String>> verifyPaymentSignature(@RequestBody Map<String, String> payload) {
        String razorpayOrderId = payload.get("razorpay_order_id");
        String razorpayPaymentId = payload.get("razorpay_payment_id");
        String razorpaySignature = payload.get("razorpay_signature");

        logger.info("Received payment verification request for Order ID: {}, Payment ID: {}", razorpayOrderId, razorpayPaymentId);

        if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
            logger.warn("Missing required parameters for payment verification.");
            return new ResponseEntity<>(Map.of("success", "false", "error", "Missing required parameters"), HttpStatus.BAD_REQUEST);
        }

        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);

            logger.info("Verifying signature with: Key Secret={}, Order ID={}, Payment ID={}, Signature={}",
                    keySecret, razorpayOrderId, razorpayPaymentId, razorpaySignature);

            boolean isSignatureValid = Utils.verifyPaymentSignature(attributes, keySecret);

            if (isSignatureValid) {
                logger.info("Payment signature verification successful for Payment ID: {}", razorpayPaymentId);
                Order order = orderService.getOrderByRazorpayOrderId(razorpayOrderId);
                if (order != null) {
                    orderService.updateOrderStatus(order.getOrderId(), razorpayPaymentId, "paid");
                    logger.info("Order status updated to 'paid' for Order ID: {}", order.getOrderId());
                    return new ResponseEntity<>(Map.of("success", "true", "paymentId", razorpayPaymentId, "orderId", order.getOrderId()), HttpStatus.OK);
                } else {
                    logger.warn("Could not find internal order for Razorpay Order ID: {}", razorpayOrderId);
                    return new ResponseEntity<>(Map.of("success", "false", "error", "Could not find internal order for Razorpay ID"), HttpStatus.NOT_FOUND);
                }

            }

            else {
                logger.error("Payment signature verification failed for Payment ID: {}", razorpayPaymentId);
                return new ResponseEntity<>(Map.of("success", "false", "error", "Payment signature verification failed"), HttpStatus.BAD_REQUEST);
            }




        } catch (RazorpayException e) {
            logger.error("Error during payment signature verification: {}", e.getMessage(), e);
            return new ResponseEntity<>(Map.of("success", "false", "error", "Error during signature verification"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}


