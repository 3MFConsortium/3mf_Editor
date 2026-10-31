[CmdletBinding()]
param([string]$TestSuiteRoot, [string]$Java = 'java', [string]$Javac = 'javac')
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Conformance.ps1')
if (-not $TestSuiteRoot) { $TestSuiteRoot = Join-Path $PSScriptRoot '../../../../../test_suites' }
$project = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$fixtures = (Resolve-Path -LiteralPath $TestSuiteRoot).Path
$schema = Join-Path $fixtures 'Consolidated_Schema/Schema'
$negative = Join-Path $fixtures 'suite2_core_prod_matl/negative_test_cases/N_XPM_0421_01.3mf'
$positive = Join-Path $fixtures 'suite2_core_prod_matl/positive_test_cases/P_XPM_0101_01.3mf'
$errorModel = Join-Path $fixtures 'suite2_core_prod_matl/negative_test_cases/N_XPM_0418_01.3mf'
$run = Join-Path $project ('tests/cli/results/negative-placement-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0,8))
$classes = Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$sources = @(Get-ChildItem (Join-Path $project 'src') -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' })
[IO.File]::WriteAllLines((Join-Path $run 'sources.txt'), [string[]]$sources, [Text.UTF8Encoding]::new($false))
$compiled = Invoke-CliProcess (Get-Command $Javac).Source @('--release','8','-encoding','UTF-8','-cp',(Join-Path $project 'jar/*'),'-d',$classes,('@' + (Join-Path $run 'sources.txt'))) $run 180
Get-Content (Join-Path $run 'stdout.log'),(Join-Path $run 'stderr.log') | Set-Content (Join-Path $run 'compile.log') -Encoding UTF8
if ($compiled.TimedOut -or $compiled.ExitCode -ne 0) { throw "Compilation failed; see $run/compile.log" }
Copy-Item -LiteralPath (Join-Path $project 'src/resources') -Destination $classes -Recurse
$classpath = $classes + [IO.Path]::PathSeparator + (Join-Path $project 'jar/*')
$helper = Invoke-CliProcess (Get-Command $Javac).Source @('--release','17','-encoding','UTF-8','-cp',$classpath,'-d',$classes,(Join-Path $project 'tests/cli/StructureWarningTest.java')) $run 60
if ($helper.TimedOut -or $helper.ExitCode -ne 0) { throw "Helper compilation failed; see $run/stderr.log" }
$checks = Invoke-CliProcess (Get-Command $Java).Source @('-Djava.awt.headless=true','-cp',$classpath,'com.qualitylogic.x3mf.StructureWarningTest',$negative,$positive,$errorModel,$schema) $run 60
Get-Content (Join-Path $run 'stdout.log'),(Join-Path $run 'stderr.log') | Set-Content (Join-Path $run 'report-visibility.log') -Encoding UTF8
if ($checks.TimedOut -or $checks.ExitCode -ne 0) { throw "GUI report visibility checks failed; see $run/report-visibility.log" }
$cases = @(
    @{ Name='single-visible'; Model=$negative; List=$false; Hide=$false }
    @{ Name='single-hidden'; Model=$negative; List=$false; Hide=$true }
    @{ Name='list-visible'; Model=$negative; List=$true; Hide=$false }
    @{ Name='list-hidden'; Model=$negative; List=$true; Hide=$true }
    @{ Name='positive-single'; Model=$positive; List=$false; Hide=$false }
    @{ Name='positive-list-hidden'; Model=$positive; List=$true; Hide=$true }
)
$results = foreach ($case in $cases) {
    $work = Join-Path $run $case.Name
    [void](New-Item -ItemType Directory -Path $work)
    @('bdoWarn = "true"','pNoUUIDchk = "false"') | Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII
    @(('pschemaLoc = "' + $schema.Replace('\','/') + '"'),'iErrMaxStructure = "100"','iErrMaxValidataion = "100"') | Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
    $arguments = @('-cp',$classpath,'com.qualitylogic.x3mf.StartUp','-C','-I')
    if ($case.List) {
        [IO.File]::WriteAllLines((Join-Path $work 'files.txt'),[string[]]@($case.Model),[Text.UTF8Encoding]::new($false))
        $arguments += @('-L','files.txt')
    } else { $arguments += @('-F',$case.Model) }
    if ($case.Hide) { $arguments += '-W' }
    $execution = Invoke-CliProcess (Get-Command $Java).Source $arguments $work 60
    $name = [IO.Path]::GetFileName($case.Model)
    $report = Join-Path $work ('cliResults/' + [IO.Path]::ChangeExtension($name,'.xml'))
    $checked = Test-CliResult $report $name 'Accept'
    $problems = [Collections.Generic.List[string]]::new()
    if ($checked.Status -ne 'Passed') { $problems.Add($checked.Detail) }
    if ($execution.TimedOut -or $execution.ExitCode -ne 0) { $problems.Add('Process failed or timed out') }
    if (Test-Path -LiteralPath $report) {
        $xml = [IO.File]::ReadAllText($report)
        $html = [IO.File]::ReadAllText([IO.Path]::ChangeExtension($report,'.html'))
        if ($case.Model -eq $negative) {
            if ($checked.Actual -ne 'Warning' -or -not $xml.Contains('WR_028') -or -not $html.Contains('WR_028')) { $problems.Add('Negative placement must remain visible as Warning/WR_028 in HTML and XML') }
            if ($html.Contains('No structural errors')) { $problems.Add('False green success message') }
            if ($case.Hide -and -not $html.Contains('Warning details are hidden')) { $problems.Add('Missing hidden-warning status') }
        } elseif ($checked.Actual -ne 'Pass' -or $xml.Contains('WR_028')) { $problems.Add('Positive placement control failed') }
    }
    [pscustomobject]@{ Suite='issue10-placement'; Name=$case.Name; Expected=$(if($case.Model -eq $negative){'Warning/WR_028'}else{'Pass'}); Actual=$checked.Actual; Status=$(if($problems.Count){'Failed'}else{'Passed'}); Detail=($problems -join '; '); Report=$report }
}
$results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
Write-JUnitReport $results (Join-Path $run 'junit.xml')
$results | Format-Table Name,Expected,Actual,Status -AutoSize
Write-Host "Reports: $run"
if (@($results | Where-Object Status -ne 'Passed').Count) { exit 1 }
exit 0
