package com.ecomarket.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PedidoRequest(
        @NotNull @Positive Integer usuarioId,
        @NotNull @Positive Integer productoId,
        @NotNull @Min(1) Integer cantidad
) {
}
