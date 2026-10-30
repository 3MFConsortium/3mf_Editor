[CmdletBinding()]
param([string]$TestSuiteRoot, [string]$Java = 'java', [string]$Javac = 'javac')
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Conformance.ps1')
if (-not $TestSuiteRoot) { $TestSuiteRoot = Join-Path $PSScriptRoot '../../../../../test_suites' }
$project = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$fixtures = (Resolve-Path -LiteralPath $TestSuiteRoot).Path
$schema = Join-Path $fixtures 'Consolidated_Schema/Schema'
$run = Join-Path $project ('tests/cli/results/secure-content-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
$classes = Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$sources = @(Get-ChildItem (Join-Path $project 'src') -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
$sources += '"' + (Join-Path $project 'tests/src/SecureContentFixtures.java').Replace('\', '/') + '"'
$sourceList = Join-Path $run 'sources.txt'
[IO.File]::WriteAllLines($sourceList, [string[]]$sources, [Text.UTF8Encoding]::new($false))
$compiled = Invoke-CliProcess (Get-Command $Javac).Source @('-encoding', 'UTF-8', '-cp', (Join-Path $project 'jar/*'), '-d', $classes, "@$sourceList") $run 180
if ($compiled.TimedOut -or $compiled.ExitCode -ne 0) { throw "Compilation failed; see $run/stderr.log" }
Copy-Item -LiteralPath (Join-Path $project 'src/resources') -Destination $classes -Recurse
$classpath = $classes + [IO.Path]::PathSeparator + (Join-Path $project 'jar/*')
$positive = Join-Path $fixtures 'suite8_secure/positive_test_cases/P_EPX_2101_01.3mf'
$generated = @{}
foreach ($kind in @('schema', 'structure', 'no-keystore')) {
    $generated[$kind] = Join-Path $run ($kind + '.3mf')
    $execution = Invoke-CliProcess (Get-Command $Java).Source @('-cp', $classpath, 'SecureContentFixtures', $positive, $generated[$kind], $kind) $run 60
    if ($execution.TimedOut -or $execution.ExitCode -ne 0) { throw "Fixture generation failed; see $run/stderr.log" }
}
$cases = @(
    @{ Name = 'single-default'; Models = @($positive); Options = @(); Expected = @('Accept'); Codes = @('') }
    @{ Name = 'single-schema'; Models = @($positive); Options = @('-V'); Expected = @('Accept'); Codes = @('') }
    @{ Name = 'single-structure'; Models = @($positive); Options = @('-S'); Expected = @('Accept'); Codes = @('') }
    @{ Name = 'sha256-deflate'; Models = @((Join-Path $fixtures 'suite8_secure/positive_test_cases/P_EPX_2101_02.3mf')); Options = @(); Expected = @('Accept'); Codes = @('') }
    @{ Name = 'uncompressed'; Models = @((Join-Path $fixtures 'suite8_secure/positive_test_cases/P_EPX_2104_03.3mf')); Options = @(); Expected = @('Accept'); Codes = @('') }
    @{ Name = 'encrypted-schema-error'; Models = @($generated.schema); Options = @('-V'); Expected = @('Reject'); Codes = @('invalid-unit') }
    @{ Name = 'encrypted-structure-error'; Models = @($generated.structure); Options = @('-S'); Expected = @('Reject'); Codes = @('ER_090') }
    @{ Name = 'list-reload'; Models = @($positive, $generated.schema, $generated.structure); Options = @('-I'); Expected = @('Accept', 'Reject', 'Reject'); Codes = @('', 'invalid-unit', 'ER_090') }
)
$results = foreach ($case in $cases) {
    $work = Join-Path $run $case.Name
    [void](New-Item -ItemType Directory -Path $work)
    foreach ($folder in @('ztmp', 'imagetmp', 'zips')) { [void](New-Item -ItemType Directory -Path (Join-Path $work $folder)) }
    @(('pschemaLoc = "' + $schema.Replace('\', '/') + '"'), 'iErrMaxStructure = "100"', 'iErrMaxValidataion = "100"') | Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
    'pNoUUIDchk = "false"' | Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII
    $arguments = @('-cp', $classpath, 'com.qualitylogic.x3mf.StartUp', '-C')
    if ($case.Models.Count -eq 1) { $arguments += @('-F', $case.Models[0]) }
    else {
        [IO.File]::WriteAllLines((Join-Path $work 'files.txt'), [string[]]$case.Models, [Text.UTF8Encoding]::new($false))
        $arguments += @('-L', 'files.txt')
    }
    $arguments += $case.Options
    $execution = Invoke-CliProcess (Get-Command $Java).Source $arguments $work 180
    for ($i = 0; $i -lt $case.Models.Count; $i++) {
        $name = [IO.Path]::GetFileName($case.Models[$i])
        $report = Join-Path $work ('cliResults/' + [IO.Path]::ChangeExtension($name, '.xml'))
        $checked = Test-CliResult $report $name $case.Expected[$i]
        $problems = [Collections.Generic.List[string]]::new()
        if ($checked.Status -ne 'Passed') { $problems.Add($checked.Detail) }
        if ($execution.TimedOut -or $execution.ExitCode -ne 0) { $problems.Add('CLI crashed or timed out') }
        if (Test-Path -LiteralPath $report) {
            $xml = [IO.File]::ReadAllText($report)
            $html = [IO.File]::ReadAllText([IO.Path]::ChangeExtension($report, '.html'))
            $code = $case.Codes[$i]
            if ($code -and ($xml -notmatch [regex]::Escape($code) -or $html -notmatch [regex]::Escape($code))) { $problems.Add("Missing decrypted-content diagnostic $code in XML or HTML") }
            if ('-S' -notin $case.Options -and $html -notmatch 'Decrypted') { $problems.Add('No schema decryption confirmation') }
        }
        [pscustomobject]@{ Suite = 'issue8'; Name = $case.Name + '/' + $name; Expected = $case.Expected[$i]; Actual = $checked.Actual; Status = $(if ($problems.Count) { 'Failed' } else { 'Passed' }); Detail = ($problems -join '; '); Report = $report }
    }
}
$work = Join-Path $run 'key-reset'
[void](New-Item -ItemType Directory -Path $work)
foreach ($folder in @('ztmp', 'imagetmp', 'zips')) { [void](New-Item -ItemType Directory -Path (Join-Path $work $folder)) }
$execution = Invoke-CliProcess (Get-Command $Java).Source @('-cp', $classpath, 'SecureContentFixtures', 'check-keys', $positive, $generated.'no-keystore') $work 60
$results += [pscustomobject]@{ Suite = 'issue8'; Name = 'key-reset'; Expected = 'Fresh package keys'; Actual = [string]$execution.ExitCode; Status = $(if ($execution.TimedOut -or $execution.ExitCode -ne 0) { 'Failed' } else { 'Passed' }); Detail = 'See key-reset/stderr.log'; Report = '' }
$results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
Write-JUnitReport $results (Join-Path $run 'junit.xml')
$results | Select-Object Name, Expected, Actual, Status, Detail | Format-Table -AutoSize
Write-Host "Reports: $run"
if (@($results | Where-Object Status -ne 'Passed').Count) { exit 1 }
