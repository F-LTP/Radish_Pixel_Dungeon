package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import java.util.Arrays;
import java.util.Collection;

/**
 * 职业管理器 - 集中持有所有职业单例常量、全量职业集合与反序列化查找。
 *
 * <p>本类只是各 {@link HeroClass} 实例的<b>持有者/管理器</b>，与职业的定义、行为分离。
 * 各职业的静态外观、初始装备、天赋层级等由
 * {@link com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition} 承载。</p>
 */
public final class HeroClasses {

	private HeroClasses(){ /* 工具类，禁止实例化 */ }

	//预定义职业实例（替代枚举常量）
	public static final HeroClass WARRIOR   = new HeroClass("WARRIOR");
	public static final HeroClass MAGE      = new HeroClass("MAGE");
	public static final HeroClass ROGUE     = new HeroClass("ROGUE");
	public static final HeroClass HUNTRESS  = new HeroClass("HUNTRESS");
	public static final HeroClass RECTOR    = new HeroClass("RECTOR");
	public static final HeroClass MOONLIGHT = new HeroClass("MOONLIGHT");

	//所有职业集合
	public static final Collection<HeroClass> ALL = Arrays.asList(
			WARRIOR, MAGE, ROGUE, HUNTRESS, RECTOR, MOONLIGHT
	);

	/** 盗贼的可选皮肤：赌徒。数值需大于普通头像皮肤索引。 */
	public static final int ROGUE_SKIN_GAMBLER = 5;
	public static final int ROGUE_SKIN_AGENT = 7;

	/** 战士的可选皮肤：流浪者。数值需大于普通头像皮肤索引。 */
	public static final int WARRIOR_SKIN_WANDERER = 5;

	/** 月华的可选皮肤：圆球。数值需大于普通头像皮肤索引。 */
	public static final int MOONLIGHT_SKIN_SPHERE = 5;

	/** 全职业共享皮肤：杂散。数值需大于普通头像皮肤索引。 */
	public static final int JUMBLE = 6;

	//存档反序列化
	public static HeroClass fromSaveName(String name){
		for (HeroClass cls : ALL){
			if (cls.name().equals(name)) return cls;
		}
		return WARRIOR;
	}
}
