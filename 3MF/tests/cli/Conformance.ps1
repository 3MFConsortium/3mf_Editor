Set-StrictMode -Version Latest

function Read-SafeXml([string]$Text) {
    $settings = New-Object System.Xml.XmlReaderSettings
    $settings.DtdProcessing = [System.Xml.DtdProcessing]::Prohibit
    $settings.XmlResolver = $null
    $reader = [System.Xml.XmlReader]::Create([System.IO.StringReader]::new($Text), $settings)
    try {
        $doc = New-Object System.Xml.XmlDocument
        $doc.XmlResolver = $null
        $doc.Load($reader)
        return ,$doc
    } finally { $reader.Dispose() }
}

function Get-SpecificationCases([string]$Path) {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [IO.Compression.ZipFile]::OpenRead($Path)
    try {
        $entry = $zip.GetEntry('word/document.xml')
        if ($null -eq $entry) { throw 'Specification has no word/document.xml' }
        $reader = [IO.StreamReader]::new($entry.Open())
        try { $doc = Read-SafeXml $reader.ReadToEnd() } finally { $reader.Dispose() }
    } finally { $zip.Dispose() }
    $ns = [Xml.XmlNamespaceManager]::new($doc.NameTable)
    $ns.AddNamespace('w', 'http://schemas.openxmlformats.org/wordprocessingml/2006/main')
    $allText = ($doc.SelectNodes('//w:t', $ns) | ForEach-Object { $_.InnerText }) -join ''
    if ($allText -notmatch '2\.4\.1') { throw 'Expected test specification version 2.4.1' }
    $tables = @($doc.SelectNodes('//w:body/w:tbl', $ns) | Where-Object {
        (($_.SelectNodes('.//w:t', $ns) | ForEach-Object { $_.InnerText }) -join '') -match 'Suites and Extensions Supported'
    })
    if ($tables.Count -ne 2) { throw 'Expected the positive and negative Appendix C mapping tables' }
    $cases = @{}
    for ($t = 0; $t -lt 2; $t++) {
        $prefix = @('P', 'N')[$t]
        foreach ($row in $tables[$t].SelectNodes('./w:tr', $ns)) {
            $cells = @($row.SelectNodes('./w:tc', $ns) | ForEach-Object {
                ($_.SelectNodes('.//w:t', $ns) | ForEach-Object { $_.InnerText }) -join ''
            })
            if ($cells.Count -ne 12 -or $cells[0].Trim() -notmatch '^\d{4}_\d{2}$') { continue }
            $id = $cells[0].Trim()
            for ($suite = 1; $suite -le 11; $suite++) {
                if ($cells[$suite].Contains([string][char]0x2022)) {
                    $key = '{0}/{1}_{2}' -f $suite, $prefix, $id
                    # Appendix C repeats some rows (for example P_3000_07); membership is a set.
                    $cases[$key] = [pscustomobject]@{ Suite = $suite; Polarity = $prefix; Id = $id }
                }
            }
        }
    }
    if ($cases.Count -eq 0) { throw 'No Appendix C test cases found' }
    return $cases
}

function Get-ModelCases([string]$Root, [int[]]$Suites) {
    $seen = @{}
    foreach ($dir in Get-ChildItem -LiteralPath $Root -Directory | Sort-Object Name) {
        if ($dir.Name -notmatch '^suite(\d+)_') { continue }
        $suite = [int]$Matches[1]
        if ($suite -notin $Suites) { continue }
        foreach ($file in Get-ChildItem -LiteralPath $dir.FullName -Recurse -File -Filter '*.3mf' | Sort-Object FullName) {
            if ($file.BaseName -notmatch '^([PN])_([A-Z]{3})_(\d{4}_\d{2})(?:_.*)?$') {
                throw "Unrecognized test case filename: $($file.FullName)"
            }
            $polarity = $Matches[1]; $id = $Matches[3]
            # Save matches before the next regex operation.
            if ($file.FullName -match '[\\/]negative_test_cases[\\/]' -and $polarity -ne 'N') {
                throw "Polarity disagrees with negative folder: $file"
            }
            if ($file.FullName -match '[\\/]positive_test_cases[^\\/]*[\\/]' -and $polarity -ne 'P') {
                throw "Polarity disagrees with positive folder: $file"
            }
            $key = "$suite/$($file.Name)"
            if ($seen.ContainsKey($key)) { throw "Duplicate output basename in suite: $key" }
            $seen[$key] = $true
            [pscustomobject]@{
                Suite = $suite; Name = $file.Name; Path = $file.FullName
                Expected = $(if ($polarity -eq 'P') { 'Accept' } else { 'Reject' })
                SpecKey = "$suite/$($polarity)_$id"
            }
        }
    }
}

