package com.ecomarket.auth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.auth.dto.UsuarioResponse;
import com.ecomarket.auth.service.UsuarioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        log.info("HTTP GET /api/usuarios iniciado");
        List<UsuarioResponse> usuarios = usuarioService.listarUsuarios();
        log.info("HTTP GET /api/usuarios finalizado. Total={}", usuarios.size());
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerUsuario(@PathVariable Integer id) {
        log.info("HTTP GET /api/usuarios/{} iniciado", id);
        UsuarioResponse usuario = usuarioService.obtenerUsuarioPorId(id);
        log.info("HTTP GET /api/usuarios/{} finalizado", id);
        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Integer id) {
        log.info("HTTP DELETE /api/usuarios/{} iniciado", id);
        usuarioService.eliminarUsuario(id);
        log.info("HTTP DELETE /api/usuarios/{} finalizado", id);
        return ResponseEntity.noContent().build();
    }
}
