package com.moneytracker.controller;

import com.moneytracker.dto.LoginRequestDTO;
import com.moneytracker.dto.LoginResponseDTO;
import com.moneytracker.dto.PersonDTO;
import com.moneytracker.exception.ResourceNotFoundException;
import com.moneytracker.service.AuthService;
import com.moneytracker.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<PersonDTO> register(@Valid @RequestBody PersonDTO personDTO) {
        PersonDTO registeredPerson = authService.register(personDTO);
        return ResponseEntity.ok(registeredPerson);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        // Authenticate the user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getIdentifier(),
                        loginRequest.getPassword())
        );

        // Load user details for JWT generation
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getIdentifier());
        String token = jwtUtil.generateToken(userDetails);

        // Fetch PersonDTO (username or email)
        PersonDTO person = authService.getPersonByIdentifier(loginRequest.getIdentifier())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Person not found with identifier: " + loginRequest.getIdentifier()
                ));

        // Build response
        LoginResponseDTO response = new LoginResponseDTO(
                true,
                "Login successful",
                token,
                userDetails.getUsername(),
                person
        );

        return ResponseEntity.ok(response);
    }
}
