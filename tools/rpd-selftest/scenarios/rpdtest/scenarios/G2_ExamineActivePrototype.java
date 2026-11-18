package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * (g) 的延伸：已经激活的原型机（地图上已经换成 DM-100 / DM-175 本体的贴图）用放大镜查看时，
 * 窗口里也应该是激活后的样子，而不是未激活时那张「躺倒」的图。
 * 用友方激活（activateByHero），这样它们不会来打英雄。
 */
public class G2_ExamineActivePrototype implements Scenario {

	@Override
	public String describe() {
		return "放大镜查看已激活的 DM-100 / DM-175 原型机：应显示激活后的图";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		WardenPrototype dm100 = h.spawn( new WardenPrototype( 100 ), Arena.c( 12, 9 ) );
		WardenPrototype dm175 = h.spawn( new WardenPrototype( 175 ), Arena.c( 12, 11 ) );
		h.world( "activate both prototypes as the hero's allies, standing still", () -> {
			dm100.activateByHero();
			dm175.activateByHero();
			dm100.state = dm100.PASSIVE;
			dm175.state = dm175.PASSIVE;
		} );
		h.frames( 10 );
		h.require( h.get( dm100::isHeroAlly ) && h.get( dm175::isHeroAlly ), "两台原型机都已激活（友方）" );

		ExamineLab.examineAndCheck( h, dm100, "已激活的 DM-100" );
		ExamineLab.examineAndCheck( h, dm175, "已激活的 DM-175" );
	}
}
