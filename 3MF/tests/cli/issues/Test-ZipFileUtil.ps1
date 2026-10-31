[CmdletBinding()]
param([string]$Java = 'java', [string]$Javac = 'javac')
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Conformance.ps1')
$project = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$run = Join-Path $PSScriptRoot ('../results/zip-utils-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
$classes = Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$sources = @((Join-Path $project 'src/com/qualitylogic/x3mf/ZipFileUtil.java'), (Join-Path $project 'tests/src/ZipFileUtilTest.java'))
$sourceList = Join-Path $run 'sources.txt'
[IO.File]::WriteAllLines($sourceList, [string[]]@($sources | ForEach-Object { '"' + $_.Replace('\', '/') + '"' }), [Text.UTF8Encoding]::new($false))
$compiled = Invoke-CliProcess (Get-Command $Javac).Source @('-encoding', 'UTF-8', '-cp', (Join-Path $project 'jar/*'), '-d', $classes, "@$sourceList") $run 180
if ($compiled.TimedOut -or $compiled.ExitCode -ne 0) { throw "Compilation failed; see $run/stderr.log" }
$execution = Invoke-CliProcess (Get-Command $Java).Source @('-cp', ($classes + [IO.Path]::PathSeparator + (Join-Path $project 'jar/*')), 'ZipFileUtilTest', (Join-Path $run 'fixtures')) $run 60
Write-Host "Reports: $run"
Get-Content (Join-Path $run 'stdout.log')
if ($execution.TimedOut -or $execution.ExitCode -ne 0) {
    Get-Content (Join-Path $run 'stderr.log')
    throw 'Archive/comparator regressions failed'
}
