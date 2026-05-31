package com.ecomarket.order.dto;

public record ProductoDTO(
        Integer id,
        String nombre,
        String descripcion,
        Double precio,
        Integer stock,
        Integer categoriaId
) {
}
