[CmdletBinding()]
param([string]$TestSuiteRoot, [string]$Java='java', [string]$Javac='javac')
$ErrorActionPreference='Stop'
. (Join-Path $PSScriptRoot '../../Conformance.ps1')
if(-not $TestSuiteRoot){$TestSuiteRoot=Join-Path $PSScriptRoot '../../../../../../test_suites'}
$project=(Resolve-Path (Join-Path $PSScriptRoot '../../../..')).Path
$fixtures=(Resolve-Path $TestSuiteRoot).Path
$schema=Join-Path $fixtures 'Consolidated_Schema/Schema'
$run=Join-Path $project ('tests/cli/results/validation-consumer-'+(Get-Date -Format 'yyyyMMdd-HHmmss')+'-'+[guid]::NewGuid().ToString('N').Substring(0,8))
$classes=Join-Path $run 'classes'
[void](New-Item -ItemType Directory -Path $classes -Force)
$sources=@(Get-ChildItem (Join-Path $project 'src') -Recurse -Filter '*.java'|ForEach-Object {'"'+$_.FullName.Replace('\','/')+'"'})
foreach($name in @('ValidationConsumerTest','ModelRulesTest','ModelSchemaTest','AlternativePolicyTest')){$sources+='"'+(Join-Path $project ('tests/cli/src/'+$name+'.java')).Replace('\','/')+'"'}
[IO.File]::WriteAllLines((Join-Path $run 'sources.txt'),[string[]]$sources,[Text.UTF8Encoding]::new($false))
$compiled=Invoke-CliProcess (Get-Command $Javac).Source @('--release','8','-encoding','UTF-8','-cp',(Join-Path $project 'jar/*'),'-d',$classes,('@'+(Join-Path $run 'sources.txt'))) $run 180
Get-Content (Join-Path $run 'stdout.log'),(Join-Path $run 'stderr.log')|Set-Content (Join-Path $run 'compile.log') -Encoding UTF8
if($compiled.TimedOut -or $compiled.ExitCode -ne 0){throw "Compilation failed: $run/compile.log"}
Copy-Item (Join-Path $project 'src/resources') $classes -Recurse
$classpath=$classes+[IO.Path]::PathSeparator+(Join-Path $project 'jar/*')
foreach($helper in @('ValidationConsumerTest','ModelRulesTest','ModelSchemaTest','AlternativePolicyTest')){
    $arguments=@('-cp',$classpath,('com.qualitylogic.x3mf.'+$helper))
    if($helper -eq 'ModelSchemaTest'){$arguments+=@((Join-Path $schema 'qli_3MF.xsd'),(Join-Path $fixtures 'suite3_core/positive_test_cases/P_XXX_0101_01.3mf'))}
    if($helper -eq 'AlternativePolicyTest'){$arguments+=$run}
    $result=Invoke-CliProcess (Get-Command $Java).Source $arguments $run 60
    Get-Content (Join-Path $run 'stdout.log'),(Join-Path $run 'stderr.log')|Set-Content (Join-Path $run ($helper+'.log')) -Encoding UTF8
    if($result.TimedOut -or $result.ExitCode -ne 0){throw "Helper failed: $run/$helper.log"}
}
$names=@('P_XPM_0101_01.3mf','N_XPM_0421_01.3mf','N_DPX_3314_07.3mf','N_EPX_2602_01.3mf','N_EPX_2602_02.3mf','N_EPX_2602_03.3mf','N_EPX_2602_04.3mf','N_EPX_2604_01.3mf','N_EPX_2604_02.3mf','N_EPX_2604_03.3mf','N_EPX_2604_04.3mf','P_XPX_0722_01.3mf','P_XPX_0722_02.3mf','P_XPX_0722_03.3mf','P_XPX_0722_04.3mf','P_EPX_0740_01.3mf','P_EPX_0740_02.3mf','P_EPX_0740_03.3mf','P_EPX_0740_04.3mf','P_EPX_0740_05.3mf','P_EPX_0740_06.3mf','N_XPM_0428_01.3mf')
$names+=@('N_SPX_0421_01.3mf','N_SXX_0421_01.3mf')
$models=@(Get-ChildItem $fixtures -Recurse -Filter '*.3mf'|Where-Object Name -in $names)
if($models.Count -ne $names.Count){throw 'Missing or duplicate consumer fixture'}
$results=[Collections.Generic.List[object]]::new()
foreach($consumer in @('printer','editor')){
    foreach($mode in @('all','schema','structure','hidden')){
        $work=Join-Path $run ($consumer+'-'+$mode)
        [void](New-Item -ItemType Directory -Path $work)
        @(('pschemaLoc = "'+$schema.Replace('\','/')+'"'),'iErrMaxStructure = "100"','iErrMaxValidataion = "100"')|Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
        @('pNoUUIDchk = "false"',('validationConsumer = "'+$consumer+'"'))|Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII
        [IO.File]::WriteAllLines((Join-Path $work 'files.txt'),[string[]]@($models|ForEach-Object FullName),[Text.UTF8Encoding]::new($false))
        $options=@('-I');if($mode -eq 'schema'){$options=@('-V')};if($mode -eq 'structure'){$options=@('-S')};if($mode -eq 'hidden'){$options+=@('-W')}
        Write-Host "Checking $consumer / $mode"
        $execution=Invoke-CliProcess (Get-Command $Java).Source (@('-Xmx2048m','-cp',$classpath,'com.qualitylogic.x3mf.StartUp','-C','-L','files.txt')+$options) $work 1200
        foreach($model in $models){
            $expected='Accept';$code=''
            if($model.Name -eq 'N_XPM_0428_01.3mf'){$expected='Reject'}
            if($consumer -eq 'printer'){
                if($model.Name -match '^N_EPX_260[24]_|^P_EPX_0740_0[56]|^P_XPX_0722_04'){$expected='Reject';$code='ER_203'}
                if($mode -ne 'schema' -and $model.Name -match '^N_(XPM|SPX|SXX)_0421_01.3mf$'){$expected='Reject';$code='ER_202'}
                if($mode -ne 'schema' -and $model.Name -eq 'N_DPX_3314_07.3mf'){$expected='Reject';$code='ER_201'}
            }
            $report=Join-Path $work ('cliResults/'+[IO.Path]::ChangeExtension($model.Name,'.xml'))
            $checked=Test-CliResult $report $model.Name $expected
            if($execution.TimedOut -or $execution.ExitCode -ne 0){$checked.Status='Error';$checked.Detail='CLI crashed or timed out'}
            if(Test-Path $report){$doc=Read-SafeXml ([IO.File]::ReadAllText($report));if($doc.DocumentElement.GetAttribute('consumer') -ne $consumer){$checked.Status='Failed';$checked.Detail+=' Wrong report consumer'};if($code -and -not $doc.SelectSingleNode('//*[@issueID="'+$code+'"]')){$checked.Status='Failed';$checked.Detail+=' Missing '+$code}}
            $results.Add([pscustomobject]@{Suite=$consumer;Name=$mode+'/'+$model.Name;Expected=$expected;Actual=$checked.Actual;Status=$checked.Status;Detail=$checked.Detail;Report=$report})
        }
        foreach($aggregate in Get-ChildItem (Join-Path $work 'cliResults') -Filter 'XmlAggregate*.xml'){$null=Read-SafeXml ([IO.File]::ReadAllText($aggregate.FullName))}
        $results|Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
    }
}
# Default, legacy INI and CLI precedence are checked through real single-file invocations.
foreach($probe in @(
    @{Name='no-ini';Ini=$null;Cli=@();Consumer='printer';Expected='Reject'}
    @{Name='legacy-ini';Ini='pNoUUIDchk = "false"';Cli=@();Consumer='printer';Expected='Reject'}
    @{Name='override-editor';Ini='validationConsumer = "printer"';Cli=@('--consumer','editor');Consumer='editor';Expected='Accept'}
    @{Name='override-printer';Ini='validationConsumer = "editor"';Cli=@('--consumer','printer');Consumer='printer';Expected='Reject'}
    @{Name='invalid-cli';Ini='validationConsumer = "printer"';Cli=@('--consumer','viewer');Consumer='';Expected='UsageError'}
    @{Name='invalid-ini';Ini='validationConsumer = "viewer"';Cli=@();Consumer='';Expected='UsageError'}
)){
    $work=Join-Path $run $probe.Name;[void](New-Item -ItemType Directory -Path $work)
    if($null -ne $probe.Ini){$probe.Ini|Set-Content (Join-Path $work '3MF.ini') -Encoding ASCII}
    @(('pschemaLoc = "'+$schema.Replace('\','/')+'"'),'iErrMaxStructure = "100"','iErrMaxValidataion = "100"')|Set-Content (Join-Path $work '3MF.txt') -Encoding ASCII
    $model=$models|Where-Object Name -eq 'N_XPM_0421_01.3mf'
    $execution=Invoke-CliProcess (Get-Command $Java).Source (@('-Xmx2048m','-cp',$classpath,'com.qualitylogic.x3mf.StartUp','-C','-F',$model.FullName,'-I')+$probe.Cli) $work 120
    $report=Join-Path $work 'cliResults/N_XPM_0421_01.xml'
    if($probe.Expected -eq 'UsageError'){$checked=[pscustomobject]@{Status=$(if(-not $execution.TimedOut -and $execution.ExitCode -ne 0 -and -not (Test-Path $report)){'Passed'}else{'Failed'});Actual='UsageError';Detail=''}}else{
        $checked=Test-CliResult $report $model.Name $probe.Expected
        if($execution.TimedOut -or $execution.ExitCode -ne 0){$checked.Status='Error'}
        $doc=Read-SafeXml ([IO.File]::ReadAllText($report));if($doc.DocumentElement.GetAttribute('consumer') -ne $probe.Consumer){$checked.Status='Failed'}
        if($probe.Name -like 'override-*' -and -not ([IO.File]::ReadAllText((Join-Path $work '3MF.ini'))).Contains($probe.Ini)){$checked.Status='Failed';$checked.Detail+=' CLI override was persisted'}
    }
    $results.Add([pscustomobject]@{Suite='configuration';Name=$probe.Name;Expected=$probe.Expected;Actual=$checked.Actual;Status=$checked.Status;Detail=$checked.Detail;Report=$report})
}
$results|Export-Csv (Join-Path $run 'results.csv') -NoTypeInformation -Encoding UTF8
Write-JUnitReport $results (Join-Path $run 'junit.xml')
$failed=@($results|Where-Object Status -ne Passed).Count
[pscustomobject]@{Total=$results.Count;Passed=$results.Count-$failed;Failed=$failed;Consumers=@('printer','editor');Modes=@('all','schema','structure','hidden')}|ConvertTo-Json|Set-Content (Join-Path $run 'summary.json') -Encoding UTF8
$results|Where-Object Status -ne Passed|Format-Table Suite,Name,Expected,Actual,Status,Detail -Wrap
Write-Host ($results.Count.ToString()+' CLI/configuration checks; '+$failed+' failures')
Write-Host "Reports: $run"
if(@($results|Where-Object Status -ne Passed).Count){exit 1}
exit 0
