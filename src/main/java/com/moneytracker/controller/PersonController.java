package com.moneytracker.controller;

import com.moneytracker.dto.PersonDTO;
import com.moneytracker.service.JwtService;
import com.moneytracker.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/people")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;
    private final JwtService jwtService;

    @GetMapping("/all")
    public ResponseEntity<List<PersonDTO>> getAllPeople(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(personService.getAllPeople(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addPerson(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(name = "name", required = false) String name) {
        Long userId = extractUserId(authHeader);
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name parameter is required"));
        }
        personService.addPerson(userId, name.trim());
        return ResponseEntity.ok(Map.of("message", "Person '" + name + "' added successfully!"));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> deletePerson(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String name) {
        Long userId = extractUserId(authHeader);
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        }
        personService.deletePerson(userId, name.trim());
        return ResponseEntity.ok(Map.of("message", "Person '" + name + "' deleted successfully!"));
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMoney(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "amount", required = false) Double amount,
            @RequestParam(required = false) String description) {
        Long userId = extractUserId(authHeader);
        if (name == null || name.trim().isEmpty() || amount == null || amount <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Valid name and amount are required"));
        }
        personService.sendMoney(userId, name.trim(), amount, description);
        return ResponseEntity.ok(Map.of("message", "Money sent successfully!"));
    }

    @PostMapping("/receive")
    public ResponseEntity<?> receiveMoney(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "amount", required = false) Double amount,
            @RequestParam(required = false) String description) {
        Long userId = extractUserId(authHeader);
        if (name == null || name.trim().isEmpty() || amount == null || amount <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Valid name and amount are required"));
        }
        personService.receiveMoney(userId, name.trim(), amount, description);
        return ResponseEntity.ok(Map.of("message", "Money received successfully!"));
    }

    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        return jwtService.getUserIdFromToken(authHeader.substring(7));
    }
}
