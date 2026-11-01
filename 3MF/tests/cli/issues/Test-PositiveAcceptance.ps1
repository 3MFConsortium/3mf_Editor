[CmdletBinding()]
param([string]$TestSuiteRoot, [string]$Java = 'java', [string]$Javac = 'javac',
    [ValidateRange(1, 3600)][int]$TimeoutSeconds = 1200, [string[]]$FixtureName)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Conformance.ps1')
if (-not $TestSuiteRoot) { $TestSuiteRoot = Join-Path $PSScriptRoot '../../../../../test_suites' }
$project = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$fixtures = (Resolve-Path -LiteralPath $TestSuiteRoot).Path
$run = Join-Path $project ('tests/cli/results/positive-acceptance-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
$classes = Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$sources = @(Get-ChildItem (Join-Path $project 'src') -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
$sources += '"' + (Join-Path $project 'tests/cli/ModelSchemaTest.java').Replace('\', '/') + '"'
[IO.File]::WriteAllLines((Join-Path $run 'sources.txt'), [string[]]$sources, [Text.UTF8Encoding]::new($false))
$compiled = Invoke-CliProcess (Get-Command $Javac).Source @('--release', '8', '-encoding', 'UTF-8', '-cp', (Join-Path $project 'jar/*'), '-d', $classes, ('@' + (Join-Path $run 'sources.txt'))) $run 180
if ($compiled.TimedOut -or $compiled.ExitCode -ne 0) { throw "Compilation failed: $run" }
Copy-Item -LiteralPath (Join-Path $project 'src/resources') -Destination $classes -Recurse
$classpath = $classes + [IO.Path]::PathSeparator + (Join-Path $project 'jar/*')
$core = Join-Path $fixtures 'suite3_core/positive_test_cases/P_XXX_0101_01.3mf'
$helper = Invoke-CliProcess (Get-Command $Java).Source @('-cp', $classpath, 'com.qualitylogic.x3mf.ModelSchemaTest', (Join-Path $fixtures 'Consolidated_Schema/Schema/qli_3MF.xsd'), $core) $run 60
Copy-Item (Join-Path $run 'stdout.log') (Join-Path $run 'model-schema.log')
if ($helper.TimedOut -or $helper.ExitCode -ne 0) { throw "Model-schema helper failed: $run/stderr.log" }
$names = @('P_SPX_0102_03', 'P_SXX_0102_03', 'P_SPX_0104_04', 'P_SXX_0104_04', 'P_XXX_2202_04',
    'P_EPX_0740_01', 'P_EPX_0740_02', 'P_EPX_0740_03', 'P_EPX_0740_04', 'P_EPX_0740_05', 'P_EPX_0740_06',
    'P_SPX_0909_01', 'P_SXX_0909_01', 'P_XXX_0101_01', 'P_SPX_0101_01', 'P_SXX_0101_01', 'P_XPM_0101_01')
if ($FixtureName) { $names = $FixtureName }
$models = @(Get-ChildItem $fixtures -Recurse -Filter '*.3mf' | Where-Object BaseName -in $names)
if ($models.Count -ne $names.Count) { throw 'Missing or duplicate issue fixture/control' }
Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.IO.Compression
$generated = Join-Path $run 'fixtures'
[void](New-Item -ItemType Directory -Path $generated)
$mutations = @(
    @{ Name = 'missing-production-object-uuid'; Source = 'P_SPX_0101_01'; Pattern = '(<object\b[^>]*?)\s+p:UUID="[^"]*"'; Replacement = '$1'; Expected = 'Fail'; Code = 'SC_000' }
    @{ Name = 'missing-production-item-uuid'; Source = 'P_SPX_0101_01'; Pattern = '(<item\b[^>]*?)\s+p:UUID="[^"]*"'; Replacement = '$1'; Expected = 'Fail'; Code = 'SC_000' }
    @{ Name = 'unqualified-attribute'; Source = 'P_XXX_2202_04'; Pattern = 'ww:unrecognized='; Replacement = 'unrecognized='; Expected = 'Fail'; Code = 'SC_000' }
    @{ Name = 'foreign-prefix-alias'; Source = 'P_XXX_2202_04'; Pattern = 'ww:'; Replacement = 'alias:'; Expected = 'Pass'; Code = '' }
    @{ Name = 'declared-unused-production'; Source = 'P_XXX_0101_01'; Pattern = '<model\b'; Replacement = '<model xmlns:unused="http://schemas.microsoft.com/3dmanufacturing/production/2015/06"'; Expected = 'Pass'; Code = '' }
    @{ Name = 'malformed-optional-uuid'; Source = 'P_XXX_0101_01'; Pattern = '<object\b'; Replacement = '<object xmlns:p="http://schemas.microsoft.com/3dmanufacturing/production/2015/06" p:UUID="invalid"'; Expected = 'Fail'; Code = 'SC_000' }
    @{ Name = 'thumbnail-same-basename'; Source = 'P_SPX_0102_03'; Pattern = 'P_SPX_0102_03\.model'; Replacement = '3dmodel.model'; Expected = 'Pass'; Code = '' }
    @{ Name = 'encrypted-alternative'; Source = 'P_EPX_2101_01'; Pattern = '</components>'; Replacement = '</components><pa:alternatives><pa:alternative UUID="b5315f3a-6c48-4cc5-bfaa-14c7a70837c4" modelresolution="fullres" objectid="2" path="/3D/3dmodel_encrypted.model"/></pa:alternatives>'; Expected = 'Pass'; Code = '' }
    @{ Name = 'encrypted-alternative-missing-id'; Source = 'P_EPX_2101_01'; Pattern = '</components>'; Replacement = '</components><pa:alternatives><pa:alternative UUID="b5315f3a-6c48-4cc5-bfaa-14c7a70837c4" modelresolution="fullres" objectid="999999" path="/3D/3dmodel_encrypted.model"/></pa:alternatives>'; Expected = 'Fail'; Code = 'ER_057' }
)
$generatedCases = @{}
if (-not $FixtureName) { foreach ($mutation in $mutations) {
    $source = Get-ChildItem $fixtures -Recurse -Filter ($mutation.Source + '.3mf') | Select-Object -First 1
    if (-not $source) { throw "Missing mutation source $($mutation.Source)" }
    $target = Join-Path $generated ($mutation.Name + '.3mf')
    Copy-Item $source.FullName $target
    $zip = [IO.Compression.ZipFile]::Open($target, [IO.Compression.ZipArchiveMode]::Update)
    $changed = $false
    try { foreach ($entry in @($zip.Entries)) {
        $entryName = $entry.FullName
        $stream = [IO.MemoryStream]::new()
        $input = $entry.Open()
        try { $input.CopyTo($stream); $bytes = $stream.ToArray() } finally { $input.Dispose(); $stream.Dispose() }
        if ($entryName -match '\.(model|rels|xml)$' -and $bytes.Length -gt 0 -and $bytes[0] -eq 60) {
            $text = [Text.Encoding]::UTF8.GetString($bytes)
            $updated = [regex]::Replace($text, $mutation.Pattern, $mutation.Replacement)
            if ($mutation.Name -eq 'foreign-prefix-alias') { $updated = $updated.Replace('xmlns:ww=', 'xmlns:alias=') }
            if ($mutation.Name -like 'encrypted-alternative*') { $updated = $updated.Replace('requiredextensions="sc p"', 'xmlns:pa="http://schemas.microsoft.com/3dmanufacturing/production/alternatives/2021/04" requiredextensions="sc p pa"') }
            if ($text -ne $updated) { $changed = $true; $bytes = [Text.UTF8Encoding]::new($false).GetBytes($updated) }
        }
        if ($mutation.Name -eq 'thumbnail-same-basename') { $entryName = $entryName.Replace('P_SPX_0102_03.model', '3dmodel.model') }
        $entry.Delete()
        $output = $zip.CreateEntry($entryName).Open()
        try { $output.Write($bytes, 0, $bytes.Length) } finally { $output.Dispose() }
    } } finally { $zip.Dispose() }
    if (-not $changed) { throw "Mutation did not change package: $($mutation.Name)" }
    $generatedCases[$mutation.Name] = $mutation
    $models += Get-Item $target
} }
$results = foreach ($model in $models) {
    if ($generatedCases.ContainsKey($model.BaseName)) { $suite = 0 }
    elseif ($model.FullName -match '[\\/]suite(\d+)_') { $suite = [int]$Matches[1] }
    else { throw 'Unrecognized suite' }
    $modes = @('auto')
    if ($model.BaseName -eq 'P_XXX_0101_01') { $modes += 'strict' }
    if ($model.BaseName -in @('P_SPX_0102_03', 'P_SXX_0102_03', 'P_XXX_2202_04')) { $modes += 'structure' }
    if ($model.BaseName -eq 'P_EPX_0740_01') { $modes += @('schema', 'structure', 'hidden') }
    foreach ($mode in $modes) {
        $work = Join-Path $run ($model.BaseName + '-' + $mode)
        [void](New-Item -ItemType Directory -Path $work)
        foreach ($folder in @('ztmp', 'imagetmp', 'zips')) { [void](New-Item -ItemType Directory -Path (Join-Path $work $folder)) }
        @(('pschemaLoc = "' + (Join-Path $fixtures 'Consolidated_Schema/Schema').Replace('\', '/') + '"'), 'iErrMaxStructure = "100"', 'iErrMaxValidataion = "100"') | Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
        'pNoUUIDchk = "false"' | Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII
        $options = @('-I'); if ($mode -eq 'structure') { $options = @('-S') }
        if ($mode -eq 'schema') { $options = @('-V') }
        if ($mode -eq 'hidden') { $options += '-W' }
        [IO.File]::WriteAllLines((Join-Path $work 'files.txt'), [string[]]@($model.FullName), [Text.UTF8Encoding]::new($false))
        $execution = Invoke-CliProcess (Get-Command $Java).Source (@('-Xmx2048m', '-cp', $classpath, 'com.qualitylogic.x3mf.StartUp', '-C', '-L', 'files.txt') + $options) $work $TimeoutSeconds
        $report = Join-Path $work ('cliResults/' + $model.BaseName + '.xml')
        $expected = 'Pass'; $requiredCode = ''
        if ($model.BaseName -match '^P_[SE]PX_0909_|^P_SXX_0909_') { $expected = 'Fail'; $requiredCode = 'ER_132' }
        if ($model.BaseName -match '^P_[SE]PX_0104_04|^P_SXX_0104_04') { $expected = 'Warning'; $requiredCode = 'WR_030' }
        if ($model.BaseName -like 'P_EPX_0740_*') { $expected = 'Warning'; $requiredCode = 'WR_031' }
        if ($generatedCases.ContainsKey($model.BaseName)) { $expected = $generatedCases[$model.BaseName].Expected; $requiredCode = $generatedCases[$model.BaseName].Code }
        $checked = Test-CliResult $report $model.Name $(if ($expected -eq 'Fail') { 'Reject' } else { 'Accept' })
        if ($checked.Actual -and $checked.Actual -ne $expected) { $checked.Status = 'Failed'; $checked.Detail = "Expected $expected; got $($checked.Actual). " + $checked.Detail }
        if ($execution.TimedOut -or $execution.ExitCode -ne 0) { $checked.Status = 'Error'; $checked.Detail = 'CLI crashed or timed out' }
        $codes = ''; if ((Test-Path $report) -and $checked.Actual) {
            $doc = Read-SafeXml ([IO.File]::ReadAllText($report))
            $codes = (@($doc.SelectNodes('//*[@issueID]') | ForEach-Object { $_.GetAttribute('issueID') }) | Select-Object -Unique) -join ', '
            if ($requiredCode -and $codes -notmatch [regex]::Escape($requiredCode)) { $checked.Status = 'Failed'; $checked.Detail += " Missing $requiredCode" }
            if ($codes -match 'ER_REPORT_XML') { $checked.Status = 'Failed'; $checked.Detail += ' Unexpected fallback report' }
            $html = [IO.File]::ReadAllText([IO.Path]::ChangeExtension($report, '.html'))
            $htmlDiagnostic = if ($requiredCode -eq 'SC_000') { 'cvc-|Invalid XML|Error in XML validation' } else { [regex]::Escape($requiredCode) }
            if ($requiredCode -and $html -notmatch $htmlDiagnostic) { $checked.Status = 'Failed'; $checked.Detail += " HTML lacks $requiredCode diagnostic" }
            foreach ($aggregate in Get-ChildItem (Join-Path $work 'cliResults') -Filter 'XmlAggregate*.xml') { $null = Read-SafeXml ([IO.File]::ReadAllText($aggregate.FullName)) }
        }
        [pscustomobject]@{ Suite = $suite; Name = $model.BaseName + '/' + $mode; Expected = $expected; Actual = $checked.Actual; Status = $checked.Status; Codes = $codes; Detail = $checked.Detail; Report = $report }
    }
}
$results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
if (-not $FixtureName) {
    $work = Join-Path $run 'mixed-batch'
    [void](New-Item -ItemType Directory -Path $work)
    foreach ($folder in @('ztmp', 'imagetmp', 'zips')) { [void](New-Item -ItemType Directory -Path (Join-Path $work $folder)) }
    @(('pschemaLoc = "' + (Join-Path $fixtures 'Consolidated_Schema/Schema').Replace('\', '/') + '"'), 'iErrMaxStructure = "100"', 'iErrMaxValidataion = "100"') | Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
    'pNoUUIDchk = "false"' | Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII
    $batch = @(
        @{ Path = (Join-Path $generated 'missing-production-item-uuid.3mf'); Expected = 'Fail' }
        @{ Path = ($models | Where-Object BaseName -eq 'P_EPX_0740_01' | Select-Object -First 1 -ExpandProperty FullName); Expected = 'Warning' }
        @{ Path = $core; Expected = 'Pass' }
    )
    foreach ($case in $batch) { if (-not (Test-Path -LiteralPath $case.Path)) { throw "Missing mixed-batch fixture: $($case.Path)" } }
    [IO.File]::WriteAllLines((Join-Path $work 'files.txt'), [string[]]@($batch | ForEach-Object Path), [Text.UTF8Encoding]::new($false))
    $execution = Invoke-CliProcess (Get-Command $Java).Source @('-Xmx2048m', '-cp', $classpath, 'com.qualitylogic.x3mf.StartUp', '-C', '-L', 'files.txt', '-I') $work $TimeoutSeconds
    foreach ($case in $batch) {
        $name = [IO.Path]::GetFileName($case.Path)
        $report = Join-Path $work ('cliResults/' + [IO.Path]::ChangeExtension($name, '.xml'))
        $checked = Test-CliResult $report $name $(if ($case.Expected -eq 'Fail') { 'Reject' } else { 'Accept' })
        if ($checked.Actual -ne $case.Expected) { $checked.Status = 'Failed'; $checked.Detail += ' Incorrect mixed-batch status' }
        if ($execution.TimedOut -or $execution.ExitCode -ne 0) { $checked.Status = 'Error'; $checked.Detail += ' CLI crashed or timed out' }
        $results += [pscustomobject]@{ Suite = 0; Name = 'mixed-batch/' + $name; Expected = $case.Expected; Actual = $checked.Actual; Status = $checked.Status; Codes = ''; Detail = $checked.Detail; Report = $report }
    }
    $aggregates = @(Get-ChildItem (Join-Path $work 'cliResults') -Filter 'XmlAggregate*.xml')
    if ($aggregates.Count -ne 1) { throw 'Missing mixed-batch aggregate report' }
    $aggregate = Read-SafeXml ([IO.File]::ReadAllText($aggregates[0].FullName))
    if ($aggregate.SelectNodes('//TestResult').Count -ne $batch.Count) { throw 'Incorrect mixed-batch aggregate count' }
}
$results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
Write-JUnitReport $results (Join-Path $run 'junit.xml')
$results | Select-Object Name, Actual, Status, Codes | Format-Table -AutoSize
Write-Host "Reports: $run"
if (@($results | Where-Object Status -ne 'Passed').Count) { exit 1 }
