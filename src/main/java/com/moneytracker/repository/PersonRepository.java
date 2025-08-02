package com.moneytracker.repository;

import com.moneytracker.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    /**
     * Find person by username
     */
    Optional<Person> findByUsername(String username);

    /**
     * Find person by email
     */
    Optional<Person> findByEmail(String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find person by username or email
     */
    @Query("SELECT p FROM Person p WHERE p.username = :identifier OR p.email = :identifier")
    Optional<Person> findByUsernameOrEmail(@Param("identifier") String identifier);

    /**
     * Find person with transactions by username
     */
    @Query("SELECT p FROM Person p LEFT JOIN FETCH p.transactions WHERE p.username = :username")
    Optional<Person> findByUsernameWithTransactions(@Param("username") String username);
}