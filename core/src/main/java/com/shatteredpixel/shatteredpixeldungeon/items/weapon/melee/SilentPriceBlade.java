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
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.WeaponDamageTypes;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

/** 沉默的代价：四阶近战，十二击蓄势，第十三次命中斩杀或追加魔法伤害。
 *  原武技「钟响」依赖决斗家，本项目无此角色，已整块去掉。 */
public class SilentPriceBlade extends MeleeWeapon {

	public static final int HITS_TO_CHARGE = 12;
	private static final String HITS = "hits";
	private static final String CHARGED = "charged";

	{
		image = ItemSpriteSheet.SILENT_PRICE_BLADE;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.9f;
		tier = 4;
		ACC = 0.9f;
	}

	private int hits = 0;
	private boolean charged = false;

	@Override
	public int min(int lvl) { return 4 + lvl; }

	@Override
	public int max(int lvl) { return 13 + 4*lvl; }

	@Override
	public String image() {
		return charged ? ItemSpriteSheet.SILENT_PRICE_BLADE_CHARGED : ItemSpriteSheet.SILENT_PRICE_BLADE;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (attacker instanceof Hero){
			if (charged){
				charged = false;
				hits = 0;
				updateQuickslot();
				reap( attacker, defender, damage );
			} else if (++hits >= HITS_TO_CHARGE){
				charged = true;
				updateQuickslot();
			}
		}
		return super.proc( attacker, defender, damage );
	}

	private void reap( Char attacker, Char defender, int damage ) {
		if (defender == null || !defender.isAlive()) return;
		boolean boss = Char.hasProp( defender, Char.Property.BOSS )
				|| Char.hasProp( defender, Char.Property.MINIBOSS );
		int hpPercent = Math.round( defender.HP * 100f / defender.HT );

		if (!boss && damage >= hpPercent){
			final Char target = defender;
			final Item cause = this;
			Actor.add( new Actor() {
				{ actPriority = VFX_PRIO; }
				@Override
				protected boolean act() {
					if (target.isAlive()){
						target.HP = 0;
						if (!target.isAlive()) target.die( cause );
					}
					Actor.remove( this );
					return true;
				}
			});
			return;
		}

		int extra = Math.round( damage * defender.HT / 100f );
		if (extra > 0 && !defender.isInvulnerable( attacker.getClass() )){
			// 原「灵魂伤害」统一到本项目已有的魔法类型。
			defender.damage( new DamageInfo( extra, WeaponDamageTypes.SOUL, attacker, this, this ) );
		}
	}

	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle( bundle );
		bundle.put( HITS, hits );
		bundle.put( CHARGED, charged );
	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle( bundle );
		hits = bundle.getInt( HITS );
		charged = bundle.getBoolean( CHARGED );
	}

	/** 本项目没有决斗家，不展示任何武技说明。 */
	@Override
	public String abilityInfo() { return ""; }
}
