<!-- docs/srs/10-consistency-review-2026-08-04.md - Documents observed SRS/app consistency findings. -->

# Revisión De Consistencia SRS-App

Fecha de revisión: 2026-08-04.

Nota de vigencia: esta revisión conserva el contexto histórico de SRS v2.6 observado en su fecha. La fuente normativa vigente del proyecto es `docs/srs/SRS_TOP_Racing_v2_7.tex`.

## Alcance

Se contrastó la SRS oficial `docs/srs/SRS_TOP_Racing_v2_6_tecnologia_abierta_obligatoria.tex`, los auxiliares `docs/srs/*.md`, y evidencia de implementación en Java/JSF, pruebas y scripts.

La revisión no modifica comportamiento funcional ni datos.

## Hallazgos Relevantes

### CON-001: Páginas anónimas adicionales frente a la lista pública auxiliar

Observación:

- `docs/srs/04-access-and-security.md:17` establece que toda vista no incluida en la lista pública debe requerir sesión válida.
- La lista pública auxiliar incluye `welcome.xhtml`, `login.xhtml`, `resetpassword.xhtml`, `confirmusermail.xhtml` y `listpointscounts.xhtml`.
- `src/main/java/Web/AuthenticationPageFilter.java:79` permite además `editparticipant.xhtml`, `complaint.xhtml`, `complaintbuyer.xhtml` y `complaintseller.xhtml`.
- `src/test/java/integration/LocalAppPublicEntryPointsIT.java:57` prueba explícitamente que esos puntos externos respondan sin sesión.

Soporte:

- `editparticipant.xhtml` puede estar parcialmente soportado por el alcance de alta de participantes en `docs/srs/02-scope.md`.
- Las páginas de queja no aparecen como públicas en `docs/srs/04-access-and-security.md` ni en `PUB-001`.

Inferencia:

- Hay inconsistencia documental/funcional para las páginas de queja públicas, salvo que exista una excepción aprobada no registrada.
- `editparticipant.xhtml` requiere aclaración de SRS auxiliar: alta pública de participante permitida, o vista protegida con flujo público separado.

Acción requerida:

- Actualizar SRS auxiliar si esas páginas deben ser públicas, incluyendo requisitos de privacidad, validación de clave y alcance anónimo.
- O restringir `complaint*.xhtml` en el filtro y ajustar pruebas.

### CON-002: Recuperación de contraseña y quejas reutilizan una búsqueda que confirma correo

Estado posterior: `Resuelto en implementación`. La lectura por clave queda separada de la confirmación explícita de correo y `confirmed` queda reservado para cuenta operativa con correo confirmado y PayPal utilizable.

Observación:

- `src/main/java/Model/ModelBean.java:3989` busca participante por `emailKey`.
- `src/main/java/Model/ModelBean.java:4005` marca `confirmed = true` para cualquier participante encontrado.
- `src/main/java/View/ConfirmParticipantMailBean.java:55` usa esa búsqueda para confirmar correo.
- `src/main/java/View/ResetPasswordBean.java:86` usa la misma búsqueda antes de resetear contraseña.
- Las páginas de queja también usan claves de correo en flujos públicos, según `src/test/java/integration/LocalAppExternalLinkFlowsIT.java`.

Soporte SRS:

- `docs/srs/09-verification-and-traceability.md:9` exige que confirmación valide clave antes de confirmar.
- `docs/srs/09-verification-and-traceability.md:10` exige que recuperación valide clave antes de cambiar contraseña.
- La SRS separa confirmación de correo y recuperación de contraseña como capacidades distintas.

Inferencia:

- Un enlace válido de recuperación de contraseña puede confirmar el correo como efecto colateral.
- Cualquier otro flujo que llame `getParticipantByEMailKey` puede producir el mismo efecto.

Acción requerida:

- Separar lectura por clave de la mutación de confirmación.
- Crear operaciones explícitas: validar clave, confirmar correo y consumir clave de recuperación.
- Agregar pruebas para que un reset válido no confirme automáticamente a un usuario no confirmado salvo que la SRS lo autorice.

### CON-003: Standings público expone nombre de participantes no confirmados

Estado posterior: `Resuelto en implementación focalizada`. El nombre de participantes sin confirmación operativa se sustituye por marcador neutro; queda recomendable una prueba de navegador específica de Standings público.

Observación:

