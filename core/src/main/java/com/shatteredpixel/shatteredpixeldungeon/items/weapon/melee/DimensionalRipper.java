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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RiftWindow;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.WeaponDamageTypes;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RiftParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Random;

/** 次元撕裂者：三阶弯刀，命中概率随机传送目标或英雄，成功后可开窗追击。
 *  原武技「次元跳跃」依赖决斗家，本项目无此角色，已整块去掉。 */
public class DimensionalRipper extends Scimitar {

	public static final float PROC_CHANCE_BASE = 1/8f;
	public static final float PROC_CHANCE_MAX = 1/2f;
	public static final float PROC_CHANCE_DECAY = 7/8f;

	{
		image = ItemSpriteSheet.DIMENSIONAL_RIPPER;
		tier = 3;
		DLY = 0.8f;
	}

	public static float procChance( int lvl ) {
		float gap = (PROC_CHANCE_MAX - PROC_CHANCE_BASE) * (float) Math.pow( PROC_CHANCE_DECAY, lvl );
		return Math.max( 0f, PROC_CHANCE_MAX - gap );
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		damage = super.proc( attacker, defender, damage );
		if (damage > 0) hitFx( defender );

		if (attacker.buff( MagicImmune.class ) == null
				&& defender.isAlive() && damage > 0
				&& Random.Float() < procChance( buffedLvl() ) * Weapon.Enchantment.genericProcChanceMultiplier( attacker )){
			Char warped;
			if (Random.Int( 2 ) == 0){
				warped = warp( defender ) ? defender : null;
			} else {
				warped = warp( attacker ) ? attacker : null;
			}
			if (warped != null){
				// 原说明只把本武器登记为 CORROSION 换跳字色；统一到本项目已有的腐蚀类型。
				defender.damage( new DamageInfo( 1, WeaponDamageTypes.CORROSION, attacker, this, this ) );
				// 这 1 点腐蚀可能刚好把目标打死，那就没有可追击的对象了，不开窗也不提示
				if (attacker == Dungeon.hero && defender.isAlive()){
					Buff.affect( Dungeon.hero, RiftWindow.class ).open( defender );
				}
			}
		}
		return damage;
	}

	private static boolean warp( Char ch ){
		if (ch == null
				|| Char.hasProp( ch, Char.Property.IMMOVABLE )
				|| ch.isImmune( ScrollOfTeleportation.class )){
			return false;
		}
		int oldPos = ch.pos;
		// 落点、贴图可见性、英雄视野刷新都由 teleportChar 负责，这里不再另行修正贴图
		if (!ScrollOfTeleportation.teleportChar( ch )) return false;
		teleportFx( oldPos, ch.pos );
		if (ch instanceof Mob){
			Mob mob = (Mob) ch;
			if (mob.state == mob.HUNTING) mob.state = mob.WANDERING;
		}
		return true;
	}

	public static final int HIT_PARTICLES = 6;
	public static final int WARP_PARTICLES = 8;

	private static void hitFx( Char defender ){
		if (defender == null || defender.sprite == null) return;
		Emitter emitter = defender.sprite.centerEmitter();
		if (emitter != null) emitter.burst( RiftParticle.HIT, HIT_PARTICLES );
	}

	public static void teleportFx( int fromPos, int toPos ){
		if (Dungeon.level == null || Dungeon.level.heroFOV == null) return;
		if (Dungeon.level.heroFOV[fromPos]) CellEmitter.get( fromPos ).burst( RiftParticle.SCATTER, WARP_PARTICLES );
		if (Dungeon.level.heroFOV[toPos]) CellEmitter.center( toPos ).burst( RiftParticle.IMPLODE, WARP_PARTICLES );
	}

	/** 主手 / 副手是否还握着次元撕裂者；窗口有效性判定要用。 */
	public static DimensionalRipper equipped( Hero hero ){
		if (hero == null || hero.belongings == null) return null;
		if (hero.belongings.weapon() instanceof DimensionalRipper){
			return (DimensionalRipper) hero.belongings.weapon();
		}
		if (hero.belongings.secondWep() instanceof DimensionalRipper){
			return (DimensionalRipper) hero.belongings.secondWep();
		}
		return null;
	}

	/** 武器离手即关窗，避免留下一个用不了的按钮。 */
	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )){
			if (equipped( hero ) == null) Buff.detach( hero, RiftWindow.class );
			return true;
		}
		return false;
	}

	@Override
	public String statsInfo() {
		// 等级未知时按 +0 显示，和 MeleeWeapon.info() 对伤害数值的处理一致；
		// 否则不用鉴定就能从这个百分比反推出强化等级
		int lvl = levelKnown ? buffedLvl() : 0;
		return Messages.get( this, "stats_desc", Math.round( procChance( lvl ) * 100 ) );
	}

	/** 本项目没有决斗家，不展示任何武技说明。 */
	@Override
	public String abilityInfo() { return ""; }
}
