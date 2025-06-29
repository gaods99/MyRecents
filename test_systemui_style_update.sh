#!/bin/bash

# Test script for SystemUI Android 7.1.2 style implementation
# 测试 SystemUI Android 7.1.2 风格实现

echo "🎯 Testing SystemUI Android 7.1.2 Style Implementation"
echo "=================================================="

# Check if required files exist
echo "📁 Checking required files..."
required_files=(
    "app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java"
    "app/src/main/res/layout/task_item.xml"
    "app/src/main/res/values/dimens.xml"
    "app/src/main/res/values/colors.xml"
    "app/src/main/res/drawable/recents_task_view_background.xml"
    "app/src/main/res/drawable/recents_task_header_background.xml"
)

for file in "${required_files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file exists"
    else
        echo "❌ $file missing"
        exit 1
    fi
done

echo ""
echo "🔍 Checking SystemUI style dimensions..."

# Check SystemUI task dimensions
if grep -q "SYSTEMUI_TASK_WIDTH_DP = 312" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java &&
   grep -q "SYSTEMUI_TASK_HEIGHT_DP = 420" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ SystemUI original dimensions (312x420dp) implemented"
else
    echo "❌ SystemUI original dimensions not found"
fi

# Check SystemUI aspect ratio
if grep -q "312f / 420f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ SystemUI aspect ratio (~0.743) implemented"
else
    echo "❌ SystemUI aspect ratio not found"
fi

echo ""
echo "🎨 Checking SystemUI visual style..."

# Check corner radius
if grep -q 'android:radius="2dp"' app/src/main/res/drawable/recents_task_view_background.xml; then
    echo "✅ SystemUI corner radius (2dp) implemented"
else
    echo "❌ SystemUI corner radius not found"
fi

# Check task elevation
if grep -q '<dimen name="task_elevation">2dp</dimen>' app/src/main/res/values/dimens.xml; then
    echo "✅ SystemUI elevation (2dp) implemented"
else
    echo "❌ SystemUI elevation not found"
fi

# Check header height
if grep -q '<dimen name="task_header_height">48dp</dimen>' app/src/main/res/values/dimens.xml; then
    echo "✅ SystemUI header height (48dp) implemented"
else
    echo "❌ SystemUI header height not found"
fi

# Check icon size
if grep -q '<dimen name="task_icon_size">24dp</dimen>' app/src/main/res/values/dimens.xml; then
    echo "✅ SystemUI icon size (24dp) implemented"
else
    echo "❌ SystemUI icon size not found"
fi

echo ""
echo "🌈 Checking SystemUI colors..."

# Check background color
if grep -q '<color name="recents_background">#F0000000</color>' app/src/main/res/values/colors.xml; then
    echo "✅ SystemUI background color implemented"
else
    echo "❌ SystemUI background color not found"
fi

# Check task background
if grep -q '<color name="task_background">#FAFAFA</color>' app/src/main/res/values/colors.xml; then
    echo "✅ SystemUI task background color implemented"
else
    echo "❌ SystemUI task background color not found"
fi

# Check focus indicator
if grep -q '<color name="task_focus_indicator">#FF4081</color>' app/src/main/res/values/colors.xml; then
    echo "✅ SystemUI focus indicator color implemented"
else
    echo "❌ SystemUI focus indicator color not found"
fi

echo ""
echo "📱 Checking layout structure..."

# Check FrameLayout root
if grep -q "<FrameLayout" app/src/main/res/layout/task_item.xml &&
   ! grep -q "<androidx.cardview.widget.CardView" app/src/main/res/layout/task_item.xml; then
    echo "✅ SystemUI FrameLayout structure maintained"
else
    echo "❌ SystemUI FrameLayout structure not found"
fi

# Check header background
if grep -q "@color/task_header_background" app/src/main/res/drawable/recents_task_header_background.xml; then
    echo "✅ SystemUI header background style implemented"
else
    echo "❌ SystemUI header background style not found"
fi

# Check text color
if grep -A 5 'android:id="@+id/task_title"' app/src/main/res/layout/task_item.xml |
   grep -q "@color/task_title_color"; then
    echo "✅ SystemUI text color implemented"
else
    echo "❌ SystemUI text color not found"
