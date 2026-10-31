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
$run = Join-Path $project ('tests/cli/results/content-type-overrides-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
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

function New-OverrideCase([string]$Name, [string]$Mode) {
    $target = Join-Path $generated ($Name + '.3mf')
    Copy-Item (Join-Path $fixtures 'suite2_core_prod_matl/negative_test_cases/N_XPM_0205_02.3mf') $target
    $zip = [IO.Compression.ZipFile]::Open($target, [IO.Compression.ZipArchiveMode]::Update)
    try {
        $entry = $zip.GetEntry('[Content_Types].xml')
        $reader = [IO.StreamReader]::new($entry.Open())
        try { $doc = Read-SafeXml ($reader.ReadToEnd()) } finally { $reader.Dispose() }
        $overrides = @($doc.SelectNodes('//*[local-name()="Override"]'))
        if ($overrides.Count -ne 2) { throw 'Expected two overrides in the source fixture' }
        $first = $overrides[0]; $second = $overrides[1]
        switch ($Mode) {
            'single' { [void]$second.ParentNode.RemoveChild($second) }
            'case' { $second.SetAttribute('PartName', $first.GetAttribute('PartName').ToUpperInvariant()) }
            'conflict' { $second.SetAttribute('ContentType', 'application/xml') }
            'distinct' {
                # The same content type on different parts is legal.
                $source = @($zip.Entries | Where-Object FullName -like '*.png')[0]
                $first.SetAttribute('PartName', '/' + $source.FullName)
                $first.SetAttribute('ContentType', 'image/png')
                $second.SetAttribute('PartName', '/Thumbnails/other.png')
                $second.SetAttribute('ContentType', 'image/png')
                $default = $doc.CreateElement('Default', $doc.DocumentElement.NamespaceURI)
                $default.SetAttribute('Extension', 'model')
                $default.SetAttribute('ContentType', 'application/vnd.ms-package.3dmanufacturing-3dmodel+xml')
                [void]$doc.DocumentElement.PrependChild($default)
                $stream = $source.Open(); $copy = $zip.CreateEntry('Thumbnails/other.png').Open()
                try { $stream.CopyTo($copy) } finally { $copy.Dispose(); $stream.Dispose() }
            }
            'default-and-override' {
                [void]$second.ParentNode.RemoveChild($second)
                $default = $doc.CreateElement('Default', $doc.DocumentElement.NamespaceURI)
                $default.SetAttribute('Extension', 'model')
                $default.SetAttribute('ContentType', $first.GetAttribute('ContentType'))
                [void]$doc.DocumentElement.PrependChild($default)
            }
        }
        $entry.Delete()
        $writer = [IO.StreamWriter]::new($zip.CreateEntry('[Content_Types].xml').Open(), [Text.UTF8Encoding]::new($false))
        try { $writer.Write($doc.OuterXml) } finally { $writer.Dispose() }
    } finally { $zip.Dispose() }
    return $target
}
$cases = @(
    foreach ($suite in 1..6) {
        $directory = @(Get-ChildItem $fixtures -Directory | Where-Object Name -like "suite${suite}_*")[0]
        foreach ($model in Get-ChildItem (Join-Path $directory.FullName 'negative_test_cases') -Filter '*0205_0?.3mf') {
            @{ Model = $model.FullName; Expected = 'Reject'; Part = $(if ($model.BaseName.EndsWith('_02')) { 'PartName' } else { 'extension' }) }
        }
    }
    foreach ($mode in @('single', 'case', 'conflict', 'distinct', 'default-and-override')) {
        @{ Model = (New-OverrideCase $mode $mode); Expected = $(if ($mode -in @('case', 'conflict')) { 'Reject' } else { 'Accept' }); Part = 'PartName' }
    }
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
        $duplicates = @($doc.DocumentElement.SelectNodes('./Issue | ./PrimaryIssue') | Where-Object { $_.GetAttribute('issueID') -eq 'ER_108' })
        $html = [IO.File]::ReadAllText([IO.Path]::ChangeExtension($report, '.html'))
        if ($case.Expected -eq 'Reject') {
            if (-not @($duplicates | Where-Object InnerText -Match $case.Part).Count -or $html -notmatch ('(?s)ER_108:.*' + $case.Part)) { $problems.Add('Missing duplicate content-type diagnostic in XML or HTML') }
        } elseif ($duplicates.Count -or $html -match 'ER_108') { $problems.Add('Spurious duplicate content-type diagnostic') }
    }
    [pscustomobject]@{ Suite = 'issue6'; Name = $name; Expected = $case.Expected; Actual = $checked.Actual; Status = $(if ($problems.Count) { 'Failed' } else { 'Passed' }); Detail = ($problems -join '; '); Report = $report }
}
$results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
Write-JUnitReport $results (Join-Path $run 'junit.xml')
$results | Select-Object Name, Expected, Actual, Status, Detail | Format-Table -AutoSize
Write-Host "Reports: $run"
if (@($results | Where-Object Status -ne 'Passed').Count) { exit 1 }
