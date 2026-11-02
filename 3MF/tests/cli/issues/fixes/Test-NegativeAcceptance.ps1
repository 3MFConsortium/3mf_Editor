[CmdletBinding()]
param([string]$TestSuiteRoot, [string]$Java = 'java', [string]$Javac = 'javac')
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../../Conformance.ps1')
if (-not $TestSuiteRoot) { $TestSuiteRoot = Join-Path $PSScriptRoot '../../../../../../test_suites' }
$project = (Resolve-Path (Join-Path $PSScriptRoot '../../../..')).Path
$fixtures = (Resolve-Path -LiteralPath $TestSuiteRoot).Path
$schema = Join-Path $fixtures 'Consolidated_Schema/Schema'
$run = Join-Path $project ('tests/cli/results/negative-acceptance-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0,8))
$classes = Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$sources = @(Get-ChildItem (Join-Path $project 'src') -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' })
$sources += '"' + (Join-Path $project 'tests/cli/src/ModelRulesTest.java').Replace('\','/') + '"'
$sources += '"' + (Join-Path $project 'tests/cli/src/ModelSchemaTest.java').Replace('\','/') + '"'
$sources += '"' + (Join-Path $project 'tests/cli/src/AlternativePolicyTest.java').Replace('\','/') + '"'
[IO.File]::WriteAllLines((Join-Path $run 'sources.txt'), [string[]]$sources, [Text.UTF8Encoding]::new($false))
$compiled = Invoke-CliProcess (Get-Command $Javac).Source @('--release','8','-encoding','UTF-8','-cp',(Join-Path $project 'jar/*'),'-d',$classes,('@' + (Join-Path $run 'sources.txt'))) $run 180
Get-Content (Join-Path $run 'stdout.log'),(Join-Path $run 'stderr.log') | Set-Content (Join-Path $run 'compile.log') -Encoding UTF8
if ($compiled.TimedOut -or $compiled.ExitCode -ne 0) { throw "Compilation failed: $run/compile.log" }
Copy-Item -LiteralPath (Join-Path $project 'src/resources') -Destination $classes -Recurse
$classpath = $classes + [IO.Path]::PathSeparator + (Join-Path $project 'jar/*')
$helper = Invoke-CliProcess (Get-Command $Java).Source @('-cp',$classpath,'com.qualitylogic.x3mf.ModelRulesTest') $run 60
Get-Content (Join-Path $run 'stdout.log'),(Join-Path $run 'stderr.log') | Set-Content (Join-Path $run 'semantic-controls.log') -Encoding UTF8
if ($helper.TimedOut -or $helper.ExitCode -ne 0) { throw "Semantic controls failed: $run/semantic-controls.log" }
$core = Join-Path $fixtures 'suite3_core/positive_test_cases/P_XXX_0101_01.3mf'
$schemaHelper = Invoke-CliProcess (Get-Command $Java).Source @('-cp',$classpath,'com.qualitylogic.x3mf.ModelSchemaTest',(Join-Path $schema 'qli_3MF.xsd'),$core) $run 60
Get-Content (Join-Path $run 'stdout.log'),(Join-Path $run 'stderr.log') | Set-Content (Join-Path $run 'schema-controls.log') -Encoding UTF8
if ($schemaHelper.TimedOut -or $schemaHelper.ExitCode -ne 0) { throw "Schema controls failed: $run/schema-controls.log" }
$alternativeHelper = Invoke-CliProcess (Get-Command $Java).Source @('-cp',$classpath,'com.qualitylogic.x3mf.AlternativePolicyTest',$run) $run 60
Get-Content (Join-Path $run 'stdout.log'),(Join-Path $run 'stderr.log') | Set-Content (Join-Path $run 'alternative-controls.log') -Encoding UTF8
if ($alternativeHelper.TimedOut -or $alternativeHelper.ExitCode -ne 0) { throw "Alternative eligibility controls failed: $run/alternative-controls.log" }
$ids = @('0204_01','0207_01','0402_03','0404_03','0405_03','0410_03','0410_04','0411_01','0412_02','0412_03','0412_04','0416_01','0416_02','0416_03','0420_01','0421_01','0427_01','0428_01','1605_01','1606_01','1607_01','1608_01','1609_01','1609_02','1612_01','2505_03','2606_01','3100_07','3102_01','3104_05','3310_17','3314_02','3314_03','3314_04','3314_07')
$cases = @(Get-ModelCases $fixtures (1..11) All | Where-Object {
    $bits = $_.Name -split '_'
    ($_.Expected -eq 'Reject' -and ($bits[2] + '_' + $bits[3].Split('.')[0]) -in $ids) -or
    ($_.Expected -eq 'Accept' -and $bits[2] -in @('0101','0302','0306','0416','0722','0915','1514','1517','1605','1606','1607','1608','1609','1612','2505','3102','3314'))
})
Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.IO.Compression
# The two official positives cross the two model parts' slice relationships.
# Preserve them as rejection controls and repair only local copies for acceptance controls.
$repaired = Join-Path $run 'fixtures'
[void](New-Item -ItemType Directory -Path $repaired)
foreach ($source in @($cases | Where-Object Name -match '^P_SPX_(0915_01|1514_02)\.3mf$')) {
    $target=Join-Path $repaired ('repaired-' + $source.Name)
    Copy-Item -LiteralPath $source.Path -Destination $target
    $zip=[IO.Compression.ZipFile]::Open($target,[IO.Compression.ZipArchiveMode]::Update)
    $changed=0
    try { foreach ($entry in @($zip.Entries | Where-Object FullName -match '^3D/_rels/midway2?\.model\.rels$')) {
        $part=$entry.FullName.Replace('/_rels/','/').Substring(0,$entry.FullName.Replace('/_rels/','/').Length-5)
        $reader=[IO.StreamReader]::new($zip.GetEntry($part).Open())
        try { $model=Read-SafeXml $reader.ReadToEnd() } finally { $reader.Dispose() }
        $slice=$model.SelectSingleNode('//*[local-name()="sliceref"]')
        $reader=[IO.StreamReader]::new($entry.Open())
        try { $doc=Read-SafeXml $reader.ReadToEnd() } finally { $reader.Dispose() }
        foreach ($rel in $doc.SelectNodes('//*[local-name()="Relationship"]')) {
            if ($rel.GetAttribute('Type') -eq 'http://schemas.microsoft.com/3dmanufacturing/2013/01/3dmodel') {
                $rel.SetAttribute('Target',$slice.GetAttribute('slicepath')); $changed++
            }
        }
        $name=$entry.FullName; $entry.Delete()
        $writer=[IO.StreamWriter]::new($zip.CreateEntry($name).Open(),[Text.UTF8Encoding]::new($false))
        try { $writer.Write($doc.OuterXml) } finally { $writer.Dispose() }
    } } finally { $zip.Dispose() }
    if ($changed -ne 2) { throw "Unexpected relationship repair count: $changed" }
    $cases += [pscustomobject]@{Suite=$source.Suite;Name=[IO.Path]::GetFileName($target);Path=$target;Expected='Accept'}
}
$results = [Collections.Generic.List[object]]::new()
foreach ($group in $cases | Group-Object Suite) {
    $models = @($group.Group)
    $work = Join-Path $run ('suite' + $group.Name)
    [void](New-Item -ItemType Directory -Path $work)
    @(('pschemaLoc = "' + $schema.Replace('\','/') + '"'),'iErrMaxStructure = "100"','iErrMaxValidataion = "100"') | Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
    ('pNoUUIDchk = "' + $(if([int]$group.Name -in @(1,2,5)){'false'}else{'true'}) + '"') | Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII
    'validationConsumer = "editor"' | Add-Content (Join-Path $work '3MF.ini') -Encoding ASCII
    [IO.File]::WriteAllLines((Join-Path $work 'files.txt'), [string[]]@($models | ForEach-Object Path), [Text.UTF8Encoding]::new($false))
    Write-Host "Checking suite $($group.Name), $($models.Count) fixtures"
    $execution = Invoke-CliProcess (Get-Command $Java).Source @('-Xmx2048m','-cp',$classpath,'com.qualitylogic.x3mf.StartUp','-C','-L','files.txt','-I') $work 1200
    foreach ($case in $models) {
        $expected = $case.Expected
        $exception = ''
        if ($case.Name -match '^N_(XPM|XXX|XPX|XXM)_0416_02\.3mf$|^N_DPX_3314_03\.3mf$') { $expected='Accept'; $exception='A reflection preserves the sign of positive base-mesh volume.' }
        if ($case.Name -in @('N_SPX_0416_03.3mf','N_SXX_0416_03.3mf')) { $expected='Accept'; $exception='Required slices supersede low-resolution fallback mesh topology.' }
        if ($case.Name -in @('N_XXX_0420_01.3mf','N_XPX_0420_01.3mf','N_XXM_0420_01.3mf')) { $expected='Accept'; $exception='The official package contains no DOCTYPE.' }
        if ($case.Name -match '^N_..._0421_01\.3mf$|^N_DPX_3314_07\.3mf$') { $expected='Accept'; $exception='Editor placement/singular-transform policy; printer-bed conformance is unverified.' }
        if ($case.Name -match '^P_SPX_(0915_01|1514_02)\.3mf$') { $expected='Reject'; $exception='Official fixture crosses the two source model parts slice relationships; repaired copies must be accepted.' }
        $report = Join-Path $work ('cliResults/' + [IO.Path]::ChangeExtension($case.Name,'.xml'))
        $checked = Test-CliResult $report $case.Name $expected
        if ($execution.TimedOut -or $execution.ExitCode -ne 0) { $checked.Status='Error'; $checked.Detail='CLI crashed or timed out. ' + $checked.Detail }
        if (Test-Path -LiteralPath $report) {
            $doc=Read-SafeXml ([IO.File]::ReadAllText($report))
            if ($checked.Actual -ne 'Fail' -and $doc.SelectNodes('//*[@severity="critical" or starts-with(@issueID,"ER_") or starts-with(@issueID,"SC_")]').Count) { $checked.Status='Failed'; $checked.Detail+=' Non-Fail report contains an error.' }
            foreach ($aggregate in Get-ChildItem (Join-Path $work 'cliResults') -Filter 'XmlAggregate*.xml') { $null=Read-SafeXml ([IO.File]::ReadAllText($aggregate.FullName)) }
        }
        $results.Add([pscustomobject]@{Suite=$case.Suite;Name=$case.Name;SpecificationExpected=$case.Expected;Expected=$expected;Exception=$exception;Actual=$checked.Actual;Status=$checked.Status;Detail=$checked.Detail;Report=$report})
    }
    $results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
}
foreach ($probe in @(
    @{Name='empty-single';Fixture='N_XPM_0207_01.3mf';Mode=@();Code='ER_008';Limit=100}
    @{Name='mock-structure';Fixture='N_XPM_0428_01.3mf';Mode=@('-S');Code='ER_192';Limit=100}
    @{Name='mock-hidden';Fixture='N_XPM_0428_01.3mf';Mode=@('-W');Code='SC_000';Limit=100}
    @{Name='boolean-schema';Fixture='N_OPX_3102_01.3mf';Mode=@('-V');Code='SC_000';Limit=100}
    @{Name='boolean-grouped-schema';Fixture='N_OPX_3102_01.3mf';Mode=@('-V');Code='SC_000';Limit=0}
)) {
    $work=Join-Path $run $probe.Name
    [void](New-Item -ItemType Directory -Path $work)
    $model=Get-ChildItem $fixtures -Recurse -Filter $probe.Fixture | Select-Object -First 1
    if (-not $model) { throw "Missing probe fixture $($probe.Fixture)" }
    @(('pschemaLoc = "' + $schema.Replace('\','/') + '"'),'iErrMaxStructure = "100"',('iErrMaxValidataion = "' + $probe.Limit + '"')) | Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
    'pNoUUIDchk = "false"' | Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII
    'validationConsumer = "editor"' | Add-Content (Join-Path $work '3MF.ini') -Encoding ASCII
    $execution=Invoke-CliProcess (Get-Command $Java).Source (@('-Xmx2048m','-cp',$classpath,'com.qualitylogic.x3mf.StartUp','-C','-F',$model.FullName,'-I') + $probe.Mode) $work 120
    $report=Join-Path $work ('cliResults/' + [IO.Path]::ChangeExtension($model.Name,'.xml'))
    $checked=Test-CliResult $report $model.Name Reject
    if ($execution.TimedOut -or $execution.ExitCode -ne 0) { $checked.Status='Error'; $checked.Detail='CLI crashed or timed out' }
    $xml=if(Test-Path $report){ [IO.File]::ReadAllText($report) }else{''}
    $html=if(Test-Path ([IO.Path]::ChangeExtension($report,'.html'))){ [IO.File]::ReadAllText([IO.Path]::ChangeExtension($report,'.html')) }else{''}
    if (-not $xml.Contains($probe.Code) -or $xml.Contains('ER_REPORT_XML') -or ($probe.Code -ne 'SC_000' -and -not $html.Contains($probe.Code))) { $checked.Status='Failed'; $checked.Detail+=' Missing or fallback diagnostic' }
    $results.Add([pscustomobject]@{Suite='mode-probe';Name=$probe.Name;SpecificationExpected='Reject';Expected='Reject';Exception='';Actual=$checked.Actual;Status=$checked.Status;Detail=$checked.Detail;Report=$report})
}
$results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
Write-JUnitReport $results (Join-Path $run 'junit.xml')
[ordered]@{Completed=$true;Cases=$results.Count;Passed=@($results | Where-Object Status -eq Passed).Count;Failed=@($results | Where-Object Status -eq Failed).Count;Errors=@($results | Where-Object Status -eq Error).Count;SpecificationSHA256=(Get-FileHash (Join-Path $fixtures 'test_specification/3MF_Test_Specification_v2_4_1.docx')).Hash} | ConvertTo-Json | Set-Content (Join-Path $run 'summary.json') -Encoding UTF8
$results | Where-Object Status -ne Passed | Format-Table Name,Expected,Actual,Status,Detail -Wrap
Write-Host "Reports: $run"
if (@($results | Where-Object Status -ne Passed).Count) { exit 1 }
exit 0
