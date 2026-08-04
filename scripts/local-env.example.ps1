# scripts/local-env.example.ps1
# Example local runtime configuration. Copy values into scripts/local-env.ps1.

$env:TOPRACING_APP_URL = "http://localhost:8080/topracingwebapp/"

# Recommended local sending mode:
$env:MAIL_DELIVERY_MODE = "smtp"
$env:MAIL_SENDER_EMAIL = "top.racing.org@gmail.com"
$env:MAIL_MONITOR_EMAIL = "top.racing.org@gmail.com"
$env:MAIL_SMTP_HOST = "smtp.gmail.com"
$env:MAIL_SMTP_PORT = "587"
$env:MAIL_SMTP_AUTH = "true"
$env:MAIL_SMTP_STARTTLS = "true"
$env:MAIL_SMTP_SSL_ENABLE = "false"
$env:MAIL_SMTP_USERNAME = "top.racing.org@gmail.com"
$env:MAIL_SMTP_PASSWORD = "replace-with-app-password"

# Test mode without network delivery:
# $env:MAIL_DELIVERY_MODE = "log"

# Legacy Gmail API mode:
# $env:MAIL_DELIVERY_MODE = "gmail-oauth"
# $env:MAIL_OAUTH_CLIENT_ID = "replace-with-client-id"
# $env:MAIL_OAUTH_CLIENT_SECRET = "replace-with-client-secret"
# $env:MAIL_OAUTH_REFRESH_TOKEN = "replace-with-refresh-token"
