package com.moneytracker.controller;

import com.moneytracker.dto.LoginRequestDTO;
import com.moneytracker.dto.LoginResponseDTO;
import com.moneytracker.dto.PersonDTO;
import com.moneytracker.service.AuthService;
import com.moneytracker.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getIdentifier(),
                        loginRequest.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getIdentifier());
        String token = jwtUtil.generateToken(userDetails);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setUsername(userDetails.getUsername());

        return ResponseEntity.ok(response);
    }

}