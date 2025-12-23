package org.albarakadigital.service;

import org.albarakadigital.controller.AuthController;
import org.albarakadigital.security.CustomUserDetailsService;
import org.albarakadigital.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_ValidCredentials_ReturnsJwt() {
        Map<String, String> credentials = Map.of("email", "test@email.com", "password", "pass");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("test@email.com")).thenReturn(userDetails);
        when(jwtUtils.generateToken(userDetails)).thenReturn("jwtToken");

        assertEquals("jwtToken", authController.login(credentials).getBody());
    }
}