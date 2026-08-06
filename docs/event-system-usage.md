# 事件系统使用指南

## 概述

事件系统提供了一种解耦的游戏内通信机制。通过事件订阅和发布，不同模块可以在不直接依赖彼此的情况下进行交互。

## 工作原理

事件系统基于**发布-订阅**模式，采用编译时注解处理技术：

### 编译时：生成索引

1. 编译器扫描所有带有 `@SubscribeEvent` 注解的方法
2. 收集订阅者信息：类名、方法名、事件类型、优先级
3. 自动生成 `EventSubscriberIndex` 类，包含所有订阅者的注册代码

### 运行时：自动注册

1. 游戏启动时，`EventManager.init()` 加载生成的 `EventSubscriberIndex`
2. 索引类调用 `EventManager.registerSubscriber()` 注册所有订阅方法
3. 订阅信息按事件类型分组存储，按优先级排序

### 事件分发

当调用 `EventManager.emit(event)` 时：
1. 根据事件类型查找对应的订阅者列表
2. 按优先级从高到低依次调用订阅方法
3. 若某订阅者调用 `event.cancel()`，停止后续执行

## 自动初始化

事件系统在游戏启动时自动初始化：

- **创建新游戏** → `Dungeon.init()` 中自动初始化
- **加载存档** → `Dungeon.loadGame()` 中自动初始化


**如果还有其他的方法开始游戏而没有初始化事件管理器，你应该将这个bug反馈给TheCatist。**


无需手动调用初始化方法。

## 创建事件

所有事件必须继承 `GameEvent`：

```java
package com.shatteredpixel.shatteredpixeldungeon.events;

public class HeroLevelUpEvent extends GameEvent {
    private final Hero hero;
    private final int previousLevel;
    private final int newLevel;

    public HeroLevelUpEvent(Hero hero, int previousLevel, int newLevel) {
        this.hero = hero;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
    }

    public Hero getHero() { return hero; }
    public int getPreviousLevel() { return previousLevel; }
    public int getNewLevel() { return newLevel; }
}
```

## 订阅事件

使用 `@SubscribeEvent` 注解标记订阅方法：

```java
@SubscribeEvent(event = HeroLevelUpEvent.class, priority = 0)
public static void onHeroLevelUp(HeroLevelUpEvent event) {
    Hero hero = event.getHero();
    // 处理逻辑...
}
```

### 订阅方法要求

| 要求 | 说明 |
|------|------|
| 静态方法 | 必须是 `static` 方法 |
| 单一参数 | 方法参数必须是事件类型 |
| 参数匹配 | 参数类型必须与注解中的 `event` 类型一致 |

### 优先级

`priority` 参数控制执行顺序：

- **数值越大，优先级越高**
- 高优先级订阅者先执行
- 高优先级订阅者可以调用 `event.cancel()` 取消事件，阻止后续订阅者执行

```java
// 高优先级 - 先执行
@SubscribeEvent(event = HeroLevelUpEvent.class, priority = 100)
public static void highPriorityHandler(HeroLevelUpEvent event) {
    // 可以取消事件，阻止后续订阅者
    event.cancel();
}

// 低优先级 - 后执行（如果事件未被取消）
@SubscribeEvent(event = HeroLevelUpEvent.class, priority = 0)
public static void normalHandler(HeroLevelUpEvent event) {
    if (event.isCancelled()) return;
    // 处理逻辑...
}
```

## 发布事件

使用 `EventManager.emit()` 发布事件：

```java
// 在英雄升级时发布事件
EventManager.emit(new HeroLevelUpEvent(hero, oldLevel, newLevel));
```

所有订阅该事件的方法会按优先级顺序自动执行。

## 取消事件

订阅者可以取消事件，阻止后续低优先级订阅者执行：

```java
@SubscribeEvent(event = HeroDeathEvent.class, priority = 100)
public static void preventDeath(HeroDeathEvent event) {
    if (hasResurrectionItem(event.getHero())) {
        event.cancel(); // 取消死亡事件
        // 执行复活逻辑...
    }
}

```
对于某些事件，例如英雄死亡，发布事件之后，可能需要检测事件执行完成之后是否被取消，以确定是否需要继续执行后续逻辑。
```java
 HeroDeathEvent event = new HeroDeathEvent(hero, killer, cause);
EventManager.emit(event);

if (event.isCancelled()) {
    // 有订阅者取消了死亡事件，执行复活逻辑
} else {
    // 事件未被取消，继续死亡流程
}
```
## 最佳实践

1. **事件命名**：使用 `XxxEvent` 格式，如 `HeroMoveEvent`、`ItemPickupEvent`
2. **继承**：事件都继承 `GameEvent`
3. **优先级规划**：预留优先级空间，便于后续扩展
4. **避免循环**：不要在订阅方法中发布同类事件，防止无限循环

## 已有事件
hook太麻烦了，你自己找地方emit去吧。

我这边有皮甲的示例，可以在commit日志里看，下个版本hook了更有意义的东西后我会删掉它的。

---

*文档版本：1.0*
*创建日期：2026 年 3 月 31 日*
*创建者：TheCatist（当然还有AI）*