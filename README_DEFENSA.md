# README_DEFENSA - EcoMarket Microservicios Spring Boot

## 1. Arquitectura general

El proyecto esta compuesto por tres microservicios independientes:

- `auth-service` (`8080`): registra usuarios, autentica credenciales y emite JWT.
- `catalog-service` (`8081`): administra categorias y productos.
- `order-service` (`8082`): administra pedidos y consulta productos mediante Feign.

Cada servicio tiene su propio `pom.xml`, su propio contexto Spring Boot y su propia base SQLite:

- `auth-service`: `usuarios.db`
- `catalog-service`: `catalogo.db`
- `order-service`: `pedidos.db`

No hay acceso directo entre bases de datos. `order-service` guarda solo `producto_id` y consulta el detalle del producto a traves de `CatalogClient`, evitando acoplamiento por tablas compartidas.

## 2. Explicacion por microservicio

### auth-service

Responsabilidad: identidad y autenticacion.

- Controller: `AuthController`, `UsuarioController`
- Service: `AuthService`, `UsuarioService`
- Repository: `UsuarioRepository`
- Entity: `Usuario`
- Migracion: `V1__crear_tabla_usuarios.sql`

Genera JWT con correo y rol. Las contrasenas se almacenan con BCrypt.

### catalog-service

Responsabilidad: catalogo de productos y categorias.

- Controller: `ProductoController`, `CategoriaController`
- Service: `ProductoService`, `CategoriaService`
- Repository: `ProductoRepository`, `CategoriaRepository`
- Entity: `Producto`, `Categoria`
- Migraciones: `V1__crear_tabla_categorias.sql`, `V2__crear_tabla_productos.sql`

`Producto` mantiene una relacion JPA `ManyToOne` con `Categoria` dentro de la misma base del catalogo. Esa relacion no cruza microservicios.

### order-service

Responsabilidad: pedidos.

- Controller: `PedidoController`
- Service: `PedidoService`
- Repository: `PedidoRepository`
- Entity: `Pedido`
- Cliente remoto: `CatalogClient`
- Migracion: `V1__crear_tabla_pedidos.sql`

`Pedido` guarda identificadores externos (`usuarioId`, `productoId`) y monto calculado. No usa relaciones JPA hacia usuarios ni productos porque pertenecen a otros microservicios.

## 3. Flujo Controller -> Service -> Repository

1. El Controller recibe HTTP, valida DTO con `@Valid` y responde con `ResponseEntity`.
2. El Service contiene la regla de negocio, transacciones, normalizacion, validaciones cruzadas y logs.
3. El Repository usa Spring Data JPA para persistencia.
4. Las excepciones viajan al `GlobalExceptionHandler`, que devuelve una respuesta consistente con `timestamp`, `status` y `mensaje`.

Ejemplo en pedidos:

1. `PedidoController.crearPedido`
2. `PedidoService.crearPedido`
3. `CatalogClient.obtenerProducto`
4. `PedidoRepository.save`
5. `201 CREATED`

## 4. Flujo JWT

1. `auth-service` recibe `/api/auth/login`.
2. `AuthService` valida credenciales con BCrypt.
3. `JwtService` genera token con `correo` y `rol`.
4. `catalog-service` y `order-service` validan el token con `JwtFilter`.
5. Los endpoints protegidos usan roles `ADMIN` y `CLIENTE`.
6. Todos los servicios usan `SessionCreationPolicy.STATELESS`.

Reglas importantes:

- Registro y login son publicos.
- Lectura del catalogo es publica.
- Escritura del catalogo requiere `ADMIN`.
- Crear/listar/ver pedidos requiere `ADMIN` o `CLIENTE`.
- Cambiar estado o eliminar pedidos requiere `ADMIN`.

## 5. Flujo Feign

`order-service` se comunica con `catalog-service` usando:

```http
GET /api/productos/{id}
```

Clase principal:

```text
order-service/src/main/java/com/ecomarket/order/client/CatalogClient.java
```

`CatalogClientConfig` agrega logging de solicitudes Feign. `PedidoService` registra:

- inicio de llamada remota,
- respuesta exitosa,
- producto no encontrado,
- error inesperado de integracion.

## 6. Flyway

Los tres servicios usan Flyway y `ddl-auto=validate`, no `update`.

Esto significa:

