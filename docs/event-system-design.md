# 基于注解的事件系统设计文档

## 一、概述

本文档描述了为 Radish Pixel Dungeon 设计的基于注解的事件系统。该系统允许：
- 在任何类中使用静态方法订阅事件
- 在任何地方发布事件并传递参数
- 按优先级自动执行所有订阅方法

---

## 二、整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      事件系统架构                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  @EventSubscriber 注解层                                    │
│    └─ 标记订阅方法（事件类型、优先级）                        │
│                                                             │
│  Event 事件层                                               │
│    ├─ GameEvent (基类)                                      │
│    └─ 具体事件类 (HeroMoveEvent, CombatEvent, etc.)         │
│                                                             │
│  EventManager 事件总线层                                    │
│    ├─ 订阅者注册/注销                                        │
│    ├─ 事件发布与分发                                         │
│    └─ 优先级排序执行                                         │
│                                                             │
│  自动注册层                                                  │
│    └─ 扫描类路径，发现注解，自动注册                         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 三、核心组件设计

### 3.1 注解定义 (`@EventSubscriber`)

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventSubscriber {
    Class<? extends GameEvent> value();  // 订阅的事件类型
    int priority() default 0;             // 优先级，数字越大越先执行
}
```

**属性说明：**
- `value()` - 指定订阅的事件类型
- `priority()` - 执行优先级，默认值为 0，数字越大优先级越高

### 3.2 事件基类 (`GameEvent`)

```java
public abstract class GameEvent {
    private boolean cancelled = false;
    
    public void cancel() { 
        cancelled = true; 
    }
    
    public boolean isCancelled() { 
        return cancelled; 
    }
}
```

**特性：**
- 支持事件取消机制
- 高优先级订阅者可取消事件，阻止低优先级订阅者执行

### 3.3 事件总线 (`EventManager`)

**核心功能：**

| 方法 | 说明 |
|------|------|
| `register(Class<?> clazz)` | 扫描类中的注解方法并注册 |
| `unregister(Class<?> clazz)` | 注销某类的所有订阅 |
| `emit(GameEvent event)` | 发布事件，触发所有订阅方法 |
| `clear()` | 清理所有订阅关系 |

**内部结构：**
```java
// 存储订阅关系
Map<Class<? extends GameEvent>, List<Subscriber>> subscribers;

// Subscriber 封装
class Subscriber {
    Method method;           // 订阅方法
    int priority;            // 优先级
    Class<?> ownerClass;     // 所属类
}
```

### 3.4 注册机制

**方案 A：手动注册入口**（推荐，简单可靠）
```java
// 游戏初始化时
EventManager.register(AchievementManager.class);
EventManager.register(QuestManager.class);
```

**方案 B：类路径扫描**（自动化，需额外依赖）
- 使用 Reflections 库扫描所有含 `@EventSubscriber` 的类
- 启动时自动注册所有订阅者

---

## 四、文件结构规划

```
core/src/main/java/com/radish/pixeldungeon/events/
├── GameEvent.java              # 事件基类
├── EventManager.java           # 事件总线核心
├── EventSubscriber.java        # 订阅注解
├── Subscriber.java             # 内部类，封装订阅方法信息
└── events/                     # 具体事件类
    ├── HeroMoveEvent.java
    ├── CombatStartEvent.java
    ├── CombatEndEvent.java
    ├── ItemPickupEvent.java
    ├── ItemUseEvent.java
    ├── LevelChangeEvent.java
    ├── HeroDeathEvent.java
    └── BossSpawnEvent.java
```

---

## 五、使用示例

### 5.1 定义事件

```java
public class HeroMoveEvent extends GameEvent {
    private final Hero hero;
    private final int fromCell;
    private final int toCell;
    
    public HeroMoveEvent(Hero hero, int fromCell, int toCell) {
        this.hero = hero;
        this.fromCell = fromCell;
        this.toCell = toCell;
    }
    
    public Hero getHero() { return hero; }
    public int getFromCell() { return fromCell; }
    public int getToCell() { return toCell; }
}
```

### 5.2 订阅事件

```java
public class AchievementManager {
    
    @EventSubscriber(HeroMoveEvent.class, priority = 10)
    public static void onHeroMove(HeroMoveEvent event) {
        // 统计移动步数
        steps++;
        if (steps == 1000) {
            unlockAchievement("MARATHON_RUNNER");
        }
    }
    
    @EventSubscriber(CombatStartEvent.class, priority = 5)
    public static void onCombatStart(CombatStartEvent event) {
        // 记录战斗次数
        combatCount++;
    }
}
```

### 5.3 发布事件

```java
// 在 Hero 移动逻辑中
public boolean move(int cell) {
    int oldPos = pos;
    if (super.move(cell)) {
        // 移动成功，发布事件
        EventManager.emit(new HeroMoveEvent(this, oldPos, cell));
        return true;
    }
    return false;
}
```

### 5.4 事件取消示例

```java
public class GameRules {
    
