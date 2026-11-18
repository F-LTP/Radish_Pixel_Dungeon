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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ButterflyField;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.WeaponDamageTypes;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Random;

/** 圣宣：四阶长鞭，4 倍速、射程 8。命中撒黑白蝴蝶、随机造成神圣/腐蚀伤害，
 *  并有低概率在目标脚下留下蝴蝶场地。原武技「哀悼」依赖决斗家，本项目无此角色，已改为被动。 */
public class HolyDecree extends Whip {

	{
		image = ItemSpriteSheet.HOLY_DECREE;
		hitSound = Assets.Sounds.HOLY_DECREE_HIT;
		hitSoundPitch = 1f;

		tier = 4;
		DLY = 0.25f;
		RCH = 8;
	}

	@Override
	public int min(int lvl) { return 1 + lvl; }

	@Override
	public int max(int lvl) { return 5 + lvl; }

	// 黑白蝴蝶：specks.png 帧 16 / 17（由移植包贴图提供）。
	public static final Emitter.Factory BUTTERFLIES = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			Speck p = (Speck) emitter.recycle( Speck.class );
			p.reset( 0, x, y, Random.Int(2) == 0
					? Speck.BUTTERFLY_BLACK : Speck.BUTTERFLY_WHITE );
		}
	};

	@Override
	public Emitter emitter() {
		Emitter e = new Emitter();
		e.pos( 8, 8 );
		e.fillTarget = false;
		e.pour( BUTTERFLIES, 0.6f );
		return e;
	}

	public static void emitButterflies( Char target ){
		if (target != null && target.sprite != null){
			target.sprite.centerEmitter().start( BUTTERFLIES, 0.15f, Random.IntRange( 4, 6 ) );
		}
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (damage > 0 && defender != null && defender.isAlive()){
			emitButterflies( defender );
			// 原设计「每击 50% 精神 / 50% 侵蚀」是伤害类型标签；本项目没有那套接口，
			// 改为追加 1 点对应类型的伤害，类型统一走 WeaponDamageTypes 的映射。
			defender.damage( new DamageInfo( 1,
					Random.Int(2) == 0 ? WeaponDamageTypes.PSYCHIC : WeaponDamageTypes.CORROSION,
					attacker, this, this ) );
			// 低概率留一小片蝴蝶场地，保留「哀悼」的观感但不需要决斗家武技。
			if (Random.Float() < 0.1f && Dungeon.level != null){
				GameScene.add( Blob.seed( defender.pos, 4 + buffedLvl(), ButterflyField.class ) );
			}
		}
		return super.proc( attacker, defender, damage );
	}

	/** 本项目没有决斗家，不展示任何武技说明。 */
	@Override
	public String abilityInfo() { return ""; }
}
