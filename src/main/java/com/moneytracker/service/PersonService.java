package com.moneytracker.service;

import com.moneytracker.dto.PersonDTO;
import com.moneytracker.entity.Person;
import java.util.List;
import java.util.Optional;

public interface PersonService {
    PersonDTO createPerson(PersonDTO personDTO);
    Optional<PersonDTO> getPersonById(Long id);
    List<PersonDTO> getAllPersons();
    PersonDTO updatePerson(Long id, PersonDTO personDTO);
    void deletePerson(Long id);
    Optional<PersonDTO> getPersonByUsername(String username);
    PersonDTO convertToDTO(Person person);
    Person convertToEntity(PersonDTO personDTO);
}