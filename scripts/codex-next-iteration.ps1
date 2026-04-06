param(
    [int]$Iterations = 1,
    [switch]$Detached,
    [switch]$Unsafe
)

$scriptPath = $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $PSScriptRoot
$logDir = Join-Path $repoRoot '.tmp\codex-control'
# Legacy reference for repo contract tests: codex exec --cd D:\Project\Xiyue
$codexCommand = Get-Command codex -ErrorAction Stop
$codexBaseDir = Split-Path $codexCommand.Source -Parent
$codexJsPath = Join-Path $codexBaseDir 'node_modules\@openai\codex\bin\codex.js'
New-Item -ItemType Directory -Path $logDir -Force | Out-Null

if ($Detached) {
    $argList = @(
        '-ExecutionPolicy', 'Bypass',
        '-File', $scriptPath,
        '-Iterations', $Iterations
    )

    if ($Unsafe) {
        $argList += '-Unsafe'
    }

    $detachedLog = Join-Path $logDir ("launcher-{0}.log" -f (Get-Date -Format 'yyyyMMdd-HHmmss'))
    $detachedErrorLog = Join-Path $logDir ("launcher-{0}.err.log" -f (Get-Date -Format 'yyyyMMdd-HHmmss'))
    $process = Start-Process powershell `
        -ArgumentList $argList `
        -WorkingDirectory $repoRoot `
        -RedirectStandardOutput $detachedLog `
        -RedirectStandardError $detachedErrorLog `
        -PassThru

    Write-Host "Started detached Codex loop. PID=$($process.Id)"
    Write-Host "Launcher log: $detachedLog"
    Write-Host "Launcher error log: $detachedErrorLog"
    exit 0
}

$codexModeArg = if ($Unsafe) {
    '--dangerously-bypass-approvals-and-sandbox'
} else {
    '--full-auto'
}

for ($iteration = 1; $iteration -le $Iterations; $iteration++) {
    $timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $logPath = Join-Path $logDir ("iteration-{0}-{1}.log" -f $iteration, $timestamp)
    $summaryPath = Join-Path $logDir ("iteration-{0}-{1}-summary.txt" -f $iteration, $timestamp)
    $promptPath = Join-Path $logDir ("iteration-{0}-{1}-prompt.txt" -f $iteration, $timestamp)

    $prompt = @"
Continue developing D:\Project\Xiyue and automatically execute the next iteration.

Rules:
1. Do not stop to ask the user questions. Choose one highest-impact, shippable slice and complete it.
2. Android App is the current priority. Prefer improvements to:
   - faster scale/chord selection
   - easier playback flow
   - stronger background playback
   - warmer and less fatiguing timbre
   - more practical notification and playback interaction
3. Keep test-first discipline: update or add tests first, then implement, then verify.
4. If Android code changes, run at least:
   - npm run test:android
   - npm test
   - npm run build:android
5. Do not reinstall existing local toolchains.
6. Final summary must include:
   - concrete improvements this round
   - test/build results
   - latest APK path
   - critical self-assessment

Project context:
- User prefers direct execution, not repeated questions
- Versioned APK archive/latest pipeline already exists and must be preserved
- Working repo is D:\Project\Xiyue

Start now and report only after this iteration is complete.
"@

    Set-Content -Path $promptPath -Value $prompt -Encoding UTF8

    Write-Host "=== Codex iteration $iteration/$Iterations ==="
    Write-Host "Log: $logPath"
    Write-Host "Summary: $summaryPath"

    $cmdLine = 'node "{0}" exec --cd "{1}" {2} --output-last-message "{3}" < "{4}" > "{5}" 2>&1' -f `
        $codexJsPath, `
        $repoRoot, `
        $codexModeArg, `
        $summaryPath, `
        $promptPath, `
        $logPath

    & cmd /c $cmdLine

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Iteration $iteration failed with exit code $LASTEXITCODE"
        Write-Host "See log: $logPath"
        exit $LASTEXITCODE
    }
}

Write-Host "Completed $Iterations iteration(s)."
Write-Host "Logs are in: $logDir"
