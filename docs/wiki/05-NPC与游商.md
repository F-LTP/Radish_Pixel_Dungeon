# 05 NPC 与游商

## 现有 NPC 家族

`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/`

| 类 | 作用 |
|---|---|
| `Peddler` / `PeddlerSprite` / `PeddlerCatalog` | 流动商人（游商）本体、贴图、货单 |
| `PeddlerShop` / `WndPeddlerShop` | 货架数据与交易窗口 |
| 灰羽 / 月华 / 其他定制 NPC | 各自继承 `Mob`，`properties = MobProperties.NEUTRAL/ALLY` |

NPC 是「不会主动战斗的 Mob」，所以它同样受 [04](04-生物与Boss.md) 的三条红线约束。

## 新增一个 NPC 的最小步骤

1. `actors/mobs/npcs/XxxNpc.java`（属性、`alignment`、对话与交互入口）
2. `sprites/XxxNpcSprite.java` + 贴图 + `Assets.java` 常量
3. 生成位置：写进对应层的 `createMobs()`（例如第 1 层入口放商人）
4. 交互：覆写 `interact(Char)` 打开窗口（要 `Game.runOnRenderThread`）
5. 文本：`actors_mobs_npcs_xxxnpc.name/desc/dialogue…`，中英同步
6. 公告：`ui/changelist/rapd/RA_v0_8_X_Changes.java` 加一条（玩家可见的改动必须写）

## 交易/以物换物

- 货币统一走 `Dungeon.gold`（扣钱 `Dungeon.gold -= price`，社区里散落的 `WndTradeItem` 是标准先例）。
- 自定义服务（治疗、修理）**必须先校验金币再扣费**，失败要给 `GLog.w` 反馈，否则玩家会以为白嫖。

## 常见坑

- NPC 被玩家误攻击：靠 `alignment` 与 `heroShouldInteract()` 控制，别在 `act()` 里硬改。
- 换位/传送前校验目标格：`Dungeon.level.passable[cell]` 且无 `traps.get(cell)`，否则会踩陷阱或卡墙。
- 一群 NPC 同一格：生成阶段不要用 `Actor.findChar()` 判空（同 04）。
