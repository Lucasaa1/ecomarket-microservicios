package com.ecomarket.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ecomarket.order.dto.ProductoDTO;

@FeignClient(name = "catalog-service", url = "${catalog.service.url}")
public interface CatalogClient {

    @GetMapping("/api/productos/{id}")
    ProductoDTO obtenerProducto(@PathVariable("id") Integer id);
}
