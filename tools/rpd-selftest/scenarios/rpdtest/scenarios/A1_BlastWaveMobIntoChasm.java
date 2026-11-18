package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (a)：冲击波法杖把一只普通怪推下相邻的深渊格，怪应该坠落死亡。
 *
 * 布局（y=10 这一行）：英雄 (10,10)，怪 (12,10)，x>=13 全是深渊。
 * 法杖 +0：中心目标沿弹道方向被推 3 格 → 落点 (15,10) 在深渊里 → 坠落。
 */
public class A1_BlastWaveMobIntoChasm implements Scenario {

	@Override
	public String describe() {
		return "冲击波法杖把怪推进相邻的深渊：怪应坠落死亡";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		Arena.chasmEastOf( h, 13 );
		Rat rat = Arena.dummy( h, Arena.c( 12, 10 ) );
		WandOfBlastWave wand = h.give( new WandOfBlastWave() );

		h.zap( wand, Arena.c( 12, 10 ) );
		h.frames( 40 );
		h.rest( 1 );
		h.frames( 20 );

		h.note( "after zap: " + Arena.describe( h, rat ) );
		h.check( h.get( () -> rat.pos ) != Arena.c( 12, 10 ), "怪被推离了原位 (12,10)（实际位置 " + h.cellStr( h.get( () -> rat.pos ) ) + "）" );
		h.check( !h.get( rat::isAlive ), "怪掉进深渊后死亡（alive=" + h.get( rat::isAlive ) + "，HP " + h.get( () -> rat.HP ) + "）" );
		h.check( !h.get( () -> Dungeon.level.mobs.contains( rat ) ), "怪已经从本层的怪物列表里移除" );
	}
}
