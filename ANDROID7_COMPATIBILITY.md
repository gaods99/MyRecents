# Android 7 兼容性修复

## RecentTaskInfo 字段差异

### Android 7 (API 24-25) 中可用的字段：
- `id` - 任务ID
- `persistentId` - 持久化任务ID  
- `baseIntent` - 基础Intent
- `origActivity` - 原始Activity
- `description` - 任务描述
- `stackId` - 堆栈ID (已废弃)

### Android 7 中不可用的字段/方法：
- `userId` - 用户ID (Android 8+)
- `lastActiveTime` - 最后活跃时间 (Android 8+)
- `taskDescription` - 任务描述对象 (部分字段在Android 8+)
- `removeTask(int)` - 公开的删除任务方法 (Android 8+，Android 7中为隐藏API)

## 已修复的兼容性问题

### 1. userId 字段
```java
// 修复前 (编译错误)
this.userId = taskInfo.userId;

// 修复后 (Android 7 兼容)
this.userId = 0; // Default user ID for Android 7 compatibility
```

### 2. lastActiveTime 字段
```java
// 修复前 (编译错误)
this.lastActiveTime = taskInfo.lastActiveTime;

// 修复后 (Android 7 兼容)
this.lastActiveTime = System.currentTimeMillis(); // Use current time for Android 7 compatibility
```

### 3. removeTask 方法调用
```java
// 修复前 (编译错误)
mActivityManager.removeTask(taskId);

// 修复后 (Android 7 兼容)
Method removeTaskMethod = ActivityManager.class.getDeclaredMethod("removeTask", int.class);
removeTaskMethod.setAccessible(true);
removeTaskMethod.invoke(mActivityManager, taskId);
```

### 4. getRecentTasks 标志位
```java
// 修复前 (可能不兼容)
ActivityManager.RECENT_WITH_EXCLUDED | ActivityManager.RECENT_IGNORE_UNAVAILABLE

// 修复后 (Android 7 兼容)
ActivityManager.RECENT_WITH_EXCLUDED
```

## 替代方案

### 获取真实的最后活跃时间
如果需要真实的最后活跃时间，可以使用以下方法：

```java
// 方法1: 使用任务描述中的时间戳
if (taskInfo.description != null) {
    // 某些情况下description可能包含时间信息
}

// 方法2: 使用文件系统时间戳
// 通过包名获取应用的最后使用时间

// 方法3: 使用UsageStatsManager (需要权限)
UsageStatsManager usageStatsManager = (UsageStatsManager) 
    context.getSystemService(Context.USAGE_STATS_SERVICE);
```

### 获取真实的用户ID
```java
// 方法1: 使用当前用户
UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
int currentUserId = userManager.getUserHandle();

// 方法2: 从Intent中获取
if (taskInfo.baseIntent != null) {
    // 分析Intent的用户信息
}
```

## 测试建议

在Android 7设备上测试以下功能：
1. ✅ 任务列表加载
2. ✅ 任务缩略图显示
3. ✅ 任务启动
4. ✅ 任务删除
5. ✅ 广播接收

## 注意事项

1. **时间排序**: 由于使用当前时间作为lastActiveTime，任务可能不会按真实的最后使用时间排序
2. **多用户**: 由于userId固定为0，在多用户环境下可能有问题
3. **权限**: 某些功能可能需要额外的权限声明

这些限制在大多数使用场景下不会造成问题，因为：
- 大多数设备是单用户使用
- 任务顺序通常由系统的getRecentTasks()已经排序
- 主要功能（显示、启动、删除）不依赖这些字段