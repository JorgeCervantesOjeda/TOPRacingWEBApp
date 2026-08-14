# Stakeholders Y Usuarios

## Stakeholders

- Organizadores de TOP Racing.
- Promotores de eventos.
- Participantes registrados.
- Dueños de vehículos.
- Pilotos, cuando sean distintos del dueño.
- Postores en subastas de acceso o vehículos.
- Patrocinadores y aportantes.
- Espectadores.
- Administradores del sistema.
- Operadores técnicos del despliegue.

## Tipos De Usuario

- `Visitante anónimo`: consulta páginas públicas y puede iniciar flujos de registro, confirmación o recuperación de cuenta.
- `Participante autenticado`: consulta y opera información asociada a su cuenta.
- `Promotor`: convoca y administra eventos conforme a reglas publicadas.
- `Dueño de vehículo`: registra vehículos y puede inscribirlos en eventos.
- `Piloto`: puede conducir o quedar asociado a una inscripción cuando el dominio lo permita.
- `Postor`: participa en subastas habilitadas para su nivel de verificación.
- `Patrocinador o aportante`: aporta fondos o publicidad conforme a reglas publicadas.
- `Administrador`: gestiona reglas, usuarios, excepciones, moderación y operación crítica.
- `Operador técnico`: mantiene despliegue, base de datos, pruebas y observabilidad.

## Papeles Y Estados

Los papeles funcionales pueden acumularse en un mismo usuario, excepto los casos que el dominio reserve a administración.

Estados relevantes del usuario:

- sesión anónima o autenticada;
- correo confirmado o no confirmado;
- identidad verificada;
- verificación reforzada;
- aceptación vigente de términos;
- exclusión global activa o inexistente, y bloqueos locales por promotor cuando correspondan.

## Reglas De Acceso Por Tipo De Usuario

- La consulta pública no debe requerir cuenta cuando la información haya sido declarada pública.
- La participación operativa debe requerir cuenta autenticada.
- Las operaciones de mayor riesgo deben requerir verificación reforzada: promover eventos, inscribir vehículos y pujar por vehículos.
- Un usuario con exclusión global activa no debe poder participar en operaciones competitivas o económicas mientras esa exclusión esté activa. Un usuario con bloqueo local vigente no debe operar en eventos del promotor aplicable mientras ese bloqueo esté vigente.

## Estado De Implementación

- `Implementado`: visitante anónimo, participante autenticado, confirmación de correo, recuperación de contraseña y acceso público a Standings.
- `Parcial`: administración de participantes, sedes, vehículos, regattas, registrations, bids y puntos.
- `Objetivo`: verificación reforzada formal, roles explícitos de promotor/postor/patrocinador, exclusión normativa, aceptación versionada de términos y moderación comunitaria.
