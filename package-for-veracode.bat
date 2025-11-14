@echo off
REM Script to package Java project for Veracode SAST scanning
REM This creates a ZIP file containing compiled code and dependencies

echo Creating Veracode package for 3MF Editor...

cd 3MF

REM Clean and create directories
if exist bin rmdir /s /q bin
if exist veracode-package rmdir /s /q veracode-package
mkdir bin
mkdir veracode-package

echo Compiling Java source...
javac -cp "jar/*" -d bin -sourcepath src src/com/qualitylogic/x3mf/*.java

if errorlevel 1 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo Copying resources...
xcopy /E /I /Y src\resources bin\resources

echo Creating JAR manifest...
mkdir META-INF
(
echo Manifest-Version: 1.0
echo Main-Class: com.qualitylogic.x3mf.StartUp
) > META-INF\MANIFEST.MF

echo Creating application JAR...
jar cfm 3MFedit.jar META-INF\MANIFEST.MF -C bin .

echo Packaging for Veracode...
copy 3MFedit.jar veracode-package\
copy jar\*.jar veracode-package\

echo Creating ZIP package...
cd veracode-package
powershell -command "Compress-Archive -Path * -DestinationPath ..\3MFedit-veracode.zip -Force"
cd ..

echo.
echo ========================================
echo Veracode package created successfully!
echo ========================================
echo Package location: 3MF\3MFedit-veracode.zip
echo.
echo Next steps:
echo 1. Go to https://analysiscenter.veracode.com
echo 2. Navigate to Scans ^& Analysis ^> Static Analysis
echo 3. Upload 3MFedit-veracode.zip
echo 4. Start the scan
echo.

pause
