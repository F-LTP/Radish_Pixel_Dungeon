package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (g)：「用放大镜查看显示的是 dm175 的图，而不是 dm100 的」。
 *
 * 做法（测试场）：放一台未激活的 DM-100 原型机和一台未激活的 DM-175 原型机，分别用放大镜查看
 * （Toolbar 的查看按钮 → GameScene.examineCell → WndInfoMob）。
 * 期望：窗口里画的图和地图上那台机体的图一致。DM-175 那台是对照组（基线上就是对的）。
 */
public class G1_ExamineInactivePrototype implements Scenario {

	@Override
	public String describe() {
		return "放大镜查看未激活的 DM-100 原型机：应显示 DM-100 的图，而不是 DM-175 的图";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		WardenPrototype dm100 = h.spawn( new WardenPrototype( 100 ), Arena.c( 12, 9 ) );
		WardenPrototype dm175 = h.spawn( new WardenPrototype( 175 ), Arena.c( 12, 11 ) );
		h.frames( 10 );
		h.require( h.get( dm100::isInactive ) && h.get( dm175::isInactive ), "两台原型机都处于未激活状态" );

		ExamineLab.examineAndCheck( h, dm175, "未激活的 DM-175（对照）" );
		ExamineLab.examineAndCheck( h, dm100, "未激活的 DM-100" );
	}
}
