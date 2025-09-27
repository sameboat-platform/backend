@echo off
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0\check-migration-immutability.ps1" %*

