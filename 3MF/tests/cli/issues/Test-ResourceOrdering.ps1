[CmdletBinding()]
param([string]$TestSuiteRoot, [string]$Java = 'java', [string]$Javac = 'javac')
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Conformance.ps1')
if (-not $TestSuiteRoot) { $TestSuiteRoot = Join-Path $PSScriptRoot '../../../../../test_suites' }
$project = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$fixtures = (Resolve-Path -LiteralPath $TestSuiteRoot).Path
$schema = Join-Path $fixtures 'Consolidated_Schema/Schema'
$run = Join-Path $project ('tests/cli/results/resource-ordering-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
$classes = Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$sources = @(Get-ChildItem (Join-Path $project 'src') -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
[IO.File]::WriteAllLines((Join-Path $run 'sources.txt'), [string[]]$sources, [Text.UTF8Encoding]::new($false))
$compiled = Invoke-CliProcess (Get-Command $Javac).Source @('-encoding', 'UTF-8', '-cp', (Join-Path $project 'jar/*'), '-d', $classes, ('@' + (Join-Path $run 'sources.txt'))) $run 180
if ($compiled.TimedOut -or $compiled.ExitCode -ne 0) { throw "Compilation failed; see $run/stderr.log" }
Copy-Item -LiteralPath (Join-Path $project 'src/resources') -Destination $classes -Recurse
Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.IO.Compression
if (-not ('ResourceOrderingSchemaResolver' -as [type])) {
    Add-Type -ReferencedAssemblies 'System.Xml' -TypeDefinition @'
using System;
using System.Xml;

public sealed class ResourceOrderingSchemaResolver : XmlUrlResolver
{
    private readonly Uri localXmlSchema;

    public ResourceOrderingSchemaResolver(string xmlSchemaPath)
    {
        localXmlSchema = new Uri(xmlSchemaPath);
    }

    public override Uri ResolveUri(Uri baseUri, string relativeUri)
    {
        Uri resolved = base.ResolveUri(baseUri, relativeUri);
        // The schema graph imports xml.xsd through both local and W3C URLs.
        // Canonicalize them before loading to avoid duplicate xml:lang declarations.
        if ((resolved.Host == "www.w3.org" || resolved.Host == "w3.org")
            && (resolved.AbsolutePath == "/2001/xml.xsd"
                || resolved.AbsolutePath == "/2009/01/xml.xsd"))
            return localXmlSchema;
        if (!resolved.IsFile)
            throw new XmlException("Expected a local schema import: " + resolved);
        return resolved;
    }
}
'@
}
$schemas = [Xml.Schema.XmlSchemaSet]::new()
$schemas.XmlResolver = New-Object ResourceOrderingSchemaResolver (Join-Path $schema 'xml.xsd')
[void]$schemas.Add('http://schemas.microsoft.com/3dmanufacturing/core/2015/02', (Join-Path $schema 'qli_3MF.xsd'))
$schemas.Compile()
$cases = @(
    @{ Name = 'core-control'; Kind = ''; After = $false; Required = ''; Expected = 'Accept' }
    @{ Name = 'volume-before'; Kind = 'volume'; After = $false; Required = 'v'; Expected = 'Accept' }
    @{ Name = 'volume-after'; Kind = 'volume'; After = $true; Required = 'v'; Expected = 'Accept' }
    @{ Name = 'volume-after-alias'; Kind = 'volume'; After = $true; Required = 'volumeAlias'; Expected = 'Accept' }
    @{ Name = 'implicit-before'; Kind = 'implicit'; After = $false; Required = 'v i'; Expected = 'Accept' }
    @{ Name = 'implicit-after'; Kind = 'implicit'; After = $true; Required = 'v i'; Expected = 'Accept' }
    @{ Name = 'implicit-after-only-implicit'; Kind = 'implicit'; After = $true; Required = 'i'; Expected = 'Accept' }
    @{ Name = 'volume-after-only-implicit'; Kind = 'volume'; After = $true; Required = 'i'; Expected = 'Accept' }
    @{ Name = 'implicit-after-only-volume'; Kind = 'implicit'; After = $true; Required = 'v'; Expected = 'Accept' }
    @{ Name = 'implicit-after-alias'; Kind = 'implicit'; After = $true; Required = 'implicitAlias'; Expected = 'Accept' }
    @{ Name = 'volume-after-whitespace'; Kind = 'volume'; After = $true; Required = " `t m `n volumeAlias  "; Expected = 'Accept' }
    @{ Name = 'volume-after-not-required'; Kind = 'volume'; After = $true; Required = ''; Expected = 'Reject' }
    @{ Name = 'implicit-after-not-required'; Kind = 'implicit'; After = $true; Required = ''; Expected = 'Reject' }
    @{ Name = 'volume-after-recommended'; Kind = 'volume'; After = $true; Required = ''; Recommended = 'v i'; Expected = 'Reject' }
    @{ Name = 'volume-after-unrelated-required'; Kind = 'volume'; After = $true; Required = 'm'; Expected = 'Reject' }
    @{ Name = 'materials-after'; Kind = 'material'; After = $true; Required = 'v'; Expected = 'Reject' }
    @{ Name = 'materials-after-implicit'; Kind = 'material'; After = $true; Required = 'i'; Expected = 'Reject' }
)
$results = foreach ($case in $cases) {
    $work = Join-Path $run $case.Name
    [void](New-Item -ItemType Directory -Path $work)
    $model = Join-Path $work ($case.Name + '.3mf')
    Copy-Item (Join-Path $fixtures 'suite3_core/positive_test_cases/P_XXX_0101_01.3mf') $model
    $zip = [IO.Compression.ZipFile]::Open($model, [IO.Compression.ZipArchiveMode]::Update)
    try {
        $entry = $zip.GetEntry('3D/3dmodel.model')
        $reader = [IO.StreamReader]::new($entry.Open())
        try { $doc = Read-SafeXml ($reader.ReadToEnd()) } finally { $reader.Dispose() }
        $root = $doc.DocumentElement
        $v = 'http://schemas.3mf.io/3dmanufacturing/volumetric/2022/01'
        $i = 'http://schemas.3mf.io/3dmanufacturing/implicit/2023/12'
        $root.SetAttribute('xmlns:v', $v)
        $root.SetAttribute('xmlns:volumeAlias', $v)
        $root.SetAttribute('xmlns:i', $i)
        $root.SetAttribute('xmlns:implicitAlias', $i)
        # .NET validation needs prefixes for schema-supplied foreign defaults.
        $schemaRoot = (Read-SafeXml ([IO.File]::ReadAllText((Join-Path $schema 'qli_3MF.xsd')))).DocumentElement
        foreach ($attribute in $schemaRoot.Attributes) {
            if ($attribute.Prefix -eq 'xmlns' -and $attribute.LocalName -notin @('xml', 'xs')) {
                $root.SetAttribute($attribute.Name, $attribute.Value)
            }
        }
        $root.RemoveAttribute('requiredextensions')
        if ($case.Required) { $root.SetAttribute('requiredextensions', $case.Required) }
        $root.RemoveAttribute('recommendedextensions')
        if ($case.ContainsKey('Recommended')) { $root.SetAttribute('recommendedextensions', $case.Recommended) }
        # Supply UUIDs required by the consolidated schema on older core fixtures.
        foreach ($element in $doc.SelectNodes('//*[local-name()="object" or local-name()="build" or local-name()="item" or local-name()="component"]')) {
            $attr = $doc.CreateAttribute('p', 'UUID', 'http://schemas.microsoft.com/3dmanufacturing/production/2015/06')
            $attr.Value = [guid]::NewGuid().ToString()
            [void]$element.Attributes.Append($attr)
        }
        $resources = $doc.SelectSingleNode('//*[local-name()="resources"]')
        $extra = switch ($case.Kind) {
            'volume' { '<v:volumedata xmlns:v="' + $v + '" id="900" />' }
            'implicit' { '<i:implicitfunction xmlns:i="' + $i + '" id="900"><i:in/><i:out/></i:implicitfunction>' }
            'material' { '<basematerials xmlns="' + $root.NamespaceURI + '" id="900"><base name="unused" displaycolor="#FFFFFFFF"/></basematerials>' }
        }
        if ($extra) {
            $fragment = $doc.CreateDocumentFragment()
            $fragment.InnerXml = $extra
            if ($case.After) { [void]$resources.AppendChild($fragment) }
            else { [void]$resources.InsertBefore($fragment, $resources.FirstChild) }
        }
        # Independently prove each generated model passes the consolidated XSD.
        $settings = [Xml.XmlReaderSettings]::new()
        $settings.Schemas = $schemas
        $settings.ValidationType = [Xml.ValidationType]::Schema
        $settings.DtdProcessing = [Xml.DtdProcessing]::Prohibit
        $settings.XmlResolver = $null
        $schemaReader = [Xml.XmlReader]::Create([IO.StringReader]::new($doc.OuterXml), $settings)
        try { while ($schemaReader.Read()) {} } finally { $schemaReader.Dispose() }
        $entry.Delete()
        $writer = [IO.StreamWriter]::new($zip.CreateEntry('3D/3dmodel.model').Open(), [Text.UTF8Encoding]::new($false))
        try { $writer.Write($doc.OuterXml) } finally { $writer.Dispose() }
    } finally { $zip.Dispose() }
    foreach ($folder in @('ztmp', 'imagetmp', 'zips')) { [void](New-Item -ItemType Directory -Path (Join-Path $work $folder)) }
    [IO.File]::WriteAllLines((Join-Path $work 'files.txt'), [string[]]@($model), [Text.UTF8Encoding]::new($false))
    @(('pschemaLoc = "' + $schema.Replace('\', '/') + '"'), 'iErrMaxStructure = "100"', 'iErrMaxValidataion = "100"') | Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
    'pNoUUIDchk = "false"' | Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII
    $execution = Invoke-CliProcess (Get-Command $Java).Source @('-cp', ($classes + [IO.Path]::PathSeparator + (Join-Path $project 'jar/*')), 'com.qualitylogic.x3mf.StartUp', '-C', '-L', 'files.txt', '-I') $work 180
    $report = Join-Path $work ('cliResults/' + $case.Name + '.xml')
    $checked = Test-CliResult $report ([IO.Path]::GetFileName($model)) $case.Expected
    $problems = [Collections.Generic.List[string]]::new()
    if ($checked.Status -ne 'Passed') { $problems.Add($checked.Detail) }
    if ($execution.TimedOut -or $execution.ExitCode -ne 0) { $problems.Add('CLI crashed or timed out') }
    $hasOrderingError = $false
    if (Test-Path -LiteralPath $report) {
        $xml = [IO.File]::ReadAllText($report)
        $html = [IO.File]::ReadAllText([IO.Path]::ChangeExtension($report, '.html'))
        $hasOrderingError = $xml.Contains('ER_103') -and $html.Contains('ER_103')
        if ($case.Expected -eq 'Reject' -and -not $hasOrderingError) { $problems.Add('Missing ER_103 in XML or HTML') }
        if ($case.Expected -eq 'Accept' -and ($xml.Contains('ER_103') -or $html.Contains('ER_103'))) { $problems.Add('Spurious ER_103') }
    }
    [pscustomobject]@{ Suite = 'issue16'; Name = $case.Name; Schema = 'Pass'; Expected = $case.Expected; Actual = $checked.Actual; ER103 = $hasOrderingError; Status = $(if ($problems.Count) { 'Failed' } else { 'Passed' }); Detail = ($problems -join '; '); Report = $report }
}
$results | Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
Write-JUnitReport $results (Join-Path $run 'junit.xml')
$results | Select-Object Name, Schema, Expected, Actual, ER103, Status | Format-Table -AutoSize
Write-Host "Reports: $run"
if (@($results | Where-Object Status -ne 'Passed').Count) { exit 1 }
