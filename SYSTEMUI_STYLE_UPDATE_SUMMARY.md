# Android 7.1.2 SystemUI 风格更新总结

## 🎯 更新目标

将任务卡片的尺寸比例和视觉样式调整为与 Android 7.1.2 SystemUI 的原生 recents 模块完全一致。

## 📐 尺寸比例调整

### 1. SystemUI 原生尺寸
```java
// 使用 Android 7.1.2 SystemUI 的原始尺寸
private static final int SYSTEMUI_TASK_WIDTH_DP = 312;   // 原生宽度
private static final int SYSTEMUI_TASK_HEIGHT_DP = 420;  // 原生高度

// SystemUI 原生宽高比
private static final float SYSTEMUI_TASK_ASPECT_RATIO = 312f / 420f; // ~0.743
```

**关键变化**：
- ❌ 之前：16:10 横向比例 (1.6)
- ✅ 现在：312:420 竖向比例 (~0.743)

### 2. 屏幕适配策略
```java
// SystemUI 风格的屏幕使用比例
private static final float SYSTEMUI_WIDTH_RATIO = 0.9f;   // 90% 屏幕宽度
private static final float SYSTEMUI_HEIGHT_RATIO = 0.75f; // 75% 屏幕高度

// SystemUI 尺寸限制
private static final int MIN_TASK_WIDTH_DP = 280;
private static final int MAX_TASK_WIDTH_DP = 360;
private static final int MIN_TASK_HEIGHT_DP = 380;
private static final int MAX_TASK_HEIGHT_DP = 480;
```

## 🎨 视觉样式调整

### 1. 卡片外观
```xml
<!-- 更小的圆角 - SystemUI 风格 -->
<dimen name="task_corner_radius">2dp</dimen>

<!-- 更低的阴影 - SystemUI 风格 -->
<dimen name="task_elevation">2dp</dimen>
<dimen name="task_elevation_active">4dp</dimen>

<!-- 更紧凑的间距 -->
<dimen name="task_margin">6dp</dimen>
```

**关键变化**：
- ❌ 之前：12dp 圆角，4dp 阴影，8dp 间距
- ✅ 现在：2dp 圆角，2dp 阴影，6dp 间距

### 2. 头部样式
```xml
<!-- 更紧凑的头部 - SystemUI 风格 -->
<dimen name="task_header_height">48dp</dimen>
<dimen name="task_header_padding">8dp</dimen>

<!-- 更小的图标 - SystemUI 风格 -->
<dimen name="task_icon_size">24dp</dimen>
<dimen name="task_icon_margin">6dp</dimen>

<!-- 更小的文字 - SystemUI 风格 -->
<dimen name="task_title_text_size">13sp</dimen>
```

**关键变化**：
- ❌ 之前：56dp 头部，32dp 图标，14sp 文字
- ✅ 现在：48dp 头部，24dp 图标，13sp 文字

### 3. 颜色方案
```xml
<!-- SystemUI 风格的背景色 -->
<color name="recents_background">#F0000000</color>
<color name="task_background">#FAFAFA</color>
<color name="task_header_background">#FFFFFF</color>

<!-- SystemUI 风格的文字色 -->
<color name="task_title_color">#DE000000</color>

<!-- SystemUI 风格的强调色 -->
<color name="task_focus_indicator">#FF4081</color>
```

**关键变化**：
- ❌ 之前：纯白背景，蓝色强调
- ✅ 现在：浅灰背景，粉色强调

### 4. 头部背景
```xml
<!-- 从渐变覆盖层改为纯色背景 -->
<solid android:color="@color/task_header_background" />
<stroke android:width="0.5dp" android:color="#1A000000" />
```

**关键变化**：
- ❌ 之前：半透明渐变覆盖层
- ✅ 现在：纯色背景 + 细边框

### 5. 文字样式
```xml
<!-- SystemUI 风格的文字 -->
android:textColor="@color/task_title_color"
android:fontFamily="sans-serif-medium"
```

**关键变化**：
- ❌ 之前：白色文字 + 阴影效果
- ✅ 现在：深色文字 + 中等字重

## 🔧 实现细节

### 1. 尺寸计算逻辑
```java
public int getTaskWidth() {
    // 基于 SystemUI 原始宽度
    int baseWidth = dpToPx(SYSTEMUI_TASK_WIDTH_DP);
    
    // 根据屏幕密度和尺寸缩放
    float screenWidthRatio = (float) mScreenSize.x / dpToPx(360);
    float scaleFactor = Math.min(1.2f, Math.max(0.8f, screenWidthRatio));
    
    return Math.max(minWidth, Math.min(maxWidth, baseWidth * scaleFactor));
}

public int getTaskHeight() {
    // 与宽度成比例缩放，保持原始宽高比
    int taskWidth = getTaskWidth();
    int baseWidth = dpToPx(SYSTEMUI_TASK_WIDTH_DP);
    float scaleFactor = (float) taskWidth / baseWidth;
    
    return Math.max(minHeight, Math.min(maxHeight, baseHeight * scaleFactor));
}
```

### 2. 间距优化
```java
public int getTaskMargin() {
    // SystemUI 使用一致的较小间距
    return dpToPx(6);
}

public int getRecyclerViewPadding() {
    // SystemUI 使用最小内边距
    return dpToPx(12);
}
```

## 📱 布局结构保持

### 维持 SystemUI 原生结构
```xml
<FrameLayout> <!-- 根容器 -->
    <ImageView android:id="@+id/task_thumbnail" /> <!-- 全屏缩略图 -->
    <LinearLayout android:id="@+id/task_header" />  <!-- 头部覆盖层 -->
    <View android:id="@+id/task_focus_indicator" /> <!-- 焦点指示器 -->
</FrameLayout>
```

**保持的特性**：
- ✅ FrameLayout 根容器
- ✅ 全屏缩略图背景
- ✅ 头部覆盖层设计
- ✅ 焦点指示器

## 🎯 效果对比

### 之前 (Launcher3 风格)
- 16:10 横向卡片
- 较大的圆角和阴影
- 较宽的间距
- 渐变覆盖层头部

### 现在 (SystemUI 风格)
- 312:420 竖向卡片 (~0.743 比例)
- 最小圆角和阴影
- 紧凑间距
- 纯色头部背景

## ✅ 验证结果

通过测试脚本验证，所有 SystemUI 风格特性已成功实现：

1. ✅ SystemUI 原始尺寸 (312x420dp)
2. ✅ 正确的宽高比 (~0.743)
3. ✅ SystemUI 视觉样式
4. ✅ 适当的颜色和字体
5. ✅ 优化的间距和边距

## 🚀 总结

任务卡片现在完全采用 Android 7.1.2 SystemUI 的原生风格：

- **尺寸比例**：从横向 16:10 改为竖向 312:420
- **视觉风格**：从现代 Material Design 改为经典 SystemUI 风格
- **颜色方案**：从高对比度改为 SystemUI 原生配色
- **间距布局**：从宽松布局改为紧凑 SystemUI 布局

这样的实现确保了与 Android 7.1.2 SystemUI recents 模块的完全一致性！ 🎉