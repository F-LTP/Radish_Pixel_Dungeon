package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.KickTracker;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * audit-buffs-a：踹飞给 throwChar 的弹道带了「到目标格就停」，而目标格正好是 3 × 方向。
 * 紧挨着英雄的敌人目标格离它正好 3 格 = 击退距离，throwChar 把「弹道在击退距离处结束」当成撞到了东西，
 * 于是空地上也结算撞墙：额外 3～6 点伤害并麻痹 2.5 回合。
 */
public class Extra_AudBuffsA_KickOpenGround implements Scenario {

	@Override
	public String describe() {
		return "踹飞：空地上紧挨英雄的敌人只受 1～3 点伤害、被踹开 3 格、不麻痹；真撞墙的才麻痹";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		// 南边 (10,13) 放一堵墙：南边那只只能飞 1 格就撞墙，作为「真撞墙仍然结算」的对照
		h.setTerrain( Arena.c( 10, 13 ), Terrain.WALL );
		Rat east = Arena.dummy( h, Arena.c( 11, 10 ) );
		Rat north = Arena.dummy( h, Arena.c( 10, 9 ) );
		Rat southWest = Arena.dummy( h, Arena.c( 9, 11 ) );
		Rat south = Arena.dummy( h, Arena.c( 10, 11 ) );

		h.world( "grant the kick action", () -> KickTracker.checkKick( Dungeon.hero ) );
		h.require( h.get( () -> ActionIndicator.action instanceof KickTracker ), "周围 3 格内有 4 个敌人时出现踹飞按钮" );

		h.input( "press the kick action button", () -> ActionIndicator.action.doAction() );
		h.waitReady();
		h.frames( 30 );

		h.note( "east: " + Arena.describe( h, east ) + " paralysed=" + h.get( () -> east.buff( Paralysis.class ) != null ) );
		h.note( "north: " + Arena.describe( h, north ) + " paralysed=" + h.get( () -> north.buff( Paralysis.class ) != null ) );
		h.note( "southWest: " + Arena.describe( h, southWest ) + " paralysed=" + h.get( () -> southWest.buff( Paralysis.class ) != null ) );
		h.note( "south (wall behind): " + Arena.describe( h, south ) + " paralysed=" + h.get( () -> south.buff( Paralysis.class ) != null ) );

		checkOpen( h, east, Arena.c( 14, 10 ), "东边" );
		checkOpen( h, north, Arena.c( 10, 6 ), "北边" );
		checkOpen( h, southWest, Arena.c( 6, 14 ), "西南" );

		h.check( h.get( () -> south.pos ) == Arena.c( 10, 12 ), "南边那只身后 2 格是墙：被踹到墙根 (10,12)（实际 " + h.cellStr( h.get( () -> south.pos ) ) + "）" );
		h.check( h.get( () -> south.buff( Paralysis.class ) != null ), "南边那只确实撞了墙：被麻痹" );
	}

	private static void checkOpen( H h, Rat rat, int expected, String who ) {
		int pos = h.get( () -> rat.pos );
		int lost = h.get( () -> rat.HT - rat.HP );
		h.check( pos == expected, who + "紧挨英雄的敌人在空地上被踹开 3 格，落在 " + h.cellStr( expected ) + "（实际 " + h.cellStr( pos ) + "）" );
		h.check( h.get( () -> rat.buff( Paralysis.class ) == null ), who + "的敌人身后是空地，没有撞到任何东西：不应被麻痹" );
		h.check( lost >= 0 && lost <= 3, who + "的敌人只受踹击的 1～3 点伤害，没有撞墙伤害（实际损失 " + lost + "）" );
	}
}
