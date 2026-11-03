[CmdletBinding()]
param([string]$TestSuiteRoot, [string]$Baseline)
$ErrorActionPreference='Stop'
. (Join-Path $PSScriptRoot '../../Conformance.ps1')
$project=(Resolve-Path (Join-Path $PSScriptRoot '../../../..')).Path
if(-not $TestSuiteRoot){$TestSuiteRoot=Join-Path $PSScriptRoot '../../../../../../test_suites'}
$fixtures=(Resolve-Path $TestSuiteRoot).Path
if(-not $Baseline){$Baseline=Join-Path $project 'tests/cli/results/issue23-combined-20261101'}
$old=@(Import-Csv (Join-Path $Baseline 'results.csv')|Where-Object Status -eq Failed)
if($old.Count -ne 37){throw 'Expected the original 37 mismatches as the baseline.'}
$out=Join-Path $project ('tests/cli/results/issue23-report-'+(Get-Date -Format 'yyyyMMdd-HHmmss')+'-'+[guid]::NewGuid().ToString('N').Substring(0,8))
[void](New-Item -ItemType Directory -Path $out)
$names=@($old.Name)+@('P_XPX_0722_04.3mf','P_EPX_0740_05.3mf','P_EPX_0740_06.3mf','P_XPM_0101_01.3mf','P_SPX_0104_04.3mf','P_SXX_0104_04.3mf')
$list=Join-Path $out 'fixtures.txt'
$names|Set-Content $list -Encoding ASCII
& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tests/cli/Run-Conformance.ps1') -TestSuiteRoot $fixtures -Consumer Printer -FixtureList $list -OutputRoot $out -TimeoutSeconds 1200
if($LASTEXITCODE -notin @(0,1)){throw 'Conformance execution failed.'}
$run=@(Get-ChildItem $out -Directory)
if($run.Count -ne 1){throw 'Ambiguous run directory'}
$run=$run[0].FullName
& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tests/cli/Compare-Specification.ps1') -RunDirectory $run
if($LASTEXITCODE -ne 0){throw 'Report generation failed.'}
$compared=@(Import-Csv (Join-Path $run 'specification-match/expected-vs-observed.csv'))
$counts=Get-Content (Join-Path $run 'specification-match/summary.json') -Raw|ConvertFrom-Json
if($counts.Models -ne 43 -or $counts.UnverifiableModelReports -or $counts.UnexplainedMismatches){throw 'Unexpected missing results or unexplained mismatches.'}
$delta=foreach($row in $old){
    $now=@($compared|Where-Object { $_.Suite -eq $row.Suite -and $_.Test -eq $row.Name })
    if($now.Count -ne 1){throw 'Missing baseline fixture'}
    [pscustomobject]@{Suite=$row.Suite;Name=$row.Name;Before=$row.Actual;After=$now[0].ObservedCliResult;Resolution=$(if($now[0].AcceptanceVerification -eq 'Match'){'Fixed'}else{$now[0].DiscrepancyCategory});Reason=$now[0].DiscrepancyReason;Report=$now[0].Report}
}
$delta|Export-Csv (Join-Path $run 'original37-resolution.csv') -NoTypeInformation -Encoding UTF8
$fixed=@($delta|Where-Object Resolution -eq Fixed).Count
if($fixed -ne 15 -or $counts.FixtureDefects -ne 9 -or $counts.SpecificationConflicts -ne 13){throw 'Unexpected issue23 resolution counts; inspect retained reports.'}
@(
    '# Issue 23: refreshed printer report'
    ''
    "Fresh sources were compiled and all 37 historical mismatches were rerun, plus six profile/positive controls. All 43 result files are verified. This is a focused refresh, not a rerun of all 2,271 models."
    ''
    '| Original 37 | Count |'
    '| --- | ---: |'
    "| Validator fixes verified under printer policy | $fixed |"
    "| Defective official fixtures | $($counts.FixtureDefects) |"
    "| Test expectations conflict with current Core rules | $($counts.SpecificationConflicts) |"
    '| Unexplained discrepancies | 0 |'
    ''
    'The 22 fixture/specification discrepancies remain failures in the strict acceptance score. No official fixture was changed and no mismatch was waived. See [the comparison](specification-match/comparison.md) and original37-resolution.csv for individual evidence.'
    ''
    'The three positive-named printer-exception controls are now correctly reported as expected rejections. Both Unicode-path positive controls remain accepted with interoperability warnings.'
) | Set-Content (Join-Path $run 'issue23-resolution.md') -Encoding UTF8
Write-Host "Issue23 report checks passed: 43 verified models; 15 fixed, 9 fixture defects, 13 specification conflicts. Reports: $run"
exit 0
