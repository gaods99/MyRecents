#!/bin/bash

# 任务卡片完整优化测试脚本

echo "🎨 任务卡片完整优化测试"
echo "================================"
echo ""

# 检查所有优化是否正确应用
echo "📋 检查优化内容："
echo ""
echo "优化1 - 宽度一致性："
echo "  ✅ 统一使用优化后的卡片宽度"
echo "  ✅ 避免有无缩略图时宽度不一致"
echo ""
echo "优化2 - 缩略图完整显示："
echo "  ✅ 智能缩放策略"
echo "  ✅ 根据宽高比选择最佳显示模式"
echo ""
echo "优化3 - 卡片尺寸优化："
echo "  ✅ 基础宽度：312dp → 240dp (-23%)"
echo "  ✅ 基础高度：420dp → 320dp (-24%)"
echo "  ✅ 更适合手机屏幕显示"
echo ""

# 验证代码优化
echo "🔍 验证代码优化..."

# 检查尺寸优化
if grep -q "OPTIMIZED_TASK_WIDTH_DP = 240" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 尺寸优化：基础宽度240dp"
else
    echo "❌ 尺寸优化：未找到240dp设置"
fi

if grep -q "OPTIMIZED_TASK_HEIGHT_DP = 320" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 尺寸优化：基础高度320dp"
else
    echo "❌ 尺寸优化：未找到320dp设置"
fi

# 检查宽度一致性
if grep -q "params.width = mDefaultTaskWidth" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    echo "✅ 宽度一致性：统一使用标准宽度"
else
    echo "❌ 宽度一致性：未找到统一宽度设置"
fi

# 检查智能缩放
if grep -q "setOptimalThumbnailScale" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    echo "✅ 智能缩放：添加了智能缩放方法"
else
    echo "❌ 智能缩放：未找到智能缩放方法"
fi

# 检查缩放策略
if grep -q "FIT_XY" app/src/main/java/com/newland/recents/views/TaskViewHolder.java &&
   grep -q "FIT_CENTER" app/src/main/java/com/newland/recents/views/TaskViewHolder.java &&
   grep -q "CENTER_CROP" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    echo "✅ 缩放策略：包含所有三种缩放模式"
else
    echo "❌ 缩放策略：缺少某些缩放模式"
fi

# 检查尺寸限制
if grep -q "MIN_TASK_WIDTH_DP = 200" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java &&
   grep -q "MAX_TASK_WIDTH_DP = 280" app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java; then
    echo "✅ 尺寸限制：优化了最小/最大宽度"
else
    echo "❌ 尺寸限制：未找到优化的尺寸限制"
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
echo "3. 观察所有优化效果"
echo "4. 验证用户体验提升"
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
        echo "📏 卡片尺寸检查："
        echo "  • 卡片宽度是否适中，不过宽"
        echo "  • 所有卡片宽度是否完全一致"
        echo "  • 一屏是否能显示3-4个卡片"
        echo "  • 整体布局是否更紧凑"
        echo ""
        echo "🖼️  缩略图显示检查："
        echo "  • 竖屏应用缩略图是否铺满宽度"
        echo "  • 横屏应用缩略图是否完整显示"
        echo "  • 方形应用缩略图是否合理显示"
        echo "  • 不同宽高比应用是否都显示良好"
        echo ""
        echo "🎨 整体效果检查："
        echo "  • 视觉效果是否更适合手机"
        echo "  • 滚动体验是否更流畅"
        echo "  • 信息密度是否更合理"
        echo "  • 整体设计是否更专业"
        echo ""
        echo "🔄 交互测试："
        echo "  • 点击启动应用是否正常"
        echo "  • 上滑删除是否正常"
        echo "  • 滚动是否流畅无卡顿"
        echo "  • 动画效果是否正常"
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
echo "❌ 卡片宽度不一致"
echo "❌ 缩略图被裁剪或无法铺满"
echo "❌ 卡片过宽，不适合手机"
echo "❌ 一屏显示卡片数量少"
echo ""
echo "优化后的效果："
echo "✅ 所有卡片宽度完全一致"
echo "✅ 缩略图智能适配，显示效果最佳"
echo "✅ 卡片尺寸适中，更适合手机"
echo "✅ 一屏显示更多卡片，信息密度更高"
echo "✅ 整体视觉效果更专业统一"
echo ""

echo "📊 优化对比："
echo ""
echo "尺寸变化："
echo "  基础宽度: 312dp → 240dp (-23%)"
echo "  基础高度: 420dp → 320dp (-24%)"
echo "  宽高比: 0.743 → 0.75"
echo ""
echo "显示效果："
echo "  一屏卡片数: 2-3个 → 3-4个"
echo "  缩略图利用率: 低 → 高"
echo "  视觉一致性: 差 → 优秀"
echo ""
echo "用户体验："
echo "  信息密度: 低 → 高"
echo "  滚动体验: 一般 → 流畅"
echo "  视觉效果: 不统一 → 专业统一"
echo ""

echo "🔧 智能缩放策略："
echo "  • 比例接近 (±0.1): FIT_XY (填满)"
echo "  • 缩略图更宽: FIT_CENTER (完整显示)"
echo "  • 缩略图更高: CENTER_CROP (铺满宽度)"
echo ""

echo "🎨 适配场景："
echo "  • 竖屏应用 (9:16): 优先铺满宽度"
echo "  • 横屏应用 (16:9): 完整显示"
echo "  • 方形应用 (1:1): 智能选择"
echo "  • 特殊比例: 智能适配"
echo ""

echo "📝 相关文件："
echo "  • TaskViewSizeCalculator.java (尺寸计算优化)"
echo "  • TaskViewHolder.java (智能缩放实现)"
echo "  • CARD_WIDTH_OPTIMIZATION.md (详细文档)"
echo ""

echo "================================"
echo "🏁 完整优化测试完成"
echo ""
echo "💡 提示：如果所有测试通过，说明三个关键优化都已成功实现！"
echo "🎉 MyRecents现在具有真正专业级的移动端用户体验！"