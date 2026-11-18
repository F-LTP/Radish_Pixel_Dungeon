package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * (b) 的对照（基线上就应该 PASS）：英雄站着不动、读传送卷轴被传走——人物应该画在它真正所在的格子上。
 *
 * 用来和 B1 / B2 对比：卷轴走的是 ScrollOfTeleportation.appear()（先掐掉移动补间再摆精灵），而且英雄此时没有在走路；
 * 陷阱是在「走上去」的那一步里触发的，走的是另一条不掐补间的路径。对照通过、B1 / B2 失败，说明问题出在后者。
 */
public class B0_TeleportScrollControl implements Scenario {

	@Override
	public String describe() {
		return "对照：读传送卷轴后英雄被传走，且 hero.pos 与精灵所在格一致、精灵可见";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.timeScale( 1f );
		ScrollOfTeleportation scroll = h.give( new ScrollOfTeleportation() );
		final int before = h.heroPos();

		h.read( scroll );
		h.waitUntil( () -> !Dungeon.hero.sprite.isMoving && Dungeon.hero.sprite.alpha() > 0.99f, 600 );
		h.frames( 30 );

		int pos = h.heroPos();
		int spriteCell = h.get( () -> H.spriteCell( Dungeon.hero.sprite ) );
		h.note( "after reading: hero.pos=" + h.cellStr( pos ) + " (was " + h.cellStr( before ) + "), sprite drawn at " + h.cellStr( spriteCell )
				+ " sprite.visible=" + h.get( () -> Dungeon.hero.sprite.visible )
				+ " sprite.alpha=" + h.get( () -> Dungeon.hero.sprite.alpha() ) );
		h.check( pos != before, "英雄被传离了原位（hero.pos=" + h.cellStr( pos ) + "）" );
		h.check( h.get( () -> H.spriteOnCell( Dungeon.hero.sprite, Dungeon.hero.pos ) ),
				"精灵画在 hero.pos 上（hero.pos=" + h.cellStr( pos ) + "，精灵在 " + h.cellStr( spriteCell ) + "）" );
		h.check( h.get( () -> Dungeon.hero.sprite.visible && Dungeon.hero.sprite.alpha() > 0.99f ), "精灵可见（visible 且不透明）" );
	}
}
