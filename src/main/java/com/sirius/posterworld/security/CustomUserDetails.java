package com.sirius.posterworld.security;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {
    private final String userId;

    public CustomUserDetails(String userId, String username, String password, Collection<?
                extends GrantedAuthority> authorities) {
        super(username, password, true, true, true, true,
                authorities);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
