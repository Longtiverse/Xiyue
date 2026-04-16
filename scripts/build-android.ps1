param(
    [string]$GradleTask = ':app:assembleRelease',
    [string]$GradleBinary = $env:GRADLE_BIN
)

function Load-EnvFile {
    param(
        [string]$filePath
    )

    if (-not (Test-Path $filePath)) {
        return
    }

    Get-Content -Path $filePath | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith('#')) {
            return
        }

        $parts = $line -split '=', 2
        if ($parts.Count -ne 2) {
            return
        }

        $name = $parts[0].Trim()
        $value = $parts[1].Trim().Trim('"')
        if ($name) {
            Set-Item -Path "Env:$name" -Value $value
        }
    }
}

function Resolve-JavaHome {
    param(
        [string]$preferredPath
    )

    $candidates = @(
        $preferredPath,
        $env:JAVA_HOME,
        $env:JDK_HOME
    ) | Where-Object { $_ } | Select-Object -Unique

    foreach ($candidate in $candidates) {
        $javaBinary = Join-Path $candidate 'bin\java.exe'
        if (Test-Path $javaBinary) {
            return $candidate
        }
    }

    $javaCommand = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCommand) {
        $javaBinDir = Split-Path -Parent $javaCommand.Source
        return Split-Path -Parent $javaBinDir
    }

    throw 'Could not resolve JAVA_HOME. Set JAVA_HOME in your environment or .env file.'
}

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

    $distRootCandidates = @(
        (Join-Path $repoRoot '.tmp\gradle-dist'),
        $env:GRADLE_WRAPPER_DISTS
    )
    if ($env:GRADLE_USER_HOME) {
        $distRootCandidates += (Join-Path $env:GRADLE_USER_HOME 'wrapper\dists')
    }
    if ($env:USERPROFILE) {
        $distRootCandidates += (Join-Path $env:USERPROFILE '.gradle\wrapper\dists')
    }
    $distRootCandidates = $distRootCandidates | Where-Object { $_ } | Select-Object -Unique

    $distRoot = $distRootCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
    if (-not $distRoot) {
        throw "Gradle distribution root not found. Please set GRADLE_BIN or GRADLE_WRAPPER_DISTS."
    }

    $candidate = Get-ChildItem -Path $distRoot -Recurse -Filter 'gradle.bat' -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $candidate) {
        throw "Gradle executable could not be discovered under '$distRoot'. Please install Gradle or set GRADLE_BIN."
    }

    return $candidate.FullName
}

