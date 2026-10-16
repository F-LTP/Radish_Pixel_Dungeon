package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.watabou.utils.Bundle;

/** 德鲁伊召唤物：橡木智者（无敌支援）。 */
public class DruidOakSage extends Mob {

	{
		spriteClass = RatSprite.class;
		alignment = Alignment.ALLY;
		state = WANDERING;
	}

	public int lifespan = 200;

	@Override
	public boolean isInvulnerable( Class damageType ) {
		return true;
	}

	@Override
	protected boolean act() {
		if (--lifespan <= 0) {
			die( null );
			return true;
		}
		int pts = Dungeon.hero != null
				? Dungeon.hero.pointsInTalent( com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.NATURE_BOND )
				: 0;
		int radius = 2 + pts;
		int shield = 2 + Math.round( pts * 0.5f );
		for (Char ch : Actor.chars()) {
			if (ch.alignment == Alignment.ALLY && ch != this
					&& Dungeon.level.distance( pos, ch.pos ) <= radius) {
				Buff.affect( ch, Barrier.class ).incShield( shield );
			}
		}
		Hero h = Dungeon.hero;
		if (h != null && h.isAlive() && Dungeon.level.distance( pos, h.pos ) <= radius) {
			Buff.affect( h, Haste.class, 1f );
		}
		return super.act();
	}

	@Override
	public int damageRoll() {
		return 0;
	}

	@Override
	public int attackSkill( Char target ) {
		return 0;
	}

	private static final String LIFESPAN = "lifespan";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LIFESPAN, lifespan );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		lifespan = bundle.getInt( LIFESPAN );
	}
}
