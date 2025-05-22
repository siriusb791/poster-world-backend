package com.sirius.posterworld.security;

import com.sirius.posterworld.models.User;
import com.sirius.posterworld.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // In a real application, you might want to allow login by username or email
        // For now, let's assume we're using username as the identifier
        User user = userService.getUserByUsername(username); // We need to implement this in UserService

        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role-> new SimpleGrantedAuthority("ROLE_"+role))
                .toList();

        String userId = user.getUserId(); // Assuming getId() returns the unique ID
        System.out.println("Authenticated user: " + username + ", with userId: " + userId); // Add this line

        return new CustomUserDetails(user.getUserId(),
                user.getUsername(),
                user.getPassword(),authorities);

//        return new org.springframework.security.core.userdetails.User(
//                user.getUsername(),
//                user.getPassword(),
//                getAuthorities(user.getRoles())
//        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Set<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // Spring Security expects roles to be prefixed with ROLE_
                .collect(Collectors.toList());
    }
}
