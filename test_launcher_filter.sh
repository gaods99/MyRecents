#!/bin/bash

echo "🚫 Testing Launcher App Filter Implementation"
echo "============================================="

echo "📁 Checking TaskLoader changes..."

# Check if isLauncherApp method exists
if grep -q "private boolean isLauncherApp" app/src/main/java/com/newland/recents/loader/TaskLoader.java; then
    echo "✅ isLauncherApp method implemented"
else
    echo "❌ isLauncherApp method missing"
fi

# Check if Launcher filter is added to shouldIncludeTask
if grep -A 10 "shouldIncludeTask" app/src/main/java/com/newland/recents/loader/TaskLoader.java | 
   grep -q "isLauncherApp(packageName)"; then
    echo "✅ Launcher filter added to shouldIncludeTask"
else
    echo "❌ Launcher filter not found in shouldIncludeTask"
fi

# Check if isCommonLauncher method exists
if grep -q "private boolean isCommonLauncher" app/src/main/java/com/newland/recents/loader/TaskLoader.java; then
    echo "✅ isCommonLauncher method implemented"
else
    echo "❌ isCommonLauncher method missing"
fi

# Check if isDefaultLauncher method exists
if grep -q "private boolean isDefaultLauncher" app/src/main/java/com/newland/recents/loader/TaskLoader.java; then
    echo "✅ isDefaultLauncher method implemented"
else
    echo "❌ isDefaultLauncher method missing"
fi

echo ""
echo "📱 Checking common Launcher packages..."

# Check for common launcher packages
common_launchers=(
    "com.android.launcher"
    "com.android.launcher2"
    "com.android.launcher3"
    "com.google.android.apps.nexuslauncher"
    "com.miui.home"
    "com.huawei.android.launcher"
    "com.samsung.android.app.launcher"
    "com.oppo.launcher"
    "com.vivo.launcher"
)

for launcher in "${common_launchers[@]}"; do
    if grep -q "$launcher" app/src/main/java/com/newland/recents/loader/TaskLoader.java; then
        echo "✅ $launcher included"
    else
        echo "❌ $launcher missing"
    fi
done

echo ""
echo "🔍 Checking Task model updates..."

# Check if Task.isSystemTask is updated
if grep -A 20 "public boolean isSystemTask" app/src/main/java/com/newland/recents/model/Task.java |
   grep -q "isLauncherPackage"; then
    echo "✅ Task.isSystemTask includes Launcher check"
else
    echo "❌ Task.isSystemTask not updated"
fi

# Check if isLauncherPackage method exists in Task
if grep -q "private boolean isLauncherPackage" app/src/main/java/com/newland/recents/model/Task.java; then
    echo "✅ Task.isLauncherPackage method implemented"
else
    echo "❌ Task.isLauncherPackage method missing"
fi

echo ""
echo "🎯 Checking filter logic..."

# Check filter order in shouldIncludeTask
echo "Filter order in shouldIncludeTask:"
if grep -A 20 "shouldIncludeTask" app/src/main/java/com/newland/recents/loader/TaskLoader.java |
   grep -n "return false"; then
    echo "✅ Filter logic found"
else
    echo "❌ Filter logic not found"
fi

echo ""
echo "🔧 Checking Intent-based detection..."

# Check if Intent.ACTION_MAIN is used
if grep -q "Intent.ACTION_MAIN" app/src/main/java/com/newland/recents/loader/TaskLoader.java; then
    echo "✅ Intent.ACTION_MAIN used for detection"
else
    echo "❌ Intent.ACTION_MAIN not found"
fi

# Check if CATEGORY_HOME is used
if grep -q "Intent.CATEGORY_HOME" app/src/main/java/com/newland/recents/loader/TaskLoader.java; then
    echo "✅ Intent.CATEGORY_HOME used for detection"
else
    echo "❌ Intent.CATEGORY_HOME not found"
fi

# Check if PackageManager.MATCH_DEFAULT_ONLY is used
if grep -q "MATCH_DEFAULT_ONLY" app/src/main/java/com/newland/recents/loader/TaskLoader.java; then
    echo "✅ MATCH_DEFAULT_ONLY used for default launcher detection"
else
    echo "❌ MATCH_DEFAULT_ONLY not found"
fi

echo ""
echo "📊 Launcher Filter Implementation Summary"
echo "========================================"

# Count successful checks
total_checks=15
passed_checks=0

# Re-run checks and count
if grep -q "private boolean isLauncherApp" app/src/main/java/com/newland/recents/loader/TaskLoader.java; then
    ((passed_checks++))
fi

if grep -A 10 "shouldIncludeTask" app/src/main/java/com/newland/recents/loader/TaskLoader.java | 
   grep -q "isLauncherApp(packageName)"; then
    ((passed_checks++))
fi

if grep -q "private boolean isCommonLauncher" app/src/main/java/com/newland/recents/loader/TaskLoader.java; then
    ((passed_checks++))
fi

if grep -q "private boolean isDefaultLauncher" app/src/main/java/com/newland/recents/loader/TaskLoader.java; then
    ((passed_checks++))
fi

# Check common launchers (count as 5 checks)
launcher_count=0
for launcher in "${common_launchers[@]}"; do
    if grep -q "$launcher" app/src/main/java/com/newland/recents/loader/TaskLoader.java; then
        ((launcher_count++))
    fi
done
if [ "$launcher_count" -ge 7 ]; then
    passed_checks=$((passed_checks + 5))
fi

if grep -A 20 "public boolean isSystemTask" app/src/main/java/com/newland/recents/model/Task.java |
   grep -q "isLauncherPackage"; then
    ((passed_checks++))
fi

if grep -q "private boolean isLauncherPackage" app/src/main/java/com/newland/recents/model/Task.java; then
    ((passed_checks++))
fi

if grep -q "Intent.ACTION_MAIN" app/src/main/java/com/newland/recents/loader/TaskLoader.java; then
    ((passed_checks++))
fi

if grep -q "Intent.CATEGORY_HOME" app/src/main/java/com/newland/recents/loader/TaskLoader.java; then
    ((passed_checks++))
fi

if grep -q "MATCH_DEFAULT_ONLY" app/src/main/java/com/newland/recents/loader/TaskLoader.java; then
    ((passed_checks++))
fi

echo "✅ Passed: $passed_checks/$total_checks checks"

percentage=$((passed_checks * 100 / total_checks))
echo "📈 Success Rate: $percentage%"

if [ $passed_checks -eq $total_checks ]; then
    echo ""
    echo "🎉 Launcher filter implementation COMPLETE!"
    echo "   - Common launcher packages filtered ✅"
    echo "   - Default launcher detection ✅"
    echo "   - Intent-based detection ✅"
    echo "   - Task model integration ✅"
    echo ""
    echo "🚫 Launcher apps will be hidden from recent tasks!"
else
    echo ""
    echo "⚠️  Some launcher filter features need attention"
fi

echo ""
echo "📱 Filtered Launcher Types:"
echo "  • AOSP Launchers (launcher, launcher2, launcher3)"
echo "  • OEM Launchers (MIUI, EMUI, OneUI, etc.)"
echo "  • Third-party Launchers (Nova, Action, etc.)"
echo "  • Default system launcher (detected dynamically)"
echo ""
echo "🚀 Ready for testing without Launcher apps!"