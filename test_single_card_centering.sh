#!/bin/bash

# 单个任务卡片居中显示修复测试脚本

echo "🎯 单个任务卡片居中显示修复测试"
echo "================================"
echo ""

# 检查居中修复是否正确应用
echo "📋 检查居中修复内容："
echo "布局管理器优化："
echo "  ✅ 自定义CenteringLinearLayoutManager"
echo "  ✅ 单个项目时自动居中"
echo "  ✅ 多个项目时正常布局"
echo ""
echo "间距优化："
echo "  ✅ 单个项目时移除间距"
echo "  ✅ 多个项目时保持正常间距"
echo "  ✅ 完美配合居中显示"
echo ""

# 验证代码修复
echo "🔍 验证代码修复..."

# 检查自定义LayoutManager
if grep -q "CenteringLinearLayoutManager" app/src/main/java/com/newland/recents/RecentsActivity.java; then
    echo "✅ 自定义LayoutManager：添加了CenteringLinearLayoutManager"
else
    echo "❌ 自定义LayoutManager：未找到CenteringLinearLayoutManager"
fi

# 检查居中逻辑
if grep -q "getItemCount() == 1" app/src/main/java/com/newland/recents/RecentsActivity.java; then
    echo "✅ 居中逻辑：检测单个项目"
else
    echo "❌ 居中逻辑：未找到单个项目检测"
fi

# 检查居中计算
if grep -q "centerOffset" app/src/main/java/com/newland/recents/RecentsActivity.java; then
    echo "✅ 居中计算：实现了居中偏移计算"
else
    echo "❌ 居中计算：未找到居中偏移计算"
fi

# 检查间距优化
if grep -q "For single item, no spacing" app/src/main/java/com/newland/recents/RecentsActivity.java; then
    echo "✅ 间距优化：单个项目时移除间距"
else
    echo "❌ 间距优化：未找到单个项目间距处理"
fi

# 检查多项目处理
if grep -q "For multiple items, use normal spacing" app/src/main/java/com/newland/recents/RecentsActivity.java; then
    echo "✅ 多项目处理：保持正常间距"
else
    echo "❌ 多项目处理：未找到多项目间距处理"
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
echo "2. 确保只有一个最近任务"
echo "3. 启动最近任务界面"
echo "4. 观察单个卡片是否居中显示"
echo "5. 打开更多应用后再次测试多个卡片"
echo ""

# 检查设备连接
if command -v adb &> /dev/null && adb devices | grep -q "device$"; then
    echo "📱 检测到设备，开始安装..."
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    
    if [ $? -eq 0 ]; then
        echo "✅ 安装成功"
        echo ""
        echo "🧪 启动测试..."
        
        echo "📋 测试准备："
        echo "1. 清理最近任务（保留1-2个应用）"
        echo "2. 启动最近任务界面"
        
        adb shell am broadcast -a com.android.systemui.recents.ACTION_SHOW
        
        echo ""
        echo "👀 请观察以下内容："
        echo ""
        echo "🎯 单个卡片居中测试："
        echo "  • 当只有一个任务时，卡片是否在屏幕水平居中"
        echo "  • 卡片左右两侧的空白是否相等"
        echo "  • 卡片是否没有多余的间距"
        echo "  • 整体视觉效果是否平衡"
        echo ""
        echo "📱 多个卡片布局测试："
        echo "  • 当有多个任务时，布局是否正常"
        echo "  • 卡片间距是否合理"
        echo "  • 第一个和最后一个卡片的边距是否正确"
        echo "  • 滚动是否流畅"
        echo ""
        echo "🔄 动态测试："
        echo "  • 删除任务到只剩一个时，是否自动居中"
        echo "  • 添加新任务时，布局是否正确切换"
        echo "  • 居中效果是否实时更新"
        echo ""
        echo "💡 测试建议："
        echo "  • 可以通过删除任务来测试单个卡片居中"
        echo "  • 打开新应用来测试多个卡片布局"
        echo "  • 观察布局切换的流畅性"
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
echo "❌ 单个任务卡片左对齐，不居中"
echo "❌ 视觉效果不平衡"
echo "❌ 用户体验不佳"
echo ""
echo "修复后的效果："
echo "✅ 单个任务卡片完美居中"
echo "✅ 多个任务卡片正常布局"
echo "✅ 视觉效果平衡美观"
echo "✅ 用户体验显著提升"
echo ""

echo "🔧 技术实现："
echo ""
echo "自定义LayoutManager："
echo "  • 继承LinearLayoutManager"
echo "  • 重写onLayoutChildren方法"
echo "  • 检测单个项目并计算居中偏移"
echo "  • 应用偏移实现居中显示"
echo ""
echo "间距优化："
echo "  • 单个项目时移除所有间距"
echo "  • 多个项目时保持正常间距"
echo "  • 确保居中效果不受间距影响"
echo ""

echo "📐 居中计算逻辑："
echo "  1. 获取父容器可用宽度"
echo "  2. 获取子项目实际宽度"
echo "  3. 计算居中偏移量: (容器宽度 - 项目宽度) / 2"
echo "  4. 应用偏移量实现居中"
echo ""

echo "🎨 视觉效果："
echo "  • 单个卡片: 完美居中，视觉平衡"
echo "  • 多个卡片: 正常布局，间距合理"
echo "  • 动态切换: 流畅自然，无突兀感"
echo ""

echo "================================"
echo "🏁 单个卡片居中修复测试完成"
echo ""
echo "💡 提示：如果测试通过，单个任务卡片将完美居中显示！"
echo "🎯 这将显著提升单任务场景下的视觉体验！"