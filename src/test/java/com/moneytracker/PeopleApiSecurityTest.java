package com.moneytracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneytracker.dto.PersonDTO;
import com.moneytracker.service.PersonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonController.class)
class PeopleApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerPerson_WithoutAuthentication_ShouldSucceed() throws Exception {
        // Given
        PersonDTO personDTO = new PersonDTO();
        personDTO.setUsername("testuser");
        personDTO.setEmail("test@example.com");
        personDTO.setPassword("password123");
        personDTO.setFullName("Test User");

        PersonDTO savedPersonDTO = new PersonDTO();
        savedPersonDTO.setId(1L);
        savedPersonDTO.setUsername("testuser");
        savedPersonDTO.setEmail("test@example.com");
        savedPersonDTO.setFullName("Test User");

        when(personService.createPerson(any(PersonDTO.class))).thenReturn(savedPersonDTO);

        // When & Then
        mockMvc.perform(post("/api/persons/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    void getPersonById_WithAuthentication_ShouldSucceed() throws Exception {
        // Given
        PersonDTO personDTO = new PersonDTO();
        personDTO.setId(1L);
        personDTO.setUsername("testuser");
        personDTO.setEmail("test@example.com");
        personDTO.setFullName("Test User");

        when(personService.getPersonById(1L)).thenReturn(java.util.Optional.of(personDTO));

        // When & Then
        mockMvc.perform(get("/api/persons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }
}
