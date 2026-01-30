package com.estudo.userService.services;

import com.estudo.userService.dtos.AuthResponse;
import com.estudo.userService.dtos.LoginRequest;
import com.estudo.userService.dtos.RegisterRequest;
import com.estudo.userService.dtos.UserResponse;
import com.estudo.userService.entities.User;
import com.estudo.userService.enums.UserRole;
import com.estudo.userService.producers.UserProducer;
import com.estudo.userService.repository.UserRepository;
import com.estudo.userService.security.CustomUserDetails;
import com.estudo.userService.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserProducer userProducer;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager,
                       UserProducer userProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userProducer = userProducer;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        if (userRepository.existsByCpf(request.cpf())) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }

        var user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setCpf(request.cpf());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole(UserRole.CITIZEN);
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);

        var savedUser = userRepository.save(user);

        // publica mensagem no RabbitMQ para envio de email de boas-vindas
        userProducer.publishUserCreatedEvent(savedUser);

        var userDetails = new CustomUserDetails(savedUser);

        Map<String, Object> extraClaims = Map.of(
                "role", savedUser.getRole().name(),
                "userId", savedUser.getId().toString()
        );

        var accessToken = jwtService.generateToken(extraClaims, userDetails);

        return new AuthResponse(accessToken, mapToUserResponse(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        var userDetails = new CustomUserDetails(user);

        Map<String, Object> extraClaims = Map.of(
                "role", user.getRole().name(),
                "userId", user.getId().toString()
        );

        var accessToken = jwtService.generateToken(extraClaims, userDetails);

        return new AuthResponse(accessToken, mapToUserResponse(user));
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCpf(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}