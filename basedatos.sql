-- Script de creación de base de datos para el sistema de Cuentas y Movimientos
-- Base de datos: PostgreSQL

-- Crear base de datos (ejecutar como superusuario)
-- CREATE DATABASE cuenta_db;
-- \c cuenta_db;

-- Eliminar tablas si existen (para recreación)
DROP TABLE IF EXISTS movimiento CASCADE;
DROP TABLE IF EXISTS cuenta CASCADE;

-- Crear tabla cuenta
CREATE TABLE cuenta (
    numero_cuenta BIGSERIAL PRIMARY KEY,
    tipo_cuenta VARCHAR(20) NOT NULL,
    saldo_inicial NUMERIC(15, 2) NOT NULL,
    estado VARCHAR(10) NOT NULL,
    cliente_id BIGINT NOT NULL,
    CONSTRAINT chk_saldo_positivo CHECK (saldo_inicial >= 0),
    CONSTRAINT chk_estado_valido CHECK (estado IN ('Activa', 'Inactiva', 'Cancelada'))
);

-- Crear tabla movimiento
CREATE TABLE movimiento (
    numero_movimiento BIGSERIAL PRIMARY KEY,
    fecha_movimiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento VARCHAR(20) NOT NULL,
    valor NUMERIC(15, 2) NOT NULL,
    saldo NUMERIC(15, 2) NOT NULL,
    numero_cuenta BIGINT NOT NULL,
    CONSTRAINT fk_movimiento_cuenta 
        FOREIGN KEY (numero_cuenta) 
        REFERENCES cuenta(numero_cuenta) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT chk_saldo_movimiento_positivo CHECK (saldo >= 0)
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX idx_cuenta_cliente_id ON cuenta(cliente_id);
CREATE INDEX idx_movimiento_numero_cuenta ON movimiento(numero_cuenta);
CREATE INDEX idx_movimiento_fecha ON movimiento(fecha_movimiento);

-- Comentarios en las tablas
COMMENT ON TABLE cuenta IS 'Tabla que almacena la información de las cuentas bancarias';
COMMENT ON TABLE movimiento IS 'Tabla que almacena los movimientos/transacciones de las cuentas';

-- Comentarios en las columnas
COMMENT ON COLUMN cuenta.numero_cuenta IS 'Identificador único de la cuenta';
COMMENT ON COLUMN cuenta.tipo_cuenta IS 'Tipo de cuenta (Ahorros, Corriente, etc.)';
COMMENT ON COLUMN cuenta.saldo_inicial IS 'Saldo actual de la cuenta';
COMMENT ON COLUMN cuenta.estado IS 'Estado de la cuenta (Activa, Inactiva, Cancelada)';
COMMENT ON COLUMN cuenta.cliente_id IS 'ID del cliente asociado (referencia al microservicio de clientes)';

COMMENT ON COLUMN movimiento.numero_movimiento IS 'Identificador único del movimiento';
COMMENT ON COLUMN movimiento.fecha_movimiento IS 'Fecha y hora del movimiento';
COMMENT ON COLUMN movimiento.tipo_movimiento IS 'Tipo de movimiento (Depósito, Retiro, etc.)';
COMMENT ON COLUMN movimiento.valor IS 'Valor del movimiento (puede ser positivo o negativo)';
COMMENT ON COLUMN movimiento.saldo IS 'Saldo de la cuenta después de este movimiento';
COMMENT ON COLUMN movimiento.numero_cuenta IS 'Referencia a la cuenta asociada';

-- Datos de ejemplo (opcional - descomentar si se desea)
/*
-- Insertar cuentas de ejemplo
INSERT INTO cuenta (tipo_cuenta, saldo_inicial, estado, cliente_id) VALUES
('Ahorros', 1000.00, 'Activa', 1),
('Corriente', 2500.50, 'Activa', 1),
('Ahorros', 500.00, 'Activa', 2);

-- Insertar movimientos de ejemplo
INSERT INTO movimiento (fecha_movimiento, tipo_movimiento, valor, saldo, numero_cuenta) VALUES
(CURRENT_TIMESTAMP, 'Depósito', 500.00, 1500.00, 1),
(CURRENT_TIMESTAMP, 'Retiro', -200.00, 1300.00, 1),
(CURRENT_TIMESTAMP, 'Depósito', 1000.00, 3500.50, 2);
*/

