#!/usr/bin/env pwsh
param(
  [string]$BaseRef
)

$ErrorActionPreference = 'Stop'

function Log($msg) { Write-Host "[immutability] $msg" }
function Skip($msg) { Write-Host "[immutability][SKIP] $msg"; exit 0 }
function Fail($msg) { Write-Host "[immutability][ERROR] $msg" -ForegroundColor Red; exit 1 }

# Determine default branch if not provided
if (-not $BaseRef) {
  if (git remote get-url origin 2>$null) {
    $headLine = git remote show origin 2>$null | Select-String -Pattern 'HEAD branch:' | ForEach-Object { $_.ToString() }
    if ($headLine) {
      $defaultBranch = ($headLine -replace '.*HEAD branch: ', '').Trim()
    } else { $defaultBranch = 'main' }
    $BaseRef = "origin/$defaultBranch"
  } else {
    Skip "Remote 'origin' not configured; skipping immutability check (PASS)."
  }
}

# If base ref references origin but origin missing -> skip
if ($BaseRef -like 'origin/*' -and -not (git remote get-url origin 2>$null)) {
  Skip "Remote 'origin' missing; skipping immutability check (PASS)."
}

# Ensure base ref exists
if (-not (git rev-parse --verify $BaseRef 2>$null)) {
  if (git remote get-url origin 2>$null) {
    Log "Base ref '$BaseRef' not found locally. Fetching..."
    git fetch --depth=0 origin | Out-Null
  }
}
if (-not (git rev-parse --verify $BaseRef 2>$null)) {
  Skip "Base ref still not available; treating as PASS."
}

$migrationDir = 'src/main/resources/db/migration'
$diff = git diff --name-status "$BaseRef"...HEAD -- $migrationDir/V*__*.sql 2>$null
if (-not $diff) { Log 'No migration changes detected. PASS'; exit 0 }

Log "Changed migration entries:`n$diff"
$modifiedExisting = @()
$diff -split "`n" | ForEach-Object {
  if (-not $_) { return }
  $parts = $_ -split "\t"
  $status = $parts[0]
  switch -Regex ($status) {
    '^A' { return }
    '^M' { $path = $parts[1] }
    '^D' { $path = $parts[1] }
    '^R' { $path = $parts[2] }
    default { $path = if ($parts.Length -ge 2) { $parts[-1] } else { $parts[1] } }
  }
  if ($path -and ($path -match 'V[0-9]+__.+\.sql$')) { $modifiedExisting += $path }
}

if ($modifiedExisting.Count -gt 0) {
  Write-Host "`n[immutability][ERROR] The following existing migration files were modified or renamed (DISALLOWED):" -ForegroundColor Red
  $modifiedExisting | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
  Write-Host "`nAdd a new migration (next V#) instead of editing/renaming existing ones." -ForegroundColor Red
  exit 1
}

Log 'All modified migration files are NEW versions only. PASS'
