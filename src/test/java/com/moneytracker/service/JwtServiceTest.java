/ JwtServiceTest.java
        package com.moneytracker.service;

import com.moneytracker.dto.PersonDTO;
import com.moneytracker.entity.Person;
import com.moneytracker.exception.DuplicateResourceException;
import com.moneytracker.exception.ResourceNotFoundException;
import com.moneytracker.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PersonServiceImpl personService;

    private PersonDTO personDTO;
    private Person person;

    @BeforeEach
    void setUp() {
        personDTO = new PersonDTO();
        personDTO.setUsername("testuser");
        personDTO.setEmail("test@example.com");
        personDTO.setPassword("password123");
        personDTO.setFullName("Test User");

        person = new Person();
        person.setId(1L);
        person.setUsername("testuser");
        person.setEmail("test@example.com");
        person.setPassword("encodedPassword");
        person.setFullName("Test User");
    }

    @Test
    void createPerson_Success() {
        // Given
        when(personRepository.existsByUsername(anyString())).thenReturn(false);
        when(personRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(personRepository.save(any(Person.class))).thenReturn(person);

        // When
        PersonDTO result = personService.createPerson(personDTO);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getFullName());
        verify(personRepository).save(any(Person.class));
    }

    @Test
    void createPerson_DuplicateUsername() {
        // Given
        when(personRepository.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> {
            personService.createPerson(personDTO);
        });

        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    void getPersonById_Success() {
        // Given
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));

        // When
        PersonDTO result = personService.getPersonById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getPersonById_NotFound() {
        // Given
        when(personRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            personService.getPersonById(1L);
        });
    }

    @Test
    void authenticatePerson_Success() {
        // Given
        when(personRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.of(person));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // When
        boolean result = personService.authenticatePerson("testuser", "password123");

        // Then
        assertTrue(result);
    }

    @Test
    void authenticatePerson_InvalidCredentials() {
        // Given
        when(personRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.of(person));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When
        boolean result = personService.authenticatePerson("testuser", "wrongpassword");

        // Then
        assertFalse(result);
    }
}