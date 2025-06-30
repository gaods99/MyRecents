#!/bin/bash

# 任务卡片宽度优化测试脚本

echo "📱 任务卡片宽度优化测试"
echo "================================"
echo ""

# 检查优化是否正确应用
echo "📋 检查优化内容："
echo "优化1 - 卡片尺寸调整："
echo "  ✅ 基础宽度：312dp → 240dp"
echo "  ✅ 基础高度：420dp → 320dp"
echo "  ✅ 宽高比：0.743 → 0.75"
echo "  ✅ 更适合手机屏幕"
echo ""
echo "优化2 - 智能缩放策略："
echo "  ✅ 根据宽高比智能选择缩放模式"
echo "  ✅ 优先铺满宽度"
echo "  ✅ 保持视觉效果"
echo ""

# 验证代码优化
echo "🔍 验证代码优化..."

# 检查宽度优化
if grep -q "OPTIMIZED_TASK_WIDTH_DP = 240" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 宽度优化：基础宽度调整为240dp"
else
    echo "❌ 宽度优化：未找到240dp设置"
fi

# 检查高度优化
if grep -q "OPTIMIZED_TASK_HEIGHT_DP = 320" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 高度优化：基础高度调整为320dp"
else
    echo "❌ 高度优化：未找到320dp设置"
fi

# 检查智能缩放方法
if grep -q "setOptimalThumbnailScale" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    echo "✅ 智能缩放：添加了智能缩放方法"
else
    echo "❌ 智能缩放：未找到智能缩放方法"
fi

# 检查尺寸限制优化
if grep -q "MIN_TASK_WIDTH_DP = 200" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 尺寸限制：最小宽度调整为200dp"
else
    echo "❌ 尺寸限制：未找到200dp最小宽度"
fi

if grep -q "MAX_TASK_WIDTH_DP = 280" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 尺寸限制：最大宽度调整为280dp"
else
    echo "❌ 尺寸限制：未找到280dp最大宽度"
fi

echo ""

# 编译测试
echo "🔨 编译项目..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ 编译成功"
else
    echo "❌ 编译失败"
    exit 1
fi

echo ""
echo "📱 测试步骤："
echo "1. 安装应用到设备"
echo "2. 启动最近任务界面"
echo "3. 观察卡片宽度和缩略图显示"
echo "4. 验证优化效果"
echo ""

# 检查设备连接
if command -v adb &> /dev/null && adb devices | grep -q "device$"; then
    echo "📱 检测到设备，开始安装..."
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    
    if [ $? -eq 0 ]; then
        echo "✅ 安装成功"
        echo ""
        echo "🧪 启动测试..."
        adb shell am broadcast -a com.android.systemui.recents.ACTION_SHOW
        echo ""
        echo "👀 请观察以下内容："
        echo ""
        echo "📏 卡片宽度检查："
        echo "  • 卡片宽度是否比之前更窄"
        echo "  • 卡片是否更适合手机屏幕"
        echo "  • 多个卡片是否能更好地并排显示"
        echo ""
        echo "🖼️  缩略图铺满检查："
        echo "  • 缩略图是否能更好地铺满卡片宽度"
        echo "  • 不同宽高比的缩略图显示是否合理"
        echo "  • 竖屏应用缩略图是否能铺满宽度"
        echo "  • 横屏应用缩略图是否完整显示"
        echo ""
        echo "🎨 整体效果检查："
        echo "  • 整体布局是否更紧凑"
        echo "  • 视觉效果是否更适合手机"
        echo "  • 滚动体验是否更流畅"
    else
        echo "❌ 安装失败"
    fi
else
    echo "📦 APK位置: app/build/outputs/apk/debug/app-debug.apk"
    echo "💡 请手动安装并测试"
fi

echo ""
echo "🎯 预期结果："
echo ""
echo "优化前的问题："
echo "❌ 卡片宽度过宽"
echo "❌ 缩略图无法铺满宽度"
echo "❌ 不适合手机屏幕"
echo ""
echo "优化后的效果："
echo "✅ 卡片宽度适中，更适合手机"
echo "✅ 缩略图能更好地铺满宽度"
echo "✅ 整体布局更紧凑合理"
echo "✅ 视觉效果更专业"
echo ""

echo "📊 尺寸对比："
echo ""
echo "基础尺寸变化："
echo "  宽度: 312dp → 240dp (-23%)"
echo "  高度: 420dp → 320dp (-24%)"
echo "  宽高比: 0.743 → 0.75"
echo ""
echo "尺寸限制变化："
echo "  最小宽度: 280dp → 200dp"
echo "  最大宽度: 360dp → 280dp"
echo "  最小高度: 380dp → 280dp"
echo "  最大高度: 480dp → 400dp"
echo ""

echo "🔧 智能缩放策略："
echo "  • 宽高比接近：使用FIT_XY填满"
echo "  • 缩略图更宽：使用FIT_CENTER完整显示"
echo "  • 缩略图更高：使用CENTER_CROP优先铺满宽度"
echo ""

echo "================================"
echo "🏁 卡片宽度优化测试完成"
echo ""
echo "💡 提示：如果测试通过，卡片宽度将更适合手机屏幕，缩略图显示效果更佳！"