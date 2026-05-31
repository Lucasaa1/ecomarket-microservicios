package com.ecomarket.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductoRequest(
        @NotBlank String nombre,
        @NotBlank String descripcion,
        @Positive Double precio,
        @Min(0) Integer stock,
        @NotNull Integer categoriaId
) {
}
