[CmdletBinding()]
param([Parameter(Mandatory = $true)][string]$JdkHome)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Conformance.ps1')
$project = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$compiler = (Resolve-Path (Join-Path $JdkHome 'bin/javac.exe')).Path
$run = Join-Path $project ('tests/cli/results/java8-build-' + [guid]::NewGuid().ToString('N'))
$classes = Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$version = Invoke-CliProcess $compiler @('-version') $run 30
$versionText = (Get-Content (Join-Path $run 'stdout.log'), (Join-Path $run 'stderr.log')) -join "`n"
if ($version.TimedOut -or $version.ExitCode -ne 0 -or $versionText -notmatch 'javac 1\.8\.') {
    throw 'This regression requires JDK 8 itself; a newer compiler with --release 8 does not reproduce the method-size failure.'
}
$sources = @(Get-ChildItem (Join-Path $project 'src/com/qualitylogic/x3mf/*.java') | ForEach-Object {
    '"' + $_.FullName.Replace('\', '/') + '"'
})
$sourceList = Join-Path $run 'sources.txt'
[IO.File]::WriteAllLines($sourceList, [string[]]$sources, [Text.UTF8Encoding]::new($false))
$result = Invoke-CliProcess $compiler @('-encoding', 'UTF-8', '-Xlint:all', '-Werror', '-cp',
    (Join-Path $project 'jar/*'), '-d', $classes, '-sourcepath', (Join-Path $project 'src'), ('@' + $sourceList)) $run 180
if ($result.TimedOut -or $result.ExitCode -ne 0) { throw "JDK 8 compilation failed; see $run/stderr.log" }
Write-Host "JDK 8 strict compilation passed. Reports: $run"
