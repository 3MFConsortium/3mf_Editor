[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$RunDirectory,
    [string]$Specification
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'Conformance.ps1')

function Get-WordText($Node, $Namespace) {
    (@($Node.SelectNodes('.//w:p', $Namespace) | ForEach-Object {
        ($_.SelectNodes('.//w:t', $Namespace) | ForEach-Object { $_.InnerText }) -join ''
    }) -join "`n").Trim()
}

$RunDirectory = (Resolve-Path -LiteralPath $RunDirectory).Path
$summary = Get-Content (Join-Path $RunDirectory 'summary.json') -Raw | ConvertFrom-Json
if (-not $summary.Completed) { throw 'Choose a completed conformance run.' }
if (-not $Specification) { $Specification = $summary.Specification }
$Specification = (Resolve-Path -LiteralPath $Specification).Path
if ((Get-FileHash $Specification -Algorithm SHA256).Hash -ne $summary.SpecificationSHA256) {
    throw 'The specification differs from the document used by the original run.'
}
$rows = @(Import-Csv (Join-Path $RunDirectory 'results.csv'))
$inventory = @(Import-Csv (Join-Path $RunDirectory 'inventory.csv'))
$appendix = Get-SpecificationCases $Specification
$models = @{}; foreach ($model in $inventory) { $models["$($model.Suite)/$($model.Name)"] = $model }

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::OpenRead($Specification)
try {
    $reader = [IO.StreamReader]::new($zip.GetEntry('word/document.xml').Open())
    try { $doc = Read-SafeXml $reader.ReadToEnd() } finally { $reader.Dispose() }
} finally { $zip.Dispose() }
$ns = [Xml.XmlNamespaceManager]::new($doc.NameTable)
$ns.AddNamespace('w', 'http://schemas.openxmlformats.org/wordprocessingml/2006/main')
$catalog = @{}; $pending = $null
foreach ($node in $doc.SelectNodes('//w:body/*', $ns)) {
    if ($node.LocalName -eq 'p') {
        $text = ($node.SelectNodes('.//w:t', $ns) | ForEach-Object { $_.InnerText }) -join ''
        $key = ''; $normalization = ''
        if ($text -match '^\s*([PN])\s*_\s*[A-Z?]{3}\s*_\s*(\d{3,4})(?!\d)') {
            $key = $Matches[1] + '_' + $Matches[2].PadLeft(4, '0')
            if ($Matches[2].Length -eq 3) { $normalization = 'Three-digit scenario number padded to four digits (document typo).' }
        } elseif ($text -match '^\s*PP_(\d{3,4})(?!\d)') {
            # The actual document calls the P_0701 and P_0704 headings PP_0701 / PP_704.
            $key = 'P_' + $Matches[1].PadLeft(4, '0')
            $normalization = 'PP heading normalized to positive scenario; number padded to four digits.'
        }
        if ($key) {
            $pending = [pscustomobject]@{ Key = $key; Heading = $text.Trim(); Note = $normalization }
        }
    }
    if ($node.LocalName -ne 'tbl' -or $null -eq $pending) { continue }
    $fields = @{}
    foreach ($row in $node.SelectNodes('./w:tr', $ns)) {
        $cells = @($row.SelectNodes('./w:tc', $ns))
        if ($cells.Count -lt 2) { continue }
        $label = Get-WordText $cells[0] $ns
        if ($label -match '^(Test Scenario Description|Pass/Fail Criteria|Test Case Iterations|Requirement Reference|Conformance Statement ID\(s\))$') {
            $fields[$label] = Get-WordText $cells[1] $ns
        }
    }
    if (-not $fields.ContainsKey('Test Scenario Description')) { continue }
    if ($catalog.ContainsKey($pending.Key)) { throw "Duplicate scenario definition: $($pending.Key)" }
    $catalog[$pending.Key] = [pscustomobject]@{
        Heading = $pending.Heading; Normalization = $pending.Note
        Description = $fields['Test Scenario Description']
        Criteria = $fields['Pass/Fail Criteria']; Iterations = $fields['Test Case Iterations']
        Reference = (@($fields['Requirement Reference'], $fields['Conformance Statement ID(s)']) | Where-Object { $_ }) -join '; '
        ScenarioText = Get-WordText $node $ns
    }
    $pending = $null
}

