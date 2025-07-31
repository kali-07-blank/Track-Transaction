package com.moneytracker.controller;

import com.moneytracker.dto.PersonDTO;
import com.moneytracker.service.JwtService;
import com.moneytracker.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/people")
@CrossOrigin(origins = "*")
public class PersonController {

    @Autowired
    private PersonService personService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/all")
    public ResponseEntity<List<PersonDTO>> getAllPeople(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(personService.getAllPeople(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<String> addPerson(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String name) {
        Long userId = extractUserId(authHeader);
        personService.addPerson(userId, name);
        return ResponseEntity.ok("Person '" + name + "' added successfully!");
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<String> deletePerson(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String name) {
        Long userId = extractUserId(authHeader);
        personService.deletePerson(userId, name);
        return ResponseEntity.ok("Person '" + name + "' deleted successfully!");
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMoney(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String name,
            @RequestParam Double amount,
            @RequestParam(required = false) String description) {
        Long userId = extractUserId(authHeader);
        personService.sendMoney(userId, name, amount, description);
        return ResponseEntity.ok("Money sent successfully!");
    }

    @PostMapping("/receive")
    public ResponseEntity<String> receiveMoney(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String name,
            @RequestParam Double amount,
            @RequestParam(required = false) String description) {
        Long userId = extractUserId(authHeader);
        personService.receiveMoney(userId, name, amount, description);
        return ResponseEntity.ok("Money received successfully!");
    }

    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        return jwtService.getUserIdFromToken(authHeader.substring(7));
    }
}
