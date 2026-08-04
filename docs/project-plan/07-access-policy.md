# Política De Acceso

Política vigente:

- un usuario no autenticado puede entrar a `welcome`, `login`, `standings`, confirmación de correo y recuperación de contraseña;
- `standings` corresponde a `listpointscounts.xhtml`;
- `standings` es una consulta pública de solo lectura respaldada por base de datos;
- `confirmusermail.xhtml` y `resetpassword.xhtml` son públicas porque forman parte de flujos iniciados antes de tener sesión válida;
- el resto de vistas funcionales deben requerir sesión autenticada.

Restricciones de datos públicos:

- Standings puede mostrar datos deportivos y nombre público del participante;
- Standings no debe mostrar correo, teléfono, contraseña, claves, datos de pago ni otros datos sensibles;
- los datos de participantes no confirmados o sin nombre público válido deben mostrarse con marcador neutro.

Puntos a decidir:

- si algunas consultas públicas adicionales deben permanecer abiertas;
- si el nombre público del participante será nombre legal, alias deportivo o ambos;
- si `Variant` debe aparecer como nivel visible en todas las consultas públicas.

Criterio de implementación:

- la restricción debe vivir en un punto central;
- los controladores y vistas solo deben complementar esa regla, no duplicarla;
- no debe existir fallback silencioso hacia datos de prueba cuando falla la base real.
