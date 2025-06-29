#!/bin/bash

echo "=== Dynamic Task Card Sizing Test ==="
echo ""

echo "1. Building project..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✓ Build successful"
else
    echo "✗ Build failed"
    exit 1
fi

echo ""
echo "2. Installing APK..."
adb install -r app/build/outputs/apk/debug/app-debug.apk

if [ $? -eq 0 ]; then
    echo "✓ Installation successful"
else
    echo "✗ Installation failed"
    exit 1
fi

echo ""
echo "3. Testing dynamic sizing..."
echo "   - Launch the app and check task card sizes"
echo "   - Rotate device to test landscape/portrait adaptation"
echo "   - Test on different screen densities if available"

echo ""
echo "4. Monitoring logs for sizing info..."
echo "   Look for 'Dynamic sizing:' messages in logcat"
echo ""

adb logcat -c
adb shell am start -n com.newland.recents/.RecentsActivity

echo "Monitoring logs (Ctrl+C to stop):"
adb logcat | grep -E "(RecentsActivity|TaskViewSizeCalculator|Dynamic sizing)"