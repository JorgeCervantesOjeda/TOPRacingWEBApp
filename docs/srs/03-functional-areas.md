# Áreas Funcionales

## Identidad Y Cuenta

El sistema debe permitir registro, login, logout, confirmación de correo, recuperación de contraseña y edición de datos básicos del participante.

Requisitos principales:

- no autenticar usuarios inexistentes, no confirmados o inválidos;
- no exponer contraseña ni datos sensibles en consultas públicas;
- registrar fallos relevantes de autenticación o recuperación;
- mantener flujo público para confirmación y recuperación por clave.

Estado: `Parcial`.

## Consulta Pública

El sistema debe permitir consulta pública de información declarada pública.

Para esta versión, Standings es público y de solo lectura. Debe mostrar información deportiva suficiente para consultar clasificaciones sin revelar datos privados.

Estado: `Implementado` para Standings básico; `Objetivo` para historial público completo.

## Geografía, Sedes Y Variantes

El sistema debe conservar una jerarquía territorial completa:

1. planeta;
2. región planetaria;
3. país;
4. región de país;
5. estado o provincia;
6. región estatal o provincial;
7. venue;
8. variant.

Cada circuito real importado debe tener al menos un `Venue`, una `Variant` y una cadena territorial completa hasta `PlanetRegion`.

Estado: `Implementado` para datos base reales importados; `Parcial` para gobierno editorial de altas futuras.

## Vehículos

El sistema debe registrar vehículos asociados a dueños y permitir su uso en inscripciones.

Requisitos objetivo:

- asociar vehículo a dueño;
- registrar características técnicas mínimas;
- exigir tecnología abierta conforme a reglas publicadas;
- impedir operaciones de subasta o transferencia sin estado válido.

Estado: `Parcial`.

## Eventos

El sistema debe gestionar eventos competitivos con estados oficiales.

Estados objetivo:

- `creado`;
- `inscripciones`;
- `velocidad`;
- `carrera`;
- `subasta`;
- `publicado`;
- `cancelado`.

Cada transición debe validar precondiciones, permisos y consistencia de datos. Los resultados publicados deben quedar como historial oficial.

Estado: `Parcial`.

## Inscripciones

El sistema debe permitir registrar participantes y vehículos en eventos abiertos.

Requisitos objetivo:

- admitir inscripción solo en ventana válida;
- conservar inscripciones canceladas como historial;
- excluir inscripciones canceladas de resultados, puntos y subastas;
- distinguir dueño, piloto y comprador cuando aplique.

Estado: `Parcial`.

## Resultados, Puntos Y Clasificaciones

El sistema debe calcular y publicar resultados de velocidad, carrera, eficiencia, puntos y clasificaciones.

Para esta versión, Standings debe quedar disponible públicamente y debe poder recalcularse desde eventos y resultados válidos.

Estado: `Parcial`.

## Subastas

El producto objetivo requiere:

- subasta de acceso para espectadores;
- subasta sellada de vehículos después de carrera;
- una sola puja vigente por usuario y vehículo;
- sustitución de puja solo por importe mayor;
- reserva total de pujas, conteos y consecuencias antes del cierre;
- publicación solo después del cierre;
- reglas de entrega y disputa del vehículo adjudicado.

Estado: `Objetivo`, con entidades existentes que no equivalen por sí solas al cumplimiento normativo completo.

## Economía, Premios Y Patrocinio

El producto objetivo debe gestionar pozos, premios, aportaciones, patrocinios, retenciones, pagos, devoluciones, comisiones y distribución posterior.

Estado: `Objetivo`.

## Comunidad Y Seguridad

El producto objetivo debe gestionar protestas de seguridad, objeciones, foro, novedades, moderación y visibilidad pública/reservada de decisiones.

Estado: `Objetivo`.

## Administración Técnica

El sistema debe permitir operación local reproducible:

- base real para uso manual;
- base aislada para pruebas automáticas;
- importación controlada de circuitos reales;
- pruebas unitarias, de integración y navegador;
- despliegue WAR en GlassFish.

Estado: `Implementado` para la separación local documentada; `Parcial` para automatización completa de ambientes.
