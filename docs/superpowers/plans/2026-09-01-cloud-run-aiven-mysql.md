# Plan de despliegue Cloud Run + Aiven MySQL

## Cambio previsto

Preparar la aplicación Jakarta Faces/PrimeFaces empaquetada como WAR para ejecutarse en Google Cloud Run con base de datos Aiven for MySQL, manteniendo compatibilidad con el entorno local de MySQL y GlassFish.

## Causa

El proveedor elegido combina HTTPS automático, arranque gratuito o de bajo costo y cobro por uso al crecer. Aiven ya tiene un servicio MySQL en ejecución y Google Cloud ya tiene el proyecto `top-racing-77628`.

## Alcance

- Añadir artefactos de contenedor para construir y ejecutar el WAR en GlassFish dentro de Cloud Run mediante despliegue `asadmin` durante el build de la imagen.
- Documentar la configuración operativa de Cloud Run, secretos y Aiven MySQL.
- Ajustar la normalización de JDBC para no introducir parámetros locales inseguros en conexiones externas con TLS.
- Añadir pruebas unitarias de la configuración JDBC.
- Incrementar la versión Maven antes de una publicación potencial.

## Efecto esperado

El repositorio quedará listo para que Cloud Build construya la imagen y para que Cloud Run despliegue el servicio con HTTPS automático. La aplicación conservará el comportamiento local actual y aceptará URLs JDBC de Aiven con TLS requerido.

## Archivos afectados

- `pom.xml`
- `Dockerfile`
- `.dockerignore`
- `cloudbuild.yaml`
- `docs/deployment/google-cloud-run-aiven-mysql.md`
- `src/main/java/Model/U_HibernateUtil.java`
- `src/main/java/Model/HibernateDatabaseConfiguration.java`
- `src/test/java/Model/HibernateDatabaseConfigurationTest.java`
