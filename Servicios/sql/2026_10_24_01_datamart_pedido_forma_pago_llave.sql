-- ---------------------------------------------------------------------------
-- LA LLAVE DE datamart.pedido_forma_pago NO AGUANTA DOS FORMAS DE PAGO
--
-- Se corre en el CENTRAL (172.19.0.25), esquema datamart. Idempotente.
--
-- QUE PASA
--
-- La tabla existe desde hace rato y esta en CERO filas: nadie la ha llenado
-- nunca. Por eso, para saber cuanto se pago con tarjeta, con QR o por link,
-- habia que entrar a las once tiendas una por una.
--
-- Al ir a replicarla aparece el problema: su llave primaria es
-- (idpedidotienda, idtienda), o sea UNA forma de pago por pedido. Y un pedido
-- se puede pagar con dos: en San Antonio, del 1 al 18 de octubre, 8 de 2.097
-- pedidos pagaron con dos formas. Con esa llave la replica reventaria con
-- "Duplicate entry" y tumbaria el dia entero de esa tienda.
--
-- Se cambia a (idpedido_forma_pago, idpedidotienda, idtienda), que es la misma
-- forma de datamart.detalle_pedido. El idpedido_forma_pago es autoincremental
-- POR TIENDA, asi que solo con idtienda queda unico en el datamart.
--
-- Cambiar la llave es seguro porque la tabla esta vacia. Si algun dia tuviera
-- filas, esto habria que pensarlo distinto.
-- ---------------------------------------------------------------------------

-- 1. Quitar la llave vieja solo si es la que estorba.
SET @vieja := (SELECT COUNT(*) FROM information_schema.statistics
                WHERE table_schema = 'datamart' AND table_name = 'pedido_forma_pago'
                  AND index_name = 'PRIMARY' AND column_name = 'idpedido_forma_pago');
SET @s := IF(@vieja = 0,
	'ALTER TABLE datamart.pedido_forma_pago DROP PRIMARY KEY',
	'DO 0');
PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 2. Poner la buena.
SET @s := IF(@vieja = 0,
	'ALTER TABLE datamart.pedido_forma_pago
	   ADD PRIMARY KEY (idpedido_forma_pago, idpedidotienda, idtienda)',
	'DO 0');
PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 3. El indice por pedido, que es como se va a consultar siempre: dame las
--    formas de pago de este pedido de esta tienda.
SET @hay := (SELECT COUNT(*) FROM information_schema.statistics
              WHERE table_schema = 'datamart' AND table_name = 'pedido_forma_pago'
                AND index_name = 'idx_pedidotienda');
SET @s := IF(@hay = 0,
	'ALTER TABLE datamart.pedido_forma_pago
	   ADD INDEX idx_pedidotienda (idpedidotienda, idtienda)',
	'DO 0');
PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 4. Y uno por forma de pago, para sumar por canal sin recorrer la tabla.
SET @hay := (SELECT COUNT(*) FROM information_schema.statistics
              WHERE table_schema = 'datamart' AND table_name = 'pedido_forma_pago'
                AND index_name = 'idx_forma');
SET @s := IF(@hay = 0,
	'ALTER TABLE datamart.pedido_forma_pago
	   ADD INDEX idx_forma (idtienda, idforma_pago)',
	'DO 0');
PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- Como quedo.
SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS cols
  FROM information_schema.statistics
 WHERE table_schema = 'datamart' AND table_name = 'pedido_forma_pago'
 GROUP BY index_name;
