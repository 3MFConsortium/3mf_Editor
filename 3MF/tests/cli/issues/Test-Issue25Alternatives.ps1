[CmdletBinding()]
param([string]$TestSuiteRoot, [string]$Specification, [string]$Java='java', [string]$Javac='javac')
$ErrorActionPreference='Stop'
. (Join-Path $PSScriptRoot '../Conformance.ps1')
$project=(Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
if(-not $TestSuiteRoot){$TestSuiteRoot=Join-Path $PSScriptRoot '../../../../../test_suites'}
$fixtures=(Resolve-Path -LiteralPath $TestSuiteRoot).Path
if(-not $Specification){$Specification=Join-Path $fixtures 'test_specification/3MF_Test_Specification_v2_4_1.docx'}
$Specification=(Resolve-Path -LiteralPath $Specification).Path
$output=Join-Path $project ('tests/cli/results/issue25-'+(Get-Date -Format 'yyyyMMdd-HHmmss')+'-'+[guid]::NewGuid().ToString('N').Substring(0,8))
[void](New-Item -ItemType Directory -Path $output)
$names=@(1..11|ForEach-Object {'P_XPX_0720_{0:d2}.3mf' -f $_})
$list=Join-Path $output 'fixtures.txt'
[IO.File]::WriteAllLines($list,[string[]]$names)
$results=[Collections.Generic.List[object]]::new()
foreach($consumer in @('Printer','Editor')){
    $consumerOutput=Join-Path $output $consumer
    & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tests/cli/Run-Conformance.ps1') -TestSuiteRoot $fixtures -Specification $Specification -Suite 5 -FixtureList $list -Consumer $consumer -UuidMode Strict -BatchSize 1 -TimeoutSeconds 180 -OutputRoot $consumerOutput -Java $Java -Javac $Javac
    if($LASTEXITCODE -ne 0){throw "Conformance failed: $consumerOutput"}
    $runs=@(Get-ChildItem -LiteralPath $consumerOutput -Directory)
    if($runs.Count -ne 1){throw 'Expected exactly one fresh run'}
    $run=$runs[0].FullName
    $summary=Get-Content (Join-Path $run 'summary.json') -Raw|ConvertFrom-Json
    if(-not $summary.Completed -or $summary.Consumer -ne $consumer.ToLowerInvariant()){throw 'Incomplete or wrong-consumer run'}
    $rows=@(Import-Csv (Join-Path $run 'results.csv'))
    $inventory=@(Import-Csv (Join-Path $run 'inventory.csv'))
    if($rows.Count -ne 11 -or $inventory.Count -ne 11){throw 'Wrong fixture coverage'}
    $junit=Read-SafeXml ([IO.File]::ReadAllText((Join-Path $run 'junit.xml')))
    if($junit.testsuite.tests -ne '11' -or $junit.testsuite.failures -ne '0' -or $junit.testsuite.errors -ne '0'){throw 'Unexpected JUnit counts'}
    foreach($name in $names){
        $row=@($rows|Where-Object Name -eq $name)
        $item=@($inventory|Where-Object Name -eq $name)
        if($row.Count -ne 1 -or $item.Count -ne 1){throw "Missing/duplicate fixture: $name"}
        $expected=if($consumer -eq 'Printer' -and $name -eq 'P_XPX_0720_03.3mf'){'Reject'}else{'Accept'}
        if($row[0].Expected -ne $expected -or $row[0].Status -ne 'Passed' -or $item[0].Expected -ne $expected -or $item[0].NamingExpected -ne 'Accept'){throw "Wrong expectation or result: $consumer/$name"}
        $doc=Read-SafeXml ([IO.File]::ReadAllText($row[0].Report))
        if($doc.DocumentElement.GetAttribute('consumer') -ne $consumer.ToLowerInvariant()){throw 'Wrong XML consumer'}
        if($expected -eq 'Reject'){
            if($row[0].Actual -ne 'Fail' -or -not $doc.SelectSingleNode('/TestResult/PrimaryIssue[@issueID="ER_203"]')){throw 'Missing primary printer representation rejection'}
        }elseif($doc.SelectSingleNode('//*[@issueID="ER_203"]')){throw "Spurious printer error: $consumer/$name"}
        $results.Add([pscustomobject]@{Consumer=$consumer;Name=$name;Expected=$expected;NamingExpected=$item[0].NamingExpected;Actual=$row[0].Actual;Status=$row[0].Status;Report=$row[0].Report})
    }
    & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tests/cli/Compare-Specification.ps1') -RunDirectory $run
    if($LASTEXITCODE -ne 0){throw "Specification comparison failed: $run"}
    $comparison=Get-Content (Join-Path $run 'specification-match/summary.json') -Raw|ConvertFrom-Json
    if($comparison.VerifiedAcceptanceMatches -ne 11 -or $comparison.VerifiedAcceptanceMismatches -ne 0 -or $comparison.UnverifiableModelReports -ne 0){throw 'Specification comparison lost consumer policy'}
}
$results|Export-Csv (Join-Path $output 'results.csv') -NoTypeInformation -Encoding UTF8
[pscustomobject]@{Completed=$true;Total=$results.Count;Passed=$results.Count;Failed=0;Scope='CLI schema/structure acceptance; no rendering or printing'}|ConvertTo-Json|Set-Content (Join-Path $output 'summary.json') -Encoding UTF8
$results|Select-Object Consumer,Name,Expected,NamingExpected,Actual,Status|Format-Table -AutoSize
Write-Host "Reports: $output"
