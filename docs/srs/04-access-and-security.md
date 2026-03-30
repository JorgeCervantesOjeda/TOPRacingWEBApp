# Access And Security

Requerimientos actuales:

- un visitante anonimo puede acceder solo a `welcome` y `login`
- una vista protegida solicitada sin sesion debe redirigir a login
- un usuario autenticado puede acceder a vistas protegidas segun el flujo de negocio

Requerimientos pendientes de decision:

- acceso anonimo a confirmacion de correo
- acceso anonimo a reset de password

Restriccion de diseño:

- la politica de acceso debe definirse en un mecanismo central
