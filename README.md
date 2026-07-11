# Minimarket - API Backend (REST, OpenAPI, HATEOAS)

Este proyecto consiste en un sistema backend robusto para la gestión y administración de un **Minimarket**. Está construido con arquitectura de Microservicios orientada al dominio y proporciona endpoints RESTful para la administración integral de Inventarios, Ventas, Usuarios, Categorías, y el Carrito de Compras. 

El proyecto destaca por su alto nivel de madurez técnica, incorporando estándares de la industria como **Test-Driven Development (TDD)**, **Seguridad con JWT**, **Documentación OAS3 (Swagger)** y **Madurez REST Nivel 3 de Richardson (HATEOAS)**.

---

## Tecnologías Principales 🚀
- **Java 20**
- **Spring Boot 3 / 4.x**
- **Spring Data JPA** (Persistencia y ORM)
- **Base de Datos H2** (Configurada en memoria para entornos de desarrollo y testing rápido)
- **Maven** (Gestor de dependencias)
- **Spring Security & JWT** (Autenticación stateless y control de acceso basado en roles)
- **Spring HATEOAS** (Navegación dinámica y paginación)
- **Springdoc OpenAPI (Swagger UI)** (Documentación interactiva de la API)
- **JUnit 5, Mockito & JaCoCo** (Testing y análisis de cobertura)

---

## Implementación de HATEOAS para cumplir requerimientos de la semana 8 🌟

Esta semana el proyecto evolucionó hacia una arquitectura completamente documentada:

1. **Documentación Avanzada (OpenAPI 3.0):** 
   Se integró `springdoc-openapi` para auto-generar la documentación de todos los endpoints. Contamos con una interfaz gráfica interactiva (**Swagger UI**) donde puedes explorar, autenticarte (JWT) y probar la API directamente. El contrato OAS generado garantiza que los tipos de respuesta y descripciones de error calcen al 100% con la implementación real del servidor.
   
2. **Navegación Dinámica y Paginación (HATEOAS):** 
   Implementación de hipermedia de Nivel 3 de Richardson. Mediante `PagedModel` y `EntityModel`, la API es capaz de **auto-paginar** el contenido inyectando metadatos dinámicos (`first`, `last`, `prev`, `next`) en conjunto con enlaces de navegación empresarial (`self`, `allProductos`, `update`, `delete`). Esto permite al cliente descubrir rutas sin depender del enrutamiento estático.

3. **Implementación segura mediante DTOs:** 
   Para evitar que la lógica interna de HATEOAS o las relaciones ORM se filtren, se implementaron **Data Transfer Objects** (Ej: `ProductoResponseDTO`). Gracias a esto, el JSON final es purificado, eliminando campos nulos mediante `@JsonInclude(NON_NULL)` y renombrando las colecciones semánticamente (`productoList`) a través de `@Relation`.

4. **Refactorización:** 
   Los Controladores fueron enriquecidos con comentarios pedagógicos línea a línea (explicando `linkTo`, `methodOn` y `withRel`) demostrando un dominio profesional del framework.

---

## Ejecución del Proyecto ⚙️

### Requisitos previos
- Tener JDK 20 instalado en el equipo.

### Pasos para iniciar el servidor
Abre una terminal en la raíz del proyecto y ejecuta el siguiente comando:

**Windows:**
```bash
.\mvnw clean spring-boot:run
```
**Linux / Mac:**
```bash
./mvnw clean spring-boot:run
```

- **API Base:** `http://localhost:8080`
- **Swagger UI (Documentación Interactiva):** `http://localhost:8080/swagger-ui.html`
- **Base de Datos H2:** `http://localhost:8080/h2-console`

*(Nota: Para acceder a los endpoints protegidos en Swagger, primero debes hacer POST en `/api/auth/login` con credenciales de administrador (ej: admin / 1234), copiar el token JWT, y pegarlo en el botón "Authorize" superior).*

---

## Guía de Endpoints y Ejemplos de Uso (Con HATEOAS) 🌐

A continuación, se muestra cómo interactuar con los endpoints principales y cómo se visualiza la nueva estructura con HATEOAS.

### 1. Autenticación (Login)
Para obtener el token JWT necesario para las demás operaciones.
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

### 2. Gestión de Productos (Paginado + HATEOAS)

**Obtener lista de Productos (GET)**
```http
GET /api/productos?page=0&size=5&sortBy=nombre&sortDir=asc
Authorization: Bearer <TU_TOKEN>
```
**Respuesta JSON esperada:**
```json
{
  "_embedded": {
    "productoList": [
      {
        "id": 1,
        "nombre": "Arroz Grado 1 (1kg)",
        "precio": 1300.0,
        "stock": 50,
        "categoria": { "id": 1, "nombre": "Abarrotes" },
        "_links": {
          "self": { "href": "http://localhost:8080/api/productos/1" },
          "allProductos": { "href": "http://localhost:8080/api/productos?page=0&size=5&sortBy=nombre&sortDir=asc" },
          "update": { "href": "http://localhost:8080/api/productos/1" },
          "delete": { "href": "http://localhost:8080/api/productos/1" }
        }
      }
    ]
  },
  "_links": {
    "self": { "href": "http://localhost:8080/api/productos?page=0&size=5&sortBy=nombre&sortDir=asc" },
    "first": { "href": "http://localhost:8080/api/productos?page=0&size=5&sortBy=nombre&sortDir=asc" },
    "last": { "href": "http://localhost:8080/api/productos?page=2&size=5&sortBy=nombre&sortDir=asc" },
    "next": { "href": "http://localhost:8080/api/productos?page=1&size=5&sortBy=nombre&sortDir=asc" }
  },
  "page": {
    "size": 5,
    "totalElements": 15,
    "totalPages": 3,
    "number": 0
  }
}
```

**Crear un Producto (POST)**
```http
POST /api/productos
Authorization: Bearer <TU_TOKEN>
Content-Type: application/json

{
  "nombre": "Galletas de Chocolate",
  "precio": 2.50,
  "stock": 50,
  "categoria": { "id": 1 }
}
```

**Eliminar un Producto (DELETE)**
```http
DELETE /api/productos/1
Authorization: Bearer <TU_TOKEN>
```
**Respuesta JSON (HATEOAS en acción):**
```json
{
  "message": "Producto eliminado exitosamente",
  "_links": {
    "allProductos": { "href": "http://localhost:8080/api/productos?page=0&size=10&sortBy=nombre&sortDir=asc" },
    "addProducto": { "href": "http://localhost:8080/api/productos" }
  }
}
```

### 3. Gestión de Usuarios

**Obtener un Usuario por ID (GET)**
```http
GET /api/usuarios/1
Authorization: Bearer <TU_TOKEN>
```

---

## Cobertura de Pruebas (CI/CD) 📊

El proyecto cuenta con una cobertura de pruebas automatizadas superior al **80%** exigido.
Para ejecutar la suite de pruebas completa y generar el reporte visual de JaCoCo:

```bash
.\mvnw test jacoco:report jacoco:check
```

Al finalizar, puedes revisar el reporte línea por línea abriendo:
`target/site/jacoco/index.html` en tu navegador.
