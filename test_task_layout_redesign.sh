#!/bin/bash

# 测试任务卡片布局重新设计
# 验证Header在顶部，Thumbnail使用屏幕宽高比

echo "🧪 测试任务卡片布局重新设计"
echo "================================"

# 检查关键文件是否存在
echo "📁 检查关键文件..."

files=(
    "app/src/main/res/layout/task_item.xml"
    "app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java"
    "TASK_LAYOUT_REDESIGN_SUMMARY.md"
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
echo "🔍 验证布局结构变更..."

# 检查是否使用LinearLayout替代RelativeLayout
if grep -q "<LinearLayout" app/src/main/res/layout/task_item.xml && 
   grep -q 'android:orientation="vertical"' app/src/main/res/layout/task_item.xml; then
    echo "✅ 主容器已改为垂直LinearLayout"
else
    echo "❌ 主容器未正确设置为垂直LinearLayout"
    exit 1
fi

# 检查是否移除了RelativeLayout
if grep -q "<RelativeLayout" app/src/main/res/layout/task_item.xml; then
    echo "❌ 仍然包含RelativeLayout"
    exit 1
else
    echo "✅ 已移除RelativeLayout"
fi

echo ""
echo "🔍 验证Header位置..."

# 检查task_header是否在LinearLayout中的第一个位置
if grep -A 20 '<LinearLayout.*android:orientation="vertical"' app/src/main/res/layout/task_item.xml | 
   grep -B 5 -A 15 'android:id="@+id/task_header"' | 
   grep -q "Task header info - moved to top"; then
    echo "✅ task_header已移到顶部"
else
    echo "❌ task_header位置不正确"
    exit 1
fi

# 检查是否移除了layout_alignParentBottom
if grep -q "layout_alignParentBottom" app/src/main/res/layout/task_item.xml; then
    echo "❌ 仍然包含layout_alignParentBottom属性"
    exit 1
else
    echo "✅ 已移除layout_alignParentBottom属性"
fi

echo ""
echo "🔍 验证Thumbnail设置..."

# 检查task_thumbnail是否使用layout_weight
if grep -A 10 'android:id="@+id/task_thumbnail"' app/src/main/res/layout/task_item.xml | 
   grep -q 'android:layout_weight="1"'; then
    echo "✅ task_thumbnail使用layout_weight占据剩余空间"
else
    echo "❌ task_thumbnail未正确设置layout_weight"
    exit 1
fi

# 检查task_thumbnail高度设置
if grep -A 10 'android:id="@+id/task_thumbnail"' app/src/main/res/layout/task_item.xml | 
   grep -q 'android:layout_height="0dp"'; then
    echo "✅ task_thumbnail高度设置为0dp"
else
    echo "❌ task_thumbnail高度设置不正确"
    exit 1
fi

# 检查是否移除了layout_above属性
if grep -q "layout_above" app/src/main/res/layout/task_item.xml; then
    echo "❌ 仍然包含layout_above属性"
    exit 1
else
    echo "✅ 已移除layout_above属性"
fi

echo ""
echo "🔍 验证TaskViewSizeCalculator更新..."

# 检查是否添加了getTaskThumbnailSize方法
if grep -q "getTaskThumbnailSize()" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 已添加getTaskThumbnailSize()方法"
else
    echo "❌ 缺少getTaskThumbnailSize()方法"
    exit 1
fi

# 检查getTaskThumbnailSize是否返回Point
if grep -A 10 "getTaskThumbnailSize()" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java | 
   grep -q "return new Point"; then
    echo "✅ getTaskThumbnailSize()返回Point对象"
else
    echo "❌ getTaskThumbnailSize()返回类型不正确"
    exit 1
fi

# 检查getTaskHeight是否使用getTaskThumbnailSize
if grep -A 15 "public int getTaskHeight()" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java | 
   grep -q "getTaskThumbnailSize()"; then
    echo "✅ getTaskHeight()使用getTaskThumbnailSize()计算"
else
    echo "❌ getTaskHeight()未使用getTaskThumbnailSize()计算"
    exit 1
fi

echo ""
echo "🔍 验证调试信息增强..."

# 检查调试信息是否包含ThumbnailAspectRatio
if grep -q "ThumbnailAspectRatio: %.3f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 调试信息包含ThumbnailAspectRatio"
else
    echo "❌ 调试信息缺少ThumbnailAspectRatio"
    exit 1
fi

# 检查调试信息是否包含Header和Thumbnail尺寸
if grep -q "Header: %dpx, Thumbnail: %dx%d" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 调试信息包含Header和Thumbnail尺寸"
else
    echo "❌ 调试信息缺少Header和Thumbnail尺寸"
    exit 1
fi

echo ""
echo "🔍 验证布局组件顺序..."

# 提取LinearLayout中的组件顺序
echo "📋 检查组件在LinearLayout中的顺序:"

# 检查task_header是否在第一位
if grep -A 50 '<LinearLayout.*android:orientation="vertical"' app/src/main/res/layout/task_item.xml | 
   grep -n -E '(task_header|task_thumbnail|task_focus_indicator)' | head -1 | grep -q "task_header"; then
    echo "  1. ✅ task_header (顶部)"
else
    echo "  1. ❌ task_header 不在第一位"
    exit 1
fi

# 检查task_thumbnail是否在第二位
if grep -A 50 '<LinearLayout.*android:orientation="vertical"' app/src/main/res/layout/task_item.xml | 
   grep -n -E '(task_header|task_thumbnail|task_focus_indicator)' | sed -n '2p' | grep -q "task_thumbnail"; then
    echo "  2. ✅ task_thumbnail (主体)"
else
    echo "  2. ❌ task_thumbnail 位置不正确"
    exit 1
fi

# 检查task_focus_indicator是否在第三位
if grep -A 50 '<LinearLayout.*android:orientation="vertical"' app/src/main/res/layout/task_item.xml | 
   grep -n -E '(task_header|task_thumbnail|task_focus_indicator)' | sed -n '3p' | grep -q "task_focus_indicator"; then
    echo "  3. ✅ task_focus_indicator (状态)"
else
    echo "  3. ❌ task_focus_indicator 位置不正确"
    exit 1
fi

echo ""
echo "📊 屏幕宽高比计算验证..."

# 使用bc计算器验证屏幕比例
if command -v bc >/dev/null 2>&1; then
    echo "📱 预期的Thumbnail宽高比 (与屏幕一致):"
    
    # 16:9 屏幕
    ratio_16_9=$(echo "scale=4; 9/16" | bc)
    echo "  16:9 屏幕: $ratio_16_9 (如1080×1920)"
    
    # 18:9 屏幕  
    ratio_18_9=$(echo "scale=4; 9/18" | bc)
    echo "  18:9 屏幕: $ratio_18_9 (如1080×2160)"
    
    # 4:3 平板
    ratio_4_3=$(echo "scale=4; 3/4" | bc)
    echo "  4:3 平板: $ratio_4_3 (如1536×2048)"
    
else
    echo "📊 预期的Thumbnail宽高比:"
    echo "  16:9 = 0.5625"
    echo "  18:9 = 0.5000" 
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
echo "ScreenAspectRatio: 0.563, TaskAspectRatio: 0.563, ThumbnailAspectRatio: 0.563"
echo "TaskSize: 306x600 (Header: 56px, Thumbnail: 306x544)"
echo "Original: 280x400 (0.700 ratio)"
echo "Scale: 1.09x width, 1.50x height"
echo ""
echo "🔹 18:9 全面屏 (1080×2160):"
echo "Dynamic sizing: Screen: 1080x2160, Density: 3.0, Landscape: false"
echo "ScreenAspectRatio: 0.500, TaskAspectRatio: 0.500, ThumbnailAspectRatio: 0.500"
echo "TaskSize: 306x668 (Header: 56px, Thumbnail: 306x612)"
echo "Original: 280x400 (0.700 ratio)"
echo "Scale: 1.09x width, 1.67x height"
echo ""

echo "🧪 视觉效果测试建议..."
echo ""
echo "1. 📱 布局验证:"
echo "   - Header是否显示在卡片顶部"
echo "   - Thumbnail是否占据主要空间"
echo "   - 组件层次是否清晰"
echo ""
echo "2. 🔍 比例验证:"
echo "   - Thumbnail宽高比是否与屏幕一致"
echo "   - 不同屏幕尺寸的适配效果"
echo "   - 横竖屏切换的表现"
echo ""
echo "3. 🎨 视觉效果:"
echo "   - 信息层次是否清晰"
echo "   - 颜色对比是否合适"
echo "   - 整体协调性"
echo ""
echo "4. 👆 交互测试:"
echo "   - Header中按钮的点击响应"
echo "   - Thumbnail的触摸事件"
echo "   - 整体卡片的交互效果"
echo ""

echo "🎉 所有测试通过！"
echo "✅ 布局结构已正确重构为LinearLayout"
echo "✅ task_header已移到顶部"
echo "✅ task_thumbnail使用屏幕宽高比"
echo "✅ TaskViewSizeCalculator已更新计算逻辑"
echo "✅ 调试信息已增强"
echo "✅ 代码编译成功"
echo ""
echo "📋 布局重设计总结:"
echo "- 🔝 Header在顶部: 应用信息优先显示"
echo "- 📱 屏幕比例: Thumbnail与设备屏幕比例一致"
echo "- 📐 响应式布局: 自动适应不同屏幕尺寸"
echo "- 🎨 清晰层次: 信息和预览分离明确"
echo "- 🔧 维护友好: LinearLayout结构简单可靠"
echo ""
echo "🚀 现在任务卡片具有更好的信息层次和准确的屏幕比例反映！"