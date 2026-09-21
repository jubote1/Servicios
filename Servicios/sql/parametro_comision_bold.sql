-- ---------------------------------------------------------------------------
-- Consignacion diaria BOLD: comision que Bold descuenta de lo que consigna.
-- Base de datos: general (172.19.0.25)
--
-- Porcentaje sobre la venta QR, con el IVA ya incluido. Va en valornumericod
-- porque tiene decimales (valornumerico es entero).
--
-- No es bloqueante: si el parametro no existe, ReporteConsignacionBold usa 1.5
-- por defecto. Correr esto sirve para poder cambiar el porcentaje sin tocar
-- codigo.
-- ---------------------------------------------------------------------------

INSERT INTO general.parametros (valorparametro, valornumericod)
VALUES ('COMISIONBOLD', 1.5) AS nuevo
ON DUPLICATE KEY UPDATE valornumericod = nuevo.valornumericod;
