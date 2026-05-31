package com.ecomarket.catalog.service;

import com.ecomarket.catalog.dto.ProductoRequest;
import com.ecomarket.catalog.dto.ProductoResponse;
import com.ecomarket.catalog.exception.RecursoNoEncontradoException;
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
        categoriaService.buscarCategoria(request.categoriaId());

        Producto producto = Producto.builder()
                .nombre(request.nombre().trim())
                .descripcion(request.descripcion().trim())
                .precio(request.precio())
                .stock(request.stock())
                .categoriaId(request.categoriaId())
                .build();

        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado con id {} y nombre {}", guardado.getId(), guardado.getNombre());
        return mapearRespuesta(guardado);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductos() {
        return productoRepository.findAll()
                .stream()
                .map(this::mapearRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerProducto(Integer id) {
        return mapearRespuesta(buscarProducto(id));
    }

    @Transactional
    public ProductoResponse actualizarProducto(Integer id, ProductoRequest request) {
        categoriaService.buscarCategoria(request.categoriaId());
        Producto producto = buscarProducto(id);
        producto.setNombre(request.nombre().trim());
        producto.setDescripcion(request.descripcion().trim());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setCategoriaId(request.categoriaId());

        Producto actualizado = productoRepository.save(producto);
        log.info("Producto actualizado con id {}", id);
        return mapearRespuesta(actualizado);
    }

    @Transactional
    public void eliminarProducto(Integer id) {
        Producto producto = buscarProducto(id);
        productoRepository.delete(producto);
        log.info("Producto eliminado con id {}", id);
    }

    private Producto buscarProducto(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
    }

    private ProductoResponse mapearRespuesta(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getCategoriaId()
        );
    }
}
