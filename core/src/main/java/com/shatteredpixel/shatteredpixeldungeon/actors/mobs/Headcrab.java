/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeadcrabSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

/**
 * 猎头蟹：螃蟹的稀有变体。
 * 属性与 {@link Crab} 完全相同，但会用弓箭进行远程攻击。
 *
 * 远程逻辑：重写 canAttack 允许在射程内（6 格、有弹道）发起攻击，
 * 重写 attack 在非贴身时射出箭矢。
 */
public class Headcrab extends Crab {

	//弓箭射程
	private static final int BOW_RANGE = 6;

	{
		spriteClass = HeadcrabSprite.class;

		// 必定掉落 2 块生肉
		loot = new com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat().quantity(2);
		lootChance = 1f;
	}

	@Override
	public void die( Object cause ) {
		// 必定额外掉落一件英雄遗骸（随机职业）
		com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass cls =
				com.watabou.utils.Random.oneOf(
						com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses.WARRIOR,
						com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses.MAGE,
						com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses.ROGUE,
						com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses.HUNTRESS );
		Dungeon.level.drop(
				com.shatteredpixel.shatteredpixeldungeon.items.remains.RemainsItem.get( cls ), pos ).sprite.drop();
		super.die( cause );
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		if (super.canAttack( enemy )) {
			return true;
		}
		if (enemy == null || !enemy.isAlive()) {
			return false;
		}
		return canShootAt( enemy );
	}

	// 射程内且弹道直达目标所在格才射得到
	private boolean canShootAt( Char enemy ) {
		if (Dungeon.level.trueDistance( pos, enemy.pos ) <= BOW_RANGE) {
			Ballistica arrow = new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE );
			return arrow.collisionPos == enemy.pos;
		}
		return false;
	}

	@Override
	public boolean attack( Char enemy, float dmgMulti, float dmgBonus, float accMulti ) {
		// 贴身，或射不到但仍在攻击距离内（精英的隔空攻击、挑战装备的长武器）：走父类的近战结算
		if (enemy != null && !Dungeon.level.adjacent( pos, enemy.pos ) && canShootAt( enemy )) {
			rangedAttack( enemy );
			return true;
		}
		return super.attack( enemy, dmgMulti, dmgBonus, accMulti );
	}

	private void rangedAttack( Char enemy ) {
		// 先结算，箭只做表现。调用方紧接着就 spend + next() 结束回合，等箭落地再结算的话，
		// 英雄能在箭还在飞的时候走开，箭落在空格上就什么判定都不做
		hitWithArrow( enemy );

		if (sprite == null || sprite.parent == null) {
			return;
		}

		Sample.INSTANCE.play( Assets.Sounds.MISS );
		((MissileSprite)sprite.parent.recycle( MissileSprite.class )).reset(
				pos, enemy.pos, new SpiritBow().knockArrow(), null );
	}

	private void hitWithArrow( Char target ) {
		if (!isAlive() || !target.isAlive()) {
			return;
		}

		if (Char.hit( this, target, 1f, false )) {
			target.damage( new DamageInfo( damageRoll(), DamageType.PHYSICAL, this, null, this ) );

			// 这一箭不经过 Char.attack，射死英雄要自己记录死因，否则这局不会进排行榜
			if (target == Dungeon.hero && !target.isAlive()) {
				Dungeon.fail( getClass() );
				GLog.n( heroKilledMessage() );
			}
		} else {
			target.sprite.showStatus( CharSprite.NEUTRAL, target.defenseVerb() );
		}
	}
}
