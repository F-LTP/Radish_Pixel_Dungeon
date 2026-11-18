package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DruidVinesSprite;
import com.watabou.utils.Bundle;

/** 德鲁伊召唤物：荆棘藤（就地反击）。 */
public class DruidVines extends Mob {

	{
		spriteClass = DruidVinesSprite.class;
		alignment = Alignment.ALLY;
		state = WANDERING;
	}

	public int grassTiles = 0;
	public int lvl = 1;

	@Override
	public int damageRoll() {
		return Char.combatRoll( 8, 13 ) + grassTiles;
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
		return super.drRoll() + Char.combatRoll( 4, 8 ) + grassTiles;
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

	/** 75% 法术抗性（所有魔法伤害减免到 25%）。 */
	@Override
	public float resistanceTo( DamageType type ) {
		float base = super.resistanceTo( type );
		if (type.isMagical()) {
			return base * 0.25f;
		}
		return base;
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
	private static final String LVL = "lvl";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( GRASS, grassTiles );
		bundle.put( LVL, lvl );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		grassTiles = bundle.getInt( GRASS );
		lvl = bundle.getInt( LVL );
	}
}
