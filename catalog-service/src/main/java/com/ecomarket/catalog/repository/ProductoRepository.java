package com.ecomarket.catalog.repository;

import com.ecomarket.catalog.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByCategoriaId(Integer categoriaId);

    @Modifying
    @Query("""
            update Producto p
            set p.stock = p.stock - :cantidad
            where p.id = :id and p.stock >= :cantidad
            """)
    int descontarStock(@Param("id") Integer id, @Param("cantidad") Integer cantidad);

    @Modifying
    @Query("""
            update Producto p
            set p.stock = p.stock + :cantidad
            where p.id = :id
            """)
    int restaurarStock(@Param("id") Integer id, @Param("cantidad") Integer cantidad);
}
