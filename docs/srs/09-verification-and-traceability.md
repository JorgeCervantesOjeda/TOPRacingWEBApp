# Verificación Y Trazabilidad

## Criterios De Aceptación Por Área

### Acceso Público

- `welcome.xhtml` responde sin sesión.
- `login.xhtml` responde sin sesión.
- `confirmusermail.xhtml` responde sin sesión y valida clave antes de confirmar.
- `resetpassword.xhtml` responde sin sesión y valida clave antes de cambiar contraseña.
- `listpointscounts.xhtml` responde sin sesión y muestra Standings de solo lectura.
- Una vista protegida pedida sin sesión redirige a login.

### Standings Público

- La página carga sin campo de login obligatorio.
- La página muestra datos de clasificación cuando existen.
- La página no muestra correo, teléfono, contraseña, claves ni datos de pago.
- La página no intenta descifrar contraseña para obtener nombre visible.
- Participantes no confirmados o sin nombre público válido se muestran con marcador neutro.

### Base Real Y Base De Pruebas

- `topracing26` contiene datos reales para uso manual.
- `topracing26_test` existe y puede recrearse desde `topracing26`.
- Las pruebas automáticas escriben solo en `topracing26_test`.
- Los datos temporales de pruebas son reconocibles y limpiables.

### Geografía, Venues Y Variants

- Todo circuito real importado tiene un `Venue`.
- Todo `Venue` real tiene al menos una `Variant`.
- Todo `Venue` real apunta a `ProvinceRegion`.
- La cadena territorial llega hasta `PlanetRegion`.
- Las coordenadas importadas permanecen disponibles para validar ubicación.

### Eventos Y Resultados

- Los eventos tienen estado válido.
- Las transiciones rechazan datos incompletos o usuario no autorizado.
- Los resultados de velocidad y carrera se calculan con registros válidos.
- Los puntos se recalculan desde datos oficiales.
- Los resultados publicados no cambian sin trazabilidad.

### Subastas Y Economía

Estos criterios son objetivo hasta que los flujos estén implementados:

- durante una subasta abierta no se publican pujas ni consecuencias provisionales;
- cada usuario mantiene una sola puja vigente por subasta;
- una puja vigente solo puede reemplazarse por otra mayor;
- el cierre produce resultado oficial trazable;
- pagos, retenciones, devoluciones y comisiones quedan reconciliados.

## Evidencia Actual

Observación:

- La versión actual del proyecto es `1.0.4`.
- La configuración local principal apunta a MySQL `topracing26`.
- La documentación operativa define `topracing26_test` para pruebas automáticas.
- El filtro de autenticación permite Standings como página pública.
- La base real fue limpiada de datos automáticos reconocibles y cargada con circuitos reales.

Inferencia:

- La app está alineada con la SRS v2.6 en acceso público a clasificaciones, jerarquía territorial y separación operativa de datos.
- La app todavía no demuestra cumplimiento completo de subastas, eficiencia relativa, pagos, patrocinio, comunidad y verificación reforzada.

Demostración pendiente:

- suite completa de pruebas de navegador sobre páginas públicas y protegidas;
- pruebas de integridad territorial para altas manuales nuevas;
- pruebas de privacidad sobre Standings público;
- pruebas de ciclo completo de evento;
- pruebas futuras de subastas y economía cuando esos flujos se completen.

## Trazabilidad De Fuentes

| Fuente | Uso En Esta SRS |
| --- | --- |
| SRS v2.6 React/Firebase | Alcance funcional objetivo de producto. |
| Código Java actual | Estado implementado observado. |
| `README.md` | Operación local, GlassFish, MySQL y separación de bases. |
| `docs/testing/README.md` | Estrategia de pruebas automáticas con base aislada. |
| Scripts de importación | Alta de venues reales desde My Maps y limpieza de fixtures. |

## Regla De Mantenimiento

Cada cambio que altere acceso, datos públicos, estado de eventos, cálculo de puntos, jerarquía territorial, subastas, pagos o bases de datos debe actualizar:

- el requisito afectado en `08-requirements-catalog.md`;
- los criterios de aceptación de este archivo;
- las decisiones abiertas si el comportamiento todavía no está cerrado;
- las pruebas automáticas que cubren el comportamiento.
