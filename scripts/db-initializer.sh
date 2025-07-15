#!/bin/bash

set -e

# Ждём, пока PostgreSQL запустится (более надёжная проверка)
echo "Waiting for PostgreSQL to start..."
max_retries=30
retry=0

until pg_isready -U "$POSTGRES_USER" -h localhost -p 5432 || [ $retry -eq $max_retries ]; do
  echo "Attempt $retry: PostgreSQL is not ready yet. Retrying in 1 second..."
  sleep 1
  retry=$((retry + 1))
done

if [ $retry -eq $max_retries ]; then
  echo "Error: PostgreSQL not responding after $max_retries attempts. Exiting."
  exit 1
fi

# Создаём базы данных
echo "PostgreSQL is ready. Creating databases..."

psql -U "$POSTGRES_USER" -c "CREATE DATABASE patient_db;" || echo "Database 'account_db' already exists"

# Запускаем PostgreSQL
echo "Starting PostgreSQL server..."
exec "$@"