[CmdletBinding()]
param(
    [string]$TestSuiteRoot,
    [string]$Java = 'java',
    [string]$Javac = 'javac'
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Conformance.ps1')
if (-not $TestSuiteRoot) { $TestSuiteRoot = Join-Path $PSScriptRoot '../../../../../test_suites' }

$project = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$fixtures = (Resolve-Path -LiteralPath $TestSuiteRoot).Path
$schema = Join-Path $fixtures 'Consolidated_Schema/Schema'
$run = Join-Path $PSScriptRoot ('../results/beam-minlength-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
$classes = Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$sources = @(Get-ChildItem (Join-Path $project 'src') -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
$sourceList = Join-Path $run 'sources.txt'
[IO.File]::WriteAllLines($sourceList, [string[]]$sources, [Text.UTF8Encoding]::new($false))
$compiled = Invoke-CliProcess (Get-Command $Javac).Source @('-encoding', 'UTF-8', '-cp', (Join-Path $project 'jar/*'), '-d', $classes, "@$sourceList") $run 180
if ($compiled.TimedOut -or $compiled.ExitCode -ne 0) { throw "Compilation failed; see $run/stderr.log" }
Copy-Item -LiteralPath (Join-Path $project 'src/resources') -Destination $classes -Recurse

$cases = @(
    foreach ($iteration in 1..3) {
        @{ Relative = ('suite7_beam/positive_test_cases/P_BXX_2003_{0:d2}.3mf' -f $iteration); Expected = 'Warning'; Code = 'WR_025' }
    }
    @{ Relative = 'suite7_beam/negative_test_cases/N_BXX_2503_03.3mf'; Expected = 'Fail'; Code = 'ER_091' }
    foreach ($iteration in 1..6) {
        @{ Relative = ('suite7_beam/negative_test_cases/N_BXX_2502_{0:d2}.3mf' -f $iteration); Expected = 'Fail'; Code = '' }
    }
    @{ Relative = 'suite2_core_prod_matl/positive_test_cases/P_XPM_0336_01.3mf'; Expected = 'Accept'; Code = '' }
    @{ Relative = 'suite5_core_prod/positive_test_cases/P_XPX_0336_01.3mf'; Expected = 'Accept'; Code = '' }
)
$results = foreach ($case in $cases) {
    $model = (Resolve-Path -LiteralPath (Join-Path $fixtures $case.Relative)).Path
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
        if ($case.Code -eq 'WR_025') {
            if ($doc.DocumentElement.FirstChild.GetAttribute('issueID') -ne 'WR_025') { $problems.Add('Short-beam warning was not selected as primary issue') }
            if (@($issues | Where-Object { $_.GetAttribute('issueID') -ne 'WR_025' -or $_.GetAttribute('severity') -ne 'info' -or $_.GetAttribute('type') -ne 'beam length warning' }).Count) { $problems.Add('Unexpected short-beam diagnostic') }
            $html = [IO.File]::ReadAllText([IO.Path]::ChangeExtension($report, '.html'))
            if ($html -notmatch 'WR_025' -or $html -match 'ER_091') { $problems.Add('HTML did not report short beams exclusively as warnings') }
        }
        if ($name -eq 'N_BXX_2503_03.3mf' -and @($issues | Where-Object { $_.GetAttribute('issueID') -eq 'ER_091' -and $_.InnerText -match 'vertexes set to the same' }).Count -eq 0) { $problems.Add('Missing identical-vertex error') }
    }
    [pscustomobject]@{ Suite = 'issue2'; Name = $name; Expected = $case.Expected; Actual = $checked.Actual; Status = $(if ($problems.Count) { 'Failed' } else { 'Passed' }); Detail = ($problems -join '; '); Report = $report }
}
$results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
Write-JUnitReport $results (Join-Path $run 'junit.xml')
$results | Select-Object Name, Expected, Actual, Status, Detail | Format-Table -AutoSize
Write-Host "Reports: $run"
if (@($results | Where-Object Status -ne 'Passed').Count) { exit 1 }
