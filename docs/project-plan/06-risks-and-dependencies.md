# Risks And Dependencies

Dependencias externas principales:

- GlassFish 6+ compatible con Jakarta
- MySQL con esquema utilizable
- variables de correo OAuth

Riesgos tecnicos principales:

- diferencias entre patrones JSF legacy y Jakarta actual
- navegacion mezclada entre `/faces/*` y `*.xhtml`
- dependencias de PrimeFaces en flujos antiguos con confirm dialogs
- reglas de acceso dispersas entre controller, vistas y sesion

Mitigacion preferida:

- arreglos pequenos, verificables y centrados en flujo
- documentar patrones fragiles al momento de encontrarlos
- no abrir refactors amplios mientras el flujo base siga siendo delicado