function Resolve-WorkspaceGradleBinary {
    param(
        [string]$gradlePath,
        [string]$repoRoot
    )

    if ($gradlePath.StartsWith($repoRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        return $gradlePath
    }

    $workspaceGradleDist = Join-Path $repoRoot '.tmp\gradle-dist'
    $sourceGradleRoot = Split-Path -Parent (Split-Path -Parent $gradlePath)
    $workspaceGradleRoot = Join-Path $workspaceGradleDist (Split-Path -Leaf $sourceGradleRoot)
    $workspaceGradlePath = Join-Path $workspaceGradleRoot 'bin\gradle.bat'

    if (-not (Test-Path $workspaceGradlePath)) {
        New-Item -ItemType Directory -Path $workspaceGradleDist -Force | Out-Null
        Copy-Item -Path $sourceGradleRoot -Destination $workspaceGradleDist -Recurse -Force
    }

    return $workspaceGradlePath
}

function Seed-WorkspaceGradleHome {
    param(
        [string]$targetHome,
        [string[]]$candidateHomes
    )

    if ((Test-Path (Join-Path $targetHome 'caches')) -and (Test-Path (Join-Path $targetHome 'wrapper'))) {
        return
    }

    Write-Host "Seeding workspace Gradle home from existing local caches when available."
    $seedDirectories = @('caches', 'wrapper', 'native', 'notifications', 'daemon', 'android')

    foreach ($candidateHome in $candidateHomes) {
        if (-not $candidateHome -or -not (Test-Path $candidateHome)) {
            continue
        }

        foreach ($dir in $seedDirectories) {
            $sourceDir = Join-Path $candidateHome $dir
            $targetDir = Join-Path $targetHome $dir
            if (Test-Path $sourceDir) {
                New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
                robocopy $sourceDir $targetDir /E /NFL /NDL /NJH /NJS /NP /R:1 /W:1 /XF *.lock | Out-Null
            }
        }

        if ((Test-Path (Join-Path $targetHome 'caches')) -and (Test-Path (Join-Path $targetHome 'wrapper'))) {
            return
        }
    }
}

function Test-AccessiblePath {
    param(
        [string]$path
    )

    if (-not $path) {
        return $false
    }

    try {
        return Test-Path $path
    }
    catch {
        return $false
    }
}

function Resolve-AndroidSdkPath {
    param(
        [string]$repoRoot,
        [string]$androidProject,
        [string]$preferredPath,
        [string]$compileSdkVersion
    )

    $localPropertiesPath = Join-Path $androidProject 'local.properties'
    $localPropertiesSdk = $null
    if (Test-Path $localPropertiesPath) {
        $localProperties = Get-Content -Path $localPropertiesPath -Raw
        if ($localProperties -match 'sdk\.dir=(?<sdk>[^\r\n]+)') {
            $localPropertiesSdk = ($Matches.sdk -replace '\\\\', '\')
        }
    }

    $sdkCandidates = @(
        $preferredPath,
        $env:ANDROID_HOME,
        $env:ANDROID_SDK_ROOT,
        $env:ANDROID_SDK,
        $localPropertiesSdk
    ) | Where-Object { $_ } | Select-Object -Unique

    foreach ($candidate in $sdkCandidates) {
        $platformPath = Join-Path $candidate "platforms\android-$compileSdkVersion"
        if ((Test-AccessiblePath $candidate) -and (Test-AccessiblePath $platformPath)) {
            return $candidate
        }
    }

    throw "Could not resolve an accessible Android SDK path."
}

function Sync-WorkspaceAndroidSdk {
    param(
        [string]$targetSdkRoot,
        [string[]]$candidateSdkRoots
    )

    $sdkDirectories = @('build-tools', 'platform-tools', 'platforms', 'licenses', 'cmdline-tools', 'tools')
    New-Item -ItemType Directory -Path $targetSdkRoot -Force | Out-Null

    foreach ($candidateSdkRoot in $candidateSdkRoots) {
        if (-not (Test-AccessiblePath $candidateSdkRoot)) {
            continue
        }

        foreach ($dir in $sdkDirectories) {
            $sourceDir = Join-Path $candidateSdkRoot $dir
            $targetDir = Join-Path $targetSdkRoot $dir
            if (Test-AccessiblePath $sourceDir) {
                New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
                robocopy $sourceDir $targetDir /E /NFL /NDL /NJH /NJS /NP /R:1 /W:1 /XF *.lock | Out-Null
            }
        }
    }
}

$scriptDir = $PSScriptRoot
$repoRoot = Split-Path -Parent $scriptDir
Load-EnvFile -filePath (Join-Path $repoRoot '.env')
$androidRoot = Join-Path $repoRoot 'apps\android'
$originalUserProfile = $env:USERPROFILE
$originalGradleUserHome = $env:GRADLE_USER_HOME
$gradleUserHome = Join-Path $repoRoot '.tmp\gradle-home'
$gradleTemp = Join-Path $gradleUserHome 'tmp'
$androidSdkHome = Join-Path $repoRoot '.tmp\android-sdk-home'
$androidUserHome = Join-Path $androidSdkHome '.android'
$workspaceAndroidSdk = Join-Path $repoRoot '.tmp\android-sdk-runtime'
New-Item -ItemType Directory -Path $gradleUserHome,$gradleTemp -Force | Out-Null
New-Item -ItemType Directory -Path $androidSdkHome,$androidUserHome,$workspaceAndroidSdk -Force | Out-Null
Seed-WorkspaceGradleHome -targetHome $gradleUserHome -candidateHomes @(
    $originalGradleUserHome,
    (Join-Path $originalUserProfile '.gradle'),
    (Join-Path (Split-Path -Path $repoRoot -Qualifier) 'gradle')
)
$env:GRADLE_USER_HOME = $gradleUserHome
$env:TEMP = $gradleTemp
$env:TMP = $gradleTemp

$gradlePath = Resolve-GradleBinary -overridePath $GradleBinary -repoRoot $repoRoot
$gradlePath = Resolve-WorkspaceGradleBinary -gradlePath $gradlePath -repoRoot $repoRoot
$gradleBin = Split-Path -Parent $gradlePath
$env:PATH = "$gradleBin;$env:PATH"
$env:ANDROID_SDK_HOME = $androidSdkHome
$env:ANDROID_USER_HOME = $androidUserHome
$env:USERPROFILE = $androidSdkHome
$env:HOME = $androidSdkHome

$javaHome = Resolve-JavaHome -preferredPath $env:JAVA_HOME
$env:JAVA_HOME = $javaHome

$appBuildFile = Join-Path $androidRoot 'app\build.gradle.kts'
$appBuildFileContent = Get-Content -Path $appBuildFile -Raw
if ($appBuildFileContent -match 'compileSdk\s*=\s*(?<compileSdk>\d+)') {
    $compileSdkVersion = $Matches.compileSdk
}
else {
    throw "Could not parse compileSdk from $appBuildFile"
}

$androidSdkCandidate = Resolve-AndroidSdkPath `
    -repoRoot $repoRoot `
    -androidProject $androidRoot `
    -preferredPath $env:ANDROID_SDK_ROOT `
    -compileSdkVersion $compileSdkVersion
$androidSdkCandidates = @(
    $androidSdkCandidate,
    $env:ANDROID_HOME,
    $env:ANDROID_SDK_ROOT,
    $env:ANDROID_SDK
) | Where-Object { $_ } | Select-Object -Unique
Sync-WorkspaceAndroidSdk -targetSdkRoot $workspaceAndroidSdk -candidateSdkRoots $androidSdkCandidates
$androidSdk = $workspaceAndroidSdk
$localPropertiesPath = Join-Path $androidRoot 'local.properties'
$localPropertiesSdk = $androidSdk -replace '\\', '\\'
"sdk.dir=$localPropertiesSdk" | Set-Content -Path $localPropertiesPath -Encoding ASCII
$env:ANDROID_SDK_ROOT = $androidSdk
$env:ANDROID_HOME = $androidSdk

Write-Host "Using Gradle at $gradlePath"
Write-Host "JAVA_HOME=$env:JAVA_HOME"
Write-Host "ANDROID_SDK_ROOT=$env:ANDROID_SDK_ROOT"
Write-Host "ANDROID_HOME=$env:ANDROID_HOME"
Write-Host "ANDROID_SDK_HOME=$env:ANDROID_SDK_HOME"
Write-Host "ANDROID_USER_HOME=$env:ANDROID_USER_HOME"
Write-Host "GRADLE_USER_HOME=$env:GRADLE_USER_HOME"
Write-Host "TEMP=$env:TEMP"

Push-Location $androidRoot
try {
    Write-Host "Running Gradle task $GradleTask"
    & $gradlePath $GradleTask --stacktrace --no-daemon
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
$apkOutputDir = Join-Path $androidRoot 'app\build\outputs\apk\release'
$apkMetadataPath = Join-Path $apkOutputDir 'output-metadata.json'
$apkSource = $null
if (Test-Path $apkMetadataPath) {
    $apkMetadata = Get-Content -Path $apkMetadataPath -Raw | ConvertFrom-Json
    $metadataOutputFile = $apkMetadata.elements | Select-Object -First 1 -ExpandProperty outputFile
    if ($metadataOutputFile) {
        $apkSource = Join-Path $apkOutputDir $metadataOutputFile
    }
}
if (-not $apkSource -or -not (Test-Path $apkSource)) {
    $apkSource = Get-ChildItem -Path $apkOutputDir -Filter '*.apk' -File |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1 -ExpandProperty FullName
}
if (-not $apkSource -or -not (Test-Path $apkSource)) {
    throw "APK not found under $apkOutputDir"
}

$archiveDir = Join-Path $repoRoot 'builds\android\archive'
$latestDir = Join-Path $repoRoot 'builds\android\latest'
New-Item -ItemType Directory -Path $archiveDir,$latestDir -Force | Out-Null

# Clean up old debug APKs from latest directory so it only contains release builds
Get-ChildItem -Path $latestDir -Filter '*-debug.apk' -File -ErrorAction SilentlyContinue | Remove-Item -Force

$archiveFilename = "xiyue-android-v$versionName-$timestamp-release.apk"
$latestFilename = "xiyue-android-v$versionName-$timestamp-release.apk"
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
    buildType = 'release'
    sourceApk = $apkSource
    archiveApk = $archiveFilename
    latestApk = $latestFilename
    stashCommit = if ($gitCommit) { $gitCommit } else { $null }
}

$infoPath = Join-Path $latestDir 'build-info.json'
$buildInfo | ConvertTo-Json -Depth 5 | Set-Content -Path $infoPath -Encoding UTF8

# Also copy to root latest/ directory for quick access
$rootLatestDir = Join-Path $repoRoot 'latest'
New-Item -ItemType Directory -Path $rootLatestDir -Force | Out-Null
$rootLatestPath = Join-Path $rootLatestDir $latestFilename
Copy-Item -Path $archivePath -Destination $rootLatestPath -Force
Get-ChildItem -Path $rootLatestDir -Filter '*.apk' -File -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending |
    Select-Object -Skip 5 |
    Remove-Item -Force

Write-Host "Archived APK to $archivePath"
Write-Host "Latest APK copied to $latestPath"
Write-Host "Root latest APK copied to $rootLatestPath"
Write-Host "Build metadata written to $infoPath"
