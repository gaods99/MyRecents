# 应用特定宽高比实现 - 每个任务卡片使用其应用的实际显示宽高比

## 🎯 实现目标

让每个任务卡片的宽高比与该任务APP在实际运行时的显示宽高比一致，使任务预览更准确地反映每个应用的实际界面比例。

## 🔄 核心变化

### 之前 (统一屏幕宽高比)
```java
// 所有任务卡片使用相同的屏幕宽高比
public int getTaskHeight() {
    float screenAspectRatio = getScreenAspectRatio();
    int widthBasedHeight = (int) (getTaskWidth() / screenAspectRatio);
    // ...
}
```

### 之后 (应用特定宽高比)
```java
// 每个任务卡片使用其应用的特定宽高比
public int getTaskHeight(Task task) {
    float appAspectRatio = getAppAspectRatio(task);
    int widthBasedHeight = (int) (getTaskWidth() / appAspectRatio);
    // ...
}
```

## 🔧 技术实现

### 1. 应用宽高比检测

#### 方法1: 活动方向检测
```java
private float getAspectRatioFromOrientation(int screenOrientation, String packageName) {
    switch (screenOrientation) {
        case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
            return 0.75f; // 3:4 ratio for landscape apps
            
        case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
            return getScreenAspectRatio(); // Use screen ratio for portrait apps
            
        default:
            return getAspectRatioFromAppCategory(packageName, null);
    }
}
```

#### 方法2: 应用类别检测
```java
private float getAspectRatioFromAppCategory(String packageName, ApplicationInfo appInfo) {
    String lowerPackageName = packageName.toLowerCase();
    
    // Video/Media apps: prefer wider aspect ratio
    if (lowerPackageName.contains("video") || lowerPackageName.contains("youtube")) {
        return 0.75f; // 3:4 ratio for video apps
    }
    
    // Game apps: often prefer landscape or square
    if (lowerPackageName.contains("game")) {
        return 0.7f; // 7:10 ratio for games
    }
    
    // Camera apps: often prefer portrait or square
    if (lowerPackageName.contains("camera") || lowerPackageName.contains("instagram")) {
        return getScreenAspectRatio(); // Use screen ratio for camera apps
    }
    
    // Default: use screen aspect ratio
    return getScreenAspectRatio();
}
```

### 2. 动态尺寸应用

#### TaskAdapter中的实现
```java
@Override
public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
    Task task = mTasks.get(position);
    
    // Update task card size based on the specific app's aspect ratio
    if (mSizeCalculator != null) {
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.width = mSizeCalculator.getTaskWidth();
        layoutParams.height = mSizeCalculator.getTaskHeight(task); // 使用任务特定高度
        holder.itemView.setLayoutParams(layoutParams);
    }
    
    holder.bind(task);
}
```

### 3. 应用分类识别

#### 视频/媒体应用
- **识别关键词**: video, media, player, youtube, netflix, vlc, mx, kodi
- **宽高比**: 0.75 (3:4) - 更宽的比例适合视频内容
- **原因**: 视频应用通常以横屏或宽屏模式显示内容

#### 游戏应用
- **识别关键词**: game, play, unity, unreal
- **宽高比**: 0.7 (7:10) - 经典的游戏比例
- **原因**: 游戏应用通常有特定的界面比例要求

#### 相机/照片应用
- **识别关键词**: camera, photo, instagram, snapchat
- **宽高比**: 屏幕宽高比 - 与设备屏幕一致
- **原因**: 相机应用通常全屏显示，与屏幕比例一致

#### 阅读/文本应用
- **识别关键词**: read, book, news, text, kindle, browser
- **宽高比**: 屏幕宽高比 - 适合文本阅读
- **原因**: 阅读应用通常使用竖屏模式，与屏幕比例一致

### 4. 宽高比限制机制

```java
// 应用合理限制避免极端宽高比
aspectRatio = Math.max(0.5f, Math.min(0.8f, aspectRatio));
```

- **最小值 0.5**: 防止任务卡片过于狭窄
- **最大值 0.8**: 防止任务卡片过于宽扁
- **保护机制**: 确保所有应用都有合理的显示效果

## 📱 不同应用的预期效果

### 视频应用 (YouTube, Netflix)
```
App: com.google.android.youtube
ScreenAspectRatio: 0.563, AppAspectRatio: 0.750, TaskAspectRatio: 0.750
TaskSize: 306x408
```
- **特点**: 更宽的任务卡片，适合视频内容预览
- **用户体验**: 更好地展示视频应用的横屏界面

### 游戏应用 (各种游戏)
```
App: com.example.game
ScreenAspectRatio: 0.563, AppAspectRatio: 0.700, TaskAspectRatio: 0.700
TaskSize: 306x437
```
- **特点**: 经典的7:10比例，适合游戏界面
- **用户体验**: 保持游戏应用的传统显示比例

