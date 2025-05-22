package com.sirius.posterworld.controllers;

import com.sirius.posterworld.models.Order;
import com.sirius.posterworld.models.ShippingAddress;
import com.sirius.posterworld.models.ShoppingCart;
import com.sirius.posterworld.security.CustomUserDetails;
import com.sirius.posterworld.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    private final ShoppingCart shoppingCart;
    private final OrderService orderService;

    @Autowired
    public CheckoutController(ShoppingCart shoppingCart,OrderService orderService) {
        this.shoppingCart = shoppingCart;
        this.orderService = orderService;
    }

    @PostMapping("/shipping-info")
    public ResponseEntity<String> submitShippingInfo(@RequestBody ShippingAddress shippingAddress) {

        System.out.println("Shipping Information Received:");
        System.out.println(shippingAddress);
        System.out.println("Items in cart at checkout: " + shoppingCart.getItems());

        shoppingCart.getItems().clear();

        return new ResponseEntity<>("Shipping information received. Order processing will follow.", HttpStatus.OK);
    }

    @PostMapping("/place-order")
    public ResponseEntity<Map<String, String>> placeOrder(@RequestBody ShippingAddress shippingAddress) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(Map.of("error", "User not authenticated."), HttpStatus.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        String userId;

        if (principal instanceof CustomUserDetails) {
            userId = ((CustomUserDetails) principal).getUserId();
            System.out.println("Saving order for userId: " + userId);
        } else {
            return new ResponseEntity<>(Map.of("error","Could not retrieve user ID."), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (shoppingCart.getItems().isEmpty()) {
            return new ResponseEntity<>(Map.of("error","Your cart is empty."), HttpStatus.BAD_REQUEST);
        }

        Order savedOrder = orderService.saveOrder(userId, shippingAddress, shoppingCart.getItems());
        if (savedOrder == null) {
            return new ResponseEntity<>(Map.of("error","Failed to save order."), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        shoppingCart.getItems().clear();

        return new ResponseEntity<>(Map.of("orderId", savedOrder.getOrderId()), HttpStatus.OK);
    }


}
