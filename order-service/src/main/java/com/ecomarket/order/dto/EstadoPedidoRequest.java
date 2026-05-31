package com.ecomarket.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record EstadoPedidoRequest(
        @JsonProperty("estado") @NotBlank String estado
) {
}