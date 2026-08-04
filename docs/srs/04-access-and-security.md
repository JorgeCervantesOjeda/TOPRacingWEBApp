# Acceso Y Seguridad

## Política Central

La política de acceso debe definirse en un mecanismo central y aplicarse antes de entregar una vista JSF protegida.

## Páginas Públicas De Esta Versión

Un visitante anónimo puede acceder a:

- `welcome.xhtml`;
- `login.xhtml`;
- `resetpassword.xhtml`;
- `confirmusermail.xhtml`;
- `listpointscounts.xhtml` como Standings público de solo lectura.

Toda vista no incluida en la lista pública debe requerir sesión válida y redirigir a login si se solicita sin autenticación.

## Reglas De Sesión

- El login debe validar credenciales contra participantes existentes.
- Un participante no confirmado no debe quedar autenticado para operación normal.
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

## Verificación Reforzada

Las operaciones siguientes deben requerir verificación reforzada cuando se implementen como flujos completos:

- promover eventos;
- inscribir vehículos;
- pujar en subastas de vehículos;
- operar funciones administrativas críticas.

Estado: `Objetivo`.

## Requisitos De Auditoría

- Los rechazos de acceso y operaciones inválidas deben dejar evidencia diagnóstica suficiente.
- Las operaciones económicas y de subasta deben ser trazables.
- Los errores de acceso a base de datos no deben degradar silenciosamente a datos de prueba o datos ficticios.
