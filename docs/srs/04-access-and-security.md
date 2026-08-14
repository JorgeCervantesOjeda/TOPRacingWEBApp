# Acceso Y Seguridad

## Política Central

La política de acceso debe definirse en un mecanismo central y aplicarse antes de entregar una vista JSF protegida.

## Páginas Públicas De Esta Versión

Un visitante anónimo puede acceder sin clave especial a:

- `welcome.xhtml`;
- `login.xhtml`;
- `editparticipant.xhtml` para alta inicial de participante;
- `listpointscounts.xhtml` como Standings público de solo lectura.

Un visitante anónimo puede acceder por enlace con clave validada a:

- `resetpassword.xhtml`;
- `confirmusermail.xhtml`;
- `paypalreturn.xhtml`;
- `complaint.xhtml`;
- `complaintbuyer.xhtml`;
- `complaintseller.xhtml`.

Las páginas públicas por enlace con clave no convierten su contenido en información pública general. Si la clave falta, es inválida, expiró o no corresponde al contexto solicitado, la página no debe mostrar datos privados ni ejecutar acciones de dominio.

Toda vista no incluida en la lista pública debe requerir sesión válida y redirigir a login si se solicita sin autenticación.

## Reglas De Sesión

- El login debe validar credenciales contra participantes existentes.
- Un participante no confirmado operativamente no debe quedar autenticado para operación normal.
- La confirmación operativa de participante requiere correo confirmado y cuenta de pagos utilizable.
- La confirmación de correo por sí sola no habilita operación normal.
- Un participante sin cuenta de pagos utilizable debe recibir rechazo explícito y no debe incrementar métricas de usuarios activos.
- El logout debe invalidar la sesión de la aplicación.
- Las vistas protegidas no deben depender solo de controles visuales; también deben validar acceso en servidor.

## Datos Públicos Permitidos

Standings público puede mostrar:

- clasificación, posición o puntuación;
- nombre público o nombre deportivo del participante;
- periodo, modalidad, evento, venue o variante relacionados;
- datos agregados necesarios para entender la clasificación.

Standings público no debe mostrar:

- contraseña o material criptográfico;
- correo electrónico;
- teléfono;
- claves de confirmación o recuperación;
- datos de pago;
- datos personales que no sean necesarios para la clasificación.

Cuando un participante no esté confirmado o no tenga nombre público válido, el sistema debe mostrar un marcador neutro y no debe intentar descifrar datos sensibles innecesarios.

## Reglas De Reserva De Subastas

Mientras una subasta de vehículos esté abierta, el sistema no debe publicar:

- pujas de terceros;
- conteos de pujas;
- precios provisionales;
- adjudicatarios provisionales;
- clasificación provisional de eficiencia;
- puntos o premios derivados de pujas no cerradas.

Cada postor autorizado solo puede consultar su propia puja vigente cuando el flujo esté implementado.

Estado: `Objetivo`.

## Reglas De Dinero Real En Pujas

Toda puja vigente y todo aumento de puja deben quedar respaldados por dinero real antes de sustituir el estado vigente del dominio.

Para usuarios distintos del dueño del vehículo, el respaldo debe ser una autorización, retención, cargo u operación económica equivalente confirmada por el proveedor de pagos. Si el proveedor rechaza, cancela, expira o no confirma la operación, la puja nueva no queda vigente y la puja vigente anterior no debe modificarse.

La puja del dueño sobre su propio vehículo está exenta de retención del principal cuando la posible autoadjudicación no genera pago principal a sí mismo. Esa excepción no elimina comisiones aplicables ni otros importes reales que deban autorizarse o cobrarse.

PayPal Seller Onboarding es el proveedor vigente para conectar la cuenta de pagos utilizable de participante en la app Java/MySQL. Para autorizaciones, capturas, liberaciones y webhooks de subastas o economía real, PayPal sigue siendo candidato preferente, pero la SRS no lo declara proveedor único hasta cerrar la decisión operativa y contractual de pagos.

## Verificación Reforzada

Las operaciones siguientes deben requerir verificación reforzada cuando se implementen como flujos completos:

- promover eventos;
- inscribir vehículos;
- pujar en subastas de vehículos;
- operar funciones administrativas críticas.

Estado: `Objetivo`.

## Requisitos De Auditoría

- Los rechazos de acceso y operaciones inválidas deben dejar evidencia diagnóstica suficiente.
- Las operaciones económicas y de subasta deben conservar rastro verificable.
- Los errores de acceso a base de datos no deben degradar silenciosamente a datos de prueba o datos ficticios.
