# Near-Term Actions

Siguientes acciones recomendadas:

1. decidir si hay consultas públicas adicionales además de Standings, confirmación y recuperación
2. configurar `MAIL_DELIVERY_MODE=smtp` y variables SMTP en `scripts\local-env.ps1`
3. cubrir con pruebas o checklist manual estos casos:
- welcome anonimo
- login valido e invalido
- alta de participante
- confirmacion de correo
- acceso bloqueado a vistas protegidas sin sesion
4. registrar en `IMPLEMENTATION_NOTES.md` cualquier patron legacy que vuelva a aparecer

Definicion de listo cercana:

- una persona nueva puede levantar, desplegar y verificar el flujo principal sin investigar el historial del proyecto
