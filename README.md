# Microservicio de Cuentas y Movimientos

Microservicio Spring Boot para la gestión de cuentas bancarias y movimientos/transacciones financieras.

## 📋 Descripción

Este microservicio proporciona una API REST para gestionar:
- **Cuentas**: Creación, consulta y actualización de cuentas bancarias asociadas a clientes
- **Movimientos**: Registro de transacciones (depósitos y retiros) con actualización automática de saldos
- **Reportes**: Generación de reportes de estado de cuenta por cliente y rango de fechas

## 🛠️ Tecnologías

- **Java 17**
- **Spring Boot 3.5.7**
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **Gradle**

## 📁 Estructura del Proyecto

```
src/main/java/com/example/demo/
├── Cuenta.java                 # Entidad Cuenta
├── Movimiento.java             # Entidad Movimiento
├── CuentaRepository.java       # Repositorio de Cuentas
├── MovimientoRepository.java   # Repositorio de Movimientos
├── CuentaController.java       # Controlador REST de Cuentas
├── MovimientoController.java   # Controlador REST de Movimientos
├── ReporteController.java     # Controlador REST de Reportes
└── CuentaApplication.java      # Clase principal de la aplicación
```

## ⚙️ Configuración

### Base de Datos

El proyecto está configurado para usar PostgreSQL. La configuración se encuentra en `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mibasedatos
spring.datasource.username=postgres
spring.datasource.password=1234
server.port=8081
```

### Variables de Entorno

Puedes sobrescribir la configuración usando variables de entorno:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## 🚀 Ejecución

### Ejecución Local

```bash
./gradlew bootRun
```

La aplicación estará disponible en `http://localhost:8081`

### Ejecución con Docker

```bash
docker-compose up --build
```

## 📡 Endpoints Disponibles

### Cuentas

#### Crear Cuenta
```bash
POST /cuentas
Content-Type: application/json

{
  "tipoCuenta": "Ahorros",
  "saldoInicial": 1000.00,
  "estado": "Activa",
  "clienteId": "1"
}
```

#### Obtener Todas las Cuentas
```bash
GET /cuentas
```

#### Obtener Cuenta por ID
```bash
GET /cuentas/{id}
```

#### Obtener Cuentas por Cliente
```bash
GET /cuentas/cliente/{clienteId}
```

#### Obtener IDs de Cuentas por Cliente
```bash
GET /cuentas/cliente/{clienteId}/ids
```

#### Actualizar Cuenta
```bash
PUT /cuentas/{id}
Content-Type: application/json

{
  "tipoCuenta": "Corriente",
  "saldoInicial": 2500.00,
  "estado": "Activa",
  "clienteId": "1"
}
```

### Movimientos

#### Crear Movimiento (Depósito)
```bash
POST /movimientos
Content-Type: application/json

{
  "tipoMovimiento": "Depósito",
  "valor": 500.00,
  "cuenta": {
    "numeroCuenta": 1
  }
}
```

#### Crear Movimiento (Retiro)
```bash
POST /movimientos
Content-Type: application/json

{
  "tipoMovimiento": "Retiro",
  "valor": -200.00,
  "cuenta": {
    "numeroCuenta": 1
  }
}
```

**Nota**: Si el saldo resultante es negativo, se devuelve el error `"Saldo no disponible"`.

#### Obtener Todos los Movimientos
```bash
GET /movimientos
```

#### Obtener Movimiento por ID
```bash
GET /movimientos/{id}
```

#### Actualizar Movimiento
```bash
PUT /movimientos/{id}
Content-Type: application/json

{
  "fechaMovimiento": "2024-11-13T15:00:00",
  "tipoMovimiento": "Depósito",
  "valor": 300.00
}
```

### Reportes

#### Generar Reporte de Estado de Cuenta
```bash
GET /reportes?fecha=2024-01-01,2024-12-31&clienteId=1
```

**Parámetros:**
- `fecha`: Rango de fechas en formato `fechaInicio,fechaFin` (yyyy-MM-dd)
- `clienteId`: ID del cliente (String)

**Respuesta:**
```json
{
  "clienteId": "1",
  "fechaInicio": "2024-01-01",
  "fechaFin": "2024-12-31",
  "fechaGeneracion": "2024-11-13T16:30:00",
  "totalCuentas": 2,
  "cuentas": [
    {
      "numeroCuenta": 1,
      "tipoCuenta": "Ahorros",
      "saldoInicial": 1000.00,
      "saldoActual": 1000.00,
      "estado": "Activa",
      "totalMovimientos": 3,
      "totalDebitos": 200.00,
      "totalCreditos": 500.00,
      "movimientos": [
        {
          "numeroMovimiento": 1,
          "fechaMovimiento": "2024-01-15T10:30:00",
          "tipoMovimiento": "Depósito",
          "valor": 500.00,
          "saldo": 1500.00
        }
      ]
    }
  ]
}
```

