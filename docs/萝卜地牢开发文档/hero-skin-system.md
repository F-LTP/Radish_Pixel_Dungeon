# 英雄皮肤系统（Hero Skin System）

> 状态：**已实现**。

## 概述

英雄皮肤（Skin）是职业（`HeroClass`）的变体定义（`SkinDefinition`），可在保留职业玩法（转职、天赋、装甲技能）的同时替换外观精灵与文字描述。本系统约定皮肤的**选择状态随存档持久化**，而非全局共享；且皮肤的**文字描述键独立于基础职业**。

典型示例：盗贼的"赌徒"皮肤（`GamblerSkin`），共享盗贼的转职/天赋，但使用独立贴图与文字。

---

## 1. 皮肤选择状态的持久化

### 背景问题

旧实现将皮肤索引存进 `SPDSettings`（全局，按职业 ordinal 为键），所有存档共享同一份，导致：

- 不同存档无法各自记住自己选的皮肤；
- 读档、状态栏、存档列表、详情头像显示的皮肤与角色实际皮肤可能不一致。

### 数据流设计

```
SPDSettings（全局暂存）  =  角色选择界面的"预选值"，用于新建角色时回填
        │
        ▼  新建角色：initHero 回填
Hero.skin（随存档持久化） =  本局皮肤索引，游戏内一切渲染的唯一依据
```

### 关键改动

**① `Hero` 持有并持久化 `skin` 字段**

```java
public class Hero ... {
    public int skin = 0;   // 本局所选皮肤索引（0 = 基础职业）
}
```

- `storeInBundle` / `restoreFromBundle` 读写该字段（key = `"skin"`）；
- `Hero.preview()` 将其写入 `GamesInProgress.Info.skin`，供存档列表预览。

**② `GamesInProgress.Info` 新增 `skin` 字段**

- `GamesInProgress.set()` 从 `Dungeon.hero.skin` 填充；
- 存档列表 / 详情界面据此按各自皮肤渲染。

**③ `HeroClass` 按上下文区分读写**

```java
// 游戏内：返回本局英雄的皮肤（随存档）
public int GetSkin() {
    if (Dungeon.hero != null && Dungeon.hero.heroClass == this)
        return Dungeon.hero.skin;
    return SPDSettings.getHeroSkin(this.ordinal());  // 未开局：全局暂存
}

// 游戏内修改时写入英雄（随存档）；角色选择界面写入全局暂存
public void SetSkin(int skinIndex) {
    // ...解锁校验...
    if (Dungeon.hero != null && Dungeon.hero.heroClass == this)
        Dungeon.hero.skin = skinIndex;
    SPDSettings.setHeroSkin(this.ordinal(), skinIndex);
}

// 绕过当前英雄，直接读全局暂存，用于新建角色回填
public int getGlobalSkin() {
    return SPDSettings.getHeroSkin(this.ordinal());
}
```

**④ 新建角色时回填全局预选值**

`HeroDefinition.initHero()` 中：

```java
hero.heroClass = heroClass();
hero.skin = hero.heroClass.getGlobalSkin();   // 把选人界面的选择固化进本局存档
Talent.initClassTalents(hero);
```

### 精灵 / 头像渲染

`HeroSprite` 新增按显式皮肤索引取图的静态方法，供无英雄上下文的界面（存档列表）使用：

```java
public static Image avatar( HeroClass cl, int armorTier, int skinIndex );
public static Image body  ( HeroClass cl, int armorTier, int skinIndex );
```

- 两参数版本委托三参数版本，皮肤索引取 `cl.GetSkin()`（游戏内自动落到本局英雄）；
- `StartScene`、`WndGameInProgress` 显式传入 `info.skin`；
- `StatusPane`、`WndHero`、`WndRanking`、`GameScene` 等使用两参数版本，因游戏内 `Dungeon.hero` 已加载，自动解析到正确皮肤。

> 约定：`HeroSprite.updateArmor()` 中 `if (hero.heroClass.activeSkin() != null) return;` 用于自定义贴图皮肤跳过标准护甲行逻辑。

---

## 2. 皮肤文字描述的独立键体系

### 背景问题

旧实现中 `HeroClass.desc()/shortDesc()/unlockMsg()` 硬编码读取 `HeroClass` 的键，皮肤没有独立的描述文本，只能沿用基础职业的描述。

### 架构设计

**① `HeroDefinition` 提供四个可覆盖文本方法**（基础职业默认读取原职业键）

```java
public String heroName()       { return Messages.get(HeroClass.class, heroClass().name()); }
public String heroDesc()       { return Messages.get(HeroClass.class, heroClass().name() + "_desc"); }
public String heroShortDesc()  { return Messages.get(HeroClass.class, heroClass().name() + "_desc_short"); }
public String heroUnlockMsg()  { return heroShortDesc() + "\n\n" + Messages.get(HeroClass.class, heroClass().name() + "_unlock"); }
```

