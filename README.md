# MyRecents - Android 系统级最近任务管理器

## 项目概述

MyRecents是一个基于Android系统的最近任务管理器，完全参考Launcher3 Quickstep和SystemUI的实现，提供流畅的任务切换和管理体验。

### 主要特性

- 🎨 **Launcher3风格UI**: 水平滚动的任务卡片界面
- 🔧 **SystemUI级任务管理**: 完整的任务获取、删除、启动功能
- 📡 **系统广播支持**: 响应标准的SystemUI广播命令
- 🏗️ **系统应用架构**: 使用android.uid.system获得完整权限
- 📱 **Android 7兼容**: 完全支持Android 7.0及以上版本

## 快速开始

### 1. 构建系统应用
```bash
# 构建系统应用APK
chmod +x build_system_app.sh
./build_system_app.sh
```

### 2. 安装到设备
```bash
# 自动安装脚本（推荐）
chmod +x install_system_app.sh
./install_system_app.sh

# 或手动安装到系统分区
adb root && adb remount
adb push app/build/outputs/apk/debug/app-debug.apk /system/app/MyRecents/
adb reboot
```

### 3. 测试功能
```bash
# 显示最近任务
adb shell am broadcast -a com.android.systemui.recents.ACTION_SHOW

# 隐藏最近任务
adb shell am broadcast -a com.android.systemui.recents.ACTION_HIDE

# 切换显示状态
adb shell am broadcast -a com.android.systemui.recents.ACTION_TOGGLE
```

## 项目架构

### 核心组件
```
com.newland.recents/
├── model/Task.java                    # 任务数据模型
├── loader/TaskLoader.java             # 异步任务加载器
├── manager/TaskManager.java           # 任务管理器
├── adapter/TaskAdapter.java           # RecyclerView适配器
├── views/TaskViewHolder.java          # 任务视图持有者
├── RecentsActivity.java               # 主界面Activity
├── RecentsController.java             # 控制器
├── SystemBroadcastReceiver.java       # 广播接收器
└── RecentsApp.java                    # 应用入口
```

### 技术特点

- **现代化UI**: 基于RecyclerView + CardView的Material Design
- **异步加载**: 使用AsyncTask异步加载任务和缩略图
- **多重删除策略**: ActivityManager API + 反射 + 广播
- **系统级权限**: android.uid.system + 完整的系统权限
- **Android 7兼容**: 处理API差异，确保向后兼容

## 系统权限

作为系统应用，MyRecents拥有以下权限：

```xml
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
<uses-permission android:name="android.permission.GET_TASKS" />
<uses-permission android:name="android.permission.REORDER_TASKS" />
<uses-permission android:name="android.permission.REMOVE_TASKS" />
<uses-permission android:name="android.permission.MANAGE_ACTIVITY_STACKS" />
<uses-permission android:name="android.permission.INTERNAL_SYSTEM_WINDOW" />
<uses-permission android:name="android.permission.STATUS_BAR_SERVICE" />
```

## 支持的广播

MyRecents响应以下系统广播：

| 广播Action | 功能 | 说明 |
|-----------|------|------|
| `com.android.systemui.recents.ACTION_SHOW` | 显示最近任务 | 打开任务切换界面 |
| `com.android.systemui.recents.ACTION_HIDE` | 隐藏最近任务 | 关闭任务切换界面 |
| `com.android.systemui.recents.ACTION_TOGGLE` | 切换显示状态 | 显示/隐藏切换 |

## 用户界面

### 任务卡片设计
- **缩略图显示**: 应用的实时截图预览
- **应用信息**: 图标、名称、状态
- **交互操作**: 点击启动、滑动删除
- **Material Design**: 圆角、阴影、动画效果

### 操作方式
- **点击任务**: 启动对应应用
- **向右滑动**: 删除任务
- **返回键**: 关闭任务界面
- **空状态**: 友好的无任务提示

## 开发指南

### 环境要求
- Android Studio 4.0+
- Android SDK 30+
- Java 8
- Gradle 7.5
- AGP 7.4.2

### 构建配置
```kotlin
android {
    compileSdk = 30
    defaultConfig {
        minSdk = 24        // Android 7.0
        targetSdk = 25     // Android 7.1
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
```

### 调试技巧
```bash
# 查看应用日志
adb logcat | grep -E "(RecentsActivity|TaskManager|TaskLoader)"

# 检查权限状态
adb shell dumpsys package com.newland.recents | grep permission

# 测试广播接收
adb shell am broadcast -a com.android.systemui.recents.ACTION_SHOW
```

## 兼容性

### Android版本支持
- ✅ Android 7.0 (API 24)
- ✅ Android 7.1 (API 25)
- ✅ Android 8.0+ (API 26+)

### 设备要求
- **Root权限**: 安装到系统分区需要
- **平台签名**: 标准安装需要平台密钥
- **内存**: 最低2GB RAM推荐

## 部署指南

### 开发环境
1. 使用root权限安装到系统分区
2. 通过ADB调试和测试
3. 查看logcat输出调试

### 生产环境
1. 使用平台密钥签名APK
2. 集成到系统ROM中
3. 配置为默认最近任务管理器

## 故障排除

### 常见问题

**Q: 安装失败，提示权限不足**
A: 确保设备已root，或使用平台签名的APK

**Q: 广播无响应**
A: 检查应用是否正确安装为系统应用

**Q: 任务删除失败**
A: 确认系统权限已正确授予

**Q: 缩略图显示异常**
A: 检查PACKAGE_USAGE_STATS权限状态

### 日志分析
```bash
# 权限相关错误
adb logcat | grep "permission denied"

# 任务管理错误
adb logcat | grep "TaskManager.*failed"

# 广播接收错误
adb logcat | grep "BroadcastReceiver"
```

## 贡献指南

1. Fork项目仓库
2. 创建功能分支
3. 提交代码变更
4. 创建Pull Request

## 许可证

本项目基于Apache 2.0许可证开源。

## 联系方式

- 项目地址: [GitHub Repository]
- 问题反馈: [Issues]
- 技术讨论: [Discussions]

---

**注意**: 本项目为系统级应用，需要相应的权限和签名才能正常运行。请确保在合适的环境中使用。