package com.ecomarket.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.order.dto.EstadoPedidoRequest;
import com.ecomarket.order.dto.PedidoRequest;
import com.ecomarket.order.dto.PedidoResponse;
import com.ecomarket.order.service.PedidoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponse> crearPedido(@Valid @RequestBody PedidoRequest request) {
        log.info("HTTP POST /api/pedidos iniciado para usuario {} y producto {}", request.usuarioId(), request.productoId());
        PedidoResponse response = pedidoService.crearPedido(request);
        log.info("HTTP POST /api/pedidos finalizado con id {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarPedidos() {
        log.info("HTTP GET /api/pedidos iniciado");
        List<PedidoResponse> pedidos = pedidoService.listarPedidos();
        log.info("HTTP GET /api/pedidos finalizado. Total={}", pedidos.size());
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtenerPedido(@PathVariable Integer id) {
        log.info("HTTP GET /api/pedidos/{} iniciado", id);
        PedidoResponse pedido = pedidoService.obtenerPedido(id);
        log.info("HTTP GET /api/pedidos/{} finalizado", id);
        return ResponseEntity.ok(pedido);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPedido(@PathVariable Integer id) {
        log.info("HTTP DELETE /api/pedidos/{} iniciado", id);
        pedidoService.eliminarPedido(id);
        log.info("HTTP DELETE /api/pedidos/{} finalizado", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/estado")
    public ResponseEntity<PedidoResponse> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody EstadoPedidoRequest request
    ) {
        log.info("HTTP POST /api/pedidos/{}/estado iniciado con estado {}", id, request.estado());
        PedidoResponse pedido = pedidoService.cambiarEstado(id, request.estado());
        log.info("HTTP POST /api/pedidos/{}/estado finalizado", id);
        return ResponseEntity.ok(pedido);
    }
}
