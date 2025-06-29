#!/bin/bash

# MyRecents Test Script

echo "=== MyRecents Testing Script ==="
echo ""

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "❌ ADB not found. Please install Android SDK platform-tools."
    exit 1
fi

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No Android device connected. Please connect a device and enable USB debugging."
    exit 1
fi

echo "✅ ADB found and device connected"
echo ""

# Function to test broadcast
test_broadcast() {
    local action=$1
    local description=$2
    
    echo "📱 Testing: $description"
    echo "   Broadcasting: $action"
    
    adb shell am broadcast -a "$action" 2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo "   ✅ Broadcast sent successfully"
    else
        echo "   ❌ Failed to send broadcast"
    fi
    
    echo ""
    sleep 2
}

# Test all broadcast actions
echo "🧪 Testing Recents Broadcasts..."
echo ""

test_broadcast "com.android.systemui.recents.ACTION_SHOW" "Show Recents"
test_broadcast "com.android.systemui.recents.ACTION_HIDE" "Hide Recents"
test_broadcast "com.android.systemui.recents.ACTION_TOGGLE" "Toggle Recents"

# Check if app is installed
echo "📦 Checking app installation..."
if adb shell pm list packages | grep -q "com.newland.recents"; then
    echo "✅ MyRecents app is installed"
else
    echo "❌ MyRecents app is not installed"
    echo "   Please install the app first:"
    echo "   ./gradlew assembleDebug"
    echo "   adb install app/build/outputs/apk/debug/app-debug.apk"
fi

echo ""
echo "🔍 Checking app permissions..."

# Check permissions
permissions=("android.permission.PACKAGE_USAGE_STATS" "android.permission.GET_TASKS" "android.permission.REORDER_TASKS")

for perm in "${permissions[@]}"; do
    if adb shell dumpsys package com.newland.recents | grep -q "$perm"; then
        echo "✅ Permission declared: $perm"
    else
        echo "❌ Permission missing: $perm"
    fi
done

echo ""
echo "📋 Manual Test Checklist:"
echo "1. ✓ Send ACTION_SHOW broadcast"
echo "2. ✓ Verify RecentsActivity opens"
echo "3. ✓ Check task list displays correctly"
echo "4. ✓ Test task click to launch app"
echo "5. ✓ Test task dismiss functionality"
echo "6. ✓ Test empty state when no tasks"
echo "7. ✓ Send ACTION_HIDE broadcast"
echo "8. ✓ Send ACTION_TOGGLE broadcast"

echo ""
echo "🎯 Quick Commands:"
echo "Show Recents:   adb shell am broadcast -a com.android.systemui.recents.ACTION_SHOW"
echo "Hide Recents:   adb shell am broadcast -a com.android.systemui.recents.ACTION_HIDE"
echo "Toggle Recents: adb shell am broadcast -a com.android.systemui.recents.ACTION_TOGGLE"
echo "View Logs:      adb logcat | grep -E '(RecentsActivity|TaskLoader|TaskManager)'"

echo ""
echo "=== Test Complete ==="