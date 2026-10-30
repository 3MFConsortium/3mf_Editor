[CmdletBinding()]
param([string]$Java = 'java', [string]$Javac = 'javac', [int]$TimeoutSeconds = 20)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Conformance.ps1')
$project = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$run = Join-Path $project ('tests/cli/results/long-xml-lines-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
$classes = Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$sourceNames = @('XmlTextPane', 'XmlEditorKit', 'XmlViewFactory', 'XmlView')
$sources = @($sourceNames | ForEach-Object { Join-Path $project "src/com/qualitylogic/x3mf/$_.java" })
$sources += Join-Path $project 'tests/src/LongLineProbe.java'
$compile = Invoke-CliProcess (Get-Command $Javac).Source (@('-encoding', 'UTF-8', '-d', $classes) + $sources) $run 60
if ($compile.TimedOut -or $compile.ExitCode -ne 0) { throw "Compilation failed; see $run" }
$cases = @(
    @{Name='compact-0.25'; Size='0.25'; Lines='false'; Bulk='false'},
    @{Name='compact-1'; Size='1'; Lines='false'; Bulk='false'},
    @{Name='compact-4'; Size='4'; Lines='false'; Bulk='false'},
    @{Name='compact-29'; Size='29'; Lines='false'; Bulk='false'},
    @{Name='multiline-29'; Size='29'; Lines='true'; Bulk='false'},
    @{Name='bulk-compact-29'; Size='29'; Lines='false'; Bulk='true'}
)
$results = foreach ($case in $cases) {
    $directory = Join-Path $run $case.Name
    [void](New-Item -ItemType Directory -Path $directory)
    $result = Invoke-CliProcess (Get-Command $Java).Source @('-Xmx1g', '-Djava.awt.headless=true', '-cp', $classes, 'LongLineProbe', $case.Size, $case.Lines, [string]$TimeoutSeconds, $case.Bulk) $directory ($TimeoutSeconds + 10)
    [pscustomobject]@{Case=$case.Name; ExitCode=$result.ExitCode; TimedOut=($result.TimedOut -or $result.ExitCode -eq 124); Directory=$directory}
}
$results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation
$results | Format-Table -AutoSize
Write-Host "Investigation results: $run"
# This is a measurement/reproduction harness, not a pass/fail conformance test.
