package com.example.simpleMessenger.controller;

import com.example.simpleMessenger.dto.UserRegisterDto;
import com.example.simpleMessenger.dto.UpdateUserDto;
import com.example.simpleMessenger.dto.UserProfileDto;
import com.example.simpleMessenger.dto.UserResponseDto;
import com.example.simpleMessenger.entity.User;
import com.example.simpleMessenger.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/user")
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/registration")
    public UserResponseDto createUser(@Valid @RequestBody UserRegisterDto registerDto) {
        return userService.saveUser(registerDto);
    }

    @MessageMapping("/user.disconnectUser")
    @SendTo("/topic/public")
    public User disconnectUser(@Payload User user){
        userService.disconnect(user);
        return user;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> findConnectedUsers(){
        return ResponseEntity.ok(userService.findAllByStatus());
    }

    @GetMapping("/profile")
    public UserProfileDto getProfile() {
        return userService.getCurrentUserProfile();
    }

    @PatchMapping("/profile")
    public UserProfileDto updateProfile(@Valid @RequestBody UpdateUserDto updateUserDto) {
        return userService.updateProfile(updateUserDto);
    }


}