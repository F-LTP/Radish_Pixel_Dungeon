# 04 生物与 Boss

> **开工前必读原文**：`docs/萝卜地牢开发文档/PD-生物开发原则.md`（第一优先级，冲突时以它为准）。
> 下面是能直接照做的执行版。

## 一只怪 = 两个类 + 一张图 + 若干文本

```
actors/mobs/XxxMob.java          ← 属性与行为（继承 Mob）
sprites/XxxSprite.java           ← 贴图与动画（继承 MobSprite）
core/src/main/assets/sprites/xxx.png
Assets.java                      ← 贴图路径常量
messages/actors/actors.properties / actors_zh.properties
```

`XxxMob` 里必须有 `spriteClass = XxxSprite.class`；`XxxSprite` 里 `texture(Assets.Sprites.XXX)`。

## 三条硬红线（踩了就卡死或闪退）

1. **远程攻击必须成套**：`Mob` 本体 + `Sprite` 本体 + `onZapComplete()` / Callback。
   只写了「发射」没写回调 → `Actor.process()` 永远等不到完成 → **整局卡住、回合无限空转**。
2. **弹窗必须回渲染线程**：`Game.runOnRenderThread(() -> GameScene.show(new WndQuest(...)))`。
   在逻辑线程直接开窗会随机崩。
3. **`restoreFromBundle` 里不要碰 `Dungeon.level` / `Dungeon.hero`**（此时还没就绪，必 NPE）。

## Boss

- 放在 `actors/mobs/`，`properties = MobProperties.BOSS`，被 `BossHealthBar.assignBoss()` 接管血条。
- 关卡里 `createMobs()` 用 `GameScene.add()`/`mobs.add()` 放 Boss；**别在生成阶段用 `Actor.findChar()` 判空**（怪物还没进队列，恒为 null → 重叠）。
- 击杀要调 `GameScene.bossSlain()`、掉落放 `die()`，出口解锁写在关卡类里。
- 半血形态切换：机制类存 `enraged` 布尔 + 存档键；**精灵换形态走「一个 Sprite 类 + 重建多组 Animation」**，不要改 `spriteClass`（懒加载，改了不生效）。

## 生成与掉落

- 普通怪：`levels/<某层>/createMobs()` 或 `Generator` 的权重表。
- 权重表 `classes` 与 `probs` **必须等长**，`probs` 是并列权重不是累加。

## 自检清单

- [ ] `:core:compileJava` 通过
- [ ] 模拟器/真机能生成、能打、能死
- [ ] 死亡掉落与击杀横幅正确
- [ ] 读档后仍正常（Boss 形态、血量）
- [ ] 中英文文本齐全（英文缺会显示 `!!!NO TEXT FOUND!!!`）
