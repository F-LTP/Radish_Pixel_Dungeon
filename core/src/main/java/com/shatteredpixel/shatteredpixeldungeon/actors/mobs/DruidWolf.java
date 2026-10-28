package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SnakeSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/** 德鲁伊召唤物：狼。 */
public class DruidWolf extends Mob {

	{
		spriteClass = SnakeSprite.class;
		alignment = Alignment.ALLY;
		state = WANDERING;
	}

	public int lvl = 1;

	public float power = 1f;

	@Override
	public int damageRoll() {
		return Math.round( (Char.combatRoll( 5, 10 ) + lvl / 3f) * power );
	}

	@Override
	public int attackSkill( Char target ) {
		Hero h = Dungeon.hero;
		return h == null ? 10 : Math.round( h.attackSkill( target ) * 1.2f * power );
	}

	@Override
	public int defenseSkill( Char enemy ) {
		Hero h = Dungeon.hero;
		return h == null ? 10 : Math.round( h.defenseSkill( enemy ) * 1.2f * power );
	}

	@Override
	public float speed() {
		return 2f;
	}

	@Override
	public float attackDelay() {
		return 0.75f;
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		if (enemy.isAlive() && Random.Float() < 0.33f) {
			Buff.affect( enemy, Bleeding.class ).set( 4 + lvl / 8f );
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
