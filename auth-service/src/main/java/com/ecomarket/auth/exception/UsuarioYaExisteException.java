package com.ecomarket.auth.exception;

public class UsuarioYaExisteException extends RuntimeException {

    public UsuarioYaExisteException(String mensaje) {
        super(mensaje);
    }
}
