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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.watabou.gltextures.AtlasFrame;
import com.watabou.gltextures.AtlasSource;
import com.watabou.gltextures.RuntimeAtlas;
import com.watabou.gltextures.RuntimeAtlasRegistry;
import com.watabou.noosa.Image;

// 把字符串写在文件里面纯粹是想要用IDE的引用计数功能，不过鉴于大部分的常量都只用了一次，其实不用也许是个好选择……
public class HeroIcon extends Image {

	public static final AtlasSource ATLAS_SOURCE = new AtlasSource("interfaces/hero_icons", "none");
	private static final RuntimeAtlas ATLAS = RuntimeAtlasRegistry.get(ATLAS_SOURCE);


	//transparent icon
	public static final String NONE = "none";

	//subclasses
	public static final String BERSERKER = "berserker";
	public static final String GLADIATOR = "gladiator";
	public static final String BATTLEMAGE = "battlemage";
	public static final String WARLOCK = "warlock";
	public static final String ASSASSIN = "assassin";
	public static final String FREERUNNER = "freerunner";
	public static final String SNIPER = "sniper";
	public static final String WARDEN = "warden";
	public static final String CHAMPION = "champion";
	public static final String MONK = "monk";
	public static final String BATTLE_PRIEST = "battle_priest";
	public static final String RED_CARDINAL = "red_cardinal";
	public static final String DEAD_KNIGHT = "dead_knight";

	//abilities
	public static final String ASH_KING = "ash_king";
	public static final String END_BLESS = "end_bless";
	public static final String SHADOW = "shadow";
	public static final String POSSESSION = "possession";
	public static final String HEROIC_LEAP = "heroic_leap";
	public static final String SHOCKWAVE = "shockwave";
	public static final String ENDURE = "endure";
	public static final String ELEMENTAL_BLAST = "elemental_blast";
	public static final String WILD_MAGIC = "wild_magic";
	public static final String WARP_BEACON = "warp_beacon";
	public static final String SMOKE_BOMB = "smoke_bomb";
	public static final String DEATH_MARK = "death_mark";
	public static final String SHADOW_CLONE = "shadow_clone";
	public static final String SPECTRAL_BLADES = "spectral_blades";
	public static final String NATURES_POWER = "natures_power";
	public static final String SPIRIT_HAWK = "spirit_hawk";
	public static final String CHALLENGE = "challenge";
	public static final String ELEMENTAL_STRIKE = "elemental_strike";
	public static final String FEINT = "feint";
	public static final String RATMOGRIFY = "ratmogrify";

	//action indicator visuals
	public static final String BERSERK = "berserk";
	public static final String COMBO = "combo";
	public static final String PREPARATION = "preparation";
	public static final String MOMENTUM = "momentum";
	public static final String SNIPERS_MARK = "snipers_mark";
	public static final String WEAPON_SWAP = "weapon_swap";
	public static final String MONK_ABILITIES = "monk_abilities";
	public static final String BLESS = "bless";

	//Moonlight SubClasses
	public static final String LITTLE_KNIGHT = "little_knight";
	public static final String DICE_MAGE = "dice_mage";
	public static final String JUTTE_CHAMPION = "jutte_champion";

	//Moonlight ArmorAbility
	public static final String TOY_BACKPACK = "toy_backpack";
	public static final String FATED_DRAW = "fated_draw";

	public HeroIcon(HeroSubClass subCls){
		this(subCls.icon());
	}

	public HeroIcon(ArmorAbility abil){
		this(abil.icon());
	}

	public HeroIcon(ActionIndicator.Action action){
		this(action.actionIcon());
	}

	public HeroIcon(String icon) {
		super();
		AtlasFrame atlasFrame = ATLAS.frame(icon);
		texture = atlasFrame.texture;
		frame(atlasFrame.uv);
	}

}
