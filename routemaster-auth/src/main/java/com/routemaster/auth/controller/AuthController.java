package com.routemaster.auth.controller;

import com.routemaster.auth.dto.*;
import com.routemaster.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {

        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {

        AuthResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenRequest request) {

        authService.logout(request);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('OPS_MANAGER')")
    @PatchMapping("/users/{email}/activate")
    public ResponseEntity<Void> activate(@PathVariable String email) {

        authService.activate(email);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('OPS_MANAGER')")
    @PatchMapping("/users/{email}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable String email) {

        authService.deactivate(email);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('OPS_MANAGER')")
    @PatchMapping("/users/{email}/role")
    public ResponseEntity<Void> assignRole(
            @PathVariable String email,
            @RequestBody @Valid AssignRoleRequest request
            ) {
        authService.assignRole(email, request.getRole());

        return ResponseEntity.noContent().build();
    }
}