- Flyway crea y versiona el esquema.
- Hibernate solo valida que las entidades coincidan con la base.
- Para agregar campos se crea una nueva migracion `Vx__descripcion.sql`.

## 7. Exception Handler

Cada microservicio tiene `@RestControllerAdvice`.

Las respuestas de error incluyen:

- `timestamp`
- `status`
- `mensaje`

Codigos usados:

- `400 BAD REQUEST`: validaciones y parametros invalidos.
- `401 UNAUTHORIZED`: credenciales invalidas o ausencia de autenticacion.
- `403 FORBIDDEN`: usuario autenticado sin rol suficiente.
- `404 NOT FOUND`: recurso inexistente.
- `409 CONFLICT`: recurso duplicado.
- `502 BAD GATEWAY`: falla controlada al consultar otro microservicio.

## 8. Logs

Se agregaron logs SLF4J en:

- Controllers: inicio y fin de cada endpoint.
- Services: inicio de operacion, decisiones de negocio, exito y rechazos controlados.
- Feign: salida hacia `catalog-service` y resultado de la llamada.
- Exception handlers: errores controlados con `warn` y errores inesperados con `error`.

Uso esperado:

- `log.info`: flujo normal y operaciones exitosas.
- `log.warn`: errores controlados como validaciones, 404, 409 o stock insuficiente.
- `log.error`: errores inesperados o fallas remotas.

Durante la defensa, ejecutar una creacion de pedido permite explicar el flujo completo desde HTTP hasta Feign y persistencia.

## 9. Posibles preguntas de defensa y respuestas

**Por que `Pedido` no tiene relacion JPA con `Producto`?**
Porque `Producto` vive en otra base y otro microservicio. Una relacion JPA cruzaria limites de servicio y acoplaria las bases. Se guarda `productoId` y se consulta por Feign.

**Por que `Producto` si tiene `ManyToOne` con `Categoria`?**
Porque ambos pertenecen al bounded context de catalogo y viven en la misma base.

**Por que se usa `ddl-auto=validate`?**
Porque Flyway es la fuente de verdad del esquema. `update` puede modificar tablas sin control y no es defendible academicamente.

**Como se maneja un producto inexistente al crear pedido?**
`CatalogClient` recibe 404 desde catalogo, `PedidoService` lo transforma en `RecursoNoEncontradoException` y el handler responde 404.

**Como se protege un endpoint administrativo?**
El JWT contiene el rol. `JwtFilter` crea el contexto de seguridad y `SecurityConfig` aplica `hasRole("ADMIN")`.

**Que evidencia muestran los logs?**
Muestran entrada HTTP, validacion de negocio, llamada Feign, persistencia y respuesta o error controlado.

## 10. Live Coding probable

### Agregar validacion

Archivo: `*/src/main/java/.../dto/*Request.java`

Ejemplo: agregar `@Size(max = 120)` a un campo de entrada y verificar que el Controller use `@Valid`.

### Agregar campo en entidad

Archivos:

- Entity: `model/*.java`
- DTO request/response: `dto/*.java`
- Service mapper: `service/*.java`
- Migracion Flyway: `src/main/resources/db/migration/Vx__agregar_campo.sql`

### Crear migracion Flyway

Ruta: `src/main/resources/db/migration/`

Nombre esperado:

```text
V3__agregar_campo_ejemplo.sql
```

### Agregar endpoint

Archivos:

- Controller: agregar metodo HTTP y `ResponseEntity`.
- Service: agregar regla de negocio.
- Repository: agregar query si hace falta.
- Exception handler: reutilizar excepciones existentes o crear una nueva.

### Agregar log

Ubicaciones:

- Controller: al inicio y fin de la peticion.
- Service: antes y despues de la regla de negocio.
- Exception handler: en error controlado o inesperado.

### Modificar llamada Feign

Archivos:

- `order-service/src/main/java/com/ecomarket/order/client/CatalogClient.java`
- `order-service/src/main/java/com/ecomarket/order/service/PedidoService.java`
- `order-service/src/main/java/com/ecomarket/order/client/CatalogClientConfig.java`

## 11. Cumplimiento estimado

El proyecto queda preparado para una defensa tecnica con foco en arquitectura desacoplada, CSR, JPA, Flyway, validaciones, respuestas HTTP, seguridad JWT, Feign, excepciones globales y observabilidad.
