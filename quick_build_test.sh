#!/bin/bash

echo "🔧 Quick Build Test for MyRecents"
echo "================================="
echo ""

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ gradlew not found. Please run this script from the project root directory."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo "🧹 Cleaning project..."
./gradlew clean

if [ $? -ne 0 ]; then
    echo "❌ Clean failed"
    exit 1
fi

echo "✅ Clean successful"
echo ""

echo "🔨 Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 Build successful!"
    echo "📦 APK location: app/build/outputs/apk/debug/app-debug.apk"
    
    # Check APK size
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        size=$(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)
        echo "📏 APK size: $size"
    fi
    
    echo ""
    echo "✅ Ready for installation and testing!"
else
    echo ""
    echo "❌ Build failed. Please check the errors above."
    echo ""
    echo "💡 Common solutions:"
    echo "1. Delete .gradle folder and try again"
    echo "2. Check Java version (should be 8 or 11)"
    echo "3. Update Android SDK if needed"
    echo "4. Check internet connection for dependency downloads"
    exit 1
fi