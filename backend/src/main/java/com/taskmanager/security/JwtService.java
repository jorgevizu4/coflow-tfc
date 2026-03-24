package com.taskmanager.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio para generación y validación de tokens JWT.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * Extrae el ID de usuario (subject) del token.
     */
    public Long extractUsuarioId(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        return Long.parseLong(subject);
    }

    /**
     * Extrae el ID de empresa del token.
     */
    public Long extractEmpresaId(String token) {
        return extractClaim(token, claims -> claims.get("empresa_id", Long.class));
    }

    /**
     * Extrae el rol del usuario del token.
     */
    public String extractRol(String token) {
        return extractClaim(token, claims -> claims.get("rol", String.class));
    }

    /**
     * Extrae un claim específico del token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera un token JWT para un usuario.
     */
    public String generateToken(Long usuarioId, Long empresaId, String rol, String email) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("empresa_id", empresaId);
        extraClaims.put("rol", rol);
        extraClaims.put("email", email);
        
        return buildToken(extraClaims, String.valueOf(usuarioId), jwtExpiration);
    }

    /**
     * Genera un refresh token.
     */
    public String generateRefreshToken(Long usuarioId) {
        return buildToken(new HashMap<>(), String.valueOf(usuarioId), refreshExpiration);
    }

    /**
     * Valida si el token es válido.
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Valida si el token pertenece al usuario indicado.
     */
    public boolean isTokenValidForUser(String token, Long usuarioId) {
        try {
            Long tokenUsuarioId = extractUsuarioId(token);
            return tokenUsuarioId.equals(usuarioId) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (RuntimeException ex) {
            log.warn("jwt.secret no está en Base64; se usará como texto plano UTF-8");
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret debe tener al menos 32 bytes para HS256");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
