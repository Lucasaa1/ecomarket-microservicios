package com.ecomarket.catalog.service;

import com.ecomarket.catalog.dto.CategoriaRequest;
import com.ecomarket.catalog.dto.CategoriaResponse;
import com.ecomarket.catalog.exception.RecursoDuplicadoException;
import com.ecomarket.catalog.exception.RecursoNoEncontradoException;
import com.ecomarket.catalog.model.Categoria;
import com.ecomarket.catalog.repository.CategoriaRepository;
import com.ecomarket.catalog.repository.ProductoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Transactional
    public CategoriaResponse crearCategoria(CategoriaRequest request) {
        log.info("Iniciando creacion de categoria con nombre {}", request.nombre());
        if (categoriaRepository.findByNombreIgnoreCase(request.nombre().trim()).isPresent()) {
            log.warn("Creacion de categoria rechazada: nombre {} ya existe", request.nombre());
            throw new RecursoDuplicadoException("Ya existe una categoria con ese nombre");
        }

        Categoria categoria = Categoria.builder()
                .nombre(request.nombre().trim())
                .build();

        Categoria guardada = categoriaRepository.save(categoria);
        log.info("Categoria creada con id {} y nombre {}", guardada.getId(), guardada.getNombre());
        return mapearRespuesta(guardada);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        log.info("Listando categorias");
        List<CategoriaResponse> categorias = categoriaRepository.findAll()
                .stream()
                .map(this::mapearRespuesta)
                .toList();
        log.info("Listado de categorias completado. Total={}", categorias.size());
        return categorias;
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtenerCategoria(Integer id) {
        log.info("Buscando categoria con id {}", id);
        return mapearRespuesta(buscarCategoria(id));
    }

    @Transactional
    public CategoriaResponse actualizarCategoria(Integer id, CategoriaRequest request) {
        log.info("Iniciando actualizacion de categoria con id {}", id);
        Categoria categoria = buscarCategoria(id);
        categoriaRepository.findByNombreIgnoreCase(request.nombre().trim())
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    log.warn("Actualizacion de categoria rechazada: nombre {} ya existe", request.nombre());
                    throw new RecursoDuplicadoException("Ya existe una categoria con ese nombre");
                });

        categoria.setNombre(request.nombre().trim());
        Categoria actualizada = categoriaRepository.save(categoria);
        log.info("Categoria actualizada con id {}", id);
        return mapearRespuesta(actualizada);
    }

    @Transactional
    public void eliminarCategoria(Integer id) {
        log.info("Iniciando eliminacion de categoria con id {}", id);
        Categoria categoria = buscarCategoria(id);
        if (productoRepository.existsByCategoriaId(id)) {
            log.warn("Eliminacion de categoria {} rechazada: existen productos asociados", id);
            throw new IllegalArgumentException("No se puede eliminar una categoria con productos asociados");
        }

        categoriaRepository.delete(categoria);
        log.info("Categoria eliminada con id {}", id);
    }

    public Categoria buscarCategoria(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Categoria no encontrada con id {}", id);
                    return new RecursoNoEncontradoException("Categoria no encontrada");
                });
    }

    public CategoriaResponse mapearRespuesta(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre());
    }
}
