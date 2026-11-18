package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.AquaBrew;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (a)：水爆魔药（AquaBrew，玩家在炼金釜里能做出来的那个「水爆」）把怪冲下深渊。
 *
 * A2 测的是水爆晶核（AquaBlast 法术）。在这个 fork 里 AquaBlast 的配方没有注册，只有测试模式的生成器能拿到；
 * 玩家平时用的是水爆魔药，所以单独测一遍。两者最终都走 GeyserTrap.activate → WandOfBlastWave.throwChar。
 *
 * 布局：英雄 (10,10)，怪 (12,10)，x>=13 全是深渊。把魔药扔到怪脚下：
 * 间歇泉在怪脚下爆发，正中的目标沿投掷方向被冲 2 格 → 落点 (14,10) 在深渊里 → 坠落。
 */
public class A2b_AquaBrewMobIntoChasm implements Scenario {

	@Override
	public String describe() {
		return "水爆魔药（AquaBrew）把怪冲进深渊：怪应坠落死亡";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		Arena.chasmEastOf( h, 13 );
		Rat rat = Arena.dummy( h, Arena.c( 12, 10 ) );
		AquaBrew brew = h.give( new AquaBrew() );

		h.throwAt( brew, Arena.c( 12, 10 ) );
		h.frames( 40 );
		h.rest( 1 );
		h.frames( 20 );

		h.note( "after throw: " + Arena.describe( h, rat ) );
		h.check( h.get( () -> rat.pos ) != Arena.c( 12, 10 ), "怪被冲离了原位 (12,10)（实际位置 " + h.cellStr( h.get( () -> rat.pos ) ) + "）" );
		h.check( !h.get( rat::isAlive ), "怪掉进深渊后死亡（alive=" + h.get( rat::isAlive ) + "）" );
		h.check( !h.get( () -> Dungeon.level.mobs.contains( rat ) ), "怪已经从本层的怪物列表里移除" );
	}
}
