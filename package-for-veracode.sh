#!/bin/bash
# Script to package Java project for Veracode SAST scanning
# This creates a ZIP file containing compiled code and dependencies

echo "Creating Veracode package for 3MF Editor..."

cd 3MF

# Clean and create directories
rm -rf bin veracode-package
mkdir -p bin
mkdir -p veracode-package

echo "Compiling Java source..."
javac -cp "jar/*" -d bin -sourcepath src src/com/qualitylogic/x3mf/*.java

if [ $? -ne 0 ]; then
    echo "ERROR: Compilation failed!"
    exit 1
fi

echo "Copying resources..."
cp -r src/resources bin/

echo "Creating JAR manifest..."
mkdir -p META-INF
cat > META-INF/MANIFEST.MF << EOF
Manifest-Version: 1.0
Main-Class: com.qualitylogic.x3mf.StartUp
EOF

echo "Creating application JAR..."
jar cfm 3MFedit.jar META-INF/MANIFEST.MF -C bin .

echo "Packaging for Veracode..."
cp 3MFedit.jar veracode-package/
cp jar/*.jar veracode-package/

echo "Creating ZIP package..."
cd veracode-package
zip -r ../3MFedit-veracode.zip .
cd ..

echo ""
echo "========================================"
echo "Veracode package created successfully!"
echo "========================================"
echo "Package location: 3MF/3MFedit-veracode.zip"
echo ""
echo "Next steps:"
echo "1. Go to https://analysiscenter.veracode.com"
echo "2. Navigate to Scans & Analysis > Static Analysis"
echo "3. Upload 3MFedit-veracode.zip"
echo "4. Start the scan"
echo ""
