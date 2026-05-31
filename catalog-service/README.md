# EcoMarket SPA - catalog-service

Microservicio de categorias y productos para EcoMarket SPA.

## Ejecutar

```bash
mvn spring-boot:run
```

El servicio inicia en `http://localhost:8081` y usa SQLite en `catalogo.db`.

## Datos iniciales

- Categorias: `Frutas`, `Verduras`
- Productos:
  - `Manzana Organica`, precio `1500`, stock `100`
  - `Lechuga Hidroponica`, precio `1200`, stock `80`

## Endpoints

```http
POST /categorias
GET /categorias
GET /categorias/{id}
PUT /categorias/{id}
DELETE /categorias/{id}

POST /productos
GET /productos
GET /productos/{id}
PUT /productos/{id}
DELETE /productos/{id}
```

## Arbol

```text
catalog-service/
|-- pom.xml
|-- README.md
`-- src/
    `-- main/
        |-- java/com/ecomarket/catalog/
        |   |-- CatalogServiceApplication.java
        |   |-- config/
        |   |   |-- DataInitializer.java
        |   |   `-- SecurityConfig.java
        |   |-- controller/
        |   |   |-- CategoriaController.java
        |   |   `-- ProductoController.java
        |   |-- dto/
        |   |   |-- CategoriaRequest.java
        |   |   |-- CategoriaResponse.java
        |   |   |-- ProductoRequest.java
        |   |   `-- ProductoResponse.java
        |   |-- exception/
        |   |   |-- ErrorResponse.java
        |   |   |-- GlobalExceptionHandler.java
        |   |   `-- RecursoNoEncontradoException.java
        |   |-- model/
        |   |   |-- Categoria.java
        |   |   `-- Producto.java
        |   |-- repository/
        |   |   |-- CategoriaRepository.java
        |   |   `-- ProductoRepository.java
        |   `-- service/
        |       |-- CategoriaService.java
        |       `-- ProductoService.java
        `-- resources/
            |-- application.properties
            `-- db/migration/
                |-- V1__crear_tabla_categorias.sql
                `-- V2__crear_tabla_productos.sql
```
