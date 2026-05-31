package com.ecomarket.catalog.repository;

import com.ecomarket.catalog.model.Categoria;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}