function Test-CliResult([string]$Path, [string]$Name, [string]$Expected, [bool]$StrictWarnings = $false) {
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        return [pscustomobject]@{ Status = 'Error'; Actual = ''; Detail = 'CLI did not write a result' }
    }
    try {
        $doc = Read-SafeXml ([IO.File]::ReadAllText($Path))
        $root = $doc.DocumentElement
        if ($root.Name -cne 'TestResult' -or $root.GetAttribute('file') -cne $Name) {
            throw 'Unexpected result root or file identity'
        }
        $actual = $root.GetAttribute('result')
        if ($actual -cnotin @('Pass', 'Warning', 'Fail')) { throw "Unknown result: $actual" }
        $issues = @($root.SelectNodes('./Issue | ./PrimaryIssue') | ForEach-Object { $_.OuterXml }) -join "`n"
        if ($actual -eq 'Fail' -and [string]::IsNullOrWhiteSpace($issues)) {
            throw 'Fail result has no diagnostic issue; cannot verify rejection'
        }
        $ok = if ($Expected -eq 'Reject') { $actual -eq 'Fail' } else {
            $actual -eq 'Pass' -or ($actual -eq 'Warning' -and -not $StrictWarnings)
        }
        [pscustomobject]@{ Status = $(if ($ok) { 'Passed' } else { 'Failed' }); Actual = $actual; Detail = $issues }
    } catch {
        [pscustomobject]@{ Status = 'Error'; Actual = ''; Detail = "Invalid CLI XML: $($_.Exception.Message)" }
    }
}

function Invoke-CliProcess([string]$Java, [string[]]$Arguments, [string]$Directory, [int]$TimeoutSeconds) {
    # Java arguments here are paths/options, never shell commands. Escape for Windows argv parsing.
    $quoted = @($Arguments | ForEach-Object {
        if ($_ -match '["\r\n]') { throw 'Quotes/newlines in Java arguments are unsupported' }
        '"' + ($_ -replace '(\\+)$', '$1$1') + '"'
    }) -join ' '
    $start = New-Object System.Diagnostics.ProcessStartInfo
    $start.FileName = $Java
    $start.Arguments = $quoted
    $start.WorkingDirectory = $Directory
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.WindowStyle = [Diagnostics.ProcessWindowStyle]::Hidden
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $start
    $started = $false
    try {
        [void]$process.Start()
        $started = $true
        [IO.File]::WriteAllText((Join-Path $Directory 'process-id.txt'), [string]$process.Id)
        $stdout = $process.StandardOutput.ReadToEndAsync()
        $stderr = $process.StandardError.ReadToEndAsync()
        $finished = $process.WaitForExit($TimeoutSeconds * 1000)
        if (-not $finished) { $process.Kill(); $process.WaitForExit() }
        [IO.File]::WriteAllText((Join-Path $Directory 'stdout.log'), $stdout.GetAwaiter().GetResult())
        [IO.File]::WriteAllText((Join-Path $Directory 'stderr.log'), $stderr.GetAwaiter().GetResult())
        [pscustomobject]@{ TimedOut = -not $finished; ExitCode = $process.ExitCode }
    } finally {
        # Also stop this child on cancellation or an output/logging exception.
        if ($started -and -not $process.HasExited) { $process.Kill(); $process.WaitForExit() }
        $process.Dispose()
    }
}

function Write-JUnitReport($Results, [string]$Path) {
    $doc = New-Object Xml.XmlDocument
    $root = $doc.CreateElement('testsuite')
    [void]$doc.AppendChild($root)
    $root.SetAttribute('name', '3MF CLI specification 2.4.1 acceptance')
    $root.SetAttribute('tests', [string]@($Results).Count)
    $root.SetAttribute('failures', [string]@($Results | Where-Object Status -eq 'Failed').Count)
    $root.SetAttribute('errors', [string]@($Results | Where-Object Status -eq 'Error').Count)
    foreach ($result in $Results) {
        $case = $doc.CreateElement('testcase')
        $case.SetAttribute('classname', "suite$($result.Suite)")
        $case.SetAttribute('name', $result.Name)
        [void]$root.AppendChild($case)
        if ($result.Status -ne 'Passed') {
            $tag = if ($result.Status -eq 'Failed') { 'failure' } else { 'error' }
            $failure = $doc.CreateElement($tag)
            $failure.SetAttribute('message', "Expected $($result.Expected); actual $($result.Actual). $($result.Detail)")
            [void]$case.AppendChild($failure)
        }
    }
    $doc.Save($Path)
}
