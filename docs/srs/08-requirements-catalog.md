# Catálogo De Requisitos

Este catálogo auxiliar ubica requisitos funcionales y no funcionales relevantes para esta app. No sustituye ni resume de forma normativa la SRS oficial completa; el estado indica el cumplimiento observado en la implementación Java actual, no el alcance total del producto.

## IAM: Identidad, Acceso Y Cuenta

| ID | Requisito | Estado |
| --- | --- | --- |
| IAM-001 | El sistema debe permitir login con participante registrado. | Implementado |
| IAM-002 | El sistema debe impedir operación autenticada a participantes no confirmados. | Implementado |
| IAM-003 | El sistema debe permitir confirmación de correo mediante clave pública validada. | Parcial |
| IAM-004 | El sistema debe permitir recuperación de contraseña mediante clave pública validada. | Parcial |
| IAM-005 | El sistema debe distinguir sesión autenticada, correo confirmado, identidad verificada, verificación reforzada, términos aceptados, exclusión global y bloqueos locales por promotor. | Parcial |
| IAM-006 | El sistema debe requerir verificación reforzada para operaciones de mayor riesgo. | Objetivo |
| IAM-007 | El sistema debe registrar rechazos de acceso relevantes. | Parcial |
| IAM-008 | El sistema debe tratar `confirmed` como cuenta operativa derivada de correo confirmado y cuenta de pagos utilizable; en esta implementación `paypal_usable` satisface esa cuenta mediante PayPal Seller Onboarding. | Implementado |
| IAM-009 | El sistema debe permitir iniciar PayPal Seller Onboarding y confirmar `paypal_usable` solo tras retorno validado y consulta de estado PayPal. | Parcial |
| IAM-010 | El sistema debe conservar historial verificable de creación y resolución de exclusiones globales, morosidad local y bloqueos locales por promotor. | Implementado |

## PUB: Publicación Y Reserva

| ID | Requisito | Estado |
| --- | --- | --- |
| PUB-001 | El visitante anónimo debe poder acceder a bienvenida, login, alta inicial de participante, Standings y enlaces con clave validada para confirmación, recuperación y quejas. | Implementado |
| PUB-002 | Toda vista no pública debe requerir sesión válida. | Implementado |
| PUB-003 | Standings público no debe exponer datos sensibles. | Parcial |
| PUB-004 | Los eventos publicados deben conservar historial consultable. | Objetivo |
| PUB-005 | La información reservada de subastas no debe publicarse antes del cierre. | Objetivo |

## GEO: Geografía, Sedes Y Variantes

| ID | Requisito | Estado |
| --- | --- | --- |
| GEO-001 | El sistema debe conservar ocho niveles territoriales: planeta, región planetaria, país, región de país, estado/provincia, región estatal/provincial, venue y variant. | Implementado |
| GEO-002 | Cada circuito real debe darse de alta como `Venue` con al menos una `Variant`. | Implementado |
| GEO-003 | Cada `Venue` real debe asociarse a una región estatal o provincial adecuada según ubicación. | Implementado |
| GEO-004 | Las altas futuras de venues deben preservar cadena territorial completa. | Parcial |
| GEO-005 | La app debe permitir corregir manualmente clasificaciones geográficas importadas. | Objetivo |

## EVT: Eventos

| ID | Requisito | Estado |
| --- | --- | --- |
| EVT-001 | El sistema debe gestionar eventos competitivos con estado oficial. | Parcial |
| EVT-002 | Los estados normativos deben ser `creado`, `inscripciones`, `velocidad`, `carrera`, `subasta`, `publicado` y `cancelado`. | Parcial |
| EVT-003 | Toda transición de estado debe validar precondiciones, permisos y reglas de negocio. | Parcial |
| EVT-004 | Un evento publicado debe bloquear capturas que alteren resultados oficiales sin rastro verificable. | Objetivo |
| EVT-005 | Un evento cancelado debe conservar historial y excluir resultados oficiales. | Objetivo |

## REG: Inscripciones

| ID | Requisito | Estado |
| --- | --- | --- |
| REG-001 | El sistema debe permitir registrar participantes en eventos abiertos. | Parcial |
| REG-002 | La inscripción debe asociar evento, dueño, vehículo y piloto cuando aplique. | Parcial |
| REG-003 | Una inscripción cancelada debe conservarse como historial y excluirse de resultados, puntos y subastas. | Parcial |
| REG-004 | Solo inscripciones computables deben participar en resultados oficiales. | Parcial |

## VEH: Vehículos Y Tecnología Abierta

| ID | Requisito | Estado |
| --- | --- | --- |
| VEH-001 | El sistema debe registrar vehículos asociados a dueño. | Parcial |
| VEH-002 | El vehículo competitivo puede ser motorizado, no motorizado, artesanal, reutilizado o de bajo costo si cumple reglas publicadas. | Objetivo |
| VEH-003 | Todo vehículo competitivo debe cumplir la condición transversal de tecnología abierta. | Objetivo |
| VEH-004 | La apertura tecnológica no implica acceso físico irrestricto al evento. | Objetivo |
| VEH-005 | Un vehículo sometido a subasta debe quedar bajo control del evento antes de iniciar la subasta. | Objetivo |

## RES: Resultados, Puntos Y Clasificaciones

