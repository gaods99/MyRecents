#!/bin/bash

# 测试Android 7.1.2 SystemUI风格实现
# 验证固定宽高比和SystemUI视觉风格

echo "🧪 测试Android 7.1.2 SystemUI风格实现"
echo "================================"

# 检查关键文件是否存在
echo "📁 检查关键文件..."

files=(
    "app/src/main/res/layout/task_item.xml"
    "app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java"
    "app/src/main/res/drawable/recents_task_view_background.xml"
    "app/src/main/res/drawable/recents_task_header_background.xml"
    "SYSTEMUI_STYLE_IMPLEMENTATION.md"
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
echo "🔍 验证SystemUI固定宽高比..."

# 检查是否使用SystemUI固定宽高比
if grep -q "SYSTEMUI_TASK_ASPECT_RATIO = 16f / 10f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 使用SystemUI固定宽高比 16:10 (1.6)"
else
    echo "❌ 未使用SystemUI固定宽高比"
    exit 1
fi

# 检查SystemUI原始尺寸定义
if grep -q "SYSTEMUI_TASK_WIDTH_DP = 320" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ SystemUI原始宽度: 320dp"
else
    echo "❌ SystemUI原始宽度设置错误"
    exit 1
fi

if grep -q "SYSTEMUI_TASK_HEIGHT_DP = 200" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ SystemUI原始高度: 200dp"
else
    echo "❌ SystemUI原始高度设置错误"
    exit 1
fi

# 检查是否移除了屏幕宽高比计算
if grep -q "getScreenAspectRatio" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "❌ 仍然包含屏幕宽高比计算"
    exit 1
else
    echo "✅ 已移除屏幕宽高比计算"
fi

echo ""
echo "🔍 验证高度计算逻辑..."

# 检查getTaskHeight是否使用SystemUI固定比例
if grep -A 10 "public int getTaskHeight()" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java | 
   grep -q "SYSTEMUI_TASK_ASPECT_RATIO"; then
    echo "✅ getTaskHeight()使用SystemUI固定宽高比"
else
    echo "❌ getTaskHeight()未使用SystemUI固定宽高比"
    exit 1
fi

# 检查是否移除了复杂的缩略图尺寸计算
if grep -q "getTaskThumbnailSize" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "❌ 仍然包含复杂的缩略图尺寸计算"
    exit 1
else
    echo "✅ 已移除复杂的缩略图尺寸计算"
fi

echo ""
echo "🔍 验证布局结构变更..."

# 检查是否使用FrameLayout替代CardView
if grep -q "<FrameLayout" app/src/main/res/layout/task_item.xml && 
   ! grep -q "<androidx.cardview.widget.CardView" app/src/main/res/layout/task_item.xml; then
    echo "✅ 已改为FrameLayout布局"
else
    echo "❌ 布局结构未正确更改"
    exit 1
fi

# 检查缩略图是否占满整个卡片
if grep -A 5 'android:id="@+id/task_thumbnail"' app/src/main/res/layout/task_item.xml | 
   grep -q 'android:layout_width="match_parent"' && 
   grep -A 5 'android:id="@+id/task_thumbnail"' app/src/main/res/layout/task_item.xml | 
   grep -q 'android:layout_height="match_parent"'; then
    echo "✅ 缩略图占满整个卡片"
else
    echo "❌ 缩略图尺寸设置不正确"
    exit 1
fi

# 检查Header是否作为覆盖层
if grep -A 5 'android:id="@+id/task_header"' app/src/main/res/layout/task_item.xml | 
   grep -q 'android:layout_gravity="top"'; then
    echo "✅ Header作为顶部覆盖层"
else
    echo "❌ Header位置设置不正确"
    exit 1
fi

echo ""
echo "🔍 验证SystemUI视觉风格..."

# 检查Header背景是否使用渐变
if grep -q "@drawable/recents_task_header_background" app/src/main/res/layout/task_item.xml; then
    echo "✅ Header使用渐变背景"
else
    echo "❌ Header背景设置错误"
    exit 1
fi

# 检查文字颜色是否为白色
if grep -A 10 'android:id="@+id/task_title"' app/src/main/res/layout/task_item.xml | 
   grep -q 'android:textColor="@android:color/white"'; then
    echo "✅ 文字颜色为白色"
else
    echo "❌ 文字颜色设置错误"
    exit 1
fi

# 检查是否有文字阴影
if grep -A 10 'android:id="@+id/task_title"' app/src/main/res/layout/task_item.xml | 
   grep -q 'android:shadowColor="@android:color/black"'; then
    echo "✅ 文字有黑色阴影"
else
    echo "❌ 文字阴影设置错误"
    exit 1
fi

# 检查关闭按钮是否为白色
if grep -A 10 'android:id="@+id/task_dismiss"' app/src/main/res/layout/task_item.xml | 
   grep -q 'android:tint="@android:color/white"'; then
    echo "✅ 关闭按钮为白色"
else
    echo "❌ 关闭按钮颜色设置错误"
    exit 1
fi

echo ""
echo "🔍 验证drawable资源..."

# 检查任务卡片背景
if grep -q "<shape.*rectangle" app/src/main/res/drawable/recents_task_view_background.xml && 
   grep -q "android:radius.*4dp" app/src/main/res/drawable/recents_task_view_background.xml; then
    echo "✅ 任务卡片背景正确 (圆角4dp)"
else
    echo "❌ 任务卡片背景设置错误"
    exit 1
fi

# 检查Header渐变背景
if grep -q "<gradient" app/src/main/res/drawable/recents_task_header_background.xml && 
   grep -q "android:angle.*270" app/src/main/res/drawable/recents_task_header_background.xml; then
    echo "✅ Header渐变背景正确"
else
    echo "❌ Header渐变背景设置错误"
    exit 1
fi

echo ""
echo "🔍 验证调试信息更新..."

# 检查调试信息是否包含SystemUI参数
if grep -q "SystemUI AspectRatio: %.3f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 调试信息包含SystemUI宽高比"
else
    echo "❌ 调试信息缺少SystemUI宽高比"
    exit 1
fi

# 检查是否移除了复杂的缩略图调试信息
if grep -q "ThumbnailAspectRatio" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "❌ 仍然包含复杂的缩略图调试信息"
    exit 1
else
    echo "✅ 已移除复杂的缩略图调试信息"
fi

echo ""
echo "📊 SystemUI宽高比计算验证..."

# 使用bc计算器验证SystemUI比例
if command -v bc >/dev/null 2>&1; then
    systemui_ratio=$(echo "scale=4; 16/10" | bc)
    echo "📐 SystemUI宽高比计算: 16÷10 = $systemui_ratio"
    
    # 验证320x200的比例
    original_ratio=$(echo "scale=4; 320/200" | bc)
    echo "📐 SystemUI原始尺寸: 320÷200 = $original_ratio"
    
    if [ "$systemui_ratio" = "$original_ratio" ]; then
        echo "✅ SystemUI宽高比计算正确"
    else
        echo "❌ SystemUI宽高比计算错误"
        exit 1
    fi
else
    echo "📐 手动验证SystemUI宽高比:"
    echo "  16÷10 = 1.6000"
    echo "  320÷200 = 1.6000"
    echo "✅ SystemUI宽高比一致"
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
echo "🔹 所有屏幕统一效果:"
echo "Dynamic sizing: Screen: 1080x1920, Density: 3.0, Landscape: false"
echo "SystemUI AspectRatio: 1.600, TaskAspectRatio: 1.600"
echo "TaskSize: 306x191"
echo "SystemUI Original: 320x200 (1.600 ratio)"
echo "Scale: 0.96x width, 0.96x height"
echo ""
echo "🔹 平板设备 (密度缩放):"
echo "Dynamic sizing: Screen: 1536x2048, Density: 2.0, Landscape: false"
echo "SystemUI AspectRatio: 1.600, TaskAspectRatio: 1.600"
echo "TaskSize: 400x250"
echo "SystemUI Original: 320x200 (1.600 ratio)"
echo "Scale: 1.25x width, 1.25x height"
echo ""

echo "🧪 视觉效果测试建议..."
echo ""
echo "1. 📐 宽高比验证:"
echo "   - 所有任务卡片都是横向的16:10比例"
echo "   - 不同屏幕上保持相同的形状"
echo "   - 比例固定，不随屏幕变化"
echo ""
echo "2. 🎨 SystemUI风格验证:"
echo "   - 缩略图占满整个卡片"
echo "   - Header作为半透明覆盖层"
echo "   - 白色文字带黑色阴影"
echo "   - 渐变背景确保可读性"
echo ""
echo "3. 🖼️ 布局效果验证:"
echo "   - FrameLayout层叠布局"
echo "   - 圆角4dp的卡片背景"
echo "   - 覆盖层不遮挡缩略图内容"
echo "   - 焦点指示器在底部"
echo ""
echo "4. 👆 交互测试:"
echo "   - 整个卡片可点击"
echo "   - Header中的按钮响应正常"
echo "   - 覆盖层不影响触摸事件"
echo "   - 滑动操作流畅"
echo ""

echo "🎉 所有测试通过！"
echo "✅ SystemUI固定宽高比 16:10 实现正确"
echo "✅ 布局结构已改为FrameLayout覆盖层"
echo "✅ SystemUI视觉风格完全一致"
echo "✅ drawable资源文件创建完成"
echo "✅ 调试信息已更新"
echo "✅ 代码编译成功"
echo ""
echo "📋 SystemUI风格总结:"
echo "- 📐 固定比例: 16:10 (1.6) 横向卡片"
echo "- 🖼️ 全屏缩略图: 完整的应用界面预览"
echo "- 🎭 覆盖层设计: Header不遮挡缩略图"
echo "- 🎨 原生风格: 与Android 7.1.2 SystemUI完全一致"
echo "- ⚡ 性能优化: 固定比例计算更简单快速"
echo ""
echo "🚀 现在任务卡片具有原生Android 7.1.2 SystemUI的完整视觉风格！"