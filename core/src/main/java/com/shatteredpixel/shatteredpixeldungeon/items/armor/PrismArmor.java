/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LifeLink;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.damage.IDamageModifier;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

/** 棱镜护铠：四阶护甲，远程伤害 -25%，远程法术伤害有概率反射回去。 */
public class PrismArmor extends Armor {

	private static final float RANGED_FACTOR = 0.75f;

	{ image = ItemSpriteSheet.ARMOR_PRISM; }

	public PrismArmor() { super( 4 ); }

	@Override public int DRMax(int lvl){ return 6 + 4 * lvl + augment.defenseFactor(lvl); }
	@Override public int DRMin(int lvl){ return 2 + lvl; }

	/** 反射概率，单位为百分点。 */
	private static int reflectChance( int lvl ){ return 10 + 7 * lvl; }

	// 未鉴定时只给 +0 的数值，否则能从描述里的数字反推出等级
	@Override
	public String statsInfo() {
		if (isIdentified()) {
			return Messages.get( this, "stats_desc", reflectChance( buffedLvl() ) );
		} else {
			return Messages.get( this, "typical_stats_desc", reflectChance( 0 ) );
		}
	}

	@Override
	protected ArmorBuff buff() { return new PrismBuff(); }

	// 普通攻击（近战、射击、投掷）都走 attack()，伤害类型恒为物理，远程减伤在这里结算
	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		if (attacker != null && defender != null && Dungeon.level != null
				&& !Dungeon.level.adjacent( attacker.pos, defender.pos )){
			damage = Math.round( damage * RANGED_FACTOR );
		}
		return super.proc( attacker, defender, damage );
	}

	/**
	 * 怪物的法术不经过 attack()，不会触发 proc()，而是直接构造 DamageInfo 进伤害管线。
	 * 这里只认「类型为法术、有施法者、施法者不相邻」的伤害，与 proc() 处理的物理攻击互不重叠，不会重复减伤。
	 */
	private boolean isRangedSpell( Char wearer, DamageInfo info ) {
		if (wearer == null || info == null || Dungeon.level == null) return false;
		// 与 proc() 的生效条件保持一致：遗落行囊期间护甲不起作用
		if (Char.defendingArmor( wearer ) != this) return false;

		DamageType type = info.getType();
		if (type == null || !type.isMagical() || type.isTrueDamage()) return false;
		// 生命联结分摊过来的伤害，attacker 填的是被分摊的那一方，不是施法者
		if (info.getSource() instanceof LifeLink) return false;

		Char caster = info.getAttacker();
		return caster != null && caster != wearer
				&& !Dungeon.level.adjacent( caster.pos, wearer.pos );
	}

	/**
	 * 反射伤害不在受击的伤害管线里当场结算：这时施法者还在自己的行动中途
	 * （多段光束打到一半，或伤害之后还要播施法动画、等回调来结束回合），
	 * 当场把它打死，后面的逻辑会在一个已经死亡、已移出行动队列的角色上继续跑。
	 * 挂一个一次性的 Actor，等当前行动结束后立刻结算，与英雄正常出手打它没有区别。
	 */
	private void reflect( final Char wearer, final Char caster, final int amount ) {
		if (caster.sprite != null) {
			caster.sprite.showStatus( CharSprite.NEGATIVE, Messages.get( PrismArmor.class, "reflect" ) );
		}
		Actor.add( new Actor() {
			{ actPriority = VFX_PRIO; }

			@Override
			protected boolean act() {
				Actor.remove( this );
				if (caster.isAlive()) {
					caster.damage( new DamageInfo( amount, DamageType.MAGICAL, wearer, PrismArmor.this, PrismArmor.this ) );
				}
				return true;
			}
		} );
	}

	public class PrismBuff extends ArmorBuff implements IDamageModifier {

		// 最终相位四舍五入取整，与 proc() 里的 Math.round 一致
		@Override
		public float finalMultiply( int dmg, DamageInfo info ) {
			return isRangedSpell( target, info ) ? RANGED_FACTOR : 1f;
		}

		@Override
		public void onDamageTaken( int dealt, DamageInfo info ) {
			if (!(target instanceof Hero) || !isRangedSpell( target, info )) return;
			if (Random.Float() >= reflectChance( buffedLvl() ) / 100f) return;
			reflect( target, info.getAttacker(), Math.max( 1, info.getDamage() ) );
		}
	}
}
