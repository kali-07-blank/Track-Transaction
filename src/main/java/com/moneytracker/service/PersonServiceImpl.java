package com.moneytracker.service;

import com.moneytracker.dto.PersonDTO;
import com.moneytracker.entity.Person;
import com.moneytracker.exception.DuplicateResourceException;
import com.moneytracker.exception.ResourceNotFoundException;
import com.moneytracker.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of PersonService interface
 * Handles all business logic related to Person operations
 * Follows OOP principles: Encapsulation, Inheritance, Polymorphism
 */
@Service
@Transactional
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor injection for dependencies (Dependency Injection principle)
     */
    @Autowired
    public PersonServiceImpl(PersonRepository personRepository, PasswordEncoder passwordEncoder) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new person with encrypted password
     * Implements business validation and duplicate checking
     */
    @Override
    public PersonDTO createPerson(PersonDTO personDTO) {
        // Input validation (Defensive programming)
        validatePersonData(personDTO);

        // Business rule: Check for duplicate username
        if (personRepository.existsByUsername(personDTO.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + personDTO.getUsername());
        }

        // Business rule: Check for duplicate email
        if (personRepository.existsByEmail(personDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + personDTO.getEmail());
        }

        // Create entity from DTO (Data mapping)
        Person person = new Person();
        person.setUsername(personDTO.getUsername());
        person.setEmail(personDTO.getEmail());
        person.setPassword(passwordEncoder.encode(personDTO.getPassword())); // Security: Password encryption
        person.setFullName(personDTO.getFullName());

        // Persist entity
        Person savedPerson = personRepository.save(person);

        // Convert back to DTO (Abstraction: Hide internal entity structure)
        return convertToDTO(savedPerson);
    }

    /**
     * Retrieves person by ID with error handling
     */
    @Override
    @Transactional(readOnly = true)
    public PersonDTO getPersonById(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + id));
        return convertToDTO(person);
    }

    /**
     * Retrieves person by username with error handling
     */
    @Override
    @Transactional(readOnly = true)
    public PersonDTO getPersonByUsername(String username) {
        Person person = personRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with username: " + username));
        return convertToDTO(person);
    }

    /**
     * Retrieves all persons and converts to DTOs
     * Uses Java 8 Streams for functional programming
     */
    @Override
    @Transactional(readOnly = true)
    public List<PersonDTO> getAllPersons() {
        return personRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates existing person with validation
     * Implements optimistic updates with duplicate checking
     */
    @Override
    public PersonDTO updatePerson(Long id, PersonDTO personDTO) {
        // Find existing person
        Person existingPerson = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + id));

        // Validate input data
        validatePersonDataForUpdate(personDTO);

        // Check if username is being changed and if it's already taken
        if (!existingPerson.getUsername().equals(personDTO.getUsername()) &&
                personRepository.existsByUsername(personDTO.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + personDTO.getUsername());
        }

        // Check if email is being changed and if it's already taken
        if (!existingPerson.getEmail().equals(personDTO.getEmail()) &&
                personRepository.existsByEmail(personDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + personDTO.getEmail());
        }

        // Update entity fields
        existingPerson.setUsername(personDTO.getUsername());
        existingPerson.setEmail(personDTO.getEmail());
        existingPerson.setFullName(personDTO.getFullName());

        // Update password only if provided (Security consideration)
        if (personDTO.getPassword() != null && !personDTO.getPassword().trim().isEmpty()) {
            existingPerson.setPassword(passwordEncoder.encode(personDTO.getPassword()));
        }

        // Save and return updated entity
        Person updatedPerson = personRepository.save(existingPerson);
        return convertToDTO(updatedPerson);
    }

    /**
     * Deletes person by ID with existence check
     */
    @Override
    public void deletePerson(Long id) {
        if (!personRepository.existsById(id)) {
            throw new ResourceNotFoundException("Person not found with id: " + id);
        }
        personRepository.deleteById(id);
    }

    /**
     * Authenticates person using username/email and password
     * Implements secure authentication logic
     */
    @Override
    @Transactional(readOnly = true)
    public boolean authenticatePerson(String identifier, String password) {
        // Find person by username or email
        Person person = personRepository.findByUsernameOrEmail(identifier)
                .orElse(null);

        // Check if person exists and password matches
        if (person == null) {
            return false;
        }

        // Use password encoder to verify password (Security)
        return passwordEncoder.matches(password, person.getPassword());
    }

    /**
     * Private helper method for input validation (Encapsulation)
     * Implements business rules for person data
     */
    private void validatePersonData(PersonDTO personDTO) {
        if (personDTO.getUsername() == null || personDTO.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (personDTO.getUsername().length() < 3 || personDTO.getUsername().length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }

        if (personDTO.getEmail() == null || personDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!isValidEmail(personDTO.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (personDTO.getPassword() == null || personDTO.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        if (personDTO.getFullName() == null || personDTO.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
    }

    /**
     * Validation for update operations (password is optional)
     */
    private void validatePersonDataForUpdate(PersonDTO personDTO) {
        if (personDTO.getUsername() == null || personDTO.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (personDTO.getUsername().length() < 3 || personDTO.getUsername().length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }

        if (personDTO.getEmail() == null || personDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!isValidEmail(personDTO.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (personDTO.getFullName() == null || personDTO.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }

        // Password validation only if provided
        if (personDTO.getPassword() != null && !personDTO.getPassword().trim().isEmpty() &&
                personDTO.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }

    /**
     * Email validation helper method
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Converts Person entity to PersonDTO (Data mapping)
     * Implements abstraction by hiding internal entity structure
     * Note: Password is not included in DTO for security
     */
    private PersonDTO convertToDTO(Person person) {
        PersonDTO dto = new PersonDTO();
        dto.setId(person.getId());
        dto.setUsername(person.getUsername());
        dto.setEmail(person.getEmail());
        dto.setFullName(person.getFullName());
        // Note: Password is intentionally not set for security reasons
        return dto;
    }
}