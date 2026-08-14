# Propósito Del Sistema

TOPRacingWEBApp debe permitir operar digitalmente la liga TOP Racing en su implementación Java/MySQL.

## Objetivo De Producto

El sistema debe organizar eventos competitivos, participantes, vehículos, sedes, variantes de pista, inscripciones, resultados, clasificaciones, puntos y datos públicos de la liga.

La SRS oficial completa v2.7 amplía ese objetivo con subastas de acceso, subastas de vehículos, eficiencia relativa al mercado, pagos, devoluciones, patrocinio, aportaciones, disputas, moderación comunitaria e historial público. Esta app debe considerarlos como alcance objetivo; los documentos auxiliares solo indican estado observado de implementación.

## Objetivo Operativo De Esta Versión

Esta versión debe:

- exponer información pública básica de TOP Racing sin exigir sesión;
- permitir consulta pública de Standings;
- permitir operación autenticada de participantes y operadores;
- conservar datos reales de circuitos, sedes y variantes;
- mantener una base real para uso manual y una base separada para pruebas automáticas;
- reducir fricción en registro, consulta, administración y validación de competencia.

## Principios Del Dominio

- Los eventos físicos se ejecutan fuera del sistema; el sistema registra, valida, calcula y publica información.
- La clasificación publicada debe conservar rastro verificable hacia reglas y datos oficiales.
- La apertura tecnológica del vehículo es un requisito de producto, pero no elimina reglas de seguridad, cupo, sede, acceso físico ni elegibilidad publicadas.
- La información pública debe ser deliberada; la información reservada debe permanecer protegida.

## Estado De Evidencia

Observación: el código actual implementa una aplicación JSF con persistencia MySQL y páginas públicas/protegidas.

Supuesto: la SRS oficial completa v2.7 describe la visión funcional completa de TOP Racing.

Inferencia: los documentos auxiliares deben separar la visión completa de la SRS oficial y el estado implementado por la app Java para evitar que las pruebas manuales se basen en datos o flujos de prueba.
