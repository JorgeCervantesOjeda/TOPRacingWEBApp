# Alcance

## Alcance De Esta Implementación

La app Java/MySQL debe cubrir:

- página pública de bienvenida;
- login, logout y manejo de sesión;
- alta, edición, confirmación de correo y recuperación de contraseña de participantes;
- consulta pública de Standings;
- administración de participantes, sedes, variantes, vehículos, eventos, inscripciones, resultados, pujas y puntos;
- importación y conservación de circuitos reales desde archivo de My Maps;
- base real limpia para pruebas manuales;
- base aislada para pruebas automáticas;
- despliegue como WAR en GlassFish.

## Alcance De Producto Objetivo

La SRS v2.7 define además:

- ciclo completo de evento: `creado`, `inscripciones`, `velocidad`, `carrera`, `subasta`, `publicado`, `cancelado`;
- convocatorias con reglas publicadas;
- matriz de pesos por niveles territoriales y temporales;
- campeonatos y clasificaciones por modalidad;
- subasta sellada de acceso para espectadores;
- subasta sellada de vehículos;
- cálculo de eficiencia relativa al mercado;
- pagos, retenciones, devoluciones, comisiones y distribución económica posterior;
- patrocinio y aportaciones;
- seguridad comunitaria, protestas y disputas de entrega;
- foro, novedades y moderación;
- historial público de eventos y resultados.

## Exclusiones De Esta Versión

Estas exclusiones no eliminan los requisitos de producto, pero no deben bloquear mantenimiento de esta versión si no forman parte de la historia activa:

- reescritura React/Firebase;
- cambio de motor de base de datos;
- rediseño visual completo;
- pasarela real de pagos sin credenciales y reglas operativas aprobadas;
- envío automático de correo cuando no existan credenciales disponibles;
- garantía de acceso físico a eventos externos a la plataforma.

## Criterio De Priorización

Primero se debe proteger la operación real de datos, acceso y consulta. Después se deben completar reglas de evento, resultados y clasificaciones. Las funciones económicas, subastas, moderación y comunidad requieren reglas de seguridad, auditoría y aceptación explícita antes de operar con dinero o consecuencias reales.
