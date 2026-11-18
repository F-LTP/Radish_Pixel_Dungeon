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
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ColdIronCoat;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.CountessDress;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PrismArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.SoulMix;

/**
 * 四件新护甲在英雄贴图上的外观行。
 *
 * 与 PrisonArmor、DarkCoat 等带专属外观的护甲同一套机制：外观是英雄贴图里的一整行
 * （行高 15 像素，每行 21 帧），按行号取用，不更换贴图。
 * 只有 hasOutfit() 里的六个职业的贴图画了这四行。各职业贴图的行数并不一样（月华只有 17 行），
 * 对没画的职业用这些行号可能取到贴图之外，所以用行号之前必须先过 hasOutfit()。
 */
public final class ArmorOutfit {

	public static final int ROW_COUNTESS = 18;
	public static final int ROW_COLDIRON = 19;
	public static final int ROW_PRISM    = 20;
	public static final int ROW_SOULMIX  = 21;

	private ArmorOutfit() {}

	/** 贴图里画了 18~21 行的职业。 */
	public static boolean hasOutfit( HeroClass cl ){
		return cl == HeroClasses.WARRIOR
				|| cl == HeroClasses.MAGE
				|| cl == HeroClasses.ROGUE
				|| cl == HeroClasses.HUNTRESS
				|| cl == HeroClasses.RECTOR
				|| cl == HeroClasses.CARDTRICKSTER;
	}

	/** 按贴图路径问同一件事：伪装后显示的是别的职业的贴图，要看的是这张贴图本身有没有画。 */
	public static boolean sheetHasOutfit( String sheetAsset ){
		for (HeroClass cl : HeroClasses.ALL){
			if (cl.spritesheet().equals( sheetAsset )) return hasOutfit( cl );
		}
		return false;
	}

	public static int rowFor( Armor armor ){
		if (armor instanceof CountessDress) return ROW_COUNTESS;
		if (armor instanceof ColdIronCoat) return ROW_COLDIRON;
		if (armor instanceof PrismArmor)   return ROW_PRISM;
		if (armor instanceof SoulMix)      return ROW_SOULMIX;
		return -1;
	}
}