**② `SkinDefinition` 覆盖并回退**

优先读取皮肤自己的键，键缺失时回退到基础职业：

```java
@Override public String heroName() {
    if (Messages.isAvailable(getClass(), "name")) return Messages.get(getClass(), "name");
    return base.heroName();
}
// heroDesc / heroShortDesc / heroUnlockMsg 同理，键名分别为 desc / desc_short / unlock
```

为此在 `Messages` 新增键存在性判断：

```java
public static boolean isAvailable(Class c, String k) {
    String key = c.getName();
    key = key.replace("com.shatteredpixel.shatteredpixeldungeon.", "");
    key += "." + k;
    return getFromBundle(key.toLowerCase(Locale.CHINESE)) != null;
}
```

**③ `HeroClass` 委托到有效定义，并提供带皮肤索引的重载**

```java
public String title()     { return activeDefinition().heroName(); }
public String desc()      { return activeDefinition().heroDesc(); }
public String shortDesc() { return activeDefinition().heroShortDesc(); }
public String unlockMsg() { return activeDefinition().heroUnlockMsg(); }

// 按指定皮肤索引取值，供存档列表等无英雄上下文使用
public String title(int skinIndex)      { SkinDefinition s = skin(skinIndex); return s != null ? s.heroName() : definition().heroName(); }
public String desc(int skinIndex)       { /* 同理 */ }
public String shortDesc(int skinIndex)  { /* 同理 */ }
public String unlockMsg(int skinIndex)  { /* 同理 */ }
```

> ⚠️ 坑：`HeroDefinition.heroName()` 基础实现必须**直接读消息键**（`Messages.get(HeroClass.class, name())`），不能调用 `heroClass().title()`。因为 `HeroClass.title()` 现在委托 `activeDefinition().heroName()`，二者会互相调用形成无限递归。

### 皮肤文字键命名约定

皮肤文字键格式：`actors.hero.definition.skins.<skin类名小写>.<key>`

```properties
actors.hero.definition.skins.gamblerskin.name=赌徒
actors.hero.definition.skins.gamblerskin.desc=赌徒与盗贼共享玩法，但多了一份幸运与表演的派头。\n\n……
actors.hero.definition.skins.gamblerskin.desc_short=赌徒与盗贼玩法相同，但更多一分运气与派头。
actors.hero.definition.skins.gamblerskin.unlock=赌徒是盗贼的皮肤变体，与盗贼一并解锁。
```

### 名称显示优先级

存档界面（`StartScene` / `WndGameInProgress`）显示角色名时的优先级：

```
1. 皮肤激活（info.skin 对应有效皮肤） → 显示皮肤名（如"赌徒"）
2. 否则有转职 → 显示转职名
3. 否则 → 显示职业名
```

```java
if (info.skin > 0 && info.heroClass.skin(info.skin) != null){
    name.text(Messages.titleCase(info.heroClass.title(info.skin)));
} else if (info.subClass != HeroSubClasses.NONE){
    name.text(Messages.titleCase(info.subClass.title()));
} else {
    name.text(Messages.titleCase(info.heroClass.title(info.skin)));
}
```

---

## 3. 新增一个皮肤的步骤

1. **定义皮肤类**：继承 `SkinDefinition`，实现 `skinIndex()/skinName()`，覆盖 `customSprite()/asset()/frameW()/frameH()/scale()/idleFrames()` 等精灵参数；共享转职/天赋默认委托基础职业即可。
2. **加入职业的 `skins()`**：在基础职业的 `HeroDefinition.skins()` 中返回该皮肤。
3. **文字键**：在 `messages/` 下添加 `actors.hero.definition.skins.<skin类名小写>.<key>`（`name/desc/desc_short/unlock`），缺失的键自动回退到基础职业文本。
4. **精灵类**（自定义贴图皮肤）：新建精灵类（如 `GamblerSprite extends HeroSprite`），在 `updateArmor()` 中定义动画并 `texture(Assets.Sprites.<ASSET>)`。

---

## 4. 关键类 / 文件

| 类 / 文件 | 作用 |
|-----------|------|
| `HeroClass` | 职业与皮肤索引的读写入口（`GetSkin/SetSkin/getGlobalSkin/title(...)`） |
| `HeroDefinition` | 职业/皮肤的文本与结构基类（`heroName/heroDesc/...`） |
| `SkinDefinition` | 皮肤变体基类（独立文本键 + 回退逻辑） |
| `Hero.skin` | 本局皮肤索引（随存档持久化） |
| `GamesInProgress.Info.skin` | 存档预览用皮肤索引 |
| `HeroSprite` | 皮肤头像/小人生成（`avatar/body` 三参数重载） |
| `Messages.isAvailable` | 文本键存在性判断 |
| `assets/messages/actors/actors[ _zh].properties` | 皮肤文字键所在文件 |
