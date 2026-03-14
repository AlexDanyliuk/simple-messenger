package com.example.simpleMessenger.controller;

import com.example.simpleMessenger.dto.*;
import com.example.simpleMessenger.entity.User;
import com.example.simpleMessenger.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
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
    public ResponseEntity<List<UserListDto>> getAllUsers(Principal principal) {
        return ResponseEntity.ok(
                userService.getAllUsersExceptMe(principal.getName())
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserListDto>> searchUsers(
            @RequestParam String q,
            Principal principal) {
        if (q == null || q.trim().length() < 3) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(
                userService.searchByUsername(q, principal.getName())
        );
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<UserListDto>> getConversations() {
        return ResponseEntity.ok(userService.getUsersWithConversations());
    }

    @PostMapping("/avatar")
    public ResponseEntity<UserProfileDto> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(userService.uploadAvatar(file));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/profile")
    public UserProfileDto getProfile() {
        return userService.getCurrentUserProfile();
    }

    @GetMapping("/{id}")
    public UserProfileDto getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PatchMapping("/profile")
    public UserProfileDto updateProfile(@Valid @RequestBody UpdateUserDto updateUserDto) {
        return userService.updateProfile(updateUserDto);
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) throws Exception {
        userService.changePassword(changePasswordDto);
        return ResponseEntity.ok().build();
    }


}