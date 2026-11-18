package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warden;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestArenaLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.WardenBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (d) 的回归保护：英雄不是从门走进来，而是从第 9 层的深渊掉进典狱长关卡。
 *
 * 基线代码里 WardenBossLevel.randomRespawnCell 被改写成「战斗区里 y<=8 的随机空格」（给原型机生成用），
 * 而 Level.fallCell 用的正是 randomRespawnCell，所以坠落的落点在战斗区里面，根本不经过门。
 * 「进门后才出现 Boss」的改法如果只认「踩过门 / 走进战斗区的那一步」，这条路上就可能永远不开战、
 * 或者 Boss 出生在英雄脚下。
 *
 * 步骤：第 9 层（测试场）挖一片深渊 → 英雄自己跳下去（确认窗选「是」）→ 第 10 层强制生成典狱长关卡。
 * 期望（两种落点都接受）：
 *  - 落在战斗区外（入口房间）：此时不应已经开战；之后从门走进去能正常开战；
 *  - 落在战斗区里：3 个回合内正常开战。
 * 「正常开战」= 已封层、恰好 1 个活着的典狱长、11 台原型机、没有角色叠在一起（包括不叠在英雄身上）。
 */
public class D5_WardenFallIn implements Scenario {

	@Override
	public String describe() {
		return "典狱长关卡：英雄从第 9 层的深渊掉进来（不经过门），落点合理，战斗能正常开始";
	}

	private static int wardens( H h ) {
		return h.get( () -> {
			int n = 0;
			for (Mob m : Dungeon.level.mobs) {
				if (m instanceof Warden && m.isAlive()) n++;
			}
			return n;
		} );
	}

	private static boolean inArena( int cell ) {
		int x = cell % WardenLab.W, y = cell / WardenLab.W;
		return x >= 1 && x <= 11 && y >= 1 && y <= 11;
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.gotoLevel( 9, TestArenaLevel.class );
		h.require( h.depth() == 9 && h.get( () -> Dungeon.level instanceof TestArenaLevel && Dungeon.level.width() == Arena.W ), "第 9 层是测试场" );
		h.setHeroHP( 2000, 2000 );
		h.placeHero( Arena.c( 10, 10 ) );
		Arena.chasmEastOf( h, 12 );
		h.world( "the next generated level (depth 10) will be the Warden level", () -> Dungeon.forcedLevelClass = WardenBossLevel.class );

		h.walkTo( Arena.c( 11, 10 ) );
		h.tryTap( Arena.c( 12, 10 ) );
		h.frames( 60 );
		h.require( h.findWindow( WndOptions.class ) != null, "走向深渊时游戏弹出了确认窗（WndOptions）" );
		h.chooseOption( 0 );
		H.Status st = h.waitReady();
		h.frames( 30 );
		h.require( st == H.Status.READY && h.depth() == 10 && h.get( () -> Dungeon.level instanceof WardenBossLevel ),
				"英雄掉到了第 10 层的典狱长关卡（实际 status=" + st + "，第 " + h.depth() + " 层，" + h.get( () -> Dungeon.level.getClass().getSimpleName() ) + "）" );

		final int landing = h.heroPos();
		final boolean landedInArena = inArena( landing );
		h.note( "landed at " + WardenLab.xy( landing ) + (landedInArena ? " = INSIDE the boss arena" : " = outside the boss arena")
				+ "; right after landing: " + h.get( WardenLab::field ) );

		if (!landedInArena) {
			h.check( !h.get( () -> Dungeon.level.locked ), "落在战斗区外：此时还没有开战（未封层）" );
			WardenLab.walkIn( h );
		} else {
			for (int i = 0; i < 3 && !h.get( () -> Dungeon.level.locked && WardenLab.prototypes().size() >= 11 ); i++) {
				h.rest( 1 );
			}
		}
		h.note( "after the fight should have started: " + h.get( WardenLab::field ) );

		h.check( h.get( () -> Dungeon.level.locked ), (landedInArena ? "落进战斗区后 3 个回合内" : "从门走进战斗区后") + "关卡已封锁" );
		h.check( wardens( h ) == 1, "场上恰好 1 个活着的典狱长（实际 " + wardens( h ) + "）" );
		final Warden w = h.get( WardenLab::warden );
		if (w != null) {
			h.check( inArena( h.get( () -> w.pos ) ) && h.get( () -> Dungeon.level.passable[w.pos] ),
					"典狱长站在战斗区里的可通行格上（实际 " + WardenLab.xy( h.get( () -> w.pos ) ) + "）" );
			h.check( h.get( () -> w.sprite != null && w.sprite.parent != null ), "典狱长的精灵挂在场景里" );
		}
		int protos = h.get( () -> WardenLab.prototypes().size() );
		h.check( protos == 11, "场上有 11 台原型机（实际 " + protos + "）" );
		h.check( h.get( () -> {
			for (WardenPrototype p : WardenLab.prototypes()) {
				if (!inArena( p.pos ) || !Dungeon.level.passable[p.pos] || p.sprite == null || p.sprite.parent == null) return false;
			}
			return true;
		} ), "所有原型机都在战斗区里的可通行格上，精灵都挂在场景里" );
		String stacked = h.get( WardenLab::stackedChars );
		h.check( stacked == null, "没有两个角色站在同一格上（包括英雄脚下）" + (stacked == null ? "" : "（实际重叠：" + stacked + "）") );
		h.check( h.heroAlive() && h.rest( 2 ) == H.Status.READY, "之后游戏能继续（英雄存活，又过了 2 个回合）" );
	}
}
