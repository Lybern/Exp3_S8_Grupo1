# Minimarket - API Backend
Implementando de forma avanzada documentación en Microservicios con OpenAPI y HATEOAS

Este proyecto consiste en un sistema backend robusto para la gestión y administración de un **Minimarket**. Está construido con arquitectura de Microservicios orientada al dominio y proporciona endpoints RESTful para la administración integral de Inventarios, Ventas, Usuarios, Categorías, y el Carrito de Compras.

## Tecnologías Principales 🚀
- **Java 20**
- **Spring Boot 3 / 4.x**
- **Spring Data JPA** (Persistencia y ORM)
- **Base de Datos H2** (Configurada en memoria para entornos de desarrollo y testing rápido)
- **Maven** (Gestor de dependencias)
- **Spring Security & JWT** (Autenticación stateless y control de acceso basado en roles)

### Testing y QA 🧪
- **JUnit 5 & Mockito** (Desarrollo y simulación de Pruebas Unitarias aisladas)
- **Jacoco Maven Plugin** (Generación de métricas y reportes de cobertura de código)

### Novedades Recientes (Semana 6) 🛡️
- **Seguridad Perimetral:** Implementación de `AccessDeniedException` y filtros JWT para asegurar que solo usuarios con roles autorizados (ADMINISTRADOR, CAJERO) puedan ejecutar acciones críticas.
- **Refactorización del Carrito:** Introducción de la entidad intermedia `DetalleCarrito` impulsada por el desarrollo guiado por pruebas (TDD), centralizando la validación de stock y acumulación de cantidades.

### Novedades Recientes (Semana 8) 🌟
- **Documentación Avanzada (OpenAPI 3.0):** Integración de `springdoc-openapi` para auto-generar la documentación de todos los endpoints REST. Ahora el sistema cuenta con una interfaz gráfica interactiva (**Swagger UI**) donde los desarrolladores pueden explorar, autenticarse (JWT) y probar la API directamente desde el navegador.
- **Navegación Dinámica (HATEOAS):** Implementación de enlaces Hypermedia en las respuestas JSON usando `PagedModel` y `EntityModel`. Los endpoints clave ahora son capaces de paginar, ordenar y entregar rutas relacionales (`self`, `first`, `last`, `prev`, `next`, `update`, `delete`) que guían al cliente sobre las operaciones disponibles sin necesidad de conocer la estructura de la URL de antemano. Todo expuesto mediante un limpio esquema DTO diseñado a la medida.

---

## Ejecución del Proyecto ⚙️

### Requisitos previos
- Tener JDK 20 instalado en el equipo.
- (Opcional) Contar con Maven instalado globalmente, aunque puedes usar el Wrapper (`mvnw`) que viene en la raíz del proyecto.

### Pasos para iniciar el servidor
Abre una terminal en la raíz del proyecto y ejecuta el siguiente comando:

**En Windows:**
```bash
.\mvnw spring-boot:run
```

**En Linux / Mac:**
```bash
./mvnw spring-boot:run
```

El servidor arrancará por defecto en el puerto `8080`.
La consola de la Base de Datos H2 estará disponible en `http://localhost:8080/h2-console`.
La Documentación Swagger UI estará disponible en `http://localhost:8080/swagger-ui.html`.

---

## Ejecución de Pruebas y Reportes de Cobertura 📊

El proyecto cuenta con una sólida arquitectura de pruebas (Test-Driven Development) diseñada para garantizar la estabilidad, seguridad y correctitud de las reglas de negocio. La suite de pruebas abarca los siguientes frentes:

1. **Pruebas de Dominio (Entity Layer) al 100%:** 
   - Utilizando la librería **OpenPojo**, automatizamos la validación estructural de todos los métodos mutadores (Setters) y accesores (Getters) en la capa de entidades (`EntidadesPojoTest.java`), garantizando una métrica de cobertura perfecta sobre el modelo de datos.
   
2. **Pruebas de Lógica Transaccional (El Carrito):**
   - Se probaron exhaustivamente los escenarios de éxito y error en `Carrito.java`. Validamos mediante aserciones estrictas que agregar un producto existente incrementa su `cantidad` (evitando duplicar filas). También probamos los flujos de fallo controlados, verificando que el sistema levante `IllegalArgumentException` ante productos nulos, cantidades negativas o insuficiencia de stock.

3. **Pruebas de Servicios (Capa Lógica) y Seguridad (Mockito):**
   - Haciendo uso extensivo de **Mockito** (con `@Mock` e `@InjectMocks`), logramos aislar la capa de servicios de la base de datos.
   - Evaluamos los flujos de seguridad comprobando que las operaciones restringidas lanzen un `AccessDeniedException` cuando un usuario sin privilegios de Administrador intenta alterar inventarios o cuando alguien sin rol de Cajero intenta facturar.

4. **Integración Continua (CI/CD) con JaCoCo:**
   - Hemos configurado el plugin de **JaCoCo** a nivel de `pom.xml` para exigir, como regla estricta de compilación, una **cobertura mínima de código del 80%**. Cualquier *commit* que introduzca código no probado romperá el pipeline de construcción, evitando regresiones.

### Instrucciones para ejecutar la Suite de Pruebas

Para correr todos los tests automatizados, comprobar la regla de cobertura y simultáneamente construir el reporte visual de código, ejecuta:

**En Windows:**
```bash
.\mvnw test jacoco:report jacoco:check
```

**En Linux / Mac:**
```bash
./mvnw test jacoco:report jacoco:check
```

Al finalizar con `BUILD SUCCESS`, podrás revisar el desglose exacto línea por línea abriendo el archivo:
`target/site/jacoco/index.html` en tu navegador web de preferencia.

---

## Endpoints Principales y Ejemplos de Uso 🌐

A continuación, se presentan algunos ejemplos de peticiones HTTP para interactuar con la API RESTful. La URL base por defecto es `http://localhost:8080`.

### 1. Gestión de Usuarios (`/api/usuarios`)

**Crear un Usuario (POST)**
```json
POST /api/usuarios
Content-Type: application/json

{
  "username": "jdoe",
  "password": "securepassword123",
  "nombre": "John",
  "apellido": "Doe",
  "email": "jdoe@example.com",
  "direccion": "Avenida Siempre Viva 742"
}
```

**Obtener todos los Usuarios (GET)**
```http
GET /api/usuarios
```

### 2. Gestión de Productos (`/api/productos`)

**Crear un Producto (POST)**
```json
POST /api/productos
Content-Type: application/json

{
  "nombre": "Galletas de Chocolate",
  "precio": 2.50,
  "stock": 50,
  "categoria": {
    "id": 1
  }
}
```

**Actualizar un Producto (PUT)**
```json
PUT /api/productos/1
Content-Type: application/json

{
  "nombre": "Galletas de Chocolate Premium",
  "precio": 3.00,
  "stock": 45
}
```

### 3. Gestión de Ventas (`/api/ventas`)

*Nota: La venta descuenta automáticamente el stock y calcula el total basado en el precio real de la base de datos.*

**Registrar una Venta (POST)**
```json
POST /api/ventas
Content-Type: application/json

{
  "usuario": {
    "id": 1
  },
  "fecha": "2023-10-25T14:30:00",
  "detalles": [
    {
      "producto": {
        "id": 1
      },
      "cantidad": 2
    }
  ]
}
```

### 4. Categorías (`/api/categorias`)

**Eliminar una Categoría (DELETE)**
```http
DELETE /api/categorias/1
```
*(Retorna HTTP 204 No Content si es exitoso, o 404 Not Found si el ID no existe).*
