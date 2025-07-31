package com.moneytracker.service;

import com.moneytracker.dto.LoginRequest;
import com.moneytracker.dto.LoginResponse;
import com.moneytracker.entity.User;
import com.moneytracker.exception.DuplicateResourceException;
import com.moneytracker.exception.ResourceNotFoundException;
import com.moneytracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials"));

        // For now we compare plain text (later you can add BCrypt encoder)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new ResourceNotFoundException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername());

        return new LoginResponse(token, user.getUsername(), "Login successful");
    }

    public User registerUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // TODO: Hash password with BCrypt in production
        return userRepository.save(user);
    }
}
