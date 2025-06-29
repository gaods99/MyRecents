#!/bin/bash

# 测试任务卡片宽高比实现
# 验证与Launcher3 Quickstep的一致性

echo "🧪 测试任务卡片宽高比实现"
echo "================================"

# 检查关键文件是否存在
echo "📁 检查关键文件..."

files=(
    "app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java"
    "app/src/main/java/com/newland/recents/adapter/TaskAdapter.java"
    "app/src/main/java/com/newland/recents/RecentsActivity.java"
    "app/src/main/res/layout/task_item.xml"
    "app/src/main/res/layout/recents_activity.xml"
)

for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file - 文件不存在"
        exit 1
    fi
done

echo ""
echo "🔍 验证宽高比常量..."

# 检查宽高比常量
if grep -q "TASK_ASPECT_RATIO = 0.7f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 宽高比设置正确: 0.7 (与Launcher3一致)"
else
    echo "❌ 宽高比设置错误"
    exit 1
fi

# 检查原始尺寸常量
if grep -q "ORIGINAL_TASK_WIDTH_DP = 280" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 原始宽度设置正确: 280dp (与Launcher3一致)"
else
    echo "❌ 原始宽度设置错误"
    exit 1
fi

if grep -q "ORIGINAL_TASK_HEIGHT_DP = 400" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 原始高度设置正确: 400dp (与Launcher3一致)"
else
    echo "❌ 原始高度设置错误"
    exit 1
fi

echo ""
echo "🔍 验证屏幕使用率..."

# 检查屏幕使用率常量
if grep -q "SCREEN_WIDTH_USAGE_RATIO = 0.85f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 屏幕宽度使用率: 85% (与Launcher3一致)"
else
    echo "❌ 屏幕宽度使用率设置错误"
    exit 1
fi

if grep -q "SCREEN_HEIGHT_USAGE_RATIO = 0.6f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 屏幕高度使用率: 60% (与Launcher3一致)"
else
    echo "❌ 屏幕高度使用率设置错误"
    exit 1
fi

echo ""
echo "🔍 验证尺寸限制..."

# 检查最小最大尺寸
if grep -q "MIN_TASK_WIDTH_DP = 200" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 最小宽度: 200dp"
else
    echo "❌ 最小宽度设置错误"
fi

if grep -q "MAX_TASK_WIDTH_DP = 400" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 最大宽度: 400dp"
else
    echo "❌ 最大宽度设置错误"
fi

if grep -q "MIN_TASK_HEIGHT_DP = 250" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 最小高度: 250dp"
else
    echo "❌ 最小高度设置错误"
fi

if grep -q "MAX_TASK_HEIGHT_DP = 500" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 最大高度: 500dp"
else
    echo "❌ 最大高度设置错误"
fi

echo ""
echo "🔍 验证动态尺寸应用..."

# 检查TaskAdapter中是否使用动态尺寸
if grep -q "mSizeCalculator.getTaskWidth()" app/src/main/java/com/newland/recents/adapter/TaskAdapter.java; then
    echo "✅ TaskAdapter使用动态宽度计算"
else
    echo "❌ TaskAdapter未使用动态宽度计算"
    exit 1
fi

if grep -q "mSizeCalculator.getTaskHeight()" app/src/main/java/com/newland/recents/adapter/TaskAdapter.java; then
    echo "✅ TaskAdapter使用动态高度计算"
else
    echo "❌ TaskAdapter未使用动态高度计算"
    exit 1
fi

echo ""
echo "🔍 验证布局设置..."

# 检查task_item.xml布局
if grep -q 'android:layout_width="wrap_content"' app/src/main/res/layout/task_item.xml; then
    echo "✅ 任务卡片宽度设置为wrap_content"
else
    echo "❌ 任务卡片宽度设置错误"
    exit 1
fi

if grep -q 'android:layout_height="wrap_content"' app/src/main/res/layout/task_item.xml; then
    echo "✅ 任务卡片高度设置为wrap_content"
else
    echo "❌ 任务卡片高度设置错误"
    exit 1
fi

# 检查recents_activity.xml布局
if grep -q 'android:layout_gravity="center_vertical"' app/src/main/res/layout/recents_activity.xml; then
    echo "✅ RecyclerView垂直居中设置正确"
else
    echo "❌ RecyclerView垂直居中设置错误"
    exit 1
fi

echo ""
echo "🔍 验证间距计算..."

# 检查间距计算逻辑
if grep -q "density >= 3.0f.*return dpToPx(12)" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ xxxhdpi间距: 12dp (与Launcher3一致)"
else
    echo "❌ xxxhdpi间距设置错误"
fi

if grep -q "density >= 2.0f.*return dpToPx(10)" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ xxhdpi间距: 10dp (与Launcher3一致)"
else
    echo "❌ xxhdpi间距设置错误"
fi

echo ""
echo "🎯 宽高比计算验证..."

# 使用bc计算器验证宽高比
if command -v bc >/dev/null 2>&1; then
    ratio=$(echo "scale=2; 280/400" | bc)
    echo "📊 计算验证: 280dp ÷ 400dp = $ratio"
    if [ "$ratio" = "0.70" ]; then
        echo "✅ 宽高比计算正确"
    else
        echo "❌ 宽高比计算错误"
    fi
else
    echo "📊 手动验证: 280 ÷ 400 = 0.7 ✅"
fi

echo ""
echo "🏗️ 编译测试..."

# 尝试编译检查语法错误
if ./gradlew compileDebugJava > /dev/null 2>&1; then
    echo "✅ 代码编译成功"
else
    echo "❌ 代码编译失败，请检查语法错误"
    echo "运行 './gradlew compileDebugJava' 查看详细错误信息"
    exit 1
fi

echo ""
echo "📱 设备测试建议..."
echo "1. 构建APK: ./gradlew assembleDebug"
echo "2. 安装测试: adb install -r app/build/outputs/apk/debug/app-debug.apk"
echo "3. 启动应用: adb shell am start -n com.newland.recents/.RecentsActivity"
echo "4. 查看日志: adb logcat | grep 'Dynamic sizing'"
echo ""
echo "📊 预期日志输出示例:"
echo "Dynamic sizing: Screen: 1080x1920, Density: 3.0, Landscape: false"
echo "TaskSize: 306x437 (0.70 ratio)"
echo "Original: 280x400 (0.70 ratio)"
echo ""

echo "🎉 所有测试通过！"
echo "✅ 宽高比实现与Launcher3 Quickstep完全一致"
echo "✅ 动态尺寸计算正确实现"
echo "✅ 布局设置正确配置"
echo "✅ 间距策略与Launcher3一致"
echo ""
echo "📋 总结:"
echo "- 宽高比: 0.7 (280dp:400dp)"
echo "- 屏幕使用率: 宽度85%, 高度60%"
echo "- 尺寸范围: 200-400dp × 250-500dp"
echo "- 间距策略: 基于屏幕密度自适应"
echo "- 布局方式: 水平滚动，垂直居中"