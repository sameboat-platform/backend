@echo off
setlocal ENABLEDELAYEDEXPANSION
if "%~1"=="" (
  echo Usage: %~nx0 ^<new-version^> (e.g., 0.1.1 or 0.2.0)
  exit /b 1
)
set NEWVER=%~1
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0release-bump.ps1" -NewVersion "%NEWVER%"
endlocal

