# Minimarket - API Backend

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

---

## Ejecución de Pruebas y Reportes de Cobertura 📊

El proyecto cuenta con una sólida arquitectura de pruebas que cubre lógicas de negocio, validaciones y garantiza un **100% de cobertura en la capa de Controladores**.

Para correr todos los tests automatizados y simultáneamente construir el reporte de cobertura de código (Jacoco), ejecuta:

**En Windows:**
```bash
.\mvnw test jacoco:report
```

Al finalizar con `BUILD SUCCESS`, podrás revisar en detalle la métrica abriendo el archivo:
`target/site/jacoco/index.html` en tu navegador web de preferencia, o explorando `target/site/jacoco/jacoco.csv`.

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
