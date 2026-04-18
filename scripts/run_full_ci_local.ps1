# File: run_full_ci_local.ps1
# Description: Run the same gates as `.github/workflows/fluvian-sdk-ci.yml` (Python + Gradle + JaCoCo) from a Windows shell.
# Author: monigarr@monigarr.com
# Date: 2026-04-18
# Version: 1.3.6
#
# Usage:
#   pwsh -File scripts/run_full_ci_local.ps1
#
# Usage example:
#   cd <repo-root>; pwsh -File scripts/run_full_ci_local.ps1

$ErrorActionPreference = "Stop"
$Root = Split-Path $PSScriptRoot -Parent
Set-Location $Root

python (Join-Path $Root "scripts\check_echelon_headers.py")
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

python (Join-Path $Root "scripts\validate_open_core_layout.py")
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$AndroidDir = Join-Path $Root "SDK_DEMO_ANDROID"
Push-Location $AndroidDir
try {
    .\gradlew.bat :fluvian-sdk-core:apiCheck --no-daemon --stacktrace
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    .\gradlew.bat `
        :fluvian-sdk-core:testDebugUnitTest `
        :app:testDebugUnitTest `
        :app:assembleDebug `
        :fluvian-sdk-core:jacocoFluvianSdkCoreDebug `
        :app:jacocoAppDebug `
        --no-daemon --stacktrace
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
finally {
    Pop-Location
}

python (Join-Path $Root "scripts\check_jacoco_coverage.py") (Join-Path $Root "SDK_DEMO_ANDROID\fluvian-sdk-core\build\reports\jacoco\jacocoFluvianSdkCoreDebug\jacoco.xml") 0.15
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

python (Join-Path $Root "scripts\check_jacoco_coverage.py") (Join-Path $Root "SDK_DEMO_ANDROID\app\build\reports\jacoco\jacocoAppDebug\jacoco.xml") 0.012
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Local CI parity run completed successfully."
