# Decisiones Abiertas

Esta lista auxiliar contiene decisiones que siguen abiertas después de comparar la SRS oficial completa con la versión actual de la app Java/MySQL.

## Producto

1. Definir el modelo formal de verificación reforzada, aceptación de términos y exclusión.
2. Decidir si `Variant` debe aparecer como nivel territorial visible en todas las consultas o solo como nivel interno para eventos y sedes.
3. Definir el nombre público preferido del participante en Standings: nombre legal, alias deportivo o ambos.
4. Definir qué historial público mínimo debe mostrarse para eventos publicados.
5. Definir reglas editoriales para corregir geocodificación de sedes importadas.

## Eventos Y Clasificaciones

1. Confirmar la correspondencia exacta entre estados actuales de `Regatta` y estados normativos v2.6.
2. Definir criterios de inmutabilidad para resultados publicados en esta app.
3. Definir cuándo recalcular puntos automáticamente y cuándo exigir acción explícita.
4. Definir si las clasificaciones de eficiencia deben bloquearse totalmente hasta cerrar subasta de vehículos.

## Economía Y Subastas

1. Seleccionar proveedor o mecanismo para pagos, retenciones y devoluciones.
2. Definir reglas legales y operativas para transferencia obligatoria de vehículos.
3. Definir comisiones aplicables por participante, postor, comprador, patrocinador y aportante.
4. Definir ventanas y evidencias para disputas de entrega.

## Comunidad

1. Definir alcance inicial de foro y novedades.
2. Definir moderación, sanciones, reporte de contenido y visibilidad pública de decisiones.
3. Definir reglas de privacidad para protestas de seguridad.

## Técnica

1. Decidir si la configuración de base debe pasar de archivo Hibernate fijo a variables de entorno en runtime.
2. Definir ambiente desechable completo para pruebas de navegador.
3. Definir política de backups antes de importaciones masivas.
4. Definir estrategia para migraciones de esquema versionadas.
