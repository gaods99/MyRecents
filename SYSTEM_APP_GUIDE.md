# MyRecents 系统应用配置指南

## 概述

MyRecents现在配置为系统应用，具有以下特性：
- 使用 `android.uid.system` 共享用户ID
- 拥有系统级权限
- 可以直接访问系统API
- 需要平台签名或root权限安装

## 系统权限

### 已声明的系统权限
```xml
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
<uses-permission android:name="android.permission.GET_TASKS" />
<uses-permission android:name="android.permission.REORDER_TASKS" />
<uses-permission android:name="android.permission.REMOVE_TASKS" />
<uses-permission android:name="android.permission.MANAGE_ACTIVITY_STACKS" />
<uses-permission android:name="android.permission.INTERNAL_SYSTEM_WINDOW" />
<uses-permission android:name="android.permission.STATUS_BAR_SERVICE" />
```

### 权限功能说明
- **PACKAGE_USAGE_STATS**: 获取应用使用统计
- **GET_TASKS**: 获取运行中的任务列表
- **REORDER_TASKS**: 重新排序任务
- **REMOVE_TASKS**: 删除任务
- **MANAGE_ACTIVITY_STACKS**: 管理Activity堆栈
- **INTERNAL_SYSTEM_WINDOW**: 创建系统内部窗口
- **STATUS_BAR_SERVICE**: 状态栏服务权限

## 构建系统应用

### 1. 构建APK
```bash
chmod +x build_system_app.sh
./build_system_app.sh
```

### 2. 检查构建结果
构建成功后会生成：
- `app/build/outputs/apk/debug/app-debug.apk`
- 包含系统权限声明
- 使用android.uid.system

## 安装系统应用

### 方法1: 自动安装脚本
```bash
chmod +x install_system_app.sh
./install_system_app.sh
```

### 方法2: 手动安装到系统分区
```bash
# 需要root权限
adb root
adb remount
adb shell mkdir -p /system/app/MyRecents/
adb push app/build/outputs/apk/debug/app-debug.apk /system/app/MyRecents/MyRecents.apk
adb shell chmod 644 /system/app/MyRecents/MyRecents.apk
adb reboot
```

### 方法3: 平台签名安装
```bash
# 使用平台密钥签名
java -jar signapk.jar platform.x509.pem platform.pk8 app-debug.apk app-signed.apk
adb install -r app-signed.apk
```

## 系统应用优势

### 1. 更强的任务管理能力
- 直接调用ActivityManager.removeTask()
- 访问更多任务信息
- 更可靠的任务删除

### 2. 更好的系统集成
- 可以接收系统级广播
- 与SystemUI更好的集成
- 更稳定的运行环境

### 3. 更多的API访问权限
- 访问隐藏API
- 系统级服务调用
- 更详细的任务信息

## 测试系统应用

### 1. 验证安装
```bash
# 检查应用是否安装
adb shell pm list packages | grep com.newland.recents

# 检查系统权限
adb shell dumpsys package com.newland.recents | grep permission
```

### 2. 测试功能
```bash
# 显示最近任务
adb shell am broadcast -a com.android.systemui.recents.ACTION_SHOW

# 隐藏最近任务
adb shell am broadcast -a com.android.systemui.recents.ACTION_HIDE

# 切换显示状态
adb shell am broadcast -a com.android.systemui.recents.ACTION_TOGGLE
```

### 3. 查看日志
```bash
# 查看应用日志
adb logcat | grep -E "(RecentsActivity|TaskManager|TaskLoader)"

# 查看系统权限相关日志
adb logcat | grep -E "(permission|security)"
```

## 注意事项

### 1. 安装要求
- **Root权限**: 安装到系统分区需要root
- **平台签名**: 标准安装需要平台密钥签名
- **系统兼容性**: 确保与目标Android版本兼容

### 2. 开发调试
- 系统应用更新需要重启设备
- 调试时可能需要重新安装
- 权限问题需要检查系统日志

### 3. 部署考虑
- 生产环境需要正确的平台签名
- 系统分区安装需要设备root
- 考虑OTA升级的影响

## 故障排除

### 1. 安装失败
```bash
# 检查设备root状态
adb root

# 检查系统分区挂载
adb shell mount | grep system

# 清理旧版本
adb uninstall com.newland.recents
```

### 2. 权限问题
```bash
# 检查SELinux状态
adb shell getenforce

# 查看权限拒绝日志
adb logcat | grep "permission denied"

# 检查应用权限状态
adb shell dumpsys package com.newland.recents | grep permission
```

### 3. 功能异常
```bash
# 检查广播接收
adb logcat | grep BroadcastReceiver

# 检查任务管理器
adb logcat | grep TaskManager

# 检查Activity启动
adb logcat | grep RecentsActivity
```

## 系统集成

作为系统应用，MyRecents可以：
1. 替代原生的最近任务界面
2. 与SystemUI深度集成
3. 提供定制化的任务管理体验
4. 支持厂商特定的功能扩展

这使得MyRecents成为一个真正的系统级最近任务管理器，具备完整的功能和可靠的性能。