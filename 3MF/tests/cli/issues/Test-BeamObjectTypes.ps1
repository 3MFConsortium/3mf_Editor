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
$run = Join-Path $project ('tests/cli/results/beam-object-types-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
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
$generated = Join-Path $run 'fixtures'
[void](New-Item -ItemType Directory -Path $generated)

function New-TypeCase([string]$Type, [bool]$WithLattice = $true) {
    $target = Join-Path $generated ($Type + '-' + $WithLattice + '.3mf')
    Copy-Item (Join-Path $fixtures 'suite7_beam/negative_test_cases/N_BXX_2503_02.3mf') $target
    $zip = [IO.Compression.ZipFile]::Open($target, [IO.Compression.ZipArchiveMode]::Update)
    try {
        foreach ($entry in @($zip.Entries | Where-Object FullName -like '*.model')) {
            $name = $entry.FullName
            $reader = [IO.StreamReader]::new($entry.Open())
            try { $doc = Read-SafeXml ($reader.ReadToEnd()) } finally { $reader.Dispose() }
            foreach ($lattice in @($doc.SelectNodes('//*[local-name()="beamlattice"]'))) {
                $object = $lattice.ParentNode.ParentNode
                if ($Type -eq 'omitted') { $object.RemoveAttribute('type') }
                else { $object.SetAttribute('type', $Type) }
                if (-not $WithLattice) { [void]$lattice.ParentNode.RemoveChild($lattice) }
            }
            $entry.Delete()
            $writer = [IO.StreamWriter]::new($zip.CreateEntry($name).Open(), [Text.UTF8Encoding]::new($false))
            try { $writer.Write($doc.OuterXml) } finally { $writer.Dispose() }
        }
    } finally { $zip.Dispose() }
    return $target
}
$cases = @(
    @{ Model = (Join-Path $fixtures 'suite7_beam/negative_test_cases/N_BXX_2503_02.3mf'); Expected = 'Reject'; Diagnostic = $true }
    foreach ($type in @('model', 'solidsupport', 'omitted', 'support', 'other')) {
        $invalid = $type -in @('support', 'other')
        @{ Model = (New-TypeCase $type); Expected = $(if ($invalid) { 'Reject' } else { 'Accept' }); Diagnostic = $invalid }
    }
    # A non-model object without a beam lattice must not trigger this rule.
    @{ Model = (New-TypeCase 'support' $false); Expected = 'Accept'; Diagnostic = $false }
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
    $checked = Test-CliResult $report $name $case.Expected
    $problems = [Collections.Generic.List[string]]::new()
    if ($checked.Status -ne 'Passed') { $problems.Add($checked.Detail) }
    if ($execution.TimedOut -or $execution.ExitCode -ne 0) { $problems.Add('CLI crashed or timed out') }
    if (Test-Path -LiteralPath $report) {
        $doc = Read-SafeXml ([IO.File]::ReadAllText($report))
        $issues = @($doc.DocumentElement.SelectNodes('./Issue | ./PrimaryIssue') | Where-Object { $_.GetAttribute('issueID') -eq 'ER_177' })
        $html = [IO.File]::ReadAllText([IO.Path]::ChangeExtension($report, '.html'))
        if ($case.Diagnostic) {
            if (-not $issues.Count -or $html -notmatch '(?s)ER_177:.*only model or solidsupport') { $problems.Add('Missing beam object-type diagnostic in XML or HTML') }
            if ($doc.DocumentElement.FirstChild.GetAttribute('issueID') -ne 'ER_177') { $problems.Add('Object-type error was not selected as primary issue') }
            if (@($issues | Where-Object { $_.GetAttribute('type') -ne 'invalid object type' -or $_.InnerText -notmatch 'object id:.*only model or solidsupport' }).Count) { $problems.Add('Incorrect object-type diagnostic') }
        } elseif ($issues.Count -or $html -match 'ER_177') { $problems.Add('Spurious beam object-type diagnostic') }
    }
    [pscustomobject]@{ Suite = 'issue7'; Name = $name; Expected = $case.Expected; Actual = $checked.Actual; Status = $(if ($problems.Count) { 'Failed' } else { 'Passed' }); Detail = ($problems -join '; '); Report = $report }
}
$results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
Write-JUnitReport $results (Join-Path $run 'junit.xml')
$results | Select-Object Name, Expected, Actual, Status, Detail | Format-Table -AutoSize
Write-Host "Reports: $run"
if (@($results | Where-Object Status -ne 'Passed').Count) { exit 1 }
