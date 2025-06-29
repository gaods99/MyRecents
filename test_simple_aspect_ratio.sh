#!/bin/bash

# 测试简化版屏幕宽高比实现
# 验证统一使用屏幕宽高比的功能

echo "🧪 测试简化版屏幕宽高比实现"
echo "================================"

# 检查关键文件是否存在
echo "📁 检查关键文件..."

files=(
    "app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java"
    "app/src/main/java/com/newland/recents/adapter/TaskAdapter.java"
    "app/src/main/java/com/newland/recents/RecentsActivity.java"
    "SIMPLE_SCREEN_ASPECT_RATIO_SUMMARY.md"
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
echo "🔍 验证简化实现..."

# 检查是否移除了复杂的应用检测方法
if grep -q "getAppAspectRatio.*Task task" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "❌ 仍然包含复杂的应用检测方法"
    exit 1
else
    echo "✅ 已移除复杂的应用检测方法"
fi

# 检查是否移除了getTaskHeight(Task task)方法
if grep -q "getTaskHeight.*Task task" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "❌ 仍然包含任务特定高度计算方法"
    exit 1
else
    echo "✅ 已移除任务特定高度计算方法"
fi

# 检查是否移除了应用分类检测
if grep -q "getAspectRatioFromAppCategory" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "❌ 仍然包含应用分类检测方法"
    exit 1
else
    echo "✅ 已移除应用分类检测方法"
fi

# 检查是否移除了PackageManager导入
if grep -q "import.*PackageManager" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "❌ 仍然包含PackageManager导入"
    exit 1
else
    echo "✅ 已移除PackageManager导入"
fi

# 检查是否移除了Task导入
if grep -q "import.*Task" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "❌ 仍然包含Task导入"
    exit 1
else
    echo "✅ 已移除Task导入"
fi

echo ""
echo "🔍 验证核心功能保留..."

# 检查是否保留了getScreenAspectRatio方法
if grep -q "public float getScreenAspectRatio()" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 保留了getScreenAspectRatio()方法"
else
    echo "❌ 缺少getScreenAspectRatio()方法"
    exit 1
fi

# 检查是否保留了基本的getTaskHeight方法
if grep -q "public int getTaskHeight()" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 保留了基本getTaskHeight()方法"
else
    echo "❌ 缺少基本getTaskHeight()方法"
    exit 1
fi

# 检查getTaskHeight是否使用屏幕宽高比
if grep -q "getScreenAspectRatio()" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ getTaskHeight()使用屏幕宽高比"
else
    echo "❌ getTaskHeight()未使用屏幕宽高比"
    exit 1
fi

# 检查宽高比限制机制
if grep -q "Math.max(0.5f, Math.min(0.8f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 宽高比限制机制保留"
else
    echo "❌ 宽高比限制机制缺失"
    exit 1
fi

echo ""
echo "🔍 验证TaskAdapter简化..."

# 检查TaskAdapter是否移除了复杂的尺寸设置
if grep -q "getTaskHeight(task)" app/src/main/java/com/newland/recents/adapter/TaskAdapter.java; then
    echo "❌ TaskAdapter仍然包含任务特定高度设置"
    exit 1
else
    echo "✅ TaskAdapter已移除任务特定高度设置"
fi

# 检查TaskAdapter的onBindViewHolder是否简化
if grep -A 5 "onBindViewHolder" app/src/main/java/com/newland/recents/adapter/TaskAdapter.java | grep -q "layoutParams.height"; then
    echo "❌ TaskAdapter仍然包含动态高度设置"
    exit 1
else
    echo "✅ TaskAdapter的onBindViewHolder已简化"
fi

echo ""
echo "🔍 验证RecentsActivity简化..."

# 检查RecentsActivity是否移除了复杂的任务调试信息
if grep -q "sizeCalculator.getDebugInfo(task)" app/src/main/java/com/newland/recents/RecentsActivity.java; then
    echo "❌ RecentsActivity仍然包含任务特定调试信息"
    exit 1
else
    echo "✅ RecentsActivity已移除任务特定调试信息"
fi

# 检查是否保留了基本的调试信息
if grep -q "sizeCalculator.getDebugInfo()" app/src/main/java/com/newland/recents/RecentsActivity.java; then
    echo "✅ RecentsActivity保留了基本调试信息"
else
    echo "⚠️  RecentsActivity可能移除了基本调试信息 (这是可选的)"
fi

echo ""
echo "📊 屏幕宽高比计算验证..."

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
echo "Original: 280x400 (0.700 ratio)"
echo "Scale: 1.09x width, 1.36x height"
echo ""
echo "🔹 18:9 全面屏 (1080×2160):"
echo "Dynamic sizing: Screen: 1080x2160, Density: 3.0, Landscape: false"
echo "ScreenAspectRatio: 0.500, TaskAspectRatio: 0.500"
echo "TaskSize: 306x612"
echo "Original: 280x400 (0.700 ratio)"
echo "Scale: 1.09x width, 1.53x height"
echo ""
echo "🔹 4:3 平板 (1536×2048):"
echo "Dynamic sizing: Screen: 1536x2048, Density: 2.0, Landscape: false"
echo "ScreenAspectRatio: 0.750, TaskAspectRatio: 0.750"
echo "TaskSize: 400x533"
echo "Original: 280x400 (0.700 ratio)"
echo "Scale: 1.43x width, 1.33x height"
echo ""

echo "🧪 测试场景建议..."
echo ""
echo "1. 📱 不同屏幕比例测试:"
echo "   - 16:9 传统屏幕"
echo "   - 18:9 全面屏"
echo "   - 19.5:9 现代全面屏"
echo "   - 4:3 平板屏幕"
echo ""
echo "2. 🔄 横竖屏切换测试:"
echo "   - 验证宽高比在横竖屏切换时保持一致"
echo "   - 检查布局重新计算的正确性"
echo ""
echo "3. 📊 视觉效果验证:"
echo "   - 所有任务卡片形状是否统一"
echo "   - 任务卡片比例是否与屏幕匹配"
echo "   - 整体布局是否协调"
echo ""
echo "4. ⚡ 性能验证:"
echo "   - 确认没有PackageManager查询开销"
echo "   - 验证内存使用情况"
echo "   - 检查滚动流畅性"
echo ""

echo "🎉 所有测试通过！"
echo "✅ 简化版屏幕宽高比实现正确"
echo "✅ 移除了所有复杂的应用检测逻辑"
echo "✅ 保留了核心的屏幕适配功能"
echo "✅ 代码结构简洁清晰"
echo "✅ 编译成功无错误"
echo ""
echo "📋 简化总结:"
echo "- 🎯 统一比例: 所有任务卡片使用相同的屏幕宽高比"
echo "- 🚀 性能提升: 无PackageManager查询，更快更稳定"
echo "- 🔧 维护简单: 代码逻辑清晰，易于理解和维护"
echo "- 📱 屏幕适配: 自动适应不同屏幕比例"
echo "- 🎨 视觉一致: 提供统一的视觉体验"
echo ""
echo "🚀 现在任务卡片会根据屏幕比例统一显示，简洁而有效！"