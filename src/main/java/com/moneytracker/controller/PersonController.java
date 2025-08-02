/ PersonController.java
        package com.moneytracker.controller;

import com.moneytracker.dto.LoginRequestDTO;
import com.moneytracker.dto.LoginResponseDTO;
import com.moneytracker.dto.PersonDTO;
import com.moneytracker.service.PersonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/persons")
@CrossOrigin(origins = "*")
public class PersonController {

    private final PersonService personService;

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping("/register")
    public ResponseEntity<PersonDTO> registerPerson(@Valid @RequestBody PersonDTO personDTO) {
        PersonDTO createdPerson = personService.createPerson(personDTO);
        return new ResponseEntity<>(createdPerson, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginPerson(@Valid @RequestBody LoginRequestDTO loginRequest) {
        boolean isAuthenticated = personService.authenticatePerson(
                loginRequest.getIdentifier(),
                loginRequest.getPassword()
        );

        if (isAuthenticated) {
            PersonDTO person = personService.getPersonByUsername(loginRequest.getIdentifier());
            if (person == null) {
                // Try by email if username lookup failed
                try {
                    person = personService.getPersonByUsername(loginRequest.getIdentifier());
                } catch (Exception e) {
                    // Handle case where identifier might be email
                }
            }

            LoginResponseDTO response = new LoginResponseDTO(true, "Login successful", person);
            return ResponseEntity.ok(response);
        } else {
            LoginResponseDTO response = new LoginResponseDTO(false, "Invalid credentials", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonDTO> getPersonById(@PathVariable Long id) {
        PersonDTO person = personService.getPersonById(id);
        return ResponseEntity.ok(person);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<PersonDTO> getPersonByUsername(@PathVariable String username) {
        PersonDTO person = personService.getPersonByUsername(username);
        return ResponseEntity.ok(person);
    }

    @GetMapping
    public ResponseEntity<List<PersonDTO>> getAllPersons() {
        List<PersonDTO> persons = personService.getAllPersons();
        return ResponseEntity.ok(persons);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonDTO> updatePerson(@PathVariable Long id, @Valid @RequestBody PersonDTO personDTO) {
        PersonDTO updatedPerson = personService.updatePerson(id, personDTO);
        return ResponseEntity.ok(updatedPerson);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }
}
