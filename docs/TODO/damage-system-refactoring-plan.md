# DamageType 系统后续重构计划

## 一、状态与目的

本计划记录 DamageType 系统未来的重构方向。目前暂停实施，以便优先处理其他更重要的系统。

当前系统已经具备：

- `DamageType` 伤害类型枚举。
- `DamageInfo` 伤害上下文和四阶段 modifier 计算。
- `DamageModifier` 修正项。
- `DamageResistance` 类型抗性容器。
- `Char.damage(DamageInfo)` 兼容入口。
- 部分环境伤害和 DoT 已迁移到 `DamageInfo`。

当前真正控制游戏行为的仍主要是 `damage(int, Object)` 和来源类判断。普通攻击、护甲、抗性、护盾、浮字和死亡处理尚未统一进入新管线。

本计划与 `docs/TODO/char-java-migration-plan.md` 有重叠。未来实施时应由 DamagePipeline 定义伤害阶段，由 Char 事件系统负责扩展点，禁止两套系统分别重复修改同一伤害数值。

## 二、已经确定、不再讨论的设计决定

### 2.1 保留当前 DamageType 模型

- 不新增 `DamageProperty` 或 properties 集合。
- 类型自身继续携带 `isMagical()`、`ignoresArmor()` 等属性。
- 流血、坠落、燃烧、饥饿等继续作为独立 `DamageType`。
- 不拆分 `DamageCause`。
- `DamageType.fromSource()` 和 `DamageSource` 暂时保留，作为旧调用迁移工具；后续由维护者逐步手动删除。

### 2.2 TRUE 的规则

`DamageType.TRUE`：

- 绕过护甲。
- 绕过 DamageType 属性抗性和免疫。
- 绕过护盾。
- 不绕过角色无敌。
- 不绕过剧情保护、死亡保护和其他必须执行的生命规则。

TRUE 不通过“100%穿透抗性”实现。它是一种独立类型，普通生物不提供 TRUE 抗性；DamagePipeline 在护甲、属性抗性和护盾阶段直接跳过对应处理。

### 2.3 source 约束

- `DamageInfo.source` 必须为 `@NonNull`。
- `attacker` 和 `sourceItem` 可以为空。
- 新工厂方法必须显式接收来源。
- 旧调用迁移期间，由 `fromSource()` 包装非空来源。

## 三、阶段一：稳定 DamageInfo 和 DamageModifier

### 3.1 删除可变计算缓存

删除 `DamageInfo` 中：

```java
cachedFinalDamage
calculated
invalidateCache()
recalculate()
```

`getDamage()` 每次直接计算。一次伤害通常只有少量 modifier，正确性优先于没有实际收益的缓存。

原因：当前 `DamageModifier.setActive()` 不会通知 `DamageInfo`，容易返回过期缓存。

### 3.2 令 DamageModifier 不可变

- 删除 `active` 和 `setActive()`。
- 条件 modifier 只在条件成立时添加。
- `DamageInfo.copy()` 可以安全共享不可变 modifier；如果以后 modifier 增加可变字段，则必须深拷贝。

### 3.3 简化暴击

暴击继续作为 `DamageInfo` 属性，而不是伤害类型。

不要把暴击作为来源文本为“暴击”的普通 modifier 插入列表。在计算阶段显式处理：

```java
if (critical) {
    result *= criticalMultiplier;
}
```

这样可以保证：

- 重复调用 `setCritical(true)` 不会重复添加倍率。
- 修改 `criticalMultiplier` 后立即生效。
- 不依赖本地化文本删除 modifier。

### 3.4 source 非空

构造函数、工厂方法和 `setSource()` 使用项目可用的 `@NonNull` 注解，并在必要处加入运行时校验。

调整无来源工厂方法：

```java
trueDamage(int damage, Object source)
hunger(int damage, Object source)
fall(int damage, Object source)
chasm(int damage, Object source)
```

删除或弃用会生成空 source 的重载。

## 四、阶段二：明确 modifier 阶段

将 modifier 类型重命名为更容易理解的名称：

```java
BASE_ADDITIVE
BASE_MULTIPLICATIVE
FINAL_MULTIPLICATIVE
FINAL_ADDITIVE
```

计算顺序：

```text
基础伤害
→ BASE_ADDITIVE
→ BASE_MULTIPLICATIVE
→ 暴击倍率
→ FINAL_MULTIPLICATIVE
→ FINAL_ADDITIVE
→ 舍入并限制为不小于 0
```

