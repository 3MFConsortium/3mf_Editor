[CmdletBinding()]
param([string]$TestSuiteRoot)
$ErrorActionPreference='Stop'
. (Join-Path $PSScriptRoot '../../Conformance.ps1')
if(-not $TestSuiteRoot){$TestSuiteRoot=Join-Path $PSScriptRoot '../../../../../../test_suites'}
$fixtures=(Resolve-Path $TestSuiteRoot).Path
$project=(Resolve-Path (Join-Path $PSScriptRoot '../../../..')).Path
$spec=Join-Path $fixtures 'test_specification/3MF_Test_Specification_v2_4_1.docx'
$run=Join-Path $project ('tests/cli/results/report-policy-'+(Get-Date -Format 'yyyyMMdd-HHmmss')+'-'+[guid]::NewGuid().ToString('N').Substring(0,8))
[void](New-Item -ItemType Directory -Path $run)
$checks=0
foreach($probe in @(
    @('5/P_0722_04','printer','Reject'),@('5/P_0740_05','printer','Reject'),@('5/P_0740_06','printer','Reject'),
    @('5/P_0722_04','editor','Accept'),@('5/P_0740_06','editor','Accept'),@('8/N_2604_01','editor','Accept'),
    @('8/N_2602_01','printer','Reject'),@('8/N_2602_01','editor','Accept'),@('2/N_0421_01','editor','Accept'),
    @('11/N_3314_07','editor','Accept'),@('2/N_0416_01','editor','Reject')
)){
    if((Get-ConsumerExpectation $probe[0] $probe[1]).Expected -ne $probe[2]){throw "Wrong policy: $probe"};$checks++
}
$names=@('P_EPX_0740_06.3mf','N_SPX_0208_01.3mf','P_XPM_0101_01.3mf','P_XPM_0102_01.3mf')
$models=@(Get-ModelCases $fixtures (1..11)|Where-Object Name -in $names)
if($models.Count -ne 4){throw 'Missing policy controls'}
$rows=foreach($model in $models){
    $policy=Get-ConsumerExpectation $model.SpecKey printer
    $model.Expected=$policy.Expected
    $report=Join-Path $run ($model.Name+'.xml')
    $actual='Pass';$status='Passed';$role=' consumer="printer"';$issues=''
    if($model.Name -eq $names[0]){$actual='Fail';$issues='<Issue issueID="ER_203">No accessible representation</Issue>'}
    if($model.Name -eq $names[1]){$actual='Warning';$status='Failed';$issues='<Issue issueID="WR_030">Unicode interoperability</Issue>'}
    if($model.Name -eq $names[2]){$role=''}
    $text='<TestResult'+$role+' file="'+$model.Name+'" result="'+$actual+'">'+$issues+'</TestResult>'
    if($model.Name -eq $names[3]){$text='<TestResult'}
    [IO.File]::WriteAllText($report,$text)
    [pscustomobject]@{Suite=$model.Suite;Name=$model.Name;Expected=$policy.Expected;Actual=$actual;Status=$status;Detail='Report-policy control';Report=$report}
}
$models|Export-Csv (Join-Path $run 'inventory.csv') -NoTypeInformation -Encoding UTF8
$rows|Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
$summary=[ordered]@{Completed=$true;Consumer='printer';Models=4;Passed=999;Failed=999;StrictWarnings=$false;Specification=$spec;SpecificationSHA256=(Get-FileHash $spec -Algorithm SHA256).Hash;SpecificationUrl='https://github.com/3MFConsortium/test_suites'}
$summary|ConvertTo-Json|Set-Content (Join-Path $run 'summary.json') -Encoding UTF8
& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tests/cli/Compare-Specification.ps1') -RunDirectory $run
if($LASTEXITCODE -ne 0){throw 'Comparison failed'}
$counts=Get-Content (Join-Path $run 'specification-match/summary.json') -Raw|ConvertFrom-Json
if($counts.VerifiedAcceptanceMatches -ne 1 -or $counts.VerifiedAcceptanceMismatches -ne 1 -or $counts.UnverifiableModelReports -ne 2 -or $counts.SpecificationConflicts -ne 1){throw 'Comparison trusted stale totals, accepted a malformed/wrong-consumer report, or lost the known discrepancy.'}
$checks+=4
$unicode=$models|Where-Object Name -eq 'N_SPX_0208_01.3mf'
$originalPath=$unicode.Path
$unicode.Path=Join-Path $run 'changed-fixture.3mf'
[IO.File]::WriteAllText($unicode.Path,'Different fixture content must not inherit a known classification.')
$models|Export-Csv (Join-Path $run 'inventory.csv') -NoTypeInformation -Encoding UTF8
& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tests/cli/Compare-Specification.ps1') -RunDirectory $run | Out-Null
if($LASTEXITCODE -ne 0){throw 'Changed-fixture comparison failed'}
$counts=Get-Content (Join-Path $run 'specification-match/summary.json') -Raw|ConvertFrom-Json
if($counts.SpecificationConflicts -ne 0 -or $counts.UnexplainedMismatches -ne 1){throw 'Classification ignored the fixture fingerprint'}
$checks++
$unicode.Path=$originalPath
$models|Export-Csv (Join-Path $run 'inventory.csv') -NoTypeInformation -Encoding UTF8
$summary.Remove('Consumer')
$summary|ConvertTo-Json|Set-Content (Join-Path $run 'summary.json') -Encoding UTF8
($rows|Where-Object Name -eq 'P_EPX_0740_06.3mf').Expected='Accept'
$rows|Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tests/cli/Compare-Specification.ps1') -RunDirectory $run | Out-Null
if($LASTEXITCODE -ne 0){throw 'Legacy comparison failed'}
$counts=Get-Content (Join-Path $run 'specification-match/summary.json') -Raw|ConvertFrom-Json
if($counts.Consumer -ne 'legacy unspecified' -or $counts.VerifiedAcceptanceMatches -ne 1 -or $counts.VerifiedAcceptanceMismatches -ne 2 -or $counts.UnverifiableModelReports -ne 1){throw 'Legacy report was silently assigned a new consumer policy'}
$checks++
$badPass=Join-Path $run 'contradictory-result.xml'
[IO.File]::WriteAllText($badPass,'<TestResult file="contradictory.3mf" result="Pass"><Issue issueID="ER_203">Printer error</Issue></TestResult>')
if((Test-CliResult $badPass 'contradictory.3mf' Accept).Status -ne 'Error'){throw 'Pass with error diagnostics was trusted'}
$checks++
Write-Host "$checks report-policy checks passed. Reports: $run"
exit 0
