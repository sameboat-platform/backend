#!/usr/bin/env bash
set -euo pipefail
if [ $# -ne 1 ]; then
  echo "Usage: $0 <new-version> (e.g., 0.1.1 or 0.2.0)" >&2
  exit 1
fi
NEWVER="$1"
# Validate semantic version format (X.Y.Z)
if ! [[ "$NEWVER" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Invalid version format: '$NEWVER'. Must match X.Y.Z (e.g., 1.2.3)" >&2
  exit 1
fi
# Update pom.xml version (safe: only project version)
./mvnw versions:set -DnewVersion="${NEWVER}" -DgenerateBackupPoms=false
# Update CHANGELOG compare link Unreleased -> new version
if grep -q "\[Unreleased\]:" CHANGELOG.md; then
  sed -i.bak -E "s|(\[Unreleased\]: .*compare/)(v[0-9]+\.[0-9]+\.[0-9]+)(\.\.\.HEAD)|\1v${NEWVER}\3|" CHANGELOG.md || true
  rm -f CHANGELOG.md.bak
fi
# Commit and tag
git add pom.xml CHANGELOG.md
git commit -m "chore(release): v${NEWVER}"
git tag -a "v${NEWVER}" -m "Release v${NEWVER}"
echo "Version bumped to ${NEWVER}. Next: push branch + tag."

