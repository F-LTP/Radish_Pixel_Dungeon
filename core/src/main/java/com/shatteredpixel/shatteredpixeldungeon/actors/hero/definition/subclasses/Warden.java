package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class Warden extends HeroSubClass {

	public Warden(){
		super("WARDEN", HeroIcon.WARDEN);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.DURABLE_TIPS, Talent.BARKSKIN, Talent.VINE_TRAP };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.MORE_DARTS, Talent.GRASS_VISION };
	}

	private static boolean isGrass( int terrain ) {
		return terrain == Terrain.GRASS || terrain == Terrain.HIGH_GRASS || terrain == Terrain.FURROWED_GRASS;
	}

	/** 草地视野 +1 草、+2 高草、+3 枯萎的植被：这些格子对英雄可见。 */
	public static void addGrassVision( boolean[] heroMindFov ) {
		int points = Dungeon.hero == null ? 0 : Dungeon.hero.pointsInTalent( Talent.GRASS_VISION );
		if (points <= 0) return;
		int[] map = Dungeon.level.map;
		for (int i = 0; i < map.length; i++) {
			int t = map[i];
			if (t == Terrain.GRASS
					|| points >= 2 && t == Terrain.HIGH_GRASS
					|| points >= 3 && t == Terrain.FURROWED_GRASS) {
				heroMindFov[i] = true;
			}
		}
	}

	/** 草地视野 +4：站在草地上的敌人移动速度 -20%。 */
	public static float grassSpeedFactor( Char ch ) {
		if (ch.alignment == Char.Alignment.ENEMY && Dungeon.hero != null && Dungeon.level != null
				&& Dungeon.hero.pointsInTalent( Talent.GRASS_VISION ) >= 4
				&& ch.pos >= 0 && ch.pos < Dungeon.level.length() && isGrass( Dungeon.level.map[ch.pos] )) {
			return 0.8f;
		}
		return 1f;
	}
}
