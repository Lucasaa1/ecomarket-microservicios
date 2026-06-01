package com.ecomarket.catalog.service;

import com.ecomarket.catalog.dto.ProductoRequest;
import com.ecomarket.catalog.dto.ProductoResponse;
import com.ecomarket.catalog.exception.RecursoNoEncontradoException;
import com.ecomarket.catalog.exception.StockInsuficienteException;
import com.ecomarket.catalog.model.Categoria;
import com.ecomarket.catalog.model.Producto;
import com.ecomarket.catalog.repository.ProductoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;

    @Transactional
    public ProductoResponse crearProducto(ProductoRequest request) {
        log.info("Iniciando creacion de producto con nombre {}", request.nombre());
        Categoria categoria = categoriaService.buscarCategoria(request.categoriaId());

        Producto producto = Producto.builder()
                .nombre(request.nombre().trim())
                .descripcion(request.descripcion().trim())
                .precio(request.precio())
                .stock(request.stock())
                .categoria(categoria)
                .build();

        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado con id {} y nombre {}", guardado.getId(), guardado.getNombre());
        return mapearRespuesta(guardado);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductos() {
        log.info("Listando productos del catalogo");
        List<ProductoResponse> productos = productoRepository.findAll()
                .stream()
                .map(this::mapearRespuesta)
                .toList();
        log.info("Listado de productos completado. Total={}", productos.size());
        return productos;
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerProducto(Integer id) {
        log.info("Buscando producto con id {}", id);
        return mapearRespuesta(buscarProducto(id));
    }

    @Transactional
    public ProductoResponse actualizarProducto(Integer id, ProductoRequest request) {
        log.info("Iniciando actualizacion de producto con id {}", id);
        Categoria categoria = categoriaService.buscarCategoria(request.categoriaId());
        Producto producto = buscarProducto(id);
        producto.setNombre(request.nombre().trim());
        producto.setDescripcion(request.descripcion().trim());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setCategoria(categoria);

        Producto actualizado = productoRepository.save(producto);
        log.info("Producto actualizado con id {}", id);
        return mapearRespuesta(actualizado);
    }

    @Transactional
    public void eliminarProducto(Integer id) {
        log.info("Iniciando eliminacion de producto con id {}", id);
        Producto producto = buscarProducto(id);
        productoRepository.delete(producto);
        log.info("Producto eliminado con id {}", id);
    }

    @Transactional
    public ProductoResponse descontarStock(Integer id, Integer cantidad) {
        log.info("Iniciando descuento de stock para producto {}. cantidad={}", id, cantidad);
        if (!productoRepository.existsById(id)) {
            log.warn("Descuento rechazado: producto {} no existe", id);
            throw new RecursoNoEncontradoException("Producto no encontrado");
        }

        int actualizados = productoRepository.descontarStock(id, cantidad);
        if (actualizados == 0) {
            log.warn("Descuento rechazado por stock insuficiente. producto={}, cantidad={}", id, cantidad);
            throw new StockInsuficienteException("Stock insuficiente para el producto solicitado");
        }

        Producto producto = buscarProducto(id);
        log.info("Stock descontado correctamente. producto={}, stockActual={}", id, producto.getStock());
        return mapearRespuesta(producto);
    }

    @Transactional
    public ProductoResponse restaurarStock(Integer id, Integer cantidad) {
        log.info("Iniciando restauracion de stock para producto {}. cantidad={}", id, cantidad);
        if (!productoRepository.existsById(id)) {
            log.warn("Restauracion rechazada: producto {} no existe", id);
            throw new RecursoNoEncontradoException("Producto no encontrado");
        }

        productoRepository.restaurarStock(id, cantidad);
        Producto producto = buscarProducto(id);
        log.info("Stock restaurado correctamente. producto={}, stockActual={}", id, producto.getStock());
        return mapearRespuesta(producto);
    }

    private Producto buscarProducto(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado con id {}", id);
                    return new RecursoNoEncontradoException("Producto no encontrado");
                });
    }

    private ProductoResponse mapearRespuesta(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getCategoria().getId()
        );
    }
}
