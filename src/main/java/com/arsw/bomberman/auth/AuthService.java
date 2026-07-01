package com.arsw.bomberman.auth;

import com.arsw.bomberman.events.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByUsernameOrEmail(request.getUsername(), request.getEmail())) {
            eventPublisher.publishEvent(
                    new UserRegistrationFailedEvent(request.getUsername(), "username_or_email_duplicate")
            );
            throw new IllegalStateException("El nombre de usuario o email ya existe");
        }

        try {
            String hash = passwordEncoder.encode(request.getPassword());
            User user = new User(request.getUsername(), request.getEmail(), hash);
            User saved = userRepository.save(user);

            eventPublisher.publishEvent(new UserRegisteredEvent(saved.getId(), saved.getUsername()));

            return saved;
        } catch (DataIntegrityViolationException e) {
            eventPublisher.publishEvent(
                    new UserRegistrationFailedEvent(request.getUsername(), "constraint_violation")
            );
            throw new IllegalStateException("El nombre de usuario o email ya existe");
        }
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> {
                    eventPublisher.publishEvent(
                            new UserLoginFailedEvent(request.getUsername(), "user_not_found")
                    );
                    throw new IllegalArgumentException("Credenciales inválidas");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            eventPublisher.publishEvent(
                    new UserLoginFailedEvent(request.getUsername(), "wrong_password")
            );
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername());

        eventPublisher.publishEvent(new UserLoggedInEvent(user.getId(), user.getUsername()));

        return new AuthResponse(token, user.getId(), user.getUsername());
    }
}