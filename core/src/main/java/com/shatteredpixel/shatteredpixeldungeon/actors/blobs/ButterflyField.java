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
package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.WeaponDamageTypes;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.HolyDecree;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

/** 圣宣留下的黑白蝴蝶场地：每回合对敌人造成 1 点神圣 + 1 点腐蚀伤害并迟缓。
 *  类型统一走 WeaponDamageTypes，不引入本项目没有的伤害类型。 */
public class ButterflyField extends Blob {

	public static final float POUR_INTERVAL = 0.12f;

	@Override
	protected void evolve() {
		int cell;
		for (int i = area.left-1; i <= area.right; i++) {
			for (int j = area.top-1; j <= area.bottom; j++) {
				cell = i + j * Dungeon.level.width();
				if (cell < 0 || cell >= cur.length) continue;
				if (cur[cell] > 0) {
					affect(cell);
					off[cell] = cur[cell] - 1;
					volume += off[cell];
				} else {
					off[cell] = 0;
				}
			}
		}
	}

	private void affect( int cell ){
		Char ch = Actor.findChar( cell );
		if (ch == null || ch.alignment != Char.Alignment.ENEMY || !ch.isAlive()) return;
		ch.damage( new DamageInfo( 1, WeaponDamageTypes.PSYCHIC, null, null, this ) );
		ch.damage( new DamageInfo( 1, WeaponDamageTypes.CORROSION, null, null, this ) );
		Buff.prolong( ch, Slow.class, 8f );
	}

	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.pour( HolyDecree.BUTTERFLIES, POUR_INTERVAL );
	}

	@Override
	public String tileDesc() {
		return Messages.get( this, "desc" );
	}
}
