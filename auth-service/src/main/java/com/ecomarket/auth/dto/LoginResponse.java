package com.ecomarket.auth.dto;

public record LoginResponse(
        String token,
        String tipo,
        Integer expiraEn,
        UsuarioResponse usuario
) {
}
