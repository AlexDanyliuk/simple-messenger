package com.example.simpleMessenger.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

    @Value("${app.auth.cookie.access-name:ACCESS_TOKEN}")
    private String accessTokenCookieName;

    @Value("${app.auth.cookie.refresh-name:REFRESH_TOKEN}")
    private String refreshTokenCookieName;

    @Value("${app.auth.cookie.secure:false}")
    private boolean secureCookies;

    public ResponseCookie buildAccessTokenCookie(String token) {
        return buildCookie(accessTokenCookieName, token, 60 * 60 * 24L);
    }

    public ResponseCookie buildRefreshTokenCookie(String token) {
        return buildCookie(refreshTokenCookieName, token, 60 * 60 * 24L * 5);
    }

    public ResponseCookie clearAccessTokenCookie() {
        return clearCookie(accessTokenCookieName);
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return clearCookie(refreshTokenCookieName);
    }

    public String extractAccessToken(HttpServletRequest request) {
        return extractCookieValue(request, accessTokenCookieName);
    }

    public String extractRefreshToken(HttpServletRequest request) {
        return extractCookieValue(request, refreshTokenCookieName);
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    private ResponseCookie clearCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}