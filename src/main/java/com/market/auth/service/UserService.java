package com.market.auth.service;

import com.market.auth.dto.RegisterRequest;
import com.market.auth.model.User;
import com.market.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final KafkaProducer kafkaProducer;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User register(RegisterRequest request) {

        repository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new RuntimeException("Email already exists");
                });

        User user = User.builder()
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role("USER")
                .build();

        User saved = repository.save(user);

        kafkaProducer.sendUserCreated(saved.getEmail());

        return saved;
    }
}