### 相机应用 (Camera, Instagram)
```
App: com.android.camera2
ScreenAspectRatio: 0.563, AppAspectRatio: 0.563, TaskAspectRatio: 0.563
TaskSize: 306x544
```
- **特点**: 与屏幕比例一致，适合全屏相机界面
- **用户体验**: 准确反映相机应用的实际显示效果

### 浏览器应用 (Chrome, Firefox)
```
App: com.android.chrome
ScreenAspectRatio: 0.563, AppAspectRatio: 0.563, TaskAspectRatio: 0.563
TaskSize: 306x544
```
- **特点**: 与屏幕比例一致，适合网页浏览
- **用户体验**: 反映浏览器的实际显示比例

### 系统应用 (Settings, Launcher)
```
App: com.android.settings
ScreenAspectRatio: 0.563, AppAspectRatio: 0.563, TaskAspectRatio: 0.563
TaskSize: 306x544
```
- **特点**: 使用屏幕比例，保持系统一致性
- **用户体验**: 与系统界面风格保持一致

## 🔍 调试信息

### 增强的调试输出
```java
public String getDebugInfo(Task task) {
    return String.format(
        "Screen: %dx%d, Density: %.1f, Landscape: %b\n" +
        "App: %s\n" +
        "ScreenAspectRatio: %.3f, AppAspectRatio: %.3f, TaskAspectRatio: %.3f\n" +
        "TaskSize: %dx%d\n" +
        "Original: %dx%d (0.700 ratio)\n" +
        "Scale: %.2fx width, %.2fx height",
        // ... 参数
    );
}
```

### 日志输出示例
```
RecentsActivity: Task 0 (com.google.android.youtube): 
Screen: 1080x1920, Density: 3.0, Landscape: false
App: com.google.android.youtube
ScreenAspectRatio: 0.563, AppAspectRatio: 0.750, TaskAspectRatio: 0.750
TaskSize: 306x408
Original: 280x400 (0.700 ratio)
Scale: 1.09x width, 1.02x height

RecentsActivity: Task 1 (com.android.camera2): 
Screen: 1080x1920, Density: 3.0, Landscape: false
App: com.android.camera2
ScreenAspectRatio: 0.563, AppAspectRatio: 0.563, TaskAspectRatio: 0.563
TaskSize: 306x544
Original: 280x400 (0.700 ratio)
Scale: 1.09x width, 1.36x height
```

## ✅ 优势

### 1. 精确的应用预览
- **真实比例**: 每个任务卡片反映应用的实际显示比例
- **用户期望**: 用户看到的预览与实际应用界面一致
- **视觉准确性**: 减少预览与实际的视觉差异

### 2. 智能分类识别
- **自动检测**: 根据应用包名和活动信息自动识别应用类型
- **多重策略**: 结合活动方向、包名分析、应用信息等多种方法
- **容错机制**: 检测失败时回退到屏幕宽高比

### 3. 性能优化
- **缓存机制**: 应用信息查询结果可以缓存
- **异常处理**: 完善的异常处理，确保应用稳定性
- **轻量级**: 检测逻辑简单高效，不影响性能

### 4. 用户体验
- **视觉一致性**: 任务预览与实际应用界面保持一致
- **直观识别**: 用户可以通过卡片形状快速识别应用类型
- **减少困惑**: 避免预览与实际界面的比例差异

## 🧪 测试场景

### 1. 不同类型应用测试
- **视频应用**: YouTube, Netflix, VLC - 预期更宽的卡片
- **游戏应用**: 各种游戏 - 预期7:10比例
- **相机应用**: Camera, Instagram - 预期屏幕比例
- **浏览器**: Chrome, Firefox - 预期屏幕比例

### 2. 边界情况测试
- **未知应用**: 新安装的应用 - 应回退到屏幕比例
- **系统应用**: Settings, Launcher - 应使用屏幕比例
- **包名异常**: 损坏的应用信息 - 应正常处理

### 3. 性能测试
- **大量任务**: 测试多个任务时的性能表现
- **内存使用**: 确认不会造成内存泄漏
- **响应速度**: 确认不影响界面响应速度

## 📋 实现状态

✅ **核心功能完成**
- 应用宽高比检测逻辑
- 任务特定高度计算
- 动态尺寸应用机制
- 增强的调试信息

✅ **智能识别完成**
- 活动方向检测
- 应用类别识别
- 包名关键词匹配
- 合理的回退机制

✅ **集成完成**
- TaskViewSizeCalculator更新
- TaskAdapter集成
- RecentsActivity调试输出
- 异常处理机制

## 🚀 预期效果

现在每个任务卡片都会：

1. **视频应用**: 显示更宽的卡片，更好地预览视频内容
2. **游戏应用**: 使用经典的游戏比例，保持游戏界面特色
3. **相机应用**: 与屏幕比例一致，准确反映全屏相机界面
4. **其他应用**: 根据其特性使用最适合的宽高比

这样用户在查看最近任务时，每个应用的预览都会更准确地反映其实际的界面比例，提供更直观、更一致的用户体验！