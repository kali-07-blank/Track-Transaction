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

public interface PersonService {
    PersonDTO createPerson(PersonDTO personDTO);
    PersonDTO getPersonById(Long id);
    PersonDTO getPersonByUsername(String username);
    List<PersonDTO> getAllPersons();
    PersonDTO updatePerson(Long id, PersonDTO personDTO);
    void deletePerson(Long id);
    boolean authenticatePerson(String identifier, String password);
}

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
        validatePersonData(personDTO);

        if (personRepository.existsByUsername(personDTO.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + personDTO.getUsername());
        }

        if (personRepository.existsByEmail(personDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + personDTO.getEmail());
        }

        Person person = new Person();
        person.setUsername(personDTO.getUsername());
        person.setEmail(personDTO.getEmail());
        person.setPassword(passwordEncoder.encode(personDTO.getPassword()));
        person.setFullName(personDTO.getFullName());

        Person savedPerson = personRepository.save(person);
        return convertToDTO(savedPerson);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonDTO getPersonById(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + id));
        return convertToDTO(person);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonDTO getPersonByUsername(String username) {
        Person person = personRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with username: " + username));
        return convertToDTO(person);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonDTO> getAllPersons() {
        return personRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PersonDTO updatePerson(Long id, PersonDTO personDTO) {
        Person existingPerson = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + id));

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

        existingPerson.setUsername(personDTO.getUsername());
        existingPerson.setEmail(personDTO.getEmail());
        existingPerson.setFullName(personDTO.getFullName());

        if (personDTO.getPassword() != null && !personDTO.getPassword().isEmpty()) {
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
    public boolean authenticatePerson(String identifier, String password) {
        Person person = personRepository.findByUsernameOrEmail(identifier)
                .orElse(null);

        if (person == null) {
            return false;
        }

        return passwordEncoder.matches(password, person.getPassword());
    }

    private void validatePersonData(PersonDTO personDTO) {
        if (personDTO.getUsername() == null || personDTO.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (personDTO.getEmail() == null || personDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (personDTO.getPassword() == null || personDTO.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (personDTO.getFullName() == null || personDTO.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
    }

    private PersonDTO convertToDTO(Person person) {
        PersonDTO dto = new PersonDTO();
        dto.setId(person.getId());
        dto.setUsername(person.getUsername());
        dto.setEmail(person.getEmail());
        dto.setFullName(person.getFullName());
        return dto;
    }
}