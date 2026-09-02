# Despliegue en Google Cloud Run con Aiven MySQL

## Decisión

La ruta recomendada es Google Cloud Run para la aplicación WAR en GlassFish y Aiven for MySQL para la base de datos. Cloud Run entrega HTTPS automático en la URL `run.app`; para `top-racing.org`, usar Firebase Hosting delante de Cloud Run es preferible a Cloud Run Domain Mappings si se busca una opción de producción sencilla con certificados gestionados.

## Datos confirmados

- Proyecto de Google Cloud: `top-racing-77628`.
- Servicio Aiven: `mysql-service-for-top-racing-001`.
- Host Aiven: `mysql-service-for-top-racing-001-top-racing.c.aivencloud.com`.
- Puerto Aiven: `10614`.
- Base de datos Aiven: `defaultdb`.
- Usuario Aiven: `avnadmin`.
- TLS de Aiven: requerido; usar `sslMode=REQUIRED` en JDBC.

## Preparación única en Google Cloud

Ejecutar estos pasos desde Cloud Shell o desde una máquina con `gcloud` instalado:

```bash
gcloud config set project top-racing-77628
gcloud services enable run.googleapis.com cloudbuild.googleapis.com artifactregistry.googleapis.com secretmanager.googleapis.com
gcloud artifacts repositories create topracing --repository-format=docker --location=us-central1
```

Crear el secreto con la contraseña de Aiven. No guardar la contraseña en Git:

```bash
printf '%s' 'PEGAR_PASSWORD_DE_AIVEN' | gcloud secrets create topracing-db-password --data-file=-
```

Permitir que Cloud Build lea el secreto:

```bash
PROJECT_NUMBER="$(gcloud projects describe top-racing-77628 --format='value(projectNumber)')"
gcloud secrets add-iam-policy-binding topracing-db-password \
  --member="serviceAccount:${PROJECT_NUMBER}@cloudbuild.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
gcloud secrets add-iam-policy-binding topracing-db-password \
  --member="serviceAccount:${PROJECT_NUMBER}-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

Si Cloud Build no tiene permisos de despliegue en el proyecto, otorgarlos una sola vez:

```bash
gcloud projects add-iam-policy-binding top-racing-77628 \
  --member="serviceAccount:${PROJECT_NUMBER}@cloudbuild.gserviceaccount.com" \
  --role="roles/run.admin"
gcloud projects add-iam-policy-binding top-racing-77628 \
  --member="serviceAccount:${PROJECT_NUMBER}@cloudbuild.gserviceaccount.com" \
  --role="roles/artifactregistry.writer"
gcloud iam service-accounts add-iam-policy-binding \
  "${PROJECT_NUMBER}-compute@developer.gserviceaccount.com" \
  --member="serviceAccount:${PROJECT_NUMBER}@cloudbuild.gserviceaccount.com" \
  --role="roles/iam.serviceAccountUser"
```

## Primer despliegue

Desde Cloud Shell, después de conectar o clonar este repositorio:

```bash
gcloud builds submit --config cloudbuild.yaml --substitutions=_TOPRACING_APP_URL=https://CHANGE_ME_AFTER_FIRST_DEPLOY/topracingwebapp/
```

Al terminar, Cloud Run mostrará una URL HTTPS `run.app`. Hacer un segundo despliegue sustituyendo `_TOPRACING_APP_URL` por esa URL real con `/topracingwebapp/` al final, para que los enlaces de correo apunten al host público correcto.

## Variables de entorno del servicio

- `TOPRACING_DB_URL=jdbc:mysql://mysql-service-for-top-racing-001-top-racing.c.aivencloud.com:10614/defaultdb?sslMode=REQUIRED`
- `TOPRACING_DB_USERNAME=avnadmin`
- `TOPRACING_DB_PASSWORD` desde Secret Manager.
- `TOPRACING_DB_CATALOG=defaultdb`
- `TOPRACING_DB_POOL_SIZE=5`
- `TOPRACING_APP_URL=https://URL_PUBLICA/topracingwebapp/`
- `MAIL_DELIVERY_MODE=log` inicialmente. Cambiar a `gmail-oauth` cuando estén cargadas las credenciales OAuth.

## Dominio `top-racing.org`

Opción inicial sin dominio propio: usar la URL HTTPS `run.app` de Cloud Run.

Opción con dominio propio: conectar Firebase Hosting a Cloud Run y luego añadir `top-racing.org` como dominio personalizado en Firebase Hosting. Firebase emitirá y renovará certificados SSL automáticamente. Esta ruta también permite usar el dominio raíz y `www.top-racing.org` con menos limitaciones que Cloud Run Domain Mappings.

Cloud Run Domain Mappings también emite certificados gestionados, pero Google lo marca en disponibilidad limitada/vista previa y no lo recomienda para producción por latencia. Por eso queda como alternativa, no como primera opción.

## Riesgos operativos

- Aiven Free tiene límite de almacenamiento; la base local medida es pequeña, pero hay que monitorear crecimiento de `pointscount`.
- Cloud Run escala a varias instancias; por eso el pool de Hibernate se baja a 5 conexiones por instancia.
- El sistema de archivos de Cloud Run no es persistente. No guardar archivos subidos o generados dentro del contenedor.
- La imagen despliega el WAR durante el arranque del contenedor y cambia temporalmente el listener HTTP de GlassFish a `18080`; después restaura el puerto público de Cloud Run. Esto evita que Cloud Run dirija tráfico a GlassFish antes de que la aplicación quede registrada; agrega tiempo al arranque en frío, pero deja evidencia explícita en los registros.
- Antes de abrir tráfico real, importar la base MySQL local a `defaultdb` y ejecutar una prueba de humo contra la URL pública.

## Fuentes verificadas

- Google Cloud Run Container Runtime Contract: https://cloud.google.com/run/docs/container-contract
- Google Cloud Run custom domains: https://docs.cloud.google.com/run/docs/mapping-custom-domains
- Firebase Hosting con Cloud Run: https://firebase.google.com/docs/hosting/cloud-run
- Firebase Hosting custom domain: https://firebase.google.com/docs/hosting/custom-domain
- Aiven MySQL con Java: https://aiven.io/docs/products/mysql/howto/connect-with-java
- Eclipse GlassFish Docker images: https://github.com/eclipse-ee4j/glassfish.docker
