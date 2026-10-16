package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;

/**
 * 天赋集合 - 存放职业级的各层天赋。
 *
 * <p>注意：子职业的天赋（T3/T4）与护甲技能天赋（T4）分别由
 * {@link com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass} 与
 * {@link com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility#talents()}
 * 提供，不在本类中。</p>
 */
public class TalentSet {

	private Talent[] tier1;
	private Talent[] tier2;
	//职业通用 T3
	private Talent[] tier3;
	//职业通用 T4
	private Talent[] tier4;

	public TalentSet() {
	}

	public Talent[] getTier1() { return tier1; }
	public TalentSet setTier1(Talent[] t) { tier1 = t; return this; }

	public Talent[] getTier2() { return tier2; }
	public TalentSet setTier2(Talent[] t) { tier2 = t; return this; }

	public Talent[] getTier3() { return tier3; }
	public TalentSet setTier3(Talent[] t) { tier3 = t; return this; }

	public Talent[] getTier4() { return tier4; }
	public TalentSet setTier4(Talent[] t) { tier4 = t; return this; }
}
