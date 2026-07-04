package com.routemaster.auth.service;

import com.routemaster.auth.entity.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import io.jsonwebtoken.security.SecurityException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET = "746573745365637265744040404040404040404040404040404040404040404040";

    private static final long TEST_EXPIRY = 900000L;

    @BeforeEach
    void setup() throws Exception {
        this.jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiry", TEST_EXPIRY);
        ReflectionTestUtils.invokeMethod(jwtService, "initSigningKey");
    }

    @Test
    void generateToken_shouldReturnValidJwtStructure() {
        String username = "driver@routemaster.com";
        Set<Role> roles = Set.of(Role.DRIVER);

        String token = jwtService.generateToken(username, roles);

        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateToken_shouldContainCorrectUsername() {
        String username = "driver@routemaster.com";
        Set<Role> roles = Set.of(Role.DRIVER);

        String token = jwtService.generateToken(username, roles);

        assertThat(jwtService.extractUsername(token)).isEqualTo(username);
    }

    @Test
    void generateToken_shouldContainCorrectRoles() {
        String username = "driver@routemaster.com";
        Set<Role> roles = Set.of(Role.DRIVER);

        String token = jwtService.generateToken(username, roles);

        assertThat(jwtService.extractRoles(token)).isEqualTo(Set.of(Role.DRIVER));
    }

    @Test
    void generateToken_shouldNotBeExpiredImmediately() {
        String username = "driver@routemaster.com";
        Set<Role> roles = Set.of(Role.DRIVER);

        String token = jwtService.generateToken(username, roles);

        assertThat(jwtService.validateToken(token, username)).isTrue();
    }

    @Test
    void extractUsername_validTokenShouldReturnCorrectUsername() {
        String username = "driver@routemaster.com";
        Set<Role> roles = Set.of(Role.DRIVER);

        String token = jwtService.generateToken(username, roles);

        assertThat(jwtService.extractUsername(token)).isEqualTo(username);
    }

    @Test
    void extractUsername_expiredTokensShouldThrowExpiredJwtException() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiry", -1000L);
        String token = jwtService.generateToken(
                "driver@routemaster.com",
                Set.of(Role.DRIVER));
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(token));
    }

    @Test
    void extractUsername_malformedTokensShouldThrowMalformedJwtException() {
        String token = "abcdefgh123456";
        assertThrows(MalformedJwtException.class, () -> jwtService.extractUsername(token));
    }

    @Test
    void extractUsername_tamperedTokensShouldThrowSecurityException() {
        String validToken = jwtService.generateToken(
                "driver@routemaster.com",
                Set.of(Role.DRIVER)
        );
        String tamperedToken = validToken.substring(0, validToken.lastIndexOf(".")) + ".invalidSignature";
        assertThrows(SecurityException.class, () -> jwtService.extractUsername(tamperedToken));
    }

    @Test
    void extractUsername_validTokensWithNullSubjectReturnsNull() {
        String token = jwtService.generateToken(
                null,
                Set.of(Role.DRIVER)
        );

        assertThat(jwtService.extractUsername(token)).isNull();
    }

    @Test
    void extractRoles_singleRoleShouldBeExtractedCorrectly() {
        String token = jwtService.generateToken(
                null,
                Set.of(Role.DRIVER)
        );

        assertThat(jwtService.extractRoles(token)).isEqualTo(Set.of(Role.DRIVER));
    }

    @Test
    void extractRoles_multipleRolesShouldBeExtractedCorrectly() {
        String token = jwtService.generateToken(
                null,
                Set.of(Role.DRIVER, Role.CUSTOMER)
        );

        assertThat(jwtService.extractRoles(token)).isEqualTo(Set.of(Role.DRIVER, Role.CUSTOMER));
    }

    @Test
    void extractRoles_emptyRolesSet() {
        String token = jwtService.generateToken(
                null,
                Set.of()
        );

        assertThat(jwtService.extractRoles(token)).isEqualTo(Set.of());
    }

    @Test
    void validateToken_shouldReturnTrueIfTokenIsValidAndUsernameMatches() {
        String username = "driver@routemaster.com";
        Set<Role> roles = Set.of(Role.DRIVER);

        String token = jwtService.generateToken(username, roles);

        assertThat(jwtService.validateToken(token, username)).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalseIfTokenIsExpiredButUsernameMatches() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiry", -1000L);
        String username = "driver@routemaster.com";
        Set<Role> roles = Set.of(Role.DRIVER);

        String expiredToken = jwtService.generateToken(username, roles);

        assertThrows(ExpiredJwtException.class, () -> jwtService.validateToken(expiredToken, username));
    }

    @Test
    void validateToken_shouldReturnFalseIfTokenIsValidButUsernameIsWrong() {
        String token = jwtService.generateToken(
                "driver@routemaster.com",
                Set.of(Role.DRIVER)
        );

        assertThat(jwtService.validateToken(token, "john")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseIfTokenIsTamperedWith() {
        String validToken = jwtService.generateToken(
                "driver@routemaster.com",
                Set.of(Role.DRIVER)
        );
        String tamperedToken = validToken.substring(0, validToken.lastIndexOf(".")) + ".invalidSignature";
        assertThrows(SecurityException.class, () -> jwtService.validateToken(tamperedToken, "driver@routemaster.com"));
    }
}