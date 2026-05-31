package com.ecomarket.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PedidoRequest(
        @NotNull Integer usuarioId,
        @NotNull Integer productoId,
        @Min(1) Integer cantidad
) {
}
