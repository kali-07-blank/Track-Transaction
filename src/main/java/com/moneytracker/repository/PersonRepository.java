package com.moneytracker.repository;

import com.moneytracker.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    List<Person> findByUserIdOrderByNameAsc(Long userId);
    Optional<Person> findByUserIdAndName(Long userId, String name);
    void deleteByUserIdAndName(Long userId, String name);

    @Query("SELECT COUNT(p) FROM Person p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
