package com.ecomarket.catalog.repository;

import com.ecomarket.catalog.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
}
