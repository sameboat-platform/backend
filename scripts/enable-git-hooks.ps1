# Description: Enable git hooks for the repository by setting core.hooksPath to .githooks
# Usage: Run this script from the root of the repository
# Example: .\scripts\enable-git-hooks.ps1
Set-StrictMode -Version Latest
$repo = & git rev-parse --show-toplevel
Set-Location $repo
git config --local core.hooksPath .githooks
"✅ core.hooksPath -> .githooks"
git config --get core.hooksPath
"✅ Git hooks enabled"
"⚠️ If you have any issues, please run the following command to disable git hooks:"
"git config --local --unset core.hooksPath"
"⚠️ To re-enable git hooks, please run the following command:"
"git config --local core.hooksPath .githooks"

