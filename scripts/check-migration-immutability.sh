#!/usr/bin/env bash
set -euo pipefail

# Auto-detect default branch if not provided.
# Usage: scripts/check-migration-immutability.sh [origin/<base-branch>]
# If no arg supplied we resolve the remote HEAD of origin; if origin absent we skip.

if git remote get-url origin >/dev/null 2>&1; then
  DEFAULT_HEAD_BRANCH=$(git remote show origin 2>/dev/null | sed -n 's/.*HEAD branch: //p') || true
else
  DEFAULT_HEAD_BRANCH=""
fi
if [[ -z "${DEFAULT_HEAD_BRANCH:-}" ]]; then
  DEFAULT_HEAD_BRANCH="main"
fi
BASE_REF="${1:-origin/${DEFAULT_HEAD_BRANCH}}"
MIGRATION_DIR="src/main/resources/db/migration"

log() { echo "[immutability] $*"; }
fail() { echo "[immutability][ERROR] $*" >&2; exit 1; }

# Graceful skip if origin remote missing and user didn't explicitly provide a base ref containing origin/
if [[ ! "$BASE_REF" =~ ^origin/ ]] && ! git remote get-url origin >/dev/null 2>&1; then
  log "Remote 'origin' not configured; skipping immutability check (PASS)."
  exit 0
fi
if [[ "$BASE_REF" =~ ^origin/ ]] && ! git remote get-url origin >/dev/null 2>&1; then
  log "Remote 'origin' missing; skipping immutability check (PASS)."; exit 0; fi

# Ensure we have the base ref (fetch full history if needed)
if ! git rev-parse --verify "$BASE_REF" >/dev/null 2>&1; then
  if git remote get-url origin >/dev/null 2>&1; then
    log "Base ref '$BASE_REF' not found locally. Attempting fetch..."
    git fetch --depth=0 origin || true
  fi
fi

if ! git rev-parse --verify "$BASE_REF" >/dev/null 2>&1; then
  log "Base ref still not available; skipping immutability check (treating as PASS)."
  exit 0
fi

# List changed migration files between base and HEAD
CHANGED_RAW=$(git diff --name-status "$BASE_REF"...HEAD -- "$MIGRATION_DIR"/V*__*.sql || true)

if [[ -z "$CHANGED_RAW" ]]; then
  log "No migration changes detected. PASS"
  exit 0
fi

log "Changed migration entries:\n$CHANGED_RAW"

MODIFIED_EXISTING=()
while IFS=$'\t' read -r status rest; do
  [[ -z "$status" ]] && continue
  case "$status" in
    A*) continue ;; # new migration allowed
    M*|R*|D*)
      if [[ "$status" == R* ]]; then
        new_path=$(echo "$rest" | awk -F'\t' '{print $2}')
        path="$new_path"
      else
        path="$rest"
      fi
      if [[ "$path" =~ ${MIGRATION_DIR//\//\/}\/(V[0-9]+__[^/]+\.sql)$ ]]; then
        MODIFIED_EXISTING+=("$path")
      fi
      ;;
    *)
      path="$rest"
      if [[ "$path" =~ ${MIGRATION_DIR//\//\/}\/(V[0-9]+__[^/]+\.sql)$ ]]; then
        MODIFIED_EXISTING+=("$path")
      fi
      ;;
  esac
done < <(echo "$CHANGED_RAW")

if (( ${#MODIFIED_EXISTING[@]} > 0 )); then
  printf '\n[immutability][ERROR] The following existing migration files were modified or renamed (DISALLOWED):\n' >&2
  for f in "${MODIFIED_EXISTING[@]}"; do echo "  - $f" >&2; done
  echo >&2
  echo "Add a new migration (next V#) instead of editing/renaming existing ones." >&2
  exit 1
fi

log "All modified migration files are NEW versions only. PASS"
