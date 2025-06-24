#!/usr/bin/env bash

echo "⏳ Waiting for MySQL to be ready..."
/opt/keycloak/bin/wait-for-it.sh keycloak-mysql:3306 --timeout=60 --strict -- echo "✅ MySQL is ready"

echo "🚀 Starting Keycloak..."
exec /opt/keycloak/bin/kc.sh start-dev \
  --db=mysql \
  --db-url-host=keycloak-mysql \
  --db-username=keycloak \
  --db-password=password \
  --hostname-strict=false \
  --hostname=keycloak \
  --http-port=8080 \
  --import-realm