## 🔧 Características Principales

### Gestión de Saldos
- Los movimientos actualizan automáticamente el saldo de la cuenta
- Validación de saldo disponible: no permite transacciones que resulten en saldo negativo
- El valor puede ser positivo (depósito) o negativo (retiro)

### Integración con Microservicio de Clientes
- Las cuentas están asociadas a clientes mediante `clienteId` (String)
- Al crear una cuenta, se inserta automáticamente en la tabla `cliente_cuentas` para que el otro microservicio pueda encontrarla
- Endpoint específico para obtener solo los IDs de cuentas: `/cuentas/cliente/{clienteId}/ids`

### Reportes
- Filtrado por rango de fechas
- Incluye todas las cuentas del cliente
- Detalle completo de movimientos en el período
- Cálculo de totales de débitos y créditos

## 📝 Ejemplos de Uso con cURL

### Crear una Cuenta
```bash
curl -X POST http://localhost:8081/cuentas \
  -H "Content-Type: application/json" \
  -d '{
    "tipoCuenta": "Ahorros",
    "saldoInicial": 1000.00,
    "estado": "Activa",
    "clienteId": "1"
  }'
```

### Crear un Movimiento de Depósito
```bash
curl -X POST http://localhost:8081/movimientos \
  -H "Content-Type: application/json" \
  -d '{
    "tipoMovimiento": "Depósito",
    "valor": 500.00,
    "cuenta": {
      "numeroCuenta": 1
    }
  }'
```

### Crear un Movimiento de Retiro
```bash
curl -X POST http://localhost:8081/movimientos \
  -H "Content-Type: application/json" \
  -d '{
    "tipoMovimiento": "Retiro",
    "valor": -200.00,
    "cuenta": {
      "numeroCuenta": 1
    }
  }'
```

### Generar Reporte
```bash
curl "http://localhost:8081/reportes?fecha=2024-01-01,2024-12-31&clienteId=1"
```

## 🗄️ Base de Datos

### Script SQL

El proyecto incluye un script `basedatos.sql` para crear las tablas en PostgreSQL:

```sql
-- Tabla cuenta
CREATE TABLE cuenta (
    numero_cuenta BIGSERIAL PRIMARY KEY,
    tipo_cuenta VARCHAR(20) NOT NULL,
    saldo_inicial NUMERIC(15, 2) NOT NULL,
    estado VARCHAR(10) NOT NULL,
    cliente_id VARCHAR(50) NOT NULL
);

-- Tabla movimiento
CREATE TABLE movimiento (
    numero_movimiento BIGSERIAL PRIMARY KEY,
    fecha_movimiento TIMESTAMP NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL,
    valor NUMERIC(15, 2) NOT NULL,
    saldo NUMERIC(15, 2) NOT NULL,
    numero_cuenta BIGINT NOT NULL,
    FOREIGN KEY (numero_cuenta) REFERENCES cuenta(numero_cuenta)
);
```

### Tabla de Relación con Clientes

El microservicio también inserta registros en la tabla `cliente_cuentas`:
```sql
CREATE TABLE cliente_cuentas (
    cliente_id VARCHAR(50),
    cuenta_id BIGINT,
    PRIMARY KEY (cliente_id, cuenta_id)
);
```

## 🐳 Docker

### Docker Compose

El proyecto incluye `docker-compose.yml` para ejecutar la aplicación y PostgreSQL:

```bash
docker-compose up --build
```

### Dockerfile

El `Dockerfile` utiliza multi-stage build para optimizar el tamaño de la imagen.

## 📦 Colección de Postman

El proyecto incluye `postman_collection.json` con todos los endpoints configurados para pruebas.

## ⚠️ Validaciones

- **Saldo no disponible**: Si un movimiento resulta en saldo negativo, se devuelve el error `"Saldo no disponible"`
- **Validación de fechas**: El reporte valida que la fecha de inicio sea anterior a la fecha de fin
- **Validación de parámetros**: Todos los endpoints validan los parámetros requeridos

## 🔗 Integración

Este microservicio se integra con:
- **Microservicio de Clientes** (puerto 8080): Las cuentas están asociadas a clientes mediante `clienteId`

## 📄 Licencia

Este proyecto es un ejemplo educativo.

