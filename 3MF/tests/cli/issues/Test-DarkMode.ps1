[CmdletBinding()]
param([string]$Java = 'java', [string]$Javac = 'javac', [switch]$EditorSmoke, [string]$TestSuiteRoot)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Conformance.ps1')
$project = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$run = Join-Path $project ('tests/cli/results/dark-mode-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
$classes = Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$sources = @(Get-ChildItem (Join-Path $project 'src') -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
$sources += '"' + (Join-Path $project 'tests/src/DarkModeTest.java').Replace('\', '/') + '"'
[IO.File]::WriteAllLines((Join-Path $run 'sources.txt'), [string[]]$sources, [Text.UTF8Encoding]::new($false))
$compiled = Invoke-CliProcess (Get-Command $Javac).Source @('-encoding', 'UTF-8', '-cp', (Join-Path $project 'jar/*'), '-d', $classes, ('@' + (Join-Path $run 'sources.txt'))) $run 180
if ($compiled.TimedOut -or $compiled.ExitCode -ne 0) { throw "Compilation failed; see $run" }
Copy-Item -LiteralPath (Join-Path $project 'src/resources') -Destination $classes -Recurse
$classpath = $classes + [IO.Path]::PathSeparator + (Join-Path $project 'jar/*')
$options = @('-Djava.awt.headless=true', '-cp', $classpath, 'com.qualitylogic.x3mf.DarkModeTest')
if ($EditorSmoke) { $options = @('-Djava.awt.headless=false', '-cp', $classpath, 'com.qualitylogic.x3mf.DarkModeTest', 'editor') }
$result = Invoke-CliProcess (Get-Command $Java).Source $options $run 60
Get-Content (Join-Path $run 'stdout.log')
if ($result.TimedOut -or $result.ExitCode -ne 0) { throw "Dark mode regression failed; see $run" }
if ($TestSuiteRoot) {
    $fixtures = (Resolve-Path -LiteralPath $TestSuiteRoot).Path
    $schema = Join-Path $fixtures 'Consolidated_Schema/Schema'
    $checks = foreach ($mode in @('false', 'true')) {
        foreach ($kind in @('positive', 'negative')) {
            $work = Join-Path $run ($mode + '-' + $kind)
            [void](New-Item -ItemType Directory -Path $work -Force)
            $model = if ($kind -eq 'positive') { Join-Path $fixtures 'suite7_beam/positive_test_cases/P_BXX_2001_01.3mf' }
                else { Join-Path $fixtures 'suite7_beam/negative_test_cases/N_BXX_2503_02.3mf' }
            @('pNoUUIDchk = "false"', ('darkMode = "' + $mode + '"')) | Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII
            ('pschemaLoc = "' + $schema.Replace('\', '/') + '"') | Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
            $execution = Invoke-CliProcess (Get-Command $Java).Source @('-cp', $classpath, 'com.qualitylogic.x3mf.StartUp', '-C', '-F', $model, '-S') $work 60
            $name = [IO.Path]::GetFileName($model)
            $expected = if ($kind -eq 'positive') { 'Accept' } else { 'Reject' }
            $checked = Test-CliResult (Join-Path $work ('cliResults/' + [IO.Path]::ChangeExtension($name, '.xml'))) $name $expected
            if ($execution.TimedOut -or $execution.ExitCode -ne 0 -or $checked.Status -ne 'Passed') { throw "CLI preference check failed: $mode/$kind; see $work" }
            $html = [IO.File]::ReadAllText((Join-Path $work ('cliResults/' + [IO.Path]::ChangeExtension($name, '.html'))))
            if ($html -match '#ff9292|#1e2024|#e1e4e8') { throw "GUI palette leaked into CLI: $work" }
            [pscustomobject]@{ DarkMode = $mode; Case = $kind; Actual = $checked.Actual; Status = $checked.Status }
        }
    }
    $checks | Export-Csv (Join-Path $run 'cli-preference-results.csv') -NoTypeInformation
    $checks | Format-Table -AutoSize
}
Write-Host "Reports: $run"