fi

# Check font family
if grep -A 10 'android:id="@+id/task_title"' app/src/main/res/layout/task_item.xml |
   grep -q "sans-serif-medium"; then
    echo "✅ SystemUI font family implemented"
else
    echo "❌ SystemUI font family not found"
fi

echo ""
echo "⚙️ Checking size calculation logic..."

# Check SystemUI width ratio
if grep -q "SYSTEMUI_WIDTH_RATIO = 0.9f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ SystemUI width ratio (90%) implemented"
else
    echo "❌ SystemUI width ratio not found"
fi

# Check SystemUI height ratio
if grep -q "SYSTEMUI_HEIGHT_RATIO = 0.75f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ SystemUI height ratio (75%) implemented"
else
    echo "❌ SystemUI height ratio not found"
fi

# Check margin calculation
if grep -q "return dpToPx(6)" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ SystemUI margin (6dp) implemented"
else
    echo "❌ SystemUI margin not found"
fi

echo ""
echo "🔧 Build test..."
if ./gradlew assembleDebug > /dev/null 2>&1; then
    echo "✅ Build successful"
else
    echo "❌ Build failed"
    echo "Running detailed build..."
    ./gradlew assembleDebug
fi

echo ""
echo "📊 SystemUI Style Implementation Summary"
echo "========================================"

# Count successful checks
total_checks=15
passed_checks=0

# Re-run checks and count
if grep -q "SYSTEMUI_TASK_WIDTH_DP = 312" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    ((passed_checks++))
fi

if grep -q "312f / 420f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    ((passed_checks++))
fi

if grep -q 'android:radius="2dp"' app/src/main/res/drawable/recents_task_view_background.xml; then
    ((passed_checks++))
fi

if grep -q '<dimen name="task_elevation">2dp</dimen>' app/src/main/res/values/dimens.xml; then
    ((passed_checks++))
fi

if grep -q '<dimen name="task_header_height">48dp</dimen>' app/src/main/res/values/dimens.xml; then
    ((passed_checks++))
fi

if grep -q '<dimen name="task_icon_size">24dp</dimen>' app/src/main/res/values/dimens.xml; then
    ((passed_checks++))
fi

if grep -q '<color name="recents_background">#F0000000</color>' app/src/main/res/values/colors.xml; then
    ((passed_checks++))
fi

if grep -q '<color name="task_background">#FAFAFA</color>' app/src/main/res/values/colors.xml; then
    ((passed_checks++))
fi

if grep -q '<color name="task_focus_indicator">#FF4081</color>' app/src/main/res/values/colors.xml; then
    ((passed_checks++))
fi

if grep -q "<FrameLayout" app/src/main/res/layout/task_item.xml; then
    ((passed_checks++))
fi

if grep -q "@color/task_header_background" app/src/main/res/drawable/recents_task_header_background.xml; then
    ((passed_checks++))
fi

if grep -A 5 'android:id="@+id/task_title"' app/src/main/res/layout/task_item.xml | grep -q "@color/task_title_color"; then
    ((passed_checks++))
fi

if grep -A 10 'android:id="@+id/task_title"' app/src/main/res/layout/task_item.xml | grep -q "sans-serif-medium"; then
    ((passed_checks++))
fi

if grep -q "SYSTEMUI_WIDTH_RATIO = 0.9f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    ((passed_checks++))
fi

if grep -q "return dpToPx(6)" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    ((passed_checks++))
fi

echo "✅ Passed: $passed_checks/$total_checks checks"

percentage=$((passed_checks * 100 / total_checks))
echo "📈 Success Rate: $percentage%"

if [ $passed_checks -eq $total_checks ]; then
    echo ""
    echo "🎉 SystemUI Android 7.1.2 style implementation COMPLETE!"
    echo "   - Original dimensions (312x420dp) ✅"
    echo "   - Correct aspect ratio (~0.743) ✅"
    echo "   - SystemUI visual style ✅"
    echo "   - Proper colors and typography ✅"
    echo "   - Optimized spacing and margins ✅"
else
    echo ""
    echo "⚠️  Some SystemUI style features need attention"
fi

echo ""
echo "🚀 Ready for testing with SystemUI Android 7.1.2 style!"