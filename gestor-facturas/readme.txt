# Proyecto: Gestor de Facturas SaaS

Este proyecto es una aplicación web completa desarrollada con **Spring Boot** para la gestión administrativa de facturas y clientes. Permite el control de acceso basado en roles, gestión documental y visualización de datos.

## 1. Arquitectura del Proyecto

La aplicación sigue el patrón de diseño **Modelo-Vista-Controlador (MVC)**, estándar en el desarrollo con Spring Framework:

* **Modelo (Model):** Clases que representan los datos (`Factura`, `Usuario`). Se utiliza **Spring Data JPA** para mapearlos a una base de datos en memoria **H2**.
* **Vista (View):** Plantillas HTML generadas en el servidor usando **Thymeleaf**. Se utiliza **Bootstrap 5** para el diseño responsivo y un sistema de temas (Claro/Oscuro).
* **Controlador (Controller):** Clases (`FacturaController`, `LoginController`) que gestionan las peticiones HTTP, coordinan la lógica de negocio y devuelven las vistas adecuadas.

### Tecnologías Principales
* **Backend:** Java 21, Spring Boot 3.5.
* **Seguridad:** Spring Security (Autenticación y Autorización por roles).
* **Frontend:** HTML5, Thymeleaf, Bootstrap 5.3.
* **Persistencia:** H2 Database (Memoria), Hibernate.
* **Herramientas:** Maven, Lombok.

---

## 2. Principales Funcionalidades

### Gestión de Facturas (CRUD)
* **Listado:** Visualización de facturas en tabla con filtros de búsqueda dinámica.
* **Creación/Edición:** Formulario validado para datos de cliente, importes e impuestos (IVA, IRPF).
* **Borrado:** Eliminación segura de registros (solo Administradores).

### Seguridad y Control de Acceso
* **Login Personalizado:** Sistema de autenticación con formulario propio.
* **Roles:**
    * `ADMIN`: Acceso total (Crear, Editar, Borrar, API REST).
    * `USER`: Acceso de solo lectura a sus propias facturas.
* **Protección de Rutas:** Bloqueo de URLs sensibles y manejo de errores 403 (Acceso Denegado).

### Experiencia de Usuario (UX)
* **Modo Oscuro:** Sistema de preferencias mediante **Cookies** para persistir el tema elegido por el usuario.
* **Diseño Responsivo:** Adaptable a móviles y escritorio.
* **Feedback:** Mensajes de error amigables y páginas 404/403 personalizadas.

### API REST
* Endpoint `/api/facturas` disponible para la integración con sistemas externos (JSON), protegido para uso exclusivo de administradores.

---

## Credenciales de Prueba

| Usuario | Contraseña | Rol | Permisos |
| :--- | :--- | :--- | :--- |
| `admin` | `1234` | **ADMIN** | Control Total + API |
| `cliente` | `12345678Z` | **USER** | Ver sus propias facturas |

*(Nota: Los usuarios clientes se generan dinámicamente según el NIF de las facturas existentes).*