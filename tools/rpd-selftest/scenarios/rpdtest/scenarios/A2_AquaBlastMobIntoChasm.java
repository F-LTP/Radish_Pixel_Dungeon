package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.AquaBlast;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (a)：水爆（AquaBlast 法术）把怪冲下深渊。
 *
 * 布局：英雄 (10,10)，怪 (12,10)，x>=13 全是深渊。对怪施放水爆：
 * 间歇泉在怪脚下爆发，中心目标沿弹道方向被冲 2 格 → 落点 (14,10) 在深渊里 → 坠落。
 */
public class A2_AquaBlastMobIntoChasm implements Scenario {

	@Override
	public String describe() {
		return "水爆（AquaBlast）把怪冲进深渊：怪应坠落死亡";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		Arena.chasmEastOf( h, 13 );
		Rat rat = Arena.dummy( h, Arena.c( 12, 10 ) );
		AquaBlast spell = h.give( new AquaBlast() );

		h.cast( spell, Arena.c( 12, 10 ) );
		h.frames( 40 );
		h.rest( 1 );
		h.frames( 20 );

		h.note( "after cast: " + Arena.describe( h, rat ) );
		h.check( h.get( () -> rat.pos ) != Arena.c( 12, 10 ), "怪被冲离了原位 (12,10)（实际位置 " + h.cellStr( h.get( () -> rat.pos ) ) + "）" );
		h.check( !h.get( rat::isAlive ), "怪掉进深渊后死亡（alive=" + h.get( rat::isAlive ) + "）" );
		h.check( !h.get( () -> Dungeon.level.mobs.contains( rat ) ), "怪已经从本层的怪物列表里移除" );
	}
}
