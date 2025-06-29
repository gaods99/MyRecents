#!/bin/bash

echo "📱 Installing MyRecents as System App"
echo "====================================="
echo ""

# Check if APK exists
if [ ! -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "❌ APK not found. Please build the app first:"
    echo "   ./build_system_app.sh"
    exit 1
fi

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "❌ ADB not found. Please install Android SDK platform-tools."
    exit 1
fi

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No Android device connected."
    echo "💡 Please connect a device and enable USB debugging."
    exit 1
fi

echo "🔍 Checking device status..."

# Check if device is rooted
adb root > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Device is rooted"
    ROOT_AVAILABLE=true
else
    echo "⚠️  Device is not rooted"
    ROOT_AVAILABLE=false
fi

echo ""
echo "📋 Installation Methods:"
echo "1. Standard install (may fail due to system permissions)"
echo "2. System partition install (requires root)"
echo "3. Replace existing system app (requires root)"
echo ""

read -p "Choose installation method (1-3): " choice

case $choice in
    1)
        echo ""
        echo "🔄 Attempting standard installation..."
        adb install -r app/build/outputs/apk/debug/app-debug.apk
        
        if [ $? -eq 0 ]; then
            echo "✅ Standard installation successful!"
        else
            echo "❌ Standard installation failed (expected for system apps)"
            echo "💡 Try method 2 or 3 if device is rooted"
        fi
        ;;
        
    2)
        if [ "$ROOT_AVAILABLE" = false ]; then
            echo "❌ Root access required for system partition install"
            exit 1
        fi
        
        echo ""
        echo "🔄 Installing to system partition..."
        
        # Remount system as read-write
        adb remount
        
        # Create app directory
        adb shell mkdir -p /system/app/MyRecents/
        
        # Push APK to system
        adb push app/build/outputs/apk/debug/app-debug.apk /system/app/MyRecents/MyRecents.apk
        
        # Set permissions
        adb shell chmod 644 /system/app/MyRecents/MyRecents.apk
        adb shell chown root:root /system/app/MyRecents/MyRecents.apk
        
        echo "✅ APK installed to system partition"
        echo "🔄 Rebooting device to activate system app..."
        
        adb reboot
        
        echo "⏳ Waiting for device to reboot..."
        adb wait-for-device
        
        echo "✅ System app installation complete!"
        ;;
        
    3)
        if [ "$ROOT_AVAILABLE" = false ]; then
            echo "❌ Root access required for system app replacement"
            exit 1
        fi
        
        echo ""
        echo "🔄 Replacing existing system app..."
        
        # Check if app already exists
        if adb shell pm list packages | grep -q "com.newland.recents"; then
            echo "📦 Existing app found, uninstalling..."
            adb uninstall com.newland.recents
        fi
        
        # Install as system app
        adb remount
        adb install -r -g app/build/outputs/apk/debug/app-debug.apk
        
        if [ $? -eq 0 ]; then
            echo "✅ System app replacement successful!"
        else
            echo "❌ System app replacement failed"
            exit 1
        fi
        ;;
        
    *)
        echo "❌ Invalid choice"
        exit 1
        ;;
esac

echo ""
echo "🧪 Testing system app installation..."

# Wait a moment for the system to recognize the app
sleep 3

# Check if app is installed
if adb shell pm list packages | grep -q "com.newland.recents"; then
    echo "✅ App is installed and recognized by system"
    
    # Test broadcast
    echo "📡 Testing system broadcast..."
    adb shell am broadcast -a com.android.systemui.recents.ACTION_SHOW
    
    if [ $? -eq 0 ]; then
        echo "✅ Broadcast sent successfully"
        echo ""
        echo "🎉 System app installation and test complete!"
        echo ""
        echo "📋 Available commands:"
        echo "  Show recents: adb shell am broadcast -a com.android.systemui.recents.ACTION_SHOW"
        echo "  Hide recents: adb shell am broadcast -a com.android.systemui.recents.ACTION_HIDE"
        echo "  Toggle recents: adb shell am broadcast -a com.android.systemui.recents.ACTION_TOGGLE"
        echo "  View logs: adb logcat | grep -E '(RecentsActivity|TaskManager)'"
    else
        echo "❌ Broadcast test failed"
    fi
else
    echo "❌ App installation verification failed"
    echo "💡 The app may need additional setup or permissions"
fi

echo ""
echo "====================================="
echo "🏁 System App Installation Complete!"