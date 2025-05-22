package com.sirius.posterworld.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String posterId;
    private int quantity;
    // You might want to include other information like poster name, price, image URL
    // for easier display in the cart. You can fetch this from the Poster service.
}
