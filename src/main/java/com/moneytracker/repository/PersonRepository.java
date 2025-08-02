package com.moneytracker.repository;

import com.moneytracker.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Person> findByUsername(String username);
    Optional<Person> findByEmail(String email);
}