[CmdletBinding()]
param([string]$TestSuiteRoot, [string]$Java = 'java', [string]$Javac = 'javac')
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Conformance.ps1')
if (-not $TestSuiteRoot) { $TestSuiteRoot = Join-Path $PSScriptRoot '../../../../../test_suites' }
$project = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$fixtures = (Resolve-Path -LiteralPath $TestSuiteRoot).Path
$schema = Join-Path $fixtures 'Consolidated_Schema/Schema'
$run = Join-Path $project ('tests/cli/results/reference-whitespace-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
$classes = Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$sources = @(Get-ChildItem (Join-Path $project 'src') -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
[IO.File]::WriteAllLines((Join-Path $run 'sources.txt'), [string[]]$sources, [Text.UTF8Encoding]::new($false))
$compiled = Invoke-CliProcess (Get-Command $Javac).Source @('-encoding', 'UTF-8', '-cp', (Join-Path $project 'jar/*'), '-d', $classes, ('@' + (Join-Path $run 'sources.txt'))) $run 180
if ($compiled.TimedOut -or $compiled.ExitCode -ne 0) { throw "Compilation failed; see $run/stderr.log" }
Copy-Item -LiteralPath (Join-Path $project 'src/resources') -Destination $classes -Recurse
Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.IO.Compression
$generated = Join-Path $run 'fixtures'
[void](New-Item -ItemType Directory -Path $generated)

function New-ReferenceCase([string]$Kind, [string]$Padding) {
    $relative = switch ($Kind) {
        'production-path' { 'suite5_core_prod/positive_test_cases/P_XPX_0107_01.3mf' }
        'texture-path' { 'suite6_core_matl/positive_test_cases/P_XXM_0104_01.3mf' }
        'slice-path' { 'suite4_core_slice/positive_test_cases/P_SXX_1511_01.3mf' }
        default { 'suite3_core/positive_test_cases/P_XXX_0101_01.3mf' }
    }
    $target = Join-Path $generated ($Kind + '-' + $Padding + '.3mf')
    Copy-Item (Join-Path $fixtures $relative) $target
    $zip = [IO.Compression.ZipFile]::Open($target, [IO.Compression.ZipArchiveMode]::Update)
    $changed = $false
    try {
        foreach ($entry in @($zip.Entries | Where-Object { $_.Name -match '\.(model|rels)$' })) {
            $part = $entry.FullName
            $reader = [IO.StreamReader]::new($entry.Open())
            try { $doc = Read-SafeXml ($reader.ReadToEnd()) } finally { $reader.Dispose() }
            # Older official fixtures lack the UUIDs required by the consolidated schema.
            foreach ($element in $doc.SelectNodes('//*[local-name()="object" or local-name()="item" or local-name()="build" or local-name()="component"]')) {
                if (-not $element.HasAttribute('UUID', 'http://schemas.microsoft.com/3dmanufacturing/production/2015/06')) {
                    $attr = $doc.CreateAttribute('p', 'UUID', 'http://schemas.microsoft.com/3dmanufacturing/production/2015/06')
                    $attr.Value = [guid]::NewGuid().ToString()
                    [void]$element.Attributes.Append($attr)
                }
            }
            $query = switch ($Kind) {
                'root-target' { if ($part -eq '_rels/.rels') { '//*[local-name()="Relationship"]/@Target' } }
                'part-target' { if ($part -ne '_rels/.rels' -and $part.EndsWith('.rels')) { '//*[local-name()="Relationship"]/@Target' } }
                'production-path' { '//*[local-name()="component"]/@*[local-name()="path"]' }
                'texture-path' { '//*[local-name()="texture2d"]/@path' }
                'slice-path' { '//*[local-name()="sliceref"]/@slicepath' }
                'thumbnail' { '//*[local-name()="object"]/@thumbnail' }
            }
            if ($query -and -not $changed) {
                $attributes = @($doc.SelectNodes($query))
                if ($Padding -ne 'multiple') { $attributes = @($attributes | Select-Object -First 1) }
                foreach ($attr in $attributes) {
                    $value = $attr.Value
                    if ($Padding -in @('internal', 'encoded', 'escaped')) {
                        $replacement = switch ($Padding) {
                            'internal' { '/Thumbnails/internal space.png' }
                            'encoded' { '/Thumbnails/%20image%20.png' }
                            'escaped' { '/Thumbnails/image & name.png' }
                        }
                        $image = $zip.GetEntry($value.TrimStart('/'))
                        $input = $image.Open()
                        $output = $zip.CreateEntry($replacement.TrimStart('/')).Open()
                        try { $input.CopyTo($output) } finally { $output.Dispose(); $input.Dispose() }
                        $image.Delete()
                        $attr.Value = $replacement
                        $value = $replacement
                    }
                    switch ($Padding) {
                        'leading' { $attr.Value = ' ' + $value }
                        'trailing' { $attr.Value = $value + ' ' }
                        'both' { $attr.Value = '  ' + $value + '  ' }
                        'tabs' { $attr.Value = "`t" + $value + "`n" }
                        'escaped' { $attr.Value = ' ' + $value + ' ' }
                        'multiple' { $attr.Value = ' ' + $value + ' ' }
                        'metadata' { $attr.OwnerElement.SetAttribute('name', '  object with spaces  ') }
                    }
                    $changed = $true
                }
            }
            $entry.Delete()
            $writer = [IO.StreamWriter]::new($zip.CreateEntry($part).Open(), [Text.UTF8Encoding]::new($false))
            try { $writer.Write($doc.OuterXml) } finally { $writer.Dispose() }
        }
    } finally { $zip.Dispose() }
    if (-not $changed) { throw "No reference found for $Kind" }
    return $target
}
$cases = @(
    foreach ($kind in @('root-target', 'part-target', 'production-path', 'texture-path', 'slice-path', 'thumbnail')) {
        foreach ($padding in @('clean', 'leading', 'trailing', 'both', 'tabs')) {
            @{ Model = (New-ReferenceCase $kind $padding); Count = $(if ($padding -ne 'clean') { 1 } else { 0 }); Escaped = $false }
        }
    }
    foreach ($padding in @('internal', 'encoded', 'escaped', 'multiple')) {
        @{ Model = (New-ReferenceCase 'root-target' $padding); Count = $(if ($padding -eq 'multiple') { 2 } elseif ($padding -eq 'escaped') { 1 } else { 0 }); Escaped = ($padding -eq 'escaped') }
    }
    @{ Model = (New-ReferenceCase 'thumbnail' 'metadata'); Count = 0; Escaped = $false }
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
    $checked = Test-CliResult $report $name 'Accept'
    $problems = [Collections.Generic.List[string]]::new()
    if ($checked.Status -ne 'Passed') { $problems.Add($checked.Detail) }
    if ($execution.TimedOut -or $execution.ExitCode -ne 0) { $problems.Add('CLI crashed or timed out') }
    if (Test-Path -LiteralPath $report) {
        $doc = Read-SafeXml ([IO.File]::ReadAllText($report))
        $warnings = @($doc.DocumentElement.SelectNodes('./Issue | ./PrimaryIssue') | Where-Object { $_.GetAttribute('issueID') -eq 'WR_026' })
        $html = [IO.File]::ReadAllText([IO.Path]::ChangeExtension($report, '.html'))
        if ($case.Count) {
            if ($warnings.Count -ne $case.Count -or ([regex]::Matches($html, 'WR_026:')).Count -ne $case.Count) { $problems.Add("Expected $($case.Count) whitespace warning(s) in XML and HTML") }
            if ($checked.Actual -ne 'Warning') { $problems.Add('Padded reference should produce Warning') }
            $primary = $doc.DocumentElement.SelectSingleNode('./PrimaryIssue')
            if (-not $primary -or $primary.GetAttribute('issueID') -ne 'WR_026') { $problems.Add('Whitespace warning was not selected as primary issue') }
            foreach ($warning in $warnings) {
                if ($warning.GetAttribute('severity') -ne 'info' -or $warning.GetAttribute('type') -ne 'reference whitespace' -or $warning.InnerText -notmatch 'Leading or trailing whitespace.*attribute.*file:') { $problems.Add('Incorrect whitespace diagnostic') }
            }
            if ($case.Escaped -and ($warnings[0].InnerText -notlike '*image & name.png*' -or $html -notlike '*image &amp; name.png*')) { $problems.Add('Diagnostic did not preserve/escape the reference') }
        } elseif ($warnings.Count -or $html -match 'WR_026') { $problems.Add('Spurious whitespace diagnostic') }
    }
    [pscustomobject]@{ Suite = 'issue12'; Name = $name; Expected = 'Accept'; Actual = $checked.Actual; Status = $(if ($problems.Count) { 'Failed' } else { 'Passed' }); Detail = ($problems -join '; '); Report = $report }
}
$results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
Write-JUnitReport $results (Join-Path $run 'junit.xml')
$results | Select-Object Name, Actual, Status, Detail | Format-Table -AutoSize
Write-Host "Reports: $run"
if (@($results | Where-Object Status -ne 'Passed').Count) { exit 1 }
