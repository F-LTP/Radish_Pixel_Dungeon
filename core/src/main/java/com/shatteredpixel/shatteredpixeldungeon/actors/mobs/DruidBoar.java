package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/** 德鲁伊召唤物：野猪。 */
public class DruidBoar extends Mob {

	{
		spriteClass = RatSprite.class;
		alignment = Alignment.ALLY;
		state = WANDERING;
	}

	public int lvl = 1;

	public float power = 1f;

	@Override
	public int damageRoll() {
		return Math.round( (Char.combatRoll( 8, 16 ) + lvl / 2f) * power );
	}

	@Override
	public int attackSkill( Char target ) {
		Hero h = Dungeon.hero;
		return h == null ? 10 : Math.round( h.attackSkill( target ) * power );
	}

	@Override
	public int defenseSkill( Char enemy ) {
		Hero h = Dungeon.hero;
		return h == null ? 10 : Math.round( h.defenseSkill( enemy ) * power );
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Math.round( Char.combatRoll( 3, 6 ) * power );
	}

	@Override
	public float speed() {
		return 1.5f;
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		if (enemy.isAlive() && Random.Float() < (20 + lvl) / 100f) {
			Buff.affect( enemy, Cripple.class, 4f );
		}
		return damage;
	}

	private static final String LVL = "lvl";
	private static final String POWER = "power";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LVL, lvl );
		bundle.put( POWER, power );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		lvl = bundle.getInt( LVL );
		power = bundle.getFloat( POWER );
	}
}
