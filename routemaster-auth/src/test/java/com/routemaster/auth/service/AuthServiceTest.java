package com.routemaster.auth.service;

import com.routemaster.auth.dto.*;
import com.routemaster.auth.entity.Role;
import com.routemaster.auth.entity.User;
import com.routemaster.auth.exception.InvalidTokenException;
import com.routemaster.auth.exception.UserAlreadyExistsException;
import com.routemaster.auth.exception.UserNotFoundException;
import com.routemaster.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .email("driver@routemaster.com")
                .id("userId001")
                .passwordHash("hashedPass123")
                .roles(new HashSet<>(Set.of(Role.CUSTOMER)))
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Test
    void register_shouldSucceed_whenEmailDoesNotExist() {
        // Arrange
        RegisterRequest request = new RegisterRequest("John", "Doe", "driver@routemaster.com", "1234567890", "Secure123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPass123");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtService.generateToken(mockUser.getEmail(), mockUser.getRoles())).thenReturn("mockAccessToken");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(AuthStatus.SUCCESS);
        assertThat(response.getUserId()).isEqualTo("userId001");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getAccessToken()).isEqualTo("mockAccessToken");
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getRoles()).isEqualTo(Set.of(Role.CUSTOMER));
    }

    @Test
    void register_shouldThrowUserAlreadyExistsException_whenEmailExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest("John", "Doe", "driver@routemaster.com", "1234567890", "Secure123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void login_shouldSucceed_whenCredentialsAreValid() {
        // Arrange
        LoginRequest request = new LoginRequest("driver@routemaster.com", "Secure123");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(mockUser.getEmail(), mockUser.getRoles())).thenReturn("mockAccessToken");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(AuthStatus.SUCCESS);
        assertThat(response.getUserId()).isEqualTo("userId001");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getAccessToken()).isEqualTo("mockAccessToken");
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getRoles()).isEqualTo(Set.of(Role.CUSTOMER));
    }

    @Test
    void login_shouldThrowBadCredentialsException_whenCredentialsAreInvalid() {
        // Arrange
        LoginRequest request = new LoginRequest("driver@routemaster.com", "Secure123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void refreshToken_shouldThrowInvalidTokenException_whenTokenNotFoundInRedis() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token-123");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        assertThrows(InvalidTokenException.class, () -> authService.refreshToken(request));
    }

    @Test
    void refreshToken_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token-123");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(request.getRefreshToken())).thenReturn("driver@routemaster.com");

        when(userRepository.findByEmail("driver@routemaster.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.refreshToken(request));
    }

    @Test
    void refreshToken_shouldSucceed_whenTokenIsValid() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token-123");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(request.getRefreshToken())).thenReturn("driver@routemaster.com");

        when(userRepository.findByEmail("driver@routemaster.com")).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(mockUser.getEmail(), mockUser.getRoles())).thenReturn("mockAccessToken");

        // Act
        AuthResponse response = authService.refreshToken(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(AuthStatus.SUCCESS);
        assertThat(response.getUserId()).isEqualTo("userId001");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getAccessToken()).isEqualTo("mockAccessToken");
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getRoles()).isEqualTo(Set.of(Role.CUSTOMER));
    }

    @Test
    void logout_shouldSucceed() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token-123");

        // Act
        authService.logout(request);

        // Assert
        verify(redisTemplate).delete("refresh:" + request.getRefreshToken());
    }

    @Test
    void activate_shouldActivateUser() {

        when(userRepository.findByEmail("driver@routemaster.com")).thenReturn(Optional.of(mockUser));

        // Act
        authService.activate("driver@routemaster.com");

        // Assert
        verify(userRepository).save(mockUser);
        assertThat(mockUser.isActive()).isTrue();
    }

    @Test
    void activate_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findByEmail("driver@routemaster.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.activate("driver@routemaster.com"));
    }

    @Test
    void deactivate_shouldDeactivateUser() {

        when(userRepository.findByEmail("driver@routemaster.com")).thenReturn(Optional.of(mockUser));

        // Act
        authService.deactivate("driver@routemaster.com");

        // Assert
        verify(userRepository).save(mockUser);
        assertThat(mockUser.isActive()).isFalse();
    }

    @Test
    void deactivate_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findByEmail("driver@routemaster.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.deactivate("driver@routemaster.com"));
    }

    @Test
    void assignRole_shouldAssignARoleToAUser() {

        when(userRepository.findByEmail("driver@routemaster.com")).thenReturn(Optional.of(mockUser));

        // Act
        authService.assignRole("driver@routemaster.com", Role.DRIVER);

        // Assert
        verify(userRepository).save(mockUser);
        assertThat(mockUser.getRoles()).isEqualTo(Set.of(Role.DRIVER, Role.CUSTOMER));
    }

    @Test
    void assignRole_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findByEmail("driver@routemaster.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.assignRole("driver@routemaster.com", Role.DRIVER));
    }

}