- `docs/srs/04-access-and-security.md:44` exige marcador neutro si el participante no está confirmado o no tiene nombre público válido.
- `src/main/java/Model/ModelBean.java:4027` consulta `confirmed`, `namesFamily` y `namesGiven`.
- `src/main/java/Model/ModelBean.java:4039` solo antepone `*` cuando no está confirmado, pero sigue devolviendo apellidos y nombres.
- `src/main/webapp/listpointscounts.xhtml` muestra ese valor en la columna de participante.

Inferencia:

- Si existe un `Pointscount` asociado a un participante no confirmado, Standings revela nombre y apellidos con prefijo `*`, no un marcador neutro.

Acción requerida:

- Cambiar `getParticipantFullNameById` para devolver marcador neutro cuando `confirmed` sea falso o el nombre público no sea válido.
- Añadir prueba que construya un `Pointscount` de usuario no confirmado y verifique que Standings no revela nombres reales.

### CON-004: Sustitución de puja no está restringida al alza

Observación:

- La SRS oficial establece en `BR-AUC-003`, `RF-AUC-017` y `DEC-163` que una puja vigente solo puede sustituirse por otra estrictamente mayor.
- `src/main/java/Tables/Bid.hbm.xml:7` usa clave compuesta por participante e inscripción, lo que soporta una sola puja por usuario/vehículo.
- `src/main/java/Controller/Controller.java:538` asigna directamente `incomingBid.getAmmount()` a la puja saneada si el usuario puede editarla.
- No se observó validación que rechace una puja menor o igual a la vigente.

Límite:

- `AUC-004` está marcado como `Objetivo` en `docs/srs/08-requirements-catalog.md:85`, por lo que no necesariamente bloquea mantenimiento de esta versión si el flujo de subasta sigue fuera de alcance.

Inferencia:

- Si el flujo actual de pujas se trata como implementación de subasta de vehículos, no cumple sustitución únicamente al alza.

Acción requerida:

- Antes de activar subasta real, validar contra la puja persistida y rechazar importes menores o iguales sin modificar la vigente.
- Agregar prueba funcional con secuencia menor, igual y mayor.

### CON-005: Fórmula de P/C no coincide con la SRS oficial

Observación:

- Al momento de esta revisión, `docs/srs/SRS_TOP_Racing_v2_6_tecnologia_abierta_obligatoria.tex:2174` definía P/C como cociente entre pozo económico oficial y costo por participante.
- `docs/srs/SRS_TOP_Racing_v2_6_tecnologia_abierta_obligatoria.tex:5846` define costo por participante como inscripción, renta imputable y pesaje obligatorio imputable.
- `src/main/java/Model/ModelBean.java:2495` calcula costo efectivo como inscripción más renta por participante.
- `src/main/java/Model/ModelBean.java:2502` devuelve `totalPrize * numOfActiveParticipants / effectiveEntryCost`.
- `src/test/java/Model/ModelBeanPriorityPointsTest.java:13` espera ese comportamiento multiplicado por participantes activos.

Inferencia:

- La implementación calcula un valor proporcional a `pozo * participantes / costo_por_participante`, no el cociente normativo `pozo / costo_por_participante`.
- El catálogo auxiliar `RES-007` describe dependencia con participantes activos, pero no resuelve el conflicto con la SRS oficial, que es la fuente normativa.

Acción requerida:

- Decisión posterior del usuario: actualizar la SRS oficial para incluir el factor de participantes activos y alinear la norma con la app actual.
- Estado posterior: resuelto por documentación en `docs/srs/SRS_TOP_Racing_v2_6_tecnologia_abierta_obligatoria.tex`, `docs/srs/08-requirements-catalog.md` y auxiliares relacionados.

## Consistencias Observadas

- La cadena territorial `Variant -> Venue -> ProvinceRegion -> Province -> CountryRegion -> Country -> PlanetRegion` está representada en SRS auxiliar y en scripts de importación.
- Hay una prueba e2e que recorre la creación manual de la cadena geográfica completa.
- Los estados internos de evento cubren los siete estados normativos en orden equivalente, aunque con etiquetas internas en inglés.
- Standings público está permitido por el filtro y tiene prueba de entrada pública básica.

## Límites De La Revisión

- No se ejecutó una auditoría completa de base real `topracing26`.
- No se verificó visualmente la app en navegador.
- No se revisó cada requisito objetivo de subastas, pagos, comunidad y verificación reforzada, porque la propia SRS auxiliar los marca como fuera del cumplimiento actual completo.
