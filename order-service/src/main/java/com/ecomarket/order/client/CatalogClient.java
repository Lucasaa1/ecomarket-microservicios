package com.ecomarket.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ecomarket.order.dto.InventarioRequest;
import com.ecomarket.order.dto.ProductoDTO;

@FeignClient(name = "catalog-service", url = "${catalog.service.url}", configuration = CatalogClientConfig.class)
public interface CatalogClient {

    @GetMapping("/api/productos/{id}")
    ProductoDTO obtenerProducto(@PathVariable("id") Integer id);

    @PostMapping("/api/productos/{id}/stock/descontar")
    ProductoDTO descontarStock(@PathVariable("id") Integer id, @RequestBody InventarioRequest request);

    @PostMapping("/api/productos/{id}/stock/restaurar")
    ProductoDTO restaurarStock(@PathVariable("id") Integer id, @RequestBody InventarioRequest request);
}
