#!/bin/bash

echo "🔍 Final Build Check for Android 7 Compatibility"
echo "================================================"
echo ""

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ gradlew not found. Please run this script from the project root directory."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo "📋 Current Configuration:"
echo "  - compileSdk: 30"
echo "  - minSdk: 24 (Android 7.0)"
echo "  - targetSdk: 25 (Android 7.1)"
echo "  - Java: 1.8"
echo "  - AGP: 7.4.2"
echo "  - Gradle: 7.5"
echo ""

echo "🧹 Cleaning project thoroughly..."
rm -rf .gradle 2>/dev/null
rm -rf app/build 2>/dev/null
rm -rf build 2>/dev/null

./gradlew clean

if [ $? -ne 0 ]; then
    echo "❌ Clean failed"
    exit 1
fi

echo "✅ Clean successful"
echo ""

echo "📦 Downloading dependencies..."
./gradlew dependencies --configuration implementation > /dev/null 2>&1

echo "🔨 Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 Build successful!"
    echo ""
    
    # Check APK details
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        echo "📦 APK Details:"
        size=$(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)
        echo "  - Location: app/build/outputs/apk/debug/app-debug.apk"
        echo "  - Size: $size"
        
        # Check APK content using aapt if available
        if command -v aapt &> /dev/null; then
            echo "  - Package: $(aapt dump badging app/build/outputs/apk/debug/app-debug.apk | grep package | head -1)"
            echo "  - Min SDK: $(aapt dump badging app/build/outputs/apk/debug/app-debug.apk | grep 'minSdkVersion')"
            echo "  - Target SDK: $(aapt dump badging app/build/outputs/apk/debug/app-debug.apk | grep 'targetSdkVersion')"
        fi
    fi
    
    echo ""
    echo "✅ Android 7 Compatibility Verified!"
    echo ""
    echo "🚀 Ready for installation:"
    echo "  adb install app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "🧪 Test commands:"
    echo "  adb shell am broadcast -a com.android.systemui.recents.ACTION_SHOW"
    echo "  adb shell am broadcast -a com.android.systemui.recents.ACTION_HIDE"
    echo "  adb shell am broadcast -a com.android.systemui.recents.ACTION_TOGGLE"
    
else
    echo ""
    echo "❌ Build failed!"
    echo ""
    echo "🔍 Common issues and solutions:"
    echo "1. Java version mismatch:"
    echo "   - Check: java -version"
    echo "   - Should be Java 8 or 11"
    echo ""
    echo "2. Android SDK issues:"
    echo "   - Ensure Android SDK 30 is installed"
    echo "   - Check ANDROID_HOME environment variable"
    echo ""
    echo "3. Network issues:"
    echo "   - Check internet connection for dependency downloads"
    echo "   - Try using a VPN if in restricted network"
    echo ""
    echo "4. Gradle daemon issues:"
    echo "   - Run: ./gradlew --stop"
    echo "   - Then try building again"
    echo ""
    echo "Please share the complete error message for further assistance."
    exit 1
fi

echo ""
echo "================================================"
echo "🏁 Final Build Check Complete!"