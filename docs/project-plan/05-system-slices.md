# System Slices

El sistema puede trabajarse por cortes relativamente independientes:

- acceso y sesion: login, logout, proteccion de vistas, confirmacion
- participante: alta, perfil, venue principal, correo y telefono
- competencia publica: welcome, standings, penalties, contenido abierto
- operacion privada: regattas, registrations, cars, variants, venues
- infraestructura: build, deploy, correo, base de datos, GlassFish

Regla de evolucion:

- cada slice debe poder verificarse con una ruta o flujo concreto

Prioridad tecnica:

1. acceso y sesion
2. participante
3. competencia publica
4. operacion privada
5. infraestructura de produccion
