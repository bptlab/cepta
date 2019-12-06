#!/bin/sh

set -e

cmd="$@"

until PGPASSWORD=${DB_PASSWORD} psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -c '\q'; do
  >&2 echo "Postgres is unavailable - sleeping"
  sleep 1
done

>&2 echo "Postgres is up - executing command"
exec $cmd