package com.moneytracker.service.impl;

import com.moneytracker.dto.PersonDTO;
import com.moneytracker.entity.Person;
import com.moneytracker.exception.DuplicateResourceException;
import com.moneytracker.exception.ResourceNotFoundException;
import com.moneytracker.repository.PersonRepository;
import com.moneytracker.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of PersonService
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
@Service
@Transactional
public class PersonServiceImpl implements PersonService {

    private static final Logger logger = LoggerFactory.getLogger(PersonServiceImpl.class);

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PersonServiceImpl(PersonRepository personRepository, PasswordEncoder passwordEncoder) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PersonDTO createPerson(PersonDTO personDTO) {
        logger.info("Creating new person with username: {}", personDTO.getUsername());

        // Check for duplicate username
        if (personRepository.existsByUsername(personDTO.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + personDTO.getUsername());
        }

        // Check for duplicate email
        if (personRepository.existsByEmail(personDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + personDTO.getEmail());
        }

        Person person = convertToEntity(personDTO);
        Person savedPerson = personRepository.save(person);

        logger.info("Successfully created person with ID: {}", savedPerson.getId());
        return convertToDTO(savedPerson);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonDTO> getPersonById(Long id) {
        logger.debug("Fetching person by ID: {}", id);
        return personRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonDTO> getPersonByUsername(String username) {
        logger.debug("Fetching person by username: {}", username);
        return personRepository.findByUsername(username).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonDTO> getPersonByEmail(String email) {
        logger.debug("Fetching person by email: {}", email);
        return personRepository.findByEmail(email).map(this::convertToDTO);
    }

    @Override
    public PersonDTO updatePerson(Long id, PersonDTO personDTO) {
        logger.info("Updating person with ID: {}", id);

        Person existingPerson = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with ID: " + id));

        // Check for duplicate username (excluding current person)
        if (!existingPerson.getUsername().equals(personDTO.getUsername()) &&
                personRepository.existsByUsernameAndIdNot(personDTO.getUsername(), id)) {
            throw new DuplicateResourceException("Username already exists: " + personDTO.getUsername());
        }

        // Check for duplicate email (excluding current person)
        if (!existingPerson.getEmail().equals(personDTO.getEmail()) &&
                personRepository.existsByEmailAndIdNot(personDTO.getEmail(), id)) {
            throw new DuplicateResourceException("Email already exists: " + personDTO.getEmail());
        }

        // Update fields
        existingPerson.setUsername(personDTO.getUsername());
        existingPerson.setEmail(personDTO.getEmail());
        existingPerson.setFullName(personDTO.getFullName());

        Person updatedPerson = personRepository.save(existingPerson);

        logger.info("Successfully updated person with ID: {}", id);
        return convertToDTO(updatedPerson);
    }

    @Override
    public void deletePerson(Long id) {
        logger.info("Deleting person with ID: {}", id);

        if (!personRepository.existsById(id)) {
            throw new ResourceNotFoundException("Person not found with ID: " + id);
        }

        personRepository.deleteById(id);
        logger.info("Successfully deleted person with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonDTO> getAllPersons(Pageable pageable) {
        logger.debug("Fetching all persons with pagination: {}", pageable);
        return personRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return personRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return personRepository.existsByEmail(email);
    }

    @Override
    public PersonDTO convertToDTO(Person person) {
        if (person == null) {
            return null;
        }

        PersonDTO dto = new PersonDTO();
        dto.setId(person.getId());
        dto.setUsername(person.getUsername());
        dto.setEmail(person.getEmail());
        dto.setFullName(person.getFullName());
        dto.setRole(person.getRole().name());
        dto.setCreatedDate(person.getCreatedDate());
        dto.setLastModifiedDate(person.getLastModifiedDate());

        return dto;
    }

    @Override
    public Person convertToEntity(PersonDTO personDTO) {
        if (personDTO == null) {
            return null;
        }

        Person person = new Person();
        person.setId(personDTO.getId());
        person.setUsername(personDTO.getUsername());
        person.setEmail(personDTO.getEmail());
        person.setFullName(personDTO.getFullName());

        if (personDTO.getRole() != null) {
            person.setRole(Person.Role.valueOf(personDTO.getRole()));
        }

        return person;
    }
}