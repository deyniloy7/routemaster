package com.routemaster.auth.dto;

import com.routemaster.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private AuthStatus status;
    private String userId;
    private String firstName;
    private Set<Role> roles;
    private String accessToken;
    private String refreshToken;

}
