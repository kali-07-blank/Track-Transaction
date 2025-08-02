package com.moneytracker.service;

import com.moneytracker.dto.PersonDTO;
import com.moneytracker.entity.Person;
import com.moneytracker.exception.DuplicateResourceException;
import com.moneytracker.exception.ResourceNotFoundException;
import com.moneytracker.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
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
        if (personRepository.existsByUsername(personDTO.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (personRepository.existsByEmail(personDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
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
    public Optional<PersonDTO> getPersonById(Long id) {
        return personRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    public Optional<PersonDTO> getPersonByUsername(String username) {
        return personRepository.findByUsername(username).map(this::convertToDTO);
    }

    @Override
    public List<PersonDTO> getAllPersons() {
        return personRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public PersonDTO updatePerson(Long id, PersonDTO personDTO) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + id));

        person.setUsername(personDTO.getUsername());
        person.setEmail(personDTO.getEmail());
        if (personDTO.getPassword() != null) {
            person.setPassword(passwordEncoder.encode(personDTO.getPassword()));
        }
        person.setFullName(personDTO.getFullName());

        Person updatedPerson = personRepository.save(person);
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
    public boolean authenticatePerson(String identifier, String password) {
        Optional<Person> optionalPerson = personRepository.findByUsernameOrEmail(identifier);
        if (optionalPerson.isPresent()) {
            Person person = optionalPerson.get();
            return passwordEncoder.matches(password, person.getPassword());
        }
        return false;
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
