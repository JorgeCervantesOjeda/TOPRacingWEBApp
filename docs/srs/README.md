# SRS TOPRacingWEBApp

Esta carpeta contiene la especificación de requisitos de software de TOP Racing.

La SRS oficial de este proyecto es el documento completo:

- [SRS_TOP_Racing_v2_6_tecnologia_abierta_obligatoria.tex](./SRS_TOP_Racing_v2_6_tecnologia_abierta_obligatoria.tex)

Ese archivo parte de la SRS fuente completa ubicada originalmente en `C:\Users\usuario\ownCloud2\TOP-Racing React Firebase` y conserva el documento detallado dentro de este proyecto. Las correcciones normativas aprobadas para TOPRacingWEBApp deben aplicarse sobre ese documento completo, no mediante resúmenes ni adaptaciones parciales.

Los archivos Markdown de esta carpeta son auxiliares operativos para ubicar estado de implementación, verificaciones y decisiones pendientes de la app Java/MySQL. No reemplazan ni reducen la SRS oficial.

## Alcance Normativo

La SRS v2.6 completa es la referencia normativa funcional. Los archivos auxiliares pueden distinguir estado observado de la app Java actual con estas etiquetas:

- `Implementado`: existe en esta app y cuenta como requisito vigente.
- `Parcial`: existe una parte, pero no cubre todo el requisito de producto.
- `Objetivo`: pertenece al producto TOP Racing completo, pero todavía no queda implementado en esta app.
- `Fuera de la versión actual`: no debe bloquear mantenimiento de esta versión, salvo que una historia lo active.

Cuando haya conflicto entre la SRS oficial completa y el código observado, se debe detener el trabajo funcional o de datos, registrar la inconsistencia y resolverla antes de continuar.

## Orden Recomendado De Lectura

1. [SRS_TOP_Racing_v2_6_tecnologia_abierta_obligatoria.tex](./SRS_TOP_Racing_v2_6_tecnologia_abierta_obligatoria.tex)
2. [00-system-purpose.md](./00-system-purpose.md)
3. [01-stakeholders-and-users.md](./01-stakeholders-and-users.md)
4. [02-scope.md](./02-scope.md)
5. [03-functional-areas.md](./03-functional-areas.md)
6. [04-access-and-security.md](./04-access-and-security.md)
7. [05-non-functional-requirements.md](./05-non-functional-requirements.md)
8. [06-data-and-integrations.md](./06-data-and-integrations.md)
9. [07-open-decisions.md](./07-open-decisions.md)
10. [08-requirements-catalog.md](./08-requirements-catalog.md)
11. [09-verification-and-traceability.md](./09-verification-and-traceability.md)

## Reglas De Redacción

- Especificar comportamiento esperado antes de mencionar detalles técnicos.
- Separar observación, supuesto, inferencia y requisito cuando el soporte no sea directo.
- No presentar como implementado un requisito que solo existe en la SRS de producto.
- Mantener la tecnología real de esta app: JSF/PrimeFaces, Hibernate, MySQL y GlassFish.
- No introducir restricciones React, Firebase o Firestore como obligatorias para esta implementación.
