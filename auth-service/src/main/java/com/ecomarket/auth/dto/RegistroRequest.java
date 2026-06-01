package com.ecomarket.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Size(min = 2, max = 100) String nombre,
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 6, max = 100) String password,
        @NotBlank @Size(min = 5, max = 7) String rol
) {
}
