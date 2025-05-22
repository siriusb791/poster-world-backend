package com.sirius.posterworld.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Data
public class ShoppingCart {
    private List<CartItem> items = new ArrayList<>();

    public void addItem(String posterId, int quantity) {
        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getPosterId().equals(posterId))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            items.add(new CartItem(posterId, quantity));
        }
    }

    public void updateItemQuantity(String posterId, int quantity) {
        items.stream()
                .filter(item -> item.getPosterId().equals(posterId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));
    }

    public void removeItem(String posterId) {
        items.removeIf(item -> item.getPosterId().equals(posterId));
    }

    public double getTotal() {
        // In a real application, you would fetch the prices from the Poster service
        // and calculate the total based on the quantities. For now, we'll just return 0.0.
        return 0.0;
    }

    @JsonIgnore
    public Object getTargetSource() throws Exception {
        if (AopUtils.isAopProxy(this) && this instanceof Advised advised) {
            return advised.getTargetSource();
        }
        return null;
    }

//    @JsonIgnore
//    public Object getTargetSource() throws Exception {
//        if (AopUtils.isAopProxy(this) && this instanceof Advised) {
//            Advised advised = (Advised) this;
//            return advised.getTargetSource();
//        }
//        return null;
//    }
}
