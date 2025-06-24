
-- =============================================================================
-- DATOS DE PRUEBA - SISTEMA TEATRO GRAN ESPECTÁCULO
-- =============================================================================
-- Script funcional y probado
-- Autor: Jonathan Vera
-- =============================================================================

-- -----------------------------------------------------------------------------
-- INSERTAR CLIENTES DE PRUEBA
-- -----------------------------------------------------------------------------
INSERT INTO clientes (nombre, apellido, email, dni, telefono, fecha_nacimiento, eventos_asistidos, pases_gratuitos, activo, fecha_registro) VALUES
('María', 'González', 'maria.gonzalez@email.com', '12345678', '+54911234567', '1985-03-15', 3, 0, TRUE, NOW()),
('Juan', 'Pérez', 'juan.perez@email.com', '87654321', '+54911234568', '1990-07-22', 7, 1, TRUE, NOW()),
('Ana', 'Rodríguez', 'ana.rodriguez@email.com', '11223344', '+54911234569', '1988-11-10', 2, 0, TRUE, NOW()),
('Carlos', 'López', 'carlos.lopez@email.com', '44332211', '+54911234570', '1992-01-05', 5, 1, TRUE, NOW()),
('Laura', 'Martínez', 'laura.martinez@email.com', '55667788', '+54911234571', '1987-09-18', 1, 0, TRUE, NOW()),
('Diego', 'Sánchez', 'diego.sanchez@email.com', '88776655', '+54911234572', '1995-05-12', 4, 0, TRUE, NOW()),
('Sofía', 'Fernández', 'sofia.fernandez@email.com', '99887766', '+54911234573', '1983-12-25', 6, 1, TRUE, NOW()),
('Pablo', 'García', 'pablo.garcia@email.com', '66778899', '+54911234574', '1991-04-30', 8, 1, TRUE, NOW()),
('Valentina', 'Romero', 'valentina.romero@email.com', '33445566', '+54911234575', '1994-08-14', 2, 0, TRUE, NOW()),
('Martín', 'Silva', 'martin.silva@email.com', '77889900', '+54911234576', '1989-02-28', 3, 0, TRUE, NOW());

-- -----------------------------------------------------------------------------
-- INSERTAR EVENTOS DE PRUEBA
-- -----------------------------------------------------------------------------
INSERT INTO eventos (nombre, descripcion, fecha_hora, tipo_evento, capacidad_total, precio_base, activo, fecha_creacion, fecha_actualizacion) VALUES
-- Obras de Teatro
('Hamlet', 'Clásico drama de Shakespeare protagonizado por los mejores actores del país', '2025-07-15 20:00:00', 'OBRA_TEATRO', 200, 2500.00, TRUE, NOW(), NOW()),
('El Fantasma de la Ópera', 'Musical romántico que ha cautivado audiencias por décadas', '2025-07-20 21:00:00', 'OBRA_TEATRO', 300, 3500.00, TRUE, NOW(), NOW()),
('La Casa de Bernarda Alba', 'Drama intenso de García Lorca sobre secretos familiares', '2025-07-25 19:30:00', 'OBRA_TEATRO', 150, 2000.00, TRUE, NOW(), NOW()),
-- Recitales
('Fito Páez en Vivo', 'Concierto único del ícono del rock nacional argentino', '2025-08-05 21:30:00', 'RECITAL', 5000, 4500.00, TRUE, NOW(), NOW()),
('Sinfónica de Buenos Aires', 'Concierto de música clásica con las mejores interpretaciones', '2025-08-10 20:00:00', 'RECITAL', 800, 3000.00, TRUE, NOW(), NOW()),
('Divididos - Gira 2025', 'Rock argentino en su máxima expresión con todos sus éxitos', '2025-08-15 22:00:00', 'RECITAL', 3000, 5000.00, TRUE, NOW(), NOW()),
-- Charlas/Conferencias
('Inteligencia Artificial y Futuro', 'Conferencia sobre el impacto de la IA en la sociedad moderna', '2025-07-30 18:00:00', 'CHARLA_CONFERENCIA', 100, 1500.00, TRUE, NOW(), NOW()),
('Liderazgo en el Siglo XXI', 'Charla motivacional sobre habilidades de liderazgo moderno', '2025-08-01 19:00:00', 'CHARLA_CONFERENCIA', 120, 1800.00, TRUE, NOW(), NOW()),
('Emprendimiento Digital', 'Workshop práctico para emprendedores del mundo digital', '2025-08-03 17:30:00', 'CHARLA_CONFERENCIA', 80, 2200.00, TRUE, NOW(), NOW()),
('Sustentabilidad Ambiental', 'Charla sobre ecología y cuidado del medio ambiente', '2025-08-08 16:00:00', 'CHARLA_CONFERENCIA', 90, 1200.00, TRUE, NOW(), NOW());

