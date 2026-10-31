[CmdletBinding()]
param([string]$TestSuiteRoot, [string]$Java = 'java', [string]$Javac = 'javac')
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Conformance.ps1')
if (-not $TestSuiteRoot) { $TestSuiteRoot = Join-Path $PSScriptRoot '../../../../../test_suites' }
$project = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$fixtures = (Resolve-Path -LiteralPath $TestSuiteRoot).Path
$schema = Join-Path $fixtures 'Consolidated_Schema/Schema'
$run = Join-Path $project ('tests/cli/results/low-severity-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
$classes = Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$sources = @(Get-ChildItem (Join-Path $project 'src') -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
$sourceList = Join-Path $run 'sources.txt'
[IO.File]::WriteAllLines($sourceList, [string[]]$sources, [Text.UTF8Encoding]::new($false))
$compiled = Invoke-CliProcess (Get-Command $Javac).Source @('--release', '8', '-encoding', 'UTF-8', '-cp', (Join-Path $project 'jar/*'), '-d', $classes, "@$sourceList") $run 180
Get-Content (Join-Path $run 'stdout.log'), (Join-Path $run 'stderr.log') | Set-Content (Join-Path $run 'compile.log') -Encoding UTF8
if ($compiled.TimedOut -or $compiled.ExitCode -ne 0) { throw "Compilation failed; see $run/stderr.log" }
Copy-Item -LiteralPath (Join-Path $project 'src/resources') -Destination $classes -Recurse
$classpath = $classes + [IO.Path]::PathSeparator + (Join-Path $project 'jar/*')
$helper = Invoke-CliProcess (Get-Command $Javac).Source @('--release','8','-d',$classes,(Join-Path $project 'src/com/qualitylogic/x3mf/PackageChecks.java'),(Join-Path $project 'src/com/qualitylogic/x3mf/SafeXml.java'),(Join-Path $project 'tests/cli/PackageChecksTest.java')) $run 60
if ($helper.TimedOut -or $helper.ExitCode -ne 0) { throw "Helper compilation failed: $run/stderr.log" }
$unit = Invoke-CliProcess (Get-Command $Java).Source @('-cp',$classpath,'com.qualitylogic.x3mf.PackageChecksTest') $run 60
Copy-Item (Join-Path $run 'stdout.log') (Join-Path $run 'package-checks.log')
if ($unit.TimedOut -or $unit.ExitCode -ne 0) { throw "Package/XML checks failed: $run/stderr.log" }
Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.Drawing
$generated = Join-Path $run 'fixtures'
[void](New-Item -ItemType Directory -Path $generated)
function New-RepairedCase([string]$Id) {
    $target = Join-Path $generated "repaired-$Id.3mf"
    Copy-Item (Join-Path $fixtures "suite2_core_prod_matl/negative_test_cases/N_XPM_$Id.3mf") $target
    $zip = [IO.Compression.ZipFile]::Open($target, [IO.Compression.ZipArchiveMode]::Update)
    try {
        foreach ($entry in @($zip.Entries)) {
            $name = $entry.FullName
            if ($Id -eq '0419_01' -and $name.EndsWith('.jpg')) {
                $entry.Delete()
                $bitmap = [Drawing.Bitmap]::new(8,8)
                $stream = $zip.CreateEntry($name).Open()
                try { $bitmap.Save($stream, [Drawing.Imaging.ImageFormat]::Jpeg) } finally { $stream.Dispose(); $bitmap.Dispose() }
                continue
            }
            if ($name -notmatch '\.(model|rels)$') { continue }
            $reader = [IO.StreamReader]::new($entry.Open())
            try { $text = $reader.ReadToEnd() } finally { $reader.Dispose() }
            switch ($Id) {
                '0208_01' { $name = $name.Replace([string][char]0x052a,'%D4%AA'); $text = $text.Replace([string][char]0x052a,'%D4%AA') }
                '0405_03' { $text = $text.Replace('/wrong3dmodel','/3dmodel') }
                '0409_01' { $text = $text.Replace(' xml:space="preserve"','') }
                '0415_01' { $name = $name.Replace('.3dmodel1','3dmodel1'); $text = $text.Replace('.3dmodel1','3dmodel1') }
                '0420_01' { $text = [regex]::Replace($text,'<!DOCTYPE[\s\S]*?\]>','') }
                '0604_03' {
                    $text = $text.Replace('pids="6 1"','pids="1 6"')
                    $text = [regex]::Replace($text, 'pindices="(\d+)\s+(\d+)"', 'pindices="$2 $1"')
                }
                '0608_01' { $text = $text.Replace('#FFHFFF','#FFFFFF') }
            }
            $entry.Delete()
            $writer = [IO.StreamWriter]::new($zip.CreateEntry($name).Open(), [Text.UTF8Encoding]::new($false))
            try { $writer.Write($text) } finally { $writer.Dispose() }
        }
    } finally { $zip.Dispose() }
    return $target
}
function New-CustomRelationshipCase {
    $target = Join-Path $generated 'custom-relationship.3mf'
    Copy-Item (New-RepairedCase '0405_03') $target
    $zip = [IO.Compression.ZipFile]::Open($target,[IO.Compression.ZipArchiveMode]::Update)
    try {
        foreach ($name in @('[Content_Types].xml','3D/_rels/3dmodel.model.rels')) {
            $entry = $zip.GetEntry($name)
            $reader = [IO.StreamReader]::new($entry.Open())
            try { $text = $reader.ReadToEnd() } finally { $reader.Dispose() }
            if ($name -eq '[Content_Types].xml') { $text = $text.Replace('</Types>','<Default Extension="bin" ContentType="application/octet-stream"/></Types>') }
            else { $text = $text.Replace('</Relationships>','<Relationship Id="custom" Target="/Other/custom.bin" Type="https://example.org/custom"/></Relationships>') }
            $entry.Delete()
            $writer = [IO.StreamWriter]::new($zip.CreateEntry($name).Open(),[Text.UTF8Encoding]::new($false))
            try { $writer.Write($text) } finally { $writer.Dispose() }
        }
        $stream = $zip.CreateEntry('Other/custom.bin').Open()
        try { $stream.WriteByte(1) } finally { $stream.Dispose() }
    } finally { $zip.Dispose() }
    return $target
}
$custom = New-CustomRelationshipCase
function New-PackageVariant([string]$Kind, [string]$Id) {
    $target = Join-Path $generated "$Kind.3mf"
    Copy-Item (Join-Path $fixtures "suite2_core_prod_matl/negative_test_cases/N_XPM_$Id.3mf") $target
    $zip = [IO.Compression.ZipFile]::Open($target,[IO.Compression.ZipArchiveMode]::Update)
    try {
        foreach ($entry in @($zip.Entries)) {
            $name = $entry.FullName
            if ($name.EndsWith('.jpg') -and $Kind -eq 'cmyk-override') {
                $input = $entry.Open(); $output = $zip.CreateEntry($name.Replace('.jpg','.bin')).Open()
                try { $input.CopyTo($output) } finally { $input.Dispose(); $output.Dispose() }
                $entry.Delete(); continue
            }
            if ($name -notmatch '\.(model|rels|xml)$') { continue }
            $reader = [IO.StreamReader]::new($entry.Open())
            try { $text = $reader.ReadToEnd() } finally { $reader.Dispose() }
            if ($Kind -eq 'cmyk-override') {
                $text = $text.Replace('CMYKjpeg.jpg','CMYKjpeg.bin')
                if ($name -eq '[Content_Types].xml') { $text = $text.Replace('</Types>','<Override PartName="/Thumbnails/CMYKjpeg.bin" ContentType="image/jpeg"/></Types>') }
            } elseif ($name.EndsWith('.model')) { $text = [regex]::Replace($text,'<\?xml[^?]*\?>','') }
            $entry.Delete()
            $writer = [IO.StreamWriter]::new($zip.CreateEntry($name).Open(),[Text.UTF8Encoding]::new($false))
            try { $writer.Write($text) } finally { $writer.Dispose() }
        }
    } finally { $zip.Dispose() }
    return $target
}
$codes = @{'0208_01'='ER_183'; '0405_03'='ER_185'; '0409_01'='ER_184'; '0415_01'='ER_183'; '0419_01'='ER_187'; '0420_01'='ER_186'; '0426_01'='ER_169'; '0604_03'='ER_188'; '0608_01'='ER_189'}
$cases = @(
    foreach ($id in @('0208_01','0208_03','0405_03','0409_01','0415_01','0419_01','0420_01','0426_01','0604_03','0608_01')) {
        @{ Name = "N_XPM_$id.3mf"; Folder = 'suite2_core_prod_matl/negative_test_cases'; Expected = 'Reject'; Code = $codes[$id] }
    }
    @{ Name = 'N_XPX_0205_02.3mf'; Folder = 'suite5_core_prod/negative_test_cases'; Expected = 'Reject' }
    @{ Name = 'P_XPM_0101_01.3mf'; Folder = 'suite2_core_prod_matl/positive_test_cases'; Expected = 'Accept' }
    @{ Name = 'P_XPX_0101_01.3mf'; Folder = 'suite5_core_prod/positive_test_cases'; Expected = 'Accept' }
    foreach ($id in @('0104_04','0104_05','0302_03')) {
        @{ Name = "P_XPM_$id.3mf"; Folder = 'suite2_core_prod_matl/positive_test_cases'; Expected = 'Accept' }
    }
    foreach ($id in @('0208_01','0405_03','0409_01','0415_01','0419_01','0420_01','0604_03','0608_01')) {
        @{ Name = "repaired-$id.3mf"; Model = (New-RepairedCase $id); Expected = 'Accept' }
    }
    foreach ($id in @('0420_01','0608_01')) {
        @{ Name = "N_XPM_$id.3mf"; Folder = 'suite2_core_prod_matl/negative_test_cases'; Expected = 'Reject'; Mode = '-S'; Code = $codes[$id] }
    }
    @{ Name = 'N_XPM_0420_01.3mf'; Folder = 'suite2_core_prod_matl/negative_test_cases'; Expected = 'Reject'; Mode = '-V'; Code = 'SC_000' }
    foreach ($mode in @('','-W')) {
        @{ Name = 'custom-relationship.3mf'; Model = $custom; Expected = 'Accept'; Mode = $mode; Code = 'WR_022'; Warning = $true }
    }
    @{ Name = 'cmyk-override.3mf'; Model = (New-PackageVariant 'cmyk-override' '0419_01'); Expected = 'Reject'; Code = 'ER_187' }
    @{ Name = 'dtd-no-declaration.3mf'; Model = (New-PackageVariant 'dtd-no-declaration' '0420_01'); Expected = 'Reject'; Code = 'SC_000'; Mode = '-V' }
)
$results = foreach ($case in $cases) {
    $model = if ($case.ContainsKey('Model')) { $case.Model } else { Join-Path $fixtures ($case.Folder + '/' + $case.Name) }
    $row = [pscustomobject]@{ Suite = 'issue9'; Name = $case.Name; Expected = $case.Expected; Actual = ''; Status = 'Unavailable'; IssueIDs = ''; Detail = 'Fixture absent from checkout'; Report = '' }
    if (Test-Path -LiteralPath $model) {
        $mode = if ($case.ContainsKey('Mode')) { $case.Mode } else { '' }
        $work = Join-Path $run ([IO.Path]::GetFileNameWithoutExtension($case.Name) + $mode)
        [void](New-Item -ItemType Directory -Path $work)
        foreach ($folder in @('ztmp','imagetmp','zips')) { [void](New-Item -ItemType Directory -Path (Join-Path $work $folder)) }
        [IO.File]::WriteAllLines((Join-Path $work 'files.txt'), [string[]]@($model), [Text.UTF8Encoding]::new($false))
        @(('pschemaLoc = "' + $schema.Replace('\', '/') + '"'), 'iErrMaxStructure = "100"', 'iErrMaxValidataion = "100"') | Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
        'pNoUUIDchk = "false"' | Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII
        $arguments = @('-Xmx2048m', '-cp', ($classes + [IO.Path]::PathSeparator + (Join-Path $project 'jar/*')), 'com.qualitylogic.x3mf.StartUp', '-C', '-L', 'files.txt', '-I')
        if ($mode) { $arguments += $mode }
        $execution = Invoke-CliProcess (Get-Command $Java).Source $arguments $work 90
        $row.Report = Join-Path $work ('cliResults/' + [IO.Path]::ChangeExtension($case.Name, '.xml'))
        $checked = Test-CliResult $row.Report $case.Name $case.Expected
        $row.Actual = $checked.Actual; $row.Status = $checked.Status; $row.Detail = $checked.Detail
        if ($execution.TimedOut -or $execution.ExitCode -ne 0) { $row.Status = 'Error'; $row.Detail = "CLI timeout=$($execution.TimedOut), exit=$($execution.ExitCode)" }
        if (Test-Path -LiteralPath $row.Report) {
            $doc = Read-SafeXml ([IO.File]::ReadAllText($row.Report))
            $row.IssueIDs = (@($doc.DocumentElement.SelectNodes('./Issue | ./PrimaryIssue') | ForEach-Object { $_.GetAttribute('issueID') }) | Select-Object -Unique) -join ', '
            $html = [IO.File]::ReadAllText([IO.Path]::ChangeExtension($row.Report,'.html'))
            if ($case.ContainsKey('Code') -and $case.Code -and ($row.IssueIDs -notmatch $case.Code -or ($mode -ne '-W' -and $html -notmatch $case.Code))) {
                $row.Status = 'Failed'; $row.Detail += " Missing $($case.Code) in XML/HTML"
            }
            if ($case.Expected -eq 'Accept' -and $html -match 'Structural check failure') { $row.Status = 'Failed'; $row.Detail += ' HTML structural failure on positive control' }
            if ($case.ContainsKey('Warning') -and $checked.Actual -ne 'Warning') { $row.Status = 'Failed'; $row.Detail += ' Expected Warning for custom relationship' }
        }
    } elseif ($case.Name -ne 'N_XPM_0208_03.3mf') { $row.Status = 'Error' }
    $row
}
$results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
$results | ConvertTo-Json -Depth 4 | Set-Content (Join-Path $run 'results.json') -Encoding UTF8
$results | Select-Object Name,Actual,Status,IssueIDs | Format-Table -AutoSize
Write-Host "Reports: $run"
# The known missing historical fixture is explicit and is not counted as tested.
if (@($results | Where-Object Status -notin @('Passed','Unavailable')).Count) { exit 1 }
exit 0
