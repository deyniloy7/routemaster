package com.routemaster.auth.service;

import com.routemaster.auth.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    private SecretKey signingKey;

    @PostConstruct
    private void initSigningKey() throws DecoderException {
        byte[] keyBytes = Hex.decodeHex(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, Set<Role> roles) {
        // TODO
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .claim("roles", roles)
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public Set<Role> extractRoles(String token) {
        List<String> roles = (List<String>) extractAllClaims(token)
                .get("roles", List.class);
        return roles.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }

    public boolean validateToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());

    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
