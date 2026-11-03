[CmdletBinding()]
param([Parameter(Mandatory=$true)][string]$RunDirectory,[string]$TestSuiteRoot)
$ErrorActionPreference='Stop'
. (Join-Path $PSScriptRoot '../../Conformance.ps1')
$project=(Resolve-Path (Join-Path $PSScriptRoot '../../../..')).Path
$RunDirectory=(Resolve-Path $RunDirectory).Path
if(-not $TestSuiteRoot){$TestSuiteRoot=Join-Path $PSScriptRoot '../../../../../../test_suites'}
$fixtures=(Resolve-Path $TestSuiteRoot).Path
$classes=Join-Path $RunDirectory 'classes'
if(-not (Test-Path (Join-Path $classes 'com/qualitylogic/x3mf/StartUp.class'))){throw 'RunDirectory must contain freshly compiled classes.'}
foreach($source in Import-Csv (Join-Path $RunDirectory 'source-sha256.csv')){
    if((Get-FileHash (Join-Path $project $source.Path) -Algorithm SHA256).Hash -ne $source.SHA256){throw 'Sources changed since compilation; create a fresh conformance run.'}
}
$work=Join-Path $RunDirectory ('fixture-repair-controls-'+[guid]::NewGuid().ToString('N').Substring(0,8))
[void](New-Item -ItemType Directory -Path $work)
$schema=Join-Path $fixtures 'Consolidated_Schema/Schema'
@( ('pschemaLoc = "'+$schema.Replace('\','/')+'"'),'iErrMaxStructure = "100"','iErrMaxValidataion = "100"')|Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
@('pNoUUIDchk = "true"','validationConsumer = "printer"')|Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII
Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.IO.Compression
$notes=@((Get-Content (Join-Path $project 'tests/cli/FixtureDiscrepancies.json') -Raw|ConvertFrom-Json)|Where-Object Category -eq 'Fixture defect')
$models=@(Get-ChildItem $fixtures -Recurse -Filter '*.3mf')
$controls=foreach($note in $notes){
    $source=@($models|Where-Object Name -eq $note.Name)
    if($source.Count -ne 1 -or (Get-FileHash $source[0].FullName -Algorithm SHA256).Hash -ne $note.SHA256){throw 'Fixture differs from the documented archive.'}
    $target=Join-Path $work ('repaired-'+$note.Name)
    Copy-Item -LiteralPath $source[0].FullName -Destination $target
    $zip=[IO.Compression.ZipFile]::Open($target,[IO.Compression.ZipArchiveMode]::Update)
    $partNames=@{}
    foreach($archivePart in $zip.Entries){
        $leaf=[IO.Path]::GetFileName($archivePart.FullName)
        if(-not $partNames.ContainsKey($leaf)){$partNames[$leaf]=@()}
        $partNames[$leaf]+=$archivePart.FullName
    }
    $changed=0; $action=''; $code=''; $expected='Accept'
    try {
        foreach($entry in @($zip.Entries)){
            $name=$entry.FullName
            $relevant=($note.Name -match '_0909_' -and $name -eq '[Content_Types].xml') -or
                ($note.Name -match '_0416_|_0420_' -and $name -eq '3D/3dmodel.model') -or
                ($note.Name -match '_0915_|_1514_' -and $name -match '^3D/_rels/midway2?\.model\.rels$')
            if(-not $relevant){continue}
            $reader=[IO.StreamReader]::new($entry.Open());try{$text=$reader.ReadToEnd()}finally{$reader.Dispose()}
            if($note.Name -match '_0420_'){
                $expected='Reject';$code='SC_000';$action='Insert the DOCTYPE specified by the negative scenario'
                if($text -notmatch '<model\b' -or $text -match '<!DOCTYPE'){throw 'Unexpected DTD fixture content'}
                $text=$text -replace '<model\b',('<!DOCTYPE model [<!ENTITY issue23 "DTD regression">]>'+[Environment]::NewLine+'<model')
                $changed++
            }else{
                $doc=Read-SafeXml $text
                if($note.Name -match '_0909_'){
                    $action='Resolve bare Override names to their actual absolute /2D/ part URIs'
                    foreach($node in $doc.SelectNodes('//*[local-name()="Override"]')){
                        $part=$node.GetAttribute('PartName')
                        if($part.StartsWith('/')){continue}
                        $match=@($partNames[$part])
                        if($match.Count -ne 1){throw "Ambiguous Override repair: $part"}
                        $node.SetAttribute('PartName','/'+$match[0]);$changed++
                    }
                }elseif($note.Name -match '_0416_'){
                    $expected='Reject';$code='ER_180';$action='Make the inverted mesh authoritative so this variant actually tests negative mesh volume'
                    foreach($node in $doc.SelectNodes('//*[local-name()="object"]')){
                        $ns='http://schemas.microsoft.com/3dmanufacturing/slice/2015/07'
                        if($node.GetAttribute('meshresolution',$ns) -eq 'lowres'){[void]$node.SetAttribute('meshresolution',$ns,'fullres');$changed++}
                    }
                }else{
                    $action='Register each midway model sliceref target in its own relationships part'
                    $part=$name.Replace('/_rels/','/');$part=$part.Substring(0,$part.Length-5)
                    $reader=[IO.StreamReader]::new($zip.GetEntry($part).Open());try{$model=Read-SafeXml $reader.ReadToEnd()}finally{$reader.Dispose()}
                    $slice=$model.SelectSingleNode('//*[local-name()="sliceref"]')
                    foreach($node in $doc.SelectNodes('//*[local-name()="Relationship"]')){
                        if($node.GetAttribute('Type') -eq 'http://schemas.microsoft.com/3dmanufacturing/2013/01/3dmodel'){$node.SetAttribute('Target',$slice.GetAttribute('slicepath'));$changed++}
                    }
                }
                $text=$doc.OuterXml
            }
            $entry.Delete();$writer=[IO.StreamWriter]::new($zip.CreateEntry($name).Open(),[Text.UTF8Encoding]::new($false))
            try{$writer.Write($text)}finally{$writer.Dispose()}
        }
    }finally{$zip.Dispose()}
    if(-not $changed){throw "No repair applied: $($note.Name)"}
    if((Get-FileHash $source[0].FullName -Algorithm SHA256).Hash -ne $note.SHA256){throw 'Official fixture changed'}
    [pscustomobject]@{Suite=$note.Suite;Name=[IO.Path]::GetFileName($target);Path=$target;Expected=$expected;Code=$code;Action=$action;Changes=$changed;OriginalSHA256=$note.SHA256;RepairedSHA256=(Get-FileHash $target -Algorithm SHA256).Hash}
}
if(@($controls).Count -ne 9){throw 'Expected nine fixture repair controls.'}
# A valid first reference must not hide a missing reference on a later object.
$source=$models|Where-Object Name -eq 'N_SPX_0416_03.3mf'
$target=Join-Path $work 'missing-second-slicestack.3mf'
Copy-Item -LiteralPath $source.FullName -Destination $target
$zip=[IO.Compression.ZipFile]::Open($target,[IO.Compression.ZipArchiveMode]::Update)
try{
    $entry=$zip.GetEntry('3D/3dmodel.model');$reader=[IO.StreamReader]::new($entry.Open())
    try{$doc=Read-SafeXml $reader.ReadToEnd()}finally{$reader.Dispose()}
    $object=$doc.SelectSingleNode('//*[local-name()="object"]')
    $clone=$object.CloneNode($true)
    $clone.SetAttribute('id','2147483646')
    [void]$clone.SetAttribute('slicestackid','http://schemas.microsoft.com/3dmanufacturing/slice/2015/07','2147483647')
    [void]$clone.SetAttribute('UUID','http://schemas.microsoft.com/3dmanufacturing/production/2015/06','11111111-1111-4111-8111-111111111111')
    [void]$object.ParentNode.AppendChild($clone)
    $entry.Delete();$writer=[IO.StreamWriter]::new($zip.CreateEntry('3D/3dmodel.model').Open(),[Text.UTF8Encoding]::new($false))
    try{$writer.Write($doc.OuterXml)}finally{$writer.Dispose()}
}finally{$zip.Dispose()}
$controls+=[pscustomobject]@{Suite=1;Name='missing-second-slicestack.3mf';Path=$target;Expected='Reject';Code='ER_064';Action='Valid first slicestack followed by a missing second reference';Changes=1;OriginalSHA256=(Get-FileHash $source.FullName -Algorithm SHA256).Hash;RepairedSHA256=(Get-FileHash $target -Algorithm SHA256).Hash}
$controls|Export-Csv (Join-Path $work 'repair-manifest.csv') -NoTypeInformation -Encoding UTF8
[IO.File]::WriteAllLines((Join-Path $work 'files.txt'),[string[]]@($controls.Path),[Text.UTF8Encoding]::new($false))
$classpath=$classes+[IO.Path]::PathSeparator+(Join-Path $project 'jar/*')
$execution=Invoke-CliProcess (Get-Command java).Source @('-Xmx2048m','-cp',$classpath,'com.qualitylogic.x3mf.StartUp','-C','-L','files.txt','-I') $work 1200
$results=foreach($control in $controls){
    $report=Join-Path $work ('cliResults/'+[IO.Path]::ChangeExtension($control.Name,'.xml'))
    $checked=Test-CliResult $report $control.Name $control.Expected
    if($execution.TimedOut -or $execution.ExitCode -ne 0){$checked.Status='Error';$checked.Detail+=' Process failed'}
    if($checked.Status -ne 'Error'){
        $doc=Read-SafeXml ([IO.File]::ReadAllText($report))
        if($doc.DocumentElement.GetAttribute('consumer') -ne 'printer' -or ($control.Code -and -not $doc.SelectSingleNode('//*[@issueID="'+$control.Code+'"]'))){$checked.Status='Failed';$checked.Detail+=' Missing consumer or expected diagnostic'}
    }
    [pscustomobject]@{Suite=$control.Suite;Name=$control.Name;Expected=$control.Expected;Actual=$checked.Actual;Status=$checked.Status;Detail=$checked.Detail;Report=$report}
}
$results|Export-Csv (Join-Path $work 'results.csv') -NoTypeInformation -Encoding UTF8
Write-JUnitReport $results (Join-Path $work 'junit.xml')
$results|Select-Object Name,Expected,Actual,Status|Format-Table -AutoSize
Write-Host "Fixture repair copies and evidence: $work"
if(@($results|Where-Object Status -ne Passed).Count){exit 1}
[pscustomobject]@{Completed=$true;Controls=@($results).Count;Passed=@($results).Count;RepairedOfficialFixtureCopies=9;AdditionalReferenceControls=1;OfficialFixturesModified=$false}|ConvertTo-Json|Set-Content (Join-Path $work 'summary.json') -Encoding UTF8
$resolution=Join-Path $RunDirectory 'issue23-resolution.md'
if(Test-Path $resolution){
    @('','## Local fixture repair verification','',
        'All nine repaired fixture copies produce the expected CLI acceptance/rejection, and the missing-second-slicestack control reports ER_064 (10 checks passed). The original 22 strict mismatches above remain recorded against the unmodified official fixtures.',
        '',('[Repair manifest and hashes]('+[IO.Path]::GetFileName($work)+'/repair-manifest.csv) and [control results]('+[IO.Path]::GetFileName($work)+'/results.csv).')
    )|Add-Content $resolution -Encoding UTF8
}
exit 0
