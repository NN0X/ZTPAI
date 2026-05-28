package com.example.taskmanager.service;

import com.example.taskmanager.domain.AppUser;
import com.example.taskmanager.domain.Role;
import com.example.taskmanager.dto.AuthRequest;
import com.example.taskmanager.dto.AuthResponse;
import com.example.taskmanager.dto.RegisterRequest;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService
{
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;

        public AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        JwtService jwtService)
        {
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.authenticationManager = authenticationManager;
                this.jwtService = jwtService;
        }

        @Transactional
        public AuthResponse register(RegisterRequest request)
        {
                if (userRepository.existsByUsername(request.username()))
                {
                        throw new IllegalArgumentException("Username is already taken");
                }

                AppUser user = new AppUser(
                                request.username(),
                                passwordEncoder.encode(request.password()),
                                Role.USER);
                userRepository.save(user);

                String token = jwtService.generateToken(user.getUsername());
                return new AuthResponse(token, user.getUsername());
        }

        public AuthResponse login(AuthRequest request)
        {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

                String token = jwtService.generateToken(request.username());
                return new AuthResponse(token, request.username());
        }
}
