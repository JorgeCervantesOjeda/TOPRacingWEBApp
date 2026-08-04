# Requisitos No Funcionales

## Plataforma

La app debe:

- desplegarse como WAR en GlassFish 6 o superior; el ambiente local documentado usa GlassFish 7;
- ejecutarse con JDK 11 o superior;
- usar JSF/PrimeFaces para la interfaz de esta implementación;
- usar Hibernate para persistencia;
- usar MySQL como base de datos local principal;
- mantener rutas JSF consistentes bajo `/faces`.

## Datos Reales Y Pruebas

- La base `topracing26` debe reservarse para datos reales y pruebas manuales.
- La base `topracing26_test` debe reservarse para pruebas automáticas.
- Las pruebas automáticas no deben crear, modificar ni borrar datos en `topracing26`.
- La preparación de pruebas puede recrear `topracing26_test` desde una copia de la base real y después insertar datos temporales.
- Los datos de prueba deben ser reconocibles y limpiables.

## Confiabilidad

- Las páginas públicas deben responder sin requerir sesión.
- Las páginas protegidas deben redirigir a login sin error visible.
- Los cálculos de puntos y clasificaciones deben ser reproducibles para el mismo conjunto de datos.
- Los fallos de base de datos deben mostrarse como error operacional y registrarse para diagnóstico.

## Seguridad Y Privacidad

- No se deben exponer datos sensibles en páginas públicas.
- Las contraseñas y claves deben tratarse como datos reservados.
- Los flujos de confirmación y recuperación deben validar claves antes de modificar cuenta.
- No debe existir fallback silencioso hacia credenciales, bases o usuarios alternativos.

## Observabilidad

- Los fallbacks operativos deben registrarse.
- Los errores de importación, despliegue, prueba y conexión deben dejar mensaje accionable.
- Las pruebas automáticas deben fallar si no pueden usar la base aislada esperada.

## Mantenibilidad

- La documentación debe distinguir requisito implementado, parcial y objetivo.
- Los scripts operativos deben ser idempotentes cuando sea posible.
- Los cambios de reglas de dominio deben quedar reflejados en SRS y pruebas.

## Usabilidad

- Las acciones lentas o de estado deben dar retroalimentación visible.
- Los mensajes de error deben depender de la condición real detectada, no de temporizadores.
- La consulta pública debe ser legible sin requerir conocimiento técnico del modelo de datos.
