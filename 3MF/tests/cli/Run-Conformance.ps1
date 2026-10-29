[CmdletBinding()]
param(
    [string]$TestSuiteRoot,
    [string]$Specification,
    [string]$SchemaRoot,
    [string]$Jar,
    [string]$Java = 'java',
    [string]$Javac = 'javac',
    [ValidateRange(1, 11)][int[]]$Suite = (1..11),
    [ValidateRange(1, 100)][int]$BatchSize = 1,
    [ValidateRange(1, 3600)][int]$TimeoutSeconds = 180,
    [string]$OutputRoot,
    [switch]$StrictWarnings,
    [switch]$InventoryOnly
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'Conformance.ps1')
try {
    if (-not $TestSuiteRoot) { $TestSuiteRoot = Join-Path $PSScriptRoot '../../../../test_suites' }
    if (-not $OutputRoot) { $OutputRoot = Join-Path $PSScriptRoot 'results' }
    $project = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
    $TestSuiteRoot = (Resolve-Path -LiteralPath $TestSuiteRoot).Path
    if (-not $Specification) { $Specification = Join-Path $TestSuiteRoot 'test_specification/3MF_Test_Specification_v2_4_1.docx' }
    if (-not $SchemaRoot) { $SchemaRoot = Join-Path $TestSuiteRoot 'Consolidated_Schema/Schema' }
    $Specification = (Resolve-Path -LiteralPath $Specification).Path
    $SchemaRoot = (Resolve-Path -LiteralPath $SchemaRoot).Path
    foreach ($required in @('qli_3MF.xsd', 'opc-contentTypes.xsd', 'opc-relationships.xsd', 'qli_SecureContent.xsd')) {
        if (-not (Test-Path -LiteralPath (Join-Path $SchemaRoot $required))) { throw "Missing schema: $required" }
    }
    $specCases = Get-SpecificationCases $Specification
    $cases = @(Get-ModelCases $TestSuiteRoot $Suite)
    if ($cases.Count -eq 0) { throw 'No test models discovered' }
    foreach ($number in $Suite) {
        if (@($cases | Where-Object Suite -eq $number).Count -eq 0) { throw "No models discovered for requested suite $number" }
    }
    $run = Join-Path ([IO.Path]::GetFullPath($OutputRoot)) ((Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
    [void](New-Item -ItemType Directory -Path $run)
    $cases | Export-Csv (Join-Path $run 'inventory.csv') -NoTypeInformation -Encoding UTF8
    $present = @{}; foreach ($case in $cases) { $present[$case.SpecKey] = $true }
    $missing = @($specCases.Keys | Sort-Object | Where-Object { $specCases[$_].Suite -in $Suite -and -not $present.ContainsKey($_) })
    $extra = @($cases | Where-Object { -not $specCases.ContainsKey($_.SpecKey) } | Select-Object Suite, Name, SpecKey)
    $metadata = [ordered]@{
        SpecificationUrl = 'https://github.com/3MFConsortium/test_suites/blob/master/test_specification/3MF_Test_Specification_v2_4_1.docx'
        Specification = $Specification; SpecificationSHA256 = (Get-FileHash $Specification -Algorithm SHA256).Hash
        TestSuiteRoot = $TestSuiteRoot; SchemaRoot = $SchemaRoot; SelectedSuites = $Suite
        Models = $cases.Count; MissingFromCheckout = $missing; NotInAppendixC = $extra
        Scope = 'CLI schema/structure acceptance only. Rendering, printing, alternative selection, colors and geometry appearance require manual verification.'
        StrictWarnings = [bool]$StrictWarnings; InventoryOnly = [bool]$InventoryOnly
        BatchSize = $BatchSize; TimeoutSeconds = $TimeoutSeconds; UUIDChecksEnabled = $true
    }
    $metadata | ConvertTo-Json -Depth 8 | Set-Content (Join-Path $run 'summary.json') -Encoding UTF8
    Write-Host "Discovered $($cases.Count) models; $($missing.Count) missing Appendix C entries; $($extra.Count) additional models."
    Write-Host "Reports: $run"
    if ($InventoryOnly) { if ($missing.Count) { exit 1 }; exit 0 }
    $javaPath = (Get-Command $Java -ErrorAction Stop).Source
    $metadata['JavaExecutable'] = $javaPath
    $metadata['SchemaHashes'] = @(Get-ChildItem -LiteralPath $SchemaRoot -Filter '*.xsd' | Sort-Object Name | ForEach-Object {
        [pscustomobject]@{ Name = $_.Name; SHA256 = (Get-FileHash $_.FullName -Algorithm SHA256).Hash }
    })
    if ($Jar) {
        $Jar = (Resolve-Path -LiteralPath $Jar).Path
        $launch = @('-jar', $Jar)
        $metadata['JarSHA256'] = (Get-FileHash $Jar -Algorithm SHA256).Hash
    } else {
        $classes = Join-Path $run 'classes'
        [void](New-Item -ItemType Directory -Path $classes)
        $sources = @(Get-ChildItem (Join-Path $project 'src') -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
        $sourceList = Join-Path $run 'sources.txt'
        [IO.File]::WriteAllLines($sourceList, [string[]]$sources, [Text.UTF8Encoding]::new($false))
        $compiler = (Get-Command $Javac -ErrorAction Stop).Source
        $compiled = Invoke-CliProcess $compiler @('-encoding', 'UTF-8', '-cp', (Join-Path $project 'jar/*'), '-d', $classes, "@$sourceList") $run 180
        Get-Content (Join-Path $run 'stdout.log'), (Join-Path $run 'stderr.log') | Set-Content (Join-Path $run 'compile.log') -Encoding UTF8
        if ($compiled.TimedOut -or $compiled.ExitCode -ne 0) { throw "Compilation failed; see $run/compile.log" }
        Copy-Item -LiteralPath (Join-Path $project 'src/resources') -Destination $classes -Recurse
        $launch = @('-cp', ($classes + [IO.Path]::PathSeparator + (Join-Path $project 'jar/*')), 'com.qualitylogic.x3mf.StartUp')
    }
    $results = [Collections.Generic.List[object]]::new()
    foreach ($key in $missing) {
        $entry = $specCases[$key]
        $results.Add([pscustomobject]@{ Suite = $entry.Suite; Name = $key; Expected = 'Present'; Actual = 'Missing'; Status = 'Error'; Detail = 'Appendix C model missing from checkout'; Report = '' })
    }
    $batchNumber = 0
    foreach ($group in $cases | Group-Object Suite) {
        $models = @($group.Group)
        for ($offset = 0; $offset -lt $models.Count; $offset += $BatchSize) {
            $batchNumber++
            $batch = @($models[$offset..([Math]::Min($offset + $BatchSize - 1, $models.Count - 1))])
            $work = Join-Path $run ('suite{0}-batch{1:d3}' -f $group.Name, $batchNumber)
            [void](New-Item -ItemType Directory -Path $work)
            foreach ($folder in @('ztmp', 'imagetmp', 'zips')) { [void](New-Item -ItemType Directory -Path (Join-Path $work $folder)) }
            [IO.File]::WriteAllLines((Join-Path $work 'files.txt'), [string[]]@($batch | ForEach-Object Path), [Text.UTF8Encoding]::new($false))
            $settings = @(
                ('pschemaLoc = "' + $SchemaRoot.Replace('\', '/') + '"')
                'iErrMaxStructure = "100"'
                'iErrMaxValidataion = "100"'
            )
            $settings | Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
            'pNoUUIDchk = "false"' | Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII
            Write-Host "Suite $($group.Name): $($offset + 1)-$($offset + $batch.Count)/$($models.Count)"
            $execution = Invoke-CliProcess $javaPath ($launch + @('-C', '-L', 'files.txt', '-I')) $work $TimeoutSeconds
            foreach ($case in $batch) {
                $xml = Join-Path $work ('cliResults/' + [IO.Path]::ChangeExtension($case.Name, '.xml'))
                $checked = Test-CliResult $xml $case.Name $case.Expected ([bool]$StrictWarnings)
                # A failed process is always an infrastructure error, even if it wrote some XML.
                if ($execution.TimedOut -or $execution.ExitCode -ne 0) {
                    $checked.Status = 'Error'
                    $checked.Detail = "CLI process timedOut=$($execution.TimedOut), exitCode=$($execution.ExitCode). " + $checked.Detail
                }
                $results.Add([pscustomobject]@{ Suite = $case.Suite; Name = $case.Name; Expected = $case.Expected; Actual = $checked.Actual; Status = $checked.Status; Detail = $checked.Detail; Report = $xml })
            }
            # Save progress after every batch, so interruptions retain completed results.
            $results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
        }
    }
    Write-JUnitReport $results (Join-Path $run 'junit.xml')
    $metadata['Passed'] = @($results | Where-Object Status -eq 'Passed').Count
    $metadata['Failed'] = @($results | Where-Object Status -eq 'Failed').Count
    $metadata['Errors'] = @($results | Where-Object Status -eq 'Error').Count
    $metadata['Completed'] = $true
    $metadata | ConvertTo-Json -Depth 8 | Set-Content (Join-Path $run 'summary.json') -Encoding UTF8
    Write-Host "Passed: $($metadata.Passed); failed: $($metadata.Failed); errors: $($metadata.Errors). Reports: $run"
    if ($metadata.Failed -or $metadata.Errors) { exit 1 }
    exit 0
} catch {
    Write-Error $_ -ErrorAction Continue
    exit 2
}
