// LoginResponseDTO.java
package com.moneytracker.dto;

public class LoginResponseDTO {
    private boolean success;
    private String message;
    private PersonDTO person;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(boolean success, String message, PersonDTO person) {
        this.success = success;
        this.message = message;
        this.person = person;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PersonDTO getPerson() {
        return person;
    }

    public void setPerson(PersonDTO person) {
        this.person = person;
    }
}