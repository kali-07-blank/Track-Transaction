package com.moneytracker.service;

import com.moneytracker.dto.PersonDTO;
import com.moneytracker.entity.Person;
import com.moneytracker.entity.Transaction;
import com.moneytracker.entity.User;
import com.moneytracker.enums.TransactionType;
import com.moneytracker.exception.DuplicateResourceException;
import com.moneytracker.exception.ResourceNotFoundException;
import com.moneytracker.repository.PersonRepository;
import com.moneytracker.repository.TransactionRepository;
import com.moneytracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public List<PersonDTO> getAllPeople(Long userId) {
        return personRepository.findByUserIdOrderByNameAsc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PersonDTO addPerson(Long userId, String name) {
        if (personRepository.findByUserIdAndName(userId, name).isPresent()) {
            throw new DuplicateResourceException("Person with name '" + name + "' already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Person person = new Person();
        person.setName(name);
        person.setBalance(0.0);
        person.setUser(user);

        return convertToDTO(personRepository.save(person));
    }

    public void deletePerson(Long userId, String name) {
        Person person = personRepository.findByUserIdAndName(userId, name)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found: " + name));
        personRepository.delete(person);
    }

    public PersonDTO sendMoney(Long userId, String personName, Double amount, String description) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        Person person = personRepository.findByUserIdAndName(userId, personName)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found: " + personName));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Subtract from balance
        person.subtractFromBalance(amount);

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.SEND);
        transaction.setAmount(amount);
        transaction.setDescription(description != null ? description : "");
        transaction.setUser(user);
        transaction.setPerson(person);

        personRepository.save(person);
        transactionRepository.save(transaction);

        return convertToDTO(person);
    }

    public PersonDTO receiveMoney(Long userId, String personName, Double amount, String description) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        Person person = personRepository.findByUserIdAndName(userId, personName)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found: " + personName));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Add to balance
        person.addToBalance(amount);

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.RECEIVE);
        transaction.setAmount(amount);
        transaction.setDescription(description != null ? description : "");
        transaction.setUser(user);
        transaction.setPerson(person);

        personRepository.save(person);
        transactionRepository.save(transaction);

        return convertToDTO(person);
    }

    private PersonDTO convertToDTO(Person person) {
        return new PersonDTO(
                person.getId(),
                person.getName(),
                person.getBalance(),
                person.getCreatedAt()
        );
    }
}
