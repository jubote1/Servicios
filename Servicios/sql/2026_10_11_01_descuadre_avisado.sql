-- ---------------------------------------------------------------------------
-- MEMORIA DE LOS DESCUADRES YA AVISADOS
--
-- El resumen de operacion corre cada 30 minutos y, mientras el descuadre no se
-- corrija, volvia a mandar el mismo correo con el mismo pedido. Un aviso que
-- llega veinte veces deja de ser un aviso.
--
-- Esta tabla guarda lo que ya se aviso. La llave unica incluye el total y lo
-- pagado a proposito: si la tienda corrige a medias y las cifras cambian, es un
-- descuadre distinto y SI vuelve a avisar.
--
-- Idempotente: se puede correr varias veces.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS general.descuadre_avisado (
	iddescuadre_avisado BIGINT        NOT NULL AUTO_INCREMENT,
	idtienda            INT           NOT NULL,
	fechapedido         DATE          NOT NULL,
	idpedidotienda      INT           NOT NULL,
	total               DECIMAL(14,2) NOT NULL,
	pagado              DECIMAL(14,2) NOT NULL,
	fecha_aviso         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (iddescuadre_avisado),
	UNIQUE KEY uk_descuadre_avisado (idtienda, fechapedido, idpedidotienda, total, pagado),
	KEY ix_descuadre_avisado_fecha (fechapedido)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SELECT COUNT(*) AS filas_ya_anotadas FROM general.descuadre_avisado;
