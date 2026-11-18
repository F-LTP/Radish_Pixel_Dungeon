package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GatewayTrap;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (b)：英雄走上虫洞陷阱（GatewayTrap）后，应该确实被传走，并且人物画在它真正所在的格子上、看得见。
 * 虫洞陷阱不会失效：第一次踩随机传送并记住落点，之后每次踩都传到那个落点附近。两次都测。
 *
 * 布局：英雄 (10,10)，虫洞陷阱 (11,10)。
 */
public class B2_GatewayTrap implements Scenario {

	@Override
	public String describe() {
		return "英雄踩虫洞陷阱（两次）：被传走，且 hero.pos 与精灵所在格一致、精灵可见";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		// 1 倍速：和动画时序有关（走路补间把被传走的精灵拖回陷阱格），理由见 B1_TeleportTrap 的说明
		h.timeScale( 1f );
		final int trap = Arena.c( 11, 10 );
		h.placeTrap( new GatewayTrap(), trap );

		// ---- 第一次：随机传送
		h.tap( trap );
		h.waitUntil( () -> !Dungeon.hero.sprite.isMoving, 600 );
		h.frames( 60 );
		final int first = h.heroPos();
		verify( h, trap, "第 1 次" );

		// ---- 第二次：走回陷阱旁边再踩一次，应该被传到第一次落点附近
		int approach = Arena.c( 12, 10 );
		if (first != approach) {
			h.walkTo( approach );
		}
		h.frames( 30 );
		h.require( h.heroPos() == approach, "英雄走回到了陷阱旁边 (12,10)" );
		h.tap( trap );
		h.waitUntil( () -> !Dungeon.hero.sprite.isMoving, 600 );
		h.frames( 60 );
		verify( h, trap, "第 2 次" );
		int second = h.heroPos();
		h.check( h.get( () -> Dungeon.level.distance( first, second ) ) <= 1,
				"第 2 次的落点在第 1 次落点 " + h.cellStr( first ) + " 的 1 格之内（实际 " + h.cellStr( second ) + "）" );
	}

	private void verify( H h, int trap, String label ) {
		int pos = h.heroPos();
		int spriteCell = h.get( () -> H.spriteCell( Dungeon.hero.sprite ) );
		h.note( label + ": hero.pos=" + h.cellStr( pos ) + " sprite drawn at " + h.cellStr( spriteCell )
				+ " sprite.visible=" + h.get( () -> Dungeon.hero.sprite.visible )
				+ " sprite.alpha=" + h.get( () -> Dungeon.hero.sprite.alpha() ) );
		h.screenshot( "after-gateway-trap" );
		h.check( pos != trap, label + "踩陷阱：英雄被传离了陷阱格（hero.pos=" + h.cellStr( pos ) + "）" );
		h.check( h.get( () -> H.spriteOnCell( Dungeon.hero.sprite, Dungeon.hero.pos ) ),
				label + "踩陷阱：精灵画在 hero.pos 上（hero.pos=" + h.cellStr( pos ) + "，精灵在 " + h.cellStr( spriteCell ) + "）" );
		h.check( h.get( () -> Dungeon.hero.sprite.visible && Dungeon.hero.sprite.alpha() > 0.99f ),
				label + "踩陷阱：精灵可见（visible 且不透明）" );
	}
}
