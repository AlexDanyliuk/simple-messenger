package com.example.simpleMessenger.User.service;


import com.example.simpleMessenger.User.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserService {

    void saveUser(User user);

    void disconnect(User user);

    List<User> findAllByStatus();

    User findAllByUsername(String username);


}
