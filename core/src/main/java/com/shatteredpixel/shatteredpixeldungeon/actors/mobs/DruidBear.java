package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GolemSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/** 德鲁伊召唤物：熊。 */
public class DruidBear extends Mob {

	{
		spriteClass = GolemSprite.class;
		alignment = Alignment.ALLY;
		state = WANDERING;
	}

	public int lvl = 1;

	public float power = 1f;

	@Override
	public int damageRoll() {
		return Math.round( (Char.combatRoll( 16, 26 ) + lvl) * power );
	}

	@Override
	public int attackSkill( Char target ) {
		Hero h = Dungeon.hero;
		return h == null ? 12 : Math.round( h.attackSkill( target ) * power );
	}

	@Override
	public int defenseSkill( Char enemy ) {
		Hero h = Dungeon.hero;
		return h == null ? 12 : Math.round( h.defenseSkill( enemy ) * power );
	}

	@Override
	public int drRoll() {
		// 防御 6~12 + 区域数（简化：按当前楼层数）
		return super.drRoll() + Math.round( (Char.combatRoll( 6, 12 ) + Dungeon.depth) * power );
	}

	@Override
	public float speed() {
		return 0.8f;
	}

	@Override
	public float attackDelay() {
		return 1.5f;
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		if (enemy == null) {
			return damage;
		}

		// 3x3 溅射 75%
		for (Char ch : Actor.chars()) {
			if (ch != enemy && ch.alignment != Alignment.ALLY
					&& ch.isAlive() && Dungeon.level.distance( enemy.pos, ch.pos ) <= 1) {
				ch.damage( new DamageInfo( Math.round( damage * 0.75f ), DamageType.PHYSICAL, this, null, this ) );
			}
		}

		if (enemy.isAlive() && Random.Float() < 0.25f) {
			Buff.affect( enemy, Paralysis.class, 3f );
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
