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

import com.ecomarket.catalog.dto.InventarioRequest;
import com.ecomarket.catalog.dto.ProductoRequest;
import com.ecomarket.catalog.dto.ProductoResponse;
import com.ecomarket.catalog.service.ProductoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody ProductoRequest request) {
        log.info("HTTP POST /api/productos iniciado para nombre {}", request.nombre());
        ProductoResponse response = productoService.crearProducto(request);
        log.info("HTTP POST /api/productos finalizado con id {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listarProductos() {
        log.info("HTTP GET /api/productos iniciado");
        List<ProductoResponse> productos = productoService.listarProductos();
        log.info("HTTP GET /api/productos finalizado. Total={}", productos.size());
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerProducto(@PathVariable Integer id) {
        log.info("HTTP GET /api/productos/{} iniciado", id);
        ProductoResponse producto = productoService.obtenerProducto(id);
        log.info("HTTP GET /api/productos/{} finalizado", id);
        return ResponseEntity.ok(producto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoRequest request
    ) {
        log.info("HTTP PUT /api/productos/{} iniciado", id);
        ProductoResponse producto = productoService.actualizarProducto(id, request);
        log.info("HTTP PUT /api/productos/{} finalizado", id);
        return ResponseEntity.ok(producto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer id) {
        log.info("HTTP DELETE /api/productos/{} iniciado", id);
        productoService.eliminarProducto(id);
        log.info("HTTP DELETE /api/productos/{} finalizado", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/stock/descontar")
    public ResponseEntity<ProductoResponse> descontarStock(
            @PathVariable Integer id,
            @Valid @RequestBody InventarioRequest request
    ) {
        log.info("HTTP POST /api/productos/{}/stock/descontar iniciado. cantidad={}", id, request.cantidad());
        ProductoResponse producto = productoService.descontarStock(id, request.cantidad());
        log.info("HTTP POST /api/productos/{}/stock/descontar finalizado. stock={}", id, producto.stock());
        return ResponseEntity.ok(producto);
    }

    @PostMapping("/{id}/stock/restaurar")
    public ResponseEntity<ProductoResponse> restaurarStock(
            @PathVariable Integer id,
            @Valid @RequestBody InventarioRequest request
    ) {
        log.info("HTTP POST /api/productos/{}/stock/restaurar iniciado. cantidad={}", id, request.cantidad());
        ProductoResponse producto = productoService.restaurarStock(id, request.cantidad());
        log.info("HTTP POST /api/productos/{}/stock/restaurar finalizado. stock={}", id, producto.stock());
        return ResponseEntity.ok(producto);
    }
}
