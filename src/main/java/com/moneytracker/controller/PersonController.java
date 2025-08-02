package com.moneytracker.controller;

import com.moneytracker.dto.ApiResponse;
import com.moneytracker.dto.PagedResponse;
import com.moneytracker.dto.PersonDTO;
import com.moneytracker.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for person management operations
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/persons")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Person Management", description = "Person management APIs")
public class PersonController {

    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);

    private final PersonService personService;

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get person by ID")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<PersonDTO>> getPersonById(@PathVariable Long id) {
        logger.debug("Get person request for ID: {}", id);

        return personService.getPersonById(id)
                .map(person -> ResponseEntity.ok(ApiResponse.success(person)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<PersonDTO>> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        logger.debug("Get current user request for: {}", username);

        return personService.getPersonByUsername(username)
                .map(person -> ResponseEntity.ok(ApiResponse.success(person)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update person")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<PersonDTO>> updatePerson(
            @PathVariable Long id,
            @Valid @RequestBody PersonDTO personDTO) {

        logger.info("Update person request for ID: {}", id);

        PersonDTO updatedPerson = personService.updatePerson(id, personDTO);

        return ResponseEntity.ok(ApiResponse.success(updatedPerson, "Person updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete person")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<String>> deletePerson(@PathVariable Long id) {
        logger.info("Delete person request for ID: {}", id);

        personService.deletePerson(id);

        return ResponseEntity.ok(ApiResponse.success("Person deleted successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all persons (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<PersonDTO>>> getAllPersons(
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

        logger.debug("Get all persons request with pagination: {}", pageable);

        Page<PersonDTO> persons = personService.getAllPersons(pageable);
        PagedResponse<PersonDTO> response = PagedResponse.of(persons);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}