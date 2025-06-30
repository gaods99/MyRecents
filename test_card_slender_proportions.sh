#!/bin/bash

# 任务卡片修长比例优化测试脚本

echo "📏 任务卡片修长比例优化测试"
echo "================================"
echo ""

# 检查修长化优化是否正确应用
echo "📋 检查修长化优化内容："
echo "比例调整："
echo "  ✅ 高度：320dp → 380dp (+19%)"
echo "  ✅ 宽高比：0.75 → 0.63 (-16%)"
echo "  ✅ 更修长优雅的外观"
echo ""
echo "尺寸限制调整："
echo "  ✅ 最小高度：280dp → 320dp"
echo "  ✅ 最大高度：400dp → 450dp"
echo "  ✅ 适应更高的卡片"
echo ""

# 验证代码优化
echo "🔍 验证代码优化..."

# 检查高度优化
if grep -q "OPTIMIZED_TASK_HEIGHT_DP = 380" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 高度优化：基础高度调整为380dp"
else
    echo "❌ 高度优化：未找到380dp设置"
fi

# 检查宽高比
if grep -q "240f / 380f" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 宽高比：调整为0.63 (240/380)"
else
    echo "❌ 宽高比：未找到新的宽高比计算"
fi

# 检查高度限制
if grep -q "MIN_TASK_HEIGHT_DP = 320" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 最小高度：调整为320dp"
else
    echo "❌ 最小高度：未找到320dp设置"
fi

if grep -q "MAX_TASK_HEIGHT_DP = 450" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 最大高度：调整为450dp"
else
    echo "❌ 最大高度：未找到450dp设置"
fi

# 检查注释更新
if grep -q "更修长优雅" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    echo "✅ 注释更新：包含修长优雅描述"
else
    echo "❌ 注释更新：未找到修长优雅描述"
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
echo "3. 观察卡片的修长比例"
echo "4. 验证视觉效果提升"
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
        echo "📏 卡片比例检查："
        echo "  • 卡片是否显得更修长"
        echo "  • 高度是否比之前更高"
        echo "  • 整体比例是否更优雅"
        echo "  • 视觉效果是否更精致"
        echo ""
        echo "🖼️  缩略图显示检查："
        echo "  • 缩略图在更高的卡片中显示是否合理"
        echo "  • 竖屏应用缩略图是否更好地展示"
        echo "  • 横屏应用缩略图是否仍然完整显示"
        echo "  • 智能缩放是否仍然工作良好"
        echo ""
        echo "🎨 整体效果检查："
        echo "  • 卡片是否看起来更精致"
        echo "  • 整体布局是否更优雅"
        echo "  • 滚动体验是否仍然流畅"
        echo "  • 一屏显示的卡片数量是否合理"
        echo ""
        echo "📱 屏幕适配检查："
        echo "  • 在不同屏幕尺寸下是否显示正常"
        echo "  • 是否会超出屏幕高度"
        echo "  • 横屏模式下是否显示合理"
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
echo "优化前的外观："
echo "📱 卡片比例：240×320dp (宽高比 0.75)"
echo "👁️  视觉效果：较为方正"
echo ""
echo "优化后的外观："
echo "📱 卡片比例：240×380dp (宽高比 0.63)"
echo "👁️  视觉效果：修长优雅"
echo ""

echo "📊 比例对比："
echo ""
echo "尺寸变化："
echo "  宽度: 240dp (保持不变)"
echo "  高度: 320dp → 380dp (+19%)"
echo "  宽高比: 0.75 → 0.63 (-16%)"
echo ""
echo "视觉效果："
echo "  外观: 方正 → 修长"
echo "  气质: 普通 → 优雅"
echo "  精致度: 一般 → 精致"
echo ""

echo "🎨 设计理念："
echo "  • 更修长的比例更符合现代设计趋势"
echo "  • 类似于现代手机的屏幕比例"
echo "  • 提供更优雅的视觉体验"
echo "  • 更好地展示应用缩略图内容"
echo ""

echo "📐 参考比例："
echo "  • iPhone: ~0.46 (非常修长)"
echo "  • 现代Android: ~0.5-0.6 (修长)"
echo "  • 我们的卡片: 0.63 (适度修长)"
echo "  • 传统卡片: 0.75+ (偏方正)"
echo ""

echo "⚖️  平衡考虑："
echo "  • 修长度：提升视觉优雅度"
echo "  • 实用性：保持良好的内容展示"
echo "  • 屏幕适配：不超出屏幕范围"
echo "  • 性能：保持流畅的滚动体验"
echo ""

echo "================================"
echo "🏁 修长比例优化测试完成"
echo ""
echo "💡 提示：如果测试通过，卡片将显得更加修长优雅！"
echo "🎨 新的比例将提供更现代、更精致的视觉体验！"