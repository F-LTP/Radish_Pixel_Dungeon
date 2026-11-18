package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GeyserTrap;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (a)：英雄被水爆击退进深渊。
 *
 * 布局：间歇泉陷阱 (9,10)，英雄 (10,10)，x>=11 全是深渊。
 * 英雄把一件物品扔到陷阱上触发它：陷阱周围 8 格里的角色被向外冲 2 格 →
 * 英雄的落点 (12,10) 在深渊里 → 坠落到下一层。
 * （原版规则：只有站在间歇泉正中的英雄才会被冲向安全方向，站在旁边的不保护。）
 */
public class A5_GeyserHeroIntoChasm implements Scenario {

	@Override
	public String describe() {
		return "英雄被旁边的间歇泉陷阱冲进深渊：应坠落到下一层";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		Arena.chasmEastOf( h, 11 );
		h.placeTrap( new GeyserTrap(), Arena.c( 9, 10 ) );
		Food food = h.give( new Food() );

		h.throwAt( food, Arena.c( 9, 10 ) );
		h.frames( 40 );

		h.note( "after trap: depth=" + h.depth() + " hero at " + h.cellStr( h.heroPos() ) + " alive=" + h.heroAlive() );
		h.check( h.depth() == 2, "英雄掉到了第 2 层（实际在第 " + h.depth() + " 层）" );
		h.check( h.heroAlive(), "英雄摔下去后还活着（300 血）" );
	}
}