$comparison = foreach ($row in $rows) {
    $key = "$($row.Suite)/$($row.Name)"
    $scenarioKey = ''; $iteration = ''; $specKey = ''; $expected = $row.Expected
    $scenario = $null; $review = ''; $iterationText = ''
    if ($models.ContainsKey($key)) {
        $model = $models[$key]; $specKey = $model.SpecKey
        if ($model.SpecKey -notmatch '^\d+/([PN]_\d{4})_(\d{2})$') { throw "Invalid specification key: $($model.SpecKey)" }
        $scenarioKey = $Matches[1]; $iteration = $Matches[2]
        $expected = if ($scenarioKey.StartsWith('P_')) { 'Accept' } else { 'Reject' }
        if ($expected -ne $row.Expected) { throw "Expectation disagrees with model polarity: $key" }
        if ($catalog.ContainsKey($scenarioKey)) {
            $scenario = $catalog[$scenarioKey]
            # Keep the exact selected iteration text where the document uses numbered paragraphs.
            $capture = $false
            foreach ($line in ($scenario.Iterations -split '\r?\n')) {
                if ($line -match '^\s*(\d{1,2})\s*[-\u2013\u2014:]') {
                    $capture = [int]$Matches[1] -eq [int]$iteration
                }
                if ($capture) { $iterationText += $line + "`n" }
            }
            $iterationText = $iterationText.Trim()
        } else { $review = 'Scenario definition not located; naming expectation only.' }
        if ($scenarioKey -in @('P_0720', 'P_0722', 'P_0740', 'N_2604')) {
            $review = 'Consumer-specific criteria: printer and viewer/editor expectations can differ. Review scenario and iteration; do not infer full conformance from the filename.'
        }
    }
    $observed = $row.Actual; $evidence = 'Validated XML result'
    if ($row.Expected -eq 'Present') { $evidence = 'Missing fixture' }
    elseif ($row.Status -eq 'Error') {
        $evidence = 'No trustworthy result available'
        # Read only a separately validated opening element, never repair or accept a broken report.
        if ($row.Report -and (Test-Path -LiteralPath $row.Report)) {
            $raw = [IO.File]::ReadAllText($row.Report)
            $opening = [regex]::Match($raw, '<TestResult\b[^>]*>')
            if ($opening.Success) {
                try {
                    $header = Read-SafeXml ($opening.Value + '</TestResult>')
                    $element = $header.DocumentElement
                    $value = $element.GetAttribute('result')
                    if ($element.GetAttribute('file') -ceq $row.Name -and $value -cin @('Pass','Warning','Fail')) {
                        $observed = $value
                        $evidence = 'Header only; full XML report is invalid or execution failed. Provisional decision, not a verified test pass.'
                    }
                } catch { } # Preserve the original error and unknown outcome.
            }
        }
    }
    $decision = 'Unavailable'
    if ($observed -cin @('Pass','Warning','Fail') -and $expected -in @('Accept','Reject')) {
        $decisionMatches = if ($expected -eq 'Reject') { $observed -eq 'Fail' } else {
            $observed -eq 'Pass' -or ($observed -eq 'Warning' -and -not $summary.StrictWarnings)
        }
        $decision = if ($decisionMatches) { 'Matches acceptance expectation' } else { 'Does not match acceptance expectation' }
    }
    $verified = switch ($row.Status) { 'Passed' { 'Match' } 'Failed' { 'Mismatch' } default { 'Unverifiable' } }
    if ($row.Expected -eq 'Present') { $verified = 'Missing fixture' }
    [pscustomobject]@{
        Suite = $row.Suite; Test = $row.Name; ScenarioKey = $scenarioKey; Iteration = $iteration
        ExpectedAcceptance = $expected; ObservedCliResult = $observed
        AcceptanceVerification = $verified; ReportedDecisionMatch = $decision; Evidence = $evidence
        AppendixC = $(if ($specKey -and $appendix.ContainsKey($specKey)) { 'Listed' } elseif ($row.Expected -eq 'Present') { 'Listed but missing' } else { 'Not listed; still tested' })
        Scenario = $(if ($scenario) { $scenario.Heading } else { '' })
        SpecificationCriteria = $(if ($scenario) { $scenario.Criteria } else { '' })
        IterationDescription = $iterationText
        RequirementReference = $(if ($scenario) { $scenario.Reference } else { '' })
        DefinitionNormalization = $(if ($scenario) { $scenario.Normalization } else { '' })
        DetailedCriteriaReview = $review
        FullCriteriaVerification = 'Not established: CLI acceptance does not verify rendering, selection, printed appearance, or the correctness of the rejection reason.'
        OriginalDiagnostic = $row.Detail; OriginalResult = $row.Status; Report = $row.Report
    }
}
if (@($comparison).Count -ne $rows.Count) { throw 'Comparison omitted result rows.' }
$out = Join-Path $RunDirectory 'specification-match'
[void](New-Item -ItemType Directory -Path $out -Force)
$comparison | Export-Csv (Join-Path $out 'expected-vs-observed.csv') -NoTypeInformation -Encoding UTF8
$catalog.GetEnumerator() | Sort-Object Key | ForEach-Object {
    [pscustomobject]@{ Key = $_.Key; Heading = $_.Value.Heading; Criteria = $_.Value.Criteria; Iterations = $_.Value.Iterations; Reference = $_.Value.Reference; Normalization = $_.Value.Normalization; ScenarioText = $_.Value.ScenarioText }
} | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $out 'scenario-definitions.json') -Encoding UTF8
$counts = [ordered]@{
    Models = $summary.Models; VerifiedAcceptanceMatches = $summary.Passed
    VerifiedAcceptanceMismatches = $summary.Failed
    UnverifiableModelReports = @($comparison | Where-Object AcceptanceVerification -eq 'Unverifiable').Count
    MissingFixtures = @($comparison | Where-Object AcceptanceVerification -eq 'Missing fixture').Count
    ConsumerSpecificModels = @($comparison | Where-Object { $_.DetailedCriteriaReview -like 'Consumer-specific*' }).Count
    AcceptanceMatchesOutsideConsumerSpecificCases = @($comparison | Where-Object { $_.DetailedCriteriaReview -notlike 'Consumer-specific*' -and $_.AcceptanceVerification -eq 'Match' }).Count
    AcceptanceMismatchesOutsideConsumerSpecificCases = @($comparison | Where-Object { $_.DetailedCriteriaReview -notlike 'Consumer-specific*' -and $_.AcceptanceVerification -eq 'Mismatch' }).Count
    ModelsWithoutScenarioDefinition = @($comparison | Where-Object { $_.ExpectedAcceptance -ne 'Present' -and -not $_.Scenario }).Count
    ProvisionalHeaderMatches = @($comparison | Where-Object { $_.Evidence -like 'Header only*' -and $_.ReportedDecisionMatch -eq 'Matches acceptance expectation' }).Count
    ProvisionalHeaderMismatches = @($comparison | Where-Object { $_.Evidence -like 'Header only*' -and $_.ReportedDecisionMatch -eq 'Does not match acceptance expectation' }).Count
    SpecificationSHA256 = $summary.SpecificationSHA256
}
$counts | ConvertTo-Json | Set-Content (Join-Path $out 'summary.json') -Encoding UTF8
$report = [Collections.Generic.List[string]]::new()
$report.Add('# Specification comparison for the completed CLI run')
$report.Add('')
$report.Add('This compares the saved run with the same SHA256-identified v2.4.1 specification, without rerunning Java or changing the original results. Match means acceptance/rejection agrees with the positive/negative expectation in section 5.1. It does not establish full rendering or printer conformance.')
$report.Add('')
$report.Add('| Models | Expected decision | Matches | Mismatches | Invalid/unverifiable reports |')
$report.Add('| --- | --- | ---: | ---: | ---: |')
foreach ($expectation in @('Accept','Reject')) {
    $selected = @($comparison | Where-Object ExpectedAcceptance -eq $expectation)
    $label = if ($expectation -eq 'Accept') { 'Positive' } else { 'Negative' }
    $matchCount = @($selected | Where-Object AcceptanceVerification -eq 'Match').Count
    $mismatchCount = @($selected | Where-Object AcceptanceVerification -eq 'Mismatch').Count
    $unverifiedCount = @($selected | Where-Object AcceptanceVerification -eq 'Unverifiable').Count
    $report.Add("| $label | $expectation | $matchCount | $mismatchCount | $unverifiedCount |")
}
$report.Add('')
$report.Add("Additionally, $($counts.MissingFixtures) Appendix C fixtures are missing from the checkout. All $($counts.Models) discovered models have scenario definitions linked in expected-vs-observed.csv. The CSV includes the scenario title, verbatim criteria, available iteration text, requirement references, actual result, original diagnostic and evidence quality.")
$report.Add('')
$report.Add('## Consumer-specific and manual criteria')
$report.Add('')
$report.Add("The $($counts.ConsumerSpecificModels) models in P_0720, P_0722, P_0740 and N_2604 need consumer-specific interpretation. Production Alternatives can require an exception for a printer while permitting a viewer to render; P_0740 iteration 06 expects an exception for both. N_2604 explicitly allows editors to ignore inaccessible content. The CSV flags all of these, without silently changing the existing naming-based totals. Outside these flagged cases there are $($counts.AcceptanceMatchesOutsideConsumerSpecificCases) acceptance matches and $($counts.AcceptanceMismatchesOutsideConsumerSpecificCases) mismatches.")
$report.Add('')
$report.Add('Rendering, selected alternative geometry, material/color appearance and physical printing remain unverified. Even a negative case that is rejected can be rejected for an unrelated defect; this comparison does not certify the diagnostic reason. Section 5.3 and the scenario definitions require observations beyond what the CLI reports.')
$report.Add('')
$report.Add('## Malformed reports')
$report.Add('')
$report.Add("The $($counts.UnverifiableModelReports) invalid reports remain unverifiable. Their separately parsed opening TestResult elements indicate $($counts.ProvisionalHeaderMatches) provisional naming-based decision matches and $($counts.ProvisionalHeaderMismatches) provisional mismatches. These header-only readings are informational and never promote an original error to a verified pass.")
$report.Add('')
$report.Add('## Source traceability')
$report.Add('')
$report.Add('The document has nonstandard headings N_???_417, PP_0701 and PP_704; the comparison records normalization to N_0417, P_0701 and P_0704. Appendix C omissions/additional checkout cases remain separately identified. P_0720 has no labeled Pass/Fail Criteria cell, so its complete scenario text is retained in scenario-definitions.json rather than inventing a criterion.')
$report.Add('')
$report.Add('Source: ' + $summary.SpecificationUrl)
$report.Add('')
$report.Add('Specification SHA256: ' + $summary.SpecificationSHA256)
$report | Set-Content (Join-Path $out 'comparison.md') -Encoding UTF8
Write-Output ($counts | ConvertTo-Json)
Write-Output "Comparison reports: $out"
