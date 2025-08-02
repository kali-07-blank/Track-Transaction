package com.moneytracker.service;

import com.moneytracker.dto.PersonDTO;
import com.moneytracker.entity.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service interface for Person operations
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
public interface PersonService {

    /**
     * Create a new person
     * @param personDTO person data
     * @return created person DTO
     */
    PersonDTO createPerson(PersonDTO personDTO);

    /**
     * Get person by ID
     * @param id person ID
     * @return person DTO if found
     */
    Optional<PersonDTO> getPersonById(Long id);

    /**
     * Get person by username
     * @param username username
     * @return person DTO if found
     */
    Optional<PersonDTO> getPersonByUsername(String username);

    /**
     * Get person by email
     * @param email email
     * @return person DTO if found
     */
    Optional<PersonDTO> getPersonByEmail(String email);

    /**
     * Update person
     * @param id person ID
     * @param personDTO updated person data
     * @return updated person DTO
     */
    PersonDTO updatePerson(Long id, PersonDTO personDTO);

    /**
     * Delete person
     * @param id person ID
     */
    void deletePerson(Long id);

    /**
     * Get all persons with pagination
     * @param pageable pagination info
     * @return page of person DTOs
     */
    Page<PersonDTO> getAllPersons(Pageable pageable);

    /**
     * Check if username exists
     * @param username username to check
     * @return true if exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     * @param email email to check
     * @return true if exists
     */
    boolean existsByEmail(String email);

    /**
     * Convert entity to DTO
     * @param person person entity
     * @return person DTO
     */
    PersonDTO convertToDTO(Person person);

    /**
     * Convert DTO to entity
     * @param personDTO person DTO
     * @return person entity
     */
    Person convertToEntity(PersonDTO personDTO);

    /**
     * Authenticate person with username/email and password
     * @param usernameOrEmail username or email
     * @param password password
     * @return true if authentication successful
     */
    boolean authenticatePerson(String usernameOrEmail, String password);
}