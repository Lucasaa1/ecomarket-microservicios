package com.ecomarket.catalog.service;

import com.ecomarket.catalog.dto.CategoriaRequest;
import com.ecomarket.catalog.dto.CategoriaResponse;
import com.ecomarket.catalog.exception.RecursoNoEncontradoException;
import com.ecomarket.catalog.model.Categoria;
import com.ecomarket.catalog.repository.CategoriaRepository;
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

    @Transactional
    public CategoriaResponse crearCategoria(CategoriaRequest request) {
        Categoria categoria = Categoria.builder()
                .nombre(request.nombre().trim())
                .build();

        Categoria guardada = categoriaRepository.save(categoria);
        log.info("Categoria creada con id {} y nombre {}", guardada.getId(), guardada.getNombre());
        return mapearRespuesta(guardada);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::mapearRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtenerCategoria(Integer id) {
        return mapearRespuesta(buscarCategoria(id));
    }

    @Transactional
    public CategoriaResponse actualizarCategoria(Integer id, CategoriaRequest request) {
        Categoria categoria = buscarCategoria(id);
        categoria.setNombre(request.nombre().trim());
        Categoria actualizada = categoriaRepository.save(categoria);
        log.info("Categoria actualizada con id {}", id);
        return mapearRespuesta(actualizada);
    }

    @Transactional
    public void eliminarCategoria(Integer id) {
        Categoria categoria = buscarCategoria(id);
        categoriaRepository.delete(categoria);
        log.info("Categoria eliminada con id {}", id);
    }

    public Categoria buscarCategoria(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada"));
    }

    public CategoriaResponse mapearRespuesta(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre());
    }
}
