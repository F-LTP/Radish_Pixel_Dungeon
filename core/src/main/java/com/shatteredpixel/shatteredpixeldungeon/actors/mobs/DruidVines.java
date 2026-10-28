package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.watabou.utils.Bundle;

/** 德鲁伊召唤物：荆棘藤（就地反击）。 */
public class DruidVines extends Mob {

	{
		spriteClass = RatSprite.class;
		alignment = Alignment.ALLY;
		state = WANDERING;
	}

	public int grassTiles = 0;

	@Override
	public int damageRoll() {
		return Char.combatRoll( 4, 8 ) + grassTiles;
	}

	@Override
	public int attackSkill( Char target ) {
		Hero h = Dungeon.hero;
		return h == null ? 10 : h.attackSkill( target );
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return 0; // 闪避为零
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Char.combatRoll( 4, 8 ) + 3 * grassTiles;
	}

	@Override
	public void move( int step, boolean travelling ) {
		// 荆棘藤无法移动
	}

	@Override
	public int defenseProc( Char enemy, int damage ) {
		damage = super.defenseProc( enemy, damage );
		// 受到物理伤害后反击真实伤害
		if (enemy != null && Dungeon.level.distance( pos, enemy.pos ) <= 1) {
			enemy.damage( new DamageInfo( 12 + Dungeon.depth / 3, DamageType.TRUE, this, null, this ) );
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
