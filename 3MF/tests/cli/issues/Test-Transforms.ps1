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
$run = Join-Path $PSScriptRoot ('../results/transforms-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
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

# Exercise both item and component transforms with and without slice references.
function New-TransformCase([string]$Name, [bool]$Slices, [string]$NodeName, [string]$Transform, [bool]$OtherError = $false) {
    $target = Join-Path $generated ($Name + '.3mf')
    Copy-Item (Join-Path $fixtures 'suite4_core_slice/positive_test_cases/P_SXX_1511_01.3mf') $target
    $zip = [IO.Compression.ZipFile]::Open($target, [IO.Compression.ZipArchiveMode]::Update)
    try {
        $entry = $zip.GetEntry('3D/3dmodel.model')
        $reader = [IO.StreamReader]::new($entry.Open())
        try { $doc = Read-SafeXml ($reader.ReadToEnd()) } finally { $reader.Dispose() }
        $obj = $doc.SelectSingleNode('//*[local-name()="object"]')
        if (-not $Slices) {
            $obj.RemoveAttribute('slicestackid', 'http://schemas.microsoft.com/3dmanufacturing/slice/2015/07')
            $obj.RemoveAttribute('meshresolution', 'http://schemas.microsoft.com/3dmanufacturing/slice/2015/07')
            foreach ($stack in @($doc.SelectNodes('//*[local-name()="slicestack"]'))) {
                [void]$stack.ParentNode.RemoveChild($stack)
            }
        }
        $item = $doc.SelectSingleNode('//*[local-name()="item"]')
        if ($NodeName -eq 'component') {
            $ns = $doc.DocumentElement.NamespaceURI
            $wrapper = $doc.CreateElement('object', $ns)
            $wrapper.SetAttribute('id', '999')
            $components = $doc.CreateElement('components', $ns)
            $node = $doc.CreateElement('component', $ns)
            $node.SetAttribute('objectid', $obj.GetAttribute('id'))
            [void]$components.AppendChild($node)
            [void]$wrapper.AppendChild($components)
            [void]$obj.ParentNode.AppendChild($wrapper)
            $item.SetAttribute('objectid', '999')
            $item.RemoveAttribute('transform')
        } else { $node = $item }
        # The consolidated schema requires Production UUIDs even on older slice fixtures.
        foreach ($element in $doc.SelectNodes('//*[local-name()="object" or local-name()="item" or local-name()="build" or local-name()="component"]')) {
            $attr = $doc.CreateAttribute('p', 'UUID', 'http://schemas.microsoft.com/3dmanufacturing/production/2015/06')
            $attr.Value = [guid]::NewGuid().ToString()
            [void]$element.Attributes.Append($attr)
        }
        if ($OtherError) {
            $doc.SelectSingleNode('//*[local-name()="build"]').RemoveAttribute('UUID', 'http://schemas.microsoft.com/3dmanufacturing/production/2015/06')
        }
        $node.SetAttribute('transform', $Transform)
        $entry.Delete()
        $writer = [IO.StreamWriter]::new($zip.CreateEntry('3D/3dmodel.model').Open(), [Text.UTF8Encoding]::new($false))
        try { $writer.Write($doc.OuterXml) } finally { $writer.Dispose() }
    } finally { $zip.Dispose() }
    return $target
}
$cases = @(
    foreach ($iteration in 1..11) {
        @{ Model = (Join-Path $positive ('P_XPX_0720_{0:d2}.3mf' -f $iteration)); Expected = 'Accept'; Code = ''; NoSlices = $true }
    }
    foreach ($node in @('item', 'component')) {
        foreach ($slices in @($false, $true)) {
            foreach ($matrix in @(
                @{ Name = 'identity'; Value = '1 0 0 0 1 0 0 0 1 25 25 25'; Code = '' },
                @{ Name = 'scale'; Value = '0.5 0 0 0 0.5 0 0 0 0.5 25 25 25'; Code = 'ER_075' },
                @{ Name = 'rotate-x'; Value = '1 0 0 0 0 1 0 -1 0 25 25 25'; Code = 'ER_072' }
            )) {
                $code = if ($slices) { $matrix.Code } else { '' }
                $model = New-TransformCase "$node-slices-$slices-$($matrix.Name)" $slices $node $matrix.Value
                @{ Model = $model; Expected = $(if ($code) { 'Fail' } else { 'Accept' }); Code = $code; NoSlices = (-not $slices) }
            }
        }
        # An unrelated error used to expose the spurious XML transform diagnostics.
        $model = New-TransformCase "$node-no-slices-unrelated-error" $false $node '0.5 0 0 0 0.5 0 0 0 0.5 25 25 25' $true
        @{ Model = $model; Expected = 'Fail'; Code = 'ER_039'; NoSlices = $true }
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
        if ($case.NoSlices -or $case.Expected -eq 'Accept') {
            $html = [IO.File]::ReadAllText([IO.Path]::ChangeExtension($report, '.html'))
            if ($html -match 'ER_07[1-6]' -or @($issues | Where-Object { $_.GetAttribute('issueID') -match '^ER_07[1-6]$' }).Count) { $problems.Add('Spurious slice transform diagnostic') }
        }
    }
    [pscustomobject]@{ Suite = 'issue4'; Name = $name; Expected = $case.Expected; Actual = $checked.Actual; Status = $(if ($problems.Count) { 'Failed' } else { 'Passed' }); Detail = ($problems -join '; '); Report = $report }
}
$results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
Write-JUnitReport $results (Join-Path $run 'junit.xml')
$results | Select-Object Name, Expected, Actual, Status, Detail | Format-Table -AutoSize
Write-Host "Reports: $run"
if (@($results | Where-Object Status -ne 'Passed').Count) { exit 1 }
