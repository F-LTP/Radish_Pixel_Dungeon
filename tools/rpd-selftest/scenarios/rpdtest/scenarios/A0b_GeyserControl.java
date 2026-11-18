package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GeyserTrap;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 对照组（基线上就应该 PASS）：间歇泉陷阱在平地上把周围的角色向外冲 2 格。
 * 布局：怪 (8,10)，陷阱 (9,10)，英雄 (10,10)，没有深渊。英雄把物品扔到陷阱上触发。
 */
public class A0b_GeyserControl implements Scenario {

	@Override
	public String describe() {
		return "对照：间歇泉陷阱在平地上把相邻的怪 / 英雄各冲开 2 格（没有深渊）";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		Rat rat = Arena.dummy( h, Arena.c( 8, 10 ) );
		h.placeTrap( new GeyserTrap(), Arena.c( 9, 10 ) );
		Food food = h.give( new Food() );

		h.throwAt( food, Arena.c( 9, 10 ) );
		h.frames( 40 );

		h.note( "after trap: hero at " + h.cellStr( h.heroPos() ) + "; " + Arena.describe( h, rat ) );
		h.check( h.heroPos() == Arena.c( 12, 10 ), "英雄从 (10,10) 被冲到 (12,10)（实际 " + h.cellStr( h.heroPos() ) + "）" );
		h.check( h.get( () -> rat.pos ) == Arena.c( 6, 10 ), "怪从 (8,10) 被冲到 (6,10)（实际 " + h.cellStr( h.get( () -> rat.pos ) ) + "）" );
	}
}
