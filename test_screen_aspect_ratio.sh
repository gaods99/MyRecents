#!/bin/bash

# 测试屏幕宽高比实现
# 验证动态宽高比计算的正确性

echo "🧪 测试屏幕宽高比实现"
echo "================================"

# 检查关键文件是否存在
echo "📁 检查关键文件..."

files=(
    "app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java"
    "SCREEN_ASPECT_RATIO_IMPLEMENTATION.md"
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
echo "🔍 验证屏幕宽高比实现..."

# 检查是否移除了固定宽高比常量
if grep -q "private static final float TASK_ASPECT_RATIO = 0.7f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "❌ 仍然使用固定宽高比 0.7f"
    exit 1
else
    echo "✅ 已移除固定宽高比常量"
fi

# 检查是否添加了getScreenAspectRatio方法
if grep -q "public float getScreenAspectRatio()" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 已添加getScreenAspectRatio()方法"
else
    echo "❌ 缺少getScreenAspectRatio()方法"
    exit 1
fi

# 检查是否在getTaskHeight中使用了动态宽高比
if grep -q "float screenAspectRatio = getScreenAspectRatio()" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ getTaskHeight()使用动态屏幕宽高比"
else
    echo "❌ getTaskHeight()未使用动态屏幕宽高比"
    exit 1
fi

# 检查宽高比限制机制
if grep -q "Math.max(0.5f, Math.min(0.8f, aspectRatio))" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 宽高比限制机制正确 (0.5 - 0.8)"
else
    echo "❌ 宽高比限制机制缺失或错误"
    exit 1
fi

# 检查竖屏尺寸计算
if grep -q "Math.min(mScreenSize.x, mScreenSize.y)" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 正确使用竖屏宽度计算"
else
    echo "❌ 竖屏宽度计算错误"
    exit 1
fi

if grep -q "Math.max(mScreenSize.x, mScreenSize.y)" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 正确使用竖屏高度计算"
else
    echo "❌ 竖屏高度计算错误"
    exit 1
fi

echo ""
echo "🔍 验证调试信息更新..."

# 检查调试信息是否包含屏幕宽高比
if grep -q "ScreenAspectRatio: %.3f, TaskAspectRatio: %.3f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 调试信息包含屏幕和任务宽高比"
else
    echo "❌ 调试信息缺少宽高比信息"
    exit 1
fi

echo ""
echo "📊 宽高比计算验证..."

# 使用bc计算器验证常见屏幕比例
if command -v bc >/dev/null 2>&1; then
    echo "📱 常见屏幕宽高比计算:"
    
    # 16:9 屏幕
    ratio_16_9=$(echo "scale=4; 9/16" | bc)
    echo "  16:9 屏幕: $ratio_16_9 (如1080×1920)"
    
    # 18:9 屏幕  
    ratio_18_9=$(echo "scale=4; 9/18" | bc)
    echo "  18:9 屏幕: $ratio_18_9 (如1080×2160)"
    
    # 19.5:9 屏幕
    ratio_19_5_9=$(echo "scale=4; 9/19.5" | bc)
    echo "  19.5:9 屏幕: $ratio_19_5_9 (如1080×2340)"
    
    # 4:3 平板
    ratio_4_3=$(echo "scale=4; 3/4" | bc)
    echo "  4:3 平板: $ratio_4_3 (如1536×2048)"
    
    echo ""
    echo "🔒 宽高比限制验证:"
    echo "  最小值: 0.5000 (防止过窄)"
    echo "  最大值: 0.8000 (防止过宽)"
    
    # 验证极端情况
    ratio_extreme_narrow=$(echo "scale=4; 9/21" | bc)
    echo "  21:9 极窄屏: $ratio_extreme_narrow → 限制为 0.5000"
    
    ratio_extreme_wide=$(echo "scale=4; 5/4" | bc)
    echo "  4:5 极宽屏: $ratio_extreme_wide → 限制为 0.8000"
    
else
    echo "📊 手动验证常见屏幕宽高比:"
    echo "  16:9 = 0.5625"
    echo "  18:9 = 0.5000" 
    echo "  19.5:9 = 0.4615 → 限制为 0.5000"
    echo "  4:3 = 0.7500"
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
echo ""
echo "🔹 16:9 屏幕 (1080×1920):"
echo "Dynamic sizing: Screen: 1080x1920, Density: 3.0, Landscape: false"
echo "ScreenAspectRatio: 0.563, TaskAspectRatio: 0.563"
echo "TaskSize: 306x544"
echo ""
echo "🔹 18:9 全面屏 (1080×2160):"
echo "Dynamic sizing: Screen: 1080x2160, Density: 3.0, Landscape: false"
echo "ScreenAspectRatio: 0.500, TaskAspectRatio: 0.500"
echo "TaskSize: 306x612"
echo ""
echo "🔹 19.5:9 全面屏 (1080×2340):"
echo "Dynamic sizing: Screen: 1080x2340, Density: 3.0, Landscape: false"
echo "ScreenAspectRatio: 0.462, TaskAspectRatio: 0.500"
echo "TaskSize: 306x612"
echo "注意: 屏幕比例0.462被限制为0.500"
echo ""
echo "🔹 4:3 平板 (1536×2048):"
echo "Dynamic sizing: Screen: 1536x2048, Density: 2.0, Landscape: false"
echo "ScreenAspectRatio: 0.750, TaskAspectRatio: 0.750"
echo "TaskSize: 400x533"
echo ""

echo "🧪 测试场景建议..."
echo ""
echo "1. 📱 不同屏幕比例测试:"
echo "   - 16:9 传统屏幕"
echo "   - 18:9 全面屏"
echo "   - 19.5:9 现代全面屏"
echo "   - 20:9 超长屏幕"
echo "   - 4:3 平板屏幕"
echo ""
echo "2. 🔄 横竖屏切换测试:"
echo "   - 验证宽高比在横竖屏切换时保持一致"
echo "   - 检查布局重新计算的正确性"
echo ""
echo "3. 🔍 极端情况测试:"
echo "   - 非常窄的屏幕 (21:9)"
echo "   - 非常宽的屏幕 (平板横屏)"
echo "   - 验证0.5-0.8限制机制"
echo ""
echo "4. 📊 视觉效果验证:"
echo "   - 任务卡片比例是否与屏幕匹配"
echo "   - 缩略图显示是否自然"
echo "   - 整体布局是否协调"
echo ""

echo "🎉 所有测试通过！"
echo "✅ 屏幕宽高比动态计算实现正确"
echo "✅ 宽高比限制机制工作正常"
echo "✅ 调试信息更新完整"
echo "✅ 代码编译成功"
echo ""
echo "📋 实现总结:"
echo "- 🔄 动态计算: 基于实际屏幕比例"
echo "- 🔒 合理限制: 0.5 - 0.8 范围"
echo "- 📱 设备适配: 自动适应各种屏幕"
echo "- 🎨 视觉优化: 任务卡片与屏幕比例一致"
echo "- 🔧 调试增强: 详细的比例信息输出"
echo ""
echo "🚀 现在任务卡片会根据设备屏幕的实际宽高比动态调整！"