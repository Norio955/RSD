#!/usr/bin/env bash
set -euo pipefail

echo "host all repuser 0.0.0.0/0 scram-sha-256" >> "${PGDATA}/pg_hba.conf"