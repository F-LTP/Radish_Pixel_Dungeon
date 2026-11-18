package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.heroes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Pulse;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.engineer.Detonator;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.engineer.ForceField;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.engineer.SummoningBeacon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.TalentSet;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MultiTool;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;

/** 工程师 - 自 reXuns Pixel Dungeon (xun分支) 移植 */
public class Engineer extends HeroDefinition {

	@Override
	public HeroClass heroClass() {
		return HeroClasses.ENGINEER;
	}

	@Override
	public String spritesheet() {
		return Assets.Sprites.H_ENGINEER;
	}

	@Override
	public String splashArt() {
		//工程师还没有自己的立绘。原来指向的 newhero.jpg 是 reXun 作者给他自己的游戏征集立绘的启事（带他的邮箱），
		//不能出现在萝卜的选人界面上：先用下水道的区域图顶替，有了立绘再换
		return Assets.Splashes.SEWERS;
	}

	@Override
	public String avatarSkin() {
		return Assets.Sprites.AVATARS_ENGINEER;
	}

	@Override
	public HeroSubClass[] subClasses() {
		return new HeroSubClass[]{ HeroSubClasses.CRAFTSMAN, HeroSubClasses.HACKER, HeroSubClasses.GRENADIER };
	}

	@Override
	public ArmorAbility[] armorAbilities() {
		return new ArmorAbility[]{ new ForceField(), new SummoningBeacon(), new Detonator() };
	}

	@Override
	public TalentSet talents() {
		TalentSet set = new TalentSet();
		set.setTier1(new Talent[]{
				Talent.KEEN_MEAL, Talent.FINE_INTUITION, Talent.TESTED_MAINTENANCE,
				Talent.GENERAL_DISARM, Talent.PULSE_ENERGY });
		set.setTier2(new Talent[]{
				Talent.TOILSOME_MEAL, Talent.IONIC_LIQUID, Talent.STRONG_PULSE,
				Talent.RESONANT_SENSING, Talent.APART_ANYTHING, Talent.REMOTE_DESTRUCTION });
		set.setTier3(new Talent[]{
				Talent.DURABLE_MODIFIES, Talent.ELECTRONIC_REPAIR, Talent.IONIZING_RADIATION });
		set.setTier4(new Talent[0]);
		return set;
	}

	@Override
	protected void initClassLoadout(Hero hero) {
		(hero.belongings.weapon = new MultiTool()).identify();
		hero.belongings.weapon.activate(hero);
		Dungeon.quickslot.setSlot(0, hero.belongings.weapon);
		ActionIndicator.setAction(Buff.affect(hero, Pulse.class));

		new PotionOfParalyticGas().identify();
		new ScrollOfRecharging().identify();
	}
}
