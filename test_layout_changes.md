# 任务卡片垂直居中布局测试

## 修改内容

### 主要更改
1. **RecyclerView布局调整**：
   - 将 `android:layout_height` 从 `match_parent` 改为 `wrap_content`
   - 添加 `android:layout_gravity="center_vertical"` 实现垂直居中
   - 将 `android:padding` 拆分为 `android:paddingLeft` 和 `android:paddingRight`，移除上下内边距

### 修改文件
- `app/src/main/res/layout/recents_activity.xml`

### 预期效果
- 任务卡片将在屏幕垂直方向上居中显示
- 水平滚动功能保持不变
- 任务卡片高度为400dp，在屏幕中央显示

### 技术原理
1. **FrameLayout容器**：使用FrameLayout作为根容器，支持子视图的gravity属性
2. **wrap_content高度**：RecyclerView只占用实际内容所需的高度（任务卡片高度）
3. **center_vertical重力**：在FrameLayout中垂直居中显示RecyclerView
4. **水平内边距**：保持左右16dp的内边距，确保卡片不贴边显示

### 兼容性
- 保持与现有代码的完全兼容
- 不影响TaskAdapter和TaskViewHolder的实现
- 不影响动画效果和交互逻辑

## 测试建议

1. **构建测试**：
   ```bash
   ./gradlew assembleDebug
   ```

2. **安装测试**：
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

3. **功能测试**：
   - 启动应用查看任务卡片是否垂直居中
   - 测试水平滚动是否正常
   - 测试任务点击和删除功能
   - 测试空状态显示

4. **视觉验证**：
   - 任务卡片应该在屏幕垂直方向上居中显示
   - 上下应该有相等的空白区域
   - 左右保持16dp的内边距