| ID | Requisito | Estado |
| --- | --- | --- |
| RES-001 | El sistema debe capturar resultados de velocidad y carrera. | Parcial |
| RES-002 | El sistema debe calcular puntos y Standings. | Parcial |
| RES-003 | Standings debe ser consultable públicamente. | Implementado |
| RES-004 | Las clasificaciones deben derivarse de reglas, niveles territoriales, niveles temporales y modalidad. | Parcial |
| RES-005 | La estructura objetivo comprende seis niveles temporales, ocho territoriales y cuatro modalidades. | Objetivo |
| RES-006 | La matriz de pesos debe publicarse desde inscripciones y permanecer visible. | Objetivo |
| RES-007 | El puntaje de prioridad de eventos debe derivarse de la bolsa total de premios, el costo efectivo de inscripción y el número de participantes activos del evento. | Parcial |

## AUC: Subastas

| ID | Requisito | Estado |
| --- | --- | --- |
| AUC-001 | La subasta de acceso corresponde a espectadores, no a competidores. | Objetivo |
| AUC-002 | La subasta de vehículos debe ser sellada. | Objetivo |
| AUC-003 | Antes del cierre no deben publicarse pujas, conteos, posiciones, precios, adjudicatarios, eficiencia ni premios provisionales. | Objetivo |
| AUC-004 | Cada usuario debe tener una sola puja vigente por vehículo y solo puede sustituirla al alza con respaldo de dinero real confirmado antes de modificar la vigente. | Objetivo |
| AUC-005 | El dueño puede pujar por su propio vehículo. | Objetivo |
| AUC-006 | La subasta debe producir resultado oficial con rastro verificable y reglas de entrega/disputa. | Objetivo |

## EFF: Eficiencia Relativa Al Mercado

| ID | Requisito | Estado |
| --- | --- | --- |
| EFF-001 | La eficiencia debe comparar precio oficial de subasta contra precio de tendencia por rendimiento observado. | Objetivo |
| EFF-002 | El rendimiento observado debe fijarse antes de abrir subasta. | Objetivo |
| EFF-003 | La tendencia debe calcularse después del cierre con datos válidos del evento. | Objetivo |
| EFF-004 | La eficiencia no debe publicarse como provisional durante la subasta. | Objetivo |

## ECO: Economía, Premios Y Pagos

| ID | Requisito | Estado |
| --- | --- | --- |
| ECO-001 | El sistema debe modelar pozo económico oficial del evento. | Objetivo |
| ECO-002 | El sistema debe gestionar aportaciones directas y contribuciones a participantes. | Objetivo |
| ECO-003 | El sistema debe calcular premios de eficiencia y terminación. | Objetivo |
| ECO-004 | El sistema debe gestionar pagos, retenciones, devoluciones y comisiones con rastro verificable. | Objetivo |
| ECO-005 | Las cantidades de terceros deben publicarse sin identidad cuando la regla lo exija. | Objetivo |
| ECO-006 | PayPal Seller Onboarding es el proveedor vigente para activar cuenta de pagos utilizable; PayPal sigue como candidato preferente para autorizaciones, capturas, liberaciones, devoluciones y webhooks de subastas y economía real, sin quedar cerrado como proveedor único hasta decisión operativa. | Parcial |

## COM: Comunidad, Seguridad Y Moderación

| ID | Requisito | Estado |
| --- | --- | --- |
| COM-001 | El sistema debe permitir protestas u objeciones de seguridad dentro de ventanas publicadas. | Objetivo |
| COM-002 | La identidad de quien protesta debe mantenerse reservada en consulta pública cuando aplique. | Objetivo |
| COM-003 | El sistema debe permitir foro y canal de novedades con moderación administrativa. | Objetivo |
| COM-004 | Las decisiones de moderación y seguridad deben conservar rastro verificable. | Objetivo |

## INT: Integraciones Y Operación

| ID | Requisito | Estado |
| --- | --- | --- |
| INT-001 | La app debe persistir en MySQL. | Implementado |
| INT-002 | La app debe desplegarse como WAR en GlassFish. | Implementado |
| INT-003 | La app debe poder importar circuitos reales desde CSV de My Maps. | Implementado |
| INT-004 | El correo saliente debe usarse para confirmación, recuperación y notificaciones cuando haya transporte SMTP u OAuth configurado. | Implementado |
| INT-005 | Las pruebas automáticas deben usar base aislada. | Implementado |
| INT-006 | La integración PayPal debe configurarse por variables de entorno o propiedades equivalentes, sin credenciales en código fuente. | Implementado |

## NFR: Requisitos No Funcionales

| ID | Requisito | Estado |
| --- | --- | --- |
| NFR-001 | Las páginas públicas deben responder sin sesión. | Implementado |
| NFR-002 | Las vistas protegidas deben redirigir a login sin error. | Implementado |
| NFR-003 | Los fallos de base deben registrarse sin fallback silencioso. | Parcial |
| NFR-004 | Los datos reales no deben mezclarse con fixtures automáticos. | Implementado |
| NFR-005 | Los scripts operativos deben ser repetibles e idempotentes cuando sea posible. | Parcial |
| NFR-006 | Las acciones lentas o críticas deben mostrar retroalimentación visible. | Parcial |
| NFR-007 | Los cambios de reglas deben actualizar documentación y pruebas. | Parcial |
