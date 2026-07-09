package com.routemaster.auth.security;

import com.routemaster.auth.entity.Role;
import com.routemaster.auth.entity.User;
import com.routemaster.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUserName_shouldReturnUserByUsername() {
        User user = User.builder()
                .email("driver123@test.com")
                .passwordHash("12345")
                .firstName("abcd")
                .lastName("defg")
                .roles(Set.of(Role.DRIVER))
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("driver123@test.com"))
                .thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("driver123@test.com");

        assertThat(result.getUsername()).isEqualTo("driver123@test.com");
    }

    @Test
    void loadUserByUserName_shouldReturnEmptyIfUserNotPresent() {

        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("unknown@test.com"));
    }
}
