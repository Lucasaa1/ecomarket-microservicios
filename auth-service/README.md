# EcoMarket SPA - auth-service

Microservicio de autenticacion para EcoMarket SPA.

## Requisitos

- Java 21
- Maven

## Ejecutar

```bash
mvn spring-boot:run
```

El servicio inicia en `http://localhost:8080` y crea la base SQLite `usuarios.db`.

## Administrador inicial

- Correo: `admin@ecomarket.cl`
- Password: `admin123`
- Rol: `ADMIN`

La clave se almacena con BCrypt al iniciar la aplicacion.

## Endpoints

### Registrar usuario

```http
POST /auth/register
Content-Type: application/json

{
  "nombre": "Cliente Eco",
  "correo": "cliente@ecomarket.cl",
  "password": "cliente123",
  "rol": "CLIENTE"
}
```

### Login

```http
POST /auth/login
Content-Type: application/json

{
  "correo": "admin@ecomarket.cl",
  "password": "admin123"
}
```

### Listar usuarios

```http
GET /usuarios
Authorization: Bearer <token-admin>
```

### Obtener usuario por id

```http
GET /usuarios/1
Authorization: Bearer <token-admin>
```

### Eliminar usuario

```http
DELETE /usuarios/1
Authorization: Bearer <token-admin>
```

## Arbol del proyecto

```text
auth-service/
|-- pom.xml
|-- README.md
`-- src/
    `-- main/
        |-- java/
        |   `-- com/
        |       `-- ecomarket/
        |           `-- auth/
        |               |-- AuthServiceApplication.java
        |               |-- config/
        |               |   `-- DataInitializer.java
        |               |-- controller/
        |               |   |-- AuthController.java
        |               |   `-- UsuarioController.java
        |               |-- dto/
        |               |   |-- LoginRequest.java
        |               |   |-- LoginResponse.java
        |               |   |-- RegistroRequest.java
        |               |   `-- UsuarioResponse.java
        |               |-- exception/
        |               |   |-- CredencialesInvalidasException.java
        |               |   |-- ErrorResponse.java
        |               |   |-- GlobalExceptionHandler.java
        |               |   |-- RecursoNoEncontradoException.java
        |               |   `-- UsuarioYaExisteException.java
        |               |-- model/
        |               |   `-- Usuario.java
        |               |-- repository/
        |               |   `-- UsuarioRepository.java
        |               |-- security/
        |               |   |-- JwtFilter.java
        |               |   |-- JwtService.java
        |               |   `-- SecurityConfig.java
        |               `-- service/
        |                   |-- AuthService.java
        |                   `-- UsuarioService.java
        `-- resources/
            |-- application.properties
            `-- db/
                `-- migration/
                    `-- V1__crear_tabla_usuarios.sql
```
