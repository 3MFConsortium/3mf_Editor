[CmdletBinding()]
param(
    [string]$TestSuiteRoot,
    [string]$Java = 'java',
    [string]$Javac = 'javac',
    [ValidateRange(10, 600)][int]$TimeoutSeconds = 90,
    [ValidateRange(512, 4096)][int]$HeapMB = 2048
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Conformance.ps1')
if (-not $TestSuiteRoot) { $TestSuiteRoot = Join-Path $PSScriptRoot '../../../../../test_suites' }
$project = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$fixtures = (Resolve-Path -LiteralPath $TestSuiteRoot).Path
$schema = Join-Path $fixtures 'Consolidated_Schema/Schema'
if (-not (Test-Path (Join-Path $schema 'qli_3MF.xsd'))) { throw 'Consolidated schema not found' }
$run = Join-Path $project ('tests/cli/results/tool-limitations-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
$classes = Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$sources = @(Get-ChildItem (Join-Path $project 'src') -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
[IO.File]::WriteAllLines((Join-Path $run 'sources.txt'), [string[]]$sources, [Text.UTF8Encoding]::new($false))
$compiled = Invoke-CliProcess (Get-Command $Javac).Source @('--release', '8', '-encoding', 'UTF-8', '-cp', (Join-Path $project 'jar/*'), '-d', $classes, ('@' + (Join-Path $run 'sources.txt'))) $run 180
Get-Content (Join-Path $run 'stdout.log'), (Join-Path $run 'stderr.log') | Set-Content (Join-Path $run 'compile.log') -Encoding UTF8
if ($compiled.TimedOut -or $compiled.ExitCode -ne 0) { throw "Compilation failed; see $run/compile.log" }
Copy-Item -LiteralPath (Join-Path $project 'src/resources') -Destination $classes -Recurse
$classpath = $classes + [IO.Path]::PathSeparator + (Join-Path $project 'jar/*')
$helper = Invoke-CliProcess (Get-Command $Javac).Source @('--release', '17', '-encoding', 'UTF-8', '-cp', $classpath, '-d', $classes, (Join-Path $project 'tests/cli/GeometryCheckTest.java')) $run 60
Get-Content (Join-Path $run 'stdout.log'), (Join-Path $run 'stderr.log') | Set-Content (Join-Path $run 'compile-helper.log') -Encoding UTF8
if ($helper.TimedOut -or $helper.ExitCode -ne 0) { throw "Test helper compilation failed; see $run/compile-helper.log" }
$unit = Invoke-CliProcess (Get-Command $Java).Source @('-cp', $classpath, 'com.qualitylogic.x3mf.GeometryCheckTest', $run) $run 60
Get-Content (Join-Path $run 'stdout.log'), (Join-Path $run 'stderr.log') | Set-Content (Join-Path $run 'geometry-checks.log') -Encoding UTF8
if ($unit.TimedOut -or $unit.ExitCode -ne 0) { throw "Geometry regression failed; see $run/geometry-checks.log" }
Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.IO.Compression
$cases = @(
    @{ Suite = 2; Folder = 'suite2_core_prod_matl/positive_test_cases'; Name = 'P_XPM_0909_04.3mf' }
    @{ Suite = 2; Folder = 'suite2_core_prod_matl/positive_test_cases'; Name = 'P_XPM_0101_01.3mf' }
)
foreach ($id in @('0416_01', '0416_02', '0416_03', '0418_01', '0421_01')) {
    $cases += @{ Suite = 2; Folder = 'suite2_core_prod_matl/negative_test_cases'; Name = "N_XPM_$id.3mf" }
}
foreach ($n in 1..6) {
    $cases += @{ Suite = 2; Folder = 'suite2_core_prod_matl/positive_test_cases'; Name = ('P_XPM_0910_{0:d2}.3mf' -f $n) }
}
foreach ($n in 1..9) {
    $cases += @{ Suite = 11; Folder = 'suite11_Displacement/Negative Tests'; Name = ('N_DPX_3314_{0:d2}.3mf' -f $n) }
}
$cases += @{ Suite = 11; Folder = 'suite11_Displacement/Positive Tests'; Name = 'P_DPX_3200_02.3mf' }
$officialCaseCount = $cases.Count
# Exercise both limit-reporting paths through the actual CLI, including -W.
$cases += @{ Suite = 2; Folder = 'suite2_core_prod_matl/positive_test_cases'; Name = 'P_XPM_0909_04.3mf'; WorkName = 'large-hidden-warnings'; ExtraArgs = @('-W') }
$schemaLimit = Join-Path $run 'schema-limit.3mf'
Copy-Item (Join-Path $fixtures 'suite2_core_prod_matl/positive_test_cases/P_XPM_0101_01.3mf') $schemaLimit
$zip = [IO.Compression.ZipFile]::Open($schemaLimit, [IO.Compression.ZipArchiveMode]::Update)
try {
    $entry = $zip.GetEntry('3D/3dmodel.model')
    $reader = [IO.StreamReader]::new($entry.Open())
    try { $text = $reader.ReadToEnd() } finally { $reader.Dispose() }
    $entry.Delete()
    $stream = $zip.CreateEntry('3D/3dmodel.model').Open()
    try {
        $bytes = [Text.UTF8Encoding]::new($false).GetBytes($text)
        $stream.Write($bytes, 0, $bytes.Length)
        $padding = [Text.Encoding]::ASCII.GetBytes((' ' * 1048576))
        [long]$remaining = 514215937 - $bytes.Length
        while ($remaining -gt 0) {
            $size = [int][Math]::Min($remaining, $padding.Length)
            $stream.Write($padding, 0, $size)
            $remaining -= $size
        }
    } finally { $stream.Dispose() }
} finally { $zip.Dispose() }
$cases += @{ Suite = 'generated'; Name = 'schema-limit.3mf'; Model = $schemaLimit }
$expectations = @{
    'P_XPM_0909_04.3mf' = @('Warning', 'WR_008')
    'N_XPM_0416_01.3mf' = @('Reject', 'ER_180')
    'N_XPM_0416_02.3mf' = @('Accept', '') # Mirroring a positive mesh is valid.
    'N_XPM_0416_03.3mf' = @('Reject', 'ER_180')
    'N_XPM_0418_01.3mf' = @('Reject', 'ER_179')
    'N_XPM_0421_01.3mf' = @('Warning', 'WR_028')
    'N_DPX_3314_01.3mf' = @('Reject', 'ER_144')
    'N_DPX_3314_02.3mf' = @('Reject', 'ER_180')
    'N_DPX_3314_03.3mf' = @('Accept', '')
    'N_DPX_3314_04.3mf' = @('Reject', 'ER_180')
    'N_DPX_3314_05.3mf' = @('Reject', 'ER_179')
    'N_DPX_3314_06.3mf' = @('Reject', 'ER_178')
    'N_DPX_3314_07.3mf' = @('Warning', 'WR_027')
    'N_DPX_3314_08.3mf' = @('Reject', 'ER_181')
    'N_DPX_3314_09.3mf' = @('Reject', 'ER_181')
    'schema-limit.3mf' = @('Reject', 'ER_182')
}
$results = [Collections.Generic.List[object]]::new()
$missing = [Collections.Generic.List[string]]::new()
foreach ($case in $cases) {
    $model = if ($case.ContainsKey('Model')) { $case.Model } else { Join-Path $fixtures ($case.Folder + '/' + $case.Name) }
    $expected = if ($case.Name.StartsWith('N_')) { 'Reject' } else { 'Accept' }
    $code = ''
    if ($expectations.ContainsKey($case.Name)) { $expected = $expectations[$case.Name][0]; $code = $expectations[$case.Name][1] }
    $row = [pscustomobject]@{
        Suite = $case.Suite; Name = $case.Name; Expected = $expected; Actual = ''; Status = 'Error'
        IssueIDs = ''; LargestPartBytes = 0L; TimedOut = $false; ExitCode = ''; Detail = ''; Report = ''
    }
    if (-not (Test-Path -LiteralPath $model)) {
        if ($case.Name -eq 'N_DPX_3314_09.3mf') {
            $missing.Add($case.Name)
            Write-Host "Unavailable official fixture: $($case.Name) (not counted as tested)"
            continue
        }
        $row.Detail = 'Fixture referenced by issue/specification is missing from checkout'
    } else {
        $zip = [IO.Compression.ZipFile]::OpenRead($model)
        try { $row.LargestPartBytes = ($zip.Entries | Measure-Object Length -Maximum).Maximum } finally { $zip.Dispose() }
        $work = Join-Path $run $(if ($case.ContainsKey('WorkName')) { $case.WorkName } else { [IO.Path]::GetFileNameWithoutExtension($case.Name) })
        [void](New-Item -ItemType Directory -Path $work)
        foreach ($folder in @('ztmp', 'imagetmp', 'zips')) { [void](New-Item -ItemType Directory -Path (Join-Path $work $folder)) }
        [IO.File]::WriteAllLines((Join-Path $work 'files.txt'), [string[]]@($model), [Text.UTF8Encoding]::new($false))
        @(('pschemaLoc = "' + $schema.Replace('\', '/') + '"'), 'iErrMaxStructure = "100"', 'iErrMaxValidataion = "100"') | Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
        'pNoUUIDchk = "false"' | Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII
        Write-Host "Checking $($case.Name)"
        $arguments = @("-Xmx${HeapMB}m", '-cp', $classpath, 'com.qualitylogic.x3mf.StartUp', '-C', '-L', 'files.txt', '-I')
        if ($case.ContainsKey('ExtraArgs')) { $arguments += $case.ExtraArgs }
        $execution = Invoke-CliProcess (Get-Command $Java).Source $arguments $work $TimeoutSeconds
        $row.TimedOut = $execution.TimedOut
        $row.ExitCode = $execution.ExitCode
        $row.Report = Join-Path $work ('cliResults/' + [IO.Path]::ChangeExtension($case.Name, '.xml'))
        $checked = Test-CliResult $row.Report $case.Name $expected
        $problems = [Collections.Generic.List[string]]::new()
        if ($expected -eq 'Warning' -and $checked.Actual -ne 'Warning') { $problems.Add("Expected Warning; got $($checked.Actual)") }
        if ($checked.Status -ne 'Passed') { $problems.Add($checked.Detail) }
        $row.Actual = $checked.Actual; $row.Status = $checked.Status; $row.Detail = $checked.Detail
        if ($execution.TimedOut -or $execution.ExitCode -ne 0) {
            $row.Status = 'Error'
            $row.Detail = "CLI process timedOut=$($execution.TimedOut), exitCode=$($execution.ExitCode). " + $row.Detail
        }
        if (Test-Path -LiteralPath $row.Report) {
            $doc = Read-SafeXml ([IO.File]::ReadAllText($row.Report))
            $row.IssueIDs = (@($doc.SelectNodes('/TestResult/Issue | /TestResult/PrimaryIssue') | ForEach-Object { $_.GetAttribute('issueID') }) | Sort-Object -Unique) -join ', '
            $htmlPath = [IO.Path]::ChangeExtension($row.Report, '.html')
            $html = if (Test-Path -LiteralPath $htmlPath) { [IO.File]::ReadAllText($htmlPath) } else { '' }
            if ($code -and (-not $row.IssueIDs.Contains($code) -or -not $html.Contains($code))) { $problems.Add("Missing $code in XML or HTML") }
            if ($case.Name -in @('P_XPM_0909_04.3mf', 'schema-limit.3mf') -and ($row.IssueIDs -match 'ER_015|ER_027' -or $html -match 'ER_015|ER_027')) { $problems.Add('Spurious missing-file diagnostic for oversized part') }
        }
        if ($problems.Count -gt 0) { if ($row.Status -ne 'Error') { $row.Status = 'Failed' }; $row.Detail += ($problems -join '; ') }
    }
    $results.Add($row)
    $results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
}
Write-JUnitReport $results (Join-Path $run 'junit.xml')
[ordered]@{
    Completed = $true; TestSuiteRoot = $fixtures; SchemaRoot = $schema; HeapMB = $HeapMB; TimeoutSeconds = $TimeoutSeconds
    SpecificationSHA256 = (Get-FileHash (Join-Path $fixtures 'test_specification/3MF_Test_Specification_v2_4_1.docx')).Hash
    Cases = $results.Count; Passed = @($results | Where-Object Status -eq 'Passed').Count
    UnavailableFixtures = @($missing); RequestedOfficialCases = $officialCaseCount
    Failed = @($results | Where-Object Status -eq 'Failed').Count; Errors = @($results | Where-Object Status -eq 'Error').Count
} | ConvertTo-Json | Set-Content (Join-Path $run 'summary.json') -Encoding UTF8
$results | Format-Table Name, Expected, Actual, Status, IssueIDs -AutoSize
Write-Host "Reports: $run"
# Current specification policy is asserted explicitly, including valid mirrors and SHOULD warnings.
if (@($results | Where-Object Status -ne 'Passed').Count) { exit 1 }
exit 0
