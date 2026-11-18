# 生物、NPC 与 Boss 开发指南

> 本文提供经验性参考；具体 API、继承关系和行为以当前代码为准。

> 编写时间：2026-11-08
> 编写作者：JDSALing
> 适用范围：生物、NPC 与 Boss 的开发任务

### 生物开发原则

先检查目标类的实际继承关系和可复用接口，再决定实现位置。常见敌对生物通常继承 `Mob`，但应以当前代码为准；新行为优先放在具体生物类中，只有多个调用方确实需要共享时才扩展公共基类。对 `Mob`、`Char`、`Actor` 等基础类的修改按影响范围评估，不把本指南理解为绝对禁止或默认允许。

#### A、近战敌对生物开发建议
可以参考**Rat.java**，该类对于基础近战已经可以完美复刻，开发者仅需要对数值，Sprite进行基本调整，
除非我向你特殊指出，否则请不要在基于**Rat.java**代码，生成臆想的多余代码。

#### B、远程敌对生物开发建议
远程怪物基本有zap()这个方法来实现远程攻击，
另外远程攻击不能只在Mob中写了一个doAttack()就完事，
因为远程攻击是一个完整的回调结果，
应该严格按照：
> 远程怪物Mob本体 + 远程怪物Sprite本体

其中，非常建议写远程怪物使用onZapComplete()来进行回调

这可以参考Warlock.java 或者 DM100.java，其中DM100还加了接口CallBack：
```java
public class DM100 extends Mob implements Callback {

  /*
  * 中间代码已省略，
  * 这里只是供你参考Callback逻辑，
  * 请你生成代码时严禁省略
  */

	@Override
	public void call() {
		next();
	}

}
```
DM100.java 这其实也是一种回调的方法
另外，建议您在写Mob时，可以看看：
##### com.watabou.utils下的：
> Callback.java
> Bundlable.java
> Bundle.java
> Random.java

其它的Utils类，在生物这里应用较少，所以不建议你使用其他接口，除非我向你特殊指出

```log
current actor:class
com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype

trace:
java.lang.Object.wait(Native Method)
java.lang.Object.wait(Object.java:405)
java.lang.Object.wait(Object.java:543)
com.shatteredpixel.shatteredpixeldungeon.actors.Actor.process(Actor.java:314)
com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene$2.run(GameScene.java:826)
```

以上报错就是因为只有zap的攻击实现，无动画实现导致的线程卡在此生物上，因为该项目是一款回合制的核心Roguelike游戏，所以对于回合的分配至关重要，由于没有动画，游戏无法对下一回合进行判定，故此，游戏在玩家的面前表现为回合无限卡死的状态。

#### C、NPC

NPC 的移动、击退、受伤和敌对行为应按具体角色设计；`NPC.java` 不保证所有 NPC 都无法移动或被击退。特殊 NPC 若需要免疫伤害或不参与战斗，应只覆写实现该行为所需的方法，避免复制一组不相关的防御/选敌覆写。交互行为由对应的 `interact` 实现处理。
需要互动时在对应 NPC 中实现 `interact(Char)`；受伤、移动或击退规则则按角色需求分别覆写，不要整段复制无关行为。

##### α、NPC基本对话框
如果你需要让NPC进行互动且显示对话框，
你可以使用以下代码：
GLog 是游戏在左下角的浮动文本，一般用于提醒文本，关于**GLog.java**，里面包含了n,w,p(分别代表：负面提示-红，警告提示-橙，正面提示-绿)

如果你需要让NPC有弹窗对话框，例如**Wandmaker.java**委托玩家任务的弹窗，这是**WndQuest.java**的实现方式。
创建窗口属于渲染/UI 操作，应从渲染线程执行。Actor 线程调用 UI 测量会触发线程检查；通过 `Game.runOnRenderThread` 调度 UI 操作：
```java
Game.runOnRenderThread(new Callback() {
									   @Override
					 public void call() {
            //套在这里
							 GameScene.show(new WndQuest(questgiver, text, 0));
					 }
				}
			);
```
这是对 Actor 线程与渲染线程边界的检查；需要操作 UI 时应通过 `Game.runOnRenderThread` 切换到渲染线程执行，
实际上，这个报错是由Evan定义的自定义崩溃：
```java
if (Thread.currentThread().getName().equals("SHPD Actor Thread")) {
				throw new RuntimeException("Text measured from the actor thread!");
			}
```
但是，严禁你去修改底层这里来解决未套Game.runOnRenderThread的报错，这绝对是严令禁止的行为。

#### Boss
好吧，我们终于讨论到了**Boss**的开发，Boss这个东西，严格来说是一个行为逻辑，状态机巨多的一个高级怪物。
如果我们要写一个Boss，我建议你参考**Goo.java**,
然后，对于状态的保存，应该使用**Bundle.java**,
而且在这里应该直接以 storeInBundle / restoreFromBundle：
```java
  private final String PUMPEDUP = "pumpedup";
	private final String HEALINC = "healinc";

	@Override
	public void storeInBundle( Bundle bundle ) {

		super.storeInBundle( bundle );

		bundle.put( PUMPEDUP , pumpedUp );
		bundle.put( HEALINC, healInc );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {

		super.restoreFromBundle( bundle );

		pumpedUp = bundle.getInt( PUMPEDUP );
		if (state != SLEEPING) BossHealthBar.assignBoss(this);
		if ((HP*2 <= HT)) BossHealthBar.bleed(true);

		healInc = bundle.getInt(HEALINC);
	}
```
storeFromBundle 是 存储状态，是在游戏内进行的
restoreFromBundle 是 读取状态，是读取内进行的

严禁在restoreFromBundle直接进行Dungeon.level 或 Dungeon.hero等**Dungeon.java**的调用，因为在这里这些尚未实例化，所以返回为null，会崩溃游戏。

##### Boss 开发检查

- 先查找项目中相近的 Boss 行为和状态持久化实现。
- 优先在具体 Boss 类中实现专属行为。
- 修改共享基础类前，检查调用方、继承关系和回归影响，并按改动范围验证。

##### Boss常用高频接口

###### 直属接口
（也就是能直接在Boss本体类使用的）
- 传送技能
- 源文件：ScrollOfTeleportation.java
- 实例：
ScrollOfTeleportation.appear(Char char,int pos)

---

- 闪电技能
- 源文件：DM100.java，DM100Sprite.java
- 实例：DM100.java 中的doAttack 与 DM100Sprite 中的zap方法结合

---

- 攻击上Buff技能
- 接口：attackProc
- 实例：FetidRat.java 中的 attackProc方法

---

- 落石技能
- 接口：dropRocks()
- 实例：DM3000 中的 dropRocks方法

###### 外部建议接口

**Level.java**

seal() 锁楼层的触发方法

unseal() 解锁楼层的触发方法

occupyCell() 实时监测楼层状态的方法


```text
上面的文本是给AI看的，
这里的文本是给人看的，AI直接忽略：
关于生物的指南暂时写到这，
后续如果有更多建议，会继续更新。
```