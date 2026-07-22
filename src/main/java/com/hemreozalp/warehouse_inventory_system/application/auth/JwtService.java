package com.hemreozalp.warehouse_inventory_system.application.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hemreozalp.warehouse_inventory_system.domain.user.Role;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final byte[] secret;
    private final long expirationMinutes;

    @Autowired
    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this(objectMapper, Clock.systemUTC(), secret, expirationMinutes);
    }

    JwtService(ObjectMapper objectMapper, Clock clock, String secret, long expirationMinutes) {
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(CustomUserDetails userDetails) {
        Instant issuedAt = Instant.now(clock);
        Instant expiresAt = issuedAt.plus(expirationMinutes, ChronoUnit.MINUTES);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", userDetails.getUsername());
        payload.put("userId", userDetails.id().toString());
        payload.put("role", userDetails.role().name());
        payload.put("iat", issuedAt.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public JwtClaims parseAndValidate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new InvalidJwtException("JWT token format is invalid.");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
            throw new InvalidJwtException("JWT token signature is invalid.");
        }

        JsonNode payload = decodePayload(parts[1]);
        String subject = requiredText(payload, "sub");
        Role role = Role.valueOf(requiredText(payload, "role"));
        Instant expiresAt = Instant.ofEpochSecond(payload.required("exp").asLong());

        if (!expiresAt.isAfter(Instant.now(clock))) {
            throw new InvalidJwtException("JWT token is expired.");
        }

        return new JwtClaims(subject, role, expiresAt);
    }

    private String encodeJson(Map<String, Object> values) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(values);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize JWT data.", exception);
        }
    }

    private JsonNode decodePayload(String payload) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            return objectMapper.readTree(decoded);
        } catch (IllegalArgumentException | IOException exception) {
            throw new InvalidJwtException("JWT token payload is invalid.");
        }
    }

    private String requiredText(JsonNode payload, String field) {
        JsonNode value = payload.required(field);
        if (!value.isTextual()) {
            throw new InvalidJwtException("JWT token payload is invalid.");
        }
        return value.asText();
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign JWT token.", exception);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return java.security.MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
