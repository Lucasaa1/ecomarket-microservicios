# EcoMarket SPA - order-service

Microservicio de pedidos para EcoMarket SPA.

## Ejecutar

Primero levanta `catalog-service` en el puerto `8081`.

```bash
mvn spring-boot:run
```

El servicio inicia en `http://localhost:8082` y usa SQLite en `pedidos.db`.

## Comunicacion

`order-service` usa OpenFeign mediante `CatalogClient` para consumir:

```http
GET http://localhost:8081/productos/{id}
```

Antes de crear un pedido valida que el producto exista y tenga stock suficiente.

## Seguridad

No implementa login ni genera JWT. `SecurityConfig` queda preparado con un comentario claro donde se integraria la validacion del JWT emitido por `auth-service`.

## Endpoints

```http
POST /pedidos
GET /pedidos
GET /pedidos/{id}
DELETE /pedidos/{id}
PATCH /pedidos/{id}/estado
```

## Arbol

```text
order-service/
|-- pom.xml
|-- README.md
`-- src/
    `-- main/
        |-- java/com/ecomarket/order/
        |   |-- OrderServiceApplication.java
        |   |-- client/
        |   |   `-- CatalogClient.java
        |   |-- config/
        |   |   |-- DataInitializer.java
        |   |   `-- SecurityConfig.java
        |   |-- controller/
        |   |   `-- PedidoController.java
        |   |-- dto/
        |   |   |-- EstadoPedidoRequest.java
        |   |   |-- PedidoRequest.java
        |   |   |-- PedidoResponse.java
        |   |   `-- ProductoDTO.java
        |   |-- exception/
        |   |   |-- ErrorResponse.java
        |   |   |-- GlobalExceptionHandler.java
        |   |   |-- RecursoNoEncontradoException.java
        |   |   `-- StockInsuficienteException.java
        |   |-- model/
        |   |   `-- Pedido.java
        |   |-- repository/
        |   |   `-- PedidoRepository.java
        |   `-- service/
        |       `-- PedidoService.java
        `-- resources/
            |-- application.properties
            `-- db/migration/
                `-- V1__crear_tabla_pedidos.sql
```
