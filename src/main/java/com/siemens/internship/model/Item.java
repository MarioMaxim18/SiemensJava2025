package com.siemens.internship.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    @Id // primary key
    @GeneratedValue(strategy = GenerationType.AUTO) // auto-incremented ID
    private Long id;

    @NotBlank(message = "Name is required") // to ensure the field is not empty
    private String name;

    @NotBlank(message = "Description is required") // to ensure the field is not empty
    private String description;

    @NotBlank(message = "Status is required") // to ensure the field is not empty
    private String status;

    // @Email - Built-in validation annotation for email
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$" // regex pattern for email validation
    )
    @NotBlank // to ensure the field is not empty
    private String email;
}