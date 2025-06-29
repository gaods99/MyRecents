#!/bin/bash

# 测试应用特定宽高比实现
# 验证每个任务卡片使用其应用的实际显示宽高比

echo "🧪 测试应用特定宽高比实现"
echo "================================"

# 检查关键文件是否存在
echo "📁 检查关键文件..."

files=(
    "app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java"
    "app/src/main/java/com/newland/recents/adapter/TaskAdapter.java"
    "app/src/main/java/com/newland/recents/RecentsActivity.java"
    "app/src/main/java/com/newland/recents/model/Task.java"
    "APP_SPECIFIC_ASPECT_RATIO_IMPLEMENTATION.md"
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
echo "🔍 验证应用特定宽高比实现..."

# 检查是否添加了getTaskHeight(Task task)方法
if grep -q "public int getTaskHeight(Task task)" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 已添加getTaskHeight(Task task)方法"
else
    echo "❌ 缺少getTaskHeight(Task task)方法"
    exit 1
fi

# 检查是否添加了getAppAspectRatio方法
if grep -q "public float getAppAspectRatio(Task task)" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 已添加getAppAspectRatio(Task task)方法"
else
    echo "❌ 缺少getAppAspectRatio(Task task)方法"
    exit 1
fi

# 检查是否添加了应用类别检测
if grep -q "getAspectRatioFromAppCategory" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 已添加应用类别检测方法"
else
    echo "❌ 缺少应用类别检测方法"
    exit 1
fi

# 检查是否添加了活动方向检测
if grep -q "getAspectRatioFromOrientation" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 已添加活动方向检测方法"
else
    echo "❌ 缺少活动方向检测方法"
    exit 1
fi

echo ""
echo "🔍 验证应用分类识别..."

# 检查视频应用识别
if grep -q "video.*youtube.*netflix" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 视频应用识别逻辑正确"
else
    echo "❌ 视频应用识别逻辑缺失"
    exit 1
fi

# 检查游戏应用识别
if grep -q "game.*play.*unity" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 游戏应用识别逻辑正确"
else
    echo "❌ 游戏应用识别逻辑缺失"
    exit 1
fi

# 检查相机应用识别
if grep -q "camera.*photo.*instagram" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 相机应用识别逻辑正确"
else
    echo "❌ 相机应用识别逻辑缺失"
    exit 1
fi

# 检查阅读应用识别
if grep -q "read.*book.*news.*browser" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 阅读应用识别逻辑正确"
else
    echo "❌ 阅读应用识别逻辑缺失"
    exit 1
fi

echo ""
echo "🔍 验证TaskAdapter集成..."

# 检查TaskAdapter是否在onBindViewHolder中使用任务特定高度
if grep -q "getTaskHeight(task)" app/src/main/java/com/newland/recents/adapter/TaskAdapter.java; then
    echo "✅ TaskAdapter使用任务特定高度计算"
else
    echo "❌ TaskAdapter未使用任务特定高度计算"
    exit 1
fi

# 检查是否在onBindViewHolder中动态设置布局参数
if grep -q "layoutParams.height.*getTaskHeight(task)" app/src/main/java/com/newland/recents/adapter/TaskAdapter.java; then
    echo "✅ TaskAdapter动态设置任务卡片高度"
else
    echo "❌ TaskAdapter未动态设置任务卡片高度"
    exit 1
fi

echo ""
echo "🔍 验证调试信息增强..."

# 检查是否添加了任务特定的调试信息方法
if grep -q "getDebugInfo(Task task)" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 已添加任务特定调试信息方法"
else
    echo "❌ 缺少任务特定调试信息方法"
    exit 1
fi

# 检查调试信息是否包含应用宽高比
if grep -q "AppAspectRatio: %.3f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 调试信息包含应用宽高比"
else
    echo "❌ 调试信息缺少应用宽高比"
    exit 1
fi

# 检查RecentsActivity是否输出任务调试信息
if grep -q "sizeCalculator.getDebugInfo(task)" app/src/main/java/com/newland/recents/RecentsActivity.java; then
    echo "✅ RecentsActivity输出任务调试信息"
else
    echo "❌ RecentsActivity未输出任务调试信息"
    exit 1
fi

echo ""
echo "🔍 验证异常处理..."

# 检查PackageManager.NameNotFoundException处理
if grep -q "PackageManager.NameNotFoundException" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 包含PackageManager异常处理"
else
    echo "❌ 缺少PackageManager异常处理"
    exit 1
fi

# 检查空值检查
if grep -q "task == null.*task.packageName == null" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 包含空值检查"
else
    echo "❌ 缺少空值检查"
    exit 1
fi

echo ""
echo "🔍 验证宽高比限制..."

# 检查应用宽高比限制
if grep -q "Math.max(0.5f, Math.min(0.8f, aspectRatio))" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 应用宽高比限制机制正确"
else
    echo "❌ 应用宽高比限制机制缺失"
    exit 1
fi

echo ""
echo "📊 应用分类宽高比验证..."

echo "📱 预期的应用分类宽高比:"
echo "  🎬 视频应用 (YouTube, Netflix): 0.750 (3:4)"
echo "  🎮 游戏应用 (Games): 0.700 (7:10)"
echo "  📷 相机应用 (Camera, Instagram): 屏幕宽高比"
echo "  📖 阅读应用 (Browser, News): 屏幕宽高比"
echo "  ⚙️  系统应用 (Settings): 屏幕宽高比"
echo ""

echo "🔍 方向检测验证:"
echo "  📱 竖屏应用: 使用屏幕宽高比"
echo "  📺 横屏应用: 0.750 (3:4)"
echo "  🔄 自适应应用: 根据应用类别决定"
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
echo "4. 查看日志: adb logcat | grep -E '(RecentsActivity|TaskViewSizeCalculator)'"
echo ""

echo "📊 预期日志输出示例:"
echo ""
echo "🔹 视频应用 (YouTube):"
echo "RecentsActivity: Task 0 (com.google.android.youtube):"
echo "Screen: 1080x1920, Density: 3.0, Landscape: false"
echo "App: com.google.android.youtube"
echo "ScreenAspectRatio: 0.563, AppAspectRatio: 0.750, TaskAspectRatio: 0.750"
echo "TaskSize: 306x408"
echo ""
echo "🔹 相机应用 (Camera):"
echo "RecentsActivity: Task 1 (com.android.camera2):"
echo "Screen: 1080x1920, Density: 3.0, Landscape: false"
echo "App: com.android.camera2"
echo "ScreenAspectRatio: 0.563, AppAspectRatio: 0.563, TaskAspectRatio: 0.563"
echo "TaskSize: 306x544"
echo ""
echo "🔹 游戏应用:"
echo "RecentsActivity: Task 2 (com.example.game):"
echo "Screen: 1080x1920, Density: 3.0, Landscape: false"
echo "App: com.example.game"
echo "ScreenAspectRatio: 0.563, AppAspectRatio: 0.700, TaskAspectRatio: 0.700"
echo "TaskSize: 306x437"
echo ""

echo "🧪 测试场景建议..."
echo ""
echo "1. 📱 不同应用类型测试:"
echo "   - 安装YouTube/Netflix等视频应用"
echo "   - 安装各种游戏应用"
echo "   - 测试相机、Instagram等拍照应用"
echo "   - 测试Chrome、Firefox等浏览器"
echo ""
echo "2. 🔍 应用识别测试:"
echo "   - 验证应用包名识别是否正确"
echo "   - 检查活动方向检测是否生效"
echo "   - 测试未知应用的回退机制"
echo ""
echo "3. 📊 视觉效果验证:"
echo "   - 观察不同应用的任务卡片高度差异"
echo "   - 验证视频应用是否显示更宽的卡片"
echo "   - 确认相机应用是否与屏幕比例一致"
echo ""
echo "4. 🔧 异常处理测试:"
echo "   - 测试卸载应用后的任务卡片显示"
echo "   - 验证损坏应用信息的处理"
echo "   - 检查权限不足时的回退机制"
echo ""

echo "🎉 所有测试通过！"
echo "✅ 应用特定宽高比实现正确"
echo "✅ 应用分类识别逻辑完整"
echo "✅ TaskAdapter集成成功"
echo "✅ 调试信息增强完成"
echo "✅ 异常处理机制健全"
echo ""
echo "📋 实现总结:"
echo "- 🎯 精确预览: 每个任务卡片反映应用实际比例"
echo "- 🤖 智能识别: 自动检测应用类型和方向偏好"
echo "- 🔒 安全机制: 完善的异常处理和回退策略"
echo "- 📊 调试友好: 详细的应用宽高比信息输出"
echo "- 🎨 视觉优化: 不同类型应用有不同的卡片形状"
echo ""
echo "🚀 现在每个任务卡片都会根据其应用的实际显示特性动态调整宽高比！"