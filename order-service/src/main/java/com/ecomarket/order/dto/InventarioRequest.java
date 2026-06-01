package com.ecomarket.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InventarioRequest(
        @NotNull @Positive Integer cantidad
) {
}
