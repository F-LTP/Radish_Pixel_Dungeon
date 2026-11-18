package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TeleportationTrap;
import com.watabou.utils.PathFinder;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (b)：英雄走上传送陷阱后，应该确实被传走，并且人物画在它真正所在的格子上、看得见。
 *
 * 布局：英雄 (10,10)，传送陷阱 (11,10)。英雄点陷阱格走上去。被传走之后在落点旁边再放一个传送陷阱，再踩一次。
 *
 * 这个场景在 1 倍速下跑（h.timeScale( 1f )）：bug 是「走路的补间动画还没播完，人已经被传走，
 * 补间继续播，把精灵拖回陷阱格」；8 倍速下补间一两帧就播完，第一次踩（相关代码第一次执行、类加载慢）时
 * 偶尔会碰巧赶在传送之前播完，看起来像是没问题。踩两次也是为了让第二次走在「热」代码上。
 */
public class B1_TeleportTrap implements Scenario {

	@Override
	public String describe() {
		return "英雄踩传送陷阱（两次）：被传走，且 hero.pos 与精灵所在格一致、精灵可见";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.timeScale( 1f );

		final int trap1 = Arena.c( 11, 10 );
		h.placeTrap( new TeleportationTrap(), trap1 );
		stepOn( h, trap1, "第 1 次" );

		// 落点旁边再放一个，再踩一次
		final int from = h.heroPos();
		final int trap2 = h.get( () -> {
			for (int d : PathFinder.NEIGHBOURS4) {
				int c = from + d;
				if (Dungeon.level.passable[c] && Actor.findChar( c ) == null && Dungeon.level.traps.get( c ) == null) return c;
			}
			return -1;
		} );
		h.require( trap2 != -1, "第 1 次的落点旁边有空格可以放第二个陷阱" );
		h.placeTrap( new TeleportationTrap(), trap2 );
		stepOn( h, trap2, "第 2 次" );

		// 传走之后还能正常行动：原地等一回合，位置不应该再变
		int pos = h.heroPos();
		h.rest( 1 );
		h.frames( 40 );
		h.check( h.heroPos() == pos, "传走后等一回合，英雄位置不变" );
		h.check( h.get( () -> H.spriteOnCell( Dungeon.hero.sprite, Dungeon.hero.pos ) ), "等一回合后精灵仍画在 hero.pos 上" );
	}

	private void stepOn( H h, int trap, String label ) {
		h.tap( trap );
		// 让走路的补间动画彻底结束（bug 正是它把精灵拖回了陷阱格）
		h.waitUntil( () -> !Dungeon.hero.sprite.isMoving, 600 );
		h.frames( 60 );

		int pos = h.heroPos();
		int spriteCell = h.get( () -> H.spriteCell( Dungeon.hero.sprite ) );
		h.note( label + ": trap at " + h.cellStr( trap ) + ", hero.pos=" + h.cellStr( pos ) + ", sprite drawn at " + h.cellStr( spriteCell )
				+ " sprite.visible=" + h.get( () -> Dungeon.hero.sprite.visible )
				+ " sprite.alpha=" + h.get( () -> Dungeon.hero.sprite.alpha() ) );
		h.screenshot( "after-teleport-trap" );

		h.check( pos != trap, label + "踩陷阱：英雄被传离了陷阱格（hero.pos=" + h.cellStr( pos ) + "）" );
		h.check( h.get( () -> H.spriteOnCell( Dungeon.hero.sprite, Dungeon.hero.pos ) ),
				label + "踩陷阱：精灵画在 hero.pos 上（hero.pos=" + h.cellStr( pos ) + "，精灵在 " + h.cellStr( spriteCell ) + "）" );
		h.check( h.get( () -> Dungeon.hero.sprite.visible && Dungeon.hero.sprite.alpha() > 0.99f ), label + "踩陷阱：精灵可见（visible 且不透明）" );
	}
}
