package com.routemaster.auth.security;

import com.routemaster.auth.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserDetailsImplTest {
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setup() throws Exception {
        userDetails = new UserDetailsImpl(
                "test-id-123",
                "driver@routemaster.com",
                "$2a$10$hashedpassword",
                Set.of(Role.DRIVER),
                true
        );
    }

    @Test
    void getAuthorities_shouldReturnTheListOfRoles() {
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("DRIVER");
    }

    @Test
    void getPassword_shouldReturnThePasswordHash() {
        assertThat(userDetails.getPassword())
                .isEqualTo("$2a$10$hashedpassword");
    }

    @Test
    void getUsername_shouldReturnTheUsername() {
        assertThat(userDetails.getUsername())
                .isEqualTo("driver@routemaster.com");
    }

    @Test
    void isAccountNonExpired_shouldReturnTrueIfAccountIsActive() {
        assertThat(userDetails.isAccountNonExpired())
                .isTrue();
    }

    @Test
    void isAccountNonLocked_shouldReturnTrueIfAccountIsNotLocked() {
        assertThat(userDetails.isAccountNonLocked())
                .isTrue();
    }

    @Test
    void isCredentialsNonExpired_shouldReturnTrueIfCredentialsAreActive() {
        assertThat(userDetails.isCredentialsNonExpired())
                .isTrue();
    }

    @Test
    void isEnabled_shouldReturnTrueIfAccountIsEnabled() {
        assertThat(userDetails.isEnabled())
                .isTrue();
    }

    @Test
    void getId_shouldReturnUserId() {
        assertThat(userDetails.getId())
                .isEqualTo("test-id-123");
    }

    @Test
    void getEmail_shouldReturnUserEmail() {
        assertThat(userDetails.getEmail())
                .isEqualTo("driver@routemaster.com");
    }

    @Test
    void isEnabled_shouldReturnFalseWhenUserIsInactive() {
        UserDetailsImpl inactiveUser = new UserDetailsImpl(
                "test-id-456",
                "inactive@routemaster.com",
                "$2a$10$hashedpassword",
                Set.of(Role.DRIVER),
                false
        );
        assertThat(inactiveUser.isEnabled()).isFalse();
    }

    @Test
    void isAccountNonLocked_shouldReturnFalseWhenUserIsInactive() {
        UserDetailsImpl inactiveUser = new UserDetailsImpl(
                "test-id-456",
                "inactive@routemaster.com",
                "$2a$10$hashedpassword",
                Set.of(Role.DRIVER),
                false
        );
        assertThat(inactiveUser.isAccountNonLocked()).isFalse();
    }
}
