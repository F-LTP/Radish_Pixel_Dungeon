# 生物（Mob）开发原则

> **本文是摘要版**；权威原文（作者 JDSALing，2026-11-08，237 行）见同目录 `PD-生物开发原则.md`，两者冲突时以原文为准。
> 来源：2026-11-04 用户提供的《生物开发原则》长图 + 本仓库两次同类事故复盘。
> 适用于本仓库**所有新增 / 修改的敌对生物**（`actors/mobs/**`）。`Hero.java` 不在此列。

## 1. 父类红线：一切从 `Mob.java` 出发

- 所有生物**必须**以 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Mob.java`
  为父类，并**复用它的接口**（`act()` / `attack()` / `doAttack()` / `damage()` / `die()` /
  `onAttackComplete()` / `onOperateComplete()` / `heroShouldInteract()` / 存档钩子 …）。
- **严禁**为了「实现某一个效果」直接改 `Mob.java` 来获得效果（唯一的例外是 `Hero.java` 这条另开的继承链）。
  一旦为了单个怪去改父类，等于让**整个游戏所有生物**一起承担这次改动 —— 这是历史事故最多的一类改动。
- 需要的效果**先在现有钩子里找**；找不到才在本类里覆写；确需改父类时，必须说明「为什么非改不可 + 对其它生物的影响」。

## 2. 近战敌对生物

- 直接参考 `actors/mobs/Rat.java`：它对「基础近战」已经是完整实现，通常**只改数值 + 换 Sprite** 就够。
- 除非用户明确要求，**不要**在 `Rat.java` 的基础上再生成一套臆想的多余逻辑（多写一层状态机、自己算命中/闪避、
  自己重写移动与视野等，都会绕开引擎既定的战斗流程）。

## 3. 远程敌对生物：`zap()` 只是「开始」，**回调才是「结束」**

远程攻击 = **一个完整的异步回调链**，不是「在 `act()` 里打一发然后 `return true`」。

### 3.1 正确范式（本仓库已验证）

```java
// Mob 侧：发起动画后必须 return false，把「这一回合什么时候结束」交给回调
@Override
protected boolean doAttack(Char enemy) {
    if (sprite != null && sprite.visible) {
        sprite.zap(enemy.pos);   // 只是播动画
        return false;            // ← 关键：本回合尚未结束
    }
    spend(attackDelay());
    return true;
}

// Sprite 侧：动画播完 → 通知 Mob 收尾
@Override
public void onZapComplete() {
    launchSpear();   // 真正的伤害/弹道结算
    spend(TICK);     // 或 attackDelay()，具体看武器/攻击节奏
    next();          // ← 把回合交还给 actor 线程
}
```

参考实现：

| 参考 | 位置 | 它怎么收尾 |
|---|---|---|
| `Warlock` | `actors/mobs/Warlock.java` | `sprite.zap()` + `onZapComplete()` 里结算并 `next()` |
| `DM100` | `actors/mobs/DM100.java` | `implements Callback`，`public void call() { next(); }` 交给 `MagicMissile.boltFromChar(...)` 的 callback |
| `GoblinRider` | `actors/mobs/RadishEnemy/GoblinRider.java` + `sprites/RadishEnemySprite/GoblinRiderSprite.java` | 修于 `7e5200787`（见 §3.3） |

### 3.2 引擎事实（决定「为什么必须回调」）

- `actors/Actor.java:290` `doNext = acting.act();`
- `actors/Actor.java:314` `if (!doNext) Thread.currentThread().wait();` —— **没人调 `next()` 就永远停在这里**
- `sprites/CharSprite.java:852-874` `onComplete(Animation anim)` **只分发两种动画**：
  `anim == attack → ch.onAttackComplete()`、`anim == operate → ch.onOperateComplete()`。
  **自定义 `zap` 动画不在分发列表里** ⇒ 若你只写了 `zap` 的动画、没有自己接回调，动画播完**什么都不会发生**。

### 3.3 症状与两次事故

**症状**：玩家点不动、回合不走、`hero.ready` 恒为 false；游戏内「Debug」按钮打出的 actor 转储长这样：

```
current actor: class ...actors.mobs.<某远程生物>
trace:
  java.lang.Object.wait(Native Method)
  ...actors.Actor.process(Actor.java:314)
  ...scenes.GameScene$2.run(GameScene.java:82x)
```

**本仓库已发生两次**：

1. `GoblinRider` 远程投矛后卡死 —— `GoblinRider.onZapComplete()` 因 `CharSprite.onComplete` 不认 `zap` 而永不执行。
   修复：`GoblinRiderSprite` 覆写 `onComplete`，`if (anim == zap) { idle(); onZapComplete(); }`。提交 `7e5200787`。
2. `WardenPrototype` 同类卡死（2026-11-04 用户提供的 dump）。

**自查：提交前必须能回答「谁在哪个回调里调了 `next()`」** —— 答不出来就是 §3.3 的事故。

## 4. `com.watabou.utils` 在生物里只用四个

`Callback` / `Bundlable` / `Bundle` / `Random`。其它工具类在生物上几乎用不到，**除非用户明确指定，不要引**。

## 5. 覆写 `Mob.act()` 的三条附加要求

1. **先让控制状态回到父类**：麻痹 / `Amok` / `Terror` / `Dread` 等状态下直接
   `return super.act();`，否则自定义战术会静默绕过控制效果（先例：`674d6893f`）。
2. **每个 `return false` 都要有对应的回调出口**（§3）。
3. **不要把「每 N 回合」的计时挂在 `Mob.act()` 里**：`act()` 的调用次数随速度变化（同伴追赶时会被加倍调用），
   节拍应挂在 `Buff.act()`（一游戏回合一次）。

## 6. 新增敌对生物自检清单

- [ ] 继承 `Mob`，只覆写必要钩子；**没有**改动 `Mob.java` 等公共基类；
- [ ] 近战：数值 + Sprite，无自定义状态机；远程：`zap` + 回调 + `next()`，并写出「谁调 next」；
- [ ] `act()` 的所有 `return false` 路径都有回调出口；
- [ ] 覆写 `act()` 时先放行麻痹/恐惧/`Amok`/`Dread`；
- [ ] 存档字段走 `Bundle`，新字段对旧档缺失做防御；
- [ ] 文本键 = 全限定类名去包前缀后全小写（例 `actors.mobs.radishenemy.goblinrider.name`），zh/en 双侧齐全；
- [ ] 精灵帧网格能整除 PNG 尺寸（见 `sprite-frame-shader-guide.md`），帧索引写注释；
- [ ] 实机跑一轮：出生 → 移动 → 攻击（近战与远程各一次）→ 死亡 → 读档。
