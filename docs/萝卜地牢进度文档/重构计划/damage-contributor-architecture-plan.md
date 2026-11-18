# DamageContributor 架构（待办·未开始）

> 状态：**未开始**。仅记录设计方向，不要在本列表完成前启动实施。

## 一、目标

把 `Char.applyDamage()` 里硬编码的乘算/加算（承伤倍率、各类加值）**分散到各自的来源类**（Buff、武器、护甲、神器、法杖等），而不是堆在一个大方法里。每个来源实现统一贡献者契约，管线在伤害结算时统一收集「攻击方 + 防守方」身上的所有贡献者并调用，逻辑内聚、扩展不侵入核心。

## 二、设计

统一契约：
```java
public interface DamageContributor {
    // 在伤害结算时被管线调用，按阶段注入 modifier
    void modifyDamage(Char target, DamageInfo info);
}
```

管线结算时统一收集并逐个调用：
```java
// DamagePipeline 内（伪代码）
for (DamageContributor c : collectContributors(attacker, defender))
    c.modifyDamage(target, info);
int final = info.getDamage();
```

每个贡献者用 `DamageInfo` 的乘区 API 各归各的乘区：
- `addFlatModifier(...)` → 直接加算
- `addStackMultModifier(...)` → 叠加乘区
- `addDirectMultModifier(...)` → 依次乘算
- `addFinalAddModifier(...)` → 最终加算

## 三、现状：已有一半机制

- `Buff` 已定义 4 个伤害修改钩子：
  `modifyOutgoingAttackDamage` / `modifyPreFinalOutgoingAttackDamage` / `modifyFinalOutgoingAttackDamage` / `modifyIncomingAttackDamage`。
- `OrdinaryAttackDamage.applyOutgoingModifiers/applyPreFinalModifiers/applyFinalModifiers`
  已按「攻击方 + 防守方」遍历 `buffs()` 调用这些钩子。

## 四、缺口

1. 只覆盖**攻击路径**（`OrdinaryAttackDamage.build`），DoT/陷阱/法杖不走这套，仍各自硬编码。
2. 管线 `applyDamage` 里的硬编码乘算（lunar ×2、doom ×1.67、deathmark ×1.25、champion ×n、抗性 ×…）没有走钩子。
3. 钩子只传 `DamageInfo`，没有明确「阶段」语义，与事件的扩展点边界需定义，避免同一效果重复应用。

## 五、实施建议（未开始）

1. 先把 `DamagePipeline` 改成统一收集贡献者（`collectContributors` + 按阶段注入），对齐现有 `Buff` 钩子。
2. 再逐个把 `applyDamage` 里的硬编码乘算搬进对应 Buff 类。
3. 注意**归属**：攻击方加成（modifyOutgoing）vs 防守方承伤（modifyIncoming），及 `defenseProc/attackProc` 与乘区的关系。
4. 每个效果只在一处注入，防止在事件和乘区里重复应用。

## 六、范围

- 这是一个**中等偏大、可增量**的重构，需分批编译验证。
- 不与当前 `damage-system-refactoring-plan.md` 中未完成的迁移项冲突，但**在本计划完成前不要启动本项**。
