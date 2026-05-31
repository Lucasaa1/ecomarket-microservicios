# EcoMarket SPA - catalog-service y order-service

Entrega generada sin modificar `auth-service`.

## Servicios

- `catalog-service`: puerto `8081`, base SQLite `catalogo.db`
- `order-service`: puerto `8082`, base SQLite `pedidos.db`

## Orden de ejecucion

1. Iniciar `catalog-service`.
2. Iniciar `order-service`.
3. Probar requests con `EcoMarket-catalog-order.postman_collection.json`.

## Compilar

Desde cada carpeta:

```bash
mvn clean package
```

## Ejecutar

Desde cada carpeta:

```bash
mvn spring-boot:run
```

## Comunicacion entre microservicios

`order-service` se comunica con `catalog-service` mediante OpenFeign:

```http
GET http://localhost:8081/productos/{id}
```

Antes de crear pedidos valida existencia del producto y stock suficiente.
