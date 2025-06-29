#!/bin/bash

# MyRecents Build and Install Script

echo "🚀 MyRecents Build and Install Script"
echo "======================================"
echo ""

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ gradlew not found. Please run this script from the project root directory."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo "🔨 Building project..."
echo ""

# Clean and build
./gradlew clean assembleDebug

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi

echo ""
echo "✅ Build successful!"
echo ""

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "❌ ADB not found. Please install Android SDK platform-tools."
    echo "📦 APK location: app/build/outputs/apk/debug/app-debug.apk"
    exit 1
fi

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No Android device connected."
    echo "📦 APK location: app/build/outputs/apk/debug/app-debug.apk"
    echo "💡 Connect a device and run: adb install app/build/outputs/apk/debug/app-debug.apk"
    exit 1
fi

echo "📱 Installing to connected device..."
echo ""

# Install APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Installation successful!"
    echo ""
    
    # Test basic functionality
    echo "🧪 Testing basic functionality..."
    echo ""
    
    echo "📡 Sending test broadcast..."
    adb shell am broadcast -a com.android.systemui.recents.ACTION_SHOW
    
    if [ $? -eq 0 ]; then
        echo "✅ Broadcast sent successfully"
        echo ""
        echo "🎉 Setup complete! MyRecents is ready to use."
        echo ""
        echo "📋 Next steps:"
        echo "1. Grant necessary permissions in Settings > Apps > MyRecents > Permissions"
        echo "2. Enable Usage Access in Settings > Security > Usage access > MyRecents"
        echo "3. Test with: adb shell am broadcast -a com.android.systemui.recents.ACTION_SHOW"
        echo ""
        echo "🔍 View logs: adb logcat | grep -E '(RecentsActivity|TaskLoader|TaskManager)'"
    else
        echo "❌ Test broadcast failed"
    fi
else
    echo ""
    echo "❌ Installation failed. Please check the errors above."
    exit 1
fi

echo ""
echo "======================================"
echo "🏁 Build and Install Complete!"