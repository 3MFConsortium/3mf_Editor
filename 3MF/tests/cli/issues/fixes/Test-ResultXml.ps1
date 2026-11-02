[CmdletBinding()]
param([string]$TestSuiteRoot, [string]$Java = 'java', [string]$Javac = 'javac')
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../../Conformance.ps1')
if (-not $TestSuiteRoot) { $TestSuiteRoot = Join-Path $PSScriptRoot '../../../../../../test_suites' }
$project = (Resolve-Path (Join-Path $PSScriptRoot '../../../..')).Path
$fixtures = (Resolve-Path -LiteralPath $TestSuiteRoot).Path
$schema = Join-Path $fixtures 'Consolidated_Schema/Schema'
$run = Join-Path $project ('tests/cli/results/result-xml-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
$classes = Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$sources = @(Get-ChildItem (Join-Path $project 'src') -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
$sources += '"' + (Join-Path $project 'tests/cli/src/ResultXmlTest.java').Replace('\', '/') + '"'
[IO.File]::WriteAllLines((Join-Path $run 'sources.txt'), [string[]]$sources, [Text.UTF8Encoding]::new($false))
$compiled = Invoke-CliProcess (Get-Command $Javac).Source @('--release', '8', '-encoding', 'UTF-8', '-cp', (Join-Path $project 'jar/*'), '-d', $classes, ('@' + (Join-Path $run 'sources.txt'))) $run 180
if ($compiled.TimedOut -or $compiled.ExitCode -ne 0) { throw "Compilation failed; see $run/stderr.log" }
Copy-Item -LiteralPath (Join-Path $project 'src/resources') -Destination $classes -Recurse
$classpath = $classes + [IO.Path]::PathSeparator + (Join-Path $project 'jar/*')
$helper = Invoke-CliProcess (Get-Command $Java).Source @('-cp', $classpath, 'com.qualitylogic.x3mf.ResultXmlTest') $run 30
if ($helper.TimedOut -or $helper.ExitCode -ne 0) { throw "Report helper failed: $run" }
$cases = @(Get-ChildItem $fixtures -Recurse -Filter '*.3mf' | Where-Object {
    $_.BaseName -match '^N_\w{3}_(0206|0422|3302|2602|2603|2604|2605|2606|2607)_' -or
    $_.BaseName -match '^P_EPX_0740_(01|05|06)$' -or $_.BaseName -eq 'P_XPM_0101_01'
})
if ($cases.Count -lt 34) { throw 'Expected all reported fixtures plus a valid control' }
$work = Join-Path $run 'batch'
[void](New-Item -ItemType Directory -Path $work)
foreach ($folder in @('ztmp', 'imagetmp', 'zips')) { [void](New-Item -ItemType Directory -Path (Join-Path $work $folder)) }
[IO.File]::WriteAllLines((Join-Path $work 'files.txt'), [string[]]@($cases | ForEach-Object FullName), [Text.UTF8Encoding]::new($false))
@(('pschemaLoc = "' + $schema.Replace('\', '/') + '"'), 'iErrMaxStructure = "100"', 'iErrMaxValidataion = "100"') | Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
'pNoUUIDchk = "false"' | Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII
'validationConsumer = "editor"' | Add-Content (Join-Path $work '3MF.ini') -Encoding ASCII
$execution = Invoke-CliProcess (Get-Command $Java).Source @('-cp', ($classes + [IO.Path]::PathSeparator + (Join-Path $project 'jar/*')), 'com.qualitylogic.x3mf.StartUp', '-C', '-L', 'files.txt', '-I') $work 180

$results = foreach ($model in $cases) {
    $name = $model.Name
    # Access restrictions alone are incomplete editor validation, not proven corruption.
    # The negative suite's printer acceptance criteria are tracked by Run-Conformance.
    $expected = if ($model.BaseName -eq 'P_XPM_0101_01' -or $model.BaseName -match '^P_EPX_0740_|^N_EPX_260[24]_') { 'Accept' } else { 'Reject' }
    $report = Join-Path $work ('cliResults/' + [IO.Path]::ChangeExtension($name, '.xml'))
    $checked = Test-CliResult $report $name $expected
    $problems = [Collections.Generic.List[string]]::new()
    if ($checked.Status -ne 'Passed') { $problems.Add($checked.Detail) }
    if ($execution.TimedOut -or $execution.ExitCode -ne 0) { $problems.Add('CLI crashed or timed out') }
    if ($checked.Status -eq 'Passed') {
        $doc = Read-SafeXml ([IO.File]::ReadAllText($report))
        if ($model.BaseName -match '^P_EPX_0740_|^N_EPX_260[24]_') {
            if ($doc.DocumentElement.GetAttribute('result') -ne 'Warning' -or -not $doc.SelectSingleNode('//*[@issueID="WR_031"]')) { $problems.Add('Inaccessible content must retain an incomplete-validation warning') }
        }
        if ($model.BaseName -eq 'N_EPX_2605_02' -and -not $doc.SelectSingleNode('//*[@issueID="ER_190"]')) { $problems.Add('Missing encrypted-data corruption error') }
        if ($model.BaseName -eq 'N_EPX_2606_02' -and -not $doc.SelectSingleNode('//*[@issueID="ER_191"]')) { $problems.Add('Missing keystore relationship error') }
        if ($model.BaseName -eq 'N_EPX_2606_01' -and -not $doc.SelectSingleNode('//*[@issueID="ER_198"]')) { $problems.Add('Missing encrypted-file relationship error') }
        if ($doc.SelectNodes('//Issue/Issue | //PrimaryIssue/Issue').Count) { $problems.Add('Nested diagnostics') }
        foreach ($issue in $doc.DocumentElement.SelectNodes('./Issue | ./PrimaryIssue')) {
            if (-not $issue.InnerText.Trim()) { $problems.Add('Empty diagnostic') }
            if ($issue.GetAttribute('issueID') -eq 'ER_REPORT_XML') { $problems.Add('Unexpected fallback report') }
        }
        if ($name -match '_0206_' -and $doc.DocumentElement.InnerText -notmatch '\[!\$&') { $problems.Add('Schema pattern text did not round trip') }
        if ($name -eq 'N_DPX_3302_01.3mf' -and -not $doc.SelectSingleNode('//*[@issueID="ER_175"]')) { $problems.Add('Missing ER_175') }
        $aggregates = @(Get-ChildItem (Join-Path $work 'cliResults') -Filter 'XmlAggregate*.xml')
        if ($aggregates.Count -ne 1) { $problems.Add('Missing aggregate report') }
        foreach ($aggregate in $aggregates) {
            try { $null = Read-SafeXml ([IO.File]::ReadAllText($aggregate.FullName)) }
            catch { $problems.Add('Malformed aggregate report') }
        }
    }
    [pscustomobject]@{ Suite = 'issue22'; Name = $name; Expected = $expected; Actual = $checked.Actual; Status = $(if ($problems.Count) { 'Failed' } else { 'Passed' }); Detail = ($problems -join '; '); Report = $report }
}
$results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
Write-JUnitReport $results (Join-Path $run 'junit.xml')
$results | Select-Object Name, Actual, Status, Detail | Format-Table -AutoSize
Write-Host "Reports: $run"
if (@($results | Where-Object Status -ne 'Passed').Count) { exit 1 }

# Exercise the separate single-file output path and attribute escaping.
$special = Join-Path $work "control & 'quoted'.3mf"
Copy-Item ($cases | Where-Object BaseName -eq 'P_XPM_0101_01' | Select-Object -First 1).FullName $special
$execution = Invoke-CliProcess (Get-Command $Java).Source @('-cp', $classpath, 'com.qualitylogic.x3mf.StartUp', '-C', '-F', $special, '-I') $work 180
if ($execution.TimedOut -or $execution.ExitCode -ne 0) { throw 'Single-file CLI failed' }
$report = Join-Path $work "cliResults/control & 'quoted'.xml"
$checked = Test-CliResult $report ([IO.Path]::GetFileName($special)) 'Accept'
if ($checked.Status -ne 'Passed') { throw "Single-file report failed: $($checked.Detail)" }
Write-Host 'Single-file filename escaping and six fallback checks passed.'

# Force schema diagnostics through the grouped/limited branch as well.
Add-Content (Join-Path $work '3MF.txt') 'iErrMaxValidataion = "1"'
$limited = $cases | Where-Object BaseName -eq 'N_XXX_0206_01' | Select-Object -First 1
$execution = Invoke-CliProcess (Get-Command $Java).Source @('-cp', $classpath, 'com.qualitylogic.x3mf.StartUp', '-C', '-F', $limited.FullName, '-I') $work 180
if ($execution.TimedOut -or $execution.ExitCode -ne 0) { throw 'Grouped schema CLI failed' }
$reports = @(Get-ChildItem (Join-Path $work 'cliResults') -Filter 'N_XXX_0206_01*.xml')
if ($reports.Count -lt 1) { throw 'Missing grouped schema output' }
foreach ($report in $reports) {
    $doc = Read-SafeXml ([IO.File]::ReadAllText($report.FullName))
    if ($doc.DocumentElement.InnerText -notmatch '\[!\$&' -or $doc.DocumentElement.InnerText -match 'line: \(%') { throw 'Grouped schema message was truncated' }
    if ($doc.DocumentElement.GetAttribute('result') -ne 'Fail' -or $doc.SelectSingleNode('//*[@issueID="ER_REPORT_XML"]')) { throw 'Invalid grouped schema result' }
}
Write-Host 'Grouped schema diagnostics passed.'
