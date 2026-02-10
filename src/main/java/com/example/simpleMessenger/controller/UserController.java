package com.example.simpleMessenger.controller;


import com.example.simpleMessenger.dto.UserDto;
import com.example.simpleMessenger.entity.User;
import com.example.simpleMessenger.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
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
    public User createUser(@RequestBody UserDto userDto) {
        return userService.saveUser(userDto);
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

    @GetMapping("current-user")
    public ResponseEntity<User> getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    //    @MessageMapping("/user.addUser")
//    @SendTo("/topic/public")
//    public User addUser(@Payload UserDto userDto){
//        User existingUser = userService.findByUsername(userDto.getUsername());
//        if(existingUser == null){
//            userService.saveUser(userDto);
//        }
//        return userService.saveUser(userDto);
//    }

}