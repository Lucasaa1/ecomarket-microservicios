package com.ecomarket.catalog.config;

import com.ecomarket.catalog.model.Categoria;
import com.ecomarket.catalog.model.Producto;
import com.ecomarket.catalog.repository.CategoriaRepository;
import com.ecomarket.catalog.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Override
    public void run(String... args) {
        Categoria frutas = crearCategoriaSiNoExiste("Frutas");
        Categoria verduras = crearCategoriaSiNoExiste("Verduras");

        crearProductoSiNoExiste("Manzana Organica", "Manzana producida localmente", 1500.0, 100, frutas.getId());
        crearProductoSiNoExiste("Lechuga Hidroponica", "Lechuga fresca cultivada en hidroponia", 1200.0, 80, verduras.getId());
    }

    private Categoria crearCategoriaSiNoExiste(String nombre) {
        return categoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> {
                    Categoria categoria = categoriaRepository.save(Categoria.builder().nombre(nombre).build());
                    log.info("Categoria inicial creada: {}", nombre);
                    return categoria;
                });
    }

    private void crearProductoSiNoExiste(
            String nombre,
            String descripcion,
            Double precio,
            Integer stock,
            Integer categoriaId
    ) {
        boolean existe = productoRepository.findAll()
                .stream()
                .anyMatch(producto -> producto.getNombre().equalsIgnoreCase(nombre));

        if (!existe) {
            productoRepository.save(Producto.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .precio(precio)
                    .stock(stock)
                    .categoriaId(categoriaId)
                    .build());
            log.info("Producto inicial creado: {}", nombre);
        }
    }
}
