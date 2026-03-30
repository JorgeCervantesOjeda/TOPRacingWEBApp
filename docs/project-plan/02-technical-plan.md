# Technical Plan

La base tecnica actual ya es viable:

- Jakarta Faces / CDI
- PrimeFaces Jakarta
- Hibernate + MySQL
- WAR Maven
- GlassFish 6+

Lineas tecnicas activas:

- mantener compatibilidad Jakarta real en vistas, beans y despliegue
- centralizar control de acceso en filtros o puntos equivalentes
- conservar la navegacion en controller + view, sin logica duplicada en XHTML
- reducir acoplamiento entre sesion HTTP y reglas de negocio

Refactor permitido:

- pequeno, dirigido y verificable

Refactor no prioritario:

- reescritura amplia de vistas
- migracion de arquitectura solo por preferencia estetica
