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

package com.shatteredpixel.shatteredpixeldungeon.items.spells;


import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector.Belief;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector.FaithObstruction;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public abstract class Spell extends Item {
	
	public static final String AC_CAST = "CAST";

	//affects how strongly on-scroll talents trigger from this scroll
	protected float talentFactor = 1;
	//the chance (0-1) of whether on-scroll talents trigger from this potion
	protected float talentChance = 1;
	
	{
		stackable = true;
		defaultAction = AC_CAST;
	}
	
	@Override
	public ArrayList<String> actions(Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_CAST );
		return actions;
	}
	
	@Override
	public void execute( final Hero hero, String action ) {

		// func 4 Muramasa mania
		// Item.execute 里的 return 只退出基类方法：这里和 Potion / Scroll / InventoryStone 一样再拦一次，
		// 否则会带着旧的 curUser / curItem（可能是 null）继续往下施放
		if (hero.buff(com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.Muramasa.MuramasaMania.class) != null){
			GLog.n(Messages.get(com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.Muramasa.MuramasaMania.class,"mania"));
			return;
		}
		
		super.execute( hero, action );
		
		if (action.equals( AC_CAST )) {

			if (curUser.buff(MagicImmune.class) != null){
				GLog.w( Messages.get(this, "no_magic") );
				return;
			}
			
			onCast( hero );
			
		}
	}

	/**
	 * 一个法术确实被消耗掉之后，由各个消耗点调用。
	 * 按下施放时大多数法术只是打开选格 / 选物品的界面，那一刻还不算用掉：
	 * 取消施放、被魔法免疫挡下都不能结算下面这些，否则站着反复「施放再取消」就能白拿。
	 */
	public static void onSpellUsed( Hero hero, Spell used ){
		if (hero == null || used == null || !hero.isAlive()) return;

		// 牧师：信仰阻断，与法杖一致，真正用掉才算
		if (hero.heroClass == HeroClasses.RECTOR
				&& hero.buff(FaithObstruction.class) == null
				&& !hero.hasTalent(Talent.SMART_BLESSING)){
			Buff.affect(hero, FaithObstruction.class, FaithObstruction.DURATION);
		}

		// 魔武士：使用法力结晶叠加附魔强化
		com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.MagicWarrior.onMagicCrystalUsed( hero );

		// 灵活信仰 +3：有概率把刚用掉的法术还回来一个（注魔 / 诅咒结晶除外）。
		// 还的必须是新建的单个物品：把背包里那一叠本身丢到地上，同一个对象就同时在背包和地上，
		// 读档后变成两叠，再触发一次还会自己并自己、数量归零。
		if (hero.pointsInTalent(Talent.SMART_BLESSING) >= 3
				&& !(used instanceof MagicalInfusion) && !(used instanceof CurseInfusion)
				&& Random.Float() <= 0.33f){
			Item refund = Reflection.newInstance( used.getClass() );
			if (refund != null){
				GLog.p( Messages.get(Spell.class, "smart_blessing", refund.name()) );
				if (!refund.collect( hero.belongings.backpack )){
					Dungeon.level.drop( refund, hero.pos ).sprite.drop();
				}
			}
		}
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	protected abstract void onCast(Hero hero );
	
}
