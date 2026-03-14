package com.example.simpleMessenger.controller;

import com.example.simpleMessenger.dto.JwtAuthenticationDto;
import com.example.simpleMessenger.dto.ForgotPasswordConfirmDto;
import com.example.simpleMessenger.dto.ForgotPasswordRequestDto;
import com.example.simpleMessenger.dto.ForgotPasswordRequestResponseDto;
import com.example.simpleMessenger.dto.RegistrationVerificationRequestDto;
import com.example.simpleMessenger.dto.RefreshTokenDto;
import com.example.simpleMessenger.dto.UserCredentialsDto;
import com.example.simpleMessenger.security.AuthCookieService;
import com.example.simpleMessenger.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import javax.naming.AuthenticationException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthCookieService authCookieService;

    @PostMapping("/sign-in")
    public ResponseEntity<Void> signIn(@Valid @RequestBody UserCredentialsDto userCredentialsDto) throws AuthenticationException {
        JwtAuthenticationDto jwtAuthenticationDto = userService.singIn(userCredentialsDto);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieService.buildAccessTokenCookie(jwtAuthenticationDto.getToken()).toString())
                .header(HttpHeaders.SET_COOKIE, authCookieService.buildRefreshTokenCookie(jwtAuthenticationDto.getRefreshToken()).toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshToken(@RequestBody(required = false) RefreshTokenDto refreshTokenDto,
                                             HttpServletRequest request) throws Exception {
        String refreshToken = refreshTokenDto != null ? refreshTokenDto.getRefreshToken() : null;
        if (refreshToken == null || refreshToken.isBlank()) {
            refreshToken = authCookieService.extractRefreshToken(request);
        }

        RefreshTokenDto dto = new RefreshTokenDto();
        dto.setRefreshToken(refreshToken);

        JwtAuthenticationDto jwtAuthenticationDto = userService.refreshToken(dto);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieService.buildAccessTokenCookie(jwtAuthenticationDto.getToken()).toString())
                .header(HttpHeaders.SET_COOKIE, authCookieService.buildRefreshTokenCookie(jwtAuthenticationDto.getRefreshToken()).toString())
                .build();

    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        try {
            userService.logout();
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieService.clearAccessTokenCookie().toString())
                .header(HttpHeaders.SET_COOKIE, authCookieService.clearRefreshTokenCookie().toString())
                .build();
    }

    @PostMapping("/forgot-password/request")
    public ResponseEntity<ForgotPasswordRequestResponseDto> requestPasswordReset(
            @Valid @RequestBody ForgotPasswordRequestDto forgotPasswordRequestDto
    ) {
        return ResponseEntity.ok(userService.requestPasswordReset(forgotPasswordRequestDto));
    }

    @PostMapping("/register/request-code")
    public ResponseEntity<ForgotPasswordRequestResponseDto> requestRegistrationVerificationCode(
            @Valid @RequestBody RegistrationVerificationRequestDto requestDto
    ) {
        return ResponseEntity.ok(userService.requestRegistrationVerificationCode(requestDto));
    }

    @PostMapping("/forgot-password/confirm")
    public ResponseEntity<Void> confirmPasswordReset(
            @Valid @RequestBody ForgotPasswordConfirmDto forgotPasswordConfirmDto
    ) throws AuthenticationException {
        userService.confirmPasswordReset(forgotPasswordConfirmDto);
        return ResponseEntity.ok().build();
    }

}



