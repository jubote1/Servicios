-- =====================================================================
-- datamart.venta_semanal_tienda: reparar las semanas que quedaron en
-- cero, y botar las filas que no son de un domingo.
--
-- 2026-10-22
--
-- QUE PASO
--
-- ReporteCierreInventario le pregunta cada domingo a la base de CADA
-- TIENDA cuanto vendio la semana, con PedidoDAO.obtenerTotalesPedidosSemana.
-- Ese metodo tiene un return(0) en el catch: si la consulta falla devuelve
-- cero, y el ciclo lo guardaba como si fuera venta. Para el sistema "la
-- tienda no contesto" y "la tienda no vendio" eran el mismo numero.
--
-- Y como VentaSemanalTiendaDAO hacia un insert pelado contra una llave
-- primaria (idtienda, fecha), con el catch vacio, el reproceso no podia
-- corregirlo: chocaba contra la llave, el error se perdia, y el proceso
-- terminaba diciendo que todo salio bien.
--
-- El caso que lo destapo: Manrique, semana que cierra el 2026-07-19.
-- Quedo en cero teniendo 879 pedidos y 38.296.162 pesos. El cierre de
-- inventario de esa misma semana SI entro (71 renglones, 13.491.743 de
-- costo), asi que en el tablero de rentabilidad Manrique aparecio con 43%
-- de comida sobre la venta en vez de 35%.
--
-- El codigo ya quedo corregido en esta misma entrega. Este script repara
-- los datos que ya estaban malos.
--
-- LA FORMULA
--
-- Es la misma de obtenerTotalesPedidosSemana: total_neto de los pedidos NO
-- anulados de lunes a domingo, menos el valor de los productos marcados
-- no_venta. Verificada contra 88 semanas buenas de sep-oct 2026: 85 dan el
-- peso exacto y 3 difieren, la peor por 50.000 sobre semanas de ~35
-- millones (0,14%). Esas diferencias son pedidos que se anularon en la
-- tienda DESPUES de que el proceso tomo la foto el domingo en la noche; el
-- datamart replico el estado final. No hay forma de recuperar la foto, y
-- 50.000 pesos no mueven ningun porcentaje.
--
-- SE PUEDE CORRER VARIAS VECES
--
-- El UPDATE solo toca filas en cero y el DELETE solo fechas que no son
-- domingo. A la segunda corrida no hay nada que hacer.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Respaldo de todo lo que este script va a tocar.
--    Antes de cambiar nada: si algo sale mal, de aqui se devuelve.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS datamart.venta_semanal_tienda_respaldo_20261022 (
  idtienda INT NOT NULL,
  fecha DATE NOT NULL,
  valor DOUBLE NULL,
  meta DOUBLE NULL,
  motivo VARCHAR(30) NOT NULL,
  PRIMARY KEY (idtienda, fecha)
) ENGINE=InnoDB;

INSERT IGNORE INTO datamart.venta_semanal_tienda_respaldo_20261022
      (idtienda, fecha, valor, meta, motivo)
SELECT v.idtienda, v.fecha, v.valor, v.meta,
       CASE WHEN DAYOFWEEK(v.fecha) <> 1 THEN 'NO ES DOMINGO' ELSE 'EN CERO' END
  FROM datamart.venta_semanal_tienda v
 WHERE DAYOFWEEK(v.fecha) <> 1
    OR (v.valor = 0 AND v.idtienda <> 12 AND v.fecha >= '2026-01-01');

-- ---------------------------------------------------------------------
-- 2. Calcular la venta real de las semanas en cero de 2026.
--
--    Se calcula aparte y no dentro del UPDATE porque la consulta recorre
--    detalle_pedido y es pesada; asi corre una sola vez y el UPDATE
--    despues es inmediato. Tambien deja el calculo a la vista para
--    revisarlo antes de aplicarlo.
--
--    La bodega (12) no es una tienda y se deja por fuera.
-- ---------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS datamart.tmp_venta_reparada;
CREATE TEMPORARY TABLE datamart.tmp_venta_reparada (
  idtienda INT NOT NULL,
  fecha DATE NOT NULL,
  valor DOUBLE NOT NULL,
  pedidos INT NOT NULL,
  PRIMARY KEY (idtienda, fecha)
) ENGINE=InnoDB;

