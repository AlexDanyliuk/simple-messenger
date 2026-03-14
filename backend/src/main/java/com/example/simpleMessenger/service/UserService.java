package com.example.simpleMessenger.service;


import com.example.simpleMessenger.dto.*;
import com.example.simpleMessenger.entity.User;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.List;


public interface UserService {

    JwtAuthenticationDto singIn(UserCredentialsDto userCredentialsDto) throws AuthenticationException;;
    JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws Exception ;

    UserResponseDto saveUser(UserRegisterDto registerDto);

    void disconnect(User user);

    UserProfileDto updateProfile(UpdateUserDto updateUserDto);

    UserProfileDto getCurrentUserProfile();

    UserProfileDto getUserById(Long id);

    UserListDto getUserListDtoById(Long id);

    List<User> findAllOnlineUsers();

    List<UserListDto> getAllUsersExceptMe(String email);

    List<UserListDto> searchByUsername(String query, String currentEmail);

    List<UserListDto> getUsersWithConversations();

    UserProfileDto uploadAvatar(MultipartFile file) throws IOException;

    void logout();

    User setUserOnline(String email);

    User setUserOffline(String email);

    void changePassword(ChangePasswordDto changePasswordDto) throws AuthenticationException;

    ForgotPasswordRequestResponseDto requestPasswordReset(ForgotPasswordRequestDto forgotPasswordRequestDto);

    void confirmPasswordReset(ForgotPasswordConfirmDto forgotPasswordConfirmDto) throws AuthenticationException;

    ForgotPasswordRequestResponseDto requestRegistrationVerificationCode(RegistrationVerificationRequestDto requestDto);

}
