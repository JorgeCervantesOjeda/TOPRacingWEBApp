#!/bin/bash
# docker/cloud-run-init.sh
# Deploys the TOP Racing WAR inside the Cloud Run GlassFish container before serving traffic.

set -euo pipefail

APP_NAME="topracingwebapp"
CONTEXT_ROOT="topracingwebapp"
WAR_PATH="${PATH_GF_HOME}/custom/topracingwebapp.war"
PUBLIC_PORT="${PORT:-8080}"
PREPARE_PORT="${TOPRACING_PREPARE_PORT:-18080}"

echo "Preparing ${APP_NAME} deployment from ${WAR_PATH}."
echo "Using temporary GlassFish HTTP port ${PREPARE_PORT} before restoring public port ${PUBLIC_PORT}."

export PORT="${PREPARE_PORT}"
asadmin --interactive=false start-domain

if asadmin --interactive=false list-applications | grep -E "^${APP_NAME}[[:space:]]" > /dev/null; then
  echo "Application ${APP_NAME} already exists; redeploying the packaged WAR."
else
  echo "Application ${APP_NAME} is not registered; deploying the packaged WAR."
fi

asadmin --interactive=false deploy \
  --force=true \
  --name "${APP_NAME}" \
  --contextroot "${CONTEXT_ROOT}" \
  "${WAR_PATH}"

asadmin --interactive=false list-applications --long=true
asadmin --interactive=false stop-domain --kill
export PORT="${PUBLIC_PORT}"

echo "Prepared ${APP_NAME} deployment for GlassFish startup."
