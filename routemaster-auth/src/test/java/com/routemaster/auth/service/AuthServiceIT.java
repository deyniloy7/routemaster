package com.routemaster.auth.service;

import com.routemaster.auth.dto.*;
import com.routemaster.auth.entity.Role;
import com.routemaster.auth.entity.User;
import com.routemaster.auth.exception.InvalidTokenException;
import com.routemaster.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@Transactional
public class AuthServiceIT {

    @Container
    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @SuppressWarnings("resource")
    @Container
    public static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", ()->redis.getMappedPort(6379));
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void register_shouldSucceed_whenEmailDoesNotExist() {
        // Arrange
        RegisterRequest request = new RegisterRequest("John", "Doe", "driver@routemaster.com", "1234567890", "Secure123");

        // Act
        AuthResponse response = authService.register(request);

        User dbUser = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AssertionError("User was not persisted"));

        // Assert
        assertThat(response.getStatus()).isEqualTo(AuthStatus.SUCCESS);
        assertThat(response.getUserId()).isNotNull();
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getRoles()).isEqualTo(Set.of(Role.CUSTOMER));

        assertThat(dbUser.getId()).isEqualTo(response.getUserId());
        assertThat(dbUser.getEmail()).isEqualTo("driver@routemaster.com");
        assertThat(dbUser.getFirstName()).isEqualTo("John");
        assertThat(dbUser.getRoles()).isEqualTo(Set.of(Role.CUSTOMER));
    }

    @Test
    void login_shouldSucceed_whenUsernameAndPasswordAreCorrect() {
        // Arrange
        String hashedPassword = passwordEncoder.encode("Secure123");

        User user = User.builder()
                .email("driver@routemaster.com")
                .passwordHash(hashedPassword)
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of(Role.CUSTOMER))
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("driver@routemaster.com", "Secure123");

        AuthResponse loginResponse = authService.login(loginRequest);

        assertThat(loginResponse.getStatus()).isEqualTo(AuthStatus.SUCCESS);
        assertThat(loginResponse.getFirstName()).isEqualTo("John");
        assertThat(loginResponse.getAccessToken()).isNotNull();
        assertThat(loginResponse.getUserId()).isNotNull();
        assertThat(loginResponse.getRefreshToken()).isNotNull();
        assertThat(loginResponse.getRoles()).isEqualTo(Set.of(Role.CUSTOMER));
    }

    @Test
    void getRefreshToken_shouldSucceed_whenTokenIsValid() {
        // Arrange
        String hashedPassword = passwordEncoder.encode("Secure123");

        User user = User.builder()
                .email("driver@routemaster.com")
                .passwordHash(hashedPassword)
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of(Role.CUSTOMER))
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("driver@routemaster.com", "Secure123");

        AuthResponse loginResponse = authService.login(loginRequest);

        assertThat(loginResponse.getRefreshToken()).isNotNull();

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(loginResponse.getRefreshToken());

        AuthResponse refreshTokenResponse = authService.refreshToken(refreshTokenRequest);

        assertThat(refreshTokenResponse.getStatus()).isEqualTo(AuthStatus.SUCCESS);
        assertThat(refreshTokenResponse.getFirstName()).isEqualTo("John");
        assertThat(refreshTokenResponse.getAccessToken()).isNotNull();
        assertThat(refreshTokenResponse.getUserId()).isNotNull();
        assertThat(refreshTokenResponse.getRefreshToken()).isNotNull();
        assertThat(refreshTokenResponse.getRoles()).isEqualTo(Set.of(Role.CUSTOMER));
    }

    @Test
    void getRefreshToken_shouldRotateRefreshToken_whenTokenIsValid() {
        // Arrange
        String hashedPassword = passwordEncoder.encode("Secure123");

        User user = User.builder()
                .email("driver@routemaster.com")
                .passwordHash(hashedPassword)
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of(Role.CUSTOMER))
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("driver@routemaster.com", "Secure123");

        AuthResponse loginResponse = authService.login(loginRequest);

        assertThat(loginResponse.getRefreshToken()).isNotNull();

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(loginResponse.getRefreshToken());

        AuthResponse refreshTokenResponse = authService.refreshToken(refreshTokenRequest);

        assertThat(refreshTokenResponse.getStatus()).isEqualTo(AuthStatus.SUCCESS);
        assertThat(refreshTokenResponse.getRefreshToken()).isNotEqualTo(loginResponse.getRefreshToken());

        assertThrows(InvalidTokenException.class, () -> authService.refreshToken(refreshTokenRequest));
    }
}
