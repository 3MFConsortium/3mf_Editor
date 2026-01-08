# Veracode Scanning Guide for 3MF Editor

## Manual Packaging for Veracode SAST

Since the Veracode VS Code plugin cannot auto-package this Java project, follow these steps for manual scanning:

### Option 1: Using Local Script (Recommended)

#### Windows:
```powershell
package-for-veracode.bat
```

#### Linux/Mac:
```bash
chmod +x package-for-veracode.sh
./package-for-veracode.sh
```

This will create `3MF/3MFedit-veracode.zip` containing:
- Your compiled application JAR
- All dependency JARs

### Option 2: Using GitHub Actions

The build will automatically create the Veracode package:

1. Push your code or create a Pull Request
2. GitHub Actions will run the `code-validation.yml` workflow
3. Download the artifact named `veracode-package` from the Actions tab
4. Upload to Veracode web platform

### Option 3: Manual Packaging

```bash
cd 3MF

# Compile
mkdir -p bin
javac -cp "jar/*" -d bin -sourcepath src src/com/qualitylogic/x3mf/*.java

# Copy resources
cp -r src/resources bin/

# Create JAR
mkdir -p META-INF
echo "Manifest-Version: 1.0" > META-INF/MANIFEST.MF
echo "Main-Class: com.qualitylogic.x3mf.StartUp" >> META-INF/MANIFEST.MF
jar cfm 3MFedit.jar META-INF/MANIFEST.MF -C bin .

# Package for Veracode
mkdir veracode-package
cp 3MFedit.jar veracode-package/
cp jar/*.jar veracode-package/
cd veracode-package
zip -r ../3MFedit-veracode.zip .
```

## Uploading to Veracode

### Web Platform Upload:

1. Go to https://analysiscenter.veracode.com
2. Login with your credentials
3. Navigate to: **Scans & Analysis** → **Static Analysis**
4. Click **"Create New Scan"** or select existing application
5. Application Name: `3MF Editor`
6. Upload: `3MFedit-veracode.zip`
7. Click **"Begin Pre-Scan"**
8. After pre-scan, click **"Submit Scan"**

### Using Veracode CLI (Alternative):

```bash
# Install Veracode CLI first
# Then run:
veracode static scan 3MF/3MFedit-veracode.zip -a "3MF Editor"
```

## Why Auto-Packaging Fails

The Veracode VS Code plugin expects:
- Standard build tools (Maven, Gradle)
- Build configuration files (pom.xml, build.gradle)

This project uses:
- Eclipse project structure (.classpath, .project)
- Manual dependency management (jar/ folder)
- No standard build tool configuration

## Required for Scanning

Veracode needs:
- ✅ Compiled bytecode (.class files in JAR)
- ✅ All dependency JARs
- ✅ ZIP package format

The scripts above provide exactly this.

## Scan Results

After scanning completes (30-90 minutes):
- Check the Veracode platform for results
- Review findings by severity
- Follow remediation guidance
- Export reports as needed

## Support

For Veracode documentation:
- https://docs.veracode.com/r/compilation_java
- https://docs.veracode.com/r/About_Modules_and_Dependencies