-- -----------------------------------------------------------------------------
-- INSERTAR PRECIOS POR TIPO DE ENTRADA
-- -----------------------------------------------------------------------------

-- Precios para Obras de Teatro (IDs 1, 2, 3)
INSERT INTO evento_precios (evento_id, precios_KEY, precio) VALUES
(1, 'GENERAL', 2500.00), (1, 'VIP', 4000.00),
(2, 'GENERAL', 3500.00), (2, 'VIP', 5500.00),
(3, 'GENERAL', 2000.00), (3, 'VIP', 3200.00);

-- Precios para Recitales (IDs 4, 5, 6)
INSERT INTO evento_precios (evento_id, precios_KEY, precio) VALUES
(4, 'CAMPO', 4500.00), (4, 'PLATEA', 6500.00), (4, 'PALCO', 8500.00),
(5, 'CAMPO', 3000.00), (5, 'PLATEA', 4500.00), (5, 'PALCO', 6000.00),
(6, 'CAMPO', 5000.00), (6, 'PLATEA', 7500.00), (6, 'PALCO', 10000.00);

-- Precios para Charlas/Conferencias (IDs 7, 8, 9, 10)
INSERT INTO evento_precios (evento_id, precios_KEY, precio) VALUES
(7, 'SIN_MEET_GREET', 1500.00), (7, 'CON_MEET_GREET', 2500.00),
(8, 'SIN_MEET_GREET', 1800.00), (8, 'CON_MEET_GREET', 2800.00),
(9, 'SIN_MEET_GREET', 2200.00), (9, 'CON_MEET_GREET', 3500.00),
(10, 'SIN_MEET_GREET', 1200.00), (10, 'CON_MEET_GREET', 2000.00);

-- -----------------------------------------------------------------------------
-- INSERTAR CAPACIDADES POR TIPO DE ENTRADA
-- -----------------------------------------------------------------------------

-- Capacidades para Obras de Teatro (IDs 1, 2, 3)
INSERT INTO evento_capacidades (evento_id, capacidades_KEY, capacidad) VALUES
(1, 'GENERAL', 150), (1, 'VIP', 50),
(2, 'GENERAL', 220), (2, 'VIP', 80),
(3, 'GENERAL', 120), (3, 'VIP', 30);

-- Capacidades para Recitales (IDs 4, 5, 6)
INSERT INTO evento_capacidades (evento_id, capacidades_KEY, capacidad) VALUES
(4, 'CAMPO', 3500), (4, 'PLATEA', 1200), (4, 'PALCO', 300),
(5, 'CAMPO', 500), (5, 'PLATEA', 250), (5, 'PALCO', 50),
(6, 'CAMPO', 2200), (6, 'PLATEA', 700), (6, 'PALCO', 100);

-- Capacidades para Charlas/Conferencias (IDs 7, 8, 9, 10)
INSERT INTO evento_capacidades (evento_id, capacidades_KEY, capacidad) VALUES
(7, 'SIN_MEET_GREET', 80), (7, 'CON_MEET_GREET', 20),
(8, 'SIN_MEET_GREET', 100), (8, 'CON_MEET_GREET', 20),
(9, 'SIN_MEET_GREET', 60), (9, 'CON_MEET_GREET', 20),
(10, 'SIN_MEET_GREET', 70), (10, 'CON_MEET_GREET', 20);

