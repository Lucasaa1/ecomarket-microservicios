package com.ecomarket.catalog.dto;

public record ProductoResponse(
        Integer id,
        String nombre,
        String descripcion,
        Double precio,
        Integer stock,
        Integer categoriaId
) {
}
