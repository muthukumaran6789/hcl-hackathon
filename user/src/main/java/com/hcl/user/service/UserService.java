package com.hcl.user.service;

import com.hcl.user.dto.AuthResponse;
import com.hcl.user.dto.LoginRequest;
import com.hcl.user.dto.RegisterRequest;
import com.hcl.user.entity.User;
import com.hcl.user.repository.UserRepository;
import com.hcl.user.security.JwtUtil;
import com.hcl.user.util.WalletIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationConfiguration authConfig;
    private final WalletIdGenerator walletIdGenerator;

    private AuthenticationManager getAuthenticationManager() throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(), 
            user.getPassword(), 
            new ArrayList<>()
        );
    }

    public AuthResponse register(RegisterRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.builder()
                    .message("Username already exists")
                    .build();
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.builder()
                    .message("Email already exists")
                    .build();
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setWalletId(walletIdGenerator.generateWalletId());
        
        userRepository.save(user);

        String token = jwtUtil.generateToken(loadUserByUsername(user.getUsername()));
        return AuthResponse.builder()
                .token(token)
                .message("User registered successfully")
                .walletId(user.getWalletId())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            var auth = getAuthenticationManager();
            auth.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception e) {
            return AuthResponse.builder()
                    .message("Invalid username or password")
                    .build();
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        UserDetails userDetails = loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        return AuthResponse.builder()
                .token(token)
                .message("Login successful")
                .walletId(user.getWalletId())
                .build();
    }
}