统一使用 `Math.round()`。同步修正文档中写成 `floor` 的公式和边界示例。

事件系统接入时，每个事件必须明确允许添加哪一阶段的 modifier，避免同一效果在事件和管线中重复应用。

## 五、阶段三：新增 DamageResult

新增不可变结果对象，记录管线各阶段的结果：

```java
public final class DamageResult {
    public final int baseDamage;
    public final int modifiedDamage;
    public final int armorBlocked;
    public final int resistanceBlocked;
    public final int shieldBlocked;
    public final int hpDamage;
    public final boolean immune;
}
```

需要保证：

```text
modifiedDamage
- armorBlocked
- resistanceBlocked
- shieldBlocked
= hpDamage
```

如游戏规则存在额外生命层或重定向伤害，应增加有明确含义的字段，不使用当前含义不清的 `dmg + shielded`。

DamageResult 用于：

- 浮动伤害数字。
- 护盾反馈。
- 战斗事件。
- 统计和成就。
- 调试跟踪。
- 死亡处理。

## 六、阶段四：统一 DamagePipeline

新增唯一权威执行入口：

```java
DamageResult DamagePipeline.apply(Char target, DamageInfo info)
```

建议执行顺序：

```text
1. 校验 target、source 和 DamageInfo
2. 计算 DamageInfo modifier
3. 应用攻击方伤害效果
4. 应用目标承伤倍率
5. 应用护甲
6. 应用 DamageType 抗性或免疫
7. 应用护盾
8. 应用 Vitae 等额外生命层
9. 扣除 HP
10. 发布受伤事件并处理死亡
11. 生成 UI 和 DamageResult
```

步骤 3 和步骤 4 应与 Char 事件系统协作：

- DamagePipeline 决定顺序和不变量。
- 事件订阅者只提供 modifier 或明确的阶段结果。
- `Char.java` 不再硬编码每件装备、天赋或 Buff。

### TRUE 的分支

对于 `DamageType.TRUE`：

- 步骤 5 的 `armorBlocked = 0`。
- 步骤 6 的 `resistanceBlocked = 0`，不查询类型抗性。
- 步骤 7 的 `shieldBlocked = 0`，不消耗护盾。
- 仍然执行无敌、剧情保护、死亡保护、HP 和死亡逻辑。

## 七、阶段五：属性抗性接口

在 `Char` 提供按 DamageType 聚合的接口：

```java
public float resistanceTo(DamageType type);
public boolean isImmuneTo(DamageType type);
```

内部聚合可能包括：

- 角色或怪物固有抗性。
- Buff。
- 装备。
- 天赋。
- 关卡和挑战效果。

`DamageResistance` 可作为聚合容器或基础配置容器，但不应要求调用者手工同步角色全部状态。

抗性数值规则需要在实施前最终确认。当前建议：

- `0` 表示无减免。
- `0.5` 表示减免 50%。
- `1` 表示完全免疫。
- 结果限制在 `0..1`。
- 多来源如何叠加必须只有一种规则，并由测试固定。

TRUE 不调用这两个接口。

## 八、阶段六：反转兼容方向

最终权威入口：

```java
public DamageResult damage(DamageInfo info)
```

旧接口只作为迁移适配器：

```java
public void damage(int damage, Object source) {
    damage(DamageInfo.fromSource(damage, source));
}
```

当前方向恰好相反：`damage(DamageInfo)` 会降级调用旧接口。实施本阶段时必须反转，确保 DamageType、attacker、sourceItem、source、暴击和 modifier 不再丢失。

迁移完成后才能删除：

- `DamageSource`。
- `DamageType.fromSource()`。
- `NO_ARMOR_PHYSICAL_SOURCES`。
- `Char.damage()` 中按来源类选择浮字的长判断列表。
- `CritClass` 和 `NoArmorCritClass` 兼容标记。

## 九、阶段七：迁移主战斗路径

普通攻击应构造显式 DamageInfo：

```java
DamageInfo info = DamageInfo.physical(damage, attacker, weapon);
info.setCritical(critical, criticalMultiplier);
target.damage(info);
```

逐步迁移：

1. `Char.attack()` 普通攻击。
2. 武器和附魔。
3. 法杖和法术。
4. 怪物技能。
5. 陷阱和 Blob。
6. 剩余 Buff 和 DoT。
7. 特殊死亡和剧情伤害。

