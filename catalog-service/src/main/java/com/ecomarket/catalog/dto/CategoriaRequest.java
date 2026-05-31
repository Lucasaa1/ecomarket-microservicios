package com.ecomarket.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
        @NotBlank String nombre
) {
}
