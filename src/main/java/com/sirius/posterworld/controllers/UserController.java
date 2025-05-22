package com.sirius.posterworld.controllers;

 // Assuming you'll create a 'controller' package


import com.sirius.posterworld.models.User;
import com.sirius.posterworld.models.dto.UserProfile;
import com.sirius.posterworld.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }





    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody User user) {
        user.setRegistrationDate(LocalDateTime.now()); // Set registration date on the server
        User savedUser = userService.saveUser(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @GetMapping("/profile/{userId}")
    @PreAuthorize("#userId == principal.userId")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable String userId) {
        UserProfile userProfile = userService.getUserProfileById(userId);
        if (userProfile != null) {
            return new ResponseEntity<>(userProfile, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

//    @GetMapping("/profile/{username}")
//    @PreAuthorize("#username == principal.username")
//    public ResponseEntity<UserProfile> getUserProfileByUsername(@PathVariable String username) {
//        User user = userService.getUserByUsername(username); // Use getUserByUsername
//        System.out.println("User object loaded in Controller: " + user); // Add this log
//
//        if (user != null) {
//            UserProfile userProfile = new UserProfile(
//                    user.getUserId(), // Assuming you still need userId in the DTO
//                    user.getUsername(),
//                    user.getEmail(),
//                    user.getRoles(),
//                    user.getRegistrationDate(),
//                    user.getShippingAddresses()
//            );
//            return new ResponseEntity<>(userProfile, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
}
