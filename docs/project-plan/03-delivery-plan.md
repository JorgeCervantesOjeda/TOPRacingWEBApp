# Delivery Plan

Fase 1. Estabilizacion

- fijar deploy reproducible
- asegurar rutas JSF y acceso anonimo/autenticado
- validar flujos login, create account y confirmacion

Fase 2. Operacion segura

- cerrar acceso anonimo fuera de welcome y login
- revisar reset de password y confirmacion segun politica deseada
- asegurar variables de correo para produccion

Fase 3. Mantenibilidad

- documentar reglas de navegacion
- aislar puntos fragiles de PrimeFaces legacy
- extraer tareas operativas a scripts y docs cortos

Criterio de paso:

- no abrir una fase nueva mientras la anterior siga rompiendo flujos base
