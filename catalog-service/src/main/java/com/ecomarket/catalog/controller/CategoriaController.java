package com.ecomarket.catalog.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.catalog.dto.CategoriaRequest;
import com.ecomarket.catalog.dto.CategoriaResponse;
import com.ecomarket.catalog.service.CategoriaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaResponse> crearCategoria(@Valid @RequestBody CategoriaRequest request) {
        log.info("HTTP POST /api/categorias iniciado para nombre {}", request.nombre());
        CategoriaResponse response = categoriaService.crearCategoria(request);
        log.info("HTTP POST /api/categorias finalizado con id {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listarCategorias() {
        log.info("HTTP GET /api/categorias iniciado");
        List<CategoriaResponse> categorias = categoriaService.listarCategorias();
        log.info("HTTP GET /api/categorias finalizado. Total={}", categorias.size());
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> obtenerCategoria(@PathVariable Integer id) {
        log.info("HTTP GET /api/categorias/{} iniciado", id);
        CategoriaResponse categoria = categoriaService.obtenerCategoria(id);
        log.info("HTTP GET /api/categorias/{} finalizado", id);
        return ResponseEntity.ok(categoria);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> actualizarCategoria(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaRequest request
    ) {
        log.info("HTTP PUT /api/categorias/{} iniciado", id);
        CategoriaResponse categoria = categoriaService.actualizarCategoria(id, request);
        log.info("HTTP PUT /api/categorias/{} finalizado", id);
        return ResponseEntity.ok(categoria);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id) {
        log.info("HTTP DELETE /api/categorias/{} iniciado", id);
        categoriaService.eliminarCategoria(id);
        log.info("HTTP DELETE /api/categorias/{} finalizado", id);
        return ResponseEntity.noContent().build();
    }
}
