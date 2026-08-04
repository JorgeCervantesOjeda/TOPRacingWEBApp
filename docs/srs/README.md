# SRS TOPRacingWEBApp

Esta carpeta contiene la especificación de requisitos de software de la implementación Java/MySQL de TOP Racing.

La SRS local usa tres fuentes:

- la SRS de producto `SRS_TOP_Racing_v2_6_tecnologia_abierta_obligatoria.tex`, ubicada en `C:\Users\usuario\ownCloud2\TOP-Racing React Firebase`;
- el comportamiento observado de esta versión de `TOPRacingWEBApp`;
- las decisiones operativas recientes sobre base de datos real, base de datos de pruebas y acceso público a Standings.

## Alcance Normativo

La SRS v2.6 es la referencia funcional de producto. Esta carpeta adapta esa referencia a la app Java actual y distingue explícitamente:

- `Implementado`: existe en esta app y cuenta como requisito vigente.
- `Parcial`: existe una parte, pero no cubre todo el requisito de producto.
- `Objetivo`: pertenece al producto TOP Racing completo, pero todavía no queda implementado en esta app.
- `Fuera de la versión actual`: no debe bloquear mantenimiento de esta versión, salvo que una historia lo active.

Cuando haya conflicto entre esta SRS local y el código observado, el conflicto debe registrarse en `07-open-decisions.md` o corregirse en la documentación antes de usarla como criterio de aceptación.

## Orden Recomendado

1. [00-system-purpose.md](./00-system-purpose.md)
2. [01-stakeholders-and-users.md](./01-stakeholders-and-users.md)
3. [02-scope.md](./02-scope.md)
4. [03-functional-areas.md](./03-functional-areas.md)
5. [04-access-and-security.md](./04-access-and-security.md)
6. [05-non-functional-requirements.md](./05-non-functional-requirements.md)
7. [06-data-and-integrations.md](./06-data-and-integrations.md)
8. [07-open-decisions.md](./07-open-decisions.md)
9. [08-requirements-catalog.md](./08-requirements-catalog.md)
10. [09-verification-and-traceability.md](./09-verification-and-traceability.md)

## Reglas De Redacción

- Especificar comportamiento esperado antes de mencionar detalles técnicos.
- Separar observación, supuesto, inferencia y requisito cuando el soporte no sea directo.
- No presentar como implementado un requisito que solo existe en la SRS de producto.
- Mantener la tecnología real de esta app: JSF/PrimeFaces, Hibernate, MySQL y GlassFish.
- No introducir restricciones React, Firebase o Firestore como obligatorias para esta implementación.
