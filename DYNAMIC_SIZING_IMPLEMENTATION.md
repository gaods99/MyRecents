# 动态尺寸实现 - 参考Launcher3 Quickstep

## 概述

参考Launcher3 Quickstep的实现方式，将固定的任务卡片尺寸改为动态计算，以适应不同屏幕尺寸和密度的设备。

## 实现原理

### 1. TaskViewSizeCalculator 工具类

新增的核心工具类，负责动态计算任务卡片的各种尺寸：

#### 主要功能：
- **动态宽度计算**：基于屏幕宽度的85%，考虑横竖屏差异
- **动态高度计算**：基于宽高比(0.7)和屏幕高度的60%
- **智能间距**：根据屏幕密度动态调整卡片间距
- **自适应内边距**：确保边缘卡片有合适的显示空间

#### 计算逻辑：
```java
// 宽度计算
int availableWidth = (int) (screenWidth * 0.85f);
if (isLandscape) availableWidth *= 1.2f;
taskWidth = clamp(availableWidth, minWidth, maxWidth);

// 高度计算  
int aspectBasedHeight = (int) (taskWidth / 0.7f);
int screenBasedHeight = (int) (screenHeight * 0.6f);
taskHeight = min(aspectBasedHeight, screenBasedHeight);
```

### 2. 尺寸限制

#### 最小/最大尺寸 (dp)：
- 宽度：200dp - 400dp
- 高度：250dp - 500dp

#### 屏幕利用率：
- 宽度：屏幕宽度的85% (横屏时120%)
- 高度：屏幕高度的60%

#### 宽高比：
- 固定比例：0.7 (宽度/高度)

### 3. 密度自适应间距

根据屏幕密度动态调整：
- xxxhdpi (3.0+): 12dp
- xxhdpi (2.0+): 10dp  
- xhdpi (1.5+): 8dp
- hdpi及以下: 6dp

## 文件修改

### 新增文件
- `app/src/main/java/com/newland/recents/utils/TaskViewSizeCalculator.java`

### 修改文件

#### 1. TaskAdapter.java
- 导入TaskViewSizeCalculator
- 在onCreateViewHolder中动态设置卡片尺寸
- 移除对固定尺寸资源的依赖

#### 2. RecentsActivity.java  
- 导入TaskViewSizeCalculator
- 在setupRecyclerView中使用动态计算的间距和内边距
- 添加调试日志输出

#### 3. task_item.xml
- 将CardView的layout_width和layout_height改为wrap_content
- 移除对@dimen/task_width和@dimen/task_height的引用

#### 4. dimens.xml
- 移除固定的task_width和task_height定义
- 保留其他必要的尺寸定义

## 优势

### 1. 设备适配性
- **多屏幕支持**：自动适应不同尺寸的设备
- **密度适配**：根据屏幕密度调整间距
- **方向适配**：横竖屏切换时自动调整

### 2. 用户体验
- **最佳显示效果**：充分利用屏幕空间
- **一致性**：在不同设备上保持相似的视觉比例
- **可读性**：确保任务信息清晰可见

### 3. 维护性
- **代码集中**：所有尺寸计算逻辑集中在一个类中
- **易于调整**：修改比例和限制值即可调整整体效果
- **调试友好**：提供详细的调试信息输出

## 调试信息

TaskViewSizeCalculator提供详细的调试信息：
```
Screen: 1080x1920, Density: 3.0, Landscape: false, TaskSize: 306x437
```

包含：
- 屏幕分辨率
- 屏幕密度
- 横竖屏状态  
- 计算出的任务卡片尺寸

## 兼容性

- ✅ 完全向后兼容现有代码
- ✅ 不影响任务管理逻辑
- ✅ 保持动画效果
- ✅ 支持Android 7.0+

## 测试建议

1. **多设备测试**：在不同屏幕尺寸的设备上测试
2. **密度测试**：测试不同DPI设备的显示效果
3. **方向测试**：测试横竖屏切换
4. **边界测试**：测试极小和极大屏幕的显示效果

## 未来扩展

可以进一步优化的方向：
- 根据任务数量动态调整卡片尺寸
- 支持用户自定义卡片大小
- 添加更多的屏幕适配策略
- 支持平板电脑的特殊布局