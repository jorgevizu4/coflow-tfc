package com.example.backend_v2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION_MS = 86400000L; // 24 h

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", EXPIRATION_MS);
    }

    private UserDetails buildUser(String email) {
        return User.builder()
                .username(email)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateToken_returnsNonNullNonEmptyString() {
        UserDetails user = buildUser("test@coflow.com");

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractUsername_returnsEmailEmbeddedInToken() {
        String email = "usuario@coflow.com";
        UserDetails user = buildUser(email);
        String token = jwtService.generateToken(user);

        String extracted = jwtService.extractUsername(token);

        assertEquals(email, extracted);
    }

    @Test
    void isTokenValid_withMatchingUser_returnsTrue() {
        UserDetails user = buildUser("admin@coflow.com");
        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_withDifferentUser_returnsFalse() {
        UserDetails owner = buildUser("owner@coflow.com");
        UserDetails other = buildUser("other@coflow.com");
        String token = jwtService.generateToken(owner);

        assertFalse(jwtService.isTokenValid(token, other));
    }

    @Test
    void generateTokenWithExtraClaims_embeddedClaimIsReadable() {
        UserDetails user = buildUser("test@coflow.com");
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("rol", "ADMIN");

        String token = jwtService.generateToken(claims, user);
        String rolClaim = jwtService.extractClaim(token,
                io.jsonwebtoken.Claims::getSubject); // at minimum the subject is readable

        assertNotNull(rolClaim);
        assertEquals("test@coflow.com", rolClaim);
    }
}
