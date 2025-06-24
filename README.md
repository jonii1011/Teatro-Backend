# 🎭 Sistema Teatro Gran Espectáculo - Backend API

La documentación completa de la API está disponible en Swagger:
- **URL**: `http://localhost:8080/swagger-ui.html`

API REST completa para gestión de eventos teatrales, desarrollada con **Spring Boot 3.x** y **MySQL**.

## 📋 Descripción

Backend del sistema de gestión teatral que proporciona una API REST completa para manejar eventos, reservas, clientes y sistema de fidelización.

## ✨ Características de la API

### 🎪 Gestión de Eventos
- **Obras de Teatro**: Entradas General y VIP
- **Recitales**: Entradas Campo, Platea y Palco  
- **Charlas/Conferencias**: Con/Sin Meet & Greet
- Control de capacidad y disponibilidad
- Gestión de precios por tipo de entrada

### 👥 Gestión de Clientes
- **Registro completo**: Nombre, apellido, email, teléfono
- **Sistema de activación/desactivación** (soft delete)
- **Búsqueda avanzada** por nombre, apellido o email
- **Tracking de eventos asistidos** automático
- **Clasificación VIP** para clientes frecuentes
- **Historial completo** de reservas por cliente

### 📝 Sistema de Reservas
- CRUD completo de reservas
- Estados: Confirmada/Cancelada
- Validaciones de disponibilidad
- Generación automática de códigos de reserva

### 🎁 Sistema de Fidelización
- **Pase gratuito cada 5 eventos asistidos**
- Tracking automático de asistencias
- API de estadísticas de fidelización
- Reportes en tiempo real

### 📊 API de Estadísticas
- Métricas de eventos y ventas
- Reportes de ocupación
- Análisis de clientes frecuentes
- Estadísticas por períodos

## 🛠️ Tecnologías Utilizadas

- **Java 17**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **Spring Web**
- **MySQL 8.0+**
- **Maven**
- **Swagger/OpenAPI 3**

## 🚀 Instalación y Configuración

### Prerrequisitos
- Java 17 o superior
- MySQL 8.0 o superior
- Maven 3.6+
- Git

### 1. Clonar el Repositorio
```bash
git clone [URL_DEL_REPOSITORIO_BACKEND]
cd teatro-backend
```

### 2. Configurar Base de Datos MySQL

```bash
# Conectar a MySQL
mysql -u root -p

# Ejecutar el script de creación
mysql -u root -p < database/teatro_schema.sql
```

**Configurar conexión en `application.properties`:**
```properties
spring.application.name=backend

# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/teatro_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Swagger Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true
springdoc.swagger-ui.filter=true

# Security & Timezone
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
spring.jackson.time-zone=America/Argentina/Buenos_Aires
spring.jackson.date-format=yyyy-MM-dd
```

### 3. Configurar y Ejecutar Backend

```bash
# Instalar dependencias y ejecutar
./mvnw clean install
./mvnw spring-boot:run
```

El backend estará disponible en: `http://localhost:8080`

## 📚 Documentación de API

### Endpoints Principales

#### 🎭 Gestión de Eventos
- `GET /api/eventos` - Listar todos los eventos
- `POST /api/eventos` - Crear nuevo evento
- `GET /api/eventos/{id}` - Obtener evento por ID
- `PUT /api/eventos/{id}` - Actualizar evento
- `DELETE /api/eventos/{id}` - Eliminar evento
- `GET /api/eventos/vigentes` - Eventos activos con fecha futura
- `GET /api/eventos/tipo/{tipoEvento}` - Filtrar por tipo (OBRA_TEATRO, RECITAL, CHARLA_CONFERENCIA)
- `GET /api/eventos/con-disponibilidad` - Eventos con entradas disponibles
- `GET /api/eventos/{id}/disponibilidad/{tipoEntrada}` - Verificar disponibilidad específica
- `GET /api/eventos/{id}/precio/{tipoEntrada}` - Obtener precio por tipo de entrada

