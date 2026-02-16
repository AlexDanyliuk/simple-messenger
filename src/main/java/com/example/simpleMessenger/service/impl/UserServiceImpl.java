package com.example.simpleMessenger.service.impl;
import com.example.simpleMessenger.dto.*;
import com.example.simpleMessenger.entity.Status;
import com.example.simpleMessenger.entity.User;
import com.example.simpleMessenger.exceptionHandler.EmailAlreadyExistsException;
import com.example.simpleMessenger.exceptionHandler.UsernameAlreadyExistsException;
import com.example.simpleMessenger.mapper.UserMapper;
import com.example.simpleMessenger.repository.UserRepository;
import com.example.simpleMessenger.security.jwt.JwtService;
import com.example.simpleMessenger.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public JwtAuthenticationDto singIn(UserCredentialsDto userCredentialsDto) throws AuthenticationException {
        User user = findByCredentials(userCredentialsDto);
        return jwtService.generateAuthToken(user.getEmail());
    }

    @Override
    public JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws Exception {
        String refreshToken = refreshTokenDto.getRefreshToken();
        if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {
            User user = findUserByEmail(jwtService.getEmailFromToken(refreshToken));
            return jwtService.refreshBaseToken(user.getEmail(), refreshToken);
        }
        throw new  AuthenticationException("Invalid refresh token");
    }

    @Override
    @Transactional
    public UserResponseDto saveUser(UserRegisterDto registerDto) {
        User user = userMapper.toEntity(registerDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new EmailAlreadyExistsException(registerDto.getEmail());
        }
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new UsernameAlreadyExistsException(registerDto.getUsername());
        }
        user.setStatus(Status.ONLINE);
        User saved = userRepository.save(user);
        return userMapper.toUserResponseDto(saved);
    }

    @Override
    public void disconnect(User user) {
        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());

        if (optionalUser.isPresent()) {
            User storedUser = optionalUser.get();

            if (storedUser.getStatus() == Status.ONLINE) {
                storedUser.setStatus(Status.OFFLINE);
                userRepository.save(storedUser);
            }
        }
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(UpdateUserDto updateUserDto) {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String email = ((UserDetails) principal).getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateUserDto.getFullName() != null) {
            user.setFullName(updateUserDto.getFullName());
        }

        if (updateUserDto.getAvatarUrl() != null) {
            user.setAvatarUrl(updateUserDto.getAvatarUrl());
        }

        if (updateUserDto.getUsername() != null) {
            validateUsername(updateUserDto.getUsername(), user.getUsername());
            user.setUsername(updateUserDto.getUsername());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toProfileDto(updatedUser);
    }

    @Override
    public List<User> findAllByStatus() {

        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String currentEmail = ((UserDetails) principal).getUsername();

        return userRepository.findAllByStatus(Status.ONLINE)
                .stream()
                .filter(user -> !user.getEmail().equals(currentEmail))
                .toList();
    }

    @Override
    public UserProfileDto getCurrentUserProfile() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String email = ((UserDetails) principal).getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.toProfileDto(user);
    }

    private void validateUsername(String newUsername, String currentUsername) {
        if (!newUsername.equals(currentUsername)) {
            boolean exists = userRepository.existsByUsername(newUsername);
            if (exists) {
                throw new RuntimeException("Username already taken");
            }
        }
    }

    private User findByCredentials(UserCredentialsDto userCredentialsDto) throws AuthenticationException {
        Optional<User> optionalUser = userRepository.findByEmail(userCredentialsDto.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(userCredentialsDto.getPassword(), user.getPassword())) {
                return user;
            }
        }
        throw new AuthenticationException("Email or password is not correct");
    }

    private User findUserByEmail(String email) throws Exception{
        return userRepository.findByEmail(email).orElseThrow(() -> new Exception("User not found"));
    }
}