    // 高优先级检查，可以取消事件
    @EventSubscriber(HeroMoveEvent.class, priority = 100)
    public static void validateMove(HeroMoveEvent event) {
        if (event.getHero().isParalyzed()) {
            event.cancel();  // 取消移动
        }
    }
    
    // 低优先级执行，需要检查是否被取消
    @EventSubscriber(HeroMoveEvent.class, priority = 0)
    public static void processMove(HeroMoveEvent event) {
        if (event.isCancelled()) {
            return;  // 事件已取消，不执行
        }
        // 正常处理移动逻辑
    }
}
```

---

## 六、优先级机制

### 6.1 优先级规则

| 优先级范围 | 说明 | 典型用途 |
|-----------|------|---------|
| `80 ~ 100` | 最高优先级 | 系统级检查、事件拦截 |
| `50 ~ 79` | 高优先级 | 核心逻辑、游戏规则 |
| `1 ~ 49` | 中等优先级 | 功能模块、任务系统 |
| `0` | 默认优先级 | 普通订阅者 |
| `-49 ~ -1` | 低优先级 | 日志、统计 |
| `-100 ~ -50` | 最低优先级 | 清理、收尾工作 |

### 6.2 执行顺序

```
 emit(event)
    ↓
 获取所有订阅者
    ↓
 按优先级降序排序
    ↓
 依次执行订阅方法
    ↓
 如遇 cancel()，停止执行
```

---

## 七、集成点

### 7.1 游戏初始化

**位置：** `ShatteredPixelDungeon.java` 或 `Dungeon.java`

```java
@Override
public void onCreate() {
    super.onCreate();
    
    // 初始化事件系统
    EventManager.init();
    
    // 注册核心事件订阅类
    EventManager.register(AchievementManager.class);
    EventManager.register(QuestManager.class);
    EventManager.register(StatTracker.class);
    
    // ... 其他初始化
}
```

### 7.2 游戏清理

```java
@Override
public void onPause() {
    // 清理订阅关系（可选）
    EventManager.clear();
}
```

---

## 八、扩展性考虑

### 8.1 未来扩展

| 功能 | 说明 |
|------|------|
| 异步事件 | 支持在后台线程执行订阅方法 |
| 事件过滤 | 支持条件订阅（如只订阅特定英雄的事件） |
| 监听器接口 | 作为注解的替代方案，支持动态订阅 |
| 事件链 | 支持事件处理后返回结果 |
| 性能分析 | 统计各订阅方法的执行时间 |

### 8.2 性能优化

- 使用 `ConcurrentHashMap` 支持并发访问
- 缓存已排序的订阅者列表
- 避免重复扫描同一类

---

## 九、实施计划与时间估算

### 9.1 阶段划分

| 阶段 | 任务 | 预计时间 |
|------|------|---------|
| **阶段 1** | 核心框架实现 | 1-2 小时 |
| **阶段 2** | 示例事件类创建 | 1 小时 |
| **阶段 3** | 集成与测试 | 1-2 小时 |
| **阶段 4** | 文档与优化 | 0.5-1 小时 |

### 9.2 详细时间估算

| 序号 | 任务 | 详细说明 | 预计时间 |
|------|------|---------|---------|
| 1 | 创建 `@EventSubscriber` 注解 | 定义注解及属性 | 15 分钟 |
| 2 | 创建 `GameEvent` 基类 | 实现取消机制 | 15 分钟 |
| 3 | 创建 `Subscriber` 内部类 | 封装方法引用和优先级 | 30 分钟 |
| 4 | 实现 `EventManager` 核心 | 注册、注销、发布逻辑 | 1 小时 |
| 5 | 创建示例事件类 | 3-5 个常用事件 | 1 小时 |
| 6 | 集成到游戏初始化 | 修改 `ShatteredPixelDungeon` 或 `Dungeon` | 30 分钟 |
| 7 | 编写测试代码 | 验证基本功能 | 1 小时 |
| 8 | 编写使用文档 | 示例和说明 | 30 分钟 |
| **总计** | | | **4.5-5.5 小时** |

### 9.3 风险与缓冲

| 风险 | 影响 | 缓冲时间 |
|------|------|---------|
| 反射性能问题 | 可能需要优化缓存策略 | +30 分钟 |
| 与现有代码冲突 | 需要调整集成方式 | +30 分钟 |
| 多线程问题 | 需要添加同步机制 | +30 分钟 |

**建议总时间预算：6 小时**

---

## 十、验收标准

- [ ] 注解可以正确标记订阅方法
- [ ] 事件可以正确发布到所有订阅者
- [ ] 优先级机制正常工作
- [ ] 事件取消机制正常工作
- [ ] 在多线程环境下安全运行
- [ ] 性能无明显下降
- [ ] 有完整的使用文档和示例

---

## 附录：参考设计模式

- **观察者模式 (Observer Pattern)** - 核心设计模式
- **发布 - 订阅模式 (Pub-Sub Pattern)** - 事件分发机制
- **单例模式 (Singleton Pattern)** - EventManager 实例

---

*文档版本：1.0*  
*创建日期：2026 年 3 月 24 日*  
*项目：Radish Pixel Dungeon*
