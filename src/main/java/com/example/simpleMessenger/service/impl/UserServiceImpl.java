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
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final SimpMessagingTemplate messagingTemplate;

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
        throw new AuthenticationException("Invalid refresh token");
    }

    @Override
    @Transactional
    public void logout() {
        User user = getCurrentUser();
        user.setStatus(Status.OFFLINE);
        userRepository.save(user);
        messagingTemplate.convertAndSend(
                "/topic/status",
                new StatusUpdateDto(user.getId(), user.getUsername(), user.getStatus().name())
        );
    }

    @Override
    @Transactional
    public User setUserOnline(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != Status.ONLINE) {
            user.setStatus(Status.ONLINE);
            userRepository.save(user);
        }

        return user;
    }

    @Override
    @Transactional
    public User setUserOffline(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != Status.OFFLINE) {
            user.setStatus(Status.OFFLINE);
            userRepository.save(user);
        }

        return user;
    }

    @Override
    @Transactional
    public UserResponseDto saveUser(UserRegisterDto registerDto) {
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new EmailAlreadyExistsException(registerDto.getEmail());
        }
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new UsernameAlreadyExistsException(registerDto.getUsername());
        }
        User user = userMapper.toEntity(registerDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(Status.ONLINE);
        return userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(UpdateUserDto updateUserDto) {
        User user = getCurrentUser();
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
        return userMapper.toProfileDto(userRepository.save(user));
    }

    @Override
    public UserProfileDto getCurrentUserProfile() {
        return userMapper.toProfileDto(getCurrentUser());
    }

    @Override
    public List<User> findAllOnlineUsers() {
        return userRepository.findAllByStatus(Status.ONLINE);
    }

    @Override
    public List<UserListDto> getAllUsersExceptMe(String email) {
        return userRepository.findAll().stream()
                .filter(user -> !user.getEmail().equals(email))
                .map(userMapper::toUserListDto)
                .toList();
    }

    @Override
    public void disconnect(User user) {
        userRepository.findByUsername(user.getUsername()).ifPresent(storedUser -> {
            if (storedUser.getStatus() == Status.ONLINE) {
                storedUser.setStatus(Status.OFFLINE);
                userRepository.save(storedUser);
            }
        });
    }

    private User getCurrentUser() {
        String email = ((UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateUsername(String newUsername, String currentUsername) {
        if (!newUsername.equals(currentUsername) && userRepository.existsByUsername(newUsername)) {
            throw new RuntimeException("Username already taken");
        }
    }

    private User findByCredentials(UserCredentialsDto dto) throws AuthenticationException {
        Optional<User> optionalUser = userRepository.findByEmail(dto.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                return user;
            }
        }
        throw new AuthenticationException("Email or password is not correct");
    }

    private User findUserByEmail(String email) throws Exception {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("User not found"));
    }
}