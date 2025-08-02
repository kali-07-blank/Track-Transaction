package com.moneytracker.service.impl;

import com.moneytracker.dto.LoginRequestDTO;
import com.moneytracker.dto.LoginResponseDTO;
import com.moneytracker.dto.PersonDTO;
import com.moneytracker.dto.RegisterRequestDTO;
import com.moneytracker.entity.Person;
import com.moneytracker.exception.AuthenticationException;
import com.moneytracker.exception.DuplicateResourceException;
import com.moneytracker.repository.PersonRepository;
import com.moneytracker.service.AuthService;
import com.moneytracker.service.PersonService;
import com.moneytracker.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException as SpringAuthException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final PersonRepository personRepository;
    private final PersonService personService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthServiceImpl(PersonRepository personRepository,
                           PersonService personService,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil) {
        this.personRepository = personRepository;
        this.personService = personService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public PersonDTO register(RegisterRequestDTO registerRequest) {
        logger.info("Registering new user with username: {}", registerRequest.getUsername());

        // Check for duplicate username
        if (personRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + registerRequest.getUsername());
        }

        // Check for duplicate email
        if (personRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + registerRequest.getEmail());
        }

        // Create new person
        Person person = new Person();
        person.setUsername(registerRequest.getUsername());
        person.setEmail(registerRequest.getEmail());
        person.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        person.setFullName(registerRequest.getFullName());
        person.setRole(Person.Role.USER);
        person.setEnabled(true);

        Person savedPerson = personRepository.save(person);

        logger.info("Successfully registered user with ID: {}", savedPerson.getId());
        return personService.convertToDTO(savedPerson);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        logger.info("Attempting login for identifier: {}", loginRequest.getIdentifier());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getIdentifier(),
                            loginRequest.getPassword()
                    )
            );

            Person person = (Person) authentication.getPrincipal();

            // Generate JWT tokens
            String accessToken = jwtUtil.generateAccessToken(person);
            String refreshToken = jwtUtil.generateRefreshToken(person);

            PersonDTO personDTO = personService.convertToDTO(person);

            logger.info("Successfully authenticated user: {}", person.getUsername());

            return new LoginResponseDTO(
                    accessToken,
                    refreshToken,
                    jwtUtil.getAccessTokenExpiration(),
                    personDTO
            );

        } catch (SpringAuthException e) {
            logger.warn("Authentication failed for identifier: {}", loginRequest.getIdentifier());
            throw new AuthenticationException("Invalid credentials");
        }
    }

    @Override
    public LoginResponseDTO refreshToken(String refreshToken) {
        logger.info("Refreshing token");

        try {
            if (!jwtUtil.isTokenValid(refreshToken)) {
                throw new AuthenticationException("Invalid refresh token");
            }

            String username = jwtUtil.extractUsername(refreshToken);
            Person person = personRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            String newAccessToken = jwtUtil.generateAccessToken(person);
            String newRefreshToken = jwtUtil.generateRefreshToken(person);

            PersonDTO personDTO = personService.convertToDTO(person);

            logger.info("Successfully refreshed token for user: {}", person.getUsername());

            return new LoginResponseDTO(
                    newAccessToken,
                    newRefreshToken,
                    jwtUtil.getAccessTokenExpiration(),
                    personDTO
            );

        } catch (Exception e) {
            logger.warn("Token refresh failed: {}", e.getMessage());
            throw new AuthenticationException("Token refresh failed");
        }
    }

    @Override
    public void logout(String token) {
        logger.info("Logging out user");

        try {
            String username = jwtUtil.extractUsername(token);
            // Add token to blacklist or invalidate in Redis/database
            jwtUtil.invalidateToken(token);

            logger.info("Successfully logged out user: {}", username);
        } catch (Exception e) {
            logger.warn("Logout failed: {}", e.getMessage());
        }
    }
}