#### 👥 Gestión de Clientes
- `GET /api/clientes` - Listar todos los clientes
- `POST /api/clientes` - Crear nuevo cliente
- `GET /api/clientes/{id}` - Obtener cliente por ID
- `PUT /api/clientes/{id}` - Actualizar cliente
- `DELETE /api/clientes/{id}` - Eliminar cliente (soft delete)
- `GET /api/clientes/activos` - Solo clientes activos
- `GET /api/clientes/email/{email}` - Buscar por email
- `GET /api/clientes/frecuentes` - Clientes con 5+ eventos
- `GET /api/clientes/con-pases-gratuitos` - Clientes con pases disponibles
- `POST /api/clientes/{id}/usar-pase-gratuito` - Consumir pase gratuito

#### 🎫 Gestión de Reservas
- `GET /api/reservas` - Listar todas las reservas
- `POST /api/reservas` - Crear nueva reserva
- `GET /api/reservas/{id}` - Obtener reserva por ID
- `DELETE /api/reservas/{id}` - Eliminar reserva
- `PUT /api/reservas/{id}/cancelar` - Cancelar reserva
- `GET /api/reservas/cliente/{clienteId}` - Reservas de un cliente
- `GET /api/reservas/evento/{eventoId}` - Reservas de un evento
- `GET /api/reservas/codigo/{codigoReserva}` - Buscar por código
- `POST /api/reservas/con-pase-gratuito` - Crear reserva con pase gratuito
- `GET /api/reservas/evento/{eventoId}/ingresos` - Calcular ingresos del evento

#### 🎁 Sistema de Fidelización
- `GET /api/fidelizacion/estadisticas` - Estadísticas generales del programa
- `GET /api/fidelizacion/ranking-clientes-frecuentes` - Top 10 clientes más frecuentes
- `GET /api/fidelizacion/asistencias-ano-actual/{clienteId}` - Asistencias del año
- `GET /api/fidelizacion/pases-pendientes/{clienteId}` - Pases gratuitos disponibles
- `GET /api/fidelizacion/estadisticas-cliente/{clienteId}` - Estadísticas detalladas del cliente
- `GET /api/fidelizacion/reporte-mensual` - Reporte mensual de fidelización

## 🗄️ Base de Datos

### Configuración MySQL
- **Base de datos**: `teatro_db` (se crea automáticamente)
- **Charset**: `utf8mb4_unicode_ci`
- **Motor**: InnoDB
- **Timezone**: UTC

### Script de Instalación
```bash
# 1. Crear la base de datos (opcional - se crea automáticamente)
mysql -u root -p < database/teatro_schema.sql

# 2. Verificar la creación
mysql -u root -p teatro_db
SHOW TABLES;
```

### Configuración de Conexión
La configuración ya está lista en `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/teatro_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
```

### Datos de Prueba
El sistema incluye datos de prueba que se insertan automáticamente con el script.

## 🎯 Funcionalidades Implementadas

### ✅ Requisitos Obligatorios
- [x] Gestión completa de eventos (3 tipos)
- [x] Sistema de reservas con estados
- [x] Control de capacidad
- [x] Sistema de fidelización (5 eventos = 1 gratis)
- [x] Base de datos de clientes

### ✅ Funcionalidades Extra
- [x] Dashboard con métricas
- [x] Búsqueda y filtros avanzados
- [x] UI moderna
- [x] Wizard de reservas paso a paso
- [x] Validaciones completas
- [x] Manejo de errores

## 🧪 Testing

```bash
./mvnw test
```

## 👨‍💻 Autor

**Jonathan Vera**
- Email: jonii10lea@gmail.com
- LinkedIn: www.linkedin.com/in/jonathan-vera-0b9784241

### Problemas Comunes
- **Error de conexión MySQL**: Verificar que el servicio esté iniciado
- **Puerto 8080 ocupado**: Cambiar puerto en `application.properties`
- **Error de versiones**: Verificar Java 17+ y Node 16+

**¡Gracias por usar el Sistema de Gestión Teatro Gran Espectáculo! 🎭✨**
