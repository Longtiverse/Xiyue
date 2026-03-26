param(
    [string]$GradleTask = ':app:assembleDebug',
    [string]$GradleBinary = $env:GRADLE_BIN
)

function Resolve-GradleBinary {
    param(
        [string]$overridePath,
        [string]$repoRoot
    )

    if ($overridePath) {
        if (Test-Path $overridePath) {
            return $overridePath
        }
        throw "Gradle executable not found at override path '$overridePath'."
    }

    $androidProject = Join-Path $repoRoot 'apps\android'
    $legacyGradlew = Join-Path $androidProject 'gradlew.bat'
    if (Test-Path $legacyGradlew) {
        return $legacyGradlew
    }

    $distRoot = Join-Path $env:USERPROFILE '.gradle\wrapper\dists'
    if (-not (Test-Path $distRoot)) {
        throw "Gradle distribution root not found at '$distRoot'. Please set GRADLE_BIN to a valid gradle.bat."
    }

    $candidate = Get-ChildItem -Path $distRoot -Recurse -Filter 'gradle.bat' -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $candidate) {
        throw "Gradle executable could not be discovered under '$distRoot'. Please install Gradle or set GRADLE_BIN."
    }

    return $candidate.FullName
}

$scriptDir = $PSScriptRoot
$repoRoot = Split-Path -Parent $scriptDir
$androidRoot = Join-Path $repoRoot 'apps\android'

$gradlePath = Resolve-GradleBinary -overridePath $GradleBinary -repoRoot $repoRoot
$gradleBin = Split-Path -Parent $gradlePath
$env:PATH = "$gradleBin;$env:PATH"

$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
    $javaHome = 'D:\AndroidStudio\jbr'
}
$env:JAVA_HOME = $javaHome

$androidSdk = $env:ANDROID_SDK_ROOT
if (-not $androidSdk) {
    $androidSdk = 'C:\Users\Alien\AppData\Local\Android\Sdk'
}
$env:ANDROID_SDK_ROOT = $androidSdk

Write-Host "Using Gradle at $gradlePath"
Write-Host "JAVA_HOME=$env:JAVA_HOME"
Write-Host "ANDROID_SDK_ROOT=$env:ANDROID_SDK_ROOT"

Push-Location $androidRoot
try {
    Write-Host "Running Gradle task $GradleTask"
    & $gradlePath $GradleTask --stacktrace
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle exited with code $LASTEXITCODE"
    }
}
finally {
    Pop-Location
}

$buildFile = Join-Path $androidRoot 'app\build.gradle.kts'
$buildFileContent = Get-Content -Path $buildFile -Raw
if ($buildFileContent -match 'versionName\s*=\s*"(?<version>[^"]+)"') {
    $versionName = $Matches.version
}
else {
    throw "Could not parse versionName from $buildFile"
}

$timestamp = Get-Date -Format 'yyyyMMdd-HHmm'
$apkSource = Join-Path $androidRoot 'app\build\outputs\apk\debug\app-debug.apk'
if (-not (Test-Path $apkSource)) {
    throw "APK not found at $apkSource"
}

$archiveDir = Join-Path $repoRoot 'builds\android\archive'
$latestDir = Join-Path $repoRoot 'builds\android\latest'
New-Item -ItemType Directory -Path $archiveDir,$latestDir -Force | Out-Null

$archiveFilename = "xiyue-android-v$versionName-$timestamp-debug.apk"
$latestFilename = "xiyue-android-v$versionName-latest-debug.apk"
$archivePath = Join-Path $archiveDir $archiveFilename
$latestPath = Join-Path $latestDir $latestFilename
$legacyLatestPath = Join-Path $latestDir 'xiyue-android-latest-debug.apk'

Copy-Item -Path $apkSource -Destination $archivePath -Force
Copy-Item -Path $archivePath -Destination $latestPath -Force
if (Test-Path $legacyLatestPath) {
    Remove-Item -Path $legacyLatestPath -Force
}

$gitCommit = $null
try {
    $gitCommit = (& git -C $repoRoot rev-parse --short HEAD 2>$null) -replace '\s+', ''
}
catch {
    # ignore; repository may not have a commit yet
}

$buildInfo = [ordered]@{
    version = $versionName
    timestamp = $timestamp
    buildType = 'debug'
    sourceApk = $apkSource
    archiveApk = $archiveFilename
    latestApk = $latestFilename
    stashCommit = if ($gitCommit) { $gitCommit } else { $null }
}

$infoPath = Join-Path $latestDir 'build-info.json'
$buildInfo | ConvertTo-Json -Depth 5 | Set-Content -Path $infoPath -Encoding UTF8

Write-Host "Archived APK to $archivePath"
Write-Host "Latest APK copied to $latestPath"
Write-Host "Build metadata written to $infoPath"
