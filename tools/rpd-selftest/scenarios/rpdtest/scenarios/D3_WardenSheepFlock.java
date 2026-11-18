package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warden;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Sheep;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

import rpdtest.H;
import rpdtest.Scenario;

import java.util.List;

/**
 * 用户报告 (d) 里的「要顶掉可能存在的羊群」：Boss 和原型机出场时，场上可能已经站满了羊
 * （羊毛炸弹可以从门口扔进去），出场逻辑必须把羊顶掉，而不是和羊 / 彼此叠在同一格上。
 *
 * 做法：英雄还在入口时，把战斗区 y=1..10 的每个空格都放上羊（极端情况：除了进门那一行，一个空位都不留），
 * 然后走进去开战。期望：典狱长 + 11 台原型机全部在场，任何两个角色都不在同一格上。
 */
public class D3_WardenSheepFlock implements Scenario {

	@Override
	public String describe() {
		return "典狱长关卡：战斗区站满羊时开战，Boss 与 11 台原型机仍应各占一格出场（顶掉羊群）";
	}

	@Override
	public void run( H h ) {
		WardenLab.enter( h );

		final int[] placed = { 0 };
		h.world( "fill arena rows 1..10 with sheep", () -> {
			for (int y = 1; y <= 10; y++) {
				for (int x = 1; x <= 11; x++) {
					int cell = WardenLab.c( x, y );
					if (!Dungeon.level.passable[cell] || Actor.findChar( cell ) != null) continue;
					Sheep sheep = new Sheep();
					sheep.lifespan = 600;
					sheep.pos = cell;
					GameScene.add( sheep );
					placed[0]++;
				}
			}
		} );
		boolean bossAlreadyThere = h.get( () -> WardenLab.warden() != null );
		h.note( "placed " + placed[0] + " sheep on arena rows 1..10"
				+ (bossAlreadyThere ? "; the Warden was already standing at " + WardenLab.xy( h.get( () -> WardenLab.warden().pos ) ) + " so that cell got no sheep" : "; no Warden on the level yet") );
		h.require( placed[0] >= 100, "战斗区里放了至少 100 只羊" );

		WardenLab.walkIn( h );
		h.frames( 30 );
		h.require( h.heroPos() == WardenLab.INSIDE, "英雄站在进门后的第一格 (6,11)" );

		int sheepLeft = h.get( () -> {
			int n = 0;
			for (Mob m : Dungeon.level.mobs) if (m instanceof Sheep) n++;
			return n;
		} );
		final Warden w = h.get( WardenLab::warden );
		List<WardenPrototype> protos = h.get( WardenLab::prototypes );
		String stacked = h.get( WardenLab::stackedChars );
		h.note( "after stepping in: locked=" + h.get( () -> Dungeon.level.locked ) + " warden=" + (w == null ? "none" : h.get( () -> WardenLab.describe( w ) ))
				+ " prototypes=" + protos.size() + " sheep left=" + sheepLeft + " stacked=" + stacked );

		h.check( h.get( () -> Dungeon.level.locked ), "关卡已封锁" );
		h.check( w != null && h.get( w::isAlive ), "典狱长在场且活着" );
		h.check( protos.size() == 11, "场上有 11 台原型机（实际 " + protos.size() + "）" );
		h.check( stacked == null, "没有两个角色站在同一格上" + (stacked == null ? "" : "（实际重叠：" + stacked + "）") );
		h.check( h.get( () -> {
			for (WardenPrototype p : WardenLab.prototypes()) {
				if (p.pos / WardenLab.W >= 12 || !Dungeon.level.passable[p.pos]) return false;
			}
			return true;
		} ), "所有原型机都在战斗区里的可通行格上" );
	}
}
