package com.ecomarket.auth.dto;

public record UsuarioResponse(
        Integer id,
        String nombre,
        String correo,
        String rol
) {
}
