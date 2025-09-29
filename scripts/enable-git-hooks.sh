#!/usr/bin/env bash

# Description: Enables git hooks by setting the core.hooksPath to .githooks directory.
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"
git config --local core.hooksPath .githooks
echo "✅ core.hooksPath -> .githooks"
git config --get core.hooksPath
echo "You can now add your git hooks to the .githooks directory."
echo "For more information, see: https://git-scm.com/docs/githooks"
echo "To disable, run: git config --local --unset core.hooksPath"
echo "To re-enable, run: ./scripts/enable-git-hooks.sh"
echo "To check the current hooks path, run: git config --get core.hooksPath"
