package com.routemaster.auth.service;

import com.routemaster.auth.dto.*;
import com.routemaster.auth.entity.Role;
import com.routemaster.auth.entity.User;
import com.routemaster.auth.exception.InvalidTokenException;
import com.routemaster.auth.exception.UserAlreadyExistsException;
import com.routemaster.auth.exception.UserNotFoundException;
import com.routemaster.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, String> redisTemplate;

    public AuthResponse register(RegisterRequest request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .roles(Set.of(Role.CUSTOMER))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        return getAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
               new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        User savedUser = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException(loginRequest.getEmail()));

        return getAuthResponse(savedUser);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String email = redisTemplate.opsForValue().get(request.getRefreshToken());

        if (email == null) {
            throw new InvalidTokenException();
        }

        User savedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        redisTemplate.delete("refresh:" + request.getRefreshToken());

        return getAuthResponse(savedUser);
    }

    public void logout(RefreshTokenRequest request) {
        redisTemplate.delete("refresh:" + request.getRefreshToken());
    }

    public void activate(String email) {
        User savedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        savedUser.setActive(true);

        userRepository.save(savedUser);
    }

    public void deactivate(String email) {
        User savedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        savedUser.setActive(false);

        userRepository.save(savedUser);
    }

    public void assignRole(String email, Role role) {
        User savedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        savedUser.getRoles().add(role);

        userRepository.save(savedUser);
    }

    @NotNull
    private AuthResponse getAuthResponse(User savedUser) {
        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getRoles());

        String refreshToken = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("refresh:" + refreshToken, savedUser.getEmail(), 7, TimeUnit.DAYS);

        return new AuthResponse(
                AuthStatus.SUCCESS,
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getRoles(),
                token,
                refreshToken);
    }
}
