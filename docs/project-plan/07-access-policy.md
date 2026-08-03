# Access Policy

Política actual deseada:

- un usuario no autenticado puede entrar a `welcome`, `login` y `standings`
  (`listpointscounts.xhtml`)
- `standings` es una consulta pública de solo lectura respaldada por base de datos
- el resto de vistas funcionales deben requerir sesión autenticada

Puntos a decidir:

- si `confirmusermail.xhtml` debe seguir siendo publica
- si `resetpassword.xhtml` debe seguir siendo publica
- si algunas consultas públicas adicionales deben permanecer abiertas

Criterio de implementación:

- la restriccion debe vivir en un punto central
- los controllers y views solo deben complementar esa regla, no duplicarla