-- -----------------------------------------------------------------------------
-- INSERTAR RESERVAS DE PRUEBA
-- -----------------------------------------------------------------------------
INSERT INTO reservas (cliente_id, evento_id, tipo_entrada, estado, precio_pagado, es_pase_gratuito, fecha_confirmacion, fecha_reserva, codigo_reserva) VALUES
(1, 1, 'GENERAL', 'CONFIRMADA', 2500.00, FALSE, NOW(), NOW(), 'RES-ABC12345'),
(1, 7, 'SIN_MEET_GREET', 'CONFIRMADA', 1500.00, FALSE, NOW(), NOW(), 'RES-DEF67890'),
(1, 4, 'CAMPO', 'CONFIRMADA', 4500.00, FALSE, NOW(), NOW(), 'RES-GHI11111'),
(2, 2, 'VIP', 'CONFIRMADA', 5500.00, FALSE, NOW(), NOW(), 'RES-JKL22222'),
(2, 5, 'PLATEA', 'CONFIRMADA', 4500.00, FALSE, NOW(), NOW(), 'RES-MNO33333'),
(2, 8, 'CON_MEET_GREET', 'CONFIRMADA', 2800.00, FALSE, NOW(), NOW(), 'RES-PQR44444'),
(2, 3, 'GENERAL', 'CONFIRMADA', 0.00, TRUE, NOW(), NOW(), 'RES-STU55555'),
(3, 6, 'CAMPO', 'CONFIRMADA', 5000.00, FALSE, NOW(), NOW(), 'RES-VWX66666'),
(3, 9, 'SIN_MEET_GREET', 'CANCELADA', 2200.00, FALSE, NOW(), NOW(), 'RES-YZA77777'),
(4, 1, 'VIP', 'CONFIRMADA', 4000.00, FALSE, NOW(), NOW(), 'RES-BCD88888'),
(4, 4, 'PLATEA', 'CONFIRMADA', 6500.00, FALSE, NOW(), NOW(), 'RES-EFG99999'),
(4, 7, 'CON_MEET_GREET', 'CONFIRMADA', 2500.00, FALSE, NOW(), NOW(), 'RES-HIJ00000'),
(4, 10, 'SIN_MEET_GREET', 'CONFIRMADA', 0.00, TRUE, NOW(), NOW(), 'RES-KLM11111'),
(5, 2, 'GENERAL', 'CONFIRMADA', 3500.00, FALSE, NOW(), NOW(), 'RES-NOP22222'),
(6, 3, 'VIP', 'CONFIRMADA', 3200.00, FALSE, NOW(), NOW(), 'RES-QRS33333'),
(6, 5, 'CAMPO', 'CONFIRMADA', 3000.00, FALSE, NOW(), NOW(), 'RES-TUV44444'),
(6, 8, 'SIN_MEET_GREET', 'CONFIRMADA', 1800.00, FALSE, NOW(), NOW(), 'RES-WXY55555'),
(7, 6, 'PLATEA', 'CONFIRMADA', 7500.00, FALSE, NOW(), NOW(), 'RES-ZAB66666'),
(7, 9, 'CON_MEET_GREET', 'CONFIRMADA', 3500.00, FALSE, NOW(), NOW(), 'RES-CDE77777'),
(7, 1, 'GENERAL', 'CONFIRMADA', 0.00, TRUE, NOW(), NOW(), 'RES-FGH88888'),
(8, 4, 'PALCO', 'CONFIRMADA', 8500.00, FALSE, NOW(), NOW(), 'RES-IJK99999'),
(8, 2, 'VIP', 'CONFIRMADA', 5500.00, FALSE, NOW(), NOW(), 'RES-LMN00000'),
(8, 10, 'CON_MEET_GREET', 'CONFIRMADA', 2000.00, FALSE, NOW(), NOW(), 'RES-OPQ11111'),
(8, 7, 'SIN_MEET_GREET', 'CONFIRMADA', 0.00, TRUE, NOW(), NOW(), 'RES-RST22222');