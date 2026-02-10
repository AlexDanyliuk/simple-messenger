package com.example.simpleMessenger.controller;

import com.example.simpleMessenger.dto.JwtAuthenticationDto;
import com.example.simpleMessenger.dto.RefreshTokenDto;
import com.example.simpleMessenger.dto.UserCredentialsDto;
import com.example.simpleMessenger.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.naming.AuthenticationException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/sign-in")
    public ResponseEntity<JwtAuthenticationDto> signIn(@RequestBody UserCredentialsDto userCredentialsDto) {
        try {
            JwtAuthenticationDto jwtAuthenticationDto = userService.singIn(userCredentialsDto);
            return ResponseEntity.ok(jwtAuthenticationDto);

        } catch (AuthenticationException e) {
            throw new RuntimeException("Authentication" + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public JwtAuthenticationDto refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) throws Exception {
        return userService.refreshToken(refreshTokenDto);

    }

}



