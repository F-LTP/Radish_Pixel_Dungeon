package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;

/**
 * 皮肤变体定义基类 - 每个皮肤是一个"类英雄"的变体，本质上是基础职业
 * {@link HeroDefinition} 的一个特化子类。
 *
 * <p><b>共享转职</b>：皮肤与基础职业共享相同的转职选项（{@link #subClasses()}）
 * 与装甲技能（{@link #armorAbilities()}），默认全部委托给基础定义。</p>
 *
 * <p><b>一切可覆盖</b>：若未显式指明则全部继承基础职业；如需自定义，
 * 可覆盖名称（{@link #heroName()}）、初始物品（{@link #initClassLoadout(Hero)}）、
 * 天赋（{@link #talents()}）、外观（{@link #spritesheet()} 等）与精灵渲染参数。</p>
 */
public abstract class SkinDefinition extends HeroDefinition {

	private final HeroDefinition base;

	protected SkinDefinition(HeroDefinition base) {
		this.base = base;
	}

	public HeroDefinition base() { return base; }

	//共享身份：皮肤属于同一个职业
	@Override public HeroClass heroClass() { return base.heroClass(); }

	//外观：默认复用基础职业贴图，可覆盖
	@Override public String spritesheet() { return base.spritesheet(); }
	@Override public String splashArt() { return base.splashArt(); }
	@Override public String avatarSkin() { return base.avatarSkin(); }

	//结构：默认共享转职与装甲技能，可覆盖
	@Override public HeroSubClass[] subClasses() { return base.subClasses(); }
	@Override public ArmorAbility[] armorAbilities() { return base.armorAbilities(); }

	//天赋：默认共享基础职业天赋，可覆盖
	@Override public TalentSet talents() { return base.talents(); }

	//初始物品：默认沿用基础职业，可覆盖
	@Override protected void initClassLoadout(Hero hero) { base.initClassLoadout(hero); }
	@Override protected boolean grantsVelvetPouch() { return base.grantsVelvetPouch(); }

	//可选：默认委托基础职业
	@Override public boolean isUnlocked() { return base.isUnlocked(); }
	@Override public int[] baseHPGrowth() { return base.baseHPGrowth(); }
	@Override public int sneakRadius() { return base.sneakRadius(); }
	@Override public void onExpGain(Hero hero, int exp) { base.onExpGain(hero, exp); }

	//皮肤身份：全局皮肤索引（用于 GetSkin/SetSkin 持久化）与显示名
	public abstract int skinIndex();

	public abstract String skinName();

	@Override public String heroName() {
		String key = "name";
		if (Messages.isAvailable(getClass(), key)) return Messages.get(getClass(), key);
		return base.heroName();
	}

	/** 皮肤长描述：优先使用皮肤独立的 {@code desc} 键，缺失时回退到基础职业。 */
	@Override public String heroDesc() {
		String key = "desc";
		if (Messages.isAvailable(getClass(), key)) return Messages.get(getClass(), key);
		return base.heroDesc();
	}

	/** 皮肤短描述：优先使用皮肤独立的 {@code desc_short} 键，缺失时回退到基础职业。 */
	@Override public String heroShortDesc() {
		String key = "desc_short";
		if (Messages.isAvailable(getClass(), key)) return Messages.get(getClass(), key);
		return base.heroShortDesc();
	}

	/** 皮肤解锁提示：优先使用皮肤独立的 {@code unlock} 键，缺失时回退到基础职业。 */
	@Override public String heroUnlockMsg() {
		String key = "unlock";
		if (Messages.isAvailable(getClass(), key)) return Messages.get(getClass(), key);
		return base.heroUnlockMsg();
	}
}
