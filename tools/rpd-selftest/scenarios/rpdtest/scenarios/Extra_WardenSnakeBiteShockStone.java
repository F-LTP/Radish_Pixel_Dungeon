package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfShock;
import com.shatteredpixel.shatteredpixeldungeon.levels.WardenBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;

import rpdtest.H;
import rpdtest.HarnessError;
import rpdtest.Scenario;

import java.util.Set;
import java.util.TreeSet;

/**
 * 顺带发现（不在用户报告里，所以不在 all 里，用 -Scenario extra 跑）。
 * 与 Extra_WardenSnakeBiteActivation 是同一个问题（warden 工作包报告里的「崩溃 D」）的另一个入口：
 * 开着「蛇咬」挑战时，玩家用电击符石把一台未激活的原型机收归己方（StoneOfShock.activate → activateByHero()，发生在渲染线程）。
 *
 * 做法：开局后打开蛇咬挑战，进典狱长关卡，走进战斗区开战，立刻朝一台未激活的原型机扔一颗电击符石。
 * 期望：不崩溃；有一台原型机归了玩家；游戏继续。
 */
public class Extra_WardenSnakeBiteShockStone implements Scenario {

	@Override
	public String describe() {
		return "顺带发现：开着蛇咬挑战打典狱长，用电击符石收一台原型机的瞬间不应崩溃";
	}

	@Override
	public void run( H h ) {
		h.newGameInArena( HeroClasses.WARRIOR, Arena.SEED );
		h.world( "turn on the Snake Bite challenge for this run", () -> Dungeon.challenges |= Challenges.SNAKE_BITE );
		h.gotoLevel( 10, WardenBossLevel.class );
		h.require( h.get( () -> Dungeon.level instanceof WardenBossLevel ), "第 10 层是典狱长关卡（WardenBossLevel）" );
		h.require( h.heroPos() == WardenLab.ENTRANCE, "英雄落在入口 (6,16)" );
		h.require( h.get( () -> Dungeon.isChallenged( Challenges.SNAKE_BITE ) ), "蛇咬挑战处于开启状态" );
		h.setHeroHP( 2000, 2000 );
		WardenLab.startFight( h );
		final StoneOfShock stone = h.give( new StoneOfShock() );
		h.require( h.get( Extra_WardenSnakeBiteShockStone::wardenAllies ) == 0, "扔符石之前典狱长还没有激活任何原型机" );
		h.note( "sprite classes of the prototypes under Snake Bite: " + h.get( Extra_WardenSnakeBiteShockStone::prototypeSpriteClasses ) );

		// 目标：一台未激活的原型机，离英雄 3 格以上（符石的电弧波及 2 格，别把英雄自己麻痹了），投掷弹道通畅。
		// 现成的没有就挪一台过去（世界操作；真实游戏里就是玩家换个站位）。
		final String[] what = { null };
		final int[] targetCell = { -1 };
		h.world( "pick (or place) an inactive prototype the hero can hit with a thrown stone", () -> {
			int hero = Dungeon.hero.pos;
			for (WardenPrototype p : WardenLab.inactivePrototypes()) {
				if (throwable( hero, p.pos )) {
					targetCell[0] = p.pos;
					what[0] = "target " + WardenLab.describe( p ) + " (already reachable)";
					return;
				}
			}
			if (WardenLab.inactivePrototypes().isEmpty()) throw new HarnessError( "no inactive prototype on the field" );
			WardenPrototype p = WardenLab.inactivePrototypes().get( 0 );
			for (int c = 0; c < Dungeon.level.length(); c++) {
				if (Actor.findChar( c ) == null && Dungeon.level.passable[c] && throwable( hero, c )) {
					int from = p.pos;
					WardenLab.move( p, c );
					targetCell[0] = c;
					what[0] = "target " + WardenLab.describe( p ) + " (moved from " + WardenLab.xy( from ) + ")";
					return;
				}
			}
			throw new HarnessError( "no cell the hero can hit with a thrown stone" );
		} );
		h.note( what[0] + "; hero at " + h.cellStr( h.heroPos() ) );

		H.Status st = h.throwAt( stone, targetCell[0] );
		h.frames( 40 );
		h.note( "after the throw: status=" + st + "; prototypes owned by the hero: " + h.get( Extra_WardenSnakeBiteShockStone::heroAllies )
				+ "; " + h.get( WardenLab::field ) );
		h.check( st == H.Status.READY, "扔出电击符石之后游戏在等下一步输入（实际 status=" + st + "）" );
		h.check( h.get( Extra_WardenSnakeBiteShockStone::heroAllies ) == 1, "电击符石把 1 台未激活的原型机收归玩家（实际 "
				+ h.get( Extra_WardenSnakeBiteShockStone::heroAllies ) + " 台）" );

		h.check( h.rest( 3 ) == H.Status.READY, "收了一台原型机之后游戏还能继续（又过了 3 个回合）" );
		h.check( h.get( () -> {
			for (WardenPrototype p : WardenLab.prototypes()) {
				if (!p.isAlive() || p.sprite == null || p.sprite.parent == null) return false;
			}
			return true;
		} ), "场上的原型机都活着，精灵都挂在场景里" );
		String stacked = h.get( WardenLab::stackedChars );
		h.check( stacked == null, "没有两个角色站在同一格上" + (stacked == null ? "" : "（实际重叠：" + stacked + "）") );
	}

	/** 英雄站在 hero 格时，能不能把东西扔到 cell：在战斗区里、看得见、3～6 格远、弹道不被挡。只能在渲染线程调用。 */
	private static boolean throwable( int hero, int cell ) {
		WardenBossLevel level = (WardenBossLevel) Dungeon.level;
		if (!level.heroInBossArena( cell ) || !level.heroFOV[cell]) return false;
		int d = level.distance( hero, cell );
		if (d < 3 || d > 6) return false;
		return new Ballistica( hero, cell, Ballistica.PROJECTILE ).collisionPos == cell;
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
	private static int heroAllies() {
		int n = 0;
		for (WardenPrototype p : WardenLab.prototypes()) {
			if (p.isHeroAlly()) n++;
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
