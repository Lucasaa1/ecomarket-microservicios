package com.ecomarket.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProductoRequest(
        @NotBlank @Size(min = 2, max = 120) String nombre,
        @NotBlank @Size(min = 5, max = 500) String descripcion,
        @NotNull @Positive Double precio,
        @NotNull @Min(0) Integer stock,
        @NotNull @Positive Integer categoriaId
) {
}
