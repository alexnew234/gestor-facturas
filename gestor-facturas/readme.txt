#SaaS Facturas - Gestor de Facturación

Aplicación web completa para la gestión de facturas, clientes e ingresos. Desarrollada con **Spring Boot** y diseñada para ofrecer una experiencia de usuario moderna con **Dashboard interactivo**, generación de **PDFs** y soporte para **Modo Oscuro**.

##Descripción del Proyecto

Este sistema permite a pymes y autónomos gestionar su ciclo de facturación de manera sencilla.
Las funcionalidades principales incluyen:
* **CRUD Completo:** Crear, Leer, Editar y Borrar facturas.
* **Dashboard Interactivo:** KPIs en tiempo real (Total facturado, Pendientes, Emitidas) y gráficos visuales.
* **Generación de PDF:** Descarga de facturas en formato profesional (con cálculo automático de IVA/IRPF).
* **Filtrado Inteligente:** Buscador por nombre de cliente o concepto.
* **Seguridad:** Sistema de Login y roles (Admin/Usuario).
* **UX/UI:** Interfaz responsiva con Bootstrap 5 y cambio de tema (Claro/Oscuro) persistente mediante Cookies.

##Tecnologías Utilizadas

* **Backend:** Java 21, Spring Boot 3 (Web, Data JPA, Security).
* **Base de Datos:** H2 Database (Base de datos en memoria para desarrollo rápido).
* **Frontend:** Thymeleaf, HTML5, CSS3, Bootstrap 5.
* **Gráficos:** Chart.js (Librería JavaScript para visualización de datos).
* **PDF:** OpenPDF (Generación de documentos dinámicos).
* **Herramientas:** Maven, IntelliJ IDEA.

##Requisitos Previos

Para ejecutar este proyecto necesitas:
* Java JDK 17 o superior (Recomendado JDK 21).
* Maven (normalmente incluido en IntelliJ).
* Un navegador web moderno (Chrome, Edge, Firefox).

##Pasos para arrancar el proyecto en IntelliJ

1.  **Clonar/Abrir:** Descarga el código y abre la carpeta del proyecto en IntelliJ IDEA.
2.  **Cargar Dependencias (Importante):**
    * Si ves un icono de una "m" pequeña (Maven) en la barra lateral derecha, haz clic y pulsa el botón de "Reload All Maven Projects".
    * Espera a que se descarguen las librerías (OpenPDF, Spring Boot, etc.).
3.  **Ejecutar:**
    * Abre el archivo `src/main/java/com/example/demo/GestorFacturasApplication.java`.
    * Pulsa el botón **Play** (verde) al lado de la clase o arriba a la derecha.
4.  **Acceder:**
    * Abre tu navegador y ve a: `http://localhost:9021`

##Usuarios de Prueba

El sistema arranca con una base de datos limpia (H2). Para acceder, utiliza las siguientes credenciales predeterminadas:


| Rol | Usuario | Contraseña | Permisos |
| **Administrador** | `admin` | `1234` | Acceso total (Crear, Editar, Borrar, Ver Todo, Email) |
| **Usuario** | *Nombre Cliente* | `NIF` | Solo puede ver SUS facturas (Lectura) |

> **Nota:** Al reiniciar la aplicación (Stop/Play), los datos se borran al usar una base de datos en memoria. Deberás crear una factura nueva con el usuario `admin` para probar la descarga de PDF.
