package com.sirius.posterworld.controllers;

import com.sirius.posterworld.models.dto.OrderDto;
import com.sirius.posterworld.models.Order;
import com.sirius.posterworld.security.CustomUserDetails;
import com.sirius.posterworld.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderHistoryController {

    private final OrderService orderService;

    @Autowired
    public OrderHistoryController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/history")
    public ResponseEntity<List<OrderDto>> getOrderHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        String userId;

        if (principal instanceof CustomUserDetails) {
            userId = ((CustomUserDetails) principal).getUserId();
            System.out.println("Fetching order history for userId: " + userId); // Should log "user101"
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        List<Order> orders = orderService.getOrdersByUserId(userId);

        List<OrderDto> orderDtos = orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(orderDtos, HttpStatus.OK);
    }

    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setOrderDate(order.getOrderDate());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setItems(order.getItems());
        dto.setTotalAmount(order.getTotalAmount());
        return dto;
    }
}
