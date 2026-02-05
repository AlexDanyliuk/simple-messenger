package com.example.simpleMessenger.User.contoller;


import com.example.simpleMessenger.User.entity.User;
import com.example.simpleMessenger.User.repository.UserRepository;
import com.example.simpleMessenger.User.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("/users")
    public ResponseEntity<List<User>> findConnectedUsers(){
        return ResponseEntity.ok(userService.findAllByStatus());
    }

    @PostMapping("/users")
    public ResponseEntity<User> addUser(@RequestBody User user) {
        userService.saveUser(user);
        return ResponseEntity.ok(user);
    }
}
