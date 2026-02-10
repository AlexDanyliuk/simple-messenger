package com.example.simpleMessenger.service;


import com.example.simpleMessenger.dto.JwtAuthenticationDto;
import com.example.simpleMessenger.dto.RefreshTokenDto;
import com.example.simpleMessenger.dto.UserCredentialsDto;
import com.example.simpleMessenger.dto.UserDto;
import com.example.simpleMessenger.entity.User;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;


public interface UserService {

    JwtAuthenticationDto singIn(UserCredentialsDto userCredentialsDto) throws AuthenticationException;;
    JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws Exception ;

    User saveUser(UserDto userDto);

    void disconnect(User user);

    List<User> findAllByStatus();

    User findByUsername(String username);

    Optional<User> getUserByEmail(String email);
    String addUser(UserDto user);

}
