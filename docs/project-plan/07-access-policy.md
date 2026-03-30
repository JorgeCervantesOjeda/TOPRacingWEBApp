# Access Policy

Politica actual deseada:

- un usuario no autenticado solo debe poder entrar a `welcome` y `login`
- el resto de vistas funcionales deben requerir sesion autenticada

Puntos a decidir:

- si `confirmusermail.xhtml` debe seguir siendo publica
- si `resetpassword.xhtml` debe seguir siendo publica
- si algunas consultas publicas adicionales deben permanecer abiertas

Criterio de implementacion:

- la restriccion debe vivir en un punto central
- los controllers y views solo deben complementar esa regla, no duplicarla
