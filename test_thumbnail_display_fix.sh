#!/bin/bash

# 缩略图完整显示修复测试脚本

echo "🖼️  缩略图完整显示修复测试"
echo "================================"
echo ""

# 检查修复是否正确应用
echo "📋 检查修复内容："
echo "1. ✅ Java代码使用FIT_CENTER"
echo "2. ✅ 布局文件使用fitCenter"
echo "3. ✅ 移除CENTER_CROP裁剪模式"
echo "4. ✅ 保持卡片宽度统一"
echo ""

# 验证代码修复
echo "🔍 验证代码修复..."

# 检查Java代码中的ScaleType
if grep -q "ImageView.ScaleType.FIT_CENTER" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    echo "✅ Java代码：使用FIT_CENTER"
else
    echo "❌ Java代码：未找到FIT_CENTER"
fi

# 检查是否移除了CENTER_CROP
if ! grep -q "ImageView.ScaleType.CENTER_CROP" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    echo "✅ Java代码：已移除CENTER_CROP"
else
    echo "❌ Java代码：仍然使用CENTER_CROP"
fi

# 检查布局文件中的scaleType
if grep -q 'android:scaleType="fitCenter"' app/src/main/res/layout/task_item.xml; then
    echo "✅ 布局文件：使用fitCenter"
else
    echo "❌ 布局文件：未使用fitCenter"
fi

# 检查是否移除了centerCrop
if ! grep -q 'android:scaleType="centerCrop"' app/src/main/res/layout/task_item.xml; then
    echo "✅ 布局文件：已移除centerCrop"
else
    echo "❌ 布局文件：仍然使用centerCrop"
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
echo "3. 观察缩略图是否完整显示"
echo "4. 验证不同宽高比的缩略图都能完整显示"
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
        echo "• 缩略图是否完整显示，无上下裁剪"
        echo "• 不同宽高比的缩略图是否都能完整显示"
        echo "• 缩略图是否在卡片中居中显示"
        echo "• 卡片宽度是否保持一致"
        echo "• 无缩略图时是否正确显示占位图"
    else
        echo "❌ 安装失败"
    fi
else
    echo "📦 APK位置: app/build/outputs/apk/debug/app-debug.apk"
    echo "💡 请手动安装并测试"
fi

echo ""
echo "🎯 预期结果："
echo "• 所有缩略图完整显示，无裁剪"
echo "• 不同宽高比的缩略图都能完整显示"
echo "• 缩略图在卡片中居中显示"
echo "• 卡片宽度保持统一"
echo "• 整体视觉效果更加完整和专业"
echo ""

echo "📊 ScaleType对比："
echo "修复前: CENTER_CROP (裁剪缩略图以填满区域)"
echo "修复后: FIT_CENTER (缩放缩略图以完整显示)"
echo ""

echo "🔧 技术说明："
echo "• FIT_CENTER: 等比缩放图片，使其完全显示在ImageView中"
echo "• CENTER_CROP: 等比缩放图片，裁剪多余部分以填满ImageView"
echo "• 修复确保用户能看到完整的应用缩略图内容"
echo ""

echo "================================"
echo "🏁 测试脚本完成"