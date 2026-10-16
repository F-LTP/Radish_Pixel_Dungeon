# 不消耗时间播放动画机制（Animation Without Spending Time）

> 状态：**已实现**。

## 概述

有时需要播放一段英雄动画（如变身、特殊演出），但**不消耗回合、不推进时间**——动画播放期间英雄被阻塞（不能移动/攻击/操作），动画结束立即恢复，且时间戳不变。

本机制封装为 `Hero` 上的通用 API，任何功能都可复用。

## 核心概念

| 概念 | 说明 |
|------|------|
| 消耗时间 | 调 `hero.spend(x)` / `hero.spendAndNext(x)`，把英雄下次轮到的时刻往后推 |
| 阻塞 | `Hero.act()` 返回 `false`（本回合不行动） |
| 不耗时阻塞动画 | 播放动画期间让 `act()` 返回 false 且**不 spend**，动画结束 `ready()` 恢复 |

> 注意：`hero.busy()` 只设 `ready=false`，**本身不阻止行动**——本项目 `Hero.act()` 在 `!ready` 时仍会执行 `ready()` 或 `curAction`。因此必须配合专门的拦截标志，见下。

## 实现位置

- `Hero.animationBusy`（`Hero.java`）—— "正在播放不耗时阻塞动画"标志。
- `Hero.playAnimationNoTime(Callback startAnim)`（`Hero.java`）—— 开始：置 `ready=false`、`animationBusy=true`，然后调用 `startAnim.call()` 播放动画。
- `Hero.finishAnimationNoTime()`（`Hero.java`）—— 结束：置 `animationBusy=false`、`ready()`。
- `Hero.act()`（`Hero.java`）—— 在 `if (!ready)` / `paralysed` 判断之前新增拦截：

  ```java
  if (animationBusy) {
      return false;   // 不行动、不 spend → 不消耗时间
  }
  ```

## 用法

动画播放完成后，**必须在动画回调里调用 `finishAnimationNoTime()`** 恢复英雄：

```java
hero.playAnimationNoTime( () -> {
    sprite.playXxx( () -> hero.finishAnimationNoTime() );   // 动画完成回调里恢复
});
```

多段动画时，把 `finishAnimationNoTime()` 放到**最后一段**动画的回调里：

```java
hero.playAnimationNoTime( () -> {
    sprite.playA( () -> {
        doSomething();                      // 两段动画之间的逻辑
        sprite.playB( () -> hero.finishAnimationNoTime() );   // 结束才恢复
    });
});
```

## 示例：杂散变身（`JumbleChangeBuff.startChange`）

变身 = 消失 → (天赋/装备变换) → 出现，全程阻塞，结束恢复：

```java
hero.playAnimationNoTime( () -> {
    sprite.playChange( () -> {
        doTalentMetamorph(hero);
        doEquipmentTransmute(hero);
        sprite.playAppear( Random.Int(6), () -> finishChange() );  // finishChange 内调 hero.finishAnimationNoTime()
    });
});
```

`finishChange()` 内调用 `hero.finishAnimationNoTime()` 收尾。

## 设计约定与坑

- **动画回调里必须 `finishAnimationNoTime()`**，否则 `animationBusy` 永真，英雄永久卡死。
- **中间逻辑要用 try/catch 保护**：回调链里若抛异常，后续回调（含 `finishAnimationNoTime`）不会执行，同样会卡死。参考杂散把 `doTalentMetamorph`/`doEquipmentTransmute` 包在 try/catch 中。
- **不要调用 `spend`**：`act()` 拦截分支 `return false` 且不 spend，时间戳不变，动画结束同一回合还能继续操作。
- **与 `spendAndNext` 冲突**：若动画回调里误调 `doEquip/doUnequip` 等会 `spendAndNext` 的方法，会推进回合、破坏"不耗时"语义并可能打断动画。此类逻辑应放在动画播放前，或改用不 spend 的方式。
- `animationBusy` 是 `Hero` 的公开字段，多个功能可共用；同时只应有一段"不耗时阻塞动画"进行中。
