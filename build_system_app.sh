#!/bin/bash

echo "🏗️  Building MyRecents as System App"
echo "===================================="
echo ""

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ gradlew not found. Please run this script from the project root directory."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo "📋 System App Configuration:"
echo "  - sharedUserId: android.uid.system"
echo "  - System permissions enabled"
echo "  - Platform signature required"
echo ""

echo "🧹 Cleaning project..."
./gradlew clean

if [ $? -ne 0 ]; then
    echo "❌ Clean failed"
    exit 1
fi

echo "✅ Clean successful"
echo ""

echo "🔨 Building system app APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 Build successful!"
    echo ""
    
    # Check APK details
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        size=$(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)
        echo "📦 System APK Details:"
        echo "  - Location: app/build/outputs/apk/debug/app-debug.apk"
        echo "  - Size: $size"
        echo "  - Type: System App (android.uid.system)"
        
        # Check APK content using aapt if available
        if command -v aapt &> /dev/null; then
            echo "  - Package: $(aapt dump badging app/build/outputs/apk/debug/app-debug.apk | grep package | head -1)"
            echo "  - Shared User ID: $(aapt dump badging app/build/outputs/apk/debug/app-debug.apk | grep 'sharedUserId')"
        fi
    fi
    
    echo ""
    echo "🔐 System App Installation Requirements:"
    echo "1. Device must be rooted OR"
    echo "2. APK must be signed with platform key OR"
    echo "3. Install to /system/app/ manually"
    echo ""
    
    echo "📱 Installation Options:"
    echo ""
    echo "Option 1 - ADB Install (requires root):"
    echo "  adb root"
    echo "  adb remount"
    echo "  adb install -r app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    
    echo "Option 2 - System Partition Install:"
    echo "  adb root"
    echo "  adb remount"
    echo "  adb push app/build/outputs/apk/debug/app-debug.apk /system/app/MyRecents/"
    echo "  adb shell chmod 644 /system/app/MyRecents/app-debug.apk"
    echo "  adb reboot"
    echo ""
    
    echo "Option 3 - Platform Signed Install:"
    echo "  1. Sign APK with platform.pk8 and platform.x509.pem"
    echo "  2. adb install -r signed-app.apk"
    echo ""
    
    echo "🧪 Test System App:"
    echo "  adb shell am broadcast -a com.android.systemui.recents.ACTION_SHOW"
    echo "  adb logcat | grep -E '(RecentsActivity|TaskManager)'"
    echo ""
    
    echo "✅ System app build complete!"
    
else
    echo ""
    echo "❌ Build failed!"
    echo ""
    echo "🔍 Common system app build issues:"
    echo "1. Signature conflicts with existing system apps"
    echo "2. Permission declaration errors"
    echo "3. Shared user ID conflicts"
    echo ""
    echo "Please check the build errors above."
    exit 1
fi

echo ""
echo "===================================="
echo "🏁 System App Build Complete!"