package com.example.simpleMessenger.controller;

import com.example.simpleMessenger.dto.*;
import com.example.simpleMessenger.entity.Status;
import com.example.simpleMessenger.entity.User;
import com.example.simpleMessenger.mapper.UserMapper;
import com.example.simpleMessenger.repository.UserRepository;
import com.example.simpleMessenger.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequestMapping("/user")
@RestController
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserMapper userMapper, UserRepository userRepository) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
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
    public ResponseEntity<List<UserListDto>> getAllUsers(Principal principal) {
        return ResponseEntity.ok(
                userService.getAllUsersExceptMe(principal.getName())
        );
    }


    @GetMapping("/profile")
    public UserProfileDto getProfile() {
        return userService.getCurrentUserProfile();
    }

    @PatchMapping("/profile")
    public UserProfileDto updateProfile(@Valid @RequestBody UpdateUserDto updateUserDto) {
        return userService.updateProfile(updateUserDto);
    }

//    @GetMapping("/users/online")
//    public List<UserDto> getOnlineUsers() {
//        return userService.findAllOnlineUsers()
//                .stream()
//                .map(userMapper::toDto)
//                .toList();
//    }


}