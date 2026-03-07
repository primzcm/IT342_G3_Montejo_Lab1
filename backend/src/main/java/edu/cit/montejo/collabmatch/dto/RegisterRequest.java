package edu.cit.montejo.collabmatch.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Firstname is required")
    @Size(min = 1, max = 100, message = "Firstname is required")
    private String firstname;

    @NotBlank(message = "Lastname is required")
    @Size(min = 1, max = 100, message = "Lastname is required")
    private String lastname;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Must be at least 8 characters")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
