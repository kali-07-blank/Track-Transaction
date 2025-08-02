package com.moneytracker.service;

import com.moneytracker.dto.LoginRequestDTO;
import com.moneytracker.dto.LoginResponseDTO;
import com.moneytracker.dto.PersonDTO;
import com.moneytracker.entity.Person;
import com.moneytracker.exception.ResourceNotFoundException;
import com.moneytracker.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final PersonService personService;

    @Autowired
    public AuthService(PersonRepository personRepository,
                       PasswordEncoder passwordEncoder,
                       PersonService personService) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.personService = personService;
    }

    public LoginResponseDTO authenticate(LoginRequestDTO loginRequest) {
        Optional<Person> personOpt = personRepository.findByUsernameOrEmail(loginRequest.getIdentifier());

        if (personOpt.isPresent()) {
            Person person = personOpt.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), person.getPassword())) {
                PersonDTO personDTO = convertToDTO(person);

                // TODO: Replace with actual JWT generation
                return new LoginResponseDTO(
                        true,
                        "Login successful",
                        "GENERATED_TOKEN_HERE",
                        person.getUsername(),
                        personDTO
                );
            }
        }

        return new LoginResponseDTO(
                false,
                "Invalid credentials",
                null,
                loginRequest.getIdentifier(),
                null
        );
    }

    public PersonDTO register(PersonDTO personDTO) {
        return personService.createPerson(personDTO);
    }

    /**
     * ✅ Added: Fetch a Person by username or email for AuthController
     */
    public Optional<PersonDTO> getPersonByIdentifier(String identifier) {
        return personRepository.findByUsername(identifier)
                .map(this::convertToDTO)
                .or(() -> personRepository.findByEmail(identifier).map(this::convertToDTO));
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