迁移 `attackProc()` 和 `defenseProc()` 时，先定义它们属于哪个管线阶段。不要让同一个效果同时修改旧的 `int damage` 和新的 modifier。

新代码必须显式指定 DamageType。`fromSource()` 只允许服务未迁移的旧接口。

## 十、测试计划

测试应优先采用纯计算测试，避免依赖完整场景和渲染环境。

### 10.1 DamageInfoTest

- 无 modifier 时返回基础伤害。
- 四个 modifier 阶段顺序正确。
- 同阶段多个加算正确累加。
- 多个乘算正确累乘。
- 伤害结果不会小于零。
- `Math.round()` 边界明确：`22.4`、`22.5`、`22.6`。
- 重复启用暴击不会重复应用倍率。
- 关闭暴击会移除暴击效果。
- 修改暴击倍率立即生效。
- `copy()` 后修改副本不污染原对象。
- `withBaseDamage()` 保留其他上下文。
- `source == null` 被拒绝。

### 10.2 DamageTypeTest

- 所有 `id` 唯一且非空。
- 每个类型具有合法浮字图标。
- `isMagical()` 与当前规则一致。
- `ignoresArmor()` 与当前规则一致。
- `isElemental()` 分类正确。
- `isDoT()` 分类正确。
- `TRUE.isTrueDamage()` 为真且其他类型为假。
- 流血、坠落、燃烧等继续保持独立类型。

### 10.3 DamageResistanceTest

- 无抗性时伤害不变。
- 50% 抗性正确减伤。
- 同类型免疫产生零伤害。
- 不同类型互不影响。
- 抗性值限制在 `0..1`。
- 累加规则正确。
- 合并规则正确。
- TRUE 不进入普通抗性计算。
- 免疫的减免百分比语义正确，不能返回 0%。

### 10.4 DamagePipelineTest

- 普通物理伤害依次经过护甲、抗性和护盾。
- `PHYSICAL_NO_ARMOR` 绕过护甲，但仍经过抗性和护盾。
- 魔法和元素伤害使用对应 DamageType 抗性。
- DoT 使用自身 DamageType 抗性。
- TRUE 绕过护甲。
- TRUE 绕过属性抗性和类型免疫。
- TRUE 不消耗护盾。
- TRUE 仍受无敌保护。
- 剧情保护和死亡保护仍对 TRUE 生效。
- 免疫在护盾前将伤害降为零。
- 护盾只吸收允许吸收的伤害。
- `DamageResult` 各字段与实际 HP 变化一致。
- 零伤害不触发错误的死亡逻辑。
- 致死伤害触发一次死亡流程。
- LifeLink、延迟伤害和 Vitae 的阶段行为由测试固定。

### 10.5 兼容迁移测试

- `damage(int, source)` 与显式等价的 DamageInfo 在迁移期产生相同结果。
- `fromSource()` 对尚未迁移的关键来源保持旧行为。
- 已迁移调用不再经过类名推断。
- 浮字图标由 DamageInfo 决定，不依赖旧 `instanceof` 列表。
- 死亡来源和成就判断没有因包装 source 而改变。

## 十一、实施顺序

未来恢复本计划时，按以下顺序执行：

```text
1. 删除缓存，修复 source 和暴击模型
2. 建立 DamageInfoTest 与 DamageTypeTest
3. 重命名 modifier 阶段并同步设计文档
4. 新增 DamageResult
5. 实现 DamagePipeline 和 DamagePipelineTest
6. 接入 DamageType 抗性
7. 反转 damage() 兼容方向
8. 迁移普通攻击
9. 迁移武器、法杖、怪物技能和剩余调用
10. 删除 fromSource 和旧来源类规则
```

每个阶段都应保持行为可验证，不要在一次改动中同时迁移全部调用点。

## 十二、完成标准

只有满足以下条件，DamageType 系统才视为完成：

- 所有伤害最终进入 `DamagePipeline`。
- `DamageInfo` 是唯一权威输入。
- `DamageResult` 是 UI、统计和事件的权威结果。
- 护甲、DamageType 抗性、护盾和 HP 的顺序只有一处实现。
- TRUE 的绕过规则有自动化测试。
- 普通攻击和特殊伤害不再依赖来源类猜测核心规则。
- `damage(int, Object)` 已删除，或只保留为明确标记的临时兼容层。
- 文档公式、代码舍入规则和测试完全一致。
