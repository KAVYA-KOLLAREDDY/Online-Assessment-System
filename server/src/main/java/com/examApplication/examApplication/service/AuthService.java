package com.examApplication.examApplication.service;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.examApplication.examApplication.dto.AuthenticationRequestDTO;
import com.examApplication.examApplication.entity.RefreshToken;
import com.examApplication.examApplication.entity.auth.Role;
import com.examApplication.examApplication.entity.auth.User;
import com.examApplication.examApplication.helpers.JwtHelpers;
import com.examApplication.examApplication.repository.RefreshTokenRepository;
import com.examApplication.examApplication.repository.RoleRepository;
import com.examApplication.examApplication.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;

    @Value("${jwt.access-token.expiration}")
    private Duration accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private Duration refreshTokenExpiration;

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user;
    }

    private String generateRefreshToken(User user) {
        String refreshToken = Jwts.builder()
                .issuer("My3Tech")
                .subject(user.getEmail())
                .claim("username", user.getName())
                .claim("authorities", user.getAuthorities()
                        .stream().map(authority -> authority.getAuthority())
                        .collect(Collectors.joining(",")))
                .claim("nonce", UUID.randomUUID().toString())
                .issuedAt(new Date())
                // .expiration(new Date(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)))
                // // 7
                // Days
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration.toMillis()))
                .signWith(Keys.hmacShaKeyFor(JwtHelpers.ACCESS_REFRESH.getBytes()))
                .compact();

        return refreshToken;
    }

    private String generateAccessToken(User user) {
        String jwt = Jwts
                .builder()
                .issuer("My3Tech")
                .subject(user.getEmail())
                .claim("username", user.getName())
                .claim("authorities", user.getAuthorities()
                        .stream().map(authority -> authority.getAuthority())
                        .collect(Collectors.joining(",")))
                .claim("nonce", UUID.randomUUID().toString())
                .issuedAt(new Date())
                // .expiration(new Date(System.currentTimeMillis() + (15 * 60 * 1000)))
                // .expiration(new Date(System.currentTimeMillis() + (10 * 1000))) // 15 min
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration.toMillis()))
                .signWith(Keys.hmacShaKeyFor(JwtHelpers.ACCESS_SECRET.getBytes()))
                .compact();
        return jwt;
    }

    @Transactional
    public String login(String agent, AuthenticationRequestDTO request, HttpServletResponse response) {
        String jwt = "";
        String refreshToken = "";
        // 1. Create Authentication Object
        Authentication authentication = UsernamePasswordAuthenticationToken
                .unauthenticated(request.getEmail(), request.getPassword());

        // 2. Start the authentication process
        Authentication authResponse = authenticationManager.authenticate(authentication);

        if (authResponse != null && authResponse.isAuthenticated()) {
            // System.out.println("Oye : " + authResponse.getPrincipal());
            User user = (User) authResponse.getPrincipal();
            jwt = this.generateAccessToken(user);
            refreshToken = this.generateRefreshToken(user);

            RefreshToken refreshJWT = RefreshToken
                    .builder()
                    .user(user)
                    .token(hashRefreshToken(refreshToken)) // hash the same refresh token again and
                                                           // check it!
                    .createdAt(LocalDateTime.now())
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .userAgent(agent)
                    .revoked(false)
                    .build();

            refreshTokenRepository.save(refreshJWT);

            ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                    .httpOnly(true) // JS Cannot access the cookie
                    .secure(false) // Sends cookie in https secure mode if set to true
                    .path("/api/v1/auth") // Browser sends cookie to this path!
                    .maxAge(Duration.ofDays(7)) // Cookie expiration
                    .sameSite("Lax") // Allows the cookie for GET requests
                    .build();

            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        // UserDetailsService
        return jwt;
    }

    public String hashRefreshToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing refresh token", e);
        }
    }

    public void register(String name, String email, String password, String roleName) {
        Boolean bool = userRepository.existsByEmail(email);
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role Doesn't Exist!"));
        if (bool) {
            throw new RuntimeException("Email Already Exists!");
        }
        User user = new User();
        user.setName(name);
        ;
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEmail(email);
        user.setIsActive(false);
        user.setIsLocked(false);
        userRepository.save(user);
        System.out.println("Username: " + name);
    }

    public String generateNewTokenViaRefresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "refresh_token");

        if (refreshToken == null) {
            System.out.println("Refresh Token == " + refreshToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token Missing!");
        }

        String hashedRefreshToken = hashRefreshToken(refreshToken);
        Optional<RefreshToken> dbToken = refreshTokenRepository.findByToken(hashedRefreshToken);

        if (!JwtHelpers.isRefreshValid(refreshToken)) {
            dbToken.ifPresent(token -> {
                token.setRevoked(true);
                token.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            });
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token Expired or Not Found!");
        }

        if (dbToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token Not Found!");
        }

        RefreshToken refreshDBToken = dbToken.get();
        if (refreshDBToken.getRevoked()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token Revoked!");
        }

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(JwtHelpers.ACCESS_REFRESH.getBytes()))
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();
        ;
        String username = String.valueOf(claims.get("sub"));
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Invalid email!"));

        refreshDBToken.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshDBToken);

        String accessToken = generateAccessToken(user);
        return accessToken;
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "refresh_token");
        if (refreshToken != null) {
            refreshTokenService.revokeToken(hashRefreshToken(refreshToken));
        }

        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(false)
                .secure(false)
                .path("/api/v1/auth")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }
}
