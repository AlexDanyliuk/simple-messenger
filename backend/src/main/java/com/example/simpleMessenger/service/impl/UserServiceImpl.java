package com.example.simpleMessenger.service.impl;

import com.example.simpleMessenger.dto.*;
import com.example.simpleMessenger.entity.ChatMessage;
import com.example.simpleMessenger.entity.Status;
import com.example.simpleMessenger.entity.User;
import com.example.simpleMessenger.exceptionHandler.EmailAlreadyExistsException;
import com.example.simpleMessenger.exceptionHandler.UsernameAlreadyExistsException;
import com.example.simpleMessenger.mapper.UserMapper;
import com.example.simpleMessenger.repository.ChatMessageRepository;
import com.example.simpleMessenger.repository.UserRepository;
import com.example.simpleMessenger.security.jwt.JwtService;
import com.example.simpleMessenger.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final JavaMailSender mailSender;
    private final Map<String, ResetCodeState> passwordResetCodes = new ConcurrentHashMap<>();
    private final Map<String, RegistrationCodeState> registrationVerificationCodes = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Value("${app.mail.reset.subject}")
    private String resetMailSubject;

    @Value("${app.mail.reset.code-ttl-minutes:10}")
    private long resetCodeTtlMinutes;

    @Value("${app.mail.registration.subject}")
    private String registrationMailSubject;

    @Value("${app.mail.registration.code-ttl-minutes:10}")
    private long registrationCodeTtlMinutes;

    @Override
    public JwtAuthenticationDto singIn(UserCredentialsDto userCredentialsDto) throws AuthenticationException {
        userCredentialsDto.setEmail(normalizeEmail(userCredentialsDto.getEmail()));
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
        user.setLastSeenAt(new Date());
        userRepository.save(user);
        messagingTemplate.convertAndSend(
                "/topic/status",
            new StatusUpdateDto(user.getId(), user.getUsername(), user.getStatus().name(), user.getLastSeenAt())
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
            user.setLastSeenAt(new Date());
            userRepository.save(user);
        }

        return user;
    }

    @Override
    @Transactional
    public UserResponseDto saveUser(UserRegisterDto registerDto) {
        registerDto.setEmail(normalizeEmail(registerDto.getEmail()));
        validateRegistrationCode(registerDto.getEmail(), registerDto.getVerificationCode());

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new EmailAlreadyExistsException(registerDto.getEmail());
        }
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new UsernameAlreadyExistsException(registerDto.getUsername());
        }
        User user = userMapper.toEntity(registerDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(Status.OFFLINE);
        user.setTheme("light");
        user.setLanguage("uk");
        user.setLastSeenAt(new Date());

        registrationVerificationCodes.remove(registerDto.getEmail());
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
        if (updateUserDto.getTheme() != null) {
            user.setTheme(updateUserDto.getTheme());
        }
        if (updateUserDto.getLanguage() != null) {
            user.setLanguage(updateUserDto.getLanguage());
        }
        return userMapper.toProfileDto(userRepository.save(user));
    }

    @Override
    public UserProfileDto getCurrentUserProfile() {
        return userMapper.toProfileDto(getCurrentUser());
    }

    @Override
    public UserProfileDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toProfileDto(user);
    }

    @Override
    public UserListDto getUserListDtoById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toUserListDto(user);
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
    public List<UserListDto> searchByUsername(String query, String currentEmail) {
        return userRepository.findByUsernameContainingIgnoreCase(query).stream()
                .filter(user -> !user.getEmail().equals(currentEmail))
                .map(userMapper::toUserListDto)
                .toList();
    }

    @Override
    public List<UserListDto> getUsersWithConversations() {
        User me = getCurrentUser();
        Long myId = me.getId();

        List<ChatMessage> allMessages = chatMessageRepository.findBySenderIdOrRecipientId(myId, myId);

        List<Long> partnerIds = allMessages.stream()
                .map(msg -> msg.getSenderId().equals(myId) ? msg.getRecipientId() : msg.getSenderId())
                .distinct()
                .toList();

        return userRepository.findAllById(partnerIds).stream()
                .map(partner -> {
                    UserListDto dto = userMapper.toUserListDto(partner);

                    chatMessageRepository
                            .findTopBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampDesc(
                                    myId, partner.getId(),
                                    partner.getId(), myId
                            )
                            .ifPresent(msg -> {
                                dto.setLastMessage(msg.getContent());
                                dto.setLastMessageTime(msg.getTimestamp());
                            });

                    int unread = chatMessageRepository.countBySenderIdAndRecipientIdAndIsRead(
                            partner.getId(), myId, false
                    );
                    dto.setUnreadCount(unread);

                    return dto;
                })
                .sorted((a, b) -> {
                    if (a.getLastMessageTime() == null) return 1;
                    if (b.getLastMessageTime() == null) return -1;
                    return b.getLastMessageTime().compareTo(a.getLastMessageTime());
                })
                .toList();
    }

    @Override
    @Transactional
    public UserProfileDto uploadAvatar(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл порожній");
        }
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new IllegalArgumentException("Дозволені лише зображення");
        }

        User user = getCurrentUser();

        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String filename = "avatar_" + user.getId() + "_" + UUID.randomUUID() + ext;

        Path dir = Paths.get(uploadDir, "avatars");
        Files.createDirectories(dir);
        Files.write(dir.resolve(filename), file.getBytes());

        user.setAvatarUrl("/uploads/avatars/" + filename);
        return userMapper.toProfileDto(userRepository.save(user));
    }

    @Override
    public void disconnect(User user) {
        userRepository.findByUsername(user.getUsername()).ifPresent(storedUser -> {
            if (storedUser.getStatus() == Status.ONLINE) {
                storedUser.setStatus(Status.OFFLINE);
                storedUser.setLastSeenAt(new Date());
                userRepository.save(storedUser);
            }
        });
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordDto changePasswordDto) throws AuthenticationException {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        if (passwordEncoder.matches(changePasswordDto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public ForgotPasswordRequestResponseDto requestPasswordReset(ForgotPasswordRequestDto forgotPasswordRequestDto) {
        String normalizedEmail = normalizeEmail(forgotPasswordRequestDto.getEmail());
        forgotPasswordRequestDto.setEmail(normalizedEmail);

        Optional<User> user = userRepository.findByEmail(normalizedEmail);
        if (user.isEmpty()) {
            return new ForgotPasswordRequestResponseDto(
                "If an account exists for this email, a reset code was sent"
            );
        }

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        long ttlMs = resetCodeTtlMinutes * 60 * 1000;
        passwordResetCodes.put(normalizedEmail, new ResetCodeState(code, System.currentTimeMillis() + ttlMs));

        sendResetEmail(normalizedEmail, code);

        return new ForgotPasswordRequestResponseDto(
            "If an account exists for this email, a reset code was sent"
        );
    }

    @Override
    public ForgotPasswordRequestResponseDto requestRegistrationVerificationCode(RegistrationVerificationRequestDto requestDto) {
        String normalizedEmail = normalizeEmail(requestDto.getEmail());
        requestDto.setEmail(normalizedEmail);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        long ttlMs = registrationCodeTtlMinutes * 60 * 1000;
        registrationVerificationCodes.put(normalizedEmail, new RegistrationCodeState(code, System.currentTimeMillis() + ttlMs));

        sendRegistrationVerificationEmail(normalizedEmail, code);

        return new ForgotPasswordRequestResponseDto(
                "Verification code sent to your email"
        );
    }

    @Override
    @Transactional
    public void confirmPasswordReset(ForgotPasswordConfirmDto forgotPasswordConfirmDto) throws AuthenticationException {
        String normalizedEmail = normalizeEmail(forgotPasswordConfirmDto.getEmail());
        forgotPasswordConfirmDto.setEmail(normalizedEmail);

        ResetCodeState state = passwordResetCodes.get(normalizedEmail);
        if (state == null) {
            throw new AuthenticationException("Reset code was not requested");
        }
        if (System.currentTimeMillis() > state.expiresAt()) {
            passwordResetCodes.remove(normalizedEmail);
            throw new AuthenticationException("Reset code expired");
        }
        if (!state.code().equals(forgotPasswordConfirmDto.getCode())) {
            throw new AuthenticationException("Invalid reset code");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        user.setPassword(passwordEncoder.encode(forgotPasswordConfirmDto.getNewPassword()));
        userRepository.save(user);
        passwordResetCodes.remove(normalizedEmail);
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

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private void sendResetEmail(String recipient, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(recipient);
            message.setSubject(resetMailSubject);
            message.setText(buildResetEmailBody(code));
            mailSender.send(message);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send reset email");
        }
    }

    private String buildResetEmailBody(String code) {
        return "Your Simple Messenger reset code is: " + code + "\n\n"
                + "This code is valid for " + resetCodeTtlMinutes + " minutes.\n"
                + "If you did not request a password reset, ignore this email.";
    }

    private void sendRegistrationVerificationEmail(String recipient, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(recipient);
            message.setSubject(registrationMailSubject);
            message.setText(buildRegistrationVerificationEmailBody(code));
            mailSender.send(message);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send registration verification email");
        }
    }

    private String buildRegistrationVerificationEmailBody(String code) {
        return "Your Simple Messenger registration verification code is: " + code + "\n\n"
                + "This code is valid for " + registrationCodeTtlMinutes + " minutes.\n"
                + "If you did not request this code, ignore this email.";
    }

    private void validateRegistrationCode(String email, String code) {
        RegistrationCodeState state = registrationVerificationCodes.get(email);
        if (state == null) {
            throw new IllegalArgumentException("Verification code was not requested");
        }
        if (System.currentTimeMillis() > state.expiresAt()) {
            registrationVerificationCodes.remove(email);
            throw new IllegalArgumentException("Verification code expired");
        }
        if (!state.code().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }
    }

    private record ResetCodeState(String code, long expiresAt) {}
    private record RegistrationCodeState(String code, long expiresAt) {}
}