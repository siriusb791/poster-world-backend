package com.sirius.posterworld.controllers;

import com.sirius.posterworld.models.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final ShoppingCart shoppingCart;

    @Autowired
    public CartController(ShoppingCart shoppingCart) {

        this.shoppingCart = shoppingCart;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addItem(@RequestParam String posterId, @RequestParam(defaultValue = "1") int quantity) {
        shoppingCart.addItem(posterId, quantity);
        return new ResponseEntity<>("Item added to cart", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ShoppingCart> viewCart() {
        return new ResponseEntity<>(shoppingCart, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateItemQuantity(@RequestParam String posterId, @RequestParam int quantity) {
        shoppingCart.updateItemQuantity(posterId, quantity);
        return new ResponseEntity<>("Cart updated", HttpStatus.OK);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeItem(@RequestParam String posterId) {
        shoppingCart.removeItem(posterId);
        return new ResponseEntity<>("Item removed from cart", HttpStatus.OK);
    }
}