INSERT INTO datamart.tmp_venta_reparada (idtienda, fecha, valor, pedidos)
SELECT v.idtienda, v.fecha,
       (SELECT IFNULL(SUM(d.total_neto),0)
          FROM datamart.pedido d
         WHERE d.idtienda = v.idtienda
           AND d.fechapedido BETWEEN DATE_SUB(v.fecha, INTERVAL 6 DAY) AND v.fecha
           AND d.idmotivoanulacion IS NULL)
     - (SELECT IFNULL(SUM(b.valortotal),0)
          FROM datamart.pedido a
          JOIN datamart.detalle_pedido b
            ON b.idtienda = a.idtienda AND b.idpedidotienda = a.idpedidotienda
          JOIN datamart.producto c ON c.idproducto = b.idproducto
         WHERE a.idtienda = v.idtienda
           AND a.fechapedido BETWEEN DATE_SUB(v.fecha, INTERVAL 6 DAY) AND v.fecha
           AND a.idmotivoanulacion IS NULL
           AND c.no_venta = 1) AS valor,
       (SELECT COUNT(*)
          FROM datamart.pedido p
         WHERE p.idtienda = v.idtienda
           AND p.fechapedido BETWEEN DATE_SUB(v.fecha, INTERVAL 6 DAY) AND v.fecha
           AND p.idmotivoanulacion IS NULL) AS pedidos
  FROM datamart.venta_semanal_tienda v
 WHERE v.valor = 0
   AND v.idtienda <> 12
   AND v.fecha >= '2026-01-01'
   AND DAYOFWEEK(v.fecha) = 1;

-- Lo que se va a escribir, para revisarlo.
SELECT t.nombre, r.fecha, r.pedidos, ROUND(r.valor) AS venta_a_escribir
  FROM datamart.tmp_venta_reparada r
  JOIN pizzaamericana.tienda t ON t.idtienda = r.idtienda
 ORDER BY r.fecha, t.nombre;

-- ---------------------------------------------------------------------
-- 3. Escribirla.
--
--    La condicion valor > 0 es un seguro: si una semana de verdad no tuvo
--    venta, no se toca. Una tienda cerrada toda la semana es un cero
--    legitimo y tiene que poder existir.
-- ---------------------------------------------------------------------
UPDATE datamart.venta_semanal_tienda v
  JOIN datamart.tmp_venta_reparada r
    ON r.idtienda = v.idtienda AND r.fecha = v.fecha
   SET v.valor = r.valor
 WHERE v.valor = 0
   AND r.valor > 0
   AND r.pedidos > 0;

-- ---------------------------------------------------------------------
-- 4. Botar las filas que no son de un domingo.
--
--    Son corridas manuales del reproceso que quedaron grabadas con la
--    fecha del dia en que se ejecutaron. Las seis fechas tienen al lado su
--    domingo completo y mas alto, asi que no se pierde ninguna semana:
--
--      2022-02-05 sab   190.361.820   (el domingo 02-06 tiene 261.102.050)
--      2022-08-20 sab   229.832.070   (el domingo 08-21 tiene 290.961.150)
--      2022-11-05 sab    24.515.420   (11 de 12 tiendas en cero)
--      2023-04-29 sab    24.276.420   (11 de 12 tiendas en cero)
--      2023-12-09 sab             0   (las 12 en cero)
--      2026-05-19 mar             0   (las 11 en cero)
--
--    El tablero de rentabilidad no las ve, porque solo lee los domingos
--    del calendario de gerencia. Pero cualquier consulta directa a la
--    tabla cuenta esas semanas dos veces.
-- ---------------------------------------------------------------------
DELETE FROM datamart.venta_semanal_tienda WHERE DAYOFWEEK(fecha) <> 1;

-- ---------------------------------------------------------------------
-- 5. Como quedo.
-- ---------------------------------------------------------------------
SELECT 'filas que no son domingo' AS revision, COUNT(*) AS quedan
  FROM datamart.venta_semanal_tienda WHERE DAYOFWEEK(fecha) <> 1
UNION ALL
SELECT 'semanas en cero en 2026 (sin bodega)', COUNT(*)
  FROM datamart.venta_semanal_tienda
 WHERE valor = 0 AND idtienda <> 12 AND fecha >= '2026-01-01'
UNION ALL
SELECT 'filas respaldadas', COUNT(*)
  FROM datamart.venta_semanal_tienda_respaldo_20261022;
