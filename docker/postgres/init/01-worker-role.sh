#!/bin/bash
# Provisions the execution worker's restricted database role (ADR-006, V014).
#
# Runs once, automatically, via postgres's docker-entrypoint-initdb.d mechanism, before the API
# container connects and runs Flyway. Creating the role HERE (with its password) means V014's
# migration finds it already present (`IF NOT EXISTS`) and only applies the grants — the password
# never needs to pass through a migration file or version control.
set -euo pipefail

: "${DB_WORKER_USERNAME:?DB_WORKER_USERNAME must be set}"
: "${DB_WORKER_PASSWORD:?DB_WORKER_PASSWORD must be set}"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    DO \$\$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '${DB_WORKER_USERNAME}') THEN
            CREATE ROLE "${DB_WORKER_USERNAME}" LOGIN NOINHERIT PASSWORD '${DB_WORKER_PASSWORD}';
        END IF;
    END
    \$\$;
EOSQL
