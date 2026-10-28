package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.watabou.utils.Bundle;

/** 德鲁伊召唤物：太阳花（远程吸血）。 */
public class DruidSunflower extends Mob {

	{
		spriteClass = RatSprite.class;
		alignment = Alignment.ALLY;
		state = WANDERING;
	}

	public int grassTiles = 0;

	public float power = 1f;

	@Override
	public int damageRoll() {
		return Math.round( (Char.combatRoll( 15, 25 ) + 5 * grassTiles) * power );
	}

	@Override
	public int attackSkill( Char target ) {
		Hero h = Dungeon.hero;
		return h == null ? 10 : Math.round( h.attackSkill( target ) * 1.5f );
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return 0; // 闪避为零
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Char.combatRoll( 8, 12 ) + 5 * grassTiles;
	}

	@Override
	public void move( int step, boolean travelling ) {
		// 太阳花无法移动
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		// 吸取等量生命；自身满血则分给盟友
		int heal = damage;
		if (HP < HT) {
			heal = Math.min( heal, HT - HP );
			HP += heal;
		}
		int left = damage - heal;
		if (left > 0) {
			for (Char ch : Actor.chars()) {
				if (ch.alignment == Alignment.ALLY && ch != this && ch.isAlive()
						&& ch.HP < ch.HT) {
					int give = Math.min( left, ch.HT - ch.HP );
					ch.HP += give;
					left -= give;
					if (left <= 0) {
						break;
					}
				}
			}
		}
		return damage;
	}

	private static final String GRASS = "grass";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( GRASS, grassTiles );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		grassTiles = bundle.getInt( GRASS );
	}
}
