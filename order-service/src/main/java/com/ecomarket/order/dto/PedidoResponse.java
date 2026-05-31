package com.ecomarket.order.dto;

import java.time.LocalDateTime;

public record PedidoResponse(
        Integer id,
        Integer usuarioId,
        Integer productoId,
        String productoNombre,
        Integer cantidad,
        LocalDateTime fecha,
        String estado,
        Double monto // 🌟 NUEVO CAMPO: Agregado para viajar directo hacia el Frontend
) {
}