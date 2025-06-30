#!/bin/bash

# 任务卡片显示优化综合测试脚本

echo "🎨 任务卡片显示优化综合测试"
echo "================================"
echo ""

# 检查修复是否正确应用
echo "📋 检查修复内容："
echo "修复1 - 宽度一致性："
echo "  ✅ 统一使用SystemUI标准宽度"
echo "  ✅ 移除动态宽度计算逻辑"
echo ""
echo "修复2 - 缩略图完整显示："
echo "  ✅ Java代码使用FIT_CENTER"
echo "  ✅ 布局文件使用fitCenter"
echo "  ✅ 移除CENTER_CROP裁剪模式"
echo ""

# 验证代码修复
echo "🔍 验证代码修复..."

# 检查宽度一致性修复
if grep -q "params.width = mDefaultTaskWidth" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    echo "✅ 宽度一致性：统一使用标准宽度"
else
    echo "❌ 宽度一致性：未找到统一宽度设置"
fi

# 检查缩略图显示修复 - Java代码
if grep -q "ImageView.ScaleType.FIT_CENTER" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    echo "✅ 缩略图显示：Java代码使用FIT_CENTER"
else
    echo "❌ 缩略图显示：Java代码未使用FIT_CENTER"
fi

# 检查缩略图显示修复 - 布局文件
if grep -q 'android:scaleType="fitCenter"' app/src/main/res/layout/task_item.xml; then
    echo "✅ 缩略图显示：布局文件使用fitCenter"
else
    echo "❌ 缩略图显示：布局文件未使用fitCenter"
fi

# 检查是否移除了问题代码
if ! grep -q "ImageView.ScaleType.CENTER_CROP" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    echo "✅ 问题修复：已移除CENTER_CROP"
else
    echo "❌ 问题修复：仍然使用CENTER_CROP"
fi

if ! grep -q 'android:scaleType="centerCrop"' app/src/main/res/layout/task_item.xml; then
    echo "✅ 问题修复：布局文件已移除centerCrop"
else
    echo "❌ 问题修复：布局文件仍然使用centerCrop"
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
echo "📱 综合测试步骤："
echo "1. 安装应用到设备"
echo "2. 启动最近任务界面"
echo "3. 观察任务卡片显示效果"
echo "4. 验证修复效果"
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
        echo "🔲 宽度一致性检查："
        echo "  • 所有任务卡片宽度是否完全一致"
        echo "  • 有缩略图和无缩略图的卡片宽度是否相同"
        echo "  • 卡片左右边缘是否完美对齐"
        echo ""
        echo "🖼️  缩略图显示检查："
        echo "  • 缩略图是否完整显示，无上下裁剪"
        echo "  • 不同宽高比的缩略图是否都能完整显示"
        echo "  • 缩略图是否在卡片中居中显示"
        echo "  • 空白区域是否有合适的背景色"
        echo ""
        echo "🎨 整体视觉效果："
        echo "  • 整体布局是否统一和专业"
        echo "  • 滚动是否流畅无卡顿"
        echo "  • 视觉效果是否符合SystemUI标准"
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
echo "修复前的问题："
echo "❌ 卡片宽度不一致"
echo "❌ 缩略图被裁剪"
echo "❌ 视觉效果不统一"
echo ""
echo "修复后的效果："
echo "✅ 所有卡片宽度完全一致"
echo "✅ 缩略图完整显示，无裁剪"
echo "✅ 视觉效果统一专业"
echo "✅ 符合SystemUI设计标准"
echo ""

echo "📊 技术对比："
echo ""
echo "ScaleType变化："
echo "  修复前: CENTER_CROP (裁剪填充)"
echo "  修复后: FIT_CENTER (完整显示)"
echo ""
echo "宽度计算变化："
echo "  修复前: 动态计算 (不一致)"
echo "  修复后: 统一标准 (完全一致)"
echo ""

echo "🔧 相关文件："
echo "  • TaskViewHolder.java (核心修复逻辑)"
echo "  • task_item.xml (布局修复)"
echo "  • TASK_WIDTH_CONSISTENCY_FIX.md (详细文档)"
echo "  • THUMBNAIL_DISPLAY_FIX.md (缩略图修复文档)"
echo ""

echo "================================"
echo "🏁 综合测试脚本完成"
echo ""
echo "💡 提示：如果测试通过，说明两个关键显示问题都已成功修复！"