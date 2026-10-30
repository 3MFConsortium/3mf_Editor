# Dependency-free tests for the test harness; no Java or official checkout required.
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Conformance.ps1')
$temp = Join-Path ([IO.Path]::GetTempPath()) ('3mf-harness-' + [guid]::NewGuid().ToString('N'))
[void](New-Item -ItemType Directory -Path $temp)
$script:assertions = 0
function Assert-Equal($Actual, $Expected, [string]$Message) {
    $script:assertions++
    if ($Actual -cne $Expected) { throw "$Message -- expected '$Expected', got '$Actual'" }
}
function Check-Result([string]$Xml, [string]$Expected, [string]$Status, [bool]$Strict = $false) {
    $path = Join-Path $temp 'case.xml'
    [IO.File]::WriteAllText($path, $Xml)
    $result = Test-CliResult $path 'P_XXX_0101_01.3mf' $Expected $Strict
    Assert-Equal $result.Status $Status $Xml
}
try {
    $pass = '<TestResult file="P_XXX_0101_01.3mf" result="Pass"/>'
    $warning = '<TestResult file="P_XXX_0101_01.3mf" result="Warning"><Issue>warning</Issue></TestResult>'
    $fail = '<TestResult file="P_XXX_0101_01.3mf" result="Fail"><PrimaryIssue id="ER_001">invalid</PrimaryIssue></TestResult>'
    Check-Result $pass Accept Passed
    Check-Result $pass Reject Failed
    Check-Result $warning Accept Passed
    Check-Result $warning Accept Failed $true
    Check-Result $warning Reject Failed
    Check-Result $fail Reject Passed
    Check-Result $fail Accept Failed
    Check-Result '<TestResult file="P_XXX_0101_01.3mf" result="Fail"/>' Reject Error
    Check-Result '<TestResult file="other.3mf" result="Pass"/>' Accept Error
    Check-Result '<TestResult file="P_XXX_0101_01.3mf" result="Unknown"/>' Reject Error
    Check-Result '<TestResult' Reject Error
    Check-Result '<Other file="P_XXX_0101_01.3mf" result="Pass"/>' Accept Error
    Check-Result '<!DOCTYPE TestResult [<!ENTITY x SYSTEM "file:///does-not-exist">]><TestResult>&x;</TestResult>' Reject Error
    Assert-Equal (Test-CliResult (Join-Path $temp 'absent.xml') 'absent.3mf' Reject).Status Error 'Missing XML must never pass a negative test'
    $root = Join-Path $temp 'suite3_core/positive_test_cases'
    [void](New-Item -ItemType Directory -Path $root -Force)
    [IO.File]::WriteAllText((Join-Path $root 'P_XXX_2203_04_Prod_Ext.3mf'), '')
    $cases = @(Get-ModelCases $temp @(3))
    Assert-Equal $cases.Count 1 'Discover suffixed filenames'
    Assert-Equal $cases[0].SpecKey '3/P_2203_04' 'Canonical specification ID'
    Assert-Equal @(Get-ModelCases $temp @(4)).Count 0 'Suite filter'
    [IO.File]::WriteAllText((Join-Path $root 'N_XXX_0202_01.3mf'), '')
    $caught = $false
    try { Get-ModelCases $temp @(3) | Out-Null } catch { $caught = $true }
    Assert-Equal $caught $true 'Folder/name polarity mismatch'
    $rows = @(
        [pscustomobject]@{ Suite = 3; Name = 'pass'; Expected = 'Accept'; Actual = 'Pass'; Status = 'Passed'; Detail = '' },
        [pscustomobject]@{ Suite = 3; Name = 'fail'; Expected = 'Reject'; Actual = 'Pass'; Status = 'Failed'; Detail = '< & "' },
        [pscustomobject]@{ Suite = 3; Name = 'error'; Expected = 'Reject'; Actual = ''; Status = 'Error'; Detail = 'missing' }
    )
    $junit = Join-Path $temp 'junit.xml'
    Write-JUnitReport $rows $junit
    $report = Read-SafeXml ([IO.File]::ReadAllText($junit))
    Assert-Equal $report.testsuite.tests '3' 'JUnit total'
    Assert-Equal $report.testsuite.failures '1' 'JUnit failures'
    Assert-Equal $report.testsuite.errors '1' 'JUnit errors'
    Assert-Equal $report.SelectNodes('//testcase').Count 3 'JUnit cases'
    # Minimal DOCX fixture: fragmented Word runs, repeated memberships and both polarities.
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    Add-Type -AssemblyName System.IO.Compression
    $blankCells = ('<w:tc><w:p/></w:tc>' * 10)
    $row = '<w:tr><w:tc><w:p><w:r><w:t>0101_</w:t></w:r><w:r><w:t>01</w:t></w:r></w:p></w:tc><w:tc><w:p><w:r><w:t>&#8226;</w:t></w:r></w:p></w:tc>' + $blankCells + '</w:tr>'
    $header = '<w:tr><w:tc><w:p><w:r><w:t>Suites and Extensions Supported</w:t></w:r></w:p></w:tc></w:tr>'
    $document = '<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body><w:p><w:r><w:t>2.4.1</w:t></w:r></w:p><w:tbl>' + $header + $row + $row + '</w:tbl><w:tbl>' + $header + $row.Replace('0101_', '0202_') + '</w:tbl></w:body></w:document>'
    $docxPath = Join-Path $temp 'spec.docx'
    $zip = [IO.Compression.ZipFile]::Open($docxPath, [IO.Compression.ZipArchiveMode]::Create)
    try {
        $entry = $zip.CreateEntry('word/document.xml')
        $writer = [IO.StreamWriter]::new($entry.Open())
        try { $writer.Write($document) } finally { $writer.Dispose() }
    } finally { $zip.Dispose() }
    $mapped = Get-SpecificationCases $docxPath
    Assert-Equal $mapped.Count 2 'Duplicate Appendix C rows collapse to one membership'
    Assert-Equal $mapped.ContainsKey('1/P_0101_01') $true 'Positive Appendix C mapping with fragmented runs'
    Assert-Equal $mapped.ContainsKey('1/N_0202_01') $true 'Negative Appendix C mapping'
    Assert-Equal $mapped.ContainsKey('2/P_0101_01') $false 'Empty suite column does not create membership'
    # Verify process output draining and timeout without launching the application.
    $shell = Join-Path $PSHOME 'powershell.exe'
    $child = Join-Path $temp 'child.ps1'
    'Write-Output (''x'' * 100000); [Console]::Error.WriteLine(''stderr''); exit 7' | Set-Content $child
    $process = Invoke-CliProcess $shell @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $child) $temp 20
    Assert-Equal $process.ExitCode 7 'Nonzero process exit'
    Assert-Equal $process.TimedOut $false 'Normal process completion'
    Assert-Equal ((Get-Item (Join-Path $temp 'stdout.log')).Length -gt 100000) $true 'Drain large stdout without deadlock'
    'Start-Sleep -Seconds 20' | Set-Content $child
    $process = Invoke-CliProcess $shell @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $child) $temp 1
    Assert-Equal $process.TimedOut $true 'Timeout terminates process'
    Write-Host "Passed $script:assertions harness assertions."
} finally {
    # Only remove this script's randomly named temporary directory.
    if ([IO.Path]::GetFullPath($temp).StartsWith([IO.Path]::GetFullPath([IO.Path]::GetTempPath()), [StringComparison]::OrdinalIgnoreCase)) {
        Remove-Item -LiteralPath $temp -Recurse -Force
    }
}
