# TOPRacingWEBApp

Aplicacion web Java para gestionar la liga TOP-Racing: participantes, autos, campeonatos (regattas), sedes y registros.

## Stack actual
- Java 11 o superior
- Jakarta Faces / CDI
- PrimeFaces Jakarta
- Hibernate + MySQL
- Maven WAR
- GlassFish 6+ o 7+

## Estructura
- `src/main/java/Controller`: logica de navegacion y flujo.
- `src/main/java/Model`: reglas de negocio y acceso a datos.
- `src/main/java/Tables`: entidades y mapeos Hibernate.
- `src/main/java/View`: beans JSF/CDI y textos de interfaz.
- `src/main/resources`: configuracion de runtime.
- `src/main/webapp`: vistas XHTML, recursos estaticos y configuracion web.

## IntelliJ IDEA
1. Abrir el archivo `pom.xml` como proyecto Maven.
2. Configurar el Project SDK en Java 11 o superior.
3. Si quieres ayudas especificas de Faces, instalar el plugin `Jakarta EE: Server Faces (JSF)`.
4. Esperar a que IntelliJ descargue dependencias y reimporte el proyecto.

## GlassFish
1. Usar GlassFish 6 o superior. GlassFish 4 no sirve para esta version porque solo soporta `javax.*`.
2. Configurar GlassFish para arrancar con JDK 11 o superior.
3. Crear la base MySQL e importar un script, por ejemplo `topracing26.sql`.
4. Ajustar conexion en `src/main/resources/hibernate.cfg.xml`.
5. Ejecutar `mvn clean package`.
6. Desplegar `target/topracingwebapp.war` en GlassFish.
7. Abrir `http://localhost:8080/topracingwebapp/faces/welcome.xhtml`.

## Entorno local en esta maquina
- JDK 17: `C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot`
- Maven 3.9.11: `C:\Users\usuario\ownCloud2\tools\apache-maven-3.9.11`
- GlassFish 7.0.25: `C:\Users\usuario\ownCloud2\tools\glassfish7\glassfish`
- Dominio GlassFish: `topracing`
- MySQL local restaurado con base `topracing26`
- Usuario MySQL local para esta app: `admin` / `admin`

## Scripts locales
- `powershell -ExecutionPolicy Bypass -File .\scripts\setup-local-mysql.ps1`
  Restaura el acceso local de MySQL e importa `topracing26.sql`.
- `powershell -ExecutionPolicy Bypass -File .\scripts\start-glassfish.ps1`
  Arranca el dominio local `topracing`.
- `powershell -ExecutionPolicy Bypass -File .\scripts\build-and-deploy.ps1`
  Compila con Maven y redepliega en GlassFish.
- `node .\scripts\import-real-venues-from-mymaps.mjs --apply --clean-test-data`
  Limpia datos automáticos de prueba reconocibles en `topracing26` e importa circuitos reales desde el CSV de My Maps, con una variante inicial por circuito.
- `powershell -ExecutionPolicy Bypass -File .\scripts\test-local.ps1 -Mode full-live`
  Prepara `topracing26_test`, arranca el dominio `topracing` y ejecuta las pruebas automáticas contra la BD aislada.

## Bases de datos locales
- `topracing26`: datos reales para uso y pruebas manuales.
- `topracing26_test`: datos para pruebas automáticas; se puede recrear desde `topracing26` con `scripts\prepare-test-db.ps1`.
- Las pruebas automáticas no deben crear datos en `topracing26`.

## Variables opcionales
- `TOPRACING_JAVA_HOME`
- `TOPRACING_MAVEN_HOME`
- `TOPRACING_GLASSFISH_HOME`
- `TOPRACING_ASADMIN`
- `TOPRACING_GF_DOMAIN`
- `TOPRACING_DB_URL`
- `TOPRACING_DB_CATALOG`
- `TOPRACING_DB_USERNAME`
- `TOPRACING_DB_PASSWORD`
- `TOPRACING_APP_URL`: URL pública base usada en enlaces de e-mail. En desarrollo local usa `http://localhost:8080/topracingwebapp/`; en otro servidor cámbiala sin modificar código.

## Correo
La app puede enviar e-mails por SMTP o por el flujo heredado de Gmail OAuth.
Los scripts `start-glassfish.ps1` y `build-and-deploy.ps1` cargan opcionalmente
`scripts\local-env.ps1`; ese archivo no se versiona porque puede contener secretos.
Usa `scripts\local-env.example.ps1` como plantilla.

Modo recomendado:
- `MAIL_DELIVERY_MODE=smtp`
- `MAIL_SENDER_EMAIL`
- `MAIL_MONITOR_EMAIL`
- `MAIL_SMTP_HOST`
- `MAIL_SMTP_PORT` (default: `587`)
- `MAIL_SMTP_AUTH` (default: `true`)
- `MAIL_SMTP_STARTTLS` (default: `true`)
- `MAIL_SMTP_SSL_ENABLE` (default: `false`)
- `MAIL_SMTP_USERNAME`
- `MAIL_SMTP_PASSWORD`

Modos alternativos:
- `MAIL_DELIVERY_MODE=gmail-oauth` con `MAIL_OAUTH_CLIENT_ID`,
  `MAIL_OAUTH_CLIENT_SECRET`, `MAIL_OAUTH_REFRESH_TOKEN` y, opcionalmente,
  `MAIL_OAUTH_TOKEN_URL` (default: `https://oauth2.googleapis.com/token`).
- `MAIL_DELIVERY_MODE=log` para pruebas sin envío real.
- `MAIL_DELIVERY_MODE=disabled` para desactivar explícitamente el envío.

Si `MAIL_DELIVERY_MODE` no se define, la app usa SMTP cuando existe
`MAIL_SMTP_HOST`, usa Gmail OAuth cuando existen variables OAuth, y falla con
un error explícito si no hay transporte configurado.
Después de crear o modificar `scripts\local-env.ps1`, reinicia el dominio de
GlassFish para que el proceso cargue las variables nuevas.
