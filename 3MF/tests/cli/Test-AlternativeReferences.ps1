[CmdletBinding()]
param(
    [string]$TestSuiteRoot,
    [string]$Java = 'java',
    [string]$Javac = 'javac'
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'Conformance.ps1')
if (-not $TestSuiteRoot) { $TestSuiteRoot = Join-Path $PSScriptRoot '../../../../test_suites' }

$project = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$fixtures = (Resolve-Path -LiteralPath $TestSuiteRoot).Path
$schema = Join-Path $fixtures 'Consolidated_Schema/Schema'
$run = Join-Path $PSScriptRoot ('results/alternative-references-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
$classes = Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$sources = @(Get-ChildItem (Join-Path $project 'src') -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
$sourceList = Join-Path $run 'sources.txt'
[IO.File]::WriteAllLines($sourceList, [string[]]$sources, [Text.UTF8Encoding]::new($false))
$compiled = Invoke-CliProcess (Get-Command $Javac).Source @('-encoding', 'UTF-8', '-cp', (Join-Path $project 'jar/*'), '-d', $classes, "@$sourceList") $run 180
if ($compiled.TimedOut -or $compiled.ExitCode -ne 0) { throw "Compilation failed; see $run/stderr.log" }
Copy-Item -LiteralPath (Join-Path $project 'src/resources') -Destination $classes -Recurse

Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.IO.Compression
$positive = Join-Path $fixtures 'suite5_core_prod/positive_test_cases_prod_alt'
$generated = Join-Path $run 'fixtures'
[void](New-Item -ItemType Directory -Path $generated)

# Derive focused negative cases from a valid fixture without modifying the checkout.
function New-AlternativeCase([string]$Name, [scriptblock]$Edit) {
    $target = Join-Path $generated ($Name + '.3mf')
    Copy-Item (Join-Path $positive 'P_XPX_0720_01.3mf') $target
    $zip = [IO.Compression.ZipFile]::Open($target, [IO.Compression.ZipArchiveMode]::Update)
    try {
        $entry = $zip.GetEntry('3D/3dmodel.model')
        $reader = [IO.StreamReader]::new($entry.Open())
        try { $doc = Read-SafeXml ($reader.ReadToEnd()) } finally { $reader.Dispose() }
        & $Edit $doc
        $entry.Delete()
        $writer = [IO.StreamWriter]::new($zip.CreateEntry('3D/3dmodel.model').Open(), [Text.UTF8Encoding]::new($false))
        try { $writer.Write($doc.OuterXml) } finally { $writer.Dispose() }
    } finally { $zip.Dispose() }
    return $target
}
$forward = New-AlternativeCase 'forward-reference' {
    param($doc)
    $resources = $doc.SelectSingleNode('//*[local-name()="resources"]')
    [void]$resources.AppendChild($resources.FirstChild)
}
$missing = New-AlternativeCase 'missing-object' {
    param($doc)
    $doc.SelectSingleNode('//*[local-name()="alternative"]').SetAttribute('objectid', '999')
}
$wrongPart = New-AlternativeCase 'wrong-model' {
    param($doc)
    # Object 2 exists locally, but must not satisfy a reference to another model.
    $doc.SelectSingleNode('//*[local-name()="alternative"]').SetAttribute('path', '/3D/missing.model')
}
$nested = New-AlternativeCase 'nested-alternatives' {
    param($doc)
    $objects = $doc.SelectNodes('//*[local-name()="object"]')
    $alternatives = $objects[1].SelectSingleNode('*[local-name()="alternatives"]')
    [void]$objects[0].AppendChild($alternatives.CloneNode($true))
}
$cases = @(
    foreach ($iteration in 1..11) {
        @{ Model = (Join-Path $positive ('P_XPX_0720_{0:d2}.3mf' -f $iteration)); Expected = 'Accept'; Code = '' }
    }
    @{ Model = $forward; Expected = 'Fail'; Code = 'ER_141' }
    @{ Model = $missing; Expected = 'Fail'; Code = 'ER_057' }
    @{ Model = $wrongPart; Expected = 'Fail'; Code = 'ER_057' }
    @{ Model = $nested; Expected = 'Fail'; Code = 'ER_140' }
)
$results = foreach ($case in $cases) {
    $model = (Resolve-Path -LiteralPath $case.Model).Path
    $name = [IO.Path]::GetFileName($model)
    $work = Join-Path $run ([IO.Path]::GetFileNameWithoutExtension($name))
    [void](New-Item -ItemType Directory -Path $work)
    foreach ($folder in @('ztmp', 'imagetmp', 'zips')) { [void](New-Item -ItemType Directory -Path (Join-Path $work $folder)) }
    [IO.File]::WriteAllLines((Join-Path $work 'files.txt'), [string[]]@($model), [Text.UTF8Encoding]::new($false))
    @(('pschemaLoc = "' + $schema.Replace('\', '/') + '"'), 'iErrMaxStructure = "100"', 'iErrMaxValidataion = "100"') | Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
    'pNoUUIDchk = "false"' | Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII
    $execution = Invoke-CliProcess (Get-Command $Java).Source @('-cp', ($classes + [IO.Path]::PathSeparator + (Join-Path $project 'jar/*')), 'com.qualitylogic.x3mf.StartUp', '-C', '-L', 'files.txt', '-I') $work 180
    $report = Join-Path $work ('cliResults/' + [IO.Path]::ChangeExtension($name, '.xml'))
    $polarity = if ($case.Expected -eq 'Fail') { 'Reject' } else { 'Accept' }
    $checked = Test-CliResult $report $name $polarity
    $problems = [Collections.Generic.List[string]]::new()
    if ($checked.Status -ne 'Passed') { $problems.Add($checked.Detail) }
    if ($execution.TimedOut -or $execution.ExitCode -ne 0) { $problems.Add('CLI crashed or timed out') }
    if ($case.Expected -ne 'Accept' -and $checked.Actual -cne $case.Expected) { $problems.Add("Expected $($case.Expected), got $($checked.Actual)") }
    if ($checked.Actual -and $checked.Detail -notlike 'Invalid CLI XML:*') {
        $doc = Read-SafeXml ([IO.File]::ReadAllText($report))
        $issues = @($doc.DocumentElement.SelectNodes('./Issue | ./PrimaryIssue'))
        if ($case.Code -and @($issues | Where-Object { $_.GetAttribute('issueID') -eq $case.Code }).Count -eq 0) { $problems.Add("Missing $($case.Code)") }
        if ($case.Expected -eq 'Accept') {
            $html = [IO.File]::ReadAllText([IO.Path]::ChangeExtension($report, '.html'))
            if ($html -match 'ER_141' -or @($issues | Where-Object { $_.GetAttribute('issueID') -eq 'ER_141' }).Count) { $problems.Add('Spurious alternative forward reference') }
        }
    }
    [pscustomobject]@{ Suite = 'issue3'; Name = $name; Expected = $case.Expected; Actual = $checked.Actual; Status = $(if ($problems.Count) { 'Failed' } else { 'Passed' }); Detail = ($problems -join '; '); Report = $report }
}
$results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
Write-JUnitReport $results (Join-Path $run 'junit.xml')
$results | Select-Object Name, Expected, Actual, Status, Detail | Format-Table -AutoSize
Write-Host "Reports: $run"
if (@($results | Where-Object Status -ne 'Passed').Count) { exit 1 }
