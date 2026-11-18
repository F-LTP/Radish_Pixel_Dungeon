package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warden;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;

import rpdtest.H;
import rpdtest.Scenario;

import java.util.List;

/**
 * 用户报告 (d) 的另一半：英雄踏进战斗区后开战——封层，典狱长与 11 台原型机（8 台 DM-100 + 3 台 DM-175）在场。
 * 基线代码这一半本来就是好的，这个场景主要用来防止修 D1 时把开战弄坏。
 */
public class D2_WardenFightStart implements Scenario {

	@Override
	public String describe() {
		return "典狱长关卡：英雄踏进战斗区后开战（封层，Boss 与 11 台原型机在场）";
	}

	@Override
	public void run( H h ) {
		WardenLab.enter( h );
		WardenLab.walkIn( h );
		h.frames( 20 );
		h.require( h.heroPos() == WardenLab.INSIDE, "英雄站在进门后的第一格 (6,11)" );
		h.note( "after stepping in: " + h.get( WardenLab::field ) );

		h.check( h.get( () -> Dungeon.level.locked ), "关卡已封锁（Dungeon.level.locked）" );
		h.note( "door cell (6,12): terrain=" + h.get( () -> Dungeon.level.map[WardenLab.DOOR] ) + " passable=" + h.get( () -> Dungeon.level.passable[WardenLab.DOOR] ) );

		final Warden w = h.get( WardenLab::warden );
		h.check( w != null && h.get( w::isAlive ), "典狱长在场且活着" );
		if (w != null) {
			h.check( h.get( () -> w.pos / WardenLab.W < 12 && Dungeon.level.passable[w.pos] ), "典狱长站在战斗区里的可通行格上（实际 " + WardenLab.xy( h.get( () -> w.pos ) ) + "）" );
			h.check( h.get( () -> w.sprite != null && w.sprite.parent != null ), "典狱长的精灵挂在场景里" );
			h.check( h.get( () -> BossHealthBar.isAssigned( w ) ), "Boss 血条已经指向典狱长" );
		}

		List<WardenPrototype> protos = h.get( WardenLab::prototypes );
		int n100 = 0, n175 = 0;
		for (WardenPrototype p : protos) {
			if (p.model == 100) n100++;
			else if (p.model == 175) n175++;
		}
		h.check( protos.size() == 11, "场上有 11 台原型机（实际 " + protos.size() + "）" );
		h.check( n100 == 8 && n175 == 3, "8 台 DM-100 + 3 台 DM-175（实际 " + n100 + " + " + n175 + "）" );
		h.check( h.get( () -> {
			for (WardenPrototype p : WardenLab.prototypes()) {
				if (p.pos / WardenLab.W >= 12 || !Dungeon.level.passable[p.pos]) return false;
			}
			return true;
		} ), "所有原型机都在战斗区里的可通行格上" );
		h.check( h.get( () -> {
			for (WardenPrototype p : WardenLab.prototypes()) {
				if (p.sprite == null || p.sprite.parent == null) return false;
			}
			return true;
		} ), "所有原型机的精灵都挂在场景里" );
		h.note( "inactive prototypes right after the fight started: " + h.get( () -> WardenLab.inactivePrototypes().size() ) + " of " + protos.size() );

		String stacked = h.get( WardenLab::stackedChars );
		h.check( stacked == null, "没有两个角色站在同一格上" + (stacked == null ? "" : "（实际重叠：" + stacked + "）") );

		h.check( h.get( () -> {
			for (Mob m : Dungeon.level.mobs) {
				if (m.pos == Dungeon.hero.pos) return false;
			}
			return true;
		} ), "没有怪和英雄站在同一格上" );
	}
}
