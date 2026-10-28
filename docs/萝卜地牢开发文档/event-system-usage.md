# 事件系统使用指南

> 状态：**已实现**。本文是 `event-system-design.md` 的操作篇，聚焦"如何写事件、如何订阅、如何发布"。

## 概述

事件系统提供解耦的游戏内通信。通过订阅/发布，不同模块可在不直接依赖彼此的情况下交互。订阅信息由**编译期注解处理器**自动收集，**无需手工注册**。

## 工作原理

- **编译期**：注解处理器扫描所有带 `@SubscribeEvent` 的方法，生成 `EventSubscriberIndex` 类。
- **运行时**：`EventManager.init()` 加载该索引并注册全部订阅者，按事件类型分组、按优先级排序。
- **分发**：`EventManager.emit(event)` 按优先级（高→低）调用订阅方法；订阅者调用 `event.cancel()` 可阻止后续执行。

## 自动初始化

事件系统在游戏启动时自动初始化，**无需手动调用**：

- 创建新游戏 → `Dungeon.init()` 中初始化
- 加载存档 → `Dungeon.loadGame()` 中初始化

> 若发现有其他入口启动游戏而未初始化事件管理器，说明存在遗漏，请向 TheCatist 反馈。

## 创建事件

所有事件继承 `GameEvent`，放在 `events/` 包，字段用 getter 暴露：

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

订阅方法必须是 `public static`，用 `@SubscribeEvent` 标记：

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
| 静态方法 | 必须是 `static` |
| 单一参数 | 参数类型必须与 `event` 类型一致 |
| 访问权限 | `public` |

### 优先级

`priority` 数字越大越先执行。高优先级订阅者可调用 `event.cancel()` 取消事件，阻止后续订阅者执行：

```java
@SubscribeEvent(event = HeroLevelUpEvent.class, priority = 100)
public static void highPriorityHandler(HeroLevelUpEvent event) {
    event.cancel();   // 阻止后续订阅者
}

@SubscribeEvent(event = HeroLevelUpEvent.class, priority = 0)
public static void normalHandler(HeroLevelUpEvent event) {
    if (event.isCancelled()) return;   // 检查是否被取消
    // 处理逻辑...
}
```

## 发布事件

```java
EventManager.emit(new HeroLevelUpEvent(hero, oldLevel, newLevel));
```

所有订阅该事件的方法按优先级顺序自动执行。

## 发布后检查取消

对于某些事件（如死亡），发布后可能需要检测是否被取消，以决定后续流程：

```java
HeroDeathEvent event = new HeroDeathEvent(hero, killer, cause);
EventManager.emit(event);

if (event.isCancelled()) {
    // 有订阅者取消了事件，走替代流程
} else {
    // 未取消，继续原流程
}
```

## 既有事件示例：伤害事件

伤害在 `Char.damage(int, Object, DamageType)` 收口处发布两套事件（详见设计文档）：

- **`CharUnprocedDamageEvent`**：减免计算前，携带原始伤害 `dmg`。
- **`CharFinalDamageEvent`**：减免计算后，携带实际 HP 扣减 `dealt`，`dealt>0` 才发。

订阅伤害事件时务必判空 `Dungeon.hero`、`Dungeon.level`、`heroFOV`、`target.pos` 范围。

## 最佳实践

1. **命名**：事件用 `XxxEvent` 格式，字段用 getter 暴露。
2. **继承**：事件一律继承 `GameEvent`。
3. **优先级规划**：预留优先级空间，便于扩展。
4. **避免循环**：不要在订阅方法中发布同类事件，防止无限循环。
5. **编译生效**：新增订阅者/事件后需重新编译，让注解处理器重新生成索引。
