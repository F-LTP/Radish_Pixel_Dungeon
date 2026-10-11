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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeadcrabSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

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
		if (Dungeon.level.trueDistance( pos, enemy.pos ) <= BOW_RANGE) {
			Ballistica arrow = new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE );
			return arrow.collisionPos == enemy.pos;
		}
		return false;
	}

	@Override
	public boolean attack( Char enemy, float dmgMulti, float dmgBonus, float accMulti ) {
		if (enemy != null && !Dungeon.level.adjacent( pos, enemy.pos )) {
			rangedAttack( enemy );
			return true;
		}
		return super.attack( enemy, dmgMulti, dmgBonus, accMulti );
	}

	private void rangedAttack( Char enemy ) {
		Ballistica arrow = new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE );
		int to = arrow.collisionPos;

		if (sprite == null || sprite.parent == null) {
			hitWithArrow( to );
			return;
		}

		Sample.INSTANCE.play( Assets.Sounds.MISS );
		((MissileSprite)sprite.parent.recycle( MissileSprite.class )).reset(
				pos, to, new SpiritBow().knockArrow(), new Callback() {
					@Override
					public void call() {
						hitWithArrow( to );
					}
				} );
	}

	private void hitWithArrow( int cell ) {
		if (!isAlive()) {
			return;
		}
		Char ch = Actor.findChar( cell );
		if (ch == null) {
			Dungeon.level.pressCell( cell );
			return;
		}
		if (ch == this || ch.alignment == alignment) {
			return;
		}

		if (Char.hit( this, ch, 1f, false )) {
			ch.damage( new DamageInfo( damageRoll(), DamageType.PHYSICAL, this, null, this ) );
		} else {
			ch.sprite.showStatus( CharSprite.NEUTRAL, ch.defenseVerb() );
		}
	}
}
