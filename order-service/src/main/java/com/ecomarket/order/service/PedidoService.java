package com.ecomarket.order.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecomarket.order.client.CatalogClient;
import com.ecomarket.order.dto.PedidoRequest;
import com.ecomarket.order.dto.PedidoResponse;
import com.ecomarket.order.dto.ProductoDTO;
import com.ecomarket.order.exception.RecursoNoEncontradoException;
import com.ecomarket.order.exception.StockInsuficienteException;
import com.ecomarket.order.model.Pedido;
import com.ecomarket.order.repository.PedidoRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CatalogClient catalogClient;

    @Transactional
    public PedidoResponse crearPedido(PedidoRequest request) {
        ProductoDTO producto = obtenerProductoRemoto(request.productoId());
        validarStock(producto, request.cantidad());

        // 🌟 CALCULAMOS EL MONTO TOTAL: Cantidad x Precio obtenido del catálogo
        Double montoTotal = request.cantidad() * producto.precio();

        // 🌟 LOG DE CONTROL: Esto nos dirá en la consola negra del IDE qué dato está llegando mal
        log.info("[DEBUG MONTO] -> Cantidad en request: {}, Precio en catalogo: {}, Total calculado: {}", 
                request.cantidad(), producto.precio(), montoTotal);

        Pedido pedido = Pedido.builder()
                .usuarioId(request.usuarioId())
                .productoId(request.productoId())
                .cantidad(request.cantidad())
                .fecha(LocalDateTime.now())
                .estado("PENDIENTE")
                .monto(montoTotal) // 🌟 GUARDAMOS EL MONTO REAL EN LA DB
                .build();

        Pedido guardado = pedidoRepository.save(pedido);
        log.info("Pedido creado con id {} para usuario {}", guardado.getId(), guardado.getUsuarioId());
        return mapearRespuesta(guardado, producto);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidos() {
        return pedidoRepository.findAll()
                .stream()
                .map(this::mapearRespuestaConProducto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedido(Integer id) {
        Pedido pedido = buscarPedido(id);
        return mapearRespuestaConProducto(pedido);
    }

    @Transactional
    public void eliminarPedido(Integer id) {
        Pedido pedido = buscarPedido(id);
        pedidoRepository.delete(pedido);
        log.info("Pedido eliminado con id {}", id);
    }

    @Transactional
    public PedidoResponse cambiarEstado(Integer id, String estado) {
        Pedido pedido = buscarPedido(id);
        String estadoNormalizado = normalizarEstado(estado);
        pedido.setEstado(estadoNormalizado);
        Pedido actualizado = pedidoRepository.save(pedido);
        log.info("Estado de pedido {} cambiado a {}", id, estadoNormalizado);
        return mapearRespuestaConProducto(actualizado);
    }

    private Pedido buscarPedido(Integer id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));
    }

    private ProductoDTO obtenerProductoRemoto(Integer productoId) {
        try {
            ProductoDTO producto = catalogClient.obtenerProducto(productoId);
            if (producto == null || producto.id() == null) {
                throw new RecursoNoEncontradoException("Producto no encontrado");
            }
            return producto;
        } catch (FeignException.NotFound ex) {
            throw new RecursoNoEncontradoException("Producto no encontrado");
        }
    }

    private void validarStock(ProductoDTO producto, Integer cantidad) {
        if (producto.stock() == null || producto.stock() <= 0 || producto.stock() < cantidad) {
            throw new StockInsuficienteException("Stock insuficiente para el producto solicitado");
        }
    }

    private String normalizarEstado(String estado) {
        String estadoNormalizado = estado.trim().toUpperCase(Locale.ROOT);
        // 👇 AGREGAMOS LA NUEVA CONDICIÓN AQUÍ
        if (!estadoNormalizado.equals("PENDIENTE")
                && !estadoNormalizado.equals("PAGADO")
                && !estadoNormalizado.equals("ENTREGADO")
                && !estadoNormalizado.equals("CANCELADO")) {
            throw new IllegalArgumentException("Estado permitido: PENDIENTE, PAGADO, ENTREGADO o CANCELADO");
        }
        return estadoNormalizado;
    }

    private PedidoResponse mapearRespuestaConProducto(Pedido pedido) {
        ProductoDTO producto = obtenerProductoRemoto(pedido.getProductoId());
        return mapearRespuesta(pedido, producto);
    }

    private PedidoResponse mapearRespuesta(Pedido pedido, ProductoDTO producto) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getUsuarioId(),
                pedido.getProductoId(),
                producto.nombre(),
                pedido.getCantidad(),
                pedido.getFecha(),
                pedido.getEstado(),
                pedido.getMonto() // 🌟 ENVIAMOS EL MONTO REAL DE VUELTA AL FRONTEND
        );
    }
}