package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (a)：英雄被击退进深渊。
 *
 * 布局：英雄 (10,10)，x>=11 全是深渊。英雄用冲击波法杖射西边相邻的空地 (9,10)：
 * 爆心周围 8 格里的角色（包括英雄自己）被向外推 1 格 → 英雄落在 (11,10) 深渊格 → 坠落到下一层。
 * （对照见 A0：同样的操作在平地上英雄确实被推了 1 格。）
 */
public class A4_BlastWaveHeroIntoChasm implements Scenario {

	@Override
	public String describe() {
		return "英雄被自己的冲击波推进相邻的深渊：应坠落到下一层";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		Arena.chasmEastOf( h, 11 );
		WandOfBlastWave wand = h.give( new WandOfBlastWave() );

		h.zap( wand, Arena.c( 9, 10 ) );
		h.frames( 40 );

		h.note( "after zap: depth=" + h.depth() + " hero at " + h.cellStr( h.heroPos() ) + " alive=" + h.heroAlive() );
		h.check( h.depth() == 2, "英雄掉到了第 2 层（实际在第 " + h.depth() + " 层）" );
		h.check( h.heroAlive(), "英雄摔下去后还活着（300 血）" );
	}
}
