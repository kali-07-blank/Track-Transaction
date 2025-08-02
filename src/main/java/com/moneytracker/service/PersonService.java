package com.moneytracker.service;

import com.moneytracker.dto.PersonDTO;

import java.util.List;
import java.util.Optional;

public interface PersonService {

    PersonDTO createPerson(PersonDTO personDTO);

    Optional<PersonDTO> getPersonById(Long id);

    Optional<PersonDTO> getPersonByUsername(String username);

    List<PersonDTO> getAllPersons();

    PersonDTO updatePerson(Long id, PersonDTO personDTO);

    void deletePerson(Long id);

    boolean authenticatePerson(String identifier, String password);
}
