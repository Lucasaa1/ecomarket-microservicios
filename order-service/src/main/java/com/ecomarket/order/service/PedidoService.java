package com.ecomarket.order.service;

import com.ecomarket.order.client.CatalogClient;
import com.ecomarket.order.dto.InventarioRequest;
import com.ecomarket.order.dto.PedidoRequest;
import com.ecomarket.order.dto.PedidoResponse;
import com.ecomarket.order.dto.ProductoDTO;
import com.ecomarket.order.exception.RecursoNoEncontradoException;
import com.ecomarket.order.exception.ServicioRemotoException;
import com.ecomarket.order.exception.StockInsuficienteException;
import com.ecomarket.order.model.Pedido;
import com.ecomarket.order.repository.PedidoRepository;
import feign.FeignException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final String ESTADO_CANCELADO = "CANCELADO";

    private final PedidoRepository pedidoRepository;
    private final CatalogClient catalogClient;

    @Transactional
    public PedidoResponse crearPedido(PedidoRequest request) {
        log.info("Iniciando creacion de pedido para usuario {} y producto {}", request.usuarioId(), request.productoId());
        ProductoDTO producto = obtenerProductoRemoto(request.productoId());
        validarStock(producto, request.cantidad());

        ProductoDTO productoActualizado = descontarStockRemoto(request.productoId(), request.cantidad());
        try {
            Double montoTotal = request.cantidad() * producto.precio();
            log.info("Monto calculado para pedido. cantidad={}, precio={}, total={}",
                    request.cantidad(), producto.precio(), montoTotal);

            Pedido pedido = Pedido.builder()
                    .usuarioId(request.usuarioId())
                    .productoId(request.productoId())
                    .cantidad(request.cantidad())
                    .fecha(LocalDateTime.now())
                    .estado("PENDIENTE")
                    .monto(montoTotal)
                    .build();

            Pedido guardado = pedidoRepository.save(pedido);
            log.info("Pedido creado con id {}. Stock restante producto {}={}",
                    guardado.getId(), productoActualizado.id(), productoActualizado.stock());
            return mapearRespuesta(guardado, producto);
        } catch (RuntimeException ex) {
            log.error("Fallo al guardar pedido despues de descontar stock. Se intentara compensar inventario", ex);
            restaurarStockRemoto(request.productoId(), request.cantidad());
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidos() {
        log.info("Listando pedidos");
        List<PedidoResponse> pedidos = pedidoRepository.findAll()
                .stream()
                .map(this::mapearRespuestaConProducto)
                .toList();
        log.info("Listado de pedidos completado. Total={}", pedidos.size());
        return pedidos;
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedido(Integer id) {
        log.info("Buscando pedido con id {}", id);
        Pedido pedido = buscarPedido(id);
        return mapearRespuestaConProducto(pedido);
    }

    @Transactional
    public void eliminarPedido(Integer id) {
        log.info("Iniciando eliminacion de pedido con id {}", id);
        Pedido pedido = buscarPedido(id);

        if (!ESTADO_CANCELADO.equals(pedido.getEstado())) {
            restaurarStockRemoto(pedido.getProductoId(), pedido.getCantidad());
            log.info("Stock restaurado antes de eliminar pedido {}", id);
        } else {
            log.info("Pedido {} ya estaba cancelado; no se restaura stock nuevamente", id);
        }

        pedidoRepository.delete(pedido);
        log.info("Pedido eliminado con id {}", id);
    }

    @Transactional
    public PedidoResponse cambiarEstado(Integer id, String estado) {
        log.info("Iniciando cambio de estado para pedido {} a {}", id, estado);
        Pedido pedido = buscarPedido(id);
        String estadoNormalizado = normalizarEstado(estado);

        if (ESTADO_CANCELADO.equals(pedido.getEstado())) {
            log.warn("Cambio de estado rechazado: pedido {} ya esta cancelado", id);
            throw new IllegalArgumentException("El pedido ya esta cancelado y no puede cambiar de estado");
        }

        if (ESTADO_CANCELADO.equals(estadoNormalizado)) {
            restaurarStockRemoto(pedido.getProductoId(), pedido.getCantidad());
            log.info("Stock restaurado por cancelacion de pedido {}", id);
        }

        pedido.setEstado(estadoNormalizado);
        Pedido actualizado = pedidoRepository.save(pedido);
        log.info("Estado de pedido {} cambiado a {}", id, estadoNormalizado);
        return mapearRespuestaConProducto(actualizado);
    }

    private Pedido buscarPedido(Integer id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Pedido no encontrado con id {}", id);
                    return new RecursoNoEncontradoException("Pedido no encontrado");
                });
    }

    private ProductoDTO obtenerProductoRemoto(Integer productoId) {
        try {
            log.info("Llamando catalog-service para obtener producto {}", productoId);
            ProductoDTO producto = catalogClient.obtenerProducto(productoId);
            if (producto == null || producto.id() == null) {
                log.warn("catalog-service respondio producto vacio para id {}", productoId);
                throw new RecursoNoEncontradoException("Producto no encontrado");
            }
            log.info("Producto {} obtenido desde catalog-service con stock {}", producto.id(), producto.stock());
            return producto;
        } catch (FeignException.NotFound ex) {
            log.warn("catalog-service no encontro producto {}", productoId);
            throw new RecursoNoEncontradoException("Producto no encontrado");
        } catch (FeignException.BadRequest ex) {
            log.warn("catalog-service rechazo solicitud para producto {}: {}", productoId, ex.getMessage());
            throw new IllegalArgumentException("Solicitud invalida al consultar catalogo");
        } catch (FeignException ex) {
            log.error("Error llamando catalog-service para producto {}. status={}", productoId, ex.status(), ex);
            throw new ServicioRemotoException("No fue posible consultar el catalogo de productos");
        }
    }

    private ProductoDTO descontarStockRemoto(Integer productoId, Integer cantidad) {
        try {
            log.info("Solicitando descuento de stock. producto={}, cantidad={}", productoId, cantidad);
            ProductoDTO producto = catalogClient.descontarStock(productoId, new InventarioRequest(cantidad));
            log.info("Descuento confirmado por catalog-service. producto={}, stockActual={}", producto.id(), producto.stock());
            return producto;
        } catch (FeignException.NotFound ex) {
            log.warn("No se pudo descontar stock: producto {} no existe", productoId);
            throw new RecursoNoEncontradoException("Producto no encontrado");
        } catch (FeignException.Conflict ex) {
            log.warn("No se pudo descontar stock por stock insuficiente. producto={}, cantidad={}", productoId, cantidad);
            throw new StockInsuficienteException("Stock insuficiente para el producto solicitado");
        } catch (FeignException.BadRequest ex) {
            log.warn("No se pudo descontar stock por solicitud invalida o stock insuficiente. producto={}, cantidad={}",
                    productoId, cantidad);
            throw new StockInsuficienteException("Stock insuficiente para el producto solicitado");
        } catch (FeignException.Forbidden ex) {
            log.warn("catalog-service rechazo descuento de stock por permisos. producto={}", productoId);
            throw new ServicioRemotoException("No autorizado para modificar inventario");
        } catch (FeignException.Unauthorized ex) {
            log.warn("catalog-service rechazo descuento de stock por falta de autenticacion. producto={}", productoId);
            throw new ServicioRemotoException("No autenticado para modificar inventario");
        } catch (FeignException ex) {
            log.error("Error inesperado descontando stock. producto={}, status={}", productoId, ex.status(), ex);
            throw new ServicioRemotoException("No fue posible descontar stock del producto");
        }
    }

    private ProductoDTO restaurarStockRemoto(Integer productoId, Integer cantidad) {
        try {
            log.info("Solicitando restauracion de stock. producto={}, cantidad={}", productoId, cantidad);
            ProductoDTO producto = catalogClient.restaurarStock(productoId, new InventarioRequest(cantidad));
            log.info("Restauracion confirmada por catalog-service. producto={}, stockActual={}", producto.id(), producto.stock());
            return producto;
        } catch (FeignException.NotFound ex) {
            log.warn("No se pudo restaurar stock: producto {} no existe", productoId);
            throw new RecursoNoEncontradoException("Producto no encontrado");
        } catch (FeignException.BadRequest ex) {
            log.warn("catalog-service rechazo restauracion de stock. producto={}, cantidad={}", productoId, cantidad);
            throw new IllegalArgumentException("Solicitud invalida al restaurar inventario");
        } catch (FeignException.Forbidden ex) {
            log.warn("catalog-service rechazo restauracion de stock por permisos. producto={}", productoId);
            throw new ServicioRemotoException("No autorizado para modificar inventario");
        } catch (FeignException.Unauthorized ex) {
            log.warn("catalog-service rechazo restauracion de stock por falta de autenticacion. producto={}", productoId);
            throw new ServicioRemotoException("No autenticado para modificar inventario");
        } catch (FeignException ex) {
            log.error("Error inesperado restaurando stock. producto={}, status={}", productoId, ex.status(), ex);
            throw new ServicioRemotoException("No fue posible restaurar stock del producto");
        }
    }

    private void validarStock(ProductoDTO producto, Integer cantidad) {
        if (producto.stock() == null || producto.stock() < cantidad) {
            log.warn("Stock insuficiente para producto {}. solicitado={}, disponible={}",
                    producto.id(), cantidad, producto.stock());
            throw new StockInsuficienteException("Stock insuficiente para el producto solicitado");
        }
        log.info("Stock validado para producto {}. solicitado={}, disponible={}",
                producto.id(), cantidad, producto.stock());
    }

    private String normalizarEstado(String estado) {
        String estadoNormalizado = estado.trim().toUpperCase(Locale.ROOT);
        if (!estadoNormalizado.equals("PENDIENTE")
                && !estadoNormalizado.equals("PAGADO")
                && !estadoNormalizado.equals("ENTREGADO")
                && !estadoNormalizado.equals(ESTADO_CANCELADO)) {
            log.warn("Estado de pedido invalido recibido: {}", estado);
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
                pedido.getMonto()
        );
    }
}
