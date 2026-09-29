-- Destinatarios del Excel semanal de consignaciones (ServiciosSer.ReporteSemanalConsignaciones),
-- una hoja por tienda mas un resumen, generado la madrugada del lunes.
--
-- Va en general.parametros_correo, PK compuesta (valorparametro, correo): INSERT IGNORE para
-- poder correr esto de nuevo sin duplicar si alguna fila ya existiera.
--
-- Correr contra la base "general" del servidor donde corre Servicios (172.19.0.25).
insert ignore into parametros_correo (valorparametro, correo) values
  ('REPORTESEMCONSIGNACIONESEXCEL', 'jubote1@gmail.com'),
  ('REPORTESEMCONSIGNACIONESEXCEL', 'monibote1@gmail.com'),
  ('REPORTESEMCONSIGNACIONESEXCEL', 'beatriz.guerra1969@gmail.com'),
  ('REPORTESEMCONSIGNACIONESEXCEL', 'monibote2019@gmail.com');
