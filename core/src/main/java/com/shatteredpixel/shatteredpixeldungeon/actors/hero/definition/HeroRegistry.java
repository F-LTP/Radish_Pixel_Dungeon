package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.heroes.Huntress;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.heroes.Mage;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.heroes.Moonlight;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.heroes.Rector;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.heroes.Rogue;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.heroes.Warrior;

import java.util.HashMap;
import java.util.Map;

/**
 * 职业注册中心 - 游戏启动时调用 {@link #initAll()}。
 */
public class HeroRegistry {

	private static final Map<HeroClass, HeroDefinition> heroes = new HashMap<>();
	private static boolean initialized = false;

	public static void register(HeroDefinition def) {
		heroes.put(def.heroClass(), def);
		def.register();
	}

	public static HeroDefinition get(HeroClass cls) {
		ensureInit();
		return heroes.get(cls);
	}

	private static void ensureInit() {
		if (!initialized){
			initialized = true;
			initAll();
		}
	}

	public static void initAll() {
		register(new Warrior());
		register(new Mage());
		register(new Rogue());
		register(new Huntress());
		register(new Rector());
		register(new Moonlight());
	}
}
