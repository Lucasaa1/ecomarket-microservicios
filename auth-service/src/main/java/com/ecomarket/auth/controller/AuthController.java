package com.ecomarket.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.auth.dto.LoginRequest;
import com.ecomarket.auth.dto.LoginResponse;
import com.ecomarket.auth.dto.RegistroRequest;
import com.ecomarket.auth.dto.UsuarioResponse;
import com.ecomarket.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        log.info("HTTP POST /api/auth/register iniciado para correo {}", request.correo());
        UsuarioResponse response = authService.registrar(request);
        log.info("HTTP POST /api/auth/register finalizado con usuario id {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("HTTP POST /api/auth/login iniciado para correo {}", request.correo());
        LoginResponse response = authService.login(request);
        log.info("HTTP POST /api/auth/login finalizado para usuario id {}", response.usuario().id());
        return ResponseEntity.ok(response);
    }
}
