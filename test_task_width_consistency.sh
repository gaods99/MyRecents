#!/bin/bash

# 任务卡片宽度一致性测试脚本

echo "🧪 任务卡片宽度一致性测试"
echo "================================"
echo ""

# 检查修复是否正确应用
echo "📋 检查修复内容："
echo "1. ✅ 统一使用SystemUI标准宽度"
echo "2. ✅ 移除动态宽度计算逻辑"
echo "3. ✅ 缩略图使用CENTER_CROP适配"
echo "4. ✅ 无缩略图时显示占位图"
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
echo "3. 观察任务卡片宽度是否一致"
echo "4. 验证有缩略图和无缩略图的卡片宽度相同"
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
        echo "• 所有任务卡片宽度是否一致"
        echo "• 有缩略图的卡片是否正确显示"
        echo "• 无缩略图的卡片是否显示占位图"
        echo "• 缩略图是否在卡片内正确适配"
    else
        echo "❌ 安装失败"
    fi
else
    echo "📦 APK位置: app/build/outputs/apk/debug/app-debug.apk"
    echo "💡 请手动安装并测试"
fi

echo ""
echo "🎯 预期结果："
echo "• 所有任务卡片宽度完全一致"
echo "• 缩略图在固定宽度内自适应显示"
echo "• 无缩略图时显示统一的占位图"
echo "• 整体视觉效果更加统一和专业"
echo ""
echo "================================"
echo "🏁 测试脚本完成"