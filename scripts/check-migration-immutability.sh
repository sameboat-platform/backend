#!/usr/bin/env bash
set -euo pipefail
BASE_REF=${1:-origin/main}
# Ensure base ref is fetched
if ! git rev-parse --verify "$BASE_REF" >/dev/null 2>&1; then
  echo "[immutability] Fetching $BASE_REF" >&2
  git fetch origin main:origin/main || true
fi
CHANGED=$(git diff --name-only "$BASE_REF" -- src/main/resources/db/migration)
VIOLATIONS=()
for f in $CHANGED; do
  # New file? allow
  if git diff --diff-filter=A --name-only "$BASE_REF" -- "$f" | grep -q "$f"; then
    continue
  fi
  # Modified existing migration file (starts with V and not repeatable R)
  if [[ $(basename "$f") == V*__*.sql ]]; then
    VIOLATIONS+=("$f")
  fi
done
if [ ${#VIOLATIONS[@]} -gt 0 ]; then
  echo "\nERROR: Detected edits to existing applied migrations:" >&2
  for v in "${VIOLATIONS[@]}"; do echo "  - $v" >&2; done
  echo "Create a new migration (next Vx__) instead of editing history." >&2
  exit 1
fi
echo "[immutability] OK - no historical migration edits detected."

