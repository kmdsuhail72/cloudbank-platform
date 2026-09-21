#!/usr/bin/env bash
set -euo pipefail
# Local development credentials match compose.yml. Existing roles are untouched.
for name in card transaction beneficiary loan audit; do
  docker exec -i cloudbank-postgres sh -c 'exec psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d postgres' <<SQL
SELECT 'CREATE ROLE cloudbank_${name} LOGIN PASSWORD ''cloudbank_${name}'''
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'cloudbank_${name}')\gexec
SELECT 'CREATE DATABASE cloudbank_${name} OWNER cloudbank_${name}'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cloudbank_${name}')\gexec
SQL
done
