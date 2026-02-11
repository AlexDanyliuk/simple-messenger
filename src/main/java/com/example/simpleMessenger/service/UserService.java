package com.example.simpleMessenger.service;


import com.example.simpleMessenger.dto.*;
import com.example.simpleMessenger.entity.User;

import javax.naming.AuthenticationException;
import java.util.List;


public interface UserService {

    JwtAuthenticationDto singIn(UserCredentialsDto userCredentialsDto) throws AuthenticationException;;
    JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws Exception ;

    UserResponseDto saveUser(UserRegisterDto registerDto);

    void disconnect(User user);

    UserProfileDto updateProfile(UpdateUserDto updateUserDto);

    UserProfileDto getCurrentUserProfile();

    List<User> findAllByStatus();


}
