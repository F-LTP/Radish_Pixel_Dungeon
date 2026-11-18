package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.levels.WardenBossLevel;

import rpdtest.H;
import rpdtest.Scenario;

import java.util.Set;
import java.util.TreeSet;

/**
 * 顺带发现（不在用户报告里，所以不在 all 里，用 -Scenario extra 跑）。
 * 这一条是 warden 工作包靠代码走查提出来的（它的报告里叫「崩溃 D」，没有运行期验证），这里补上运行期验证：
 * 开着「蛇咬」挑战时怪物的贴图会被换成蛇（Mob.sprite() 返回 SnakeSprite），
 * 而基线的 WardenPrototype.activateByWarden() / activateByHero() 把精灵强转成 DMPrototypeSprite。
 *
 * 做法：开局后打开蛇咬挑战，进典狱长关卡，走进战斗区开战，然后原地等，让典狱长自己去激活一台原型机（发生在 Actor 线程）。
 * 期望：不崩溃；典狱长能激活原型机；游戏继续。
 * （蛇咬下原型机显示成蛇还是照常显示成机器，属于设计选择，这里不判断，只记录。）
 */
public class Extra_WardenSnakeBiteActivation implements Scenario {

	@Override
	public String describe() {
		return "顺带发现：开着蛇咬挑战打典狱长，典狱长激活原型机的瞬间不应崩溃";
	}

	@Override
	public void run( H h ) {
		enterWithSnakeBite( h );
		h.setHeroHP( 2000, 2000 );
		WardenLab.startFight( h );
		h.note( "sprite classes of the prototypes under Snake Bite: " + h.get( Extra_WardenSnakeBiteActivation::prototypeSpriteClasses ) );

		int turns = 0;
		while (turns < 30 && h.heroAlive() && h.get( Extra_WardenSnakeBiteActivation::wardenAllies ) == 0) {
			h.rest( 1 );
			turns++;
		}
		h.note( "after " + turns + " turn(s): prototypes activated by the Warden: " + h.get( Extra_WardenSnakeBiteActivation::wardenAllies )
				+ "; " + h.get( WardenLab::field ) );
		h.require( h.heroAlive(), "等典狱长激活原型机的过程中英雄没有死" );
		h.check( h.get( Extra_WardenSnakeBiteActivation::wardenAllies ) > 0, "蛇咬挑战下典狱长在 30 回合内激活了至少一台原型机" );

		h.check( h.rest( 3 ) == H.Status.READY, "原型机被激活之后游戏还能继续（又过了 3 个回合）" );
		h.check( h.get( () -> {
			for (WardenPrototype p : WardenLab.prototypes()) {
				if (!p.isAlive() || p.sprite == null || p.sprite.parent == null) return false;
			}
			return true;
		} ), "场上的原型机都活着，精灵都挂在场景里" );
		String stacked = h.get( WardenLab::stackedChars );
		h.check( stacked == null, "没有两个角色站在同一格上" + (stacked == null ? "" : "（实际重叠：" + stacked + "）") );
	}

	/** 开新局、打开蛇咬挑战、进第 10 层的典狱长关卡。英雄在入口。 */
	private static void enterWithSnakeBite( H h ) {
		h.newGameInArena( HeroClasses.WARRIOR, Arena.SEED );
		h.world( "turn on the Snake Bite challenge for this run", () -> Dungeon.challenges |= Challenges.SNAKE_BITE );
		h.gotoLevel( 10, WardenBossLevel.class );
		h.require( h.get( () -> Dungeon.level instanceof WardenBossLevel ), "第 10 层是典狱长关卡（WardenBossLevel）" );
		h.require( h.heroPos() == WardenLab.ENTRANCE, "英雄落在入口 (6,16)" );
		h.require( h.get( () -> Dungeon.isChallenged( Challenges.SNAKE_BITE ) ), "蛇咬挑战处于开启状态" );
	}

	/** 只能在渲染线程调用。 */
	private static int wardenAllies() {
		int n = 0;
		for (WardenPrototype p : WardenLab.prototypes()) {
			if (p.isWardenAlly()) n++;
		}
		return n;
	}

	/** 只能在渲染线程调用。 */
	private static String prototypeSpriteClasses() {
		Set<String> names = new TreeSet<>();
		for (WardenPrototype p : WardenLab.prototypes()) {
			names.add( p.sprite == null ? "null" : p.sprite.getClass().getSimpleName() );
		}
		return names.toString();
	}
}
