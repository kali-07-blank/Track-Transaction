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
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PersonServiceImpl(PersonRepository personRepository, PasswordEncoder passwordEncoder) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PersonDTO createPerson(PersonDTO personDTO) {
        // Check if username already exists
        if (personRepository.findByUsername(personDTO.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already exists: " + personDTO.getUsername());
        }

        // Check if email already exists
        if (personRepository.findByEmail(personDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already exists: " + personDTO.getEmail());
        }

        Person person = convertToEntity(personDTO);
        person.setPassword(passwordEncoder.encode(person.getPassword()));

        Person savedPerson = personRepository.save(person);
        return convertToDTO(savedPerson);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonDTO> getPersonById(Long id) {
        Optional<Person> person = personRepository.findById(id);
        return person.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonDTO> getAllPersons(Pageable pageable) {
        return personRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public PersonDTO updatePerson(Long id, PersonDTO personDTO) {
        Person existingPerson = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + id));

        // Check if username is being changed and if it already exists
        if (!existingPerson.getUsername().equals(personDTO.getUsername())) {
            Optional<Person> personWithUsername = personRepository.findByUsername(personDTO.getUsername());
            if (personWithUsername.isPresent() && !personWithUsername.get().getId().equals(id)) {
                throw new DuplicateResourceException("Username already exists: " + personDTO.getUsername());
            }
        }

        // Check if email is being changed and if it already exists
        if (!existingPerson.getEmail().equals(personDTO.getEmail())) {
            Optional<Person> personWithEmail = personRepository.findByEmail(personDTO.getEmail());
            if (personWithEmail.isPresent() && !personWithEmail.get().getId().equals(id)) {
                throw new DuplicateResourceException("Email already exists: " + personDTO.getEmail());
            }
        }

        existingPerson.setUsername(personDTO.getUsername());
        existingPerson.setEmail(personDTO.getEmail());
        existingPerson.setFullName(personDTO.getFullName());

        // Only update password if it's provided and different
        if (personDTO.getPassword() != null && !personDTO.getPassword().trim().isEmpty()) {
            existingPerson.setPassword(passwordEncoder.encode(personDTO.getPassword()));
        }

        Person updatedPerson = personRepository.save(existingPerson);
        return convertToDTO(updatedPerson);
    }

    @Override
    public void deletePerson(Long id) {
        if (!personRepository.existsById(id)) {
            throw new ResourceNotFoundException("Person not found with id: " + id);
        }
        personRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonDTO> getPersonByUsername(String username) {
        Optional<Person> person = personRepository.findByUsername(username);
        return person.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonDTO> getPersonByEmail(String email) {
        Optional<Person> person = personRepository.findByEmail(email);
        return person.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return personRepository.findByUsername(username).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return personRepository.findByEmail(email).isPresent();
    }

    @Override
    public PersonDTO convertToDTO(Person person) {
        PersonDTO dto = new PersonDTO();
        dto.setId(person.getId());
        dto.setUsername(person.getUsername());
        dto.setEmail(person.getEmail());
        dto.setFullName(person.getFullName());
        // Note: Don't include password in DTO for security
        return dto;
    }

    @Override
    public Person convertToEntity(PersonDTO dto) {
        Person person = new Person();
        person.setId(dto.getId());
        person.setUsername(dto.getUsername());
        person.setEmail(dto.getEmail());
        person.setFullName(dto.getFullName());
        person.setPassword(dto.getPassword());
        return person;
    }
}