# Datos E Integraciones

## Entidades Principales

La app trabaja con estas familias de datos:

- identidad: `Participant`;
- geografía: `PlanetRegion`, `Country`, `CountryRegion`, `Province`, `ProvinceRegion`;
- sedes: `Venue`, `Variant`;
- competencia: `Regatta`, `Registration`, resultados y penalizaciones;
- vehículos: `Car`;
- pujas: `Bid`;
- clasificaciones: `Pointscount`;
- soporte operativo: estadísticas, sesión, claves y correo.

## Campos Derivados De Eficiencia

Para eficiencia relativa al mercado, la SRS oficial completa define el modelo log-lineal `ln(C)=a+bS`.

En la app Java/MySQL:

- `Regatta.intercept` conserva el parámetro derivado `a`;
- `Regatta.slope` conserva el parámetro derivado `b`;
- `Registration.value_auction` conserva el precio oficial de subasta `C_i`;
- `Registration.value_base` conserva el precio de tendencia `C_hat(S_i)`;
- `Registration.pos_efficiency` conserva la posición oficial derivada de ordenar `C_hat(S_i)-C_i` en forma descendente.

Estos campos son derivados del cierre de subasta y resultados computables; no son campos de captura manual para eventos nuevos.

## Jerarquía Territorial

Cada `Variant` pertenece a un `Venue`. Cada `Venue` debe pertenecer a una región estatal o provincial. La cadena completa debe llegar hasta región planetaria:

`Variant -> Venue -> ProvinceRegion -> Province -> CountryRegion -> Country -> PlanetRegion`.

Requisito vigente:

- todo circuito real importado desde My Maps debe tener al menos una `Variant`;
- todo `Venue` real debe tener coordenadas y cadena territorial completa cuando el archivo de origen lo permita;
- las altas futuras de sedes deben preservar la cadena territorial;
- los nombres generados por coordenadas solo son aceptables cuando el archivo de origen no trae nombre utilizable.

## Bases De Datos

Base real:

- nombre local: `topracing26`;
- uso: datos reales y pruebas manuales;
- contenido esperado: participantes reales y circuitos reales importados.

Base de pruebas:

- nombre local: `topracing26_test`;
- uso: pruebas automáticas;
- puede recrearse desde la base real;
- puede contener participantes, venues, variants, regattas, registrations, cars, bids y pointscounts temporales.

Regla obligatoria: una prueba automática que escriba datos debe apuntar a `topracing26_test`.

## Integraciones

- MySQL para persistencia.
- GlassFish para runtime de aplicación.
- Correo saliente para confirmación, recuperación y notificaciones cuando exista transporte SMTP u OAuth configurado.
- My Maps CSV como fuente de alta inicial de circuitos reales.
- Geocodificación inversa como apoyo para ubicar sedes en región estatal, estado, región de país, país y región planetaria.

## Calidad De Datos

- Los datos reales no deben mezclarse con fixtures automáticos.
- Los datos importados deben ser deduplicados por nombre y coordenadas cuando sea posible.
- Los datos de prueba deben usar nombres, correos o marcas reconocibles para poder limpiarse.
- La eliminación de datos de prueba no debe borrar participantes reales ni sedes reales.

## Dependencias Pendientes

- Sin transporte de correo configurado, la confirmación y recuperación pueden generar claves, pero el envío automático debe fallar con diagnóstico explícito.
- Sin reglas editoriales adicionales, la geocodificación inversa es una ayuda operativa y debe poder corregirse manualmente cuando una ubicación quede mal clasificada.
