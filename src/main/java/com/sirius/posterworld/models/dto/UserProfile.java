package com.sirius.posterworld.models.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private String userId;
    private String username;
    private String email;
    private Set<String> roles;
    private LocalDateTime registrationDate;
    private List<String> shippingAddresses;
}
