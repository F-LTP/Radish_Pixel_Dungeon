package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DruidSunflowerSprite;
import com.watabou.utils.Bundle;

/** 德鲁伊召唤物：太阳花（远程吸血）。 */
public class DruidSunflower extends Mob {

	{
		spriteClass = DruidSunflowerSprite.class;
		alignment = Alignment.ALLY;
		state = WANDERING;
	}

	public int grassTiles = 0;

	public float power = 1f;

	@Override
	public int damageRoll() {
		return Math.round( (Char.combatRoll( 12, 20 ) + grassTiles) * power );
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
		return super.drRoll() + Char.combatRoll( 8, 12 ) + grassTiles;
	}

	// 扎根的植物不会自己走动：追击、游荡、逃跑时都留在原地。
	// 不要用「把 move 覆写成空」来实现：英雄与友方换位靠的就是 move，
	// 它不生效时英雄会走进植物所在的格子，两个单位叠在同一格，植物的贴图还会错位。
	@Override
	protected boolean getCloser( int target ) {
		return false;
	}

	@Override
	protected boolean getFurther( int target ) {
		return false;
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		// 吸取等量生命；自身满血则分给盟友
		int heal = Math.min( damage, Math.max( 0, HT - HP ) );
		if (heal > 0) {
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
	private static final String POWER = "power";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( GRASS, grassTiles );
		bundle.put( POWER, power );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		grassTiles = bundle.getInt( GRASS );
		// 旧存档没有这个键，按未强化处理
		power = bundle.contains( POWER ) ? bundle.getFloat( POWER ) : 1f;
	}
}
