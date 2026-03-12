package com.example.simpleMessenger.controller;

import com.example.simpleMessenger.dto.JwtAuthenticationDto;
import com.example.simpleMessenger.dto.RefreshTokenDto;
import com.example.simpleMessenger.dto.UserCredentialsDto;
import com.example.simpleMessenger.security.AuthCookieService;
import com.example.simpleMessenger.service.UserService;
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
    public ResponseEntity<Void> signIn(@RequestBody UserCredentialsDto userCredentialsDto) {
        try {
            JwtAuthenticationDto jwtAuthenticationDto = userService.singIn(userCredentialsDto);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, authCookieService.buildAccessTokenCookie(jwtAuthenticationDto.getToken()).toString())
                    .header(HttpHeaders.SET_COOKIE, authCookieService.buildRefreshTokenCookie(jwtAuthenticationDto.getRefreshToken()).toString())
                    .build();

        } catch (AuthenticationException e) {
            throw new RuntimeException("Authentication" + e.getMessage());
        }
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

}



