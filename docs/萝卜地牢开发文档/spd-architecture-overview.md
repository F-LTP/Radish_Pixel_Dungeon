# SPD 架构速览（本仓库 = Shattered PD 的下游分支）

> 用途：给「新接手本仓库的开发者 / AI」一张地图 —— 知道**什么该去哪找**，而不是把整个 SPD 读一遍。
> 说明：本文由**目录结构 + 已核对的关键类**整理，不是逐行通读的产物；标注「需核对」的地方以源码为准。
> 上游权威副本：`_upstream/spd`（只读）。本仓库包名与之相同，可直接对照。

## 1. 三个模块

| 模块 | 路径 | 作用 |
|---|---|---|
| `core` | `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/` | **游戏本体**（所有玩法/关卡/UI/文本） |
| `SPD-classes` | `SPD-classes/src/main/java/com/watabou/` | 引擎地基：`noosa`（渲染/场景/输入/粒子）、`utils`（`Bundle`/`Random`/`Callback`…） |
| `android` / `desktop` | `android/`、`desktop/` | 两端壳：`AndroidLauncher` / `DesktopLauncher`，玩家数据目录、存档位置都在这里决定 |

文本资源：`core/src/main/assets/messages/**/*.properties`（zh + en 双份）；贴图：`core/src/main/assets/{sprites,items,environment,...}`。

## 2. 一次「玩家点一下」的完整链路（最常用的读代码路线）

```
输入 → scenes/GameScene（场景树 + actor 线程 + UI 层）
     → scenes/CellSelector（选格/瞄准）
     → actors/hero/Hero（handle / 生成 HeroAction）
     → actors/Actor.act()（回合调度，单线程 actor thread）
     → 具体 Actor：Mob / Blob / Buff / 陷阱 …
     → 表现：sprites/CharSprite（动画）+ effects/（粒子、浮字、光束）
```

关键事实（已核对）：

- `actors/Actor.java:290` `doNext = acting.act();`；`:314` 未完成则 `wait()` —— **任何 `act()` 返回 false 又没人 `next()` 的地方都会永久卡死**。
- `scenes/GameScene.java` 在 `!Actor.processing()` 时起/复用 actor 线程（约 `:816-819`）；`GameScene.show()` 负责把 `Window` 挂到前景并做 camera 换算。
- `sprites/CharSprite.java:852-874` `onComplete()` 只分发 `attack` / `operate` 两种回调。

## 3. 包职责（`core/src/main/java/.../`）

| 包 | 文件量（约） | 职责 / 常改的东西 |
|---|---|---|
| `actors/` | 2 + 子包 | `Actor`/`Char` 基类；`hero/`、`mobs/`、`buffs/`、`blobs/`（酸/火/水/网等范围效果） |
| `levels/` | 22 + 子包 | `Level` 基类、`*BossLevel`、`branches/`（分支）、`rooms/`、`painters/`、`traps/`、`builders/` |
| `items/` | 32 + 二十余子包 | 武器/护甲/戒指/法杖/神器/药剂/卷轴/卡牌/袋子… |
| `sprites/` | 108 | 每个生物/物品的贴图与帧动画（`TextureFilm`） |
| `windows/` | 67 | 弹窗（背包、商店、选择器、`WndTextNumberInput` …） |
| `ui/` | 58 | 常驻 UI 组件（状态栏、快捷栏、血条、`ScrollPane`、`RedButton`…） |
| `scenes/` | 21 | 场景（标题、选人、`GameScene`、`InterlevelScene`、结算…） |
| `effects/` | 46 | 粒子/特效/浮字/光束 |
| `mechanics/` | 3 | 弹道、视野、路径等底层算法 |
| `custom/` | 自定义 | 本仓库的测试模式、自定义内容（`testmode/`、`messages/M.java` 等） |
| `messages/` | — | `Messages.get(Class, key)` 入口（键 = 全限定类名去包前缀 + 全小写） |

## 4. 常见任务 → 该看哪里

| 任务 | 入口 |
|---|---|
| 加一个怪 | `actors/mobs/**` + `sprites/**` + `messages/actors*`；先读 `mob-dev-principles.md` |
| 加一个物品 | `items/**` 对应子包；掉落进 `levels/` 的 `Generator`/`prizes` |
| 改关卡生成 | `levels/`（`build()` + `painters/`），Boss 层看同名 `*BossLevel` |
| 加/改 UI 弹窗 | `windows/**`；宽度务必按 `PixelScene.uiCamera` 夹取 |
| 改战斗数值 | `Char`/`Mob`/`Hero` 的 `attackSkill/defenseSkill/damageRoll/drRoll`；**别在子类里另造一套公式** |
| 改文本 | `assets/messages/**`（zh/en 双份），改完逐键 grep |
| 存档 | 各类的 `storeInBundle/restoreFromBundle`；对象一律 `Bundle` 集合 |
| 音乐/音效 | `Assets` + `core/src/main/assets/{sounds,music}` |

## 5. 已知与本仓库的差异（读上游时注意）

- 本仓库有大量**自定义内容**（自定义职业、Boss、分支、测试模式 `custom/testmode/`），上游没有。
- 本仓库改过 `SPD-classes` 的若干共用层（如 `PointerArea` 的拖动阈值、`ScrollPane`）。
- 因此「上游怎么写的」是**机制权威**，但**具体类可能本仓库已经魔改** —— 两边都要看。

## 6. 怎么继续深入（建议路线）

1. `actors/Actor.java` + `scenes/GameScene.java`：搞清回合同步模型（最高优先级，卡死类问题都在这里）。
2. `actors/Char.java` + `actors/mobs/Mob.java` + `actors/hero/Hero.java`：搞清「伤害/命中/闪避/召唤/死亡」的汇聚点。
3. `items/Item.java` + `items/weapon/Weapon.java` + `items/armor/Armor.java`：物品与装备的钩子。
4. `scenes/InterlevelScene.java` + `Dungeon.java`：楼层切换与存档。
5. 具体内容再按需 `grep`，不要通读。
