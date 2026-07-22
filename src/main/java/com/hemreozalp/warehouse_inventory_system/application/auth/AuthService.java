package com.hemreozalp.warehouse_inventory_system.application.auth;

import com.hemreozalp.warehouse_inventory_system.common.error.ErrorCode;
import com.hemreozalp.warehouse_inventory_system.common.exception.DomainException;
import com.hemreozalp.warehouse_inventory_system.domain.user.Role;
import com.hemreozalp.warehouse_inventory_system.domain.user.User;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.user.UserRepository;
import com.hemreozalp.warehouse_inventory_system.web.auth.AuthResponse;
import com.hemreozalp.warehouse_inventory_system.web.auth.LoginRequest;
import com.hemreozalp.warehouse_inventory_system.web.auth.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DomainException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    HttpStatus.CONFLICT,
                    "Email is already registered.");
        }

        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.fullName().trim(),
                Role.USER);

        User savedUser = userRepository.save(user);
        return toAuthResponse(new CustomUserDetails(savedUser));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        return toAuthResponse(new CustomUserDetails(user));
    }

    private AuthResponse toAuthResponse(CustomUserDetails userDetails) {
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(
                token,
                "Bearer",
                userDetails.id(),
                userDetails.email(),
                userDetails.fullName(),
                userDetails.role());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private DomainException invalidCredentials() {
        return new DomainException(
                ErrorCode.BUSINESS_RULE_VIOLATION,
                HttpStatus.UNAUTHORIZED,
                "Email or password is invalid.");
    }
}
