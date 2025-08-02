package com.moneytracker.repository;

import com.moneytracker.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Person entity operations
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    /**
     * Find person by username
     * @param username the username to search for
     * @return Optional containing the person if found
     */
    Optional<Person> findByUsername(String username);

    /**
     * Find person by email
     * @param email the email to search for
     * @return Optional containing the person if found
     */
    Optional<Person> findByEmail(String email);

    /**
     * Find person by username or email
     * @param username the username to search for
     * @param email the email to search for
     * @return Optional containing the person if found
     */
    @Query("SELECT p FROM Person p WHERE p.username = :identifier OR p.email = :identifier")
    Optional<Person> findByUsernameOrEmail(@Param("identifier") String identifier);

    /**
     * Check if username exists
     * @param username the username to check
     * @return true if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     * @param email the email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if username exists excluding a specific person ID
     * @param username the username to check
     * @param personId the person ID to exclude
     * @return true if username exists for another person
     */
    @Query("SELECT COUNT(p) > 0 FROM Person p WHERE p.username = :username AND p.id != :personId")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("personId") Long personId);

    /**
     * Check if email exists excluding a specific person ID
     * @param email the email to check
     * @param personId the person ID to exclude
     * @return true if email exists for another person
     */
    @Query("SELECT COUNT(p) > 0 FROM Person p WHERE p.email = :email AND p.id != :personId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("personId") Long personId);
}
