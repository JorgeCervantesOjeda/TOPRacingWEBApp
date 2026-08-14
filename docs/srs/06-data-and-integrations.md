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

## Campos De Cuenta De Participante

En la app Java/MySQL, `Participant.confirmed` representa confirmación operativa y debe derivarse de:

- `email_confirmed`: el correo fue confirmado mediante enlace con clave válida;
- `paypal_usable`: existe una cuenta PayPal verificada que satisface la cuenta de pagos utilizable requerida por TOP Racing en la app Java/MySQL vigente.

La app puede conservar campos de soporte de PayPal como `paypal_payer_id`, `paypal_merchant_id`, `paypal_status` y `paypal_verified_at` para rastro verificable y reconciliación. `confirmed_at` registra cuándo una cuenta queda operativamente confirmada.

Regla vigente para esta implementación: `confirmed = email_confirmed AND paypal_usable`. Una llave de recuperación o queja puede identificar al participante, pero no debe confirmar correo ni habilitar la cuenta.

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
- PayPal Seller Onboarding como proveedor vigente para conectar cuentas de participante antes de habilitar operación normal.
- PayPal como candidato preferente de pagos para autorizar dinero real, capturar cobros, liberar autorizaciones, procesar devoluciones y recibir webhooks de estado cuando se implementen subastas y economía reales.

## Criterios Para PayPal En Esta Implementación

PayPal Seller Onboarding queda seleccionado para activar la cuenta de pagos utilizable de participantes en la app Java/MySQL. PayPal encaja como candidato preferente para subastas de pocas decenas de minutos si se usa con un modelo de autorización y captura diferida:

- una puja nueva de usuario distinto del dueño crea o actualiza una autorización por el importe vigente requerido;
- un aumento de puja no reemplaza la puja vigente hasta que PayPal confirme respaldo suficiente para el nuevo importe;
- al cierre, el sistema captura el importe oficial adjudicado y libera las autorizaciones no ganadoras;
- si una autorización expira, se anula o falla, el sistema debe bloquear la puja dependiente y conservar rastro verificable;
- los webhooks de PayPal deben reconciliar el estado local para evitar depender solo de la respuesta síncrona del navegador.

Para activación de cuenta, TOP Racing debe generar un enlace de onboarding de PayPal con tracking interno, recibir el retorno en `paypalreturn.xhtml` y consultar el estado de seller antes de marcar `paypal_usable = true`. El retorno del navegador no basta por sí solo para activar la cuenta.

Variables de configuración esperadas:

- `TOPRACING_PAYPAL_BASE_URL`;
- `TOPRACING_PAYPAL_ALLOW_LIVE`;
- `TOPRACING_PAYPAL_SANDBOX_MOCK`;
- `TOPRACING_PAYPAL_CLIENT_ID`;
- `TOPRACING_PAYPAL_CLIENT_SECRET`;
- `TOPRACING_PAYPAL_PARTNER_ID`;
- `TOPRACING_PAYPAL_PRODUCT`.

Regla operativa de esta etapa: el sandbox de PayPal es obligatorio por defecto. Si `TOPRACING_PAYPAL_BASE_URL` apunta a `https://api-m.paypal.com`, la app debe rechazar la integración salvo que `TOPRACING_PAYPAL_ALLOW_LIVE=true` esté configurado de forma explícita.

Para desarrollo local sin credenciales PayPal, `TOPRACING_PAYPAL_SANDBOX_MOCK=true` habilita un simulador local que no llama a PayPal y solo prueba navegación, tracking interno, retorno y activación local. Ese modo no demuestra disponibilidad real de PayPal ni sustituye una prueba posterior con credenciales sandbox oficiales.

Límites pendientes para pagos de subasta y economía real:

- confirmar disponibilidad de autorización/captura, moneda y país de la cuenta mercantil;
- definir si se usa PayPal Checkout directo, Braintree u otro producto PayPal;
- definir política para autorizaciones que expiren antes del cierre de la subasta;
- definir conciliación contable, comisiones y manejo de disputas.

## Calidad De Datos

- Los datos reales no deben mezclarse con fixtures automáticos.
- Los datos importados deben ser deduplicados por nombre y coordenadas cuando sea posible.
- Los datos de prueba deben usar nombres, correos o marcas reconocibles para poder limpiarse.
- La eliminación de datos de prueba no debe borrar participantes reales ni sedes reales.

## Dependencias Pendientes

- Sin transporte de correo configurado, la confirmación y recuperación pueden generar claves, pero el envío automático debe fallar con diagnóstico explícito.
- Sin reglas editoriales adicionales, la geocodificación inversa es una ayuda operativa y debe poder corregirse manualmente cuando una ubicación quede mal clasificada.
- Sin proveedor de pagos configurado y verificado, una puja con dinero real no debe quedar vigente para operación económica real